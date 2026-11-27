'use strict';

/**
 * Make otomasyonu: haberleri Make webhook'una gönderir (Make senaryosu X'e vb. paylaşır).
 * Telegram paylaşımı social.js'te ayrı çalışır; bu modül yalnızca Make'e gider ve kendi durumunu
 * `notify_state/make` belgesinde tutar.
 *
 * Üç tür kayıt gider; her kayıtta `tur` alanı vardır (Make'te Router ile ayrılabilir):
 *   - "son_dakika": son dakika kaynağından gelen taze haber (tek kaynak yeterli)
 *   - "gundem"    : en az `gundem_min_sources` (varsayılan 5) farklı yayıncının verdiği haber
 *   - "bulten"    : sabah 08:30 / akşam 19:00 bülteni (AI özeti + maddeler)
 *
 * Ayarlar `app_meta/social` belgesinde (Telegram ile ortak belge; deploy gerekmez):
 *   { enabled: true, x_enabled: true, x_webhook: "https://hook.eu1.make.com/...", dry_run: false,
 *     send_breaking: true, send_gundem: true, send_bulletin: true,
 *     gundem_min_sources: 5, make_daily_cap: 12, make_gap_min: 30, breaking_gap_min: 10,
 *     skip_categories: ["magazin"], allowed_sources: [] }
 *
 * - `dry_run: true` → hiçbir şey gönderilmez; ne gönderileceği `social_log` koleksiyonuna yazılır.
 * - `test_ping: true` → social.js bağlantı testi sırasında makeSamples() ile her türden birer deneme kaydı gider
 *   (Make'te "Redetermine data structure" ile alanları öğrenir).
 * - Son çalışmanın durumu `make_status` / `make_run_at` alanlarına yazılır (Console'dan izlenir).
 */

const { Timestamp } = require('firebase-admin/firestore');
const { isClickbait, storyTokens, sameStory } = require('./importance');
const { isQuietHour, isCritical } = require('./quiet-hours');

const MAX_AGE_MS = 3 * 3_600_000;
const BREAKING_MAX_AGE_MS = 90 * 60_000;
const BULLETIN_MAX_AGE_MS = 3 * 3_600_000;
const POSTED_KEEP = 300;
const POSTED_T_KEEP = 80;
const X_LIMIT = 280;
const X_URL_LENGTH = 23;
const APP_URL = 'https://haber-68a3c.web.app';

const DEFAULTS = {
  enabled: false, dry_run: true, x_enabled: false, x_webhook: '',
  send_breaking: true, send_gundem: true, send_bulletin: true,
  gundem_min_sources: 5, make_daily_cap: 12, make_gap_min: 30, breaking_gap_min: 10,
  skip_categories: ['magazin'], allowed_sources: [],
};

const webhookUrl = (config) => String(config.webhook || config.x_webhook || '').trim();
const xReady = (config) => config.x_enabled === true && /^https:\/\/[^\s]+$/.test(webhookUrl(config));
const norm = (x) => String(x || '').trim().toLocaleLowerCase('tr-TR');
const list = (v) => (Array.isArray(v) ? v : []);

/** İstanbul tarihine göre gün anahtarı. */
const dayKey = (ms) =>
  new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(ms));

const fit = (text, room) => (text.length <= room ? text : `${text.slice(0, Math.max(0, room - 1)).trimEnd()}…`);

const PREFIX = { son_dakika: '🔴 SON DAKİKA: ', gundem: '📌 ', bulten: '' };

/** X için hazır metin: bağlantı dahil en fazla 280 karakter. */
function xText(story, tur = 'gundem') {
  const n = story.srcs.length;
  const source = [story.source ? `Kaynak: ${story.source}` : '', n >= 2 ? `${n} kaynakta` : ''].filter(Boolean).join(' · ');
  const tail = `\n\n${source}\n`;
  const prefix = PREFIX[tur] || '';
  const room = X_LIMIT - X_URL_LENGTH - tail.length - prefix.length;
  return `${prefix}${fit(story.title, room)}${tail}${story.url}`;
}

