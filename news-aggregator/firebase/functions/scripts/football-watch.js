'use strict';

/**
 * Futbol izleyicisi — GitHub Actions'ta 15 dakikada bir başlar (news-football.yml).
 *
 *   FOOTBALL_API_KEY=... GOOGLE_APPLICATION_CREDENTIALS=... node scripts/football-watch.js [watch|check|sync-teams]
 *
 *  check      : planı, kalan isteği ve güncel sezon verisine erişimi test eder (3 istek, Firestore'a yazmaz)
 *  sync-teams : Süper Lig takımlarını yeniler
 *  watch      : (varsayılan) takımlar/günün maçları gerekirse güncellenir; maç yakın veya canlıysa
 *               maçlar bitene kadar skor izlenir ve bildirim gönderilir.
 */

const { getFirestore, Timestamp } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { initAdmin } = require('./init-admin');
const fb = require('../src/football');

const MAX_RUN_MS = 5.5 * 60 * 60 * 1000; // GitHub işi 6 saatte kesilir
const START_EARLY_MS = 35 * 60 * 1000; // maçtan bu kadar önce başlayan çalışma bekler
const MATCH_LENGTH_MIN = 125;
const TEAMS_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const log = (...a) => console.log(new Date().toISOString().slice(11, 19), ...a);

async function check(api) {
  const status = await api.get('/status');
  const sub = status.subscription || {};
  const req = status.requests || {};
  log(`Plan: ${sub.plan || '?'} | bugün kullanılan: ${req.current}/${req.limit_day} | aktif: ${sub.active}`);

  const leagues = await api.get('/leagues', { id: fb.SUPER_LIG_ID, current: 'true' });
  const season = leagues[0] && leagues[0].seasons && leagues[0].seasons[0];
  if (!season) throw new fb.FootballApiError('Süper Lig güncel sezonu bulunamadı');
  log(`Süper Lig güncel sezon: ${season.year} (${season.start} → ${season.end}) | canlı skor kapsamı: ${season.coverage && season.coverage.fixtures && season.coverage.fixtures.events ? 'var' : 'yok'}`);

  try {
    const from = fb.istanbulDate();
    const to = fb.istanbulDate(new Date(Date.now() + 10 * 86_400_000));
    const fixtures = await api.get('/fixtures', { league: fb.SUPER_LIG_ID, season: season.year, from, to, timezone: 'Europe/Istanbul' });
    log(`✔ Güncel sezona erişim VAR. Önümüzdeki 10 günde ${fixtures.length} maç:`);
    for (const f of fixtures.slice(0, 5)) log(`   ${f.fixture.date.slice(0, 16)}  ${f.teams.home.name} - ${f.teams.away.name}`);
  } catch (err) {
    log(`✘ Güncel sezona erişim YOK: ${err.message}`);
    log('  Ücretsiz plan bu sezonu kapsamıyor olabilir. Canlı skor için ücretli plan gerekir.');
    process.exitCode = 2;
  }
  log(`Kalan günlük istek: ${api.remaining}`);
}

async function currentSeason(api) {
  const leagues = await api.get('/leagues', { id: fb.SUPER_LIG_ID, current: 'true' });
  const s = leagues[0] && leagues[0].seasons && leagues[0].seasons[0];
  if (!s) throw new fb.FootballApiError('Güncel sezon bulunamadı');
  return s.year;
}

async function syncTeams(db, api, meta) {
  const season = await currentSeason(api);
  const teams = await api.get('/teams', { league: fb.SUPER_LIG_ID, season });
  const batch = db.batch();
  for (const t of teams) {
    batch.set(db.doc(`football_teams/${t.team.id}`), {
      name: t.team.name,
      code: t.team.code || '',
      logo: t.team.logo || '',
      league_id: fb.SUPER_LIG_ID,
      season,
      updated_at: Timestamp.now(),
    }, { merge: true });
  }
  batch.set(db.doc('football_meta/state'), { season, teams_synced_at: Timestamp.now() }, { merge: true });
  await batch.commit();
  meta.season = season;
  log(`Takımlar güncellendi: ${teams.length} takım (sezon ${season})`);
}

async function syncTodayFixtures(db, api, meta) {
  const today = fb.istanbulDate();
  const fixtures = await api.get('/fixtures', {
    league: fb.SUPER_LIG_ID, season: meta.season, date: today, timezone: 'Europe/Istanbul',
  });
  const batch = db.batch();
  for (const f of fixtures) {
    const doc = fb.matchDocFromFixture(f);
    batch.set(db.doc(`matches/${doc.fixture_id}`), { ...doc, updated_at: Timestamp.now() }, { merge: true });
  }
  batch.set(db.doc('football_meta/state'), { fixtures_date: today }, { merge: true });
  await batch.commit();
  log(`Bugünün maçları (${today}): ${fixtures.length}`);
}

