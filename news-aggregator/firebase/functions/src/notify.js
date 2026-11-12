'use strict';

/**
 * Haber bildirimleri — "az ama önemli" (Firebase Cloud Messaging, konu/koşul tabanlı).
 *
 * Kullanıcı uygulamada bildirim YOĞUNLUĞU seçer; uygulama buna göre konulara abone olur:
 *   Sadece önemli : lvl_important + calm (+ bulletin)        → önemli son dakika + günde 3 bülten  (~4–6/gün)
 *   Dengeli       : lvl_balanced  + calm (+ bulletin)        → + kategori özetleri saatte en fazla 1 (~8–12/gün)
 *   Tümü          : lvl_all            (+ bulletin)          → her son dakika + kategori özetleri 18 dk'da bir
 * Kategori konuları (news_<kategori>) seviye konusuyla birlikte koşulda kullanılır:
 *   "('news_ekonomi' in topics || 'news_spor' in topics) && 'lvl_balanced' in topics"
 *
 * Önem: aynı olayı kaç FARKLI yayıncının verdiği (src/importance.js hikâye kümeleri). 3+ yayıncı (ya da son dakika
 * kaynağından deprem/afet/savaş gibi kritik haber) → "önemli son dakika". Tıklama tuzağı/magazin başlıkları
 * bildirime girmez. Günlük üst sınırlar ve gece sessiz saatleri uygulanır.
 *
 * Mesajlar cihaza VERİ mesajı olarak gider (kind=news_alert); Android uygulaması bildirimi kendisi oluşturur ve
 * üzerine "Daha az bildirim" düğmesi ekler (LessNotificationsReceiver). iOS için apns uyarısı eklenir.
 * Durum `notify_state/news` belgesinde (1 okuma + 1 yazma / çalışma).
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
const BRAND_COLOR = '#0B1F3A'; // lacivert (logo)

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
        color: BRAND_COLOR,
      },
    },
  };
}


/** Kategori özeti profilleri (seviye başına ayrı kuyruk). */
const PROFILES = {
  all: { level: 'lvl_all', gapMs: 18 * 60 * 1000, cooldownMs: CATEGORY_COOLDOWN_MS, dailyCap: Infinity },
  balanced: { level: 'lvl_balanced', gapMs: 60 * 60 * 1000, cooldownMs: 3 * 60 * 60 * 1000, dailyCap: 6 },
};
const DIGEST_GAP_MS = PROFILES.all.gapMs;
/** Tek özet bildirimde en fazla kategori (FCM koşulu en fazla 5 konu alır: 3 kategori + seviye). */
const DIGEST_MAX_CATEGORIES = 3;

const CALM_TOPIC = 'calm'; // "Sadece önemli" + "Dengeli"
const BULLETIN_TOPIC = 'bulletin';
const MAJOR_MIN_SOURCES = 3;
const MAJOR_COOLDOWN_MS = 30 * 60 * 1000;
const MAJOR_DAILY_CAP = 6;
const MAJOR_MAX_AGE_MS = 2 * 60 * 60 * 1000;
const MAJOR_SKIP_CATEGORIES = new Set(['spor', 'magazin']); // son dakika kaynağı değilse
const BULLETIN_SLOTS = [
  { minute: 8 * 60 + 30, title: '☀️ Sabah Bülteni' },
  { minute: 13 * 60, title: '📰 Öğle Bülteni' },
  { minute: 19 * 60, title: '🌙 Akşam Bülteni' },
];
const BULLETIN_WINDOW_MIN = 120; // slot saatinden sonra en geç 2 saat içinde
const BULLETIN_ITEMS = 4;

const { mergeStories, pruneStories, isClickbait } = require('./importance');

const levelCondition = (categoryIds, level) => {
  const cats = categoryIds.map((id) => `'${topicFor(id)}' in topics`);
  const left = cats.length > 1 ? `(${cats.join(' || ')})` : cats[0];
  return `${left} && '${level}' in topics`;
};

