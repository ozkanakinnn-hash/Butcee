'use strict';

/**
 * X (Twitter) bülteni için haber paketi hazırlar ve Make (make.com) webhook'una gönderir.
 *
 * Günde 4 akış (İstanbul saati, UTC+3 — Türkiye'de yaz saati yok):
 *   sabah 07:30  → önceki gün 23:30'dan beri
 *   ogle  12:30  → 07:30'dan beri
 *   aksam 18:30  → 12:30'dan beri
 *   gece  23:30  → 18:30'dan beri; gün yoğunsa (DENSE_DAY_THRESHOLD) "Günün Bülteni" modu açılır
 *
 * Bu modül ham listeyi küçültür (aynı olayı anlatan kaynakları tek "olay"da toplar) ve paylaşılan konuların
 * hafızasını Firestore `bulletin_topics` koleksiyonunda tutar. Metni src/ai.js (ücretsiz yapay zekâ) yazar;
 * Make yalnızca X'te paylaşır. Hafta sonu (Cmt/Paz) bülten gönderilmez.
 */

const { foldForSearch } = require('./text');

const TZ_OFFSET_MS = 3 * 3600 * 1000; // Europe/Istanbul, sabit UTC+3

const SLOTS = {
  sabah: { label: 'Sabah Akışı', emoji: '🌅', end: [7, 30], start: [23, 30], startPrevDay: true },
  ogle: { label: 'Öğle Akışı', emoji: '☀️', end: [12, 30], start: [7, 30] },
  aksam: { label: 'Akşam Akışı', emoji: '🌆', end: [18, 30], start: [12, 30] },
  gece: { label: 'Gece Akışı', emoji: '🌙', end: [23, 30], start: [18, 30] },
  // Cumartesi ve pazar günde bir kez. Cmt: cuma 23:30 → 20:30; Paz: cmt 20:30 → 20:30.
  // Pazartesi sabah akışı pazar 20:30'dan başlar, böylece arada haber kaybolmaz.
  haftasonu: { label: 'Hafta Sonu Akışı', emoji: '🗓️', end: [20, 30], start: [23, 30], startPrevDay: true, weekend: true },
};

const WEEKEND_END = SLOTS.haftasonu.end;

/** Gün içi toplam haber (olay) sayısı bunu aşarsa gece akışı "Günün Bülteni" olur. */
const DENSE_DAY_THRESHOLD = 40;
/** Yapay zekâya gönderilecek en fazla olay sayısı (jeton/maliyet sınırı). */
const MAX_EVENTS = 60;

/** Şu anki saate göre en yakın akışı seçer (zamanlayıcı birkaç dakika geç kalabilir). */
function slotForTime(now = new Date()) {
  const local = new Date(now.getTime() + TZ_OFFSET_MS);
  const minutes = local.getUTCHours() * 60 + local.getUTCMinutes();
  let best = 'sabah';
  let bestDiff = Infinity;
  const weekend = isWeekend(now);
  if (weekend) return 'haftasonu';
  for (const [key, s] of Object.entries(SLOTS)) {
    if (s.weekend) continue;
    const target = s.end[0] * 60 + s.end[1];
    let diff = Math.abs(minutes - target);
    diff = Math.min(diff, 1440 - diff);
    if (diff < bestDiff) {
      bestDiff = diff;
      best = key;
    }
  }
  return best;
}

/** İstanbul yerel tarihinde (y, m, d, saat, dakika) → UTC Date. */
function istanbulDate(y, m, d, hh, mm) {
  return new Date(Date.UTC(y, m, d, hh, mm) - TZ_OFFSET_MS);
}

