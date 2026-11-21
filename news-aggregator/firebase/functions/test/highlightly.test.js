'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { Highlightly, toFixture, statusShort, parseScore, parseMinute, isSuperLig } = require('../src/football-highlightly');
const fb = require('../src/football');

const sample = (over = {}) => ({
  id: 9001,
  round: 'Regular Season - 12',
  date: '2026-11-08T16:00:00.000Z',
  league: { id: 71, name: 'Super Lig', season: 2026 },
  homeTeam: { id: 1, name: 'Galatasaray', logo: 'https://x/gs.png' },
  awayTeam: { id: 2, name: 'Fenerbahce', logo: 'https://x/fb.png' },
  state: { description: 'Second half', clock: 67, score: { current: '2 - 1' } },
  events: [
    { team: { id: 1 }, time: "12'", type: 'Goal', player: 'M. Icardi' },
    { team: { id: 2 }, time: "40'", type: 'Penalty', player: 'E. Dzeko' },
    { team: { id: 1 }, time: "45+2'", type: 'Own Goal', player: 'X' },
    { team: { id: 2 }, time: "55'", type: 'Missed Penalty', player: 'Y' },
    { team: { id: 1 }, time: "60'", type: 'Yellow Card', player: 'Z' },
    { team: { id: 2 }, time: "70'", type: 'Red Card', player: 'K. Kart' },
  ],
  ...over,
});

test('durum metinleri API-Football kodlarına çevrilir', () => {
  assert.equal(statusShort('Not started'), 'NS');
  assert.equal(statusShort('First half', 12), '1H');
  assert.equal(statusShort('Half time'), 'HT');
  assert.equal(statusShort('Second half'), '2H');
  assert.equal(statusShort('Finished'), 'FT');
  assert.equal(statusShort('Finished after extra time'), 'AET');
  assert.equal(statusShort('Finished after penalties'), 'PEN');
  assert.equal(statusShort('Penalties'), 'P');
  assert.equal(statusShort('Postponed'), 'PST');
  assert.equal(statusShort('Bilinmeyen durum', 30), 'LIVE');
  assert.equal(statusShort('', null), 'NS');
});

test('skor ve dakika biçimleri esnek okunur', () => {
  assert.deepEqual(parseScore('2 - 1'), { home: 2, away: 1 });
  assert.deepEqual(parseScore({ current: '0-0' }), { home: 0, away: 0 });
  assert.deepEqual(parseScore({ home: 3, away: '1' }), { home: 3, away: 1 });
  assert.deepEqual(parseScore(null), { home: null, away: null });
  assert.deepEqual(parseMinute("45+2'"), { elapsed: 45, extra: 2 });
  assert.deepEqual(parseMinute(67), { elapsed: 67, extra: 0 });
});

test('Highlightly maçı → uygulamanın maç belgesi (takımlar bizim kimliklerimizle, goller, olaylar)', () => {
  const f = toFixture(sample());
  assert.equal(f.fixture.status.short, '2H');
  assert.deepEqual(f.goals, { home: 2, away: 1 });
  const goals = f.events.filter((e) => e.type === 'Goal' && e.detail !== 'Missed Penalty');
  assert.equal(goals.length, 3, 'kaçan penaltı gol sayılmaz');
  assert.equal(f.events.filter((e) => e.detail === 'Missed Penalty').length, 1, 'kaçan penaltı panel için alınır');
  assert.deepEqual(goals.map((e) => e.detail), ['Normal Goal', 'Penalty', 'Own Goal']);
  assert.deepEqual(f.events.filter((e) => e.type === 'Card').map((e) => e.detail), ['Yellow Card', 'Red Card'], 'sarı (panelde sessiz) ve kırmızı kart alınır');
  const doc = fb.matchDocFromFixture(f);
  assert.equal(doc.home.id, 'galatasaray');
  assert.equal(doc.away.id, 'fenerbahce');
  assert.equal(doc.is_live, true);
  assert.deepEqual(doc.goal_events.map((g) => g.player), ['M. Icardi', 'E. Dzeko', 'X']);
  assert.deepEqual(doc.penalties.map((p) => [p.player, p.scored]), [['E. Dzeko', true], ['Y', false]]);
  assert.deepEqual(doc.red_cards.map((c) => [c.team_id, c.player, c.minute]), [['fenerbahce', 'K. Kart', 70]]);

  const notStarted = toFixture(sample({ state: { description: 'Not started', score: { current: null } }, events: [] }));
  assert.deepEqual(notStarted.goals, { home: null, away: null });
  assert.equal(fb.matchDocFromFixture(notStarted).is_live, false);
});

