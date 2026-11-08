'use strict';

/**
 * Highlightly futbol API'si (ücretsiz Basic plan: günde 100 istek) için uyarlayıcı.
 *
 * football-job.js API-Football'un `get(path, params)` arayüzünü kullanır. Bu sınıf aynı arayüzü sunar ve
 * Highlightly yanıtlarını API-Football biçimine çevirir; böylece skor karşılaştırma, bildirim ve Firestore
 * kodu değişmeden iki sağlayıcıyla da çalışır.
 *
 * Desteklenen çağrılar (football-job'un kullandıkları):
 *   /status, /leagues (Süper Lig), /teams, /fixtures (date | from-to | ids)
 *
 * Highlightly yanıt biçimi dokümandaki örneklere göre esnek okunur (alan adları küçük farklarla değişebilir).
 * Doğrulama: `node scripts/football-watch.js hl-raw` ham yanıt örneğini yazdırır.
 */

// Sport API (ücretsiz BASIC planı tüm sporları kapsar) futbolu /football altında verir; ayrı Football API
// aboneliği olanlar için soccer.highlightly.net. Çalışan ilk adres seçilir ve saklanır.
const BASES = ['https://sports.highlightly.net/football', 'https://soccer.highlightly.net'];
const TIMEZONE = 'Europe/Istanbul';

class HighlightlyError extends Error {}

/** Highlightly durum metni → API-Football kısa durum kodu. */
function statusShort(description, clock) {
  const d = String(description || '').toLowerCase().trim();
  const table = [
    [/^not started|^scheduled|^to be (announced|defined)|^tba|^tbd/, 'NS'],
    [/first half|1st half/, '1H'],
    [/half ?time/, 'HT'],
    [/second half|2nd half/, '2H'],
    [/break/, 'BT'],
    [/extra time|overtime/, 'ET'],
    [/penalt/, d.startsWith('finished') ? 'PEN' : 'P'],
    [/finished after extra|after extra time|aet/, 'AET'],
    [/^finished|^full ?time|^ended|^ft$/, 'FT'],
    [/postponed/, 'PST'],
    [/cancel/, 'CANC'],
    [/abandon/, 'ABD'],
    [/suspend/, 'SUSP'],
    [/interrupt/, 'INT'],
    [/award/, 'AWD'],
    [/live|in progress|playing/, 'LIVE'],
  ];
  // Bitiş türleri, oyun içi durumlardan ("extra time", "penalties") ÖNCE kontrol edilir.
  if (/^finished|^ended|after (extra|penalt)/.test(d)) {
    if (/penalt/.test(d)) return 'PEN';
    if (/extra/.test(d)) return 'AET';
    return 'FT';
  }
  for (const [re, code] of table) if (re.test(d)) return code;
  return Number(clock) > 0 ? 'LIVE' : 'NS';
}

/** "2 - 1", "2-1", {home:2,away:1} → {home, away}; okunamazsa null'lar. */
function parseScore(score) {
  if (score && typeof score === 'object') {
    if ('home' in score || 'away' in score) return { home: toInt(score.home), away: toInt(score.away) };
    if (score.current !== undefined) return parseScore(score.current);
  }
  const m = /(\d+)\s*[-:]\s*(\d+)/.exec(String(score || ''));
  return m ? { home: Number(m[1]), away: Number(m[2]) } : { home: null, away: null };
}

const toInt = (v) => (v === null || v === undefined || v === '' || Number.isNaN(Number(v)) ? null : Number(v));

/** "67'", "45+2'", 67, {elapsed, extra} → {elapsed, extra} */
function parseMinute(t) {
  if (t && typeof t === 'object') return { elapsed: toInt(t.elapsed ?? t.minute) || 0, extra: toInt(t.extra) || 0 };
  const m = /(\d+)(?:\s*\+\s*(\d+))?/.exec(String(t ?? ''));
  return m ? { elapsed: Number(m[1]), extra: m[2] ? Number(m[2]) : 0 } : { elapsed: 0, extra: 0 };
}

