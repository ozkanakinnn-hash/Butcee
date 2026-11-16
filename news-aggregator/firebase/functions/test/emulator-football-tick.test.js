'use strict';

/** Firebase dakikalık görevi: footballTick turları (emülatör + sahte API + sahte FCM). */
const test = require('node:test');
const assert = require('node:assert/strict');

/** Maç olayları artık panel mesajının uyarı alanında gider (ayrı bildirim yok): testlerde bildirim gibi oku. */
const asAlert = (x) => ({ condition: x.condition, notification: { title: x.data.alert_title, body: x.data.alert_body }, data: x.data, android: { notification: { channelId: x.data.alert_quiet === '1' ? 'quiet' : x.data.alert === 'goal' || x.data.alert === 'goal_cancelled' ? 'goal_alert' : 'match' } } });
const alertsOf = (m) => m.filter((x) => x.data && x.data.alert).map(asAlert);

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('tick: maç yokken API çağrılmaz; canlı maçta tur atar, aralığı korur, gol bildirir', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');

  initializeApp({ projectId: 'demo-tick' });
  const db = getFirestore();
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today, national_date: today });

  let made = 0;
  const frames = [];
  const api = { remaining: 1000, used: 0, get: async () => { api.used++; return [frames.shift()]; } };
  const makeApi = () => { made++; return api; };
  const sent = [];
  const panels = [];
  const messaging = { sendEach: async (m) => { sent.push(...m.filter((x) => x.notification), ...alertsOf(m)); panels.push(...m.filter((x) => !x.notification)); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  // 1) Maç yok → boşta, API istemcisi bile oluşturulmaz
  assert.equal(await tick(db, makeApi, messaging), 'idle');
  assert.equal(made, 0);

  // 2) Anahtar yoksa (makeApi null) ve hazırlık gerekiyorsa sessizce çıkar
  await db.doc('football_meta/state').update({ fixtures_date: 'eski' });
  assert.equal(await tick(db, () => null, messaging), 'no-key');
  await db.doc('football_meta/state').update({ fixtures_date: today, national_date: today });

  // 3) Canlı maç → tur atılır, gol bildirilir, sonraki kontrol zamanı yazılır
  const kickoff = new Date(Date.now() - 10 * 60_000);
  const mk = (status, home, away, events = []) => ({
    fixture: { id: 88, date: kickoff.toISOString(), status: { short: status, elapsed: 12 } },
    league: { id: 203, name: 'Süper Lig' },
    teams: { home: { id: 645, name: 'Galatasaray' }, away: { id: 611, name: 'Fenerbahçe' } },
    goals: { home, away },
    events,
  });
  await db.doc('matches/88').set(fb.matchDocFromFixture(mk('1H', 0, 0)));
  frames.push(mk('1H', 1, 0, [{ time: { elapsed: 11 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]));
  assert.equal(await tick(db, makeApi, messaging), 'polled');
  assert.equal(api.used, 1);
  assert.deepEqual(sent.map((m) => m.notification.title), ['⚽ GOL! Galatasaray 1-0 Fenerbahçe']);
  assert.ok((await db.doc('football_meta/state').get()).get('next_poll_at'));
  // Canlı skor paneli: aynı turda veri mesajı (bildirim geçmişine yazılmaz)
  assert.equal(panels.length, 1);
  assert.equal(panels[0].data.type, 'match_live');
  assert.equal(panels[0].data.gh, '1');
  assert.equal(panels[0].data.home_short, 'GS');
  assert.equal(panels[0].data.finished, '0');

  // 4) Hemen ardından gelen tur: aralık dolmadı → istek yok
  assert.equal(await tick(db, makeApi, messaging), 'throttled');
  assert.equal(api.used, 1);

  // 5) Aralık dolunca tekrar tur; maç bitti → sonuç bildirimi
  frames.push(mk('FT', 1, 0, [{ time: { elapsed: 11 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]));
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 70_000), 'polled');
  assert.equal(sent[sent.length - 1].notification.title, '🏁 Maç sonucu');
  assert.equal(panels[panels.length - 1].data.finished, '1', 'maç bitince panel son kez "bitti" olarak güncellenir');
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 200_000), 'idle', 'biten maç izlenmez');
});

