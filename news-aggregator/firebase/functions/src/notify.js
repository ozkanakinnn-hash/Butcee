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
const { isQuietHour, isCritical } = require('./quiet-hours');

const CATEGORY_COOLDOWN_MS = 2 * 60 * 60 * 1000;
const BREAKING_COOLDOWN_MS = 10 * 60 * 1000;
const BREAKING_CATEGORY_ID = 'son-dakika';
const MAX_BREAKING_AGE_MS = 3 * 60 * 60 * 1000; // eski haberi "son dakika" diye gönderme

const topicFor = (categoryId) => `news_${categoryId}`;

function trimText(text, max) {
  return text.length <= max ? text : text.slice(0, max - 1).trimEnd() + '…';
}

/**
 * Bildirim kanalları (uygulamadaki NotificationService.channels ile aynı):
 *  breaking_news  : Son Dakika — telefonun varsayılan bildirim sesi (kullanıcı kanal ayarından değiştirebilir), yüksek öncelik
 *  news           : kategori özetleri — varsayılan ses
 *  quiet          : gece 00:00–08:00 — sessiz (olağanüstü durum haberleri hariç)
 */
const BREAKING_TITLE = '🔴 SON DAKİKA';

/** Tek bir kategori bildirimi için FCM mesajı oluşturur (saf fonksiyon; test edilir). */
function buildNewsMessage({ categoryId, categoryName, articles, breaking = false, now = new Date() }) {
  const sorted = [...articles].sort((a, b) => b.data.published_at.toMillis() - a.data.published_at.toMillis());
  const top = sorted[0];
  const more = sorted.length - 1;
  const title = breaking ? BREAKING_TITLE : categoryName;
  // Gece sessiz: deprem/afet/savaş/olağanüstü hal gibi son dakika haberleri hariç.
  const critical = breaking && isCritical(top.data.title, top.data.summary);
  const quiet = isQuietHour(now) && !critical;
  const channelId = quiet ? 'quiet' : breaking ? 'breaking_news' : 'news';
  // Telif ve şeffaflık: her bildirimde haberin kaynağı belirtilir (ör. "Kaynak: AA").
  const source = top.data.source_name ? `Kaynak: ${top.data.source_name}` : '';
  const extra = !breaking && more > 0 ? `ve ${more} haber daha` : '';
  const footer = [source, extra].filter(Boolean).join(' · ');
  const body = trimText(top.data.title, 160 - (footer ? footer.length + 1 : 0)) + (footer ? `\n${footer}` : '');
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
      priority: breaking && !quiet ? 'high' : 'normal',
      collapseKey: topicFor(categoryId), // aynı kategoriden gelen eski bildirim yenisiyle değişir
      notification: {
        channelId,
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
/** Kategori haberleri arasında en az bu kadar süre (bot 20 dk'da bir çalışır → çalışma başına en fazla 1 bildirim). */
const DIGEST_GAP_MS = 18 * 60 * 1000;
/** Tek özet bildirimde en fazla kategori (FCM koşulu en fazla 5 konu alır). */
const DIGEST_MAX_CATEGORIES = 3;

/** Bekleyen kategori kaydı (Firestore'da saklanır; yalnızca düz alanlar). */
function pendingEntry(categoryId, categoryName, articles, prev) {
  const sorted = [...articles].sort((a, b) => b.data.published_at.toMillis() - a.data.published_at.toMillis());
  const top = sorted[0];
  return {
    category_id: categoryId,
    name: categoryName,
    article_id: top.id,
    title: top.data.title,
    url: top.data.original_url,
    source: top.data.source_name || '',
    image: top.data.image_url && top.data.has_original_image !== false ? top.data.image_url : '',
    newest_ms: top.data.published_at.toMillis(),
    count: sorted.length + (prev ? prev.count : 0),
  };
}

/** Birden fazla kategori için TEK özet bildirim (ör. "📰 3 yeni haber"). */
function buildDigestMessage(entries, now = new Date()) {
  const total = entries.reduce((n, e) => n + e.count, 0);
  const lines = entries.map((e) => `${e.name}: ${trimText(e.title, 70)}`);
  const sources = [...new Set(entries.map((e) => e.source).filter(Boolean))];
  const body = [...lines, sources.length ? `Kaynak: ${sources.join(', ')}` : ''].filter(Boolean).join('\n');
  const quiet = isQuietHour(now);
  const image = (entries.find((e) => e.image) || {}).image;
  return {
    condition: entries.map((e) => `'${topicFor(e.category_id)}' in topics`).join(' || '),
    notification: { title: `📰 ${total} yeni haber`, body, ...(image ? { imageUrl: image } : {}) },
    data: {
      type: 'article',
      category_id: entries[0].category_id,
      article_id: entries[0].article_id,
      url: entries[0].url,
      title: entries[0].title,
      source: entries[0].source,
      digest: entries.map((e) => e.category_id).join(','),
    },
    android: {
      priority: 'normal',
      collapseKey: 'news_digest',
      notification: { channelId: quiet ? 'quiet' : 'news', tag: 'news_digest', icon: 'ic_stat_news', color: '#0A84FF' },
    },
  };
}

/**
 * Çalışma sonuçlarından gönderilecek bildirimleri seçer (saf fonksiyon; test edilir).
 *  - Son Dakika: bekletilmeden hemen.
 *  - Kategoriler: önce "bekleyenler" kuyruğuna girer (kategori başına 2 sa bekleme süresi korunur).
 *    Son kategori bildiriminden 18 dk geçmişse kuyruktan en yeni ≤3 kategori gönderilir:
 *    1 kategori → normal kategori bildirimi; 2–3 kategori → TEK özet bildirim. Kalanlar sonraki çalışmaya.
 *    Böylece haberler aynı anda yağmaz, gün içine yayılır.
 * @returns {{messages: object[], nextState: object}}
 */
function planNotifications({ results, categories, state, now = Date.now() }) {
  const lastSent = { ...(state.last_sent || {}) };
  const pending = { ...(state.pending || {}) };
  const messages = [];

  // Kategori bazında bu çalışmada eklenen haberler → kuyruğa
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
    const category = categories.get(categoryId);
    if (!category) continue;
    pending[categoryId] = pendingEntry(categoryId, category.name || categoryId, articles, pending[categoryId]);
  }

  // Kuyruktan gönder: genel aralık dolduysa, bekleme süresi geçmiş en yeni ≤3 kategori
  let lastDigestAt = state.last_digest_at || 0;
  if (now - lastDigestAt >= DIGEST_GAP_MS) {
    const ready = Object.values(pending)
      .filter((e) => now - (lastSent[e.category_id] || 0) >= CATEGORY_COOLDOWN_MS)
      .sort((a, b) => b.newest_ms - a.newest_ms)
      .slice(0, DIGEST_MAX_CATEGORIES);
    if (ready.length === 1) {
      const e = ready[0];
      const articles = [{ id: e.article_id, data: {
        title: e.title, original_url: e.url, source_name: e.source, image_url: e.image,
        has_original_image: Boolean(e.image), published_at: { toMillis: () => e.newest_ms },
      } }];
      const msg = buildNewsMessage({ categoryId: e.category_id, categoryName: e.name, articles, now: new Date(now) });
      if (e.count > 1) {
        msg.notification.body = msg.notification.body.replace(/(\nKaynak: [^\n]*)?$/, (m) => `${m} · ve ${e.count - 1} haber daha`);
      }
      messages.push(msg);
    } else if (ready.length > 1) {
      messages.push(buildDigestMessage(ready, new Date(now)));
    }
    for (const e of ready) {
      lastSent[e.category_id] = now;
      delete pending[e.category_id];
    }
    if (ready.length) lastDigestAt = now;
  }

  const base = { last_sent: lastSent, pending, last_digest_at: lastDigestAt };

  // Son Dakika (bekletilmez)
  const breaking = results
    .flatMap((r) => r.breakingArticles || [])
    .filter((a) => now - a.data.published_at.toMillis() < MAX_BREAKING_AGE_MS)
    .filter((a) => a.id !== state.last_breaking_id);
  if (breaking.length && now - (lastSent[BREAKING_CATEGORY_ID] || 0) >= BREAKING_COOLDOWN_MS) {
    const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles: breaking, breaking: true, now: new Date(now) });
    messages.push(msg);
    lastSent[BREAKING_CATEGORY_ID] = now;
    return { messages, nextState: { ...base, last_breaking_id: msg.data.article_id } };
  }
  return { messages, nextState: { ...base, last_breaking_id: state.last_breaking_id || null } };
}

