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
  assert.deepEqual(m.team_ids, ['galatasaray', 'fenerbahce']);
  assert.equal(m.home.api_id, 645);
  assert.equal(m.home.short, 'GS');
  assert.equal(m.goal_events[0].team_id, 'galatasaray', 'gol olayı da kendi kimliğimize çevrilir');
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
  const msg = fb.buildMatchMessage({ type: 'goal', side: 'home', scorer: m.goal_events[2] }, m, new Date('2026-11-06T17:00:00Z'));
  assert.equal(msg.condition, "'team_galatasaray' in topics || 'team_fenerbahce' in topics");
  assert.equal(msg.notification.title, '⚽ GOL! Galatasaray 2-1 Fenerbahçe');
  assert.equal(msg.notification.body, "67+2' M. Icardi (pen.) — Galatasaray");
  assert.equal(msg.android.notification.channelId, 'goal_alert');
  assert.equal(msg.android.notification.sound, undefined, 'telefonda seçili ses');
  assert.equal(msg.android.priority, 'high');
  assert.equal(msg.data.fixture_id, '1001');

  const end = fb.buildMatchMessage({ type: 'fulltime' }, m, new Date('2026-11-06T17:00:00Z'));
  const late = fb.buildMatchMessage({ type: 'goal', side: 'home', scorer: m.goal_events[2] }, m, new Date('2026-11-06T21:30:00Z'));
  assert.equal(late.android.notification.channelId, 'quiet', 'gece 00:30 golü sessiz');
  assert.equal(late.android.notification.sound, undefined);
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

test('takım çözücü: API adları kendi kimliğimize eşlenir, bilinmeyen takım kimlik alır', () => {
  const { createTeamResolver, teamKeyText } = require('../src/teams');
  const r = createTeamResolver();
  assert.equal(r('Besiktas'), 'besiktas');
  assert.equal(r('Beşiktaş JK'), 'besiktas');
  assert.equal(r('Istanbul Basaksehir'), 'basaksehir');
  assert.equal(r('Caykur Rizespor'), 'rizespor');
  assert.equal(r('Fatih Karagümrük'), 'karagumruk');
  assert.equal(r('Sivasspor'), 'sivasspor');
  assert.equal(r('Adana Demirspor AS'), 'adana-demirspor');
  assert.equal(teamKeyText('Gaziantep FK'), 'gaziantep');
  assert.equal(r.team('galatasaray').short, 'GS');
});

test('canlı skor paneli verisi: kısaltma, renk, skor, dakika; yalnızca veri (bildirimsiz)', () => {
  const m = {
    fixture_id: 9, league: { name: 'Süper Lig' },
    home: { id: 'besiktas', name: 'Beşiktaş', short: 'BJK', colors: ['#111111', '#FFFFFF'] },
    away: { id: 'yeni-kulup', name: 'Yeni Kulüp', short: '', colors: [] },
    goals: { home: 2, away: 1 }, status: { short: '2H', elapsed: 67 }, is_finished: false,
  };
  const msg = fb.buildLiveMessage(m, 1000);
  assert.equal(msg.notification, undefined);
  assert.match(msg.condition, /'team_besiktas' in topics \|\| 'team_yeni-kulup' in topics/);
  assert.equal(msg.data.home_color, '#FFFFFF', 'siyaha yakın renk yerine ikinci renk');
  assert.equal(msg.data.away_short, 'YEN');
  assert.deepEqual([msg.data.gh, msg.data.ga, msg.data.status, msg.data.elapsed, msg.data.finished], ['2', '1', '2H', '67', '0']);
  assert.ok(Object.values(msg.data).every((v) => typeof v === 'string'), 'FCM veri alanları metin olmalı');
});

