'use strict';

/**
 * Haber Akışı — Cloud Functions (Firebase Blaze planı)
 *
 *  fetchRssFeeds     : 20 dakikada bir haber çekimi + bildirimler, ardından piyasa verisi ve bakım işleri
 *  cleanupOldArticles: her gece 04:00'te 30 günden eski haberleri siler
 *  briefingTick      : bülten saatlerinde 10 dk'da bir; o dilimin gündemi yazılmadıysa üretir ve bildirimi gönderir
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
const { runSocial } = require('./src/social');
const { setGlobalOptions } = require('firebase-functions/v2');
const { defineSecret } = require('firebase-functions/params');
const { logger } = require('firebase-functions');
const { ingestAllSources, deleteOldArticles } = require('./src/ingest');
const { runMaintenance } = require('./src/maintenance');
const { updateMarket } = require('./src/market');
const { ApiFootball } = require('./src/football');
const { Highlightly } = require('./src/football-highlightly');
const { tick } = require('./src/football-job');
const { runTestMatch } = require('./src/test-match');

initializeApp();

// Aynı görev üst üste binmesin (maxInstances 1); bölge Firestore'dan bağımsız seçilebilir.
setGlobalOptions({ region: 'europe-west1', maxInstances: 1 });

const FOOTBALL_API_KEY = defineSecret('FOOTBALL_API_KEY');
// Ücretsiz canlı skor: Highlightly (Basic plan, günde 100 istek). Tanımlıysa API-Football yerine kullanılır.
const HIGHLIGHTLY_API_KEY = defineSecret('HIGHLIGHTLY_API_KEY');
// Telegram kanalı için bot jetonu (BotFather). Yoksa kurulumda "-" girilir; paylaşım o zaman hiçbir şey yapmaz.
const TELEGRAM_BOT_TOKEN = defineSecret('TELEGRAM_BOT_TOKEN');
// Bülten AI özeti (Gemini). Model `app_meta/ai.model` belgesinden değiştirilir; anahtar yoksa özet atlanır.
const GEMINI_API_KEY = defineSecret('GEMINI_API_KEY');
const usable = (k) => Boolean(k && k.trim() && k.trim() !== '-');

exports.fetchRssFeeds = onSchedule(
  {
    schedule: 'every 20 minutes',
    timeZone: 'Europe/Istanbul',
    timeoutSeconds: 300,
    memory: '512MiB',
    retryCount: 0, // bir sonraki çalışma zaten 20 dk sonra
    secrets: [TELEGRAM_BOT_TOKEN, GEMINI_API_KEY],
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
    try {
      const token = TELEGRAM_BOT_TOKEN.value();
      const r = await runSocial(db, { token: usable(token) ? token.trim() : '' });
      if (r.status !== 'kapalı' && r.status !== 'bekle') logger.info('Sosyal paylaşım', r);
    } catch (err) {
      logger.warn('Sosyal paylaşım hatası', { error: err.message });
    }
  }
);

exports.cleanupOldArticles = onSchedule(
  { schedule: '0 4 * * *', timeZone: 'Europe/Istanbul', timeoutSeconds: 540 },
  async () => {
    await deleteOldArticles(getFirestore(), Number(process.env.ARTICLE_RETENTION_DAYS || 30));
  }
);

// Bülten güvencesi: 08:30–10:59 ve 19:00–21:59 arası 10 dakikada bir. Gündem belgesi o dilim için yazılmadıysa
// (haber çekimi gecikti/hata verdi) üretir; bildirim gitmediyse gönderir. Uygulama böylece dünkü bülteni göstermez.
exports.briefingTick = onSchedule(
  { schedule: '30,40,50 8 * * *', timeZone: 'Europe/Istanbul', timeoutSeconds: 180, memory: '512MiB', retryCount: 1, secrets: [GEMINI_API_KEY] },
  async () => runBriefingTick(),
);
exports.briefingTickLate = onSchedule(
  { schedule: '*/10 9,10,19,20,21 * * *', timeZone: 'Europe/Istanbul', timeoutSeconds: 180, memory: '512MiB', retryCount: 1, secrets: [GEMINI_API_KEY] },
  async () => runBriefingTick(),
);
async function runBriefingTick() {
  const { ensureBriefing } = require('./src/briefing-job');
  const r = await ensureBriefing(getFirestore(), { messaging: getMessaging() });
  if (r.status !== 'güncel') logger.info('Bülten güvencesi', r);
}

// Deprem uyarısı: 2 dakikada bir AFAD (yedek EMSC); M≥4.5 'deprem' konusuna, M≥5 son dakika abonelerine de.
exports.earthquakeTick = onSchedule(
  { schedule: 'every 2 minutes', timeZone: 'Europe/Istanbul', timeoutSeconds: 60, memory: '256MiB', retryCount: 0 },
  async () => {
    const { runEarthquake } = require('./src/earthquake');
    const r = await runEarthquake(getFirestore(), getMessaging());
    if (r.status !== 'tamam' || (r.alerts && r.alerts.length)) logger.info('Deprem', r);
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
    makeApi.provider = usable(hl) ? 'highlightly' : usable(af) ? 'api-football' : null;
    // Test maçı (football_meta/test varsa): panel + gol bildirimi denemesi, API isteği harcamaz
    try {
      const t = await runTestMatch(getFirestore(), getMessaging());
      if (t && t !== 'test-waiting') logger.info('Test maçı', { result: t });
    } catch (err) {
      logger.warn('Test maçı hatası', { error: err.message });
    }
    // fast: istek hakkı yeterliyse görev içinde 15 sn'de bir kontrol (ücretli planda gol anında)
    const result = await tick(getFirestore(), makeApi, getMessaging(), Date.now(), { fast: true });
    // Maç günü teşhisi: yalnızca hiç maç yokken sessiz (bekleme/kısıtlama nedenleri de günlükte görünsün)
    if (result !== 'idle') console.log(`Futbol turu: ${result}`);
  }
);