const teamOf = (t) => ({ id: t && t.id, name: (t && (t.name || t.displayName)) || '', logo: (t && t.logo) || '' });

/** Highlightly olay listesi → API-Football gol olayları (yalnızca goller). */
function mapEvents(events) {
  if (!Array.isArray(events)) return [];
  const out = [];
  for (const e of events) {
    const type = String(e.type || e.eventType || '').toLowerCase();
    if (!/goal|penalty/.test(type) || /missed|miss|cancel|disallow/.test(type)) continue;
    const detail = /own/.test(type) ? 'Own Goal' : /penalty/.test(type) ? 'Penalty' : 'Normal Goal';
    const player = e.player && typeof e.player === 'object' ? e.player.name : e.player;
    out.push({
      time: parseMinute(e.time ?? e.minute),
      team: { id: e.team && typeof e.team === 'object' ? e.team.id : e.teamId },
      player: { name: player || '' },
      type: 'Goal',
      detail,
    });
  }
  return out;
}

/** Highlightly maç nesnesi → API-Football fikstür nesnesi (matchDocFromFixture'ın beklediği biçim). */
function toFixture(m) {
  const state = m.state || {};
  const clock = toInt(state.clock ?? state.minute);
  const short = statusShort(state.description ?? state.status ?? m.status, clock);
  const score = parseScore(state.score ?? m.score);
  const started = short !== 'NS' && short !== 'PST' && short !== 'CANC';
  const league = m.league || {};
  return {
    fixture: {
      id: m.id,
      date: m.date || m.startTime || m.kickoff,
      status: { short, long: state.description || '', elapsed: clock },
    },
    league: { id: league.id, name: league.name || 'Süper Lig', round: m.round || '' },
    teams: { home: teamOf(m.homeTeam), away: teamOf(m.awayTeam) },
    goals: { home: score.home ?? (started ? 0 : null), away: score.away ?? (started ? 0 : null) },
    events: mapEvents(m.events),
  };
}

/** Yanıttan liste çıkarır ({data:[...]} | [...] | {...}). */
const listOf = (json) => (Array.isArray(json) ? json : Array.isArray(json && json.data) ? json.data : json ? [json] : []);

const isSuperLig = (l) => /s[uü]per\s*lig/i.test(l.name || '') && !/1\.?\s*lig|women|kad[ıi]n|u\d\d/i.test(l.name || '');

/** Futbol sezonu yıl olarak (Temmuz–Haziran): 2026 Kasım → 2026. */
const seasonFor = (date = new Date()) => (date.getUTCMonth() >= 6 ? date.getUTCFullYear() : date.getUTCFullYear() - 1);

class Highlightly {
  constructor({ key, fetchImpl = fetch, base = null, dailyLimit = 100 } = {}) {
    if (!key) throw new HighlightlyError('HIGHLIGHTLY_API_KEY tanımlı değil');
    this.key = key;
    this.fetchImpl = fetchImpl;
    this.base = base; // null → findLeague sırayla dener
    this.used = 0;
    this.remaining = null; // başlıktan gelirse; yoksa football-job günlük sayaçtan hesaplar
    this.dailyLimit = dailyLimit;
    this.provider = 'highlightly';
    this.leagueId = null;
  }

