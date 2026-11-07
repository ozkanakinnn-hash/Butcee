'use strict';

/** Firebase dakikalık görevi: footballTick turları (emülatör + sahte API + sahte FCM). */
const test = require('node:test');
const assert = require('node:assert/strict');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('tick: maç yokken API çağrılmaz; canlı maçta tur atar, aralığı korur, gol bildirir', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');

  initializeApp({ projectId: 'demo-tick' });
  const db = getFirestore();
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today });

  let made = 0;
  const frames = [];
  const api = { remaining: 1000, used: 0, get: async () => { api.used++; return [frames.shift()]; } };
  const makeApi = () => { made++; return api; };
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  // 1) Maç yok → boşta, API istemcisi bile oluşturulmaz
  assert.equal(await tick(db, makeApi, messaging), 'idle');
  assert.equal(made, 0);

  // 2) Anahtar yoksa (makeApi null) ve hazırlık gerekiyorsa sessizce çıkar
  await db.doc('football_meta/state').update({ fixtures_date: 'eski' });
  assert.equal(await tick(db, () => null, messaging), 'no-key');
  await db.doc('football_meta/state').update({ fixtures_date: today });

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

  // 4) Hemen ardından gelen tur: aralık dolmadı → istek yok
  assert.equal(await tick(db, makeApi, messaging), 'throttled');
  assert.equal(api.used, 1);

  // 5) Aralık dolunca tekrar tur; maç bitti → sonuç bildirimi
  frames.push(mk('FT', 1, 0, [{ time: { elapsed: 11 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]));
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 70_000), 'polled');
  assert.equal(sent[sent.length - 1].notification.title, '🏁 Maç sonucu');
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
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today });

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
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

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
