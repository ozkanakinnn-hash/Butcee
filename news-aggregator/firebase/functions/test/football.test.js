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

test('panel dakikası: API dakikasından çapalanır, turlarla daralır, planlanan saati kullanmaz, API önüne geçmez', () => {
  const { Timestamp } = require('firebase-admin/firestore');
  const kickoff = Date.parse('2026-11-12T17:00:00Z'); // planlanan 20:00
  const realStart = kickoff + 150_000; // maç 2,5 dk geç başladı
  const m = (short, elapsed) => ({
    fixture_id: 5, kickoff: Timestamp.fromMillis(kickoff), status: { short, elapsed }, goals: { home: 0, away: 0 },
    home: { id: 'a', name: 'A' }, away: { id: 'b', name: 'B' }, league: { name: 'L' }, is_live: true, is_finished: false,
  });
  const apiMin = (t) => Math.floor((t - realStart) / 60_000); // API'nin göreceği tam dakika (gecikmesiz)
  // Turlar ~83 sn arayla
  let clock = null;
  for (let t = realStart + 70_000; t < realStart + 15 * 60_000; t += 83_000) {
    clock = fb.clockAnchor(clock ? { clock } : null, m('1H', apiMin(t)), t);
    assert.ok(clock.anchor_ms >= realStart, 'panel gerçek saatin önüne geçmez');
  }
  assert.ok(clock.anchor_ms - realStart < 15_000, `birkaç turda 15 sn içine yaklaşır (fark ${clock.anchor_ms - realStart} ms)`);
  const now = realStart + 13 * 60_000 + 29_000; // gerçek 13:29
  const d = fb.buildLiveMessage({ ...m('1H', 13), clock }, now).data;
  const shown = Number(d.elapsed) * 60 + (now - Number(d.sent_ms)) / 1000;
  assert.ok(shown <= 13 * 60 + 29 && shown > 13 * 60 + 10, `gösterilen ${shown} sn`);
  // Eski sürüm çapası (planlanan saat, v yok) yok sayılır
  const old = { clock: { period: '1H', anchor_ms: kickoff } };
  assert.equal(fb.clockAnchor(old, m('1H', 10), realStart + 10 * 60_000 + 5_000).anchor_ms, realStart + 5_000);
  // İstenirse TV gecikmesi kadar geri kaydırma
  assert.equal(fb.DEFAULT_CLOCK_OFFSET_SEC, 0);
  const tv = fb.buildLiveMessage({ ...m('1H', 3), clock: { period: '1H', anchor_ms: now - 200_000, v: 2, offset_sec: 25 } }, now).data;
  assert.equal(tv.elapsed, '2');
  // Devre arası: çapa yok
  assert.equal(fb.clockAnchor(null, m('HT', 45), now), null);
});

test('penaltı yalnızca panelde: olaydan sonra 3 dk üst satırda, sonra lig adı; ayrı bildirim yok', () => {
  const base = {
    fixture_id: 1, is_live: true, league: { name: 'UEFA Nations League' }, goals: { home: 1, away: 0 }, goal_events: [], red_cards: [],
    home: { id: 'turkiye', name: 'Türkiye', short: 'TÜR' }, away: { id: 'belgium', name: 'Belçika', short: 'BEL' },
  };
  const scored = { ...base, status: { short: '2H', elapsed: 68 }, penalties: [{ minute: 67, extra: 0, team_id: 'turkiye', player: 'H. Çalhanoğlu', scored: true }] };
  assert.equal(fb.buildLiveMessage(scored).data.league, "🎯 Penaltı golü 67' H. Çalhanoğlu (TÜR)");
  const missed = { ...scored, penalties: [{ minute: 67, extra: 0, team_id: 'belgium', player: 'R. Lukaku', scored: false }] };
  assert.equal(fb.buildLiveMessage(missed).data.league, "❌ Kaçan penaltı 67' R. Lukaku (BEL)");
  assert.equal(fb.buildLiveMessage({ ...scored, status: { short: '2H', elapsed: 75 } }).data.league, 'UEFA Nations League');
  // Penaltı için ayrı olay/bildirim üretilmez (gol zaten gol olarak bildirilir)
  assert.deepEqual(fb.diffMatch({ ...missed, status: { short: '2H' }, penalties: [] }, missed).map((e) => e.type), []);
});

test('panelde skorun altı: gol atanlar ve kırmızı kartlar, yoksa takım adı', () => {
  const m = {
    home: { id: 'turkiye', name: 'Türkiye' }, away: { id: 'belgium', name: 'Belçika' },
    goal_events: [{ minute: 21, extra: 0, team_id: 'turkiye', player: 'K. Yıldız', detail: 'Normal Goal' }, { minute: 45, extra: 2, team_id: 'turkiye', player: 'H. Çalhanoğlu', detail: 'Penalty' }],
    red_cards: [{ minute: 70, extra: 0, team_id: 'belgium', player: 'X' }],
  };
  assert.equal(fb.scorerLine(m, 'home'), "⚽ 21' K. Yıldız, 45+2' H. Çalhanoğlu (pen.)");
  assert.equal(fb.scorerLine(m, 'away'), "🟥 70' X");
  assert.equal(fb.scorerLine({ ...m, red_cards: [] }, 'away'), 'Belçika');
  // Geniş panel: satır satır, dakika sırasıyla
  assert.equal(fb.eventLines(m, 'home'), "⚽ 21' K. Yıldız\n⚽ 45+2' H. Çalhanoğlu (pen.)");
  assert.equal(fb.eventLines(m, 'away'), "🟥 70' X");
  const live = fb.buildLiveMessage({ ...m, fixture_id: 1, status: { short: '2H', elapsed: 71 }, goals: { home: 2, away: 0 }, league: { name: 'L' } }).data;
  assert.equal(live.home_team, 'Türkiye');
  assert.equal(live.home_events.split('\n').length, 2);
});

