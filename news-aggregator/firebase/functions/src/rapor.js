'use strict';

/**
 * Günlük Telegram raporu — YALNIZCA sahibin özel sohbetine (app_meta/social.owner_chat_id), kanala gitmez.
 * İçerik (son 24 saat): yapay zekâ ile her kategorinin kısa özeti, sisteme giren haber sayısı (kategori kırılımıyla),
 * uygulamada bildirim olarak giden haber sayısı (türlere göre).
 *
 * fetchRssFeeds (20 dk) içinden çağrılır → ek zamanlayıcı/ücret yok. Her gün `rapor_saat` (vars. 12) TSİ'den sonraki
 * ilk turda bir kez gider. Hemen göndermek için `app_meta/social.rapor_simdi = true` (gönderince false olur).
 *
 * Ayarlar (app_meta/social): rapor_enabled (vars. true), rapor_saat (vars. 12), rapor_simdi.
 * Durum: rapor_status, rapor_run_at. Kayıt: notify_state/rapor.last_day.
 */

const { Timestamp } = require('firebase-admin/firestore');
const { storyTokens, sameStory, isClickbait } = require('./importance');

const TZ_OFFSET_MS = 3 * 3_600_000;
const WINDOW_MS = 24 * 3_600_000;
const TG_LIMIT = 4000;
const TOP_PER_CAT = 6;

const local = (ms) => {
  const d = new Date(ms + TZ_OFFSET_MS);
  return { day: d.toISOString().slice(0, 10), hour: d.getUTCHours() };
};

/** Rapor şimdi gitmeli mi? (saf fonksiyon) */
function reportDue({ now, state = {}, config = {} }) {
  if (config.rapor_enabled === false || !config.owner_chat_id) return false;
  if (config.rapor_simdi === true) return true;
  const L = local(now);
  const hour = Number.isFinite(Number(config.rapor_saat)) ? Number(config.rapor_saat) : 12;
  return L.hour >= hour && state.last_day !== L.day;
}

/** Haberleri kategoriye göre sayar ve her kategoride en çok kaynaklı olayları seçer (saf fonksiyon). */
function summarizeArticles(articles, names = {}) {
  const byCat = new Map();
  for (const a of articles) {
    const cat = a.category || 'diger';
    if (!byCat.has(cat)) byCat.set(cat, { id: cat, name: names[cat] || cat, count: 0, events: [] });
    const c = byCat.get(cat);
    c.count += 1;
    if (!a.title || isClickbait(a.title)) continue;
    const t = storyTokens(a.title);
    const e = c.events.find((x) => sameStory(x.t, t));
    if (e) {
      if (a.source && !e.srcs.includes(a.source)) e.srcs.push(a.source);
    } else c.events.push({ title: a.title, srcs: a.source ? [a.source] : [], t });
  }
  return [...byCat.values()]
    .map((c) => ({
      id: c.id, name: c.name, count: c.count,
      top: c.events.sort((x, y) => y.srcs.length - x.srcs.length).slice(0, TOP_PER_CAT).map((e) => ({ title: e.title, sources: e.srcs.length })),
    }))
    .sort((a, b) => b.count - a.count);
}

/** Bildirim kayıtlarını sayar (saf fonksiyon). */
function countNotifications(docs) {
  const TYPE = {
    article: 'Haber', earthquake: 'Deprem', goal: 'Gol', kickoff: 'Maç başladı', halftime: 'Devre arası',
    second_half: 'İkinci yarı', fulltime: 'Maç sonucu', red_card: 'Kırmızı kart', match: 'Maç', match_live: 'Canlı maç',
  };
  const byType = {};
  let breaking = 0;
  let bulletin = 0;
  for (const n of docs) {
    const label = TYPE[n.type] || n.type || 'diğer';
    byType[label] = (byType[label] || 0) + 1;
    const topics = Array.isArray(n.topics) ? n.topics : [];
    if (n.type === 'article' && topics.some((t) => /son-dakika/.test(t))) breaking += 1;
    if (topics.includes('bulletin')) bulletin += 1;
  }
  const news = byType.Haber || 0;
  return { total: docs.length, news, breaking, bulletin, byType };
}

function reportPrompt(cats) {
  return [
    'Aşağıda son 24 saatte haber uygulamasına giren haberlerin kategorilere göre en önemli başlıkları var.',
    'Her kategori için TEK kısa Türkçe cümleyle (en fazla 140 karakter) günün özetini yaz.',
    'Başlıkları kopyalama, kendi cümlenle yaz. Yalnızca verilen bilgiyi kullan, yorum ekleme. Emoji ve madde işareti yok.',
    'Çıktı yalnızca JSON: {"ozetler":{"<kategori_id>":"cümle", ...}}',
    '',
    JSON.stringify(cats.filter((c) => c.top.length).map((c) => ({ kategori_id: c.id, kategori: c.name, basliklar: c.top.map((t) => t.title) }))),
  ].join('\n');
}

