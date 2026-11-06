'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fb = require('../src/football');

/** API-Football /fixtures yanıtındaki tek maç biçimi. */
function fixture({ status = 'NS', elapsed = null, home = 0, away = 0, events = [] } = {}) {
  return {
    fixture: { id: 1001, date: '2026-11-08T17:00:00+03:00', status: { short: status, long: status, elapsed } },
    league: { id: 203, name: 'Süper Lig', round: 'Regular Season - 12' },
    teams: { home: { id: 645, name: 'Galatasaray', logo: 'https://l/645.png' }, away: { id: 611, name: 'Fenerbahçe', logo: 'https://l/611.png' } },
    goals: { home: status === 'NS' ? null : home, away: status === 'NS' ? null : away },
    events,
  };
}
const goal = (min, teamId, player, detail = 'Normal Goal', extra = null) => ({
  time: { elapsed: min, extra }, team: { id: teamId }, player: { name: player }, type: 'Goal', detail,
});

test('fikstür → maç belgesi', () => {
  const m = fb.matchDocFromFixture(fixture({ status: '1H', elapsed: 23, home: 1, events: [goal(12, 645, 'M. Icardi'), goal(20, 611, 'X', 'Missed Penalty')] }));
  assert.equal(m.fixture_id, 1001);
  assert.deepEqual(m.goals, { home: 1, away: 0 });
  assert.deepEqual(m.team_ids, [645, 611]);
  assert.equal(m.is_live, true);
  assert.equal(m.goal_events.length, 1, 'kaçan penaltı gol sayılmaz');
  assert.equal(m.kickoff.toDate().toISOString(), '2026-11-08T14:00:00.000Z');
  const ns = fb.matchDocFromFixture(fixture());
  assert.deepEqual(ns.goals, { home: 0, away: 0 });
});

test('olaylar: başlama, gol (atanla), devre, ikinci yarı, iptal, bitiş', () => {
  const ns = fb.matchDocFromFixture(fixture());
  const k = fb.matchDocFromFixture(fixture({ status: '1H', elapsed: 1 }));
  assert.deepEqual(fb.diffMatch(ns, k).map((e) => e.type), ['kickoff']);

  const g = fb.matchDocFromFixture(fixture({ status: '1H', elapsed: 13, home: 1, events: [goal(12, 645, 'M. Icardi')] }));
  const ev = fb.diffMatch(k, g);
  assert.equal(ev.length, 1);
  assert.equal(ev[0].type, 'goal');
  assert.equal(ev[0].side, 'home');
  assert.equal(ev[0].scorer.player, 'M. Icardi');

  // Tek turda iki gol (ör. 90 sn içinde) → iki bildirim
  const g3 = fb.matchDocFromFixture(fixture({ status: '1H', elapsed: 30, home: 1, away: 2, events: [goal(12, 645, 'M. Icardi'), goal(25, 611, 'E. Dzeko'), goal(29, 611, 'E. Dzeko', 'Penalty')] }));
  assert.deepEqual(fb.diffMatch(g, g3).map((e) => `${e.type}:${e.side}:${e.scorer && e.scorer.minute}`), ['goal:away:25', 'goal:away:29']);

  const ht = fb.matchDocFromFixture(fixture({ status: 'HT', elapsed: 45, home: 1, away: 2 }));
  assert.deepEqual(fb.diffMatch(g3, ht).map((e) => e.type), ['halftime']);
  const sh = fb.matchDocFromFixture(fixture({ status: '2H', elapsed: 46, home: 1, away: 2 }));
  assert.deepEqual(fb.diffMatch(ht, sh).map((e) => e.type), ['second_half']);

  const var_ = fb.matchDocFromFixture(fixture({ status: '2H', elapsed: 60, home: 1, away: 1 }));
  assert.deepEqual(fb.diffMatch(sh, var_).map((e) => `${e.type}:${e.side}`), ['goal_cancelled:away']);

  const ft = fb.matchDocFromFixture(fixture({ status: 'FT', elapsed: 90, home: 1, away: 1 }));
  assert.deepEqual(fb.diffMatch(var_, ft).map((e) => e.type), ['fulltime']);
  assert.deepEqual(fb.diffMatch(ft, ft), [], 'değişiklik yoksa olay yok');
});

test('gol bildirimi: iki takımın konusuna tek mesaj, gol kanalı ve sesi', () => {
  const m = fb.matchDocFromFixture(fixture({ status: '2H', elapsed: 67, home: 2, away: 1, events: [goal(12, 645, 'A'), goal(30, 611, 'B'), goal(67, 645, 'M. Icardi', 'Penalty', 2)] }));
  const msg = fb.buildMatchMessage({ type: 'goal', side: 'home', scorer: m.goal_events[2] }, m);
  assert.equal(msg.condition, "'team_645' in topics || 'team_611' in topics");
  assert.equal(msg.notification.title, '⚽ GOL! Galatasaray 2-1 Fenerbahçe');
  assert.equal(msg.notification.body, "67+2' M. Icardi (pen.) — Galatasaray");
  assert.equal(msg.android.notification.channelId, 'goals');
  assert.equal(msg.android.notification.sound, 'goal_whistle');
  assert.equal(msg.android.priority, 'high');
  assert.equal(msg.data.fixture_id, '1001');

  const end = fb.buildMatchMessage({ type: 'fulltime' }, m);
  assert.equal(end.notification.body, 'Galatasaray 2-1 Fenerbahçe');
  assert.equal(end.android.notification.channelId, 'match');
  assert.equal(end.android.notification.sound, undefined);
});

test('tur aralığı kalan isteğe göre ayarlanır (60 sn – 10 dk)', () => {
  assert.equal(fb.pollIntervalMs({ remaining: null, liveMinutesLeft: 100 }), 90_000);
  assert.equal(fb.pollIntervalMs({ remaining: 1000, liveMinutesLeft: 100 }), 60_000);
  // 3 maçlık gün: 390 dk, 90 istek → ~4.6 dk
  const ms = fb.pollIntervalMs({ remaining: 90, liveMinutesLeft: 390 });
  assert.ok(ms > 250_000 && ms < 300_000, String(ms));
  assert.equal(fb.pollIntervalMs({ remaining: 3, liveMinutesLeft: 100 }), 600_000);
});

test('API istemcisi: anahtar, kalan istek ve API hata mesajı', async () => {
  assert.throws(() => new fb.ApiFootball({ key: '' }), /FOOTBALL_API_KEY/);
  const calls = [];
  const ok = async (url, init) => {
    calls.push({ url: String(url), key: init.headers['x-apisports-key'] });
    return new Response(JSON.stringify({ errors: [], response: [{ x: 1 }] }), { headers: { 'x-ratelimit-requests-remaining': '87' } });
  };
  const api = new fb.ApiFootball({ key: 'k', fetchImpl: ok });
  assert.deepEqual(await api.get('/fixtures', { ids: '1-2', season: undefined }), [{ x: 1 }]);
  assert.equal(api.remaining, 87);
  assert.equal(calls[0].url, 'https://v3.football.api-sports.io/fixtures?ids=1-2');
  assert.equal(calls[0].key, 'k');

  const planErr = async () => new Response(JSON.stringify({ errors: { plan: 'Free plans do not have access to this season, try from 2021 to 2023.' }, response: [] }));
  await assert.rejects(new fb.ApiFootball({ key: 'k', fetchImpl: planErr }).get('/fixtures'), /Free plans do not have access/);
});
