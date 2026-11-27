'use strict';

/**
 * X akışları: hafta içi günde 4 (sabah/öğle/akşam/gece), cumartesi ve pazar günde 1 (hafta sonu) özet gönderisi.
 * Haberler Gemini ile KENDİ cümlelerimizle özetlenir (telifsiz), mevcut Make webhook'una (`app_meta/social.x_webhook`)
 * `tur: "akis"` olarak gider. Kayıt, diğer türlerle AYNI alanları taşır (`metin` ≤ 280 karakter) → mevcut senaryo
 * (Webhook → Google Tablolar → filtre deneme=false → Buffer → X) değiştirilmeden paylaşır.
 *
 * Tekrar etmeme: paylaşılan konular `bulletin_topics/{konu_anahtari}` belgelerinde tutulur (96 saat yapay zekâya verilir).
 * Bir konuda yeni gelişme yoksa yazılmaz; varsa "🔄" ile yalnızca yeni bilgi yazılır.
 *
 * fetchRssFeeds (20 dk) içinden çağrılır → ek zamanlayıcı/ücret yok. Akış saati geçtikten sonraki ilk turda gider.
 *
 * Ayarlar `app_meta/social` (deploy gerekmez):
 *   send_akis: false (VARSAYILAN KAPALI — açılana kadar hiçbir şey değişmez)
 *   akis_dry_run: true → yalnızca akışlar denenir (gönderilmez, metin social_log'a yazılır); ortak dry_run da geçerli
 *   akis_bulten_birlikte: false → akışlar açıkken eski sabah/akşam "bulten" kaydı Make'e ayrıca gitmez (çift gönderi olmasın)
 * Durum: `akis_status`, `akis_run_at` (aynı belgede).
 */

const { Timestamp, FieldValue } = require('firebase-admin/firestore');
const { storyTokens, sameStory, isClickbait } = require('./importance');
const { fold } = require('./quiet-hours');

const TZ_OFFSET_MS = 3 * 3_600_000; // Europe/Istanbul, sabit UTC+3
const APP_URL = 'https://haber-68a3c.web.app';
const X_LIMIT = 280;
const X_URL_LENGTH = 23;
const DUE_WINDOW_MS = 100 * 60_000; // saat geçtikten sonra en geç 100 dk içinde gönderilir, sonra atlanır
const HISTORY_HOURS = 96;
const TOPICS = 'bulletin_topics';
const MAX_EVENTS = 40;
const DENSE_DAY_THRESHOLD = 40;

const SLOTS = {
  sabah: { label: 'Sabah Akışı', emoji: '🌅', end: [7, 30], start: [23, 30], prevDay: true },
  ogle: { label: 'Öğle Akışı', emoji: '☀️', end: [12, 30], start: [7, 30] },
  aksam: { label: 'Akşam Akışı', emoji: '🌆', end: [18, 30], start: [12, 30] },
  gece: { label: 'Gece Akışı', emoji: '🌙', end: [23, 30], start: [18, 30] },
  haftasonu: { label: 'Hafta Sonu Akışı', emoji: '🗓️', end: [20, 30], start: [23, 30], prevDay: true, weekend: true },
};
const WEEKEND_END = SLOTS.haftasonu.end;