/** Akışın kapsadığı zaman aralığı ve günün başlangıcı. */
function slotWindow(slot, now = new Date()) {
  const s = SLOTS[slot];
  if (!s) throw new Error(`Bilinmeyen akış: ${slot} (sabah | ogle | aksam | gece | haftasonu)`);
  const local = new Date(now.getTime() + TZ_OFFSET_MS);
  let y = local.getUTCFullYear();
  let m = local.getUTCMonth();
  let d = local.getUTCDate();
  // Gece yarısından sonra elle çalıştırılan "gece" akışı önceki güne aittir.
  if (slot === 'gece' && local.getUTCHours() < 6) {
    const prev = new Date(Date.UTC(y, m, d - 1));
    [y, m, d] = [prev.getUTCFullYear(), prev.getUTCMonth(), prev.getUTCDate()];
  }
  const end = istanbulDate(y, m, d, s.end[0], s.end[1]);
  let start = istanbulDate(y, m, d - (s.startPrevDay ? 1 : 0), s.start[0], s.start[1]);
  const dow = new Date(Date.UTC(y, m, d)).getUTCDay();
  // Pazar hafta sonu akışı ve pazartesi sabah akışı, bir önceki hafta sonu akışının bittiği 20:30'dan başlar.
  if ((slot === 'haftasonu' && dow === 0) || (slot === 'sabah' && dow === 1)) {
    start = istanbulDate(y, m, d - 1, WEEKEND_END[0], WEEKEND_END[1]);
  }
  const dayStart = istanbulDate(y, m, d, 0, 0);
  const dateLabel = new Intl.DateTimeFormat('tr-TR', {
    timeZone: 'Europe/Istanbul', day: 'numeric', month: 'long', year: 'numeric',
  }).format(end);
  return { start, end: end > now ? now : end, dayStart, dateLabel };
}

const STOP = new Set(['ve', 'ile', 'bir', 'bu', 'da', 'de', 'icin', 'olarak', 'son', 'dakika', 'iste', 'mi', 'mu', 'o', 'en', 'cok']);

/** Başlıktaki anlamlı kelimeler (Türkçe karakterden bağımsız). */
function titleTokens(title) {
  return new Set(
    foldForSearch(title)
      .split(/[^a-z0-9]+/)
      .filter((w) => w.length > 2 && !STOP.has(w))
      .map((w) => w.slice(0, 6)) // kaba kök: "gözaltına" ≈ "gözaltı"
  );
}

function jaccard(a, b) {
  if (a.size === 0 || b.size === 0) return 0;
  let inter = 0;
  for (const x of a) if (b.has(x)) inter++;
  return inter / (a.size + b.size - inter);
}

/**
 * Aynı olayı anlatan haberleri gruplar. Girdi en yeniden eskiye sıralı olmalı.
 * Çıktı: [{ title, summary, sources[], category, url, published_at, breaking, count }]
 */
function groupEvents(articles, threshold = 0.45) {
  const events = [];
  for (const a of articles) {
    const tokens = titleTokens(a.title);
    const match = events.find((e) => jaccard(e.tokens, tokens) >= threshold);
    if (match) {
      match.count++;
      if (a.source && !match.sources.includes(a.source)) match.sources.push(a.source);
      if (!match.summary && a.summary) match.summary = a.summary;
      match.breaking = match.breaking || a.breaking;
      for (const t of tokens) match.tokens.add(t);
    } else {
      events.push({ ...a, sources: a.source ? [a.source] : [], count: 1, tokens: new Set(tokens) });
    }
  }
  return events.map(({ tokens, source, ...e }) => e);
}

/** Önem sırası: son dakika > çok kaynakta geçen > yeni. */
function rankEvents(events) {
  return [...events].sort(
    (a, b) =>
      Number(b.breaking) - Number(a.breaking) ||
      b.count - a.count ||
      String(b.published_at).localeCompare(String(a.published_at))
  );
}

/** Firestore belgesini yapay zekâya gidecek sade nesneye çevirir. */
function toPlainArticle(doc) {
  const d = typeof doc.data === 'function' ? doc.data() : doc;
  const cat = d.category_id;
  const ts = d.published_at;
  return {
    title: d.title || '',
    summary: d.summary || '',
    source: d.source_name || '',
    category: cat && typeof cat === 'object' && cat.id ? cat.id : cat || '',
    url: d.original_url || '',
    published_at: ts && typeof ts.toDate === 'function' ? ts.toDate().toISOString() : String(ts || ''),
    breaking: Boolean(d.breaking_at),
  };
}

/** Webhook'a gönderilecek paketi kurar (saf fonksiyon — testlerde kullanılır). */
function buildPayload({ slot, window, slotArticles, dayEventCount, appLink = '' }) {
  const s = SLOTS[slot];
  const events = rankEvents(groupEvents(slotArticles)).slice(0, MAX_EVENTS);
  const dense = slot === 'gece' && dayEventCount >= DENSE_DAY_THRESHOLD;
  return {
    slot,
    slot_label: dense ? 'Günün Bülteni' : s.label,
    emoji: s.emoji,
    date: window.dateLabel,
    window_start: window.start.toISOString(),
    window_end: window.end.toISOString(),
    daily_recap: dense, // true → gece akışında "Günün öne çıkanları" bölümü de yazılır
    day_event_count: dayEventCount,
    event_count: events.length,
    app_link: appLink,
    events,
  };
}

