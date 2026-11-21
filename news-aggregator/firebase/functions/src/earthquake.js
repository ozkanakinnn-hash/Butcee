'use strict';

/**
 * Deprem uyarısı (Türkiye ve yakın çevresi).
 *
 * Kaynak: AFAD Deprem Dairesi açık olay servisi (deprem.afad.gov.tr/apiv2). Ulaşılamazsa yedek: EMSC (seismicportal.eu,
 * FDSN). Her iki biçim de toleranslı ayrıştırılır (alan adları/tarih biçimi değişse de çalışsın diye).
 *
 * Bildirim kuralları:
 *  - M ≥ ALERT_MIN  → 'deprem' konusu (yeni uygulama sürümünde "Deprem uyarıları" ayarı, varsayılan açık)
 *  - M ≥ BROAD_MIN  → ayrıca son dakika aboneleri (eski sürümler dahil herkese yakın kitle)
 *  - Gece sessiz saatine takılmaz (kritik bilgi), her olay bir kez gider, 20 dakikadan eski olay gönderilmez.
 * Son depremler `quakes/latest` belgesine yazılır (uygulamadaki "Son depremler" listesi).
 * Durum: `notify_state/earthquake` { sent: [olay kimlikleri] }.
 */

const { Timestamp } = require('firebase-admin/firestore');

const ALERT_MIN = 4.5;
const BROAD_MIN = 5.0;
const LIST_MIN = 3.0;
const MAX_AGE_MS = 20 * 60_000;
const BOX = { minLat: 35.5, maxLat: 42.5, minLon: 25.5, maxLon: 45.0 };
const TOPIC = 'deprem';
const KEEP_SENT = 100;

const AFAD_URL = 'https://deprem.afad.gov.tr/apiv2/event/filter';
const EMSC_URL = 'https://www.seismicportal.eu/fdsnws/event/1/query';

const num = (v) => (v === null || v === undefined || v === '' ? NaN : Number(String(v).replace(',', '.')));
const inBox = (q) => q.lat >= BOX.minLat && q.lat <= BOX.maxLat && q.lon >= BOX.minLon && q.lon <= BOX.maxLon;
const iso = (ms) => new Date(ms).toISOString().slice(0, 19);

/** Saat dilimi yazmayan tarih UTC kabul edilir; sonuç gelecekte kalırsa (yerel saatle yazılmış) 3 saat geri alınır. */
function parseTime(v, now) {
  if (!v) return NaN;
  const s = String(v).trim().replace(' ', 'T');
  let ms = Date.parse(/[zZ]|[+-]\d\d:?\d\d$/.test(s) ? s : `${s}Z`);
  if (ms > now + 5 * 60_000) ms -= 3 * 3_600_000;
  return ms;
}

/** AFAD olay listesi → ortak biçim. */
function parseAfad(json, now = Date.now()) {
  const list = Array.isArray(json) ? json : (json && (json.result || json.data || json.events)) || [];
  return list
    .map((e) => ({
      id: `afad-${e.eventID ?? e.eventId ?? e.id}`,
      mag: num(e.magnitude ?? e.mag),
      lat: num(e.latitude ?? e.lat),
      lon: num(e.longitude ?? e.lon),
      depth: num(e.depth),
      time_ms: parseTime(e.date ?? e.time ?? e.eventDate, now),
      place: [e.province, e.district].filter(Boolean).join(' / ') || e.location || '',
      country: e.country || '',
      source: 'AFAD',
    }))
    .filter((q) => Number.isFinite(q.mag) && Number.isFinite(q.time_ms) && Number.isFinite(q.lat));
}

/** EMSC FDSN JSON → ortak biçim. */
function parseEmsc(json, now = Date.now()) {
  const list = (json && json.features) || [];
  return list
    .map((f) => {
      const p = f.properties || {};
      return {
        id: `emsc-${p.unid || f.id}`,
        mag: num(p.mag),
        lat: num(p.lat),
        lon: num(p.lon),
        depth: num(p.depth),
        time_ms: parseTime(p.time, now),
        place: p.flynn_region ? String(p.flynn_region) : '',
        country: '',
        source: 'EMSC',
      };
    })
    .filter((q) => Number.isFinite(q.mag) && Number.isFinite(q.time_ms) && Number.isFinite(q.lat));
}