  async raw(path, params = {}) {
    const url = new URL((this.base || BASES[0]) + path);
    for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== null) url.searchParams.set(k, String(v));
    const res = await this.fetchImpl(url, {
      headers: { 'x-rapidapi-key': this.key },
      signal: AbortSignal.timeout(20_000),
    });
    this.used += 1;
    for (const h of ['x-ratelimit-requests-remaining', 'x-ratelimit-remaining']) {
      const v = res.headers.get(h);
      if (v !== null && v !== '' && !Number.isNaN(Number(v))) {
        this.remaining = Number(v);
        this.remainingFromHeader = true; // sağlayıcının bildirdiği gerçek kalan hak
      }
    }
    // Plan yükseltilince (Pro: 7.500/gün) günlük sınır başlıktan öğrenilir; sabit 100'e takılmaz.
    for (const h of ['x-ratelimit-requests-limit', 'x-ratelimit-limit']) {
      const v = res.headers.get(h);
      if (v !== null && v !== '' && Number(v) > 0) this.dailyLimit = Number(v);
    }
    if (!res.ok) {
      const body = await res.text().catch(() => '');
      throw new HighlightlyError(`HTTP ${res.status} (${path}) ${body.slice(0, 200)}`);
    }
    return res.json();
  }

  async findLeague() {
    if (this.leagueId && this.base) return this.leagueId;
    const tries = [{ countryCode: 'TR', limit: 100 }, { leagueName: 'Super Lig', limit: 50 }, { leagueName: 'Süper Lig', limit: 50 }];
    const bases = this.base ? [this.base] : BASES;
    let lastErr = null;
    for (const base of bases) {
      this.base = base;
      try {
        if (this.leagueId) return this.leagueId; // kimlik biliniyor, adres de artık seçildi
        for (const p of tries) {
          const l = listOf(await this.raw('/leagues', p)).find(isSuperLig);
          if (l) {
            this.leagueId = l.id;
            this.league = l;
            return l.id;
          }
        }
      } catch (err) {
        lastErr = err; // ör. bu API'ye abonelik yok (401/403/404) → sıradaki adres
        if (!/HTTP (401|403|404)/.test(err.message) || bases.length === 1) throw err;
      }
    }
    this.base = null;
    throw lastErr || new HighlightlyError('Highlightly\'de Süper Lig bulunamadı');
  }

  async matches(params) {
    const leagueId = await this.findLeague();
    return listOf(await this.raw('/matches', { leagueId, timezone: TIMEZONE, limit: 100, ...params })).map(toFixture);
  }

  /** API-Football uyumlu arayüz. */
  async get(path, params = {}) {
    switch (path) {
      case '/status':
        return {
          subscription: { plan: 'Highlightly', active: true },
          requests: { current: this.used, limit_day: this.dailyLimit },
        };
      case '/leagues': {
        const id = await this.findLeague();
        const seasons = (this.league && Array.isArray(this.league.seasons) ? this.league.seasons : [])
          .map((s) => (typeof s === 'object' ? toInt(s.season ?? s.year) : toInt(s)))
          .filter(Boolean)
          .sort((a, b) => b - a);
        const year = seasons[0] || seasonFor();
        return [{ league: { id, name: this.league.name }, seasons: [{ year, coverage: { fixtures: { events: true } } }] }];
      }
      case '/teams': {
        const leagueId = await this.findLeague();
        const teams = listOf(await this.raw('/teams', { leagueId, limit: 100 }));
        return teams.map((t) => ({ team: { id: t.id, name: t.name, code: t.abbreviation || t.code || '', logo: t.logo || '' } }));
      }
      case '/fixtures': {
        if (params.ids) {
          // Highlightly'de çoklu kimlik sorgusu yok: bugünün lig maçlarını TEK istekle alıp süzeriz.
          const ids = new Set(String(params.ids).split('-').map(String));
          const today = new Intl.DateTimeFormat('en-CA', { timeZone: TIMEZONE }).format(new Date());
          return (await this.matches({ date: today })).filter((f) => ids.has(String(f.fixture.id)));
        }
        if (params.date) return this.matches({ date: params.date });
        if (params.from) return this.matches({ date: params.from });
        return this.matches({ season: params.season });
      }
      default:
        throw new HighlightlyError(`Desteklenmeyen çağrı: ${path}`);
    }
  }
}

module.exports = { BASES, Highlightly, HighlightlyError, toFixture, statusShort, parseScore, parseMinute, mapEvents, isSuperLig, seasonFor };