async function cleanupOldMatches(db) {
  const cutoff = Timestamp.fromMillis(Date.now() - 3 * 86_400_000);
  const old = await db.collection('matches').where('kickoff', '<', cutoff).limit(200).get();
  if (old.empty) return;
  const batch = db.batch();
  old.docs.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  log(`Eski maç kayıtları silindi: ${old.size}`);
}

/** Bugünkü maçlardan izlenmesi gerekenler (başlamak üzere veya canlı). */
async function activeMatches(db, now = Date.now()) {
  const from = Timestamp.fromMillis(now - 4 * 3_600_000);
  const to = Timestamp.fromMillis(now + START_EARLY_MS);
  const snap = await db.collection('matches').where('kickoff', '>=', from).where('kickoff', '<=', to).get();
  return snap.docs.map((d) => d.data()).filter((m) => !m.is_finished);
}

async function watch(db, api, messaging, { sleep: wait_ = sleep } = {}) {
  const started = Date.now();
  const metaRef = db.doc('football_meta/state');
  const meta = (await metaRef.get()).data() || {};

  const teamsAge = meta.teams_synced_at ? Date.now() - meta.teams_synced_at.toMillis() : Infinity;
  if (!meta.season || teamsAge > TEAMS_TTL_MS) await syncTeams(db, api, meta);
  if (meta.fixtures_date !== fb.istanbulDate()) {
    await syncTodayFixtures(db, api, meta);
    await cleanupOldMatches(db);
  }

  let active = await activeMatches(db);
  if (active.length === 0) {
    log('Yakında veya şu an canlı maç yok; çıkılıyor.');
    return;
  }
  log(`İzlenecek maçlar: ${active.map((m) => `${m.home.name}-${m.away.name}`).join(', ')}`);

  while (active.length && Date.now() - started < MAX_RUN_MS) {
    // Henüz başlamamışsa başlama saatine kadar istek harcamadan bekle.
    const nextKickoff = Math.min(...active.map((m) => m.kickoff.toMillis()));
    const anyLive = active.some((m) => m.is_live);
    if (!anyLive && nextKickoff - Date.now() > 60_000) {
      const wait = Math.min(nextKickoff - Date.now() - 30_000, 15 * 60_000);
      log(`İlk maça ${Math.round((nextKickoff - Date.now()) / 60000)} dk var; ${Math.round(wait / 60000)} dk bekleniyor.`);
      await wait_(Math.max(wait, 30_000));
      active = await activeMatches(db);
      continue;
    }

    const ids = active.slice(0, 20).map((m) => m.fixture_id).join('-');
    let fixtures = [];
    try {
      fixtures = await api.get('/fixtures', { ids, timezone: 'Europe/Istanbul' });
    } catch (err) {
      log(`Skor alınamadı: ${err.message}`);
      if (api.remaining !== null && api.remaining <= 1) {
        log('Günlük istek hakkı bitti; izleme durduruldu.');
        return;
      }
    }

    const messages = [];
    const batch = db.batch();
    for (const f of fixtures) {
      const next = fb.matchDocFromFixture(f);
      const prev = active.find((m) => m.fixture_id === next.fixture_id);
      for (const ev of fb.diffMatch(prev, next)) {
        const msg = fb.buildMatchMessage(ev, next);
        if (msg) messages.push(msg);
        log(`Olay: ${ev.type} ${next.home.name} ${next.goals.home}-${next.goals.away} ${next.away.name}`);
      }
      batch.set(db.doc(`matches/${next.fixture_id}`), { ...next, updated_at: Timestamp.now() }, { merge: true });
    }
    if (fixtures.length) await batch.commit();
    if (messages.length) {
      const res = await messaging.sendEach(messages);
      log(`Bildirim: ${res.successCount} gönderildi, ${res.failureCount} hata`);
    }

    active = await activeMatches(db);
    const liveMinutesLeft = active.reduce(
      (max, m) => Math.max(max, (m.kickoff.toMillis() + MATCH_LENGTH_MIN * 60_000 - Date.now()) / 60_000),
      0
    );
    const wait = fb.pollIntervalMs({ remaining: api.remaining, liveMinutesLeft });
    log(`Kalan istek: ${api.remaining ?? '?'} | sonraki kontrol ${Math.round(wait / 1000)} sn sonra`);
    if (active.length) await wait_(wait);
  }
  log(`İzleme bitti. Bu çalışmada ${api.used} istek kullanıldı.`);
}

async function main() {
  const mode = process.argv[2] || 'watch';
  const api = new fb.ApiFootball({ key: process.env.FOOTBALL_API_KEY });
  if (mode === 'check') return check(api);
  initAdmin();
  const db = getFirestore();
  if (mode === 'sync-teams') return syncTeams(db, api, {});
  return watch(db, api, getMessaging());
}

if (require.main === module) {
  main().catch((err) => {
    console.error(err.message);
    process.exit(1);
  });
}

module.exports = { activeMatches, watch };
