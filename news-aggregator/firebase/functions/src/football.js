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
  const penalties = (f.events || [])
    .filter((e) => e.type === 'Goal' && /penalty/i.test(e.detail || ''))
    .map((e) => ({
      minute: (e.time && e.time.elapsed) || 0,
      extra: (e.time && e.time.extra) || 0,
      team_id: keyForApiTeam(e.team && e.team.id),
      player: (e.player && e.player.name) || '',
      scored: !/missed/i.test(e.detail || ''),
    }));
  const yellowCards = (f.events || [])
    .filter((e) => e.type === 'Card' && /^yellow card$/i.test(e.detail || ''))
    .map((e) => ({
      minute: (e.time && e.time.elapsed) || 0,
      extra: (e.time && e.time.extra) || 0,
      team_id: keyForApiTeam(e.team && e.team.id),
      player: (e.player && e.player.name) || '',
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
    yellow_cards: yellowCards,
    penalties,
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

  // Sarı kartlar (panelde sessiz)
  if (prev && Array.isArray(prev.yellow_cards)) {
    for (const side of ['home', 'away']) {
      const teamId = next[side].id;
      const before = prev.yellow_cards.filter((c) => c.team_id === teamId).length;
      const cards = (next.yellow_cards || []).filter((c) => c.team_id === teamId);
      for (let i = before; i < cards.length; i++) events.push({ type: 'yellow_card', side, card: cards[i] });
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
  // Olayların takım kimliği de yeni kimliğe taşınır ("turkey" → "turkiye"); yoksa panel Türkiye'nin gol/kartlarını
  // rakibin tarafına yazar ve devre skoru yanlış çıkar.
  const remap = { [doc.home.id]: home.id, [doc.away.id]: away.id };
  const fix = (list) => (list || []).map((e) => ({ ...e, team_id: remap[e.team_id] ?? e.team_id }));
  return {
    ...doc, home, away, team_ids: [home.id, away.id], national: true, national_q: q ? JSON.stringify(q) : '',
    goal_events: fix(doc.goal_events), red_cards: fix(doc.red_cards), yellow_cards: fix(doc.yellow_cards), penalties: fix(doc.penalties),
  };
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
 * Maç saatinin saniye hassasiyetinde çapası (içinde bulunulan devrenin "0. saniyesi", ms).
 * API dakikayı yalnızca tam sayı verir. Her turda gelen dakika, çapa için bir üst sınır verir:
 *   çapa ≤ şimdi − (devredeki dakika × 60 sn)   (gerçek saat API'nin bildirdiğinden geride olamaz)
 * Turlar boyunca bu üst sınırların EN KÜÇÜĞÜ tutulur → birkaç turda gerçek başlama anına birkaç saniye yaklaşır.
 * Planlanan başlama saati KULLANILMAZ (maçlar geç başlayabilir). Panel hiçbir zaman API dakikasının önüne geçmez.
 * @returns {{period: string, anchor_ms: number, v: number, offset_sec?: number} | null}
 */
const PERIOD_BASE_MIN = { '1H': 0, '2H': 45, ET: 90 };
const CLOCK_VERSION = 2;
function clockAnchor(prev, next, now = Date.now(), offsetSec = DEFAULT_CLOCK_OFFSET_SEC) {
  const period = next.status && next.status.short;
  if (!(period in PERIOD_BASE_MIN) || next.status.elapsed === null || next.status.elapsed === undefined) return null;
  const minIn = Math.max(0, next.status.elapsed - PERIOD_BASE_MIN[period]);
  const hi = now - minIn * 60 * 1000;
  const lo = now - (minIn * 60 + 59 + 90) * 1000; // çok eski çapa (ör. veri sıçraması) → sıfırla
  const keep = prev && prev.clock && prev.clock.period === period && prev.clock.v === CLOCK_VERSION && prev.clock.anchor_ms >= lo;
  const anchor = keep ? Math.min(prev.clock.anchor_ms, hi) : hi;
  return { period, anchor_ms: anchor, v: CLOCK_VERSION, offset_sec: offsetSec };
}

/** Panelde skorun altı: o takımın golleri ("⚽ 21' K. Yıldız, 55' Arda") ve kırmızı kartları; yoksa takım adı. */
function scorerLine(match, side) {
  const team = match[side] || {};
  const min = (e) => `${e.minute}${e.extra ? '+' + e.extra : ''}'`;
  const goals = (match.goal_events || []).filter((g) => g.team_id === team.id);
  const reds = (match.red_cards || []).filter((c) => c.team_id === team.id);
  const parts = [];
  if (goals.length) {
    parts.push('⚽ ' + goals.map((g) => `${min(g)} ${g.player || ''}${g.detail === 'Own Goal' ? ' (k.k.)' : g.detail === 'Penalty' ? ' (pen.)' : ''}`.trim()).join(', '));
  }
  if (reds.length) parts.push('🟥 ' + reds.map((c) => `${min(c)} ${c.player || ''}`.trim()).join(', '));
  return parts.length ? parts.join(' · ') : team.name || '';
}

/** Geniş panel: o takımın golleri ve kırmızı kartları, dakika sırasıyla, her biri ayrı satır. */
function eventLines(match, side) {
  const team = match[side] || {};
  const min = (e) => `${e.minute}${e.extra ? '+' + e.extra : ''}'`;
  const rows = [
    ...(match.goal_events || []).filter((g) => g.team_id === team.id).map((g) => ({
      at: g.minute * 100 + (g.extra || 0),
      text: `⚽ ${min(g)} ${g.player || ''}${g.detail === 'Own Goal' ? ' (k.k.)' : g.detail === 'Penalty' ? ' (pen.)' : ''}`.trim(),
    })),
    ...(match.red_cards || []).filter((c) => c.team_id === team.id).map((c) => ({
      at: c.minute * 100 + (c.extra || 0),
      text: `🟥 ${min(c)} ${c.player || ''}${c.second_yellow ? ' (2. sarı)' : ''}`.trim(),
    })),
  ];
  return rows.sort((a, b) => a.at - b.at).map((r) => r.text).join('\n');
}

/**
 * Geniş panelin zaman çizelgesi (telefon satır satır çizer). Her satır "taraf|dakika|metin":
 *   H = ev sahibi (solda), A = deplasman (sağda), S = ayırıcı ("İLK YARI 1 – 0")
 * Goller, sarı ve kırmızı kartlar dakika sırasıyla; ilk yarı bitmişse araya ayırıcı girer. En fazla son 9 satır.
 */
function buildTimeline(match) {
  const min = (e) => `${e.minute}${e.extra ? '+' + e.extra : ''}'`;
  const sideOf = (teamId) => (teamId === (match.home && match.home.id) ? 'H' : 'A');
  const at = (e) => e.minute * 100 + (e.extra || 0);
  const rows = [
    ...(match.goal_events || []).map((g) => ({
      at: at(g), side: sideOf(g.team_id), min: min(g),
      text: `⚽ ${g.player || ''}${g.detail === 'Own Goal' ? ' (k.k.)' : g.detail === 'Penalty' ? ' (pen.)' : ''}`.trim(),
    })),
    ...(match.yellow_cards || []).map((c) => ({ at: at(c), side: sideOf(c.team_id), min: min(c), text: `🟨 ${c.player || ''}`.trim() })),
    ...(match.red_cards || []).map((c) => ({
      at: at(c), side: sideOf(c.team_id), min: min(c), text: `🟥 ${c.player || ''}${c.second_yellow ? ' (2. sarı)' : ''}`.trim(),
    })),
  ].sort((a, b) => a.at - b.at);
  const short = match.status && match.status.short;
  const pastFirstHalf = short && !['NS', '1H'].includes(short);
  if (pastFirstHalf) {
    const ht = match.ht_goals || {
      home: (match.goal_events || []).filter((g) => g.minute <= 45 && sideOf(g.team_id) === 'H').length,
      away: (match.goal_events || []).filter((g) => g.minute <= 45 && sideOf(g.team_id) === 'A').length,
    };
    rows.push({ at: 4599, side: 'S', min: '', text: `İLK YARI ${ht.home} – ${ht.away}` });
    rows.sort((a, b) => a.at - b.at);
  }
  // Ev sahibi olayında metin solda, deplasmanda sağda; emoji deplasmanda sona alınır (taslaktaki gibi)
  const lines = rows.map((r) => {
    if (r.side !== 'A') return `${r.side}|${r.min}|${r.text}`;
    const m = /^(\S+)\s(.*)$/u.exec(r.text);
    return `A|${r.min}|${m ? `${m[2]} ${m[1]}` : r.text}`;
  });
  return lines.slice(-9).join('\n');
}

/** Bir turda birden çok olay olursa panelde tek uyarı: en önemlisi. Sarı kart ve 2. yarı başlangıcı sessiz. */
const ALERT_ORDER = ['goal', 'goal_cancelled', 'red_card', 'fulltime', 'halftime', 'kickoff'];
function pickAlert(events, match, now = new Date()) {
  const ev = ALERT_ORDER.map((t) => events.find((e) => e.type === t)).find(Boolean);
  if (!ev) return null;
  const msg = buildMatchMessage(ev, match, now);
  if (!msg) return null;
  return {
    alert: ev.type,
    alert_title: msg.notification.title,
    alert_body: msg.notification.body || '',
    alert_quiet: msg.android.notification.channelId === 'quiet' ? '1' : '0',
  };
}

/** Son penaltı (atılan/kaçan) olaydan sonraki 3 dakika panelin üst satırında; yoksa lig adı. Ayrı bildirim yok. */
const PENALTY_SHOW_MIN = 3;
function panelHeadline(match) {
  const league = match.league && match.league.name ? match.league.name : 'Süper Lig';
  const now = match.status && match.status.elapsed;
  const p = (match.penalties || []).slice(-1)[0];
  if (!p || !match.is_live || now === null || now === undefined || now - p.minute > PENALTY_SHOW_MIN || now < p.minute) return league;
  const side = p.team_id === (match.home && match.home.id) ? match.home : match.away;
  const who = [p.player, side ? `(${shortOf(side)})` : ''].filter(Boolean).join(' ');
  const min = `${p.minute}${p.extra ? '+' + p.extra : ''}'`;
  return p.scored ? `🎯 Penaltı golü ${min} ${who}` : `❌ Kaçan penaltı ${min} ${who}`;
}

/** Panel gerçek maç saatini gösterir (TV yayını ~20–30 sn geriden gelir). İstenirse geri kaydırma:
 * Firestore football_meta/state.clock_offset_sec (yükleme gerekmez). */
const DEFAULT_CLOCK_OFFSET_SEC = 0; // kullanıcı kararı: gerçek maç saati (TV'ye göre kaydırma yok)

function liveClock(match, now) {
  const c = match.clock;
  if (!c || !c.anchor_ms || !(c.period in PERIOD_BASE_MIN)) return { elapsed: String(match.status.elapsed ?? 0), sent_ms: String(now) };
  const offset = Number.isFinite(c.offset_sec) ? c.offset_sec : DEFAULT_CLOCK_OFFSET_SEC;
  const sec = Math.max(0, Math.floor((now - c.anchor_ms) / 1000) - offset) + PERIOD_BASE_MIN[c.period] * 60;
  return { elapsed: String(Math.floor(sec / 60)), sent_ms: String(now - (sec % 60) * 1000) };
}


/**
 * Canlı skor paneli verisi (yalnızca VERİ mesajı; bildirimi telefondaki LiveScoreNotifier.kt çizer ve yerinde günceller).
 * Her skor turunda canlı maçlar için gönderilir; maç bitince bir kez `finished: '1'`. Bildirim geçmişine yazılmaz.
 */
function buildLiveMessage(match, now = Date.now(), alert = null) {
  const condition = match.national
    ? `'${NATIONAL_TOPIC}' in topics`
    : `'${teamTopic(match.home.id)}' in topics || '${teamTopic(match.away.id)}' in topics`;
  return {
    condition,
    data: {
      type: 'match_live',
      fixture_id: String(match.fixture_id),
      league: panelHeadline(match),
      home_short: shortOf(match.home),
      // Skorun altındaki satır: gol atanlar (yoksa takım adı) — gol panelin içinde görünür
      home_name: scorerLine(match, 'home'), // eski APK'lar: skorun altında tek satır
      home_team: match.home.name || '', // yeni APK: küçük görünümde takım adı
      home_events: eventLines(match, 'home'), // yeni APK: aşağı kaydırınca gol/kart listesi
      home_color: dotColor(match.home),
      home_flag: match.home.flag || '', // milli maçta bayrak emojisi (telefon renk noktası yerine gösterir)
      away_short: shortOf(match.away),
      away_name: scorerLine(match, 'away'),
      away_team: match.away.name || '',
      away_events: eventLines(match, 'away'),
      away_color: dotColor(match.away),
      away_flag: match.away.flag || '',
      gh: String(match.goals.home ?? 0),
      ga: String(match.goals.away ?? 0),
      status: match.status.short,
      // Saniye hassasiyeti: telefon dakikayı elapsed×60 + (şimdi − sent_ms) olarak sayar → çapadan hesaplanan
      // saniyeler sent_ms'e katılır (eski APK'larla da çalışır).
      ...liveClock(match, now),
      finished: match.is_finished ? '1' : '0',
      // Başlamadıysa panelin ortasında başlama saati (ör. "20:00")
      // Yeni APK: aşağı kaydırınca zaman çizelgesi (taraf|dakika|metin satırları)
      timeline: buildTimeline(match),
      // Olay uyarısı: panelin kendisi sesli uyarır (ayrı bildirim kartı yok). Panel kapalıysa telefon bunu
      // normal bildirim olarak gösterir. Türler: goal, goal_cancelled, red_card, halftime, fulltime, kickoff.
      alert: alert ? alert.alert : '',
      alert_title: alert ? alert.alert_title : '',
      alert_body: alert ? alert.alert_body : '',
      alert_quiet: alert ? alert.alert_quiet : '0',
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
  clockAnchor,
  panelHeadline,
  scorerLine,
  eventLines,
  buildTimeline,
  pickAlert,
  DEFAULT_CLOCK_OFFSET_SEC,
  nationalizeMatch,
  flagEmoji,
  NATIONAL_TOPIC,
  pollIntervalMs,
  teamTopic,
};