async function fetchArticles(db, start, end) {
  const snap = await db
    .collection('articles')
    .where('published_at', '>=', start)
    .where('published_at', '<', end)
    .orderBy('published_at', 'desc')
    .limit(400)
    .get();
  return snap.docs.map(toPlainArticle);
}

/** Firestore'dan okuyup paketi hazırlar. */
async function collectBulletin(db, { slot, now = new Date(), appLink = '' } = {}) {
  const chosen = slot || slotForTime(now);
  const window = slotWindow(chosen, now);
  const slotArticles = await fetchArticles(db, window.start, window.end);
  let dayEventCount = groupEvents(slotArticles).length;
  if (chosen === 'gece') {
    dayEventCount = groupEvents(await fetchArticles(db, window.dayStart, window.end)).length;
  }
  return buildPayload({ slot: chosen, window, slotArticles, dayEventCount, appLink });
}

/** İstanbul saatine göre cumartesi/pazar mı? (hafta sonu yalnızca 'haftasonu' akışı çalışır) */
function isWeekend(now = new Date()) {
  const day = new Date(now.getTime() + TZ_OFFSET_MS).getUTCDay();
  return day === 0 || day === 6;
}

const TOPICS = 'bulletin_topics';
/** Pazartesi sabahı cuma akşamının konularını da görmek için 4 günlük hafıza. */
const HISTORY_HOURS = 96;

/** Firestore belge kimliği olarak güvenli konu anahtarı. */
function topicId(key) {
  return foldForSearch(String(key)).replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 120);
}

/** Son paylaşılan konular (tekrar kontrolü için yapay zekâya verilir). */
async function loadHistory(db, now = new Date(), hours = HISTORY_HOURS) {
  const since = new Date(now.getTime() - hours * 3600 * 1000);
  const snap = await db.collection(TOPICS).where('son_paylasim', '>=', since).orderBy('son_paylasim', 'desc').limit(200).get();
  return snap.docs.map((d) => ({ konu_anahtari: d.id, son_durum: d.get('son_durum') || '' }));
}

/** Paylaşılan konuları kaydeder (yalnızca gönderi başarıyla atıldıktan sonra çağrılır). */
async function saveTopics(db, konular, { slot, now = new Date() } = {}) {
  const { FieldValue } = require('firebase-admin/firestore');
  const batch = db.batch();
  let n = 0;
  for (const k of konular) {
    const id = topicId(k.konu_anahtari);
    if (!id) continue;
    batch.set(
      db.collection(TOPICS).doc(id),
      {
        son_durum: String(k.son_durum || '').slice(0, 300),
        son_paylasim: now,
        son_akis: slot || '',
        paylasim_sayisi: FieldValue.increment(1),
        ...(k.durum === 'yeni' ? { ilk_paylasim: now } : {}),
      },
      { merge: true }
    );
    n++;
  }
  if (n) await batch.commit();
  return n;
}

/** 14 günden eski konu kayıtlarını siler. */
async function cleanupTopics(db, now = new Date()) {
  const cutoff = new Date(now.getTime() - 14 * 86400 * 1000);
  const snap = await db.collection(TOPICS).where('son_paylasim', '<', cutoff).limit(400).get();
  if (snap.empty) return 0;
  const batch = db.batch();
  snap.docs.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  return snap.size;
}

/** Paketi Make webhook'una gönderir. Make'in "Accepted" yanıtını döner. */
async function sendToMake(url, payload, fetchImpl = fetch) {
  const res = await fetchImpl(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`Make webhook HTTP ${res.status}: ${text.slice(0, 200)}`);
  return text;
}

module.exports = {
  SLOTS,
  DENSE_DAY_THRESHOLD,
  MAX_EVENTS,
  slotForTime,
  slotWindow,
  titleTokens,
  groupEvents,
  rankEvents,
  toPlainArticle,
  buildPayload,
  collectBulletin,
  sendToMake,
  isWeekend,
  topicId,
  loadHistory,
  saveTopics,
  cleanupTopics,
  HISTORY_HOURS,
};