/** Bülten için X metni: başlık + AI özeti (ya da maddeler) + bağlantı. */
function bulletinXText(briefing) {
  const head = `${briefing.title || 'Gündem'}\n\n`;
  const link = briefing.link || APP_URL;
  const room = X_LIMIT - X_URL_LENGTH - head.length - 2;
  const body = briefing.ai_summary
    ? briefing.ai_summary
    : list(briefing.items).slice(0, 3).map((i) => `• ${i.title}`).join('\n');
  return `${head}${fit(body, room)}\n\n${link}`;
}

const zaman = (now) =>
  new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', dateStyle: 'short', timeStyle: 'short' }).format(new Date(now));

/** Make webhook'una gönderilecek JSON (haber). */
function xPayload(story, now, extra = {}) {
  const tur = extra.tur || 'gundem';
  return {
    tur,
    zaman: zaman(now),
    baslik: story.title,
    ozet: '',
    kaynak: story.source || '',
    kaynak_sayisi: story.srcs.length,
    kaynaklar: story.srcs.slice(0, 12).join(', '),
    link: story.url,
    gorsel: story.image || '',
    kategori: story.cat || '',
    metin: xText(story, tur),
    haber_id: story.id,
    maddeler: [],
    deneme: false,
    ...extra,
  };
}

/** Make webhook'una gönderilecek JSON (bülten). Alan adları haber kaydıyla aynıdır. */
function bulletinPayload(briefing, now, extra = {}) {
  const items = list(briefing.items);
  const top = items[0] || {};
  return {
    tur: 'bulten',
    zaman: zaman(now),
    baslik: briefing.title || 'Gündem',
    ozet: briefing.ai_summary || '',
    kaynak: [...new Set(items.map((i) => i.source).filter(Boolean))].slice(0, 5).join(', '),
    kaynak_sayisi: items.length,
    kaynaklar: '',
    link: top.url || APP_URL,
    gorsel: top.image || '',
    kategori: '',
    metin: bulletinXText({ ...briefing, link: top.url }),
    haber_id: briefing.id,
    maddeler: items.slice(0, 7).map((i) => ({ baslik: i.title, link: i.url, kaynak: i.source || '', kaynak_sayisi: i.source_count || 0 })),
    deneme: false,
    ...extra,
  };
}

async function webhookSend(url, payload, fetchImpl = fetch) {
  const res = await fetchImpl(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(15_000),
  });
  if (!res.ok) throw new Error(`webhook HTTP ${res.status}`);
}

const tokenKey = (title) => storyTokens(title || '').join(' ');

/** Daha önce (başka kimlikle bile) paylaşılmış hikâye mi? */
function alreadyPosted(story, state) {
  if (list(state.posted).includes(story.id)) return true;
  const t = storyTokens(story.title || '');
  return list(state.posted_t).some((k) => sameStory(String(k).split(' '), t));
}

/**
 * Paylaşılacak haberleri seçer (saf fonksiyon): en fazla bir son dakika + bir gündem.
 * @returns {{picks: Array<{tur: string, story: object}>, nextState: object, reasons: object}}
 */
