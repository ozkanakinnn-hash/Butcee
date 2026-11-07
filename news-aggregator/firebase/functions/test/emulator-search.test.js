'use strict';

/** Arama dizini doldurma ve bildirim kaydı sıralaması (emülatör). */
const test = require('node:test');
const assert = require('node:assert/strict');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('eski haberlere arama anahtarı eklenir; arama sorgusu çalışır; bildirimler yeniden eskiye gelir', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const { backfillSearchKeywords } = require('../src/maintenance');
  const { logSentMessages } = require('../src/notification-log');

  initializeApp({ projectId: 'demo-search' });
  const db = getFirestore();
  const t = (h) => Timestamp.fromMillis(Date.parse(`2026-11-06T${h}:00:00Z`));
  await db.doc('articles/a').set({ title: 'Galatasaray Avrupa\'da kazandı', source_name: 'NTV', published_at: t('10') });
  await db.doc('articles/b').set({ title: 'İstanbul\'da şiddetli yağış', source_name: 'TRT Haber', published_at: t('11') });
  await db.doc('articles/c').set({ title: 'Galatasaray kampa girdi', source_name: 'A Spor', published_at: t('12'), search_keywords: ['zaten'] });

  const r = await backfillSearchKeywords(db, { maxDocs: 1 });
  assert.equal(r.done, false, 'sayfa sınırında kaldığı yeri kaydeder');
  const r2 = await backfillSearchKeywords(db);
  assert.equal(r2.done, true);
  assert.equal((await backfillSearchKeywords(db)).updated, 0, 'bitince bir daha çalışmaz');

  const hits = await db.collection('articles').where('search_keywords', 'array-contains', 'galatasara').get();
  assert.deepEqual(hits.docs.map((d) => d.id), ['a'], 'c zaten anahtar sahibiydi, dokunulmadı');
  const istanbul = await db.collection('articles').where('search_keywords', 'array-contains', 'ista').get();
  assert.deepEqual(istanbul.docs.map((d) => d.id), ['b'], '"İstanbul" küçük harf ve aksansız aranır');

  // Bildirim kaydı: sıralama belirtmeden en yeni önce
  const msg = (topic, title) => ({ topic, notification: { title, body: '' }, data: { type: 'article' } });
  await logSentMessages(db, [msg('news_spor', 'eski')], null, { now: 1_000 });
  await logSentMessages(db, [msg('news_gundem', 'yeni')], null, { now: 5_000 });
  await logSentMessages(db, [msg('news_magazin', 'takip edilmeyen')], null, { now: 9_000 });
  const feed = await db.collection('notifications').where('topics', 'array-contains-any', ['news_spor', 'news_gundem']).limit(10).get();
  assert.deepEqual(feed.docs.map((d) => d.get('title')), ['yeni', 'eski']);
});