/** İstanbul yerel tarih bileşenleri. */
function local(ms) {
  const d = new Date(ms + TZ_OFFSET_MS);
  return { y: d.getUTCFullYear(), m: d.getUTCMonth(), d: d.getUTCDate(), dow: d.getUTCDay(), min: d.getUTCHours() * 60 + d.getUTCMinutes() };
}
const at = (y, m, d, hh, mm) => Date.UTC(y, m, d, hh, mm) - TZ_OFFSET_MS;
const isWeekendDow = (dow) => dow === 0 || dow === 6;
const dayKeyOf = ({ y, m, d }) => `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

/** Akışın aralığı (ms). Pazar hafta sonu ve pazartesi sabah akışı önceki hafta sonu akışının bittiği 20:30'dan başlar. */
function slotWindow(slot, y, m, d) {
  const s = SLOTS[slot];
  const end = at(y, m, d, s.end[0], s.end[1]);
  let start = at(y, m, d - (s.prevDay ? 1 : 0), s.start[0], s.start[1]);
  const dow = new Date(Date.UTC(y, m, d)).getUTCDay();
  if ((slot === 'haftasonu' && dow === 0) || (slot === 'sabah' && dow === 1)) start = at(y, m, d - 1, WEEKEND_END[0], WEEKEND_END[1]);
  const date = new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', day: 'numeric', month: 'long' }).format(new Date(end));
  return { start, end, dayStart: at(y, m, d, 0, 0), date };
}

/** Şu an gönderilmesi gereken akış (saf fonksiyon). Hafta içi 4 akış, hafta sonu yalnızca 'haftasonu'. */
function dueSlot(now, state = {}) {
  const L = local(now);
  const weekend = isWeekendDow(L.dow);
  for (const [slot, s] of Object.entries(SLOTS)) {
    if (Boolean(s.weekend) !== weekend) continue;
    const endMs = at(L.y, L.m, L.d, s.end[0], s.end[1]);
    if (now < endMs || now - endMs > DUE_WINDOW_MS) continue;
    const id = `${dayKeyOf(L)}-${slot}`;
    if (state.last_id === id) continue;
    return { slot, id, ...slotWindow(slot, L.y, L.m, L.d) };
  }
  return null;
}

/** Aynı olayı anlatan haberleri tek olayda toplar (en yeniden eskiye sıralı girdi). */
function groupEvents(articles) {
  const events = [];
  for (const a of articles) {
    if (!a.title || isClickbait(a.title)) continue;
    const t = storyTokens(a.title);
    const e = events.find((x) => sameStory(x.t, t));
    if (e) {
      if (a.source && !e.kaynaklar.includes(a.source)) e.kaynaklar.push(a.source);
      if (!e.ozet && a.summary) e.ozet = a.summary;
      e.son_dakika = e.son_dakika || a.breaking;
    } else {
      events.push({ baslik: a.title, ozet: a.summary || '', kaynaklar: a.source ? [a.source] : [], kategori: a.category || '', son_dakika: a.breaking, t });
    }
  }
  return events
    .sort((a, b) => Number(b.son_dakika) - Number(a.son_dakika) || b.kaynaklar.length - a.kaynaklar.length)
    .map(({ t, ...e }) => ({ ...e, ozet: String(e.ozet).slice(0, 160), son_dakika: e.son_dakika || undefined }));
}

/** Firestore haber belgesi → sade nesne. */
function plain(doc) {
  const d = doc.data();
  const cat = d.category_id;
  return {
    title: d.title || '', summary: d.summary || '', source: d.source_name || '',
    category: cat && cat.id ? cat.id : String(cat || ''), breaking: Boolean(d.breaking_at),
  };
}

const PROMPT = `Sen tarafsız bir Türkçe haber editörüsün. Verilen haber listesinden X (Twitter) için kısa bir akış özeti hazırlarsın.

