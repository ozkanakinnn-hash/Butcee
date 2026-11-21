'use strict';

/**
 * Futbol işleri (ortak): takım/fikstür senkronu ve canlı skor TURU.
 *
 * Firebase'de `footballTick` dakikada bir çalışır ve tek bir tur yapar:
 *  - maç yoksa: 1 belge + 1 sorgu okur, çıkar (API isteği yok)
 *  - maç canlıysa: `football_meta/state.next_poll_at` zamanı geldiyse TEK istekle skorları günceller,
 *    olayları (gol, başladı, devre, bitti) bildirir ve bir sonraki kontrol zamanını kalan isteğe göre ayarlar.
 * scripts/football-watch.js aynı fonksiyonları komut satırından kullanır.
 */

const { Timestamp } = require('firebase-admin/firestore');
const fb = require('./football');
const { logSentMessages } = require('./notification-log');

const { isPlanSeasonError } = fb;
const START_EARLY_MS = 35 * 60 * 1000;
const MATCH_LENGTH_MIN = 125;
// Hızlı mod: istek hakkı yeterliyse (ör. Highlightly Pro) dakikalık görev içinde ~50 sn boyunca 15 sn'de bir
// skor kontrolü → gol ~15–30 sn içinde panelde ve bildirimde. Ücretsiz planda bütçe aralığı zaten büyük tutar.
const FAST_MIN_MS = 15_000;
// Devre arası ~15 dk: bu sürede istek harcanmaz; ikinci yarıya doğru (13. dk) kontrol yeniden başlar.
const HALFTIME_RESUME_MS = 13 * 60_000;
// Takip sayaçları (team_follows) en fazla 15 dk'da bir okunur (≤18 belge).
const FOLLOWS_TTL_MS = 15 * 60_000;
const FAST_WINDOW_MS = 50_000;
/** Canlı skor paneli maçtan bu kadar önce (başlama saatiyle) bir kez gösterilir; API isteği harcamaz. */
const PREMATCH_PANEL_MS = 10 * 60_000;
const TEAMS_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const log = (...a) => console.log(new Date().toISOString().slice(11, 19), ...a);

async function check(api) {
  const status = await api.get('/status');
  const sub = status.subscription || {};
  const req = status.requests || {};
  log(`Plan: ${sub.plan || '?'} | bugün kullanılan: ${req.current}/${req.limit_day} | aktif: ${sub.active}`);

  const leagues = await api.get('/leagues', { id: fb.SUPER_LIG_ID, current: 'true' });
  const season = leagues[0] && leagues[0].seasons && leagues[0].seasons[0];
  if (!season) throw new fb.FootballApiError('Süper Lig güncel sezonu bulunamadı');
  log(`Süper Lig güncel sezon: ${season.year} (${season.start} → ${season.end}) | canlı skor kapsamı: ${season.coverage && season.coverage.fixtures && season.coverage.fixtures.events ? 'var' : 'yok'}`);

  try {
    const from = fb.istanbulDate();
    const to = fb.istanbulDate(new Date(Date.now() + 10 * 86_400_000));
    const fixtures = await api.get('/fixtures', { league: fb.SUPER_LIG_ID, season: season.year, from, to, timezone: 'Europe/Istanbul' });
    log(`✔ Güncel sezona erişim VAR. Önümüzdeki 10 günde ${fixtures.length} maç:`);
    for (const f of fixtures.slice(0, 5)) log(`   ${f.fixture.date.slice(0, 16)}  ${f.teams.home.name} - ${f.teams.away.name}`);
  } catch (err) {
    log(`✘ Güncel sezona erişim YOK: ${err.message}`);
    log('  Ücretsiz plan bu sezonu kapsamıyor olabilir. Canlı skor için ücretli plan gerekir.');
    process.exitCode = 2;
  }
  log(`Kalan günlük istek: ${api.remaining}`);
}

async function currentSeason(api) {
  const leagues = await api.get('/leagues', { id: fb.SUPER_LIG_ID, current: 'true' });
  const s = leagues[0] && leagues[0].seasons && leagues[0].seasons[0];
  if (!s) throw new fb.FootballApiError('Güncel sezon bulunamadı');
  return s.year;
}

