'use strict';

/**
 * Sosyal paylaşım: önemli haberleri X (Twitter) için Make webhook'una gönderir.
 *
 * Ayarlar `app_meta/social` belgesinde (deploy gerekmez):
 *   { enabled: true, x_enabled: true, x_webhook: "https://hook.eu1.make.com/...",
 *     daily_cap: 8, min_gap_min: 45, dry_run: false,
 *     allowed_sources: ["NTV", "Hürriyet"] }
 *
 * - `dry_run: true` → hiçbir şey gönderilmez; ne paylaşılacağı `social_log` koleksiyonuna yazılır.
 * - `test_ping: true` → Make webhook'una bir deneme kaydı gönderir (Make'te "Redetermine data structure"
 *   butonuyla alanları öğrenir), sonucu `test_result` alanına yazar ve kendini kapatır.
 * - `allowed_sources`: doluysa yalnızca bu yayıncıların haberleri paylaşılır.
 */

const { Timestamp } = require('firebase-admin/firestore');
const { isClickbait } = require('./importance');
const { isQuietHour, isCritical } = require('./quiet-hours');

const MIN_SOURCES = 3;
const MAX_AGE_MS = 3 * 3_600_000;
const SKIP_CATEGORIES = new Set(['spor', 'magazin']);
const POSTED_KEEP = 300;
const X_LIMIT = 280;
const X_URL_LENGTH = 23;

const DEFAULTS = {
  enabled: false, dry_run: true, daily_cap: 8, min_gap_min: 45,
  allowed_sources: [], x_enabled: false, x_webhook: '',
};

const xReady = (config) => config.x_enabled === true && /^https:\/\/[^\s]+$/.test(String(config.x_webhook || ''));
const norm = (x) => String(x || '').trim().toLocaleLowerCase('tr-TR');

/** İstanbul tarihine göre gün anahtarı. */
const dayKey = (ms) =>
  new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(ms));

/** X için hazır metin: bağlantı dahil en fazla 280 karakter. */
function xText(story) {
  const n = story.srcs.length;
  const source = [story.source ? `Kaynak: ${story.source}` : '', n >= 2 ? `${n} kaynakta` : ''].filter(Boolean).join(' · ');
  const tail = `\n\n${source}\n`;
  const room = X_LIMIT - X_URL_LENGTH - tail.length;
  const title = story.title.length <= room ? story.title : `${story.title.slice(0, room - 1).trimEnd()}…`;
  return `${title}${tail}${story.url}`;
}

/** Make webhook'una gönderilecek JSON. */
function xPayload(story, now, extra = {}) {
  return {
    zaman: new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', dateStyle: 'short', timeStyle: 'short' }).format(new Date(now)),
    baslik: story.title,
    kaynak: story.source || '',
    kaynak_sayisi: story.srcs.length,
    link: story.url,
    metin: xText(story),
    haber_id: story.id,
    deneme: false,
    ...extra,
  };
}

async function webhookSend(url, payload) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(15_000),
  });
  if (!res.ok) throw new Error(`webhook ${res.status}`);
}

/**
 * Paylaşılacak haber seçimi (saf fonksiyon).
 * @returns {{story: object|null, nextState: object, reason: string}}
 */