/** Son 6 saatin depremleri (AFAD; olmazsa EMSC). @returns {Promise<{quakes, provider, error?}>} */
async function fetchQuakes(fetchImpl = fetch, now = Date.now()) {
  const errors = [];
  try {
    const url = new URL(AFAD_URL);
    url.search = new URLSearchParams({
      start: iso(now - 6 * 3_600_000), end: iso(now + 3 * 3_600_000), minmag: String(LIST_MIN), orderby: 'timedesc', limit: '100',
    });
    const res = await fetchImpl(url, { signal: AbortSignal.timeout(15_000) });
    if (!res.ok) throw new Error(`AFAD HTTP ${res.status}`);
    return { quakes: parseAfad(await res.json(), now), provider: 'AFAD' };
  } catch (err) {
    errors.push(err.message);
  }
  try {
    const url = new URL(EMSC_URL);
    url.search = new URLSearchParams({
      format: 'json', start: iso(now - 6 * 3_600_000), minmag: String(LIST_MIN), limit: '100',
      minlat: String(BOX.minLat), maxlat: String(BOX.maxLat), minlon: String(BOX.minLon), maxlon: String(BOX.maxLon),
    });
    const res = await fetchImpl(url, { signal: AbortSignal.timeout(15_000) });
    if (!res.ok) throw new Error(`EMSC HTTP ${res.status}`);
    return { quakes: parseEmsc(await res.json(), now), provider: 'EMSC', error: errors.join(' | ') };
  } catch (err) {
    errors.push(err.message);
  }
  return { quakes: [], provider: null, error: errors.join(' | ') };
}

const timeTr = (ms) =>
  new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' }).format(new Date(ms));

/** Deprem bildirimi (iç biçim; notify.toWire ile Android veri + iOS uyarısına çevrilir). */
function buildQuakeMessage(q) {
  const mag = q.mag.toFixed(1);
  const depth = Number.isFinite(q.depth) ? ` · ${Math.round(q.depth)} km derinlik` : '';
  const condition = q.mag >= BROAD_MIN ? `'${TOPIC}' in topics || 'news_son-dakika' in topics` : `'${TOPIC}' in topics`;
  return {
    condition,
    notification: {
      title: `🔴 DEPREM · ${mag} büyüklüğünde`,
      body: `${q.place || 'Türkiye'}${depth} · ${timeTr(q.time_ms)}\nKaynak: ${q.source}`,
    },
    data: { type: 'earthquake', quake_id: q.id, magnitude: mag },
    android: { priority: 'high', collapseKey: `quake_${q.id}`, notification: { channelId: 'breaking_news', tag: `quake_${q.id}` } },
  };
}

/** Gönderilecek depremleri seçer (saf fonksiyon). */
function pickAlerts(quakes, sent = [], now = Date.now()) {
  const done = new Set(sent);
  return quakes
    .filter((q) => inBox(q) && q.mag >= ALERT_MIN && now - q.time_ms <= MAX_AGE_MS && now - q.time_ms >= -5 * 60_000)
    .filter((q) => !done.has(q.id))
    .sort((a, b) => a.time_ms - b.time_ms);
}

/** Bir tur: depremleri çeker, listeyi yazar, yeni büyük depremleri bildirir. */
async function runEarthquake(db, messaging, { fetchImpl = fetch, now = Date.now() } = {}) {
  const { toWire } = require('./notify');
  const { quakes, provider, error } = await fetchQuakes(fetchImpl, now);
  if (!provider) return { status: 'kaynak yok', error };
  const local = quakes.filter(inBox).sort((a, b) => b.time_ms - a.time_ms);
  const stateRef = db.doc('notify_state/earthquake');
  const state = (await stateRef.get()).data() || {};
  const alerts = pickAlerts(local, state.sent || [], now);
  let sent = 0;
  if (alerts.length) {
    const res = await messaging.sendEach(alerts.map((q) => toWire(buildQuakeMessage(q))));
    sent = res.successCount;
  }
  const newest = local[0] ? local[0].id : '';
  if (alerts.length || newest !== state.newest) {
    await db.doc('quakes/latest').set({
      provider,
      updated_at: Timestamp.fromMillis(now),
      items: local.slice(0, 30).map((q) => ({ ...q, depth: Number.isFinite(q.depth) ? q.depth : null })),
    });
  }
  await stateRef.set({
    sent: [...(state.sent || []), ...alerts.map((q) => q.id)].slice(-KEEP_SENT),
    newest,
    provider,
    checked_at: Timestamp.fromMillis(now),
    ...(error ? { last_error: error.slice(0, 300) } : { last_error: '' }),
  });
  return { status: 'tamam', provider, count: local.length, alerts: alerts.map((q) => `${q.mag} ${q.place}`), sent };
}

module.exports = { parseAfad, parseEmsc, parseTime, pickAlerts, buildQuakeMessage, fetchQuakes, runEarthquake, ALERT_MIN, BROAD_MIN, TOPIC };
