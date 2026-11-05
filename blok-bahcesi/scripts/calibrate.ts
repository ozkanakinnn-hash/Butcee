// Bölümleri bot ile kalibre eder ve src/levels-extra.ts dosyasını yazar.
// Kullanım: npx vite-node scripts/calibrate.ts
import { writeFileSync } from 'node:fs';
import { Game } from '../src/game/game';
import { playOut } from '../src/game/bot';
import { generateAdventure } from '../src/game/levelgen';
import { findFullRows, parseMap } from '../src/game/board';
import { LEVELS_BASE, RESCUE_MAPS } from '../src/levels-base';
import type { LevelDef } from '../src/game/types';

const N = 40;
const LAST = 60;
/** Hedef bot kazanma oranı: 1. bölüm ~%98 → 60. bölüm ~%55, boss biraz daha zor. */
export const target = (id: number, boss: boolean) => 0.98 - ((id - 1) / (LAST - 1)) * 0.43 - (boss ? 0.05 : 0);

function needed(l: LevelDef, n = N): number[] {
  const out: number[] = [];
  for (let i = 0; i < n; i++) {
    const r = playOut(new Game('adventure', { ...l, moves: 150 }, 7919 * (i + 1) + l.id), 150);
    out.push(r.won ? r.moves : Infinity);
  }
  return out.sort((a, b) => a - b);
}

function winRate(l: LevelDef, n = N): number {
  let w = 0;
  for (let i = 0; i < n; i++) if (playOut(new Game('adventure', l, 104729 * (i + 1) + l.id)).won) w++;
  return w / n;
}

/** Hedef orana göre hamle sınırı (kalibrasyon: hamle ihtiyacı dağılımının ilgili yüzdeliği). */
function calibrateMoves(l: LevelDef): { moves: number; solved: number } | null {
  const need = needed(l);
  const solved = need.filter((x) => x !== Infinity).length / need.length;
  const t = target(l.id, l.goal.type === 'boss');
  if (solved < t + 0.02) return null;
  const q = need[Math.min(need.length - 1, Math.ceil(t * need.length) - 1)];
  const minMoves = l.id <= 3 ? 10 : 8;
  return { moves: Math.max(minMoves, q + 1), solved };
}

const report: string[] = [];
const out: LevelDef[] = [];
for (const base of LEVELS_BASE) {
  if (base.id <= 3) { out.push(base); report.push(`${base.id}\t${base.goal.type}\t${base.moves}\t(öğretici)\t${(winRate(base) * 100).toFixed(0)}`); continue; }
  const c = calibrateMoves(base);
  const l = { ...base, moves: c ? c.moves : base.moves };
  out.push(l);
  report.push(`${l.id}\t${l.goal.type}\t${l.moves}\t${c ? (c.solved * 100).toFixed(0) : 'X'}\t${(winRate(l) * 100).toFixed(0)}`);
}
for (let id = LEVELS_BASE.length + 1; id <= LAST; id++) {
  const d = (id - 21) / (LAST - 21);
  let chosen: LevelDef | null = null;
  for (let s = 0; s < 60 && !chosen; s++) {
    const l = generateAdventure(id, id * 1009 + s * 31, d);
    if (findFullRows(parseMap(l.map)).length) continue;
    if (l.goal.type === 'animals' && !l.map.join('').match(/[HPU]/)) continue;
    const g0 = new Game('adventure', l, 1);
    if (g0.isStuck()) continue;
    const c = calibrateMoves(l);
    if (!c) continue;
    // hayvan/tohum bölümleri en az 6 hamle istesin (çok kolay olmasın)
    const need = needed(l, 12);
    if (need[Math.floor(need.length / 2)] < 5 + d * 6) continue;
    chosen = { ...l, moves: c.moves };
  }
  if (!chosen) throw new Error('bölüm üretilemedi: ' + id);
  out.push(chosen);
  report.push(`${id}\t${chosen.goal.type}\t${chosen.moves}\t-\t${(winRate(chosen) * 100).toFixed(0)}`);
  console.log(report[report.length - 1]);
}

// Kurtarma bölümleri: su hızını hedef orana göre seç.
const rescue: LevelDef[] = [];
RESCUE_MAPS.forEach((r, i) => {
  const t = [0.95, 0.85, 0.85, 0.6, 0.6, 0.6][i] ?? 0.6;
  let pick: LevelDef | null = null;
  let best: { l: LevelDef; rate: number } | null = null;
  for (let every = 1; every <= 10 && !pick; every++) {
    const l: LevelDef = { id: r.id, map: r.map, goal: { type: 'rescue' }, moves: null, waterEvery: every, title: r.title, colors: r.colors };
    let w = 0;
    for (let k = 0; k < N; k++) if (playOut(new Game('adventure', l, 31337 * (k + 1)), 200).won) w++;
    if (!best || w / N > best.rate) best = { l, rate: w / N };
    if (w / N >= t) { pick = l; report.push(`${r.id}\trescue\tsu/${every}\t-\t${(w / N * 100).toFixed(0)}`); }
  }
  // hedefe ulaşılamazsa (bot tavanı) en iyi oranı veren hızı al
  if (!pick && best && best.rate >= 0.3) { pick = best.l; report.push(`${r.id}\trescue\tsu/${best.l.waterEvery}\t-\t${(best.rate * 100).toFixed(0)}`); }
  if (!pick) throw new Error('kurtarma bölümü çok zor: ' + r.id);
  rescue.push(pick);
});

const header = '// OTOMATİK ÜRETİLDİ: scripts/calibrate.ts (bot kalibrasyonu). Elle düzenlemeyin.\nimport type { LevelDef } from \'./game/types\';\n\n';
writeFileSync(new URL('../src/levels-extra.ts', import.meta.url),
  header + `export const CALIBRATED: LevelDef[] = ${JSON.stringify(out, null, 1)};\n\nexport const RESCUE_LEVELS: LevelDef[] = ${JSON.stringify(rescue, null, 1)};\n`);
writeFileSync(new URL('../docs/difficulty.tsv', import.meta.url), 'id\ttür\thamle\tçözülme%\tbot_kazanma%\n' + report.join('\n') + '\n');
console.log(report.join('\n'));
