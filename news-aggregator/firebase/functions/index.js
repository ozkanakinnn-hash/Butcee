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
  },
  async () => {
    await ingestAllSources(getFirestore());
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
