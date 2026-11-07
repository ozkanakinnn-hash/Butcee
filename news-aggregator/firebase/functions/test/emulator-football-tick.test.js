'use strict';

/** Firebase dakikalık görevi: footballTick turları (emülatör + sahte API + sahte FCM). */
const test = require('node:test');
const assert = require('node:assert/strict');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('tick: maç yokken API çağrılmaz; canlı maçta tur atar, aralığı korur, gol bildirir', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { tick } = require('../src/football-job');

  initializeApp({ projectId: 'demo-tick' });
  const db = getFirestore();
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today });

  let made = 0;
  const frames = [];
  const api = { remaining: 1000, used: 0, get: async () => { api.used++; return [frames.shift()]; } };
  const makeApi = () => { made++; return api; };
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  // 1) Maç yok → boşta, API istemcisi bile oluşturulmaz
  assert.equal(await tick(db, makeApi, messaging), 'idle');
  assert.equal(made, 0);

  // 2) Anahtar yoksa (makeApi null) ve hazırlık gerekiyorsa sessizce çıkar
  await db.doc('football_meta/state').update({ fixtures_date: 'eski' });
  assert.equal(await tick(db, () => null, messaging), 'no-key');
  await db.doc('football_meta/state').update({ fixtures_date: today });

  // 3) Canlı maç → tur atılır, gol bildirilir, sonraki kontrol zamanı yazılır
  const kickoff = new Date(Date.now() - 10 * 60_000);
  const mk = (status, home, away, events = []) => ({
    fixture: { id: 88, date: kickoff.toISOString(), status: { short: status, elapsed: 12 } },
    league: { id: 203, name: 'Süper Lig' },
    teams: { home: { id: 645, name: 'Galatasaray' }, away: { id: 611, name: 'Fenerbahçe' } },
    goals: { home, away },
    events,
  });
  await db.doc('matches/88').set(fb.matchDocFromFixture(mk('1H', 0, 0)));
  frames.push(mk('1H', 1, 0, [{ time: { elapsed: 11 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]));
  assert.equal(await tick(db, makeApi, messaging), 'polled');
  assert.equal(api.used, 1);
  assert.deepEqual(sent.map((m) => m.notification.title), ['⚽ GOL! Galatasaray 1-0 Fenerbahçe']);
  assert.ok((await db.doc('football_meta/state').get()).get('next_poll_at'));

  // 4) Hemen ardından gelen tur: aralık dolmadı → istek yok
  assert.equal(await tick(db, makeApi, messaging), 'throttled');
  assert.equal(api.used, 1);

  // 5) Aralık dolunca tekrar tur; maç bitti → sonuç bildirimi
  frames.push(mk('FT', 1, 0, [{ time: { elapsed: 11 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]));
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 70_000), 'polled');
  assert.equal(sent[sent.length - 1].notification.title, '🏁 Maç sonucu');
  assert.equal(await tick(db, makeApi, messaging, Date.now() + 200_000), 'idle', 'biten maç izlenmez');
});
