'use strict';

/**
 * Futbol: takım listesi, günün maçları, canlı skor ve gol/maç bildirimleri (API-Football v3).
 *
 * Akış (scripts/football-watch.js, GitHub Actions'ta 15 dakikada bir başlar):
 *  1. Haftada bir Süper Lig takımları → `football_teams/{teamId}` (1 istek)
 *  2. Günde bir bugünün maçları → `matches/{fixtureId}` (1 istek)
 *  3. Takip edilen bir maç başlamak üzereyse/sürüyorsa: maç bitene kadar döngü.
 *     Her turda TEK istekle (`/fixtures?ids=a-b-c`) tüm canlı maçlar güncellenir;
 *     gol/başlama/devre/bitiş değişiklikleri FCM ile `team_<id>` konularına bildirilir.
 *  Uygulama `matches` belgelerini canlı dinler → skor anında ekrana yansır.
 *
 * Ücretsiz planda günde 100 istek vardır; tur aralığı kalan isteğe göre otomatik ayarlanır.
 */

const { Timestamp } = require('firebase-admin/firestore');
const { createTeamResolver } = require('./teams');

const defaultResolver = createTeamResolver();

const API_BASE = 'https://v3.football.api-sports.io';
const SUPER_LIG_ID = 203;
const TIMEZONE = 'Europe/Istanbul';
const FINISHED = new Set(['FT', 'AET', 'PEN', 'PST', 'CANC', 'ABD', 'AWD', 'WO']);
const LIVE = new Set(['1H', 'HT', '2H', 'ET', 'BT', 'P', 'SUSP', 'INT', 'LIVE']);

class FootballApiError extends Error {}

/** Ücretsiz planın güncel sezonu kapsamadığını söyleyen API hatası mı? */
const isPlanSeasonError = (err) => /do not have access to this season|plan/i.test(String(err && err.message));

/** API-Football istemcisi; kalan günlük istek sayısını başlıklardan izler. */
class ApiFootball {
  constructor({ key, fetchImpl = fetch, base = API_BASE } = {}) {
    if (!key) throw new FootballApiError('FOOTBALL_API_KEY tanımlı değil');
    this.key = key;
    this.fetchImpl = fetchImpl;
    this.base = base;
    this.used = 0;
    this.remaining = null; // günlük kalan istek
  }

  async get(path, params = {}) {
    const url = new URL(this.base + path);
    for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== null) url.searchParams.set(k, String(v));
    const res = await this.fetchImpl(url, {
      headers: { 'x-apisports-key': this.key },
      signal: AbortSignal.timeout(20_000),
    });
    this.used += 1;
    const rem = res.headers.get('x-ratelimit-requests-remaining');
    if (rem !== null && rem !== '') this.remaining = Number(rem);
    if (!res.ok) throw new FootballApiError(`HTTP ${res.status} (${path})`);
    const json = await res.json();
    const errors = json.errors && (Array.isArray(json.errors) ? json.errors : Object.values(json.errors));
    if (errors && errors.length) throw new FootballApiError(`${path}: ${errors.join(' | ')}`);
    return json.response;
  }
}

/** İstanbul saatine göre YYYY-MM-DD. */
function istanbulDate(date = new Date()) {
  return new Intl.DateTimeFormat('en-CA', { timeZone: TIMEZONE, year: 'numeric', month: '2-digit', day: '2-digit' }).format(date);
}

/**
 * API fikstürünü Firestore `matches` belgesine çevirir (saf fonksiyon).
 * Takım kimlikleri API sayısı değil, kendi kimliğimizdir (`galatasaray`); `resolve` adı kimliğe çevirir.
 */
function matchDocFromFixture(f, resolve = defaultResolver) {
  const team = (t) => {
    const id = resolve(t.name);
    const known = resolve.team ? resolve.team(id) : null;
    return {
      id,
      api_id: t.id,
      name: known ? known.name : t.name,
      short: known ? known.short : '',
      colors: known ? known.colors : [],
      logo: t.logo || '',
    };
  };
  const home = team(f.teams.home);
  const away = team(f.teams.away);
  const keyForApiTeam = (apiId) => (apiId === home.api_id ? home.id : apiId === away.api_id ? away.id : null);
  const goalsEvents = (f.events || [])
    .filter((e) => e.type === 'Goal' && e.detail !== 'Missed Penalty')
    .map((e) => ({
      minute: (e.time && e.time.elapsed) || 0,
      extra: (e.time && e.time.extra) || 0,
      team_id: keyForApiTeam(e.team && e.team.id),
      player: (e.player && e.player.name) || '',
      detail: e.detail || '',
    }));
  return {
    fixture_id: f.fixture.id,
    kickoff: Timestamp.fromDate(new Date(f.fixture.date)),
    status: {
      short: f.fixture.status.short,
      long: f.fixture.status.long || '',
      elapsed: f.fixture.status.elapsed ?? null,
    },
    league: { id: f.league.id, name: f.league.name, round: f.league.round || '' },
    home,
    away,
    goals: { home: f.goals.home ?? 0, away: f.goals.away ?? 0 },
    goal_events: goalsEvents,
    team_ids: [home.id, away.id],
    is_live: LIVE.has(f.fixture.status.short),
    is_finished: FINISHED.has(f.fixture.status.short),
  };
}

