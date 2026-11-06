'use strict';

/**
 * RSS → Firestore aktarımı ve mükerrer (duplicate) kontrolü.
 *
 * Mükerrer kontrolü 3 katmanlıdır:
 *  1. Belge kimliği = normalleştirilmiş original_url'nin hash'i. Aynı URL ikinci kez
 *     `create()` edilemez (Firestore ALREADY_EXISTS döner) → yarış durumunda bile güvenli.
 *  2. Yazmadan önce bu kimlikler `getAll` ile topluca okunur; var olanlar atlanır
 *     (gereksiz yazma maliyeti oluşmaz).
 *  3. content_hash (başlık + yayın günü) ile `in` sorgusu: aynı haber farklı URL ile
 *     (mobil adres, farklı kaynak) gelirse yine eklenmez.
 */

const { FieldValue, Timestamp } = require('firebase-admin/firestore');
const { logger } = require('firebase-functions');
const { fetchFeed } = require('./feed');
const {
  stripHtml,
  truncate,
  cleanTitle,
  normalizeUrl,
  extractImage,
  articleIdForUrl,
  contentHash,
  parsePublishedAt,
} = require('./text');

const MAX_ITEMS_PER_FEED = 40;
const MAX_ARTICLE_AGE_DAYS = 7;
const FIRESTORE_IN_LIMIT = 30;
const SOURCE_CONCURRENCY = 5;

/**
 * Bir RSS öğesini `articles` belgesine çevirir. Başlığı veya geçerli bağlantısı
 * olmayan öğeler için null döner.
 */
function buildArticle(item, { source, categoryRef, placeholderImageUrl, feedLink, now = new Date() }) {
  const originalUrl = normalizeUrl(item.link || item.guid || (item.id && String(item.id)));
  const title = cleanTitle(item.title);
  if (!originalUrl || !title) return null;

  const publishedAt = parsePublishedAt(item, now);
  if (now.getTime() - publishedAt.getTime() > MAX_ARTICLE_AGE_DAYS * 86_400_000) return null;

  // rss-parser, RSS <description> alanını `content` olarak da verir; Atom'da `summary` kullanılır.
  const rawSummary = item.description || item.summary || item.content || item.contentEncoded || '';
  let summary = truncate(stripHtml(rawSummary));
  if (summary === title) summary = '';

  const imageUrl = extractImage(item, feedLink || originalUrl) || placeholderImageUrl || '';

  return {
    id: articleIdForUrl(originalUrl),
    data: {
      title,
      summary,
      image_url: imageUrl,
      has_original_image: imageUrl !== '' && imageUrl !== placeholderImageUrl,
      original_url: originalUrl,
      source_name: source.source_name || '',
      source_id: source.id,
      source_logo_url: source.logo_url || '',
      category_id: categoryRef || null,
      published_at: Timestamp.fromDate(publishedAt),
      content_hash: contentHash(title, publishedAt),
    },
  };
}

function chunk(array, size) {
  const out = [];
  for (let i = 0; i < array.length; i += size) out.push(array.slice(i, i + size));
  return out;
}

/** Aynı akış içindeki tekrarları (aynı kimlik veya aynı content_hash) eler. */
function dedupeWithinBatch(articles) {
  const seenIds = new Set();
  const seenHashes = new Set();
  return articles.filter((a) => {
    if (seenIds.has(a.id) || seenHashes.has(a.data.content_hash)) return false;
    seenIds.add(a.id);
    seenHashes.add(a.data.content_hash);
    return true;
  });
}

/** Veritabanında zaten bulunan haberleri listeden çıkarır. */
async function filterExisting(db, articles) {
  if (articles.length === 0) return [];
  const col = db.collection('articles');

  const snaps = await db.getAll(...articles.map((a) => col.doc(a.id)), { fieldMask: ['content_hash'] });
  const existingIds = new Set(snaps.filter((s) => s.exists).map((s) => s.id));
  let remaining = articles.filter((a) => !existingIds.has(a.id));

  const existingHashes = new Set();
  for (const hashes of chunk(remaining.map((a) => a.data.content_hash), FIRESTORE_IN_LIMIT)) {
    const qs = await col.where('content_hash', 'in', hashes).select('content_hash').get();
    qs.forEach((d) => existingHashes.add(d.get('content_hash')));
  }
  remaining = remaining.filter((a) => !existingHashes.has(a.data.content_hash));
  return remaining;
}

/** Yeni haberleri yazar. ALREADY_EXISTS (eşzamanlı çalışmada) hata sayılmaz. */
async function writeArticles(db, articles) {
  if (articles.length === 0) return 0;
  let written = 0;
  const writer = db.bulkWriter();
  writer.onWriteError((err) => {
    if (err.code === 6 /* ALREADY_EXISTS */) return false;
    return err.failedAttempts < 3;
  });
  const col = db.collection('articles');
  const tasks = articles.map((a) =>
    writer
      .create(col.doc(a.id), { ...a.data, created_at: FieldValue.serverTimestamp() })
      .then(() => { written += 1; })
      .catch((err) => {
        if (err.code !== 6) logger.warn('Haber yazılamadı', { id: a.id, error: err.message });
      })
  );
  await writer.close();
  await Promise.all(tasks);
  return written;
}

