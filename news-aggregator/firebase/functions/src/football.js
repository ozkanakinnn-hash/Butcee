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
const { isQuietHour } = require('./quiet-hours');

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
    if (rem !== null && rem !== '') {
      this.remaining = Number(rem);
      this.remainingFromHeader = true;
    }
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
  const redCards = (f.events || [])
    .filter((e) => e.type === 'Card' && /red|second yellow/i.test(e.detail || ''))
    .map((e) => ({
      minute: (e.time && e.time.elapsed) || 0,
      extra: (e.time && e.time.extra) || 0,
      team_id: keyForApiTeam(e.team && e.team.id),
      player: (e.player && e.player.name) || '',
      second_yellow: /second yellow/i.test(e.detail || ''),
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
    red_cards: redCards,
    team_ids: [home.id, away.id],
    is_live: LIVE.has(f.fixture.status.short),
    is_finished: FINISHED.has(f.fixture.status.short),
  };
}

/**
 * Önceki ve yeni maç durumunu karşılaştırıp bildirilecek olayları çıkarır (saf fonksiyon).
 * @returns {Array<{type: 'kickoff'|'goal'|'goal_cancelled'|'red_card'|'halftime'|'second_half'|'fulltime', side?: 'home'|'away', scorer?: object}>}
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

  // Kırmızı kartlar (önceki kayıtta kart listesi yoksa — eski belge — taban sayılır, bildirilmez)
  if (prev && Array.isArray(prev.red_cards)) {
    for (const side of ['home', 'away']) {
      const teamId = next[side].id;
      const before = prev.red_cards.filter((c) => c.team_id === teamId).length;
      const cards = (next.red_cards || []).filter((c) => c.team_id === teamId);
      for (let i = before; i < cards.length; i++) events.push({ type: 'red_card', side, card: cards[i] });
    }
  }

  if (now === 'HT' && was !== 'HT') events.push({ type: 'halftime' });
  if (was === 'HT' && now === '2H') events.push({ type: 'second_half' });
  if (FINISHED.has(now) && !FINISHED.has(was) && was !== 'NS') events.push({ type: 'fulltime' });
  return events;
}

/** Milli maç paneli: canlı skor paneli açık TÜM kullanıcılar bu konuya abonedir (futbol ilgisi şart değil). */
const NATIONAL_TOPIC = 'national_tr';
const fold = (t) => String(t || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/ı/g, 'i').trim();
// Sık rakipler: Türkçe ad, kısaltma, panel rengi
const NATIONS = {
  turkey: ['Türkiye', 'TÜR', ['#E30A17']], turkiye: ['Türkiye', 'TÜR', ['#E30A17']],
  belgium: ['Belçika', 'BEL', ['#E30613']], france: ['Fransa', 'FRA', ['#1F3D8B']], italy: ['İtalya', 'İTA', ['#0066B3']],
  spain: ['İspanya', 'İSP', ['#C60B1E']], germany: ['Almanya', 'ALM', ['#DD0000']], england: ['İngiltere', 'İNG', ['#CF142B']],
  netherlands: ['Hollanda', 'HOL', ['#F36C21']], portugal: ['Portekiz', 'POR', ['#C8102E']], croatia: ['Hırvatistan', 'HIR', ['#E30A17']],
  bulgaria: ['Bulgaristan', 'BUL', ['#00966E']], georgia: ['Gürcistan', 'GÜR', ['#E30A17']], greece: ['Yunanistan', 'YUN', ['#0D5EAF']],
  wales: ['Galler', 'GAL', ['#C8102E']], scotland: ['İskoçya', 'İSK', ['#005EB8']], austria: ['Avusturya', 'AVU', ['#ED2939']],
  switzerland: ['İsviçre', 'İSV', ['#D52B1E']], hungary: ['Macaristan', 'MAC', ['#CD2A3E']], poland: ['Polonya', 'POL', ['#DC143C']],
  serbia: ['Sırbistan', 'SIR', ['#C6363C']], ukraine: ['Ukrayna', 'UKR', ['#FFD500']], 'czech republic': ['Çekya', 'ÇEK', ['#D7141A']],
  czechia: ['Çekya', 'ÇEK', ['#D7141A']], denmark: ['Danimarka', 'DAN', ['#C60C30']], sweden: ['İsveç', 'İSV', ['#FECC02']],
  norway: ['Norveç', 'NOR', ['#BA0C2F']], iceland: ['İzlanda', 'İZL', ['#02529C']], azerbaijan: ['Azerbaycan', 'AZE', ['#00B5E2']],
  'bosnia and herzegovina': ['Bosna Hersek', 'BOS', ['#002395']], albania: ['Arnavutluk', 'ARN', ['#E41E20']],
  romania: ['Romanya', 'ROM', ['#FCD116']], slovakia: ['Slovakya', 'SLK', ['#0B4EA2']], slovenia: ['Slovenya', 'SLO', ['#005DA4']],
  'north macedonia': ['Kuzey Makedonya', 'MKD', ['#D20000']], montenegro: ['Karadağ', 'KAR', ['#C40308']],
  israel: ['İsrail', 'İSR', ['#0038B8']], russia: ['Rusya', 'RUS', ['#0039A6']], 'republic of ireland': ['İrlanda', 'İRL', ['#169B62']],
  ireland: ['İrlanda', 'İRL', ['#169B62']], finland: ['Finlandiya', 'FİN', ['#002F6C']], estonia: ['Estonya', 'EST', ['#0072CE']],
  latvia: ['Letonya', 'LET', ['#9E3039']], lithuania: ['Litvanya', 'LİT', ['#006A44']], kazakhstan: ['Kazakistan', 'KAZ', ['#00AFCA']],
  brazil: ['Brezilya', 'BRE', ['#FFDF00']], argentina: ['Arjantin', 'ARJ', ['#74ACDF']], usa: ['ABD', 'ABD', ['#3C3B6E']],
  'united states': ['ABD', 'ABD', ['#3C3B6E']], mexico: ['Meksika', 'MEK', ['#006847']], japan: ['Japonya', 'JAP', ['#BC002D']],
};

// Bayrak emojisi için ülke kodu (ISO 3166-1); İngiltere/İskoçya/Galler alt bölge bayrağı.
const FLAG_CODES = {
  turkey: 'TR', turkiye: 'TR', belgium: 'BE', france: 'FR', italy: 'IT', spain: 'ES', germany: 'DE', netherlands: 'NL',
  portugal: 'PT', croatia: 'HR', bulgaria: 'BG', georgia: 'GE', greece: 'GR', austria: 'AT', switzerland: 'CH', hungary: 'HU',
  poland: 'PL', serbia: 'RS', ukraine: 'UA', 'czech republic': 'CZ', czechia: 'CZ', denmark: 'DK', sweden: 'SE', norway: 'NO',
  iceland: 'IS', azerbaijan: 'AZ', 'bosnia and herzegovina': 'BA', albania: 'AL', romania: 'RO', slovakia: 'SK', slovenia: 'SI',
  'north macedonia': 'MK', montenegro: 'ME', israel: 'IL', russia: 'RU', 'republic of ireland': 'IE', ireland: 'IE',
  finland: 'FI', estonia: 'EE', latvia: 'LV', lithuania: 'LT', kazakhstan: 'KZ', brazil: 'BR', argentina: 'AR', usa: 'US',
  'united states': 'US', mexico: 'MX', japan: 'JP',
  england: 'gbeng', scotland: 'gbsct', wales: 'gbwls',
};
/** "TR" → 🇹🇷; "gbeng" → 🏴󠁧󠁢󠁥󠁮󠁧󠁿 (etiket dizisi). */
function flagEmoji(code) {
  if (!code) return '';
  if (code.length === 2) return String.fromCodePoint(...[...code.toUpperCase()].map((c) => 0x1f1e6 + c.charCodeAt(0) - 65));
  return String.fromCodePoint(0x1f3f4, ...[...code].map((c) => 0xe0000 + c.charCodeAt(0)), 0xe007f);
}

/** Milli maç belgesi: takım adları Türkçe, Türkiye'nin kimliği "turkiye"; panel herkese (national: true). */
function nationalizeMatch(doc, q = null) {
  const side = (t) => {
    const key = fold(t.name);
    const n = NATIONS[key];
    const flag = flagEmoji(FLAG_CODES[key]);
    if (!n) return { ...t, flag };
    return { ...t, id: n[0] === 'Türkiye' ? 'turkiye' : t.id, name: n[0], short: n[1], colors: n[2], flag };
  };
  const home = side(doc.home);
  const away = side(doc.away);
  return { ...doc, home, away, team_ids: [home.id, away.id], national: true, national_q: q ? JSON.stringify(q) : '' };
}

const scoreLine = (m) => `${m.home.name} ${m.goals.home}-${m.goals.away} ${m.away.name}`;
const teamTopic = (id) => `team_${id}`;

/** Olay için FCM mesajı (iki takımın konusuna tek mesaj; ikisini de takip eden tek bildirim alır). */
function buildMatchMessage(event, match, now = new Date(), { ignoreQuiet = false, titlePrefix = '' } = {}) {
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
      channelId = 'goal_alert';
      break;
    }
    case 'goal_cancelled':
      title = `❌ Gol iptal: ${scoreLine(match)}`;
      body = `${match[event.side].name} golü geçersiz sayıldı (VAR)`;
      channelId = 'goal_alert';
      break;
    case 'red_card': {
      const c = event.card;
      const minute = c ? `${c.minute}${c.extra ? '+' + c.extra : ''}'` : '';
      title = `🟥 Kırmızı kart: ${match[event.side].name}`;
      body = `${minute} ${c && c.player ? c.player : ''}${c && c.second_yellow ? ' (ikinci sarı)' : ''} · ${scoreLine(match)}`.trim();
      break;
    }
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
  // Gece 00:00–08:00 maç bildirimleri sessiz (geç biten maçlar / uzatmalar).
  const quiet = !ignoreQuiet && isQuietHour(now);
  if (quiet) channelId = 'quiet';
  return {
    condition,
    notification: { title: titlePrefix + title, body },
    data: { type: 'match', fixture_id: String(match.fixture_id), event: event.type },
    android: {
      priority: quiet ? 'normal' : 'high',
      notification: {
        channelId,
        tag: `match_${match.fixture_id}_${event.type === 'goal' ? 'goal' : event.type === 'red_card' ? `card_${event.side}_${event.card ? event.card.minute : ''}` : 'status'}`,
        icon: 'ic_stat_news',
        color: '#0B1F3A',
      },
    },
  };
}

