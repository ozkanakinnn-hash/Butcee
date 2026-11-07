'use strict';

/**
 * Firestore emülatörüyle uçtan uca test (yalnızca emülatör çalışırken koşar):
 *   npx firebase-tools emulators:exec --only firestore --project demo-haber "npm test"
 */

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('iki ardışık çalıştırmada haberler mükerrer yazılmaz', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore } = require('firebase-admin/firestore');
  const { ingestAllSources } = require('../src/ingest');

  initializeApp({ projectId: process.env.GCLOUD_PROJECT || 'demo-haber' });
  const db = getFirestore();

  const fixture = fs.readFileSync(path.join(__dirname, 'fixtures', 'windows-1254.xml'));
  const fetchImpl = async () => new Response(fixture, { status: 200, headers: { etag: '"v1"' } });
  const now = new Date('2026-11-06T12:00:00Z');

  await db.collection('categories').doc('gundem').set({ name: 'Gündem', order_index: 1, placeholder_image_url: 'https://cdn.example.com/gundem.jpg' });
  await db.collection('sources').doc('ornek').set({
    source_name: 'Örnek',
    rss_url: 'https://ornek-haber.com.tr/rss',
    category_id: db.doc('categories/gundem'),
    is_active: true,
  });
  await db.collection('sources').doc('pasif').set({ source_name: 'Pasif', rss_url: 'https://x', is_active: false });

  const first = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(first.summary.sources, 1, 'pasif kaynak işlenmemeli');
  assert.equal(first.summary.added, 4);

  const second = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(second.summary.added, 0, 'ikinci çalıştırmada yeni haber yazılmamalı');

  const all = await db.collection('articles').get();
  assert.equal(all.size, 4);

  const resimsiz = all.docs.map((d) => d.data()).find((a) => a.title === 'Resimsiz haber');
  assert.equal(resimsiz.image_url, 'https://cdn.example.com/gundem.jpg');
  assert.equal(resimsiz.category_id.path, 'categories/gundem');

  const src = (await db.doc('sources/ornek').get()).data();
  assert.equal(src.etag, '"v1"');
  assert.equal(src.last_error, null);
});
