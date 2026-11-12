'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { Highlightly, isTurkey } = require('../src/football-highlightly');
const fb = require('../src/football');

const match = (id, home, away, extra = {}) => ({
  id, date: '2026-11-12T17:00:00Z', league: { id: 77, name: 'UEFA Nations League' },
  homeTeam: { id: home === 'Turkey' ? 9 : 10, name: home }, awayTeam: { id: 11, name: away },
  state: { description: 'Not started', score: null }, ...extra,
});
const fake = (routes, calls = []) => async (url) => {
  calls.push(`${url.pathname}?${url.searchParams}`);
  for (const [re, body] of routes) {
    if (re.test(`${url.pathname}?${url.searchParams}`)) {
      return typeof body === 'number' ? new Response('x', { status: body }) : new Response(JSON.stringify(body), { headers: { 'content-type': 'application/json' } });
    }
  }
  return new Response(JSON.stringify({ data: [] }));
};

test('Türkiye A takımı tanınır; U21/kadın tanınmaz', () => {
  assert.ok(isTurkey('Turkey') && isTurkey('Türkiye') && isTurkey('Turkiye'));
  assert.ok(!isTurkey('Turkey U21') && !isTurkey('Turkey W'));
});

test('milli maç: takım kimliğiyle bulunur, sorgu saklanır, tek maç yeniden sorgulanır', async () => {
  const calls = [];
  const api = new Highlightly({ key: 'k', base: 'https://h/football', fetchImpl: fake([
    [/\/teams\?name=Turkey/, { data: [{ id: 9, name: 'Turkey' }, { id: 99, name: 'Turkey U21' }] }],
    [/homeTeamId=9/, { data: [match(501, 'Turkey', 'Belgium')] }],
    [/awayTeamId=9/, { data: [] }],
  ], calls) });
  api.leagueId = 1;
  const list = await api.nationalFixtures('2026-11-12');
  assert.equal(list.length, 1);
  assert.deepEqual(list[0]._q, { homeTeamId: 9 });
  const doc = fb.nationalizeMatch(fb.matchDocFromFixture(list[0]), list[0]._q);
  assert.equal(doc.home.name, 'Türkiye');
  assert.equal(doc.away.name, 'Belçika');
  assert.equal(doc.national, true);
  const live = fb.buildLiveMessage(doc);
  assert.equal(live.condition, "'national_tr' in topics");
  assert.equal(live.data.home_short, 'TÜR');
  assert.equal(live.data.home_flag, '🇹🇷');
  assert.equal(live.data.away_flag, '🇧🇪');
  assert.equal(fb.flagEmoji('gbeng'), '🏴󠁧󠁢󠁥󠁮󠁧󠁿');
  const polled = await api.nationalPoll(501, { homeTeamId: 9 }, '2026-11-12');
  assert.equal(polled.length, 1);
});

test('milli maç: takım sorgusu desteklenmezse günün maçları taranır', async () => {
  const api = new Highlightly({ key: 'k', base: 'https://h/football', fetchImpl: fake([
    [/\/teams/, 400],
    [/offset=0/, { data: [...Array.from({ length: 100 }, (_, i) => match(i + 1, `A${i}`, `B${i}`))] }],
    [/offset=100/, { data: [match(777, 'France', 'Türkiye')] }],
  ]) });
  api.leagueId = 1;
  const list = await api.nationalFixtures('2026-11-15');
  assert.deepEqual(list.map((f) => f.fixture.id), [777]);
  assert.equal(list[0]._q, null);
  const doc = fb.nationalizeMatch(fb.matchDocFromFixture(list[0]));
  assert.equal(doc.away.id, 'turkiye');
  assert.equal(doc.home.name, 'Fransa');
});
