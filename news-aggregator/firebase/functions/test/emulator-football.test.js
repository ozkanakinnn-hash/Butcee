'use strict';

/** Futbol izleyicisi uçtan uca (emülatör + sahte API + sahte FCM). */
const test = require('node:test');
const assert = require('node:assert/strict');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('canlı maç izlenir, goller bildirilir, maç bitince döngü sona erer', { skip, timeout: 60_000 }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const fb = require('../src/football');
  const { watch } = require('../scripts/football-watch');

  initializeApp({ projectId: 'demo-football' });
  const db = getFirestore();
  const kickoff = new Date(Date.now() - 10 * 60_000);
  const today = fb.istanbulDate();
  await db.doc('football_meta/state').set({ season: 2026, teams_synced_at: Timestamp.now(), fixtures_date: today });

  const mk = (status, home, away, events = []) => ({
    fixture: { id: 77, date: kickoff.toISOString(), status: { short: status, elapsed: 10 } },
    league: { id: 203, name: 'Süper Lig' },
    teams: { home: { id: 645, name: 'Galatasaray' }, away: { id: 611, name: 'Fenerbahçe' } },
    goals: { home, away },
    events,
  });
  // Başlangıç: maç kayıtlı ve canlı 0-0
  await db.doc('matches/77').set({ ...fb.matchDocFromFixture(mk('1H', 0, 0)) });

  // Sahte API: her çağrıda maç ilerler
  const frames = [
    mk('1H', 1, 0, [{ time: { elapsed: 15 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]),
    mk('FT', 1, 0, [{ time: { elapsed: 15 }, team: { id: 645 }, player: { name: 'M. Icardi' }, type: 'Goal', detail: 'Normal Goal' }]),
  ];
  const api = { remaining: 1000, used: 0, get: async () => { api.used++; return [frames.shift() || frames[frames.length - 1]]; } };
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0 }; } };

  const waits = [];
  await watch(db, api, messaging, { sleep: async (ms) => { waits.push(ms); } });

  assert.equal(api.used, 2);
  assert.equal(waits.length, 1, 'maç bitince beklemeden çıkmalı');
  assert.equal(waits[0], 60_000, 'bol istek varken 60 sn aralık');
  assert.deepEqual(sent.map((m) => m.notification.title), ['⚽ GOL! Galatasaray 1-0 Fenerbahçe', '🏁 Maç sonucu']);
  const doc = (await db.doc('matches/77').get()).data();
  assert.deepEqual(doc.goals, { home: 1, away: 0 });
  assert.equal(doc.is_finished, true);
});

test('plan güncel sezonu kapsamıyorsa 24 saat beklenir, istek harcanmaz', { skip }, async () => {
  const { getFirestore } = require('firebase-admin/firestore');
  const { watch } = require('../scripts/football-watch');
  const db = getFirestore();
  await db.doc('football_meta/state').set({ season: null, teams_synced_at: null, fixtures_date: null, plan_blocked_until: null });
  let calls = 0;
  const api = {
    remaining: 100,
    used: 0,
    get: async () => {
      calls++;
      throw new Error('/fixtures: Free plans do not have access to this season, try from 2022 to 2024.');
    },
  };
  await watch(db, api, { sendEach: async () => ({}) }, { sleep: async () => {} });
  assert.equal(calls, 1);
  const meta = (await db.doc('football_meta/state').get()).data();
  assert.ok(meta.plan_blocked_until.toMillis() > Date.now() + 23 * 3_600_000);
  await watch(db, api, { sendEach: async () => ({}) }, { sleep: async () => {} });
  assert.equal(calls, 1, 'beklemedeyken API çağrılmaz');
});