/**
 * Güncel sezonun takımlarını yazar: listedeki takımlara API kimliği ve logo eklenir, listede olmayan
 * (yeni çıkan) takımlar oluşturulur, bu sezon ligde olmayanlar `active: false` yapılır.
 */
async function syncTeams(db, api, meta) {
  const season = await currentSeason(api);
  if (api.provider === 'highlightly') {
    // Highlightly'nin takım listesi farklı sezon/kupa takımlarını da içerebilir; uygulamanın takım listesi
    // src/teams.js'teki Süper Lig listesinden gelir (adlar maçlarda bu listeye eşlenir). Yalnızca etkin tutulur.
    const { SUPER_LIG_TEAMS } = require('./teams');
    const batch = db.batch();
    for (const t of SUPER_LIG_TEAMS) {
      batch.set(db.doc(`football_teams/${t.id}`), { name: t.name, short: t.short, colors: t.colors, league_id: fb.SUPER_LIG_ID, active: true }, { merge: true });
    }
    batch.set(db.doc('football_meta/state'), { season, teams_synced_at: Timestamp.now(), teams_source: 'local' }, { merge: true });
    await batch.commit();
    meta.season = season;
    meta.teams_source = 'local';
    log(`Takımlar (yerel liste): ${SUPER_LIG_TEAMS.length} takım etkin (sezon ${season})`);
    return;
  }
  const teams = await api.get('/teams', { league: fb.SUPER_LIG_ID, season });
  if (teams.length < 10) throw new fb.FootballApiError(`Takım listesi eksik (${teams.length}); takımlara dokunulmadı`);
  const resolve = fb.defaultResolver;
  const seen = new Set();
  const batch = db.batch();
  for (const t of teams) {
    const id = resolve(t.team.name);
    seen.add(id);
    const known = resolve.team(id);
    batch.set(db.doc(`football_teams/${id}`), {
      ...(known ? {} : { name: t.team.name, short: t.team.code || '', colors: [] }),
      api_id: t.team.id,
      api_name: t.team.name,
      logo: t.team.logo || '',
      league_id: fb.SUPER_LIG_ID,
      season,
      active: true,
      updated_at: Timestamp.now(),
    }, { merge: true });
  }
  const existing = await db.collection('football_teams').get();
  for (const d of existing.docs) {
    if (!seen.has(d.id)) batch.set(d.ref, { active: false }, { merge: true });
  }
  batch.set(db.doc('football_meta/state'), { season, teams_synced_at: Timestamp.now() }, { merge: true });
  await batch.commit();
  meta.season = season;
  log(`Takımlar güncellendi: ${teams.length} takım (sezon ${season}); ligde olmayan: ${existing.docs.filter((d) => !seen.has(d.id)).map((d) => d.id).join(', ') || '-'}`);
}

async function syncTodayFixtures(db, api, meta) {
  const today = fb.istanbulDate();
  const fixtures = await api.get('/fixtures', {
    league: fb.SUPER_LIG_ID, season: meta.season, date: today, timezone: 'Europe/Istanbul',
  });
  const batch = db.batch();
  for (const f of fixtures) {
    const doc = fb.matchDocFromFixture(f);
    batch.set(db.doc(`matches/${doc.fixture_id}`), { ...doc, updated_at: Timestamp.now() }, { merge: true });
  }
  batch.set(db.doc('football_meta/state'), { fixtures_date: today, league_count_today: fixtures.length }, { merge: true });
  await batch.commit();
  meta.league_count_today = fixtures.length;
  log(`Bugünün maçları (${today}): ${fixtures.length}`);
}

/** Milli maç araması bulamazsa bu kadar sonra, hata alırsa daha erken tekrar denenir. */
const NATIONAL_RETRY_MS = 4 * 3_600_000;
const NATIONAL_ERROR_RETRY_MS = 30 * 60_000;