test('tick + Highlightly: günlük bütçe sayılır, lig kimliği saklanır, gol bildirilir, bütçe bitince durur', { skip }, async () => {
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');
  const { Highlightly } = require('../src/football-highlightly');
  const db = getFirestore();
  for (const d of (await db.collection('matches').get()).docs) await d.ref.delete();
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today, national_date: today });

  const kickoff = new Date(Date.now() - 20 * 60_000).toISOString();
  let score = '0 - 0';
  let events = [];
  const calls = [];
  const fetchImpl = async (url) => {
    calls.push(url.pathname.replace('/football', ''));
    if (url.pathname.endsWith('/leagues')) return Response.json({ data: [{ id: 71, name: 'Super Lig' }] });
    return Response.json({ data: [{
      id: 555, date: kickoff, league: { id: 71, name: 'Super Lig' },
      homeTeam: { id: 1, name: 'Besiktas' }, awayTeam: { id: 2, name: 'Trabzonspor' },
      state: { description: 'First half', clock: 20, score: { current: score } }, events,
    }] });
  };
  const makeApi = () => new Highlightly({ key: 'k', fetchImpl, dailyLimit: 100 });
  makeApi.provider = 'highlightly';
  await db.doc('football_meta/state').set({ teams_source: 'local' }, { merge: true });
  const sent = [];
  const panels = [];
  const messaging = { sendEach: async (m) => { sent.push(...m.filter((x) => x.notification), ...alertsOf(m)); panels.push(...m.filter((x) => !x.notification)); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  // Maç kaydı (günlük fikstür senkronunun yazdığı hali)
  const { toFixture } = require('../src/football-highlightly');
  const first = (await (await fetchImpl(new URL('https://x/matches'))).json()).data[0];
  await db.doc('matches/555').set(fb.matchDocFromFixture(toFixture(first)));
  calls.length = 0;

  score = '1 - 0';
  events = [{ team: { id: 1 }, time: "21'", type: 'Goal', player: 'C. Immobile' }];
  assert.equal(await tick(db, makeApi, messaging), 'polled');
  assert.deepEqual(calls, ['/leagues', '/matches']);
  assert.equal(sent[0].notification.title, '⚽ GOL! Beşiktaş 1-0 Trabzonspor');
  let meta = (await db.doc('football_meta/state').get()).data();
  assert.equal(meta.req_used, 2);
  assert.equal(meta.provider_league_id, 71);
  assert.ok(meta.last_remaining <= 98);
  const firstWait = meta.next_poll_at.toMillis() - Date.now();
  assert.ok(firstWait > 30_000, 'bütçe korunur: dakikada bir değil');

  // Sonraki tur: lig yeniden aranmaz (1 istek)
  calls.length = 0;
  assert.equal(await tick(db, makeApi, messaging, Date.now() + firstWait + 60_000), 'polled');
  assert.deepEqual(calls, ['/matches']);
  meta = (await db.doc('football_meta/state').get()).data();
  assert.equal(meta.req_used, 3);

  // Günlük bütçe biterse istek atılmaz
  await db.doc('football_meta/state').update({ req_used: 100, next_poll_at: null });
  calls.length = 0;
  assert.equal(await tick(db, makeApi, messaging), 'out-of-requests');
  assert.equal(calls.length, 0);
});

test('Highlightly: takım senkronu yerel 18 takımı etkin tutar, API takım listesine dokunmaz', { skip }, async () => {
  const { getFirestore } = require('firebase-admin/firestore');
  const { syncTeams } = require('../src/football-job');
  const { Highlightly } = require('../src/football-highlightly');
  const { SUPER_LIG_TEAMS } = require('../src/teams');
  const db = getFirestore();
  await db.doc('football_teams/galatasaray').set({ active: false }, { merge: true });
  const paths = [];
  const api = new Highlightly({ key: 'k', fetchImpl: async (url) => { paths.push(url.pathname); return Response.json({ data: [{ id: 71, name: 'Super Lig', seasons: [{ season: 2026 }] }] }); } });
  const meta = {};
  await syncTeams(db, api, meta);
  assert.equal(meta.season, 2026);
  assert.ok(!paths.some((p) => p.endsWith('/teams')), 'takım isteği yapılmaz');
  const snap = await db.collection('football_teams').where('active', '==', true).get();
  assert.ok(SUPER_LIG_TEAMS.every((t) => snap.docs.some((d) => d.id === t.id)));
});

