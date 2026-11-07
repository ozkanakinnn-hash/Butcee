'use strict';

/**
 * Futbol işleri (ortak): takım/fikstür senkronu ve canlı skor TURU.
 *
 * Firebase'de `footballTick` dakikada bir çalışır ve tek bir tur yapar:
 *  - maç yoksa: 1 belge + 1 sorgu okur, çıkar (API isteği yok)
 *  - maç canlıysa: `football_meta/state.next_poll_at` zamanı geldiyse TEK istekle skorları günceller,
 *    olayları (gol, başladı, devre, bitti) bildirir ve bir sonraki kontrol zamanını kalan isteğe göre ayarlar.
 * scripts/football-watch.js aynı fonksiyonları komut satırından kullanır.
 */

const { Timestamp } = require('firebase-admin/firestore');
const fb = require('./football');
const { logSentMessages } = require('./notification-log');

const { isPlanSeasonError } = fb;
const START_EARLY_MS = 35 * 60 * 1000;
const MATCH_LENGTH_MIN = 125;
const TEAMS_TTL_MS = 7 * 24 * 60 * 60 * 1000;
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

/**
 * Güncel sezonun takımlarını yazar: listedeki takımlara API kimliği ve logo eklenir, listede olmayan
 * (yeni çıkan) takımlar oluşturulur, bu sezon ligde olmayanlar `active: false` yapılır.
 */
