'use strict';

/**
 * Futbol izleyicisi (komut satırı). Firebase'de bunun yerine `footballTick` dakikada bir çalışır.
 *
 *   FOOTBALL_API_KEY=... GOOGLE_APPLICATION_CREDENTIALS=... node scripts/football-watch.js [watch|check|sync-teams]
 *
 *  check      : planı, kalan isteği ve güncel sezon verisine erişimi test eder (3 istek, Firestore'a yazmaz)
 *  sync-teams : Süper Lig takımlarını yeniler
 *  watch      : takımlar/günün maçları gerekirse güncellenir; maç yakın veya canlıysa maçlar bitene kadar izlenir.
 *  hl-raw     : (Highlightly) Süper Lig kaydını ve bugünkü/sıradaki maçların HAM yanıtını yazdırır — biçim doğrulama.
 *
 * HIGHLIGHTLY_API_KEY tanımlıysa Highlightly, değilse FOOTBALL_API_KEY ile API-Football kullanılır.
 */

const { getFirestore } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { initAdmin } = require('./init-admin');
const fb = require('../src/football');
const job = require('../src/football-job');
const { Highlightly, toFixture } = require('../src/football-highlightly');

const MAX_RUN_MS = 5.5 * 60 * 60 * 1000;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const log = (...a) => console.log(new Date().toISOString().slice(11, 19), ...a);

async function watch(db, api, messaging, { sleep: wait_ = sleep } = {}) {
  const started = Date.now();
  const meta = (await db.doc('football_meta/state').get()).data() || {};
  if (!(await job.prepare(db, api, meta))) return;

  let active = await job.activeMatches(db);
  if (active.length === 0) {
    log('Yakında veya şu an canlı maç yok; çıkılıyor.');
    return;
  }
  log(`İzlenecek maçlar: ${active.map((m) => `${m.home.name}-${m.away.name}`).join(', ')}`);

  while (active.length && Date.now() - started < MAX_RUN_MS) {
    const nextKickoff = Math.min(...active.map((m) => m.kickoff.toMillis()));
    const anyLive = active.some((m) => m.is_live);
    if (!anyLive && nextKickoff - Date.now() > 60_000) {
      const wait = Math.min(nextKickoff - Date.now() - 30_000, 15 * 60_000);
      log(`İlk maça ${Math.round((nextKickoff - Date.now()) / 60000)} dk var; ${Math.round(wait / 60000)} dk bekleniyor.`);
      await wait_(Math.max(wait, 30_000));
      active = await job.activeMatches(db);
      continue;
    }
    active = await job.pollOnce(db, api, messaging, active);
    if (api.remaining !== null && api.remaining <= 1) {
      log('Günlük istek hakkı bitti; izleme durduruldu.');
      return;
    }
    const wait = job.nextWaitMs(api, active);
    log(`Kalan istek: ${api.remaining ?? '?'} | sonraki kontrol ${Math.round(wait / 1000)} sn sonra`);
    if (active.length) await wait_(wait);
  }
  log(`İzleme bitti. Bu çalışmada ${api.used} istek kullanıldı.`);
}

async function hlRaw(api) {
  if (!(api instanceof Highlightly)) throw new Error('hl-raw için HIGHLIGHTLY_API_KEY gerekli');
  const short = (o) => JSON.stringify(o, null, 1).slice(0, 1500);
  const id = await api.findLeague();
  log(`Süper Lig bulundu: id=${id}`);
  console.log(short(api.league));
  const today = fb.istanbulDate();
  let raw = await api.raw('/matches', { leagueId: id, date: today, timezone: 'Europe/Istanbul', limit: 5 });
  let list = Array.isArray(raw) ? raw : raw.data || [];
  if (!list.length) {
    log('Bugün maç yok; sezonun maçlarından örnek alınıyor…');
    raw = await api.raw('/matches', { leagueId: id, season: (await api.get('/leagues'))[0].seasons[0].year, limit: 3 });
    list = Array.isArray(raw) ? raw : raw.data || [];
  }
  log(`Maç sayısı: ${list.length}. İlk maçın HAM yanıtı:`);
  console.log(short(list[0] || raw));
  if (list[0]) {
    log('Uygulamanın göreceği hali:');
    console.log(short(toFixture(list[0])));
    const detail = await api.raw(`/matches/${list[0].id}`);
    log('Maç ayrıntısı (olaylar/goller):');
    console.log(short(detail));
  }
  log(`Bu kontrol ${api.used} istek kullandı; kalan (başlıktan): ${api.remaining ?? 'bildirilmedi'}`);
}

async function main() {
  const mode = process.argv[2] || 'watch';
  const hlKey = (process.env.HIGHLIGHTLY_API_KEY || '').trim();
  const api = hlKey && hlKey !== '-'
    ? new Highlightly({ key: hlKey })
    : new fb.ApiFootball({ key: process.env.FOOTBALL_API_KEY });
  if (mode === 'hl-raw') return hlRaw(api);
  if (mode === 'check') return job.check(api);
  initAdmin();
  const db = getFirestore();
  if (mode === 'sync-teams') return job.syncTeams(db, api, {});
  return watch(db, api, getMessaging());
}

if (require.main === module) {
  main().catch((err) => {
    console.error(err.message);
    process.exit(1);
  });
}

module.exports = { activeMatches: job.activeMatches, watch };