test('hızlı mod: istek hakkı bolsa görev içinde 15 sn arayla birden çok kontrol; az ise tek kontrol', { skip }, async () => {
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');
  const db = getFirestore();
  for (const d of (await db.collection('matches').get()).docs) await d.ref.delete();
  const today = fb.istanbulDate();
  const kickoff = new Date(Date.now() - 30 * 60_000);
  const mk = (home) => ({
    fixture: { id: 4242, date: kickoff.toISOString(), status: { short: '1H', elapsed: 30 } },
    league: { id: 203, name: 'Süper Lig' },
    teams: { home: { id: 1, name: 'Galatasaray' }, away: { id: 2, name: 'Fenerbahçe' } },
    goals: { home, away: 0 }, events: [],
  });
  const run = async (remaining) => {
    await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today, teams_source: 'local' });
    await db.doc('matches/4242').set(fb.matchDocFromFixture(mk(0)));
    let calls = 0;
    const api = { remaining, used: 0, get: async () => { calls++; api.used++; api.remaining--; return [mk(calls >= 3 ? 1 : 0)]; } };
    const sleeps = [];
    const sent = [];
    const messaging = { sendEach: async (m) => { sent.push(...m.filter((x) => x.notification), ...alertsOf(m)); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };
    const r = await tick(db, () => api, messaging, Date.now(), { fast: true, sleep: async (ms) => { sleeps.push(ms); } });
    return { r, calls, sleeps, sent };
  };
  const pro = await run(7000);
  assert.equal(pro.r, 'polled');
  assert.equal(pro.calls, 4, '50 sn pencerede 15 sn arayla 4 kontrol');
  assert.deepEqual(pro.sleeps, [15000, 15000, 15000]);
  assert.equal(pro.sent.filter((m) => m.notification.title.startsWith('⚽ GOL')).length, 1, '3. kontrolde gelen gol aynı görevde bildirildi');
  const free = await run(60);
  assert.equal(free.calls, 1, 'ücretsiz bütçede görev içinde tekrar yok');
});

test('yalnızca takip edilen takımlar: takipsiz maç için istek yok; devre arasında istek yok', { skip }, async () => {
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick, halftimeAwareWait } = require('../src/football-job');
  const db = getFirestore();
  for (const d of (await db.collection('matches').get()).docs) await d.ref.delete();
  for (const d of (await db.collection('team_follows').get()).docs) await d.ref.delete();
  const today = fb.istanbulDate();
  const kickoff = new Date(Date.now() - 20 * 60_000);
  const mk = (id, home, away, status = '1H') => ({
    fixture: { id, date: kickoff.toISOString(), status: { short: status, elapsed: 20 } },
    league: { id: 203, name: 'Süper Lig' },
    teams: { home: { id: 1, name: home }, away: { id: 2, name: away } },
    goals: { home: 0, away: 0 }, events: [],
  });
  const reset = async () => db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today, teams_source: 'local' });
  await reset();
  await db.doc('matches/501').set(fb.matchDocFromFixture(mk(501, 'Konyaspor', 'Kayserispor')));
  let calls = 0;
  const api = { remaining: 1000, used: 0, get: async () => { calls++; api.used++; return [mk(501, 'Konyaspor', 'Kayserispor')]; } };
  const messaging = { sendEach: async (m) => ({ successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }) };

  // Biri Beşiktaş'ı takip ediyor, Konyaspor–Kayserispor'u kimse → istek yok
  await db.doc('team_follows/besiktas').set({ count: 1 });
  await db.doc('team_follows/konyaspor').set({ count: 0 });
  assert.equal(await tick(db, () => api, messaging), 'idle-unfollowed');
  assert.equal(calls, 0);
  assert.deepEqual((await db.doc('football_meta/state').get()).get('followed_teams'), ['besiktas']);

  // Konyaspor takip edilmeye başladı (önbellek 15 dk → sıfırla) → istek atılır
  await db.doc('team_follows/konyaspor').set({ count: 2 });
  await reset();
  assert.equal(await tick(db, () => api, messaging), 'polled');
  assert.equal(calls, 1);

  // Devre arası: istek harcamadan ~13 dk bekle
  const now = Date.now();
  const htMatch = { ...fb.matchDocFromFixture(mk(501, 'Konyaspor', 'Kayserispor', 'HT')), ht_since: Timestamp.fromMillis(now - 60_000) };
  const w = halftimeAwareWait([htMatch], 70_000, now);
  assert.ok(w > 11 * 60_000 && w <= 12 * 60_000, `devre arası beklemesi ~12 dk olmalı (${w})`);
  const playing = fb.matchDocFromFixture(mk(502, 'Galatasaray', 'Fenerbahçe', '2H'));
  assert.equal(halftimeAwareWait([htMatch, playing], 70_000, now), 70_000, 'başka maç oynanıyorsa normal aralık');

  // Eski uygulama sürümleri: sayaç hiç yoksa tüm maçlar izlenir
  for (const d of (await db.collection('team_follows').get()).docs) await d.ref.delete();
  await reset();
  calls = 0;
  assert.equal(await tick(db, () => api, messaging, Date.now() + 120_000), 'polled');
  assert.equal(calls, 1);
});