TELİF (kesin): Başlığı veya özeti KOPYALAMA; her maddeyi kendi cümlenle baştan yaz. Kaynaktan art arda 5 kelimeden uzun
dizi alma (özel adlar, kurum adları, sayılar hariç). Doğrudan alıntı, yorum, ünlem yok. Listede olmayan bilgi EKLEME.
HUKUK: "gözaltına alındı / tutuklandı / iddia edildi" ayrımını koru, hüküm bildiren sözcük kullanma. Çocuk, mağdur ve
sıradan vatandaş adı yazma. Hakaret/küfür/şiddet ayrıntısını aktarma.
TEKRAR: "DAHA ÖNCE PAYLAŞILAN KONULAR" listesindeki bir konuda YENİ gelişme (karar, sonuç, sayı, tutuklama/tahliye,
açıklama, kabul/ret) varsa maddeyi "🔄 " ile başlatıp yalnızca yeni bilgiyi yaz ve durum "gelisme" ver. Gelişme YOKSA o
konuyu YAZMA, "atlananlar"a ekle. Aynı olayı anlatan haberleri tek maddede birleştir.
konu_anahtari: küçük harf, Türkçe karaktersiz, tireli, 3–6 kelime; önceki listede aynı konu varsa AYNI anahtarı kullan.
SEÇİM: Önem sırasıyla en fazla 6 madde (son dakika ve çok kaynaklı olanlar önce; magazin en fazla 1).
Her "metin" en fazla 90 karakter, tek cümle, emoji yok (gelişmede yalnızca başta "🔄 ").
ÇIKTI yalnızca JSON:
{"maddeler":[{"metin":"...","konu_anahtari":"...","durum":"yeni","son_durum":"tek cümle güncel durum"}],"atlananlar":["..."]}
Paylaşılacak yeni haber veya gelişme yoksa: {"maddeler":[],"atlananlar":[...]}`;

function buildPrompt({ label, date, dense, events, history }) {
  return [
    PROMPT, '',
    `AKIŞ: ${label} — ${date}${dense ? ' (yoğun gün: en önemli maddeleri seç)' : ''}`, '',
    'DAHA ÖNCE PAYLAŞILAN KONULAR:',
    history.length ? history.map((h) => `${h.konu_anahtari} | ${h.son_durum}`).join('\n') : '(yok)', '',
    'YENİ HABERLER (JSON):',
    JSON.stringify(events),
  ].join('\n');
}

/** Model çıktısından JSON (çit ve fazlalık temizlenir). */
function parseJson(text) {
  const s = String(text || '').replace(/```(?:json)?/gi, '');
  const a = s.indexOf('{');
  const b = s.lastIndexOf('}');
  if (a < 0 || b < a) throw new Error('yapay zekâ JSON döndürmedi');
  return JSON.parse(s.slice(a, b + 1));
}

/** Konu anahtarını belge kimliğine çevirir. */
const topicId = (k) => fold(k).replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 120);

/**
 * X metni: başlık + sığdığı kadar madde + uygulama bağlantısı (≤ 280; bağlantı 23 sayılır).
 * @returns {{metin: string, used: object[]}}
 */
function composeText({ emoji, label, date }, maddeler, link = APP_URL) {
  const head = `${emoji} ${label} | ${date}`;
  const room = X_LIMIT - X_URL_LENGTH - 2; // "\n\n" + bağlantı
  let body = head;
  const used = [];
  for (const m of maddeler) {
    const t = String(m.metin || '').trim();
    if (!t) continue;
    const line = t.startsWith('🔄') ? t : `🔸 ${t}`;
    if ((`${body}\n${line}`).length > room) continue; // sığmayan atlanır, daha kısası sığabilir
    body = `${body}\n${line}`;
    used.push(m);
  }
  return { metin: `${body}\n\n${link}`, used };
}

/** Kaynak başlıklarından art arda 6 kelime kopyalanmış mı? */
function copiedPhrases(texts, titles, n = 6) {
  const words = (s) => fold(s).replace(/[^a-z0-9\s]/g, ' ').split(/\s+/).filter(Boolean);
  const grams = new Set();
  for (const t of titles) { const w = words(t); for (let i = 0; i + n <= w.length; i++) grams.add(w.slice(i, i + n).join(' ')); }
  const hits = new Set();
  for (const t of texts) { const w = words(t); for (let i = 0; i + n <= w.length; i++) { const g = w.slice(i, i + n).join(' '); if (grams.has(g)) hits.add(g); } }
  return [...hits];
}

