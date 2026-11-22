'use strict';

/**
 * Bakım işleri (her RSS çekiminde, ucuz):
 *  - Arama dizini: `search_keywords` alanı olmayan eski haberlere bir kerelik eklenir
 *    (durum `app_meta/search` belgesinde; bittiğinde bir daha çalışmaz).
 *  - 7 günden eski bildirim kayıtlarını siler.
 */

const { FieldPath, FieldValue } = require('firebase-admin/firestore');
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

/**
 * Bir kerelik: konu kaynaklarının (Ekonomi, Spor…) "son görülen" işaretini sıfırlar. Böylece bir sonraki çekimde
 * akıştaki tüm haberler yeniden karşılaştırılır ve daha önce Gündem/Son Dakika'ya yazılmış olanlar konu
 * kategorisine taşınır (ingest.reassignToTopicCategory). Maliyet: bir kez ~1000 okuma.
 */
async function rescanTopicSources(db) {
  const metaRef = db.doc('app_meta/topic_rescan');
  if (((await metaRef.get()).data() || {}).version >= 1) return { done: true, reset: 0 };
  const sources = await db.collection('sources').where('is_active', '==', true).get();
  const batch = db.batch();
  let reset = 0;
  for (const d of sources.docs) {
    const cat = d.get('category_id');
    const catId = cat ? (typeof cat === 'string' ? cat.split('/').pop() : cat.id) : '';
    if (d.get('is_breaking') || catId === 'gundem' || catId === 'son-dakika') continue;
    batch.update(d.ref, { last_seen_published_at: FieldValue.delete(), etag: null, last_modified: null });
    reset += 1;
  }
  batch.set(metaRef, { version: 1 }, { merge: true });
  await batch.commit();
  return { done: true, reset };
}

/**
 * Sonradan eklenen kaynakları (seed-data.json'da `added_version` ≤ SOURCES_VERSION) Firestore'a bir kez yazar;
 * "seed" çalıştırmaya gerek kalmaz. Var olan kaynağın ayarlarına (is_active vb.) dokunmaz.
 */
const SOURCES_VERSION = 2;
// v2 (2026-11-22): Ekonomi tek kaynağa (NTV) bağlıydı ve boş kalıyordu → AA + TRT Haber; dünya/teknoloji de TRT ile desteklendi.
const NEW_SOURCE_IDS = ['aa-ekonomi', 'trt-ekonomi', 'trt-dunya', 'trt-teknoloji'];

async function addNewSources(db, { data = require('../scripts/seed-data.json') } = {}) {
  const metaRef = db.doc('app_meta/sources');
  if (((await metaRef.get()).data() || {}).version >= SOURCES_VERSION) return { done: true, added: 0 };
  const batch = db.batch();
  let added = 0;
  for (const s of data.sources.filter((x) => NEW_SOURCE_IDS.includes(x.id))) {
    const ref = db.collection('sources').doc(s.id);
    if ((await ref.get()).exists) continue;
    batch.set(ref, {
      source_name: s.source_name,
      rss_url: s.rss_url,
      category_id: db.collection('categories').doc(s.category),
      logo_url: `https://www.google.com/s2/favicons?domain=${s.domain}&sz=128`,
      is_active: s.is_active !== false,
      is_breaking: s.is_breaking === true,
      allow_images: s.allow_images !== false,
      topic_only: s.topic_only === true,
      created_at: FieldValue.serverTimestamp(),
    });
    added += 1;
  }
  batch.set(metaRef, { version: SOURCES_VERSION }, { merge: true });
  await batch.commit();
  return { done: true, added };
}

/**
 * Bir kerelik: hukuken sorunlu kaynakları emekliye ayırır (kaynak kapatılır, haberleri silinir, takedowns kaydı).
 * v1 (2026-11-11): Investing.com — kullanım koşulları ticari yeniden yayını yasaklıyor.
 */
const RETIRED_SOURCES = { version: 1, ids: ['investing-ekonomi', 'investing-borsa', 'investing-doviz'], note: 'Yayın öncesi telif temizliği: Investing.com koşulları ticari yeniden yayına kapalı' };

async function retireSources(db, cfg = RETIRED_SOURCES) {
  const metaRef = db.doc('app_meta/retired_sources');
  if (((await metaRef.get()).data() || {}).version >= cfg.version) return { done: true, removed: 0 };
  const { removeSource } = require('./takedown');
  let removed = 0;
  for (const id of cfg.ids) {
    if (!(await db.collection('sources').doc(id).get()).exists) continue;
    removed += (await removeSource(db, id, { note: cfg.note })).removed;
  }
  await metaRef.set({ version: cfg.version }, { merge: true });
  return { done: true, removed };
}

async function runMaintenance(db) {
  const retired = await retireSources(db);
  const newSources = await addNewSources(db);
  const topicRescan = await rescanTopicSources(db);
  const breaking = await moveBreakingSourceArticles(db);
  const search = await backfillSearchKeywords(db);
  const deletedNotifications = await cleanupNotificationLog(db);
  return { retired, newSources, topicRescan, breaking, search, deletedNotifications };
}

module.exports = { backfillSearchKeywords, moveBreakingSourceArticles, rescanTopicSources, addNewSources, retireSources, runMaintenance, SEARCH_INDEX_VERSION };