/**
 * Türkiye A Milli Takımı maçları (yalnızca sağlayıcı destekliyorsa; arama 2 istek).
 *
 * Gün "tamamlandı" sayılmaz: maç BULUNANA kadar gün içinde tekrar aranır (4 saatte bir, hatadan sonra 30 dk).
 * Eskiden günde bir kez aranıyor ve sonuç ne olursa olsun gün kapatılıyordu; sabah fikstür yayımlanmamışsa
 * ya da o an bir hata olduysa o günün maçı hiç bulunamıyordu (15 Kasım Fransa–Türkiye böyle kaçtı).
 * Ayrıca "bugün lig maçı varsa arama" koşulu kaldırıldı: lig sorgusu beklenenden fazla maç döndürdüğünde
 * milli maç sessizce atlanıyordu.
 */
async function syncNationalFixtures(db, api, meta, today = fb.istanbulDate(), now = Date.now()) {
  const metaRef = db.doc('football_meta/state');
  if (typeof api.nationalFixtures !== 'function') {
    await metaRef.set({ national_date: today }, { merge: true });
    meta.national_date = today;
    return 0;
  }
  // Bugün zaten bulunduysa ya da bir sonraki deneme zamanı gelmediyse istek harcanmaz.
  if (meta.national_date === today) return 0;
  if (meta.national_next_ms && meta.national_next_ms > now) return 0;

  let fixtures = [];
  let failed = false;
  try {
    fixtures = await api.nationalFixtures(today);
  } catch (err) {
    failed = true;
    log(`Milli maç aranamadı: ${err.message}`);
  }
  const batch = db.batch();
  for (const f of fixtures) {
    const doc = fb.nationalizeMatch(fb.matchDocFromFixture(f), f._q || null);
    batch.set(db.doc(`matches/${doc.fixture_id}`), { ...doc, updated_at: Timestamp.now() }, { merge: true });
  }
  const found = fixtures.length > 0;
  const nextMs = now + (failed ? NATIONAL_ERROR_RETRY_MS : NATIONAL_RETRY_MS);
  batch.set(metaRef, { national_next_ms: nextMs, ...(found ? { national_date: today } : {}) }, { merge: true });
  await batch.commit();
  meta.national_next_ms = nextMs;
  if (found) meta.national_date = today;
  log(
    `Milli maç (${today}): ${fixtures.map((f) => `${f.teams.home.name}-${f.teams.away.name}`).join(', ') || 'yok'}` +
      (found ? '' : ` — sonraki deneme ${new Date(nextMs).toISOString()}`),
  );
  return fixtures.length;
}

async function cleanupOldMatches(db) {
  const cutoff = Timestamp.fromMillis(Date.now() - 3 * 86_400_000);
  const old = await db.collection('matches').where('kickoff', '<', cutoff).limit(200).get();
  if (old.empty) return;
  const batch = db.batch();
  old.docs.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  log(`Eski maç kayıtları silindi: ${old.size}`);
}

/**
 * Uygulamada takip edilen takımlar (isimsiz sayaç: team_follows/{takım}.count > 0).
 * Sayaç hiç yoksa (eski uygulama sürümleri) null → tüm maçlar izlenir.
 */
async function followedTeamIds(db, meta, now = Date.now()) {
  if (meta.follows_at && now - meta.follows_at.toMillis() < FOLLOWS_TTL_MS && 'followed_teams' in meta) {
    return meta.followed_teams;
  }
  const snap = await db.collection('team_follows').get();
  const ids = snap.empty ? null : snap.docs.filter((d) => (d.get('count') || 0) > 0).map((d) => d.id);
  await db.doc('football_meta/state').set({ followed_teams: ids, follows_at: Timestamp.fromMillis(now) }, { merge: true });
  meta.followed_teams = ids;
  return ids;
}

// Milli maçlar herkese (takip şartı yok); lig maçları yalnızca takip edilen takımlar için izlenir.
const involves = (ids) => (m) => m.national || !ids || ids.includes(m.home.id) || ids.includes(m.away.id);

/**
 * Devre arasındaki maçlar için bekleme: hepsi devre arasındaysa (ya da henüz başlamamışsa) istek harcamadan
 * ikinci yarıya doğru (devre başlangıcı + 13 dk) ya da sıradaki başlama saatine kadar bekle.
 */
