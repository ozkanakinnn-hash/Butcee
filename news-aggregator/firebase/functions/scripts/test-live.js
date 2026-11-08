'use strict';

/**
 * Canlı skor paneli TEST maçı kurar (Cloud Shell'den bir kez çalıştırılır; maçı Firebase görevi oynatır).
 *
 *   node scripts/test-live.js besiktas genclerbirligi 00:45 [gol_dakikası=15] [bitiş_dakikası=20]
 *   node scripts/test-live.js iptal
 *
 * Takım kimlikleri: src/teams.js (galatasaray, fenerbahce, besiktas, trabzonspor, …).
 */
const { getFirestore } = require('firebase-admin/firestore');
const { initAdmin } = require('./init-admin');
const { nextIstanbulTime } = require('../src/test-match');
const { SUPER_LIG_TEAMS } = require('../src/teams');

async function main() {
  const [home, away, time, goal = '15', end = '20'] = process.argv.slice(2);
  initAdmin();
  const ref = getFirestore().doc('football_meta/test');
  if (home === 'iptal') {
    await ref.delete();
    console.log('Test maçı iptal edildi.');
    return;
  }
  const ids = SUPER_LIG_TEAMS.map((t) => t.id);
  if (!ids.includes(home) || !ids.includes(away) || !/^\d{1,2}:\d{2}$/.test(time || '')) {
    throw new Error(`Kullanım: node scripts/test-live.js <ev> <deplasman> SS:DD [gol_dk] [bitiş_dk]\nTakımlar: ${ids.join(', ')}`);
  }
  const start = nextIstanbulTime(time);
  await ref.set({ home, away, start_ms: start, goal_minute: Number(goal), end_minute: Number(end) });
  const fmt = (ms) => new Date(ms).toLocaleString('tr-TR', { timeZone: 'Europe/Istanbul' });
  console.log(`✔ Test maçı kuruldu: ${home} – ${away}`);
  console.log(`  Başlama: ${fmt(start)} | Gol: ${fmt(start + Number(goal) * 60_000)} | Bitiş: ${fmt(start + Number(end) * 60_000)}`);
  console.log(`  Bildirimler yalnızca "${home}" veya "${away}" takımını takip eden cihazlara gider.`);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