test('zaman çizelgesi ve panel uyarısı: sarı kart sessiz, gol/kırmızı/devre/sonuç/başlama sesli; tek uyarı (en önemlisi)', () => {
  const m = {
    fixture_id: 9, status: { short: '2H', elapsed: 72 }, goals: { home: 2, away: 1 }, is_live: true, league: { name: 'L' },
    home: { id: 'turkiye', name: 'Türkiye' }, away: { id: 'belgium', name: 'Belçika' },
    goal_events: [
      { minute: 21, team_id: 'turkiye', player: 'K. Yıldız', detail: 'Normal Goal' },
      { minute: 58, team_id: 'belgium', player: 'R. Lukaku', detail: 'Normal Goal' },
      { minute: 67, team_id: 'turkiye', player: 'H. Çalhanoğlu', detail: 'Penalty' },
    ],
    yellow_cards: [{ minute: 34, team_id: 'belgium', player: 'A. Onana' }, { minute: 40, team_id: 'turkiye', player: 'O. Kökçü' }],
    red_cards: [{ minute: 70, team_id: 'belgium', player: 'A. Theate' }],
    ht_goals: { home: 1, away: 0 },
  };
  assert.deepEqual(fb.buildTimeline(m).split('\n'), [
    "H|21'|⚽ K. Yıldız", "A|34'|A. Onana 🟨", "H|40'|🟨 O. Kökçü", 'S||İLK YARI 1 – 0',
    "A|58'|R. Lukaku ⚽", "H|67'|⚽ H. Çalhanoğlu (pen.)", "A|70'|A. Theate 🟥",
  ]);
  const day = new Date('2026-11-12T18:00:00Z');
  assert.equal(fb.pickAlert([{ type: 'yellow_card', side: 'away' }], m, day), null, 'sarı kart sessiz');
  assert.equal(fb.pickAlert([{ type: 'second_half' }], m, day), null, '2. yarı başlangıcı sessiz');
  for (const t of ['red_card', 'halftime', 'fulltime', 'kickoff']) {
    const ev = t === 'red_card' ? { type: t, side: 'away', card: m.red_cards[0] } : { type: t };
    assert.equal(fb.pickAlert([ev], m, day).alert, t);
  }
  const both = fb.pickAlert([{ type: 'kickoff' }, { type: 'goal', side: 'home', scorer: m.goal_events[0] }], m, day);
  assert.equal(both.alert, 'goal', 'aynı turda birden çok olay → en önemlisi');
  assert.equal(fb.pickAlert([{ type: 'goal', side: 'home' }], m, new Date('2026-11-12T23:30:00Z')).alert_quiet, '1', 'gece sessiz');
  const live = fb.buildLiveMessage(m, Date.now(), both).data;
  assert.equal(live.alert, 'goal');
  assert.ok(live.timeline.includes('İLK YARI'));
  // Yeni sarı kart olayı diffMatch'te üretilir (bildirimsiz)
  const prev = { ...m, yellow_cards: [m.yellow_cards[0]] };
  assert.deepEqual(fb.diffMatch(prev, m).map((e) => e.type), ['yellow_card']);
});

test('iOS: canlı skor tek bildirim olarak yerinde güncellenir; olayda sesli, arada sessiz/pasif', () => {
  const m = {
    fixture_id: 9, league: { name: 'Süper Lig' },
    home: { id: 'galatasaray', name: 'Galatasaray', short: 'GS', colors: ['#A90432'] },
    away: { id: 'samsunspor', name: 'Samsunspor', short: 'SAM', colors: ['#E30613'] },
    goals: { home: 1, away: 0 }, status: { short: '1H', elapsed: 27 }, is_live: true, is_finished: false,
    goal_events: [{ minute: 20, extra: 0, team_id: 'galatasaray', player: 'Oyuncu A', detail: 'Normal Goal' }],
  };
  const quiet = fb.buildLiveMessage(m, 1000).apns;
  assert.equal(quiet.headers['apns-collapse-id'], 'live_9');
  assert.equal(quiet.payload.aps.alert.title, "GS 1-0 SAM · 27'");
  assert.match(quiet.payload.aps.alert.body, /GS: ⚽ 20' Oyuncu A/);
  assert.equal(quiet.payload.aps.sound, undefined);
  assert.equal(quiet.payload.aps['interruption-level'], 'passive');
  const loud = fb.buildLiveMessage(m, 1000, { alert: 'goal', alert_title: 'GOL! Galatasaray', alert_body: 'x', alert_quiet: '0' }).apns;
  assert.equal(loud.headers['apns-collapse-id'], 'live_9');
  assert.equal(loud.payload.aps.sound, 'default');
  assert.match(loud.payload.aps.alert.body, /^GOL! Galatasaray/);
  const night = fb.buildLiveMessage(m, 1000, { alert: 'goal', alert_title: 'GOL', alert_body: 'x', alert_quiet: '1' }).apns;
  assert.equal(night.payload.aps.sound, undefined);
  const ht = fb.buildLiveMessage({ ...m, status: { short: 'HT', elapsed: 45 } }, 1000).apns;
  assert.match(ht.payload.aps.alert.title, /devre arası$/);
  const ft = fb.buildLiveMessage({ ...m, status: { short: 'FT', elapsed: 90 }, is_live: false, is_finished: true }, 1000).apns;
  assert.match(ft.payload.aps.alert.title, /maç sonu$/);
});
