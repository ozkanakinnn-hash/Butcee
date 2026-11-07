'use strict';

/**
 * Haber Akışı — Cloud Functions (Firebase Blaze planı)
 *
 *  fetchRssFeeds     : 20 dakikada bir haber çekimi + bildirimler, ardından piyasa verisi ve bakım işleri
 *  cleanupOldArticles: her gece 04:00'te 30 günden eski haberleri siler
 *  footballTick      : dakikada bir canlı skor turu (maç yoksa API isteği yapmadan saniyeler içinde biter)
 *
 * Dağıtım (Cloud Shell): docs/FIREBASE_TASIMA.md
 * Gizli değerler (Secret Manager): HIGHLIGHTLY_API_KEY (ücretsiz canlı skor, öncelikli) ve FOOTBALL_API_KEY
 * (API-Football, ücretli plan). Kullanılmayana `-` girilir: `firebase functions:secrets:set <AD>`
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { setGlobalOptions } = require('firebase-functions/v2');
const { defineSecret } = require('firebase-functions/params');
const { logger } = require('firebase-functions');
const { ingestAllSources, deleteOldArticles } = require('./src/ingest');
const { runMaintenance } = require('./src/maintenance');
const { updateMarket } = require('./src/market');
const { ApiFootball } = require('./src/football');
const { Highlightly } = require('./src/football-highlightly');
const { tick } = require('./src/football-job');

initializeApp();

// Aynı görev üst üste binmesin (maxInstances 1); bölge Firestore'dan bağımsız seçilebilir.
setGlobalOptions({ region: 'europe-west1', maxInstances: 1 });

const FOOTBALL_API_KEY = defineSecret('FOOTBALL_API_KEY');
// Ücretsiz canlı skor: Highlightly (Basic plan, günde 100 istek). Tanımlıysa API-Football yerine kullanılır.
const HIGHLIGHTLY_API_KEY = defineSecret('HIGHLIGHTLY_API_KEY');
const usable = (k) => Boolean(k && k.trim() && k.trim() !== '-');

exports.fetchRssFeeds = onSchedule(
  {
    schedule: 'every 20 minutes',
    timeZone: 'Europe/Istanbul',
    timeoutSeconds: 300,
    memory: '512MiB',
    retryCount: 0, // bir sonraki çalışma zaten 20 dk sonra
  },
  async () => {
    const db = getFirestore();
    await ingestAllSources(db);
    // Ek işler: hata verirlerse haber çekimi başarısız sayılmasın.
    try {
      logger.info('Bakım', await runMaintenance(db));
    } catch (err) {
      logger.warn('Bakım hatası', { error: err.message });
    }
    try {
      logger.info('Piyasa', await updateMarket(db));
    } catch (err) {
      logger.warn('Piyasa hatası', { error: err.message });
    }
  }
);

exports.cleanupOldArticles = onSchedule(
  { schedule: '0 4 * * *', timeZone: 'Europe/Istanbul', timeoutSeconds: 540 },
  async () => {
    await deleteOldArticles(getFirestore(), Number(process.env.ARTICLE_RETENTION_DAYS || 30));
  }
);

exports.footballTick = onSchedule(
  {
    schedule: 'every 1 minutes',
    timeZone: 'Europe/Istanbul',
    timeoutSeconds: 120,
    memory: '256MiB',
    retryCount: 0,
    secrets: [FOOTBALL_API_KEY, HIGHLIGHTLY_API_KEY],
  },
  async () => {
    const hl = HIGHLIGHTLY_API_KEY.value();
    const af = FOOTBALL_API_KEY.value();
    const makeApi = () =>
      usable(hl) ? new Highlightly({ key: hl.trim() }) : usable(af) ? new ApiFootball({ key: af.trim() }) : null;
    const result = await tick(getFirestore(), makeApi, getMessaging());
    if (!['idle', 'waiting-kickoff', 'throttled'].includes(result)) logger.info('Futbol', { result });
  }
);
