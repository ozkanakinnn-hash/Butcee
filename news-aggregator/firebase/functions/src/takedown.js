'use strict';

/**
 * Telif "Uyar-Kaldır" işlemleri (5651 sayılı Kanun / hak sahibi talebi).
 *  - removeArticleByUrl: tek bir haberi siler (aynı haberin farklı adresli kopyaları dahil)
 *  - removeSource: kaynağı kapatır (yeni haber çekilmez), mevcut haberlerini siler
 *  - disableSourceImages: kaynağın haberlerinde yayıncı görseli yerine kategori görseli kullanılır
 * Her işlem `takedowns` koleksiyonuna kayıt düşer (talep tarihi, hedef, silinen sayısı).
 */

const { FieldValue } = require('firebase-admin/firestore');
const { normalizeUrl, articleIdForUrl } = require('./text');

async function deleteDocs(db, docs) {
  for (let i = 0; i < docs.length; i += 400) {
    const batch = db.batch();
    docs.slice(i, i + 400).forEach((d) => batch.delete(d.ref));
    await batch.commit();
  }
  return docs.length;
}

async function record(db, entry) {
  await db.collection('takedowns').add({ ...entry, at: FieldValue.serverTimestamp() });
}

async function removeArticleByUrl(db, url, { note = '' } = {}) {
  const normalized = normalizeUrl(url);
  if (!normalized) throw new Error('Geçersiz adres');
  const col = db.collection('articles');
  const doc = await col.doc(articleIdForUrl(normalized)).get();
  const docs = doc.exists ? [doc] : (await col.where('original_url', '==', normalized).get()).docs;
  // Aynı haberin başka adresle kaydedilmiş kopyaları (content_hash aynı)
  const hash = docs[0] && docs[0].get('content_hash');
  if (hash) {
    const dupes = await col.where('content_hash', '==', hash).get();
    for (const d of dupes.docs) if (!docs.some((x) => x.id === d.id)) docs.push(d);
  }
  const removed = await deleteDocs(db, docs);
  await record(db, { type: 'article', target: normalized, removed, note });
  return { removed };
}

async function removeSource(db, sourceId, { note = '' } = {}) {
  const ref = db.collection('sources').doc(sourceId);
  if (!(await ref.get()).exists) throw new Error(`Kaynak bulunamadı: ${sourceId}`);
  await ref.update({ is_active: false, takedown_at: FieldValue.serverTimestamp() });
  let removed = 0;
  for (;;) {
    const snap = await db.collection('articles').where('source_id', '==', sourceId).limit(400).get();
    if (snap.empty) break;
    removed += await deleteDocs(db, snap.docs);
  }
  await record(db, { type: 'source', target: sourceId, removed, note });
  return { removed };
}

async function disableSourceImages(db, sourceId, { note = '' } = {}) {
  const ref = db.collection('sources').doc(sourceId);
  const src = await ref.get();
  if (!src.exists) throw new Error(`Kaynak bulunamadı: ${sourceId}`);
  await ref.update({ allow_images: false });
  // Mevcut haberlerde yayıncı görselini kategori görseliyle değiştir
  const cats = new Map((await db.collection('categories').get()).docs.map((d) => [d.id, d.data()]));
  const snap = await db.collection('articles').where('source_id', '==', sourceId).get();
  let updated = 0;
  for (let i = 0; i < snap.docs.length; i += 400) {
    const batch = db.batch();
    for (const d of snap.docs.slice(i, i + 400)) {
      const cat = d.get('category_id');
      const placeholder = (cat && cats.get(cat.id) && cats.get(cat.id).placeholder_image_url) || '';
      batch.update(d.ref, { image_url: placeholder, has_original_image: false });
      updated += 1;
    }
    await batch.commit();
  }
  await record(db, { type: 'images', target: sourceId, removed: updated, note });
  return { updated };
}

module.exports = { removeArticleByUrl, removeSource, disableSourceImages };