test('test maçı: başlangıçta panel + "başladı", 15. dk gol, 20. dk sonuç ve belge silinir; gece de sesli', { skip }, async () => {
  const { getFirestore } = require('firebase-admin/firestore');
  const { runTestMatch, nextIstanbulTime } = require('../src/test-match');
  const db = getFirestore();
  const start = Date.parse('2026-11-08T21:45:00Z'); // İstanbul 00:45 (sessiz saat)
  await db.doc('football_meta/test').set({ home: 'besiktas', away: 'genclerbirligi', start_ms: start, goal_minute: 15, end_minute: 20 });
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(m); return { successCount: m.length, failureCount: 0 }; } };
  const titles = (batch) => batch.filter((x) => x.notification).map((x) => x.notification.title);

  assert.equal(await runTestMatch(db, messaging, start - 60_000), 'test-waiting');
  assert.equal(sent.length, 0);

  assert.equal(await runTestMatch(db, messaging, start + 5_000), 'test-running');
  assert.deepEqual(titles(sent[0]), ['[TEST] 🟢 Maç başladı']);
  const kick = sent[0].find((x) => x.notification);
  assert.notEqual(kick.android.notification.channelId, 'quiet', 'gece sessiz saatine takılmaz');
  const panel = sent[0].find((x) => !x.notification);
  assert.equal(panel.data.type, 'match_live');
  assert.equal(panel.data.home_short, 'BJK');
  assert.match(panel.condition, /team_besiktas/);

  await runTestMatch(db, messaging, start + 7 * 60_000); // ara dakika: yalnız panel
  assert.deepEqual(titles(sent[1]), []);

  await runTestMatch(db, messaging, start + 15 * 60_000 + 3_000);
  assert.deepEqual(titles(sent[2]), ['[TEST] ⚽ GOL! Beşiktaş 1-0 Gençlerbirliği']);
  assert.equal(sent[2].find((x) => x.notification).android.notification.channelId, 'goal_alert');
  assert.equal(sent[2].find((x) => !x.notification).data.gh, '1');

  await runTestMatch(db, messaging, start + 16 * 60_000); // gol tekrar gitmez
  assert.deepEqual(titles(sent[3]), []);

  assert.equal(await runTestMatch(db, messaging, start + 20 * 60_000), 'test-finished');
  assert.deepEqual(titles(sent[4]), ['[TEST] 🏁 Maç sonucu']);
  assert.equal(sent[4].find((x) => !x.notification).data.finished, '1');
  assert.equal((await db.doc('football_meta/test').get()).exists, false);
  assert.equal(await runTestMatch(db, messaging, start + 21 * 60_000), null);

  // Konsoldan elle: yalnız start_at metni (takımlar varsayılan)
  await db.doc('football_meta/test').set({ start_at: '2026-11-09T00:45' });
  assert.equal(await runTestMatch(db, messaging, start - 1000), 'test-waiting');
  assert.equal(await runTestMatch(db, messaging, start + 1000), 'test-running');
  await db.doc('football_meta/test').delete();

  // 00:45 her zaman sıradaki gece yarısından sonrası
  const t = nextIstanbulTime('00:45', Date.parse('2026-11-08T19:00:00Z')); // İstanbul 22:00
  assert.equal(new Date(t).toISOString(), '2026-11-08T21:45:00.000Z');
});

