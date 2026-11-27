'use strict';

/**
 * Haber Toplayıcı — Cloud Functions giriş noktası
 *
 *  fetchRssFeeds     : 15 dakikada bir tüm aktif kaynakları çeker, yeni haberleri yazar.
 *  cleanupOldArticles: Her gece 04:00'te 30 günden eski haberleri siler.
 *
 * Dağıtım: `firebase deploy --only functions` (Blaze planı gerekir; zamanlayıcı ücretsiz kotada kalır).
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { setGlobalOptions } = require('firebase-functions/v2');
const { ingestAllSources, deleteOldArticles } = require('./src/ingest');
const { runSocial } = require('./src/social');

initializeApp();

// Türkiye'ye en yakın bölgelerden biri; Firestore konumunuzla aynı yapmanız önerilir.
setGlobalOptions({ region: 'europe-west1', maxInstances: 1 });

exports.fetchRssFeeds = onSchedule(
  {
    schedule: 'every 15 minutes',
    timeZone: 'Europe/Istanbul',
    timeoutSeconds: 300,
    memory: '512MiB',
    retryCount: 0, // Bir sonraki çalışma zaten 15 dk sonra; başarısız çalışmayı tekrarlamaya gerek yok.
    secrets: ['GEMINI_API_KEY'],
  },
  async () => {
    const db = getFirestore();
    await ingestAllSources(db);
    try {
      const r = await runSocial(db);
      if (r.status !== 'kapalı' && r.status !== 'bekle') {
        const { logger } = require('firebase-functions');
        logger.info('Sosyal paylaşım', r);
      }
    } catch (err) {
      const { logger } = require('firebase-functions');
      logger.warn('Sosyal paylaşım hatası', { error: err.message });
    }
  }
);

exports.cleanupOldArticles = onSchedule(
  {
    schedule: '0 4 * * *',
    timeZone: 'Europe/Istanbul',
    timeoutSeconds: 540,
  },
  async () => {
    await deleteOldArticles(getFirestore(), Number(process.env.ARTICLE_RETENTION_DAYS || 30));
  }
);