function parseOzetler(text) {
  try {
    const s = String(text || '').replace(/```(?:json)?/gi, '');
    const o = JSON.parse(s.slice(s.indexOf('{'), s.lastIndexOf('}') + 1));
    return o && typeof o.ozetler === 'object' ? o.ozetler : {};
  } catch {
    return {};
  }
}

const fmt = (n) => new Intl.NumberFormat('tr-TR').format(n);

/** Telegram metni (saf fonksiyon, düz metin). */
function reportText({ now, cats, notif, ozetler, aiError = '' }) {
  const when = new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', dateStyle: 'long', timeStyle: 'short' }).format(new Date(now));
  const total = cats.reduce((s, c) => s + c.count, 0);
  const lines = [
    `📊 Haber Akışı günlük rapor — ${when}`,
    '(son 24 saat)',
    '',
    `📥 Sisteme giren haber: ${fmt(total)}`,
    ...cats.map((c) => `   • ${c.name}: ${fmt(c.count)}`),
    '',
    `🔔 Uygulamada giden bildirim: ${fmt(notif.total)}`,
    `   • Haber bildirimi: ${fmt(notif.news)} (son dakika: ${fmt(notif.breaking)}, bülten: ${fmt(notif.bulletin)})`,
    ...Object.entries(notif.byType).filter(([k]) => k !== 'Haber').map(([k, v]) => `   • ${k}: ${fmt(v)}`),
    '',
    '🧠 Kategori özetleri (yapay zekâ)',
  ];
  const withSummary = cats.filter((c) => ozetler[c.id]);
  if (withSummary.length) {
    for (const c of withSummary) lines.push(`▪️ ${c.name}: ${String(ozetler[c.id]).trim()}`);
  } else {
    lines.push(`(özet alınamadı${aiError ? `: ${aiError}` : ''})`);
  }
  let text = lines.join('\n');
  if (text.length > TG_LIMIT) text = `${text.slice(0, TG_LIMIT - 1)}…`;
  return text;
}

/**
 * Bir tur: rapor zamanıysa hazırlar ve sahibin Telegram sohbetine gönderir.
 * @returns {Promise<{status: string}>}
 */
async function runRapor(db, { now = Date.now(), token = '', fetchImpl = fetch, generate, config: cfgIn } = {}) {
  const cfgRef = db.doc('app_meta/social');
  const config = cfgIn || (await cfgRef.get()).data() || {};
  const stateRef = db.doc('notify_state/rapor');
  const state = (await stateRef.get()).data() || {};
  if (!reportDue({ now, state, config })) return { status: 'bekle' };
  if (!token) return { status: 'telegram jetonu yok' };

  const since = Timestamp.fromMillis(now - WINDOW_MS);
  const [artSnap, notifSnap, catSnap] = await Promise.all([
    db.collection('articles').where('created_at', '>=', since).select('title', 'category_id', 'source_name').limit(5000).get(),
    db.collection('notifications').where('sent_at', '>=', since).select('type', 'topics', 'title').limit(5000).get(),
    db.collection('categories').get(),
  ]);
  const names = Object.fromEntries(catSnap.docs.map((d) => [d.id, d.get('name') || d.id]));
  const articles = artSnap.docs.map((d) => {
    const cat = d.get('category_id');
    return { title: d.get('title') || '', source: d.get('source_name') || '', category: cat && cat.id ? cat.id : String(cat || '') };
  });
  const cats = summarizeArticles(articles, names);
  const notif = countNotifications(notifSnap.docs.map((d) => ({ type: d.get('type'), topics: d.get('topics'), title: d.get('title') })));

  let ozetler = {};
  let aiError = '';
  if (cats.some((c) => c.top.length)) {
    const gem = require('./gemini');
    const gen = generate || (async (p) => gem.generate(p, { config: await gem.loadAiConfig(db), fetchImpl, maxOutputTokens: 8192, timeoutMs: 60_000, json: true }));
    const r = await gen(reportPrompt(cats));
    ozetler = parseOzetler(r.text);
    aiError = r.error || '';
  }
  const text = reportText({ now, cats, notif, ozetler, aiError });

  const { telegramSend } = require('./social');
  let status;
  try {
    await telegramSend(fetchImpl, token, config.owner_chat_id, text, { html: false });
    status = 'gönderildi';
    // Elle (rapor_simdi) gönderim günlük 12:00 raporunu engellemez.
    if (config.rapor_simdi !== true) await stateRef.set({ last_day: local(now).day, last_at: now }, { merge: true });
  } catch (err) {
    status = `hata: ${err.message}`;
  }
  await cfgRef.set({
    rapor_status: `${status} (${cats.reduce((s, c) => s + c.count, 0)} haber, ${notif.total} bildirim)`,
    rapor_run_at: Timestamp.fromMillis(now),
    ...(config.rapor_simdi === true ? { rapor_simdi: false } : {}),
  }, { merge: true });
  return { status, text };
}

module.exports = { reportDue, summarizeArticles, countNotifications, reportPrompt, parseOzetler, reportText, runRapor };
