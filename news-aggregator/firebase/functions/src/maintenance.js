'use strict';

/**
 * Bakım işleri (her RSS çekiminde, ucuz):
 *  - Arama dizini: `search_keywords` alanı olmayan eski haberlere bir kerelik eklenir
 *    (durum `app_meta/search` belgesinde; bittiğinde bir daha çalışmaz).
 *  - 7 günden eski bildirim kayıtlarını siler.
 */

const { FieldPath } = require('firebase-admin/firestore');
const { searchKeywords } = require('./text');
const { cleanupNotificationLog } = require('./notification-log');

const SEARCH_INDEX_VERSION = 1;
const PAGE = 300;

async function backfillSearchKeywords(db, { maxDocs = 3000 } = {}) {
  const metaRef = db.doc('app_meta/search');
  const meta = (await metaRef.get()).data() || {};
  if (meta.version >= SEARCH_INDEX_VERSION) return { done: true, updated: 0 };

  let last = meta.cursor || null;
  let updated = 0;
  let scanned = 0;
  for (;;) {
    let q = db.collection('articles').orderBy(FieldPath.documentId()).select('title', 'source_name', 'search_keywords').limit(PAGE);
    if (last) q = q.startAfter(last);
    const snap = await q.get();
    if (snap.empty) {
      await metaRef.set({ version: SEARCH_INDEX_VERSION, cursor: null }, { merge: true });
      return { done: true, updated, scanned };
    }
    const batch = db.batch();
    let n = 0;
    for (const d of snap.docs) {
      if (Array.isArray(d.get('search_keywords'))) continue;
      batch.update(d.ref, { search_keywords: searchKeywords(d.get('title') || '', d.get('source_name') || '') });
      n += 1;
    }
    if (n) await batch.commit();
    updated += n;
    scanned += snap.size;
    last = snap.docs[snap.docs.length - 1].id;
    if (scanned >= maxDocs) {
      await metaRef.set({ cursor: last }, { merge: true }); // sonraki çalışmada kaldığı yerden
      return { done: false, updated, scanned };
    }
  }
}

/**
 * Bir kerelik: Son Dakika kaynaklarının daha önce Gündem'e yazılmış haberlerini Son Dakika kategorisine taşır.
 */
async function moveBreakingSourceArticles(db) {
  const metaRef = db.doc('app_meta/breaking_category');
  if (((await metaRef.get()).data() || {}).version >= 1) return { done: true, moved: 0 };
  const sources = await db.collection('sources').where('is_breaking', '==', true).get();
  const ids = sources.docs.map((d) => d.id);
  const target = db.collection('categories').doc('son-dakika');
  let moved = 0;
  if (ids.length) {
    for (;;) {
      const snap = await db.collection('articles').where('source_id', 'in', ids.slice(0, 30)).limit(400).get();
      const batch = db.batch();
      let n = 0;
      for (const d of snap.docs) {
        const cat = d.get('category_id');
        if (cat && cat.id === 'son-dakika') continue;
        batch.update(d.ref, { category_id: target });
        n += 1;
      }
      if (n) await batch.commit();
      moved += n;
      if (n === 0 || snap.size < 400) break;
    }
  }
  await metaRef.set({ version: 1 }, { merge: true });
  return { done: true, moved };
}

async function runMaintenance(db) {
  const breaking = await moveBreakingSourceArticles(db);
  const search = await backfillSearchKeywords(db);
  const deletedNotifications = await cleanupNotificationLog(db);
  return { breaking, search, deletedNotifications };
}

module.exports = { backfillSearchKeywords, moveBreakingSourceArticles, runMaintenance, SEARCH_INDEX_VERSION };
