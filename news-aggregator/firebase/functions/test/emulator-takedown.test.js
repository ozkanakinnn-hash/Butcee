'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');

if (!process.env.FIRESTORE_EMULATOR_HOST) {
  test('emülatör yok — atlandı', { skip: true }, () => {});
  return;
}

const { initializeApp, getApps } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { articleIdForUrl, normalizeUrl } = require('../src/text');
const { buildArticle } = require('../src/ingest');
const { removeArticleByUrl, removeSource, disableSourceImages } = require('../src/takedown');

if (!getApps().length) initializeApp({ projectId: 'demo-takedown' }) // ayrı proje = diğer testlerden yalıtılmış veri;
const db = getFirestore();

test('uyar-kaldır: haber (kopyasıyla), kaynak ve görsel kaldırma', async () => {
  const url = 'https://ornek.com/haber/1';
  await db.doc('categories/gundem').set({ placeholder_image_url: 'https://cdn/gundem.png' });
  await db.doc('sources/telif').set({ source_name: 'Telif', is_active: true });
  await db.doc(`articles/${articleIdForUrl(normalizeUrl(url))}`).set({ original_url: url, content_hash: 'h1', source_id: 'telif' });
  await db.doc('articles/kopya').set({ original_url: 'https://ornek.com/amp/1', content_hash: 'h1', source_id: 'telif' });
  await db.doc('articles/diger').set({ original_url: 'https://ornek.com/2', content_hash: 'h2', source_id: 'telif',
    category_id: db.doc('categories/gundem'), image_url: 'https://ornek.com/foto.jpg', has_original_image: true });

  assert.deepEqual(await removeArticleByUrl(db, url, { note: 'test' }), { removed: 2 });
  assert.equal((await db.doc('articles/kopya').get()).exists, false);

  assert.deepEqual(await disableSourceImages(db, 'telif'), { updated: 1 });
  const d = (await db.doc('articles/diger').get()).data();
  assert.equal(d.image_url, 'https://cdn/gundem.png');
  assert.equal((await db.doc('sources/telif').get()).get('allow_images'), false);

  assert.deepEqual(await removeSource(db, 'telif'), { removed: 1 });
  assert.equal((await db.doc('sources/telif').get()).get('is_active'), false);
  assert.equal((await db.collection('takedowns').get()).size, 3);
});

test('allow_images:false kaynakta yayıncı görseli alınmaz', () => {
  const item = { title: 'Başlık', link: 'https://ornek.com/x', enclosure: { url: 'https://ornek.com/foto.jpg', type: 'image/jpeg' } };
  const ctx = { categoryRef: null, placeholderImageUrl: 'https://cdn/p.png', now: new Date() };
  assert.equal(buildArticle(item, { ...ctx, source: { id: 's' } }).data.image_url, 'https://ornek.com/foto.jpg');
  const off = buildArticle(item, { ...ctx, source: { id: 's', allow_images: false } }).data;
  assert.equal(off.image_url, 'https://cdn/p.png');
  assert.equal(off.has_original_image, false);
});