test('milli maç: lig maçı olmayan gün Türkiye maçı bulunur; panel national_tr; olaylar panelde sesli uyarı, ayrı bildirim yok', { skip }, async () => {
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');
  const { Highlightly } = require('../src/football-highlightly');
  const db = getFirestore();
  for (const d of (await db.collection('matches').get()).docs) await d.ref.delete();
  for (const d of (await db.collection('team_follows').get()).docs) await d.ref.delete();
  await db.doc('team_follows/galatasaray').set({ count: 1 }); // yalnızca GS takip ediliyor
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), teams_source: 'local', fixtures_date: today, league_count_today: 0, provider_league_id: 1, provider_base: 'https://h/football' });

  const kickoff = new Date(Date.now() + 60_000).toISOString();
  let score = '0 - 0';
  let desc = 'Not started';
  const fetchImpl = async (url) => {
    const p = `${url.pathname}?${url.searchParams}`;
    const body = /\/teams\?name=Turkey/.test(p) ? { data: [{ id: 9, name: 'Turkey' }] }
      : /homeTeamId=9/.test(p) ? { data: [{ id: 501, date: kickoff, league: { id: 77, name: 'UEFA Nations League' }, homeTeam: { id: 9, name: 'Turkey' }, awayTeam: { id: 11, name: 'Belgium' }, state: { description: desc, score: { current: score }, clock: 20 } }] }
        : { data: [] };
    return new Response(JSON.stringify(body), { headers: { 'content-type': 'application/json' } });
  };
  const makeApi = () => new Highlightly({ key: 'k', fetchImpl });
  makeApi.provider = 'highlightly';
  const panels = [];
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m.filter((x) => x.notification), ...alertsOf(m)); panels.push(...m.filter((x) => !x.notification)); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  await tick(db, makeApi, messaging);
  const doc = (await db.doc('matches/501').get()).data();
  // Maçtan önce (başlama 1 dk sonra): panel saatle bir kez gösterilir, API isteği yok
  assert.equal(panels.length, 1, 'maç öncesi panel');
  assert.equal(panels[0].data.status, 'NS');
  assert.match(panels[0].data.kickoff_text, /^\d\d:\d\d$/);
  assert.equal(doc.prematch_sent, true);
  await tick(db, makeApi, messaging);
  assert.equal(panels.length, 1, 'ikinci kez gönderilmez');
  assert.equal(doc.national, true);
  assert.equal(doc.home.name, 'Türkiye');
  assert.equal(doc.away.name, 'Belçika');

  desc = 'First half';
  score = '1 - 0';
  await db.doc('football_meta/state').update({ next_poll_at: null });
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 61_000), 'polled');
  assert.equal(panels.length >= 1, true);
  const last = panels[panels.length - 1];
  assert.equal(last.condition, "'national_tr' in topics");
  assert.equal(last.data.gh, '1');
  assert.equal(last.data.home_short, 'TÜR');
  assert.equal(last.data.league, 'UEFA Nations League');
  // Ayrı bildirim yok; olaylar panel uyarısı. Aynı turda başlama + gol → tek uyarı: gol (daha önemli)
  const real = (list) => list.filter((x) => x.data && x.data.type !== 'match_live');
  assert.equal(real(sent).length, 0, 'ayrı bildirim kartı gönderilmez');
  let p = panels[panels.length - 1];
  assert.equal(p.condition, "'national_tr' in topics");
  assert.equal(p.data.alert, 'goal');
  assert.equal(p.data.alert_title, '⚽ GOL! Türkiye 1-0 Belçika');
  assert.equal(p.data.gh, '1');

  // Rakibin golü: panel sesli uyarır
  score = '1 - 1';
  await db.doc('football_meta/state').update({ next_poll_at: null });
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 180_000), 'polled');
  p = panels[panels.length - 1];
  assert.equal(p.data.alert, 'goal');
  assert.equal(p.data.ga, '1');

  // Değişiklik yoksa uyarı yok (panel sessiz güncellenir)
  await db.doc('football_meta/state').update({ next_poll_at: null });
  await tick(db, makeApi, messaging, Date.now() + 240_000);
  assert.equal(panels[panels.length - 1].data.alert, '');

  // Maç sonu: sesli uyarı
  desc = 'Finished';
  await db.doc('football_meta/state').update({ next_poll_at: null });
  await tick(db, makeApi, messaging, Date.now() + 300_000);
  assert.equal(panels[panels.length - 1].data.alert, 'fulltime');
  assert.equal(panels[panels.length - 1].data.alert_title, '🏁 Maç sonucu');
  assert.equal(real(sent).length, 0);
  assert.equal(panels[panels.length - 1].data.finished, '1');
});