/** İstanbul tarihine göre gün anahtarı ve günün dakikası. */
function istanbulClock(ms) {
  const parts = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' })
      .formatToParts(new Date(ms)).map((p) => [p.type, p.value]),
  );
  return { day: `${parts.year}-${parts.month}-${parts.day}`, minute: Number(parts.hour) * 60 + Number(parts.minute) };
}

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
function buildDigestMessage(entries, now = new Date(), level = 'lvl_all') {
  const total = entries.reduce((n, e) => n + e.count, 0);
  const lines = entries.map((e) => `${e.name}: ${trimText(e.title, 70)}`);
  const sources = [...new Set(entries.map((e) => e.source).filter(Boolean))];
  const body = [...lines, sources.length ? `Kaynak: ${sources.join(', ')}` : ''].filter(Boolean).join('\n');
  const quiet = isQuietHour(now);
  const image = (entries.find((e) => e.image) || {}).image;
  return {
    condition: levelCondition(entries.map((e) => e.category_id), level),
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
      notification: { channelId: quiet ? 'quiet' : 'news', tag: 'news_digest', icon: 'ic_stat_news', color: BRAND_COLOR },
    },
  };
}

/** Bir profilin (seviye) kategori kuyruğunu işler. */
function planDigest(profile, st, categoryAdds, now) {
  const lastSent = { ...(st.last_sent || {}) };
  const pending = { ...(st.pending || {}) };
  const { day } = istanbulClock(now);
  let count = st.day === day ? st.count || 0 : 0;
  for (const [categoryId, { name, articles }] of categoryAdds) {
    pending[categoryId] = pendingEntry(categoryId, name, articles, pending[categoryId]);
  }
  const messages = [];
  let lastDigestAt = st.last_digest_at || 0;
  if (now - lastDigestAt >= profile.gapMs && count < profile.dailyCap) {
    const ready = Object.values(pending)
      .filter((e) => now - (lastSent[e.category_id] || 0) >= profile.cooldownMs)
      .sort((a, b) => b.newest_ms - a.newest_ms)
      .slice(0, DIGEST_MAX_CATEGORIES);
    if (ready.length === 1) {
      const e = ready[0];
      const articles = [{ id: e.article_id, data: {
        title: e.title, original_url: e.url, source_name: e.source, image_url: e.image,
        has_original_image: Boolean(e.image), published_at: { toMillis: () => e.newest_ms },
      } }];
      const msg = buildNewsMessage({ categoryId: e.category_id, categoryName: e.name, articles, now: new Date(now) });
      delete msg.topic;
      msg.condition = levelCondition([e.category_id], profile.level);
      if (e.count > 1) {
        msg.notification.body = msg.notification.body.replace(/(\nKaynak: [^\n]*)?$/, (m) => `${m} · ve ${e.count - 1} haber daha`);
      }
      messages.push(msg);
    } else if (ready.length > 1) {
      messages.push(buildDigestMessage(ready, new Date(now), profile.level));
    }
    for (const e of ready) {
      lastSent[e.category_id] = now;
      delete pending[e.category_id];
    }
    if (ready.length) {
      lastDigestAt = now;
      count += 1;
    }
  }
  return { messages, state: { last_sent: lastSent, pending, last_digest_at: lastDigestAt, day, count } };
}

/** Önemli son dakika mesajı (hikâyeden). */
function buildMajorMessage(story, now) {
  const articles = [{ id: story.id, data: {
    title: story.title, original_url: story.url, source_name: story.source, image_url: story.image,
    has_original_image: Boolean(story.image), published_at: { toMillis: () => story.last_ms },
  } }];
  const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles, breaking: true, now: new Date(now) });
  delete msg.topic;
  msg.condition = `'${topicFor(BREAKING_CATEGORY_ID)}' in topics && '${CALM_TOPIC}' in topics`;
  if (!story.br) msg.notification.title = '⚡ ÖNEMLİ GELİŞME';
  if (story.srcs.length >= 2) msg.notification.body += ` · ${story.srcs.length} kaynakta`;
  msg.android.collapseKey = 'news_major';
  msg.android.notification.tag = `major_${story.id}`;
  msg.data.importance = String(story.srcs.length);
  return msg;
}

