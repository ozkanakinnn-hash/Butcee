'use strict';

/**
 * Bülten güvencesi: sabah 08:30 ve akşam 19:00 gündeminin MUTLAKA yazılması.
 *
 * Normalde bülten haber çekiminin (fetchRssFeeds, 20 dk) sonunda planNotifications ile oluşur. Çekim gecikir, hata
 * verir ya da o anda hikâye listesi yetersiz kalırsa `briefings/latest` bir önceki dilimde kalıyor ve uygulama dünkü
 * bülteni gösteriyordu. Bu iş, bülten saatlerinde 10 dakikada bir çalışır (index.js → briefingTick):
 *
 *  1. Şu anki dilimin belgesi (`briefings/{gün}-{am|pm}`) zaten varsa hiçbir şey yapmaz.
 *  2. Yoksa `notify_state/news.stories`'ten üretir; yetmezse son 14 saatin haberlerinden (articles) hikâye kurar.
 *  3. Bülten saatindeyse ve bildirim henüz gitmediyse bildirimi gönderir. `notify_state/news.bulletin.last` alanı
 *     işlemle (transaction) "sahiplenilir" → haber çekimiyle aynı anda çalışsa da bildirim iki kez gitmez.
 */

const { Timestamp } = require('firebase-admin/firestore');
const { logger } = require('firebase-functions');
const { mergeStories } = require('./importance');
const {
  bulletinSlot, pickBriefingStories, buildBriefing, buildBulletinMessage, writeExtras, toWire, BULLETIN_ITEMS,
} = require('./notify');

const ARTICLE_LOOKBACK_MS = 14 * 3_600_000;
const ARTICLE_LIMIT = 400;

/** Son 14 saatin haberlerinden hikâye listesi (notify_state boş/yetersizse). */
async function storiesFromArticles(db, now) {
  const snap = await db
    .collection('articles')
    .where('published_at', '>=', Timestamp.fromMillis(now - ARTICLE_LOOKBACK_MS))
    .orderBy('published_at', 'desc')
    .limit(ARTICLE_LIMIT)
    .get();
  const articles = snap.docs.map((d) => ({ id: d.id, data: d.data() }));
  return mergeStories([], articles, { now }).stories;
}

/**
 * Şu anki dilimin bültenini garanti eder.
 * @returns {Promise<{status: string, id: string, source?: string, sent?: number}>}
 */
async function ensureBriefing(db, { messaging, now = Date.now(), fromArticles = storiesFromArticles } = {}) {
  const { slot, slotDay, key, id, inWindow } = bulletinSlot(now);
  const stateRef = db.doc('notify_state/news');

  const existing = await db.doc(`briefings/${id}`).get();
  let picks = null;
  if (!existing.exists) {
    const state = (await stateRef.get()).data() || {};
    let { picks: p, fresh } = pickBriefingStories(state.stories || [], now);
    let source = 'hikâyeler';
    if (p.length < 2) {
      ({ picks: p, fresh } = pickBriefingStories(await fromArticles(db, now), now));
      source = 'haberler';
    }
    if (p.length < 2) {
      logger.warn('Bülten üretilemedi: yeterli haber yok', { id });
      return { status: 'haber yok', id };
    }
    const briefing = buildBriefing(slot, slotDay, p, now, fresh);
    await writeExtras(db, briefing);
    picks = p;
    logger.info('Bülten yedek işle oluşturuldu', { id, source, items: p.length });
    if (!inWindow) return { status: 'oluşturuldu', id, source };
  } else if (!inWindow) {
    return { status: 'güncel', id };
  } else {
    // Belge var; bildirim gitti mi? (gittiyse iş yok)
    const bul = ((await stateRef.get()).data() || {}).bulletin || {};
    if (bul.last === key) return { status: 'güncel', id };
    picks = (existing.data().items || []).map((i) => ({
      id: i.id, title: i.title, url: i.url, source: i.source, image: i.image, cat: i.category_id,
    }));
    if (picks.length < 2) return { status: 'güncel', id };
  }

  // Bildirim: bülten saatinde, henüz gitmediyse (işlemle sahiplen)
  const claimed = await db.runTransaction(async (tx) => {
    const snap = await tx.get(stateRef);
    const bul = ((snap.data() || {}).bulletin) || {};
    if (bul.last === key) return false;
    tx.set(stateRef, { bulletin: { ...bul, last: key, last_ms: now, briefing_key: key } }, { merge: true });
    return true;
  });
  if (!claimed || !messaging) return { status: 'oluşturuldu', id };

  const msg = buildBulletinMessage(slot, picks.slice(0, BULLETIN_ITEMS), now, id);
  try {
    const res = await messaging.sendEach([toWire(msg)]);
    await require('./notification-log').logSentMessages(db, [msg], res.responses, { now });
    logger.info('Bülten bildirimi yedek işle gönderildi', { id, sent: res.successCount });
    return { status: 'gönderildi', id, sent: res.successCount };
  } catch (err) {
    logger.error('Bülten bildirimi gönderilemedi', { id, error: err.message });
    return { status: 'bildirim hatası', id };
  }
}

module.exports = { ensureBriefing, storiesFromArticles };