function pickStories({ stories = [], state = {}, config = DEFAULTS, now = Date.now() }) {
  const today = dayKey(now);
  const allowed = list(config.allowed_sources).map(norm).filter(Boolean);
  const skip = new Set(list(config.skip_categories).map(norm));
  let count = state.day === today ? state.count || 0 : 0;
  const next = {
    ...state, day: today, count,
    posted: list(state.posted), posted_t: list(state.posted_t),
    last_at: state.last_at || 0, last_breaking_at: state.last_breaking_at || 0,
  };
  const picks = [];
  const reasons = {};
  const quiet = isQuietHour(new Date(now));

  const base = stories
    .filter((s) => s.url && s.title && Array.isArray(s.srcs) && s.srcs.length)
    .filter((s) => !isClickbait(s.title))
    .filter((s) => !allowed.length || allowed.includes(norm(s.source)));

  const take = (tur, story) => {
    picks.push({ tur, story });
    count += 1;
    next.count = count;
    next.posted = [...next.posted, story.id].slice(-POSTED_KEEP);
    next.posted_t = [...next.posted_t, tokenKey(story.title)].slice(-POSTED_T_KEEP);
    if (tur === 'son_dakika') next.last_breaking_at = now;
    else next.last_at = now;
  };

  // 1) Son dakika
  if (config.send_breaking === false) reasons.son_dakika = 'kapalı';
  else if (count >= config.make_daily_cap) reasons.son_dakika = 'günlük sınır';
  else if (now - next.last_breaking_at < config.breaking_gap_min * 60_000) reasons.son_dakika = 'aralık';
  else {
    const c = base
      .filter((s) => s.br && now - s.last_ms <= BREAKING_MAX_AGE_MS && !alreadyPosted(s, next))
      .filter((s) => !quiet || isCritical(s.title))
      .sort((a, b) => b.last_ms - a.last_ms || b.srcs.length - a.srcs.length);
    if (c.length) take('son_dakika', c[0]);
    else reasons.son_dakika = 'aday yok';
  }

  // 2) Gündem: çok kaynaklı haber
  const minSources = Math.max(2, Number(config.gundem_min_sources) || DEFAULTS.gundem_min_sources);
  if (config.send_gundem === false) reasons.gundem = 'kapalı';
  else if (count >= config.make_daily_cap) reasons.gundem = 'günlük sınır';
  else if (now - next.last_at < config.make_gap_min * 60_000) reasons.gundem = 'aralık';
  else {
    const c = base
      .filter((s) => s.srcs.length >= minSources && now - s.last_ms <= MAX_AGE_MS && !alreadyPosted(s, next))
      .filter((s) => !skip.has(norm(s.cat)) || s.br)
      .filter((s) => !quiet || (s.br && isCritical(s.title)))
      .sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms);
    if (c.length) take('gundem', c[0]);
    else reasons.gundem = 'aday yok';
  }

  return { picks, nextState: next, reasons };
}

/** Bülten Make'e gönderilmeli mi? (saf fonksiyon) */
function bulletinDue({ briefing, state = {}, config = DEFAULTS, now = Date.now() }) {
  if (config.send_bulletin === false || !briefing || !briefing.id) return false;
  if (briefing.notified !== true || state.bulletin_last === briefing.id) return false;
  const created = briefing.created_ms || (briefing.created_at && briefing.created_at.toMillis && briefing.created_at.toMillis()) || 0;
  return now - created <= BULLETIN_MAX_AGE_MS;
}

/** Bağlantı testi için her türden birer deneme kaydı (social.js test_ping kullanır). */
function makeSamples(now = Date.now()) {
  const sample = {
    id: 'test', title: 'Haber Akışı bağlantı testi', url: APP_URL, source: 'Haber Akışı',
    srcs: ['A', 'B', 'C', 'D', 'E'], image: '', cat: 'gundem',
  };
  const briefing = {
    id: 'test', title: 'Sabah gündemi', ai_summary: 'Deneme özeti: bu kayıt bağlantı testidir.',
    items: [{ title: 'Deneme maddesi', url: APP_URL, source: 'Haber Akışı', source_count: 5 }],
  };
  return [
    xPayload(sample, now, { tur: 'son_dakika', deneme: true, ozet: 'Deneme özeti.' }),
    xPayload(sample, now, { tur: 'gundem', deneme: true, ozet: 'Deneme özeti.' }),
    bulletinPayload(briefing, now, { deneme: true }),
  ];
}

/**
 * Bir tur: ayarları ve hikâyeleri okur, uygun kayıtları Make webhook'una gönderir.
 * @returns {Promise<{status: string, sent?: string[], ...}>}
 */