const zaman = (now) => new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', dateStyle: 'short', timeStyle: 'short' }).format(new Date(now));

/** Make'e giden kayıt: mevcut türlerle aynı alanlar (+ akis, uzun_metin). */
function akisPayload({ slot, id, label, emoji, date }, maddeler, metin, now) {
  return {
    tur: 'akis',
    zaman: zaman(now),
    baslik: `${emoji} ${label} | ${date}`,
    ozet: maddeler.map((m) => m.metin).join(' '),
    kaynak: '',
    kaynak_sayisi: maddeler.length,
    kaynaklar: '',
    link: APP_URL,
    gorsel: '',
    kategori: '',
    metin,
    haber_id: id,
    maddeler: maddeler.map((m) => ({ baslik: m.metin, link: APP_URL, kaynak: '', kaynak_sayisi: 0 })),
    deneme: false,
    akis: slot,
    uzun_metin: [`${emoji} ${label} | ${date}`, ...maddeler.map((m) => (m.metin.startsWith('🔄') ? m.metin : `🔸 ${m.metin}`))].join('\n'),
  };
}

async function fetchArticles(db, start, end) {
  const snap = await db.collection('articles')
    .where('published_at', '>=', Timestamp.fromMillis(start))
    .where('published_at', '<', Timestamp.fromMillis(end))
    .orderBy('published_at', 'desc').limit(400).get();
  return snap.docs.map(plain);
}

async function loadHistory(db, now) {
  const snap = await db.collection(TOPICS)
    .where('son_paylasim', '>=', Timestamp.fromMillis(now - HISTORY_HOURS * 3_600_000))
    .orderBy('son_paylasim', 'desc').limit(150).get();
  return snap.docs.map((d) => ({ konu_anahtari: d.id, son_durum: d.get('son_durum') || '' }));
}

async function saveTopics(db, maddeler, slot, now) {
  const batch = db.batch();
  let n = 0;
  for (const m of maddeler) {
    const id = topicId(m.konu_anahtari || m.metin);
    if (!id) continue;
    batch.set(db.collection(TOPICS).doc(id), {
      son_durum: String(m.son_durum || m.metin || '').slice(0, 300),
      son_paylasim: Timestamp.fromMillis(now), son_akis: slot, paylasim_sayisi: FieldValue.increment(1),
      ...(m.durum === 'gelisme' ? {} : { ilk_paylasim: Timestamp.fromMillis(now) }),
    }, { merge: true });
    n += 1;
  }
  if (n) await batch.commit();
  // 14 günden eski konular silinir
  const old = await db.collection(TOPICS).where('son_paylasim', '<', Timestamp.fromMillis(now - 14 * 86_400_000)).limit(200).get();
  if (!old.empty) { const b = db.batch(); old.docs.forEach((d) => b.delete(d.ref)); await b.commit(); }
  return n;
}

/**
 * Bir tur: sırası gelen akış varsa yazar ve Make'e gönderir.
 * @returns {Promise<{status: string, ...}>}
 */