test('Süper Lig adı eşleşir; 1. Lig ve kadınlar ligi eşleşmez', () => {
  assert.ok(isSuperLig({ name: 'Super Lig' }));
  assert.ok(isSuperLig({ name: 'Trendyol Süper Lig' }));
  assert.ok(!isSuperLig({ name: '1. Lig' }));
  assert.ok(!isSuperLig({ name: 'Super Lig Women' }));
});

test('istemci: anahtar başlıkta, lig bir kez aranır, ids ile tek istekte bugünkü maçlar süzülür', async () => {
  const calls = [];
  const fetchImpl = async (url, opts) => {
    calls.push({ path: url.pathname.replace('/football', ''), host: url.host, q: Object.fromEntries(url.searchParams), key: opts.headers['x-rapidapi-key'] });
    if (url.pathname.endsWith('/leagues')) return Response.json({ data: [{ id: 99, name: '1. Lig' }, { id: 71, name: 'Super Lig', seasons: [{ season: 2025 }, { season: 2026 }] }] });
    if (url.pathname.endsWith('/matches')) return Response.json({ data: [sample(), sample({ id: 9002 })], pagination: {} });
    return new Response('yok', { status: 404 });
  };
  const api = new Highlightly({ key: 'k', fetchImpl });
  const leagues = await api.get('/leagues', { id: 203, current: 'true' });
  assert.equal(leagues[0].seasons[0].year, 2026);
  const fx = await api.get('/fixtures', { ids: '9001', timezone: 'Europe/Istanbul' });
  assert.equal(fx.length, 1);
  assert.equal(fx[0].fixture.id, 9001);
  assert.equal(calls.filter((c) => c.path === '/leagues').length, 1, 'lig kimliği önbellekte');
  assert.ok(calls.every((c) => c.key === 'k'));
  assert.equal(calls.find((c) => c.path === '/matches').q.leagueId, '71');
  assert.equal(api.used, 2);
  await assert.rejects(api.get('/bilinmeyen'), /Desteklenmeyen/);
});

test('HTTP hatası anlaşılır mesajla fırlatılır', async () => {
  const api = new Highlightly({ key: 'k', fetchImpl: async () => new Response('limit', { status: 429 }) });
  await assert.rejects(api.get('/leagues'), /HTTP 429/);
});

test('Sport API adresi önce denenir; abonelik yoksa (403) Football API adresine geçilir', async () => {
  const hosts = [];
  const fetchImpl = async (url) => {
    hosts.push(url.host);
    if (url.host === 'sports.highlightly.net') return new Response('no subscription', { status: 403 });
    return Response.json({ data: [{ id: 71, name: 'Super Lig' }] });
  };
  const api = new Highlightly({ key: 'k', fetchImpl });
  assert.equal(await api.findLeague(), 71);
  assert.deepEqual(hosts, ['sports.highlightly.net', 'soccer.highlightly.net']);
  assert.equal(api.base, 'https://soccer.highlightly.net');

  const ok = new Highlightly({ key: 'k', fetchImpl: async (url) => { hosts.push(url.href); return Response.json([{ id: 5, name: 'Süper Lig' }]); } });
  assert.equal(await ok.findLeague(), 5);
  assert.match(hosts[hosts.length - 1], /^https:\/\/sports\.highlightly\.net\/football\/leagues/);
});

test('istemci: lig kimliği ve adres önceden biliniyorsa (Firestore önbelleği) /leagues çökmez, istek harcamaz', async () => {
  // Gerçek hata (Kasım 2026): footballTick takım listesi 7 günden eskiyken her dakika
  // "Cannot read properties of undefined (reading 'name')" ile çöküyordu.
  let calls = 0;
  const api = new Highlightly({ key: 'k', fetchImpl: async () => { calls += 1; return Response.json({ data: [] }); } });
  api.leagueId = 71;
  api.base = 'https://sports.highlightly.net/football';
  const leagues = await api.get('/leagues', { id: 203, current: 'true' });
  assert.equal(leagues[0].league.id, 71);
  assert.ok(leagues[0].seasons[0].year >= 2025);
  assert.equal(calls, 0);
});
