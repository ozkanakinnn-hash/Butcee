'use strict';

/**
 * Sosyal paylaşım: "önemli" haberleri Telegram kanalına OTOMATİK gönderir ve X (Twitter) için hazır metin üretir.
 *
 * Neden X'e doğrudan paylaşım yok: X API'si ücretli (kullandıkça öde). Telegram Bot API ücretsizdir. X için sunucu,
 * aynı haberlerin kopyala-yapıştır metnini özel sohbete (owner_chat_id) gönderir; ileride ücretli API istenirse
 * yalnızca gönderim adımı eklenir, seçim mantığı aynı kalır.
 *
 * Seçim (bildirim sistemiyle aynı önem ölçütü): `notify_state/news.stories` içindeki hikâyelerden
 *  - en az MIN_SOURCES farklı yayıncı vermiş,
 *  - tıklama tuzağı/magazin değil, yeterince yeni,
 *  - daha önce paylaşılmamış, haber hâlâ kayıtlı (telif kaldırma talebiyle silinen haber paylaşılmaz).
 *
 * Ayarlar `app_meta/social` belgesinde (dağıtım gerekmez):
 *   { enabled: true, dry_run: true, channel: "@kanal", owner_chat_id: "123", daily_cap: 8, min_gap_min: 45,
 *     link_preview: false, allowed_sources: ["Kaynak A", "Kaynak B"] }
 * - `dry_run: true` iken hiçbir şey gönderilmez; ne paylaşılacağı `social_log` koleksiyonuna yazılır.
 * - `test_ping: true` → bir kez kanala ve özel sohbete "bağlantı testi" mesajı gönderir (deneme modu, sessiz saat ve
 *   haber şartı gözetmez), sonucu `test_result` alanına yazar ve kendini kapatır.
 * - `link_preview: false` (varsayılan): Telegram yayıncının görselini/açıklamasını göstermez (telif için daha güvenli).
 * - `allowed_sources`: doluysa yalnızca bu yayıncıların (kaynak adı, büyük/küçük harf farkı gözetilmez) haberleri
 *   paylaşılır; boşsa tüm kaynaklar.
 */

const { Timestamp } = require('firebase-admin/firestore');
const { isClickbait } = require('./importance');
const { isQuietHour, isCritical } = require('./quiet-hours');

const MIN_SOURCES = 3;
const MAX_AGE_MS = 3 * 3_600_000;
const SKIP_CATEGORIES = new Set(['spor', 'magazin']);
const POSTED_KEEP = 300;
const X_LIMIT = 280;
const X_URL_LENGTH = 23; // X her bağlantıyı 23 karakter sayar

const DEFAULTS = {
  enabled: false, dry_run: true, channel: '', owner_chat_id: '', daily_cap: 8, min_gap_min: 45,
  link_preview: false, allowed_sources: [],
};
const norm = (x) => String(x || '').trim().toLocaleLowerCase('tr-TR');

const escapeHtml = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

/** İstanbul tarihine göre gün anahtarı. */
const dayKey = (ms) =>
  new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(ms));

/** Telegram kanal mesajı (HTML): kalın başlık, kaynak satırı, yayıncının kendi haber bağlantısı. */
function telegramText(story) {
  const n = story.srcs.length;
  const source = [story.source ? `Kaynak: ${escapeHtml(story.source)}` : '', n >= 2 ? `${n} kaynakta` : ''].filter(Boolean).join(' · ');
  return [`<b>${escapeHtml(story.title)}</b>`, source, story.url].filter(Boolean).join('\n\n');
}

/** X için hazır metin: bağlantı dahil en fazla 280 (bağlantı 23 sayılır). */
function xText(story) {
  const n = story.srcs.length;
  const source = [story.source ? `Kaynak: ${story.source}` : '', n >= 2 ? `${n} kaynakta` : ''].filter(Boolean).join(' · ');
  const tail = `\n\n${source}\n`;
  const room = X_LIMIT - X_URL_LENGTH - tail.length;
  const title = story.title.length <= room ? story.title : `${story.title.slice(0, room - 1).trimEnd()}…`;
  return `${title}${tail}${story.url}`;
}