async function runAkis(db, { now = Date.now(), fetchImpl = fetch, generate, config: cfgIn } = {}) {
  const cfgRef = db.doc('app_meta/social');
  const config = cfgIn || (await cfgRef.get()).data() || {};
  if (config.send_akis !== true) return { status: 'kapalı' };
  const url = String(config.webhook || config.x_webhook || '').trim();
  if (config.enabled !== true || config.x_enabled !== true || !/^https:\/\/\S+$/.test(url)) return { status: 'make kapalı' };

  const stateRef = db.doc('notify_state/akis');
  const state = (await stateRef.get()).data() || {};
  const due = dueSlot(now, state);
  if (!due) return { status: 'bekle' };
  const s = SLOTS[due.slot];
  const info = { ...due, label: s.label, emoji: s.emoji };

  const finish = async (status, extra = {}) => {
    await stateRef.set({ last_id: due.id, last_at: now, last_status: status }, { merge: true });
    await cfgRef.set({ akis_status: `${due.id}: ${status}`, akis_run_at: Timestamp.fromMillis(now) }, { merge: true });
    return { status, id: due.id, ...extra };
  };

  const articles = await fetchArticles(db, due.start, Math.min(due.end, now));
  const events = groupEvents(articles);
  if (!events.length) return finish('haber yok');
  let dense = false;
  if (due.slot === 'gece' || due.slot === 'haftasonu') {
    dense = groupEvents(await fetchArticles(db, due.dayStart, Math.min(due.end, now))).length >= DENSE_DAY_THRESHOLD;
  }
  const history = await loadHistory(db, now);

  const gem = require('./gemini');
  const aiConfig = await gem.loadAiConfig(db);
  const gen = generate || ((prompt) => gem.generate(prompt, { config: aiConfig, fetchImpl, maxOutputTokens: 8192, timeoutMs: 60_000, json: true }));
  const r = await gen(buildPrompt({ label: s.label, date: due.date, dense, events: events.slice(0, MAX_EVENTS), history }));
  if (!generate) await gem.recordStatus(db, r);
  if (!r.text) {
    // Saat penceresi içinde bir sonraki turda yeniden denenir (durum kaydı yazılır ama last_id ilerlemez).
    await cfgRef.set({ akis_status: `${due.id}: yapay zekâ hatası — ${r.error}`, akis_run_at: Timestamp.fromMillis(now) }, { merge: true });
    return { status: 'ai hata', error: r.error };
  }
  let out;
  try {
    out = parseJson(r.text);
  } catch (err) {
    await cfgRef.set({ akis_status: `${due.id}: ${err.message}`, akis_run_at: Timestamp.fromMillis(now) }, { merge: true });
    return { status: 'ai hata', error: err.message };
  }
  const maddeler = (Array.isArray(out.maddeler) ? out.maddeler : []).filter((m) => m && m.metin);
  const atlanan = Array.isArray(out.atlananlar) ? out.atlananlar.length : 0;
  if (!maddeler.length) return finish(`yeni gelişme yok (atlanan ${atlanan})`);

  const { metin, used } = composeText(info, maddeler);
  const payload = akisPayload(info, maddeler, metin, now);
  const copied = copiedPhrases(maddeler.map((m) => m.metin), articles.map((a) => a.title));
  const log = {
    at: Timestamp.fromMillis(now), tur: 'akis', story_id: due.id, title: payload.baslik, url: APP_URL, source: '',
    sources: used.length, make: true, x: metin, ...(copied.length ? { kopya_uyari: copied } : {}),
  };

  if (config.dry_run || config.akis_dry_run === true) {
    await db.collection('social_log').add({ ...log, dry_run: true, uzun_metin: payload.uzun_metin });
    return finish(`deneme (${used.length} madde, atlanan ${atlanan})`, { metin });
  }
  try {
    const res = await fetchImpl(url, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify(payload), signal: AbortSignal.timeout(15_000),
    });
    if (!res.ok) throw new Error(`webhook HTTP ${res.status}`);
  } catch (err) {
    await db.collection('social_log').add({ ...log, dry_run: false, errors: [err.message] });
    // Aynı akış iki kez gitmesin diye tekrar denenmez; hata social_log ve akis_status'ta görünür.
    return finish(`hata: ${err.message}`);
  }
  await db.collection('social_log').add({ ...log, dry_run: false });
  const saved = await saveTopics(db, used, due.slot, now);
  return finish(`gönderildi (${used.length} madde, atlanan ${atlanan}, hafıza ${saved})`, { metin });
}

module.exports = {
  SLOTS, dueSlot, slotWindow, groupEvents, buildPrompt, parseJson, composeText, copiedPhrases, akisPayload,
  topicId, runAkis, DUE_WINDOW_MS,
};