test('kırmızı kart: yeni kart bildirilir; eski belgede kart listesi yoksa bildirilmez', () => {
  const base = {
    fixture_id: 1, status: { short: '2H' }, goals: { home: 1, away: 0 }, goal_events: [],
    home: { id: 'galatasaray', name: 'Galatasaray' }, away: { id: 'fenerbahce', name: 'Fenerbahçe' },
  };
  const prev = { ...base, red_cards: [] };
  const next = { ...base, red_cards: [{ minute: 70, extra: 0, team_id: 'fenerbahce', player: 'K. Kart', second_yellow: true }] };
  const ev = fb.diffMatch(prev, next);
  assert.deepEqual(ev.map((e) => [e.type, e.side]), [['red_card', 'away']]);
  const msg = fb.buildMatchMessage(ev[0], next, new Date('2026-11-12T18:00:00Z'));
  assert.equal(msg.notification.title, '🟥 Kırmızı kart: Fenerbahçe');
  assert.equal(msg.notification.body, "70' K. Kart (ikinci sarı) · Galatasaray 1-0 Fenerbahçe");
  assert.equal(msg.android.notification.channelId, 'match');
  assert.deepEqual(fb.diffMatch({ ...base }, next), [], 'eski belge: taban');
  assert.equal(fb.diffMatch(next, next).length, 0, 'aynı kart tekrar bildirilmez');
});

test('panel dakikası saniye hassasiyetinde: 1. yarı başlama saatine, 2. yarı ilk görüldüğü ana göre; API dakikasıyla sınırlı', () => {
  const { Timestamp } = require('firebase-admin/firestore');
  const kickoff = Date.parse('2026-11-12T17:00:00Z');
  const m = (short, elapsed, extra = {}) => ({
    fixture_id: 5, kickoff: Timestamp.fromMillis(kickoff), status: { short, elapsed }, goals: { home: 0, away: 0 },
    home: { id: 'a', name: 'A' }, away: { id: 'b', name: 'B' }, league: { name: 'L' }, is_finished: false, ...extra,
  });
  // 1. yarı, şimdi 3:20 geçti, API "3" diyor → çapa başlama saati
  const now = kickoff + 200_000;
  const c1 = fb.clockAnchor(null, m('1H', 3), now, 0);
  assert.equal(c1.anchor_ms, kickoff);
  const d = fb.buildLiveMessage({ ...m('1H', 3), clock: c1 }, now).data;
  assert.equal(d.elapsed, '3');
  assert.equal(Number(d.sent_ms), now - 20_000, '20 sn telefona aktarılır');
  // TV gecikmesi: varsayılan 25 sn geriden (3:20 → 2:55)
  const tv = fb.buildLiveMessage({ ...m('1H', 3), clock: fb.clockAnchor(null, m('1H', 3), now) }, now).data;
  assert.equal(fb.DEFAULT_CLOCK_OFFSET_SEC, 25);
  assert.equal(tv.elapsed, '2');
  assert.equal(Number(tv.sent_ms), now - 55_000);
  // Maç 2 dk geç başladıysa (API "1" diyor) çapa API dakikasına sıkıştırılır (en fazla 90 sn önde)
  const late = fb.clockAnchor(null, m('1H', 1), now);
  assert.ok(now - late.anchor_ms <= (60 + 59 + 90) * 1000);
  // 2. yarı: ilk görüldüğünde API "46" → 1 dk 30 sn varsayılır; sonraki turda aynı çapa korunur
  const t2 = kickoff + 3_600_000;
  const c2 = fb.clockAnchor(m('HT', 45), m('2H', 46), t2, 0);
  assert.equal(c2.anchor_ms, t2 - 90_000);
  assert.equal(fb.clockAnchor({ clock: c2 }, m('2H', 47), t2 + 60_000).anchor_ms, c2.anchor_ms);
  const d2 = fb.buildLiveMessage({ ...m('2H', 47), clock: c2 }, t2 + 60_000).data;
  assert.equal(d2.elapsed, '47');
  // Devre arası / başlamadı: çapa yok, eski davranış
  assert.equal(fb.clockAnchor(null, m('HT', 45), t2), null);
});