function pickStory({ stories = [], state = {}, config = DEFAULTS, now = Date.now() }) {
  const today = dayKey(now);
  const allowed = (Array.isArray(config.allowed_sources) ? config.allowed_sources : []).map(norm).filter(Boolean);
  const count = state.day === today ? state.count || 0 : 0;
  const posted = state.posted || [];
  const base = { day: today, count, last_at: state.last_at || 0, posted };

  if (count >= config.daily_cap) return { story: null, nextState: base, reason: 'günlük sınır' };
  if (now - (state.last_at || 0) < config.min_gap_min * 60_000) return { story: null, nextState: base, reason: 'aralık' };

  const candidates = stories
    .filter((s) => s.url && s.title && Array.isArray(s.srcs) && s.srcs.length >= MIN_SOURCES)
    .filter((s) => now - s.last_ms <= MAX_AGE_MS && !posted.includes(s.id))
    .filter((s) => !isClickbait(s.title) && (s.br || !SKIP_CATEGORIES.has(s.cat)))
    .filter((s) => !allowed.length || allowed.includes(norm(s.source)))
    .filter((s) => !isQuietHour(new Date(now)) || (s.br && isCritical(s.title)))
    .sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms);

  if (!candidates.length) return { story: null, nextState: base, reason: 'aday yok' };
  const story = candidates[0];
  return {
    story,
    reason: 'seçildi',
    nextState: { day: today, count: count + 1, last_at: now, posted: [...posted, story.id].slice(-POSTED_KEEP) },
  };
}

/**
 * Bir tur: ayarları ve hikâyeleri okur, uygunsa bir haber Make webhook'una gönderir.
 * @returns {Promise<{status: string, ...}>}
 */
async function runSocial(db, { now = Date.now() } = {}) {
  const config = { ...DEFAULTS, ...((await db.doc('app_meta/social').get()).data() || {}) };

  // Bağlantı testi
  if (config.test_ping === true) {
    const errors = [];
    if (xReady(config)) {
      const sample = { id: 'test', title: 'Haber Akışı bağlantı testi', url: 'https://haber-68a3c.web.app', source: 'Haber Akışı', srcs: ['test'] };
      try {
        await webhookSend(config.x_webhook, xPayload(sample, now, { deneme: true }));
      } catch (err) {
        errors.push(`webhook: ${err.message}`);
      }
    } else {
      errors.push('x_webhook ayarı eksik veya x_enabled: false');
    }
    await db.doc('app_meta/social').set(
      { test_ping: false, test_result: errors.length ? errors.join(' | ') : 'başarılı', test_at: Timestamp.fromMillis(now) },
      { merge: true },
    );
    return { status: 'test', ...(errors.length ? { errors } : {}) };
  }

  if (!config.enabled) return { status: 'kapalı' };
  if (!xReady(config) && !config.dry_run) return { status: 'ayar eksik (x_webhook)' };

  const [newsSnap, stateSnap] = await Promise.all([
    db.doc('notify_state/news').get(),
    db.doc('notify_state/social').get(),
  ]);
  const { story, nextState, reason } = pickStory({
    stories: (newsSnap.data() || {}).stories || [],
    state: stateSnap.data() || {},
    config,
    now,
  });
  if (!story) return { status: 'bekle', reason };

  // Haber hâlâ kayıtlı mı? (telif kaldırma talebiyle silinmiş olabilir)
  if (!(await db.doc(`articles/${story.id}`).get()).exists) {
    await db.doc('notify_state/social').set({ ...nextState, count: nextState.count - 1, last_at: stateSnap.get('last_at') || 0 });
    return { status: 'haber kaldırılmış', id: story.id };
  }

  const entry = {
    at: Timestamp.fromMillis(now), story_id: story.id,
    title: story.title, url: story.url, source: story.source || '', sources: story.srcs.length,
  };

  if (config.dry_run) {
    await db.collection('social_log').add({ ...entry, dry_run: true, x: xText(story) });
    await db.doc('notify_state/social').set(nextState);
    return { status: 'deneme', title: story.title };
  }

  try {
    await webhookSend(config.x_webhook, xPayload(story, now));
    await db.doc('notify_state/social').set(nextState);
    await db.collection('social_log').add({ ...entry, dry_run: false });
    return { status: 'gönderildi', title: story.title };
  } catch (err) {
    await db.collection('social_log').add({ ...entry, dry_run: false, errors: [err.message] });
    return { status: 'hata', error: err.message };
  }
}

module.exports = { pickStory, xText, xPayload, runSocial, MIN_SOURCES, DEFAULTS };
