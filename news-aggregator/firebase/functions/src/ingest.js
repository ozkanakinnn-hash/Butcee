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
  searchKeywords,
} = require('./text');

const MAX_ITEMS_PER_FEED = 40;
const MAX_ARTICLE_AGE_DAYS = 7;
const FIRESTORE_IN_LIMIT = 30;
const SOURCE_CONCURRENCY = 5;
/**
 * Okuma kotası koruması: kaynağın bir önceki çalışmada gördüğü en yeni haberden bu kadar eski
 * haberler zaten işlenmiştir; veritabanına sorulmadan atlanır (geç yayınlananlar için pay bırakılır).
 */
const SEEN_WINDOW_MS = 45 * 60 * 1000;

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
      search_keywords: searchKeywords(title, source.source_name || ''),
      // "Son Dakika" kaynaklarından gelen haberler işaretlenir; uygulamadaki Son Dakika sekmesi
      // orderBy('breaking_at') ile yalnızca bu alana sahip haberleri listeler (ek dizin gerekmez).
      ...(source.is_breaking ? { breaking_at: Timestamp.fromDate(publishedAt) } : {}),
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

/**
 * Adayları veritabanıyla karşılaştırır.
 * @returns {{fresh: Array, existing: Array<{ref, hasBreaking: boolean, candidate}>}}
 *   fresh: yazılacak yeni haberler; existing: zaten var olanlar (Son Dakika işareti için).
 */
async function findExisting(db, articles) {
  if (articles.length === 0) return { fresh: [], existing: [] };
  const col = db.collection('articles');
  const existing = [];

  const snaps = await db.getAll(...articles.map((a) => col.doc(a.id)), { fieldMask: ['content_hash', 'breaking_at'] });
  const byId = new Map(snaps.filter((s) => s.exists).map((s) => [s.id, s]));
  let remaining = [];
  for (const a of articles) {
    const snap = byId.get(a.id);
    if (snap) existing.push({ ref: snap.ref, hasBreaking: snap.get('breaking_at') != null, candidate: a });
    else remaining.push(a);
  }

  const byHash = new Map();
  for (const hashes of chunk(remaining.map((a) => a.data.content_hash), FIRESTORE_IN_LIMIT)) {
    const qs = await col.where('content_hash', 'in', hashes).select('content_hash', 'breaking_at').get();
    qs.forEach((d) => byHash.set(d.get('content_hash'), d));
  }
  const fresh = [];
  for (const a of remaining) {
    const snap = byHash.get(a.data.content_hash);
    if (snap) existing.push({ ref: snap.ref, hasBreaking: snap.get('breaking_at') != null, candidate: a });
    else fresh.push(a);
  }
  return { fresh, existing };
}

/** Veritabanında zaten bulunan haberleri listeden çıkarır. */
async function filterExisting(db, articles) {
  return (await findExisting(db, articles)).fresh;
}

/**
 * Son Dakika kaynağında görülen ama başka bir akıştan önce kaydedilmiş haberleri işaretler.
 * Yalnızca işareti olmayanlara yazılır (her haber en fazla bir kez güncellenir).
 */
async function markBreaking(db, existing) {
  const targets = existing.filter((e) => !e.hasBreaking);
  if (targets.length === 0) return 0;
  const batch = db.batch();
  targets.forEach((e) => batch.update(e.ref, { breaking_at: e.candidate.data.published_at }));
  await batch.commit();
  return targets.length;
}

/** Yeni haberleri yazar. ALREADY_EXISTS (eşzamanlı çalışmada) hata sayılmaz. */
async function writeArticles(db, articles, onWritten) {
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
      .then(() => { written += 1; if (onWritten) onWritten(a); })
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

    const built = dedupeWithinBatch(items.map((it) => buildArticle(it, ctx)).filter(Boolean));
    const lastSeenMs = source.last_seen_published_at ? source.last_seen_published_at.toMillis() : 0;
    const candidates = built.filter((a) => a.data.published_at.toMillis() > lastSeenMs - SEEN_WINDOW_MS);
    result.skippedSeen = built.length - candidates.length;
    const newestMs = Math.max(lastSeenMs, ...built.map((a) => a.data.published_at.toMillis()));
    const { fresh, existing } = await findExisting(db, candidates);
    // Bildirim modülü için: bu çalışmada yazılan haberler ve Son Dakika olarak işaretlenenler.
    result.addedArticles = [];
    result.added = await writeArticles(db, fresh, (a) => result.addedArticles.push(a));
    if (source.is_breaking) {
      result.markedBreaking = await markBreaking(db, existing);
      result.breakingArticles = [
        ...result.addedArticles,
        ...existing.filter((e) => !e.hasBreaking).map((e) => ({ id: e.ref.id, data: e.candidate.data })),
      ];
    }

    await sourceDoc.ref.update({
      last_fetched_at: FieldValue.serverTimestamp(),
      last_error: null,
      etag: res.etag || null,
      last_modified: res.lastModified || null,
      last_added_count: result.added,
      ...(newestMs > 0 ? { last_seen_published_at: Timestamp.fromMillis(newestMs) } : {}),
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
  if (options.notify !== false) {
    const { sendNewsNotifications } = require('./notify');
    summary.notifications = await sendNewsNotifications(db, results, categories, options.notifyOptions);
  }
  logger.info('RSS çekimi tamamlandı', {
    ...summary,
    details: results.map(({ addedArticles, breakingArticles, ...rest }) => rest),
  });
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
  findExisting,
  filterExisting,
  markBreaking,
  writeArticles,
  ingestSource,
  ingestAllSources,
  deleteOldArticles,
  mapWithConcurrency,
  MAX_ARTICLE_AGE_DAYS,
};