async function syncTeams(db, api, meta) {
  const season = await currentSeason(api);
  const teams = await api.get('/teams', { league: fb.SUPER_LIG_ID, season });
  const resolve = fb.defaultResolver;
  const seen = new Set();
  const batch = db.batch();
  for (const t of teams) {
    const id = resolve(t.team.name);
    seen.add(id);
    const known = resolve.team(id);
    batch.set(db.doc(`football_teams/${id}`), {
      ...(known ? {} : { name: t.team.name, short: t.team.code || '', colors: [] }),
      api_id: t.team.id,
      api_name: t.team.name,
      logo: t.team.logo || '',
      league_id: fb.SUPER_LIG_ID,
      season,
      active: true,
      updated_at: Timestamp.now(),
    }, { merge: true });
  }
  const existing = await db.collection('football_teams').get();
  for (const d of existing.docs) {
    if (!seen.has(d.id)) batch.set(d.ref, { active: false }, { merge: true });
  }
  batch.set(db.doc('football_meta/state'), { season, teams_synced_at: Timestamp.now() }, { merge: true });
  await batch.commit();
  meta.season = season;
  log(`Takımlar güncellendi: ${teams.length} takım (sezon ${season}); ligde olmayan: ${existing.docs.filter((d) => !seen.has(d.id)).map((d) => d.id).join(', ') || '-'}`);
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

/** Bugün henüz başlamamış maçlar (istek bütçesini güne yaymak için). */
async function laterMatchesToday(db, now = Date.now()) {
  const snap = await db.collection('matches')
    .where('kickoff', '>', Timestamp.fromMillis(now))
    .where('kickoff', '<=', Timestamp.fromMillis(now + 14 * 3_600_000)).get();
  return snap.docs.map((d) => d.data()).filter((m) => !m.is_finished);
}

/** Bugünkü maçlardan izlenmesi gerekenler (başlamak üzere veya canlı). */
async function activeMatches(db, now = Date.now()) {
  const from = Timestamp.fromMillis(now - 4 * 3_600_000);
  const to = Timestamp.fromMillis(now + START_EARLY_MS);
  const snap = await db.collection('matches').where('kickoff', '>=', from).where('kickoff', '<=', to).get();
  return snap.docs.map((d) => d.data()).filter((m) => !m.is_finished);
}


/**
 * Günlük hazırlık: takımlar (haftada bir) ve bugünün maçları (günde bir). Plan güncel sezonu kapsamıyorsa
 * 24 saat bekleme işareti koyar. @returns {Promise<boolean>} devam edilebilir mi
 */
async function prepare(db, api, meta) {
  const metaRef = db.doc('football_meta/state');
  if (meta.plan_blocked_until && meta.plan_blocked_until.toMillis() > Date.now()) {
    log(`Plan bu sezonu kapsamıyor (${meta.plan_blocked_reason || ''}); ${meta.plan_blocked_until.toDate().toISOString()} tarihine kadar beklemede.`);
    return false;
  }
  try {
    const teamsAge = meta.teams_synced_at ? Date.now() - meta.teams_synced_at.toMillis() : Infinity;
    if (!meta.season || teamsAge > TEAMS_TTL_MS) await syncTeams(db, api, meta);
    if (meta.fixtures_date !== fb.istanbulDate()) {
      await syncTodayFixtures(db, api, meta);
      await cleanupOldMatches(db);
      meta.fixtures_date = fb.istanbulDate();
    }
  } catch (err) {
    if (isPlanSeasonError(err)) {
      await metaRef.set(
        { plan_blocked_until: Timestamp.fromMillis(Date.now() + 24 * 3_600_000), plan_blocked_reason: err.message.slice(0, 200) },
        { merge: true }
      );
      log(`Plan güncel sezonu kapsamıyor; 24 saat beklenecek. (${err.message})`);
      return false;
    }
    throw err;
  }
  return true;
}

/** Bir tur skor güncellemesi: değişiklikleri yazar, bildirimleri gönderir. @returns {Promise<object[]>} güncel aktif maçlar */
async function pollOnce(db, api, messaging, active) {
  const ids = active.slice(0, 20).map((m) => m.fixture_id).join('-');
  let fixtures = [];
  try {
    fixtures = await api.get('/fixtures', { ids, timezone: 'Europe/Istanbul' });
  } catch (err) {
    log(`Skor alınamadı: ${err.message}`);
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
    await logSentMessages(db, messages, res.responses);
  }
  return activeMatches(db);
}

/**
 * Kalan maç süresine ve günlük kalan isteğe göre bir sonraki kontrole kadar bekleme.
 * `later`: bugün daha sonra başlayacak maçlar — günlük istek sınırı olan sağlayıcıda (Highlightly, 100/gün)
 * istekler günün SON maçının bitişine kadar yayılır; yoksa ilk maçta bütçe biter.
 */
function nextWaitMs(api, active, later = []) {
  const ends = [...active, ...(api.dailyLimit ? later : [])];
  const liveMinutesLeft = ends.reduce(
    (max, m) => Math.max(max, (m.kickoff.toMillis() + MATCH_LENGTH_MIN * 60_000 - Date.now()) / 60_000),
    0
  );
  return fb.pollIntervalMs({ remaining: api.remaining, liveMinutesLeft });
}

/**
 * Dakikalık Firebase görevi için tek tur. API istemcisi yalnızca gerektiğinde oluşturulur (`makeApi`).
 * @returns {Promise<string>} ne yapıldığı (log/test için)
 */
async function tick(db, makeApi, messaging, now = Date.now()) {
  const metaRef = db.doc('football_meta/state');
  const meta = (await metaRef.get()).data() || {};
  const today = fb.istanbulDate(new Date(now));
  const needsPrepare = meta.fixtures_date !== today ||
    !meta.season || !meta.teams_synced_at || now - meta.teams_synced_at.toMillis() > TEAMS_TTL_MS;
  if (meta.plan_blocked_until && meta.plan_blocked_until.toMillis() > now) return 'plan-blocked';

  // Günlük istek sayacı: sağlayıcı kalan isteği bildirmiyorsa (Highlightly) bütçe buradan hesaplanır.
  const usedBefore = meta.req_day === today ? meta.req_used || 0 : 0;
  let api = null;
  const open = () => {
    api = api || makeApi();
    if (api && 'leagueId' in api && !api.leagueId && meta.provider_league_id) api.leagueId = meta.provider_league_id;
    return api;
  };
  const budget = () => {
    if (api && (api.remaining === null || api.remaining === undefined) && api.dailyLimit) {
      api.remaining = Math.max(0, api.dailyLimit - usedBefore - api.used);
    }
  };
  const save = async (extra = {}) => {
    if (!api) return;
    await metaRef.set({
      req_day: today,
      req_used: usedBefore + api.used,
      ...(api.leagueId ? { provider_league_id: api.leagueId } : {}),
      ...extra,
    }, { merge: true });
  };

  if (needsPrepare) {
    if (!open()) return 'no-key';
    const ok = await prepare(db, api, meta);
    await save();
    if (!ok) return 'plan-blocked';
  }

  const active = await activeMatches(db, now);
  if (active.length === 0) return 'idle';
  const anyLive = active.some((m) => m.is_live);
  const nextKickoff = Math.min(...active.map((m) => m.kickoff.toMillis()));
  if (!anyLive && nextKickoff - now > 90_000) return 'waiting-kickoff';
  if (meta.next_poll_at && meta.next_poll_at.toMillis() > now) return 'throttled';

  if (!open()) return 'no-key';
  if (api.dailyLimit && usedBefore + api.used >= api.dailyLimit) return 'out-of-requests';
  const after = await pollOnce(db, api, messaging, active);
  budget();
  const later = api.dailyLimit ? await laterMatchesToday(db, now) : [];
  const wait = after.length ? nextWaitMs(api, after, later) : 0;
  // Görev dakikada bir çalıştığı için bekleme dakikaya yuvarlanır (en az 1 tur atlanmaz).
  await save({ next_poll_at: Timestamp.fromMillis(now + Math.max(0, wait - 30_000)), last_remaining: api.remaining ?? null });
  log(`Skor turu: ${active.length} maç | kalan istek ${api.remaining ?? '?'} | sonraki ~${Math.round(wait / 1000)} sn`);
  return 'polled';
}

module.exports = {
  check, currentSeason, syncTeams, syncTodayFixtures, cleanupOldMatches, activeMatches,
  prepare, pollOnce, nextWaitMs, laterMatchesToday, tick, START_EARLY_MS,
};