function halftimeAwareWait(after, wait, now = Date.now()) {
  if (!after.length) return wait;
  const playing = after.filter((m) => m.is_live && m.status.short !== 'HT');
  const ht = after.filter((m) => m.status.short === 'HT');
  if (playing.length || !ht.length) return wait;
  const resume = Math.min(...ht.map((m) => (m.ht_since ? m.ht_since.toMillis() : now) + HALFTIME_RESUME_MS));
  const kickoffs = after.filter((m) => !m.is_live && !m.is_finished).map((m) => m.kickoff.toMillis());
  const until = Math.min(resume, ...kickoffs.filter((k) => k > now));
  return Math.max(wait, until - now);
}

/** Bugün henüz başlamamış maçlar (istek bütçesini güne yaymak için). */
async function laterMatchesToday(db, now = Date.now()) {
  const snap = await db.collection('matches')
    .where('kickoff', '>', Timestamp.fromMillis(now))
    .where('kickoff', '<=', Timestamp.fromMillis(now + 14 * 3_600_000)).get();
  return snap.docs.map((d) => d.data()).filter((m) => !m.is_finished);
}

/** Bugünkü maçlardan izlenmesi gerekenler (başlamak üzere veya canlı). */
async function activeMatches(db, now = Date.now()) {
  const from = Timestamp.fromMillis(now - 4 * 3_600_000);
  const to = Timestamp.fromMillis(now + START_EARLY_MS);
  const snap = await db.collection('matches').where('kickoff', '>=', from).where('kickoff', '<=', to).get();
  return snap.docs.map((d) => d.data()).filter((m) => !m.is_finished);
}


/**
 * Günlük hazırlık: takımlar (haftada bir) ve bugünün maçları (günde bir). Plan güncel sezonu kapsamıyorsa
 * 24 saat bekleme işareti koyar. @returns {Promise<boolean>} devam edilebilir mi
 */
async function prepare(db, api, meta) {
  const metaRef = db.doc('football_meta/state');
  if (meta.plan_blocked_until && meta.plan_blocked_until.toMillis() > Date.now()) {
    log(`Plan bu sezonu kapsamıyor (${meta.plan_blocked_reason || ''}); ${meta.plan_blocked_until.toDate().toISOString()} tarihine kadar beklemede.`);
    return false;
  }
  try {
    const teamsAge = meta.teams_synced_at ? Date.now() - meta.teams_synced_at.toMillis() : Infinity;
    if (!meta.season || teamsAge > TEAMS_TTL_MS) await syncTeams(db, api, meta);
    if (meta.fixtures_date !== fb.istanbulDate()) {
      await syncTodayFixtures(db, api, meta);
      await cleanupOldMatches(db);
      meta.fixtures_date = fb.istanbulDate();
    }
    if (meta.national_date !== fb.istanbulDate()) await syncNationalFixtures(db, api, meta, fb.istanbulDate(), Date.now());
  } catch (err) {
    if (isPlanSeasonError(err)) {
      await metaRef.set(
        { plan_blocked_until: Timestamp.fromMillis(Date.now() + 24 * 3_600_000), plan_blocked_reason: err.message.slice(0, 200) },
        { merge: true }
      );
      log(`Plan güncel sezonu kapsamıyor; 24 saat beklenecek. (${err.message})`);
      return false;
    }
    throw err;
  }
  return true;
}

/** Bir tur skor güncellemesi: değişiklikleri yazar, bildirimleri gönderir. @returns {Promise<object[]>} güncel aktif maçlar */
/** Panel dakikasının TV yayınına göre geri kaydırılması (sn); tick her turda meta'dan günceller. */
let clockOffsetSec = fb.DEFAULT_CLOCK_OFFSET_SEC;

