'use strict';

/**
 * İlgi alanı haber bildirimleri (Firebase Cloud Messaging, konu/topic tabanlı).
 *
 * Uygulama, kullanıcının takip ettiği her kategori için `news_<kategoriId>` konusuna abone olur.
 * Bu modül her RSS çekiminden sonra çalışır:
 *  - Son Dakika (`news_son-dakika`): yeni işaretlenen en güncel haber hemen gönderilir
 *    (aynı haber iki kez gönderilmez; en fazla BREAKING_COOLDOWN_MS'de bir).
 *  - Diğer kategoriler: en fazla CATEGORY_COOLDOWN_MS'de (2 saat) bir özet:
 *    en yeni haber başlığı + "ve N haber daha".
 * Gönderim durumu `notify_state/news` belgesinde tutulur (1 okuma + 1 yazma / çalışma).
 * FCM ücretsizdir; Spark planında da çalışır.
 */

const { getMessaging } = require('firebase-admin/messaging');
const { logger } = require('firebase-functions');

const CATEGORY_COOLDOWN_MS = 2 * 60 * 60 * 1000;
const BREAKING_COOLDOWN_MS = 10 * 60 * 1000;
const BREAKING_CATEGORY_ID = 'son-dakika';
const MAX_BREAKING_AGE_MS = 3 * 60 * 60 * 1000; // eski haberi "son dakika" diye gönderme

const topicFor = (categoryId) => `news_${categoryId}`;

function trimText(text, max) {
  return text.length <= max ? text : text.slice(0, max - 1).trimEnd() + '…';
}

/** Tek bir kategori bildirimi için FCM mesajı oluşturur (saf fonksiyon; test edilir). */
function buildNewsMessage({ categoryId, categoryName, articles, breaking = false }) {
  const sorted = [...articles].sort((a, b) => b.data.published_at.toMillis() - a.data.published_at.toMillis());
  const top = sorted[0];
  const more = sorted.length - 1;
  const title = breaking ? `⚡ Son Dakika` : categoryName;
  const body = trimText(top.data.title, 160) + (!breaking && more > 0 ? `\nve ${more} haber daha` : '');
  const image = top.data.image_url && top.data.has_original_image !== false ? top.data.image_url : undefined;

  return {
    topic: topicFor(categoryId),
    notification: { title, body, ...(image ? { imageUrl: image } : {}) },
    data: {
      type: 'article',
      category_id: categoryId,
      article_id: top.id,
      url: top.data.original_url,
      title: top.data.title,
      source: top.data.source_name || '',
    },
    android: {
      priority: breaking ? 'high' : 'normal',
      collapseKey: topicFor(categoryId), // aynı kategoriden gelen eski bildirim yenisiyle değişir
      notification: {
        channelId: breaking ? 'breaking' : 'news',
        tag: topicFor(categoryId),
        icon: 'ic_stat_news',
        color: '#0A84FF',
      },
    },
  };
}

/**
 * Çalışma sonuçlarından gönderilecek bildirimleri seçer (saf fonksiyon; test edilir).
 * @returns {{messages: object[], nextState: object}}
 */
function planNotifications({ results, categories, state, now = Date.now() }) {
  const lastSent = { ...(state.last_sent || {}) };
  const messages = [];

  // Kategori bazında bu çalışmada eklenen haberler
  const byCategory = new Map();
  for (const r of results) {
    for (const a of r.addedArticles || []) {
      const id = a.data.category_id && a.data.category_id.id;
      if (!id) continue;
      if (!byCategory.has(id)) byCategory.set(id, []);
      byCategory.get(id).push(a);
    }
  }
  for (const [categoryId, articles] of byCategory) {
    if (categoryId === BREAKING_CATEGORY_ID) continue;
    if (now - (lastSent[categoryId] || 0) < CATEGORY_COOLDOWN_MS) continue;
    const category = categories.get(categoryId);
    if (!category) continue;
    messages.push(buildNewsMessage({ categoryId, categoryName: category.name || categoryId, articles }));
    lastSent[categoryId] = now;
  }

  // Son Dakika
  const breaking = results
    .flatMap((r) => r.breakingArticles || [])
    .filter((a) => now - a.data.published_at.toMillis() < MAX_BREAKING_AGE_MS)
    .filter((a) => a.id !== state.last_breaking_id);
  if (breaking.length && now - (lastSent[BREAKING_CATEGORY_ID] || 0) >= BREAKING_COOLDOWN_MS) {
    const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles: breaking, breaking: true });
    messages.push(msg);
    lastSent[BREAKING_CATEGORY_ID] = now;
    return { messages, nextState: { last_sent: lastSent, last_breaking_id: msg.data.article_id } };
  }
  return { messages, nextState: { last_sent: lastSent, last_breaking_id: state.last_breaking_id || null } };
}

/** Bildirimleri gönderir ve durumu kaydeder. Hata bildirimi çekimi bozmaz. */
async function sendNewsNotifications(db, results, categories, { messaging = getMessaging(), now = Date.now() } = {}) {
  const ref = db.doc('notify_state/news');
  try {
    const snap = await ref.get();
    const { messages, nextState } = planNotifications({ results, categories, state: snap.data() || {}, now });
    if (messages.length === 0) return { sent: 0 };
    const res = await messaging.sendEach(messages);
    await ref.set(nextState, { merge: true });
    const failed = res.responses.filter((r) => !r.success).map((r) => r.error && r.error.message);
    logger.info('Haber bildirimleri gönderildi', {
      sent: res.successCount,
      failed: res.failureCount,
      topics: messages.map((m) => m.topic),
      ...(failed.length ? { errors: failed } : {}),
    });
    return { sent: res.successCount, failed: res.failureCount, topics: messages.map((m) => m.topic) };
  } catch (err) {
    logger.error('Haber bildirimi gönderilemedi', { error: err.message });
    return { sent: 0, error: err.message };
  }
}

module.exports = {
  buildNewsMessage,
  planNotifications,
  sendNewsNotifications,
  topicFor,
  CATEGORY_COOLDOWN_MS,
  BREAKING_COOLDOWN_MS,
};