/**
 * Önceki ve yeni maç durumunu karşılaştırıp bildirilecek olayları çıkarır (saf fonksiyon).
 * @returns {Array<{type: 'kickoff'|'goal'|'goal_cancelled'|'halftime'|'second_half'|'fulltime', side?: 'home'|'away', scorer?: object}>}
 */
function diffMatch(prev, next) {
  const events = [];
  const was = prev ? prev.status.short : 'NS';
  const now = next.status.short;
  if (was === 'NS' && LIVE.has(now)) events.push({ type: 'kickoff' });

  for (const side of ['home', 'away']) {
    const before = prev ? prev.goals[side] : 0;
    const after = next.goals[side];
    const teamId = next[side].id;
    if (after > before) {
      const scorers = next.goal_events.filter((g) => g.team_id === teamId);
      for (let i = before; i < after; i++) {
        events.push({ type: 'goal', side, scorer: scorers[i] || null });
      }
    } else if (after < before) {
      events.push({ type: 'goal_cancelled', side });
    }
  }

  if (now === 'HT' && was !== 'HT') events.push({ type: 'halftime' });
  if (was === 'HT' && now === '2H') events.push({ type: 'second_half' });
  if (FINISHED.has(now) && !FINISHED.has(was) && was !== 'NS') events.push({ type: 'fulltime' });
  return events;
}

const scoreLine = (m) => `${m.home.name} ${m.goals.home}-${m.goals.away} ${m.away.name}`;
const teamTopic = (id) => `team_${id}`;

/** Olay için FCM mesajı (iki takımın konusuna tek mesaj; ikisini de takip eden tek bildirim alır). */
function buildMatchMessage(event, match) {
  const condition = `'${teamTopic(match.home.id)}' in topics || '${teamTopic(match.away.id)}' in topics`;
  let title;
  let body;
  let channelId = 'match';
  switch (event.type) {
    case 'goal': {
      const team = match[event.side].name;
      const s = event.scorer;
      const minute = s ? `${s.minute}${s.extra ? '+' + s.extra : ''}'` : '';
      const who = s && s.player ? ` ${s.player}${s.detail === 'Own Goal' ? ' (k.k.)' : s.detail === 'Penalty' ? ' (pen.)' : ''}` : '';
      title = `⚽ GOL! ${scoreLine(match)}`;
      body = `${minute}${who} — ${team}`.trim();
      channelId = 'goals';
      break;
    }
    case 'goal_cancelled':
      title = `❌ Gol iptal: ${scoreLine(match)}`;
      body = `${match[event.side].name} golü geçersiz sayıldı (VAR)`;
      channelId = 'goals';
      break;
    case 'kickoff':
      title = `🟢 Maç başladı`;
      body = `${match.home.name} - ${match.away.name}`;
      break;
    case 'halftime':
      title = `⏸ İlk yarı bitti`;
      body = scoreLine(match);
      break;
    case 'second_half':
      title = `▶️ İkinci yarı başladı`;
      body = scoreLine(match);
      break;
    case 'fulltime':
      title = `🏁 Maç sonucu`;
      body = scoreLine(match);
      break;
    default:
      return null;
  }
  return {
    condition,
    notification: { title, body },
    data: { type: 'match', fixture_id: String(match.fixture_id), event: event.type },
    android: {
      priority: 'high',
      notification: {
        channelId,
        tag: `match_${match.fixture_id}_${event.type === 'goal' ? 'goal' : 'status'}`,
        icon: 'ic_stat_news',
        color: '#0A84FF',
        ...(channelId === 'goals' ? { sound: 'goal_whistle' } : {}),
      },
    },
  };
}

/**
 * Bir sonraki tur için bekleme süresi: kalan istekleri, maçların kalan süresine yay.
 * En az 60 sn (ücretsiz planda dakikada 10 istek sınırı), en fazla 10 dk.
 */
function pollIntervalMs({ remaining, liveMinutesLeft, reserve = 6 }) {
  if (remaining === null || remaining === undefined) return 90_000;
  const usable = Math.max(1, remaining - reserve);
  const ms = (Math.max(1, liveMinutesLeft) * 60_000) / usable;
  return Math.min(600_000, Math.max(60_000, Math.round(ms)));
}

module.exports = {
  ApiFootball,
  FootballApiError,
  isPlanSeasonError,
  SUPER_LIG_ID,
  FINISHED,
  LIVE,
  istanbulDate,
  matchDocFromFixture,
  defaultResolver,
  diffMatch,
  buildMatchMessage,
  pollIntervalMs,
  teamTopic,
};