/** Takım kısaltması: listede varsa o (GS, BJK), yoksa adın ilk 3 harfi (Türkçe büyük harf). */
const shortOf = (t) => (t.short || String(t.name || '').replace(/[^A-Za-zÇĞİÖŞÜçğıöşü]/g, '').slice(0, 3).toLocaleUpperCase('tr-TR'));
/** Panel noktası rengi: takımın ilk rengi; neredeyse siyahsa ikincisi (koyu temada görünsün). */
function dotColor(t) {
  const [a, b] = t.colors || [];
  const dark = (hex) => /^#?([0-9a-f]{6})$/i.test(hex || '') && parseInt(hex.replace('#', '').slice(0, 2), 16) +
    parseInt(hex.replace('#', '').slice(2, 4), 16) + parseInt(hex.replace('#', '').slice(4, 6), 16) < 90;
  return (a && !dark(a) ? a : b || a) || '#8E8E93';
}

/**
 * Canlı skor paneli verisi (yalnızca VERİ mesajı; bildirimi telefondaki LiveScoreNotifier.kt çizer ve yerinde günceller).
 * Her skor turunda canlı maçlar için gönderilir; maç bitince bir kez `finished: '1'`. Bildirim geçmişine yazılmaz.
 */
function buildLiveMessage(match, now = Date.now()) {
  const condition = match.national
    ? `'${NATIONAL_TOPIC}' in topics`
    : `'${teamTopic(match.home.id)}' in topics || '${teamTopic(match.away.id)}' in topics`;
  return {
    condition,
    data: {
      type: 'match_live',
      fixture_id: String(match.fixture_id),
      league: match.league && match.league.name ? match.league.name : 'Süper Lig',
      home_short: shortOf(match.home),
      home_name: match.home.name || '',
      home_color: dotColor(match.home),
      home_flag: match.home.flag || '', // milli maçta bayrak emojisi (telefon renk noktası yerine gösterir)
      away_short: shortOf(match.away),
      away_name: match.away.name || '',
      away_color: dotColor(match.away),
      away_flag: match.away.flag || '',
      gh: String(match.goals.home ?? 0),
      ga: String(match.goals.away ?? 0),
      status: match.status.short,
      elapsed: String(match.status.elapsed ?? 0),
      sent_ms: String(now),
      finished: match.is_finished ? '1' : '0',
      // Başlamadıysa panelin ortasında başlama saati (ör. "20:00")
      kickoff_text: match.kickoff && match.kickoff.toDate
        ? new Intl.DateTimeFormat('tr-TR', { timeZone: TIMEZONE, hour: '2-digit', minute: '2-digit', hourCycle: 'h23' }).format(match.kickoff.toDate())
        : '',
    },
    android: { priority: 'high', ttl: 10 * 60 * 1000, collapseKey: `live_${match.fixture_id}` },
  };
}

/**
 * Bir sonraki tur için bekleme süresi: kalan istekleri, maçların kalan süresine yay.
 * En az 60 sn (ücretsiz planda dakikada 10 istek sınırı), en fazla 10 dk.
 */
function pollIntervalMs({ remaining, liveMinutesLeft, reserve = 6, minMs = 60_000 }) {
  if (remaining === null || remaining === undefined) return 90_000;
  const usable = Math.max(1, remaining - reserve);
  const ms = (Math.max(1, liveMinutesLeft) * 60_000) / usable;
  return Math.min(600_000, Math.max(minMs, Math.round(ms)));
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
  buildLiveMessage,
  nationalizeMatch,
  flagEmoji,
  NATIONAL_TOPIC,
  pollIntervalMs,
  teamTopic,
};