/** Kategori dokümanlarını bir kez okuyup önbelleğe alır (placeholder görseli için). */
async function loadCategories(db) {
  const map = new Map();
  const snap = await db.collection('categories').get();
  snap.forEach((doc) => map.set(doc.id, { ref: doc.ref, ...doc.data() }));
  return map;
}

function resolveCategoryRef(db, value) {
  if (!value) return null;
  if (typeof value === 'string') {
    const id = value.includes('/') ? value.split('/').pop() : value;
    return db.collection('categories').doc(id);
  }
  return value; // Zaten DocumentReference
}

/** Tek bir kaynağı işler ve sonucu kaynak belgesine yazar. */
async function ingestSource(db, sourceDoc, categories, { fetchImpl, now = new Date() } = {}) {
  const source = { id: sourceDoc.id, ...sourceDoc.data() };
  const result = { source: source.source_name, fetched: 0, added: 0, notModified: false, error: null };

  try {
    const res = await fetchFeed(source.rss_url, {
      etag: source.etag,
      lastModified: source.last_modified,
      fetchImpl,
    });

    if (res.notModified) {
      result.notModified = true;
      await sourceDoc.ref.update({ last_fetched_at: FieldValue.serverTimestamp(), last_error: null });
      return result;
    }

    const categoryRef = resolveCategoryRef(db, source.category_id);
    const category = categoryRef ? categories.get(categoryRef.id) : null;
    const ctx = {
      source,
      categoryRef,
      placeholderImageUrl: (category && category.placeholder_image_url) || process.env.DEFAULT_PLACEHOLDER_IMAGE_URL || '',
      feedLink: res.feed.link,
      now,
    };

    const items = (res.feed.items || []).slice(0, MAX_ITEMS_PER_FEED);
    result.fetched = items.length;

    const candidates = dedupeWithinBatch(items.map((it) => buildArticle(it, ctx)).filter(Boolean));
    const fresh = await filterExisting(db, candidates);
    result.added = await writeArticles(db, fresh);

    await sourceDoc.ref.update({
      last_fetched_at: FieldValue.serverTimestamp(),
      last_error: null,
      etag: res.etag || null,
      last_modified: res.lastModified || null,
      last_added_count: result.added,
    });
  } catch (err) {
    result.error = err.message;
    logger.error('Kaynak işlenemedi', { source: source.source_name, url: source.rss_url, error: err.message });
    await sourceDoc.ref
      .update({ last_fetched_at: FieldValue.serverTimestamp(), last_error: String(err.message).slice(0, 500) })
      .catch(() => {});
  }
  return result;
}

/** Basit eşzamanlılık sınırı: aynı anda en fazla `limit` kaynak indirilir. */
async function mapWithConcurrency(items, limit, fn) {
  const results = new Array(items.length);
  let next = 0;
  async function worker() {
    while (next < items.length) {
      const i = next++;
      results[i] = await fn(items[i], i);
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, worker));
  return results;
}

/** Tüm aktif kaynakları işler. Zamanlanmış fonksiyon ve yerel betik bunu çağırır. */
async function ingestAllSources(db, options = {}) {
  const [sourcesSnap, categories] = await Promise.all([
    db.collection('sources').where('is_active', '==', true).get(),
    loadCategories(db),
  ]);

  const results = await mapWithConcurrency(sourcesSnap.docs, SOURCE_CONCURRENCY, (doc) =>
    ingestSource(db, doc, categories, options)
  );

  const summary = {
    sources: results.length,
    added: results.reduce((n, r) => n + r.added, 0),
    failed: results.filter((r) => r.error).length,
    notModified: results.filter((r) => r.notModified).length,
  };
  logger.info('RSS çekimi tamamlandı', { ...summary, details: results });
  return { summary, results };
}

/** Belirtilen günden eski haberleri siler (veritabanı maliyetini sabit tutar). */
async function deleteOldArticles(db, maxAgeDays = 30) {
  const cutoff = Timestamp.fromMillis(Date.now() - maxAgeDays * 86_400_000);
  let deleted = 0;
  for (;;) {
    const snap = await db.collection('articles').where('published_at', '<', cutoff).limit(400).get();
    if (snap.empty) break;
    const batch = db.batch();
    snap.docs.forEach((d) => batch.delete(d.ref));
    await batch.commit();
    deleted += snap.size;
    if (snap.size < 400) break;
  }
  logger.info('Eski haberler silindi', { deleted, maxAgeDays });
  return deleted;
}

module.exports = {
  buildArticle,
  dedupeWithinBatch,
  filterExisting,
  writeArticles,
  ingestSource,
  ingestAllSources,
  deleteOldArticles,
  mapWithConcurrency,
  MAX_ARTICLE_AGE_DAYS,
};
