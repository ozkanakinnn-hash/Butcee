'use strict';

/**
 * Haber bildirimleri — yalnızca sabah/akşam bülten (Firebase Cloud Messaging).
 *
 * Breaking news ve kategori özeti bildirimleri kapalıdır; haberler arka planda toplanmaya ve
 * "5 dakikada gündem" belgesi oluşturmaya devam eder. Kullanıcıya günde en fazla 2 bildirim
 * gider: sabah 08:30 ve akşam 19:00 bültenleri.
 *
 * Bülten içeriği Gemini API ile Türkçe özetlenir; özet `briefings/{id}.ai_summary` alanında
 * saklanır. Anahtar yoksa özet alanı boş bırakılır.
 *
 * Bildirim seviyeleri (günlük cap; ileride tekrar etkinleştirilecek bildirim türleri için):
 *   Düşük   (lvl_low)      : günde max 5
 *   Dengeli (lvl_balanced) : günde max 9
 *   Yüksek  (lvl_all)      : günde max 15
 */

const { getMessaging } = require('firebase-admin/messaging');
const { logger } = require('firebase-functions');
const { isQuietHour } = require('./quiet-hours');
const { mergeStories, pruneStories, isClickbait } = require('./importance');
const { developmentKeys } = require('./scoring');

const BREAKING_CATEGORY_ID = 'son-dakika';
const BRAND_COLOR = '#0B1F3A';

const BULLETIN_TOPIC = 'bulletin';
const BULLETIN_SLOTS = [
  { minute: 8 * 60 + 30, title: '☀️ 5 dakikada gündem', key: 'am', name: 'Sabah gündemi' },
  { minute: 19 * 60, title: '🌙 Akşam gündemi', key: 'pm', name: 'Akşam gündemi' },
];
const BULLETIN_WINDOW_MIN = 120;
const BULLETIN_ITEMS = 4;
const BRIEFING_ITEMS = 7;

/** Bildirim seviyeleri için günlük üst sınırlar (ileride tekrar etkinleştirilecek türler için). */
const PROFILES = {
  low:      { level: 'lvl_low',      dailyCap: 5  },
  balanced: { level: 'lvl_balanced', dailyCap: 9  },
  all:      { level: 'lvl_all',      dailyCap: 15 },
};

const topicFor = (categoryId) => `news_${categoryId}`;

function trimText(text, max) {
  return text.length <= max ? text : text.slice(0, max - 1).trimEnd() + '…';
}

/** İstanbul tarihine göre gün anahtarı ve günün dakikası. */
function istanbulClock(ms) {
  const parts = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Europe/Istanbul',
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    }).formatToParts(new Date(ms)).map((p) => [p.type, p.value]),
  );
  return { day: `${parts.year}-${parts.month}-${parts.day}`, minute: Number(parts.hour) * 60 + Number(parts.minute) };
}

/** "5 dakikada gündem" belgesi (briefings/{gün}-{am|pm}); uygulama ekranı bunu okur. */
function buildBriefing(slot, day, stories, now, pool = stories) {
  const item = (x) => ({
    id: x.id, title: x.title, url: x.url, source: x.source || '', image: x.image || '', category_id: x.cat || '',
    sources: x.srcs.slice(0, 12), source_count: x.srcs.length,
  });
  // İlgi alanlarına göre kısa bölümler: her kategoriden en çok kaynaklı 3 başlık
  const byCategory = {};
  for (const x of [...pool].sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms)) {
    if (!x.cat || x.cat === BREAKING_CATEGORY_ID) continue;
    const list = (byCategory[x.cat] = byCategory[x.cat] || []);
    if (list.length < 3) list.push(item(x));
  }
  return {
    by_category: byCategory,
    id: `${day}-${slot.key}`,
    slot: slot.key,
    title: slot.name,
    day,
    created_ms: now,
    items: stories.map(item),
    ai_summary: '', // Gemini tamamlanınca writeExtras tarafından güncellenir
  };
}

/** Günün bülteni (en çok kaynağın verdiği haberler). */
function buildBulletinMessage(slot, stories, now, briefingId = '') {
  const lines = stories.map((s) => `• ${trimText(s.title, 80)}`);
  const sources = [...new Set(stories.map((s) => s.source).filter(Boolean))].slice(0, 4);
  const top = stories[0];
  return {
    topic: BULLETIN_TOPIC,
    notification: {
      title: slot.title,
      body: [...lines, sources.length ? `Kaynak: ${sources.join(', ')}` : ''].filter(Boolean).join('\n'),
      ...(top.image ? { imageUrl: top.image } : {}),
    },
    data: {
      type: 'article', category_id: top.cat || '', article_id: top.id, url: top.url,
      title: top.title, source: top.source, bulletin: '1', briefing_id: briefingId,
    },
    android: {
      priority: 'normal',
      collapseKey: 'news_bulletin',
      notification: { channelId: isQuietHour(new Date(now)) ? 'quiet' : 'news', tag: 'news_bulletin', icon: 'ic_stat_news', color: BRAND_COLOR },
    },
  };
}

/**
 * FCM'e giden biçim: Android'de VERİ mesajı (uygulama bildirimi kendisi kurar),
 * iOS'ta apns uyarısı.
 */