async function pollOnce(db, api, messaging, active) {
  const league = active.filter((m) => !m.national);
  const national = active.filter((m) => m.national);
  let fixtures = [];
  if (league.length) {
    try {
      fixtures = await api.get('/fixtures', { ids: league.slice(0, 20).map((m) => m.fixture_id).join('-'), timezone: 'Europe/Istanbul' });
    } catch (err) {
      log(`Skor alınamadı: ${err.message}`);
    }
  }
  // Milli maçlar: maç başına 1 istek (kayıtlı sorguyla)
  const nationalIds = new Map();
  for (const m of national) {
    if (typeof api.nationalPoll !== 'function') break;
    try {
      const q = m.national_q ? JSON.parse(m.national_q) : null;
      const list = await api.nationalPoll(m.fixture_id, q, fb.istanbulDate());
      for (const f of list) nationalIds.set(String(f.fixture.id), m.national_q || '');
      fixtures = fixtures.concat(list);
    } catch (err) {
      log(`Milli maç skoru alınamadı: ${err.message}`);
    }
  }
  const messages = [];
  const panels = []; // canlı skor paneli (veri mesajı; bildirim geçmişine yazılmaz)
  const batch = db.batch();
  for (const f of fixtures) {
    const nq = nationalIds.get(String(f.fixture.id));
    const next = nq !== undefined
      ? fb.nationalizeMatch(fb.matchDocFromFixture(f), nq ? JSON.parse(nq) : null)
      : fb.matchDocFromFixture(f);
    const prev = active.find((m) => m.fixture_id === next.fixture_id);
    next.clock = fb.clockAnchor(prev, next, Date.now(), clockOffsetSec); // paneldeki dakika saniye hassasiyetinde (TV'ye göre)
    // Devre arası başlangıcı (istek harcamadan beklemek için)
    next.ht_since = next.status.short === 'HT'
      ? (prev && prev.status.short === 'HT' && prev.ht_since ? prev.ht_since : Timestamp.now())
      : null;
    // Maç olayları AYRI BİLDİRİM olarak gönderilmez: panelin kendisi sesli uyarır (gol, kırmızı kart, devre, sonuç,
    // başlama düdüğü); sarı kart sessiz. Panel kapalı kullanıcıya telefon aynı olayı normal bildirim olarak gösterir.
    // Olaylar yalnızca "Bildirimler" sekmesi için kaydedilir.
    const events = fb.diffMatch(prev, next);
    if (next.status.short === 'HT' && (!prev || prev.status.short !== 'HT')) next.ht_goals = { ...next.goals };
    else if (prev && prev.ht_goals) next.ht_goals = prev.ht_goals;
    const alert = fb.pickAlert(events, next);
    for (const ev of events.filter((e) => e.type !== 'yellow_card')) {
      const msg = fb.buildMatchMessage(ev, next);
      if (msg && next.national) msg.condition = `'${fb.NATIONAL_TOPIC}' in topics`;
      if (msg) messages.push(msg);
      log(`Olay: ${ev.type} ${next.home.name} ${next.goals.home}-${next.goals.away} ${next.away.name}`);
    }
    const justFinished = next.is_finished && prev && !prev.is_finished;
    if (next.is_live || justFinished || ['HT', 'BT', 'P'].includes(next.status.short)) panels.push(fb.buildLiveMessage(next, Date.now(), alert));
    batch.set(db.doc(`matches/${next.fixture_id}`), { ...next, updated_at: Timestamp.now() }, { merge: true });
  }
  if (fixtures.length) await batch.commit();
  if (messages.length) {
    // Gönderilmez (uyarı panelin içinde); yalnızca Bildirimler sekmesi için kayıt
    await logSentMessages(db, messages, null);
    log(`Maç olayı (panel uyarısı): ${messages.map((m) => m.notification.title).join(' | ')}`);
  }
  if (panels.length) {
    try {
      const res = await messaging.sendEach(panels);
      log(`Canlı skor paneli: ${res.successCount}/${panels.length} gönderildi${failures(res)}`);
    } catch (err) {
      log(`Canlı skor paneli gönderilemedi: ${err.message}`);
    }
  }
  return activeMatches(db);
}

/** FCM sendEach sonucundaki hataların kısa özeti (günlük için). */
function failures(res) {
  if (!res || !res.failureCount) return '';
  const errs = res.responses.filter((r) => !r.success).map((r) => (r.error && (r.error.code || r.error.message)) || '?');
  return ` | REDDEDİLEN ${res.failureCount}: ${[...new Set(errs)].join(', ')}`;
}