async function runMake(db, { now = Date.now(), fetchImpl = fetch, summarize } = {}) {
  const cfgRef = db.doc('app_meta/social');
  const config = { ...DEFAULTS, ...((await cfgRef.get()).data() || {}) };
  const url = webhookUrl(config);
  const send = (payload) => webhookSend(url, payload, fetchImpl);
  const summarizeStory = summarize || ((story) => require('./gemini').summarizeStory(story, { db }));

  if (!config.enabled) return { status: 'kapalı' };
  if (!xReady(config)) return { status: 'make kapalı' }; // x_enabled + x_webhook gerekli

  const [newsSnap, stateSnap, briefSnap] = await Promise.all([
    db.doc('notify_state/news').get(),
    db.doc('notify_state/make').get(),
    db.doc('briefings/latest').get(),
  ]);
  let state = stateSnap.data() || {};
  const sent = [];
  const errors = [];

  const deliver = async (payload, logEntry) => {
    if (config.dry_run) {
      await db.collection('social_log').add({ ...logEntry, make: true, dry_run: true, x: payload.metin });
      sent.push(`${payload.tur} (deneme)`);
      return true;
    }
    try {
      await send(payload);
      await db.collection('social_log').add({ ...logEntry, make: true, dry_run: false });
      sent.push(payload.tur);
      return true;
    } catch (err) {
      await db.collection('social_log').add({ ...logEntry, make: true, dry_run: false, errors: [err.message] });
      errors.push(`${payload.tur}: ${err.message}`);
      return false;
    }
  };

  // 1) Bülten (bildirim olarak gönderildiyse bir kez)
  const briefing = briefSnap.exists ? briefSnap.data() : null;
  // X akışları (akis.js) açıksa sabah/akşam bülteni Make'e ayrıca gitmez (aynı içerik iki kez paylaşılmasın).
  const akisReplacesBulletin = config.send_akis === true && config.akis_bulten_birlikte !== true;
  if (!akisReplacesBulletin && bulletinDue({ briefing, state, config, now })) {
    const ok = await deliver(bulletinPayload(briefing, now), {
      at: Timestamp.fromMillis(now), tur: 'bulten', story_id: briefing.id, title: briefing.title || '', url: '', source: '', sources: 0,
    });
    // Hata olursa bir sonraki turda tekrar denenmez (aynı bülten iki kez gitmesin); social_log'da görünür.
    state = { ...state, bulletin_last: briefing.id, bulletin_ok: ok };
  }

  // 2) Son dakika + gündem
  const { picks, nextState, reasons } = pickStories({
    stories: (newsSnap.data() || {}).stories || [], state, config, now,
  });
  let finalState = nextState;
  for (const { tur, story } of picks) {
    // Haber hâlâ kayıtlı mı? (telif kaldırma talebiyle silinmiş olabilir)
    if (!(await db.doc(`articles/${story.id}`).get()).exists) {
      reasons[tur] = 'haber kaldırılmış';
      finalState = { ...finalState, count: Math.max(0, finalState.count - 1) };
      continue;
    }
    let ozet = '';
    try {
      ozet = await summarizeStory(story);
    } catch {
      // özet olmadan da gönderilir
    }
    await deliver(xPayload(story, now, { tur, ozet }), {
      at: Timestamp.fromMillis(now), tur, story_id: story.id,
      title: story.title, url: story.url, source: story.source || '', sources: story.srcs.length,
    });
  }

  await db.doc('notify_state/make').set(finalState);
  const status = errors.length ? 'hata' : sent.length ? (config.dry_run ? 'deneme' : 'gönderildi') : 'bekle';
  await cfgRef.set(
    {
      make_status: [status, sent.length ? `gönderilen: ${sent.join(', ')}` : '', errors.join(' | '),
        Object.entries(reasons).map(([k, v]) => `${k}: ${v}`).join(', ')].filter(Boolean).join(' — '),
      make_run_at: Timestamp.fromMillis(now),
    },
    { merge: true },
  );
  return { status, sent, ...(errors.length ? { errors } : {}), reasons };
}

module.exports = {
  pickStories, bulletinDue, xText, bulletinXText, xPayload, bulletinPayload, runMake, makeSamples, webhookSend, DEFAULTS,
};
