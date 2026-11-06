'use strict';

/**
 * Son Dakika işaretlemesi ve okuma kotası koruması (emülatörde):
 *   npx firebase-tools emulators:exec --only firestore --project demo-haber "npm test"
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('Son Dakika kaynağı var olan haberleri işaretler, yenilerini breaking_at ile yazar', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore } = require('firebase-admin/firestore');
  const { ingestAllSources } = require('../src/ingest');

  initializeApp({ projectId: 'demo-breaking' });
  const db = getFirestore();
  const fixture = fs.readFileSync(path.join(__dirname, 'fixtures', 'windows-1254.xml'));
  const fetchImpl = async () => new Response(fixture, { status: 200 });
  const now = new Date('2026-11-06T12:00:00Z');

  await db.doc('categories/gundem').set({ name: 'Gündem', order_index: 1 });
  await db.doc('sources/normal').set({
    source_name: 'Normal', rss_url: 'https://a/rss', category_id: db.doc('categories/gundem'), is_active: true,
  });

  // 1) Normal kaynak haberleri yazar; hiçbiri Son Dakika değil.
  const r1 = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(r1.summary.added, 4);
  let docs = (await db.collection('articles').get()).docs;
  assert.equal(docs.filter((d) => d.get('breaking_at')).length, 0);

  // 2) Aynı haberler bir Son Dakika kaynağında görünür → yeni kayıt yok, 4 haber işaretlenir.
  await db.doc('sources/sondakika').set({
    source_name: 'SD', rss_url: 'https://b/rss', category_id: db.doc('categories/gundem'),
    is_active: true, is_breaking: true,
  });
  const r2 = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(r2.summary.added, 0);
  const sd = r2.results.find((r) => r.source === 'SD');
  assert.equal(sd.markedBreaking, 4);
  docs = (await db.collection('articles').get()).docs;
  assert.equal(docs.length, 4);
  assert.equal(docs.filter((d) => d.get('breaking_at')).length, 4);

  // Uygulamanın Son Dakika sorgusu: orderBy('breaking_at') yalnızca işaretlileri döndürür.
  const breaking = await db.collection('articles').orderBy('breaking_at', 'desc').limit(20).get();
  assert.equal(breaking.size, 4);

  // 3) Okuma kotası koruması: üçüncü çalıştırmada zaten görülmüş haberler veritabanına sorulmadan atlanır.
  const r3 = await ingestAllSources(db, { fetchImpl, now, notify: false });
  for (const r of r3.results) {
    assert.equal(r.added, 0);
    assert.ok(r.skippedSeen >= 3, `${r.source}: görülmüş haberler atlanmalı (atlanan: ${r.skippedSeen})`);
  }
});