/** Başlamak üzere olan maçlar için panel (kayıtlı bilgiyle, istek yok); maç başına bir kez. */
async function sendPrematchPanels(db, messaging, active, now = Date.now()) {
  const pre = active.filter((m) => !m.is_live && !m.is_finished && !m.prematch_sent &&
    (m.status ? m.status.short : 'NS') === 'NS' &&
    m.kickoff.toMillis() - now <= PREMATCH_PANEL_MS && m.kickoff.toMillis() > now - 5 * 60_000);
  if (!pre.length) return 0;
  try {
    const res = await messaging.sendEach(pre.map((m) => fb.buildLiveMessage(m, now)));
    if (res && res.failureCount) log(`Maç öncesi panel reddedildi:${failures(res)}`);
    const batch = db.batch();
    pre.forEach((m) => batch.set(db.doc(`matches/${m.fixture_id}`), { prematch_sent: true }, { merge: true }));
    await batch.commit();
    log(`Maç öncesi panel: ${pre.map((m) => `${m.home.name}-${m.away.name}`).join(', ')}`);
  } catch (err) {
    log(`Maç öncesi panel gönderilemedi: ${err.message}`);
  }
  return pre.length;
}

/**
 * Kalan maç süresine ve günlük kalan isteğe göre bir sonraki kontrole kadar bekleme.
 * `later`: bugün daha sonra başlayacak maçlar — günlük istek sınırı olan sağlayıcıda (Highlightly, 100/gün)
 * istekler günün SON maçının bitişine kadar yayılır; yoksa ilk maçta bütçe biter.
 */
function nextWaitMs(api, active, later = [], minMs = 60_000) {
  const ends = [...active, ...(api.dailyLimit ? later : [])];
  const liveMinutesLeft = ends.reduce(
    (max, m) => Math.max(max, (m.kickoff.toMillis() + MATCH_LENGTH_MIN * 60_000 - Date.now()) / 60_000),
    0
  );
  return fb.pollIntervalMs({ remaining: api.remaining, liveMinutesLeft, minMs });
}

/**
 * Dakikalık Firebase görevi için tek tur. API istemcisi yalnızca gerektiğinde oluşturulur (`makeApi`).
 * @returns {Promise<string>} ne yapıldığı (log/test için)
 */