test('milli maç araması: bulunamayınca gün kapatılmaz, aralıklarla tekrar denenir; lig maçı olsa da aranır', { skip }, async () => {
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { syncNationalFixtures } = require('../src/football-job');
  const db = getFirestore();
  const today = fb.istanbulDate();
  const metaRef = db.doc('football_meta/state');
  await metaRef.set({ league_count_today: 9 }); // lig maçı olan gün bile aranmalı

  const kickoff = new Date(Date.now() + 3_600_000).toISOString();
  const fixture = {
    fixture: { id: 777, date: kickoff, status: { short: 'NS', elapsed: null } },
    league: { id: 77, name: 'UEFA Uluslar Ligi', round: '' },
    teams: { home: { id: 11, name: 'France' }, away: { id: 9, name: 'Turkey' } },
    goals: { home: null, away: null },
    events: [],
  };

  // 1) Sabah: fikstür henüz yok → gün KAPANMAZ, tekrar denemesi planlanır
  const t0 = Date.parse('2026-11-15T06:00:00Z');
  const empty = { nationalFixtures: async () => [] };
  assert.equal(await syncNationalFixtures(db, empty, {}, today, t0), 0);
  let meta = (await metaRef.get()).data();
  assert.equal(meta.national_date, undefined, 'bulunamayınca gün kapatılmaz');
  assert.equal(meta.national_next_ms, t0 + 4 * 3_600_000);

  // 2) Bekleme dolmadan: istek harcanmaz
  let calls = 0;
  const counting = { nationalFixtures: async () => { calls++; return []; } };
  await syncNationalFixtures(db, counting, { national_next_ms: meta.national_next_ms }, today, t0 + 60_000);
  assert.equal(calls, 0);

  // 3) Hata: kısa sürede (30 dk) yeniden denenir
  const failing = { nationalFixtures: async () => { throw new Error('HTTP 503'); } };
  const t1 = t0 + 4 * 3_600_000;
  await syncNationalFixtures(db, failing, {}, today, t1);
  assert.equal((await metaRef.get()).get('national_next_ms'), t1 + 30 * 60_000);

  // 4) Akşam: maç yayımlanmış → kaydedilir ve gün kapanır
  const t2 = t1 + 60 * 60_000;
  const ok = { nationalFixtures: async () => [{ ...fixture, _q: { awayTeamId: 9 } }] };
  assert.equal(await syncNationalFixtures(db, ok, {}, today, t2), 1);
  meta = (await metaRef.get()).data();
  assert.equal(meta.national_date, today, 'bulununca gün kapanır');
  const doc = (await db.doc('matches/777').get()).data();
  assert.equal(doc.national, true);
  assert.equal(doc.away.name, 'Türkiye');
  assert.equal(doc.home.name, 'Fransa');

  // 5) Aynı gün tekrar aranmaz
  calls = 0;
  await syncNationalFixtures(db, counting, meta, today, t2 + 5 * 3_600_000);
  assert.equal(calls, 0);
});