/** Günün bülteni (en çok kaynağın verdiği haberler). */
function buildBulletinMessage(slot, stories, now) {
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
    data: { type: 'article', category_id: top.cat || '', article_id: top.id, url: top.url, title: top.title, source: top.source, bulletin: '1' },
    android: {
      priority: 'normal',
      collapseKey: 'news_bulletin',
      notification: { channelId: isQuietHour(new Date(now)) ? 'quiet' : 'news', tag: 'news_bulletin', icon: 'ic_stat_news', color: BRAND_COLOR },
    },
  };
}

/**
 * Çalışma sonuçlarından gönderilecek bildirimleri seçer (saf fonksiyon; test edilir).
 * @returns {{messages: object[], nextState: object}}
 */
function planNotifications({ results, categories, state = {}, now = Date.now() }) {
  const messages = [];
  const next = {};

  // 1) Yeni haberler → kategori kuyrukları (son dakika kategorisi hariç) ve hikâyeler
  const byCategory = new Map();
  const forStories = [];
  for (const r of results) {
    for (const a of r.addedArticles || []) {
      forStories.push(a);
      const id = a.data.category_id && a.data.category_id.id;
      if (!id || id === BREAKING_CATEGORY_ID || isClickbait(a.data.title || '')) continue;
      const category = categories.get(id);
      if (!category) continue;
      if (!byCategory.has(id)) byCategory.set(id, { name: category.name || id, articles: [] });
      byCategory.get(id).articles.push(a);
    }
    for (const a of r.breakingArticles || []) forStories.push({ ...a, breaking: true });
  }
  for (const [key, profile] of Object.entries(PROFILES)) {
    const { messages: m, state: s } = planDigest(profile, state[key] || {}, byCategory, now);
    messages.push(...m);
    next[key] = s;
  }

  // 2) Hikâyeler (önem) — aynı haber farklı akışlarda tekrar gelirse yayıncı bir kez sayılır
  const { stories } = mergeStories(state.stories || [], forStories, { now });

  // 3) Önemli son dakika ("Sadece önemli" + "Dengeli")
  const br = { ...(state.breaking || {}) };
  const { day } = istanbulClock(now);
  if (br.day !== day) Object.assign(br, { day, major_count: 0 });
  const major = stories
    .filter((s) => !s.major_ms && now - s.first_ms <= MAJOR_MAX_AGE_MS && !isClickbait(s.title))
    .map((s) => ({ s, critical: s.br && isCritical(s.title) }))
    .filter(({ s, critical }) => critical || (s.srcs.length >= MAJOR_MIN_SOURCES && (s.br || !MAJOR_SKIP_CATEGORIES.has(s.cat))))
    .sort((a, b) => Number(b.critical) - Number(a.critical) || b.s.srcs.length - a.s.srcs.length)[0];
  if (major) {
    const allowed = major.critical
      || ((br.major_count || 0) < MAJOR_DAILY_CAP && now - (br.last_major_at || 0) >= MAJOR_COOLDOWN_MS);
    if (allowed) {
      messages.push(buildMajorMessage(major.s, now));
      major.s.major_ms = now;
      br.last_major_at = now;
      br.major_count = (br.major_count || 0) + 1;
    }
  }

  // 4) Tüm son dakikalar ("Tümü") — eskisi gibi hemen, 10 dk aralıkla
  const minor = results
    .flatMap((r) => r.breakingArticles || [])
    .filter((a) => now - a.data.published_at.toMillis() < MAX_BREAKING_AGE_MS)
    .filter((a) => a.id !== br.last_minor_id && !isClickbait(a.data.title || ''));
  if (minor.length && now - (br.last_minor_at || 0) >= BREAKING_COOLDOWN_MS) {
    const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles: minor, breaking: true, now: new Date(now) });
    delete msg.topic;
    msg.condition = `'${topicFor(BREAKING_CATEGORY_ID)}' in topics && '${PROFILES.all.level}' in topics`;
    messages.push(msg);
    br.last_minor_at = now;
    br.last_minor_id = msg.data.article_id;
  }
  next.breaking = br;

  // 5) Bülten (sabah / öğle / akşam)
  const bul = { ...(state.bulletin || {}) };
  const clock = istanbulClock(now);
  BULLETIN_SLOTS.forEach((slot, i) => {
    const key = `${clock.day}#${i}`;
    if (bul.last === key || clock.minute < slot.minute || clock.minute >= slot.minute + BULLETIN_WINDOW_MIN) return;
    const since = bul.last_ms || now - 6 * 3_600_000;
    const picks = stories
      .filter((s) => s.last_ms > since && !isClickbait(s.title))
      .sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms)
      .slice(0, BULLETIN_ITEMS);
    if (picks.length >= 2) messages.push(buildBulletinMessage(slot, picks, now));
    bul.last = key;
    bul.last_ms = now;
  });
  next.bulletin = bul;
  next.stories = pruneStories(stories, { now, keepSinceMs: bul.last_ms });

  return { messages, nextState: next };
}

