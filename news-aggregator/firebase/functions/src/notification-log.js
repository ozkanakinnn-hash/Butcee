'use strict';

/**
 * Gönderilen bildirimlerin kaydı → uygulamadaki "Bildirimler" sekmesi.
 *
 * Belge kimliği ters zaman damgasıyla başlar (`<9999999999999 - ms>_<kısa hash>`); Firestore sıralama
 * belirtilmeyen sorgularda belge kimliğine göre sıraladığı için en yeni bildirim önce gelir.
 * Böylece uygulama `where('topics', arrayContainsAny: [...])` ile birleşik dizin GEREKMEDEN sorgular.
 * 7 günden eski kayıtlar silinir.
 */

const crypto = require('node:crypto');
const { Timestamp } = require('firebase-admin/firestore');

const RETENTION_MS = 7 * 86_400_000;

function logId(ms, seed) {
  const rev = String(9_999_999_999_999 - ms).padStart(13, '0');
  return `${rev}_${crypto.createHash('sha1').update(seed).digest('hex').slice(0, 8)}`;
}

/** FCM mesajından kayıt belgesi üretir (saf fonksiyon). */
function logEntryFromMessage(message, { now = Date.now(), extra = {} } = {}) {
  const topics = message.topic
    ? [message.topic]
    : [...String(message.condition || '').matchAll(/'([^']+)' in topics/g)].map((m) => m[1]);
  return {
    id: logId(now, `${topics.join(',')}|${message.notification.title}|${message.notification.body}`),
    data: {
      topics,
      type: (message.data && message.data.type) || 'article',
      title: message.notification.title,
      body: message.notification.body || '',
      image: message.notification.imageUrl || '',
      url: (message.data && message.data.url) || '',
      article_title: (message.data && message.data.title) || '',
      source: (message.data && message.data.source) || '',
      category_id: (message.data && message.data.category_id) || '',
      fixture_id: (message.data && message.data.fixture_id) || '',
      channel: (message.android && message.android.notification && message.android.notification.channelId) || '',
      sent_at: Timestamp.fromMillis(now),
      ...extra,
    },
  };
}

/** Başarıyla gönderilen mesajları kaydeder. `responses`: sendEach yanıtları (aynı sırada). */
async function logSentMessages(db, messages, responses, { now = Date.now() } = {}) {
  const batch = db.batch();
  let n = 0;
  messages.forEach((m, i) => {
    if (responses && responses[i] && !responses[i].success) return;
    const { id, data } = logEntryFromMessage(m, { now: now + i });
    batch.set(db.collection('notifications').doc(id), data);
    n += 1;
  });
  if (n) await batch.commit();
  return n;
}

async function cleanupNotificationLog(db, now = Date.now()) {
  const cutoff = Timestamp.fromMillis(now - RETENTION_MS);
  const old = await db.collection('notifications').where('sent_at', '<', cutoff).limit(400).get();
  if (old.empty) return 0;
  const batch = db.batch();
  old.docs.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  return old.size;
}

module.exports = { logId, logEntryFromMessage, logSentMessages, cleanupNotificationLog };