function toWire(m) {
  const n = m.notification;
  const an = (m.android && m.android.notification) || {};
  const data = {};
  for (const [k, v] of Object.entries(m.data || {})) data[k] = String(v ?? '');
  Object.assign(data, {
    kind: 'news_alert',
    n_title: n.title,
    n_body: n.body || '',
    n_image: n.imageUrl || '',
    n_channel: an.channelId || 'news',
    n_tag: an.tag || '',
  });
  return {
    ...(m.topic ? { topic: m.topic } : { condition: m.condition }),
    data,
    android: { priority: (m.android && m.android.priority) || 'normal', collapseKey: m.android && m.android.collapseKey, ttl: 6 * 3_600_000 },
    apns: {
      headers: { 'apns-priority': an.channelId === 'quiet' ? '5' : '10' },
      payload: {
        aps: {
          alert: { title: n.title, body: n.body || '' },
          ...(an.channelId === 'quiet' ? { 'interruption-level': 'passive' } : { sound: 'default' }),
        },
      },
    },
  };
}

/**
 * Çalışma sonuçlarından bülten bildirimi planlar (saf fonksiyon; test edilir).
 * Breaking news ve kategori özeti bildirimleri bu versiyonda devre dışıdır.
 * @returns {{messages: object[], nextState: object, briefing: object|null, sourceUpdates: object[]}}
 */
function planNotifications({ results, categories, state = {}, now = Date.now() }) {
  const messages = [];
  const next = {};

  // 1) Yeni haberler → hikâye birleştirme (bülten ve kaynak listesi için; bildirim gönderilmez)
  const forStories = [];
  for (const r of results) {
    for (const a of r.addedArticles || []) forStories.push(a);
    for (const a of r.breakingArticles || []) forStories.push({ ...a, breaking: true });
  }

  // 2) Hikâyeler (önem) — aynı haber farklı akışlarda tekrar gelirse yayıncı bir kez sayılır
  const { stories } = mergeStories(state.stories || [], forStories, { now, devKeys: developmentKeys });

  // 3) Bülten (sabah 08:30 / akşam 19:00) — günde en fazla 2 bildirim
  const bul = { ...(state.bulletin || {}) };
  const clock = istanbulClock(now);
  let briefing = null;

  const slot = clock.minute >= BULLETIN_SLOTS[1].minute ? BULLETIN_SLOTS[1]
    : clock.minute >= BULLETIN_SLOTS[0].minute ? BULLETIN_SLOTS[0] : BULLETIN_SLOTS[1];
  const slotDay = clock.minute < BULLETIN_SLOTS[0].minute ? istanbulClock(now - 12 * 3_600_000).day : clock.day;
  const key = `${slotDay}#${slot.key}`;
  const inWindow = slotDay === clock.day && clock.minute >= slot.minute && clock.minute < slot.minute + BULLETIN_WINDOW_MIN;

  if (bul.briefing_key !== key || (inWindow && bul.last !== key)) {
    const since = now - 14 * 3_600_000;
    const fresh = stories.filter((x) => x.last_ms > since && !isClickbait(x.title));
    const picks = [...fresh].sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms).slice(0, BRIEFING_ITEMS);

    if (picks.length >= 2) {
      briefing = buildBriefing(slot, slotDay, picks, now, fresh);
      bul.briefing_key = key;

      if (inWindow && bul.last !== key) {
        messages.push(buildBulletinMessage(slot, picks.slice(0, BULLETIN_ITEMS), now, briefing.id));
        bul.last = key;
        bul.last_ms = now;
      }
    }
  }

  next.bulletin = bul;
  next.stories = pruneStories(stories, { now, keepSinceMs: bul.last_ms });

  return { messages, nextState: next, briefing };
}

/** Gündem belgesi ve AI özeti. Hata bildirimi çekimi bozmaz. */
async function writeExtras(db, briefing) {
  if (!briefing) return;
  try {
    const { Timestamp } = require('firebase-admin/firestore');
    const { summarizeBriefing } = require('./gemini');
    const aiSummary = await summarizeBriefing(briefing.items);
    const doc = { ...briefing, ai_summary: aiSummary, created_at: Timestamp.fromMillis(briefing.created_ms) };
    await db.doc(`briefings/${briefing.id}`).set(doc);
    await db.doc('briefings/latest').set(doc);
    if (aiSummary) logger.info('Bülten AI özeti oluşturuldu', { id: briefing.id, len: aiSummary.length });
  } catch (err) {
    logger.warn('Gündem belgesi yazılamadı', { error: err.message });
  }
}

/** Bildirimleri gönderir ve durumu kaydeder. Hata bildirimi çekimi bozmaz. */
async function sendNewsNotifications(db, results, categories, { messaging = getMessaging(), now = Date.now() } = {}) {
  const ref = db.doc('notify_state/news');
  try {
    const snap = await ref.get();
    const { messages, nextState, briefing } = planNotifications({ results, categories, state: snap.data() || {}, now });
    await writeExtras(db, briefing);
    // Durum her çalışmada güncellenir (hikâyeler ve kaynak listesi için)
    await ref.set(nextState);
    if (messages.length === 0) return { sent: 0 };
    const res = await messaging.sendEach(messages.map(toWire));
    await require('./notification-log').logSentMessages(db, messages, res.responses, { now });
    const failed = res.responses.filter((r) => !r.success).map((r) => r.error && r.error.message);
    logger.info('Bülten bildirimi gönderildi', {
      sent: res.successCount,
      failed: res.failureCount,
      targets: messages.map((m) => m.topic || m.condition),
      ...(failed.length ? { errors: failed } : {}),
    });
    return { sent: res.successCount, failed: res.failureCount, targets: messages.map((m) => m.topic || m.condition) };
  } catch (err) {
    logger.error('Bülten bildirimi gönderilemedi', { error: err.message });
    return { sent: 0, error: err.message };
  }
}

module.exports = {
  BULLETIN_SLOTS,
  BULLETIN_TOPIC,
  PROFILES,
  buildBriefing,
  buildBulletinMessage,
  planNotifications,
  sendNewsNotifications,
  toWire,
  topicFor,
  istanbulClock,
};