/** Bildirimleri gönderir ve durumu kaydeder. Hata bildirimi çekimi bozmaz. */
async function sendNewsNotifications(db, results, categories, { messaging = getMessaging(), now = Date.now() } = {}) {
  const ref = db.doc('notify_state/news');
  try {
    const snap = await ref.get();
    const { messages, nextState } = planNotifications({ results, categories, state: snap.data() || {}, now });
    if (messages.length === 0) {
      // Gönderilecek yok ama kuyruk (bekleyen kategoriler) değişmiş olabilir → kaydet
      if (Object.keys(nextState.pending || {}).length || (snap.data() || {}).pending) await ref.set(nextState); // birleştirme değil: gönderilen kategoriler kuyruktan gerçekten silinsin
      return { sent: 0, pending: Object.keys(nextState.pending || {}).length };
    }
    const res = await messaging.sendEach(messages);
    await ref.set(nextState); // birleştirme değil: gönderilen kategoriler kuyruktan gerçekten silinsin
    await require('./notification-log').logSentMessages(db, messages, res.responses, { now });
    const failed = res.responses.filter((r) => !r.success).map((r) => r.error && r.error.message);
    logger.info('Haber bildirimleri gönderildi', {
      sent: res.successCount,
      failed: res.failureCount,
      topics: messages.map((m) => m.topic || m.condition),
      ...(failed.length ? { errors: failed } : {}),
    });
    return { sent: res.successCount, failed: res.failureCount, topics: messages.map((m) => m.topic || m.condition) };
  } catch (err) {
    logger.error('Haber bildirimi gönderilemedi', { error: err.message });
    return { sent: 0, error: err.message };
  }
}

module.exports = {
  BREAKING_TITLE,
  buildDigestMessage,
  DIGEST_GAP_MS,
  buildNewsMessage,
  planNotifications,
  sendNewsNotifications,
  topicFor,
  CATEGORY_COOLDOWN_MS,
  BREAKING_COOLDOWN_MS,
};
