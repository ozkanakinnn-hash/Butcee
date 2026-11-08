'use strict';

/**
 * TEST MAÇI — canlı skor paneli ve gol bildirimini gerçek maç beklemeden denemek için.
 *
 * `football_meta/test` belgesi varsa dakikalık `footballTick` görevi bu sahte maçı oynatır:
 *   başlangıç   → "🟢 Maç başladı" + canlı skor paneli
 *   her dakika  → panel güncellenir (dakika telefonda kendi sayar)
 *   goal_minute → "⚽ GOL!" bildirimi + panel 1–0
 *   end_minute  → "🏁 Maç sonucu" + panel "MS"; belge silinir
 * Bildirimler yalnızca seçilen takımların konularına gider; başlıkta [TEST] yazar; gece sessiz saatine
 * takılmaz (deneme sesli duyulsun). Bildirim geçmişine yazılmaz. API isteği harcamaz.
 * Kurulum: node scripts/test-live.js besiktas genclerbirligi 00:45
 */

const { buildMatchMessage, buildLiveMessage } = require('./football');
const { SUPER_LIG_TEAMS } = require('./teams');

const team = (id) => {
  const t = SUPER_LIG_TEAMS.find((x) => x.id === id);
  return t ? { id: t.id, name: t.name, short: t.short, colors: t.colors } : { id, name: id, short: '', colors: [] };
};

/** Sahte maçın `now` anındaki durumu (saf fonksiyon). */
function testMatchState(t, now) {
  const min = Math.max(0, Math.floor((now - t.start_ms) / 60_000));
  const goalAt = t.goal_minute ?? 15;
  const endAt = t.end_minute ?? 20;
  const finished = min >= endAt;
  const scored = min >= goalAt;
  return {
    fixture_id: 'test',
    league: { id: 203, name: 'TEST · Süper Lig' },
    home: team(t.home),
    away: team(t.away),
    goals: { home: scored ? 1 : 0, away: 0 },
    goal_events: scored ? [{ minute: goalAt, extra: 0, team_id: t.home, player: 'Test Oyuncu', detail: 'Normal Goal' }] : [],
    status: finished ? { short: 'FT', long: 'Match Finished', elapsed: endAt } : { short: '1H', long: 'First Half', elapsed: min },
    is_live: !finished,
    is_finished: finished,
    minute: min,
  };
}

/** @returns {Promise<string|null>} ne yapıldığı; test yoksa null */
async function runTestMatch(db, messaging, now = Date.now()) {
  const ref = db.doc('football_meta/test');
  const snap = await ref.get();
  if (!snap.exists) return null;
  const t = snap.data();
  // Konsoldan elle kurulum için: start_at = "2026-11-09T00:45" (İstanbul saati, metin)
  if (!t.start_ms && t.start_at) t.start_ms = Date.parse(`${t.start_at}:00+03:00`);
  t.home = t.home || 'besiktas';
  t.away = t.away || 'genclerbirligi';
  if (!t.start_ms || Number.isNaN(t.start_ms)) return 'test-invalid';
  if (now < t.start_ms) return 'test-waiting';
  const m = testMatchState(t, now);
  const opts = { ignoreQuiet: true, titlePrefix: '[TEST] ' };
  const at = new Date(now);
  const alerts = [];
  const upd = {};
  if (!t.kickoff_sent) {
    alerts.push(buildMatchMessage({ type: 'kickoff' }, m, at, opts));
    upd.kickoff_sent = true;
  }
  if (m.goals.home > 0 && !t.goal_sent) {
    alerts.push(buildMatchMessage({ type: 'goal', side: 'home', scorer: m.goal_events[0] }, m, at, opts));
    upd.goal_sent = true;
  }
  if (m.is_finished && !t.ft_sent) {
    alerts.push(buildMatchMessage({ type: 'fulltime' }, m, at, opts));
    upd.ft_sent = true;
  }
  const messages = [...alerts.filter(Boolean), buildLiveMessage(m, now)];
  await messaging.sendEach(messages);
  if (m.is_finished) await ref.delete();
  else if (Object.keys(upd).length) await ref.set(upd, { merge: true });
  console.log(`[TEST] ${m.minute}. dk ${m.home.short} ${m.goals.home}-${m.goals.away} ${m.away.short}${alerts.length ? ` | bildirim: ${alerts.length}` : ''}`);
  return m.is_finished ? 'test-finished' : 'test-running';
}

/** İstanbul saatiyle "SS:DD" → sıradaki o an (ms). Türkiye 2016'dan beri sabit UTC+3. */
function nextIstanbulTime(hhmm, now = Date.now()) {
  const [h, mi] = hhmm.split(':').map(Number);
  const day = new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul' }).format(new Date(now));
  let t = Date.parse(`${day}T${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:00+03:00`);
  if (t <= now) t += 24 * 3_600_000;
  return t;
}

module.exports = { runTestMatch, testMatchState, nextIstanbulTime };