async function tick(db, makeApi, messaging, now = Date.now(), { fast = false, sleep = (ms) => new Promise((r) => setTimeout(r, ms)) } = {}) {
  const metaRef = db.doc('football_meta/state');
  const meta = (await metaRef.get()).data() || {};
  clockOffsetSec = Number.isFinite(meta.clock_offset_sec) ? meta.clock_offset_sec : fb.DEFAULT_CLOCK_OFFSET_SEC;
  const today = fb.istanbulDate(new Date(now));
  // Milli maç aramasının bekleme süresi dolmadıysa hazırlık turu açılmaz (her dakika boşuna çalışmasın).
  const nationalDue = meta.national_date !== today && !(meta.national_next_ms > now);
  const needsPrepare = meta.fixtures_date !== today || nationalDue ||
    !meta.season || !meta.teams_synced_at || now - meta.teams_synced_at.toMillis() > TEAMS_TTL_MS;
  // Highlightly'ye geçişte takım listesi bir kez yerel listeyle yenilenir (önceki sürüm API listesini yazmış olabilir).
  const forceTeams = makeApi.provider === 'highlightly' && meta.teams_source !== 'local';
  if (meta.plan_blocked_until && meta.plan_blocked_until.toMillis() > now) return 'plan-blocked';

  // Günlük istek sayacı: sağlayıcı kalan isteği bildirmiyorsa (Highlightly) bütçe buradan hesaplanır.
  const usedBefore = meta.req_day === today ? meta.req_used || 0 : 0;
  let api = null;
  const open = () => {
    api = api || makeApi();
    if (api && 'leagueId' in api && !api.leagueId && meta.provider_league_id) {
      api.leagueId = meta.provider_league_id;
      if (meta.provider_base) api.base = meta.provider_base;
    }
    return api;
  };
  const budget = () => {
    if (api && (api.remaining === null || api.remaining === undefined) && api.dailyLimit) {
      api.remaining = Math.max(0, api.dailyLimit - usedBefore - api.used);
    }
  };
  const save = async (extra = {}) => {
    if (!api) return;
    await metaRef.set({
      req_day: today,
      req_used: usedBefore + api.used,
      ...(api.leagueId ? { provider_league_id: api.leagueId } : {}),
      ...(api.base ? { provider_base: api.base } : {}),
      // Sağlayıcının bildirdiği kalan hak (ücretli planda gerçek sınır); yoksa null → kendi sayacımız geçerli
      last_remaining_header: api.remainingFromHeader ? api.remaining : null,
      ...extra,
    }, { merge: true });
  };

  if (needsPrepare || forceTeams) {
    if (!open()) return 'no-key';
    if (forceTeams) meta.teams_synced_at = null;
    const ok = await prepare(db, api, meta);
    await save();
    if (!ok) return 'plan-blocked';
  }

  let active = await activeMatches(db, now);
  if (active.length === 0) return 'idle';
  // Yalnızca uygulamada takip edilen takımların maçları (kimse takip etmiyorsa istek harcanmaz)
  const followed = await followedTeamIds(db, meta, now);
  active = active.filter(involves(followed));
  if (active.length === 0) return 'idle-unfollowed';
  await sendPrematchPanels(db, messaging, active, now);
  const anyLive = active.some((m) => m.is_live);
  const nextKickoff = Math.min(...active.map((m) => m.kickoff.toMillis()));
  if (!anyLive && nextKickoff - now > 90_000) return 'waiting-kickoff';
  if (meta.next_poll_at && meta.next_poll_at.toMillis() > now) return 'throttled';

  if (!open()) return 'no-key';
  // Hak bitti mi? Sağlayıcı kalan isteği bildiriyorsa (son turdaki değer) ona, yoksa günlük sayaca bak.
  const knownLeft = meta.req_day === today && typeof meta.last_remaining_header === 'number' ? meta.last_remaining_header : null;
  if (knownLeft !== null ? knownLeft <= 1 : api.dailyLimit && usedBefore + api.used >= api.dailyLimit) return 'out-of-requests';
  const minMs = fast ? FAST_MIN_MS : 60_000;
  const t0 = Date.now();
  let after = await pollOnce(db, api, messaging, active);
  budget();
  const later = api.dailyLimit ? (await laterMatchesToday(db, now)).filter(involves(followed)) : [];
  const waitFor = (list) => (list.length ? halftimeAwareWait(list, nextWaitMs(api, list, later, minMs), Date.now()) : 0);
  after = after.filter(involves(followed));
  let wait = waitFor(after);
  let polls = 1;
  let planned = 0; // görev içinde planlanan toplam bekleme (pencereyi aşmasın; saatten bağımsız, test edilebilir)
  // Hızlı mod: aynı görev içinde pencere dolana kadar tekrar kontrol (gol anında yakalanır).
  while (fast && after.length && planned + wait <= FAST_WINDOW_MS && (api.remaining ?? 0) > 8) {
    await sleep(wait);
    planned += wait;
    after = (await pollOnce(db, api, messaging, after)).filter(involves(followed));
    budget();
    polls += 1;
    wait = waitFor(after);
  }
  const spent = Date.now() - t0;
  // Görev dakikada bir çalıştığı için bekleme dakikaya yuvarlanır (en az 1 tur atlanmaz).
  await save({ next_poll_at: Timestamp.fromMillis(now + spent + Math.max(0, wait - 30_000)), last_remaining: api.remaining ?? null });
  log(`Skor turu: ${active.length} maç, ${polls} kontrol | kalan istek ${api.remaining ?? '?'} | sonraki ~${Math.round(wait / 1000)} sn`);
  return 'polled';
}

module.exports = {
  check, currentSeason, syncTeams, syncTodayFixtures, syncNationalFixtures, cleanupOldMatches, activeMatches,
  prepare, pollOnce, sendPrematchPanels, nextWaitMs, laterMatchesToday, followedTeamIds, halftimeAwareWait, tick, START_EARLY_MS,
};