/**
 * Paylaşılacak haber seçimi (saf fonksiyon). @returns {{story: object|null, nextState: object, reason: string}}
 * Gün başına üst sınır, iki paylaşım arası en az aralık, gece sessiz saat (kritik haber hariç).
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

/** Telegram Bot API sendMessage. Hata fırlatır (çağıran yakalar). */
async function telegramSend(fetchImpl, token, chatId, text, { html = true, preview = false } = {}) {
  const res = await fetchImpl(`https://api.telegram.org/bot${token}/sendMessage`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ chat_id: chatId, text, ...(html ? { parse_mode: 'HTML' } : {}), disable_web_page_preview: !preview }),
    signal: AbortSignal.timeout(15_000),
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok || body.ok === false) throw new Error(`Telegram ${res.status}: ${body.description || 'hata'}`);
  return body;
}

/**
 * Bir tur: ayarları ve hikâyeleri okur, uygunsa bir haber paylaşır.
 * @returns {Promise<{status: string, ...}>}
 */
async function runSocial(db, { token, fetchImpl = fetch, now = Date.now() } = {}) {
  const config = { ...DEFAULTS, ...((await db.doc('app_meta/social').get()).data() || {}) };

  // Bağlantı testi: jeton, kanal adı ve botun yöneticiliği haber çıkmasını beklemeden doğrulanır.
  if (config.test_ping === true) {
    const errors = [];
    if (!token) errors.push('jeton yok (TELEGRAM_BOT_TOKEN tanımlı değil ya da "-")');
    else if (!config.channel) errors.push('channel ayarı boş');
    else {
      const text = '✅ Haber Akışı: bağlantı testi başarılı. Önemli haberler bu kanala gelecek.';
      try {
        await telegramSend(fetchImpl, token, config.channel, text, { html: false });
      } catch (err) {
        errors.push(`kanal: ${err.message}`);
      }
      if (config.owner_chat_id) {
        try {
          await telegramSend(fetchImpl, token, config.owner_chat_id, `${text}\n(X taslakları bu sohbete gelecek.)`, { html: false });
        } catch (err) {
          errors.push(`özel sohbet: ${err.message}`);
        }
      }
    }
    await db.doc('app_meta/social').set(
      { test_ping: false, test_result: errors.length ? errors.join(' | ') : 'başarılı', test_at: Timestamp.fromMillis(now) },
      { merge: true },
    );
    return { status: 'test', ...(errors.length ? { errors } : {}) };
  }

  if (!config.enabled) return { status: 'kapalı' };
  if (!config.dry_run && (!token || !config.channel)) return { status: 'ayar eksik' };

  const [newsSnap, stateSnap] = await Promise.all([db.doc('notify_state/news').get(), db.doc('notify_state/social').get()]);
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

  const entry = { at: Timestamp.fromMillis(now), story_id: story.id, title: story.title, url: story.url, source: story.source || '', sources: story.srcs.length };
  if (config.dry_run) {
    await db.collection('social_log').add({ ...entry, dry_run: true, telegram: telegramText(story), x: xText(story) });
    await db.doc('notify_state/social').set(nextState);
    return { status: 'deneme', title: story.title };
  }

  const errors = [];
  try {
    await telegramSend(fetchImpl, token, config.channel, telegramText(story), { preview: config.link_preview === true });
  } catch (err) {
    errors.push(err.message);
  }
  if (config.owner_chat_id) {
    try {
      await telegramSend(fetchImpl, token, config.owner_chat_id, `X için hazır metin:\n\n${xText(story)}`, { html: false });
    } catch (err) {
      errors.push(`x taslağı: ${err.message}`);
    }
  }
  const channelFailed = errors.some((e) => !e.startsWith('x taslağı'));
  // Kanal gönderimi başarısızsa sayaç ilerlemez (bir sonraki turda yeniden denenir)
  if (!channelFailed) await db.doc('notify_state/social').set(nextState);
  await db.collection('social_log').add({ ...entry, dry_run: false, ...(errors.length ? { errors } : {}) });
  return channelFailed ? { status: 'hata', errors } : { status: 'gönderildi', title: story.title, ...(errors.length ? { errors } : {}) };
}

module.exports = { pickStory, telegramText, xText, telegramSend, runSocial, MIN_SOURCES, DEFAULTS };