/**
 * FCM'e giden biçim: Android'de VERİ mesajı (uygulama bildirimi kendisi kurar, "Daha az bildirim" düğmesiyle),
 * iOS'ta apns uyarısı. İç biçim (notification alanlı) kayıt ve testler için korunur.
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
    apns: { payload: { aps: { alert: { title: n.title, body: n.body || '' }, sound: 'default' } } },
  };
}

/** Bildirimleri gönderir ve durumu kaydeder. Hata bildirimi çekimi bozmaz. */
async function sendNewsNotifications(db, results, categories, { messaging = getMessaging(), now = Date.now() } = {}) {
  const ref = db.doc('notify_state/news');
  try {
    const snap = await ref.get();
    const { messages, nextState } = planNotifications({ results, categories, state: snap.data() || {}, now });
    if (messages.length === 0) {
      await ref.set(nextState); // kuyruk ve hikâyeler her çalışmada güncellenir (birleştirme değil: silinenler gitsin)
      return { sent: 0 };
    }
    const res = await messaging.sendEach(messages.map(toWire));
    await ref.set(nextState);
    await require('./notification-log').logSentMessages(db, messages, res.responses, { now });
    const failed = res.responses.filter((r) => !r.success).map((r) => r.error && r.error.message);
    logger.info('Haber bildirimleri gönderildi', {
      sent: res.successCount,
      failed: res.failureCount,
      targets: messages.map((m) => m.topic || m.condition),
      ...(failed.length ? { errors: failed } : {}),
    });
    return { sent: res.successCount, failed: res.failureCount, targets: messages.map((m) => m.topic || m.condition) };
  } catch (err) {
    logger.error('Haber bildirimi gönderilemedi', { error: err.message });
    return { sent: 0, error: err.message };
  }
}

module.exports = {
  BREAKING_TITLE,
  BULLETIN_SLOTS,
  CALM_TOPIC,
  BULLETIN_TOPIC,
  PROFILES,
  buildDigestMessage,
  DIGEST_GAP_MS,
  buildNewsMessage,
  planNotifications,
  sendNewsNotifications,
  toWire,
  topicFor,
  istanbulClock,
  CATEGORY_COOLDOWN_MS,
  BREAKING_COOLDOWN_MS,
  MAJOR_DAILY_CAP,
};
