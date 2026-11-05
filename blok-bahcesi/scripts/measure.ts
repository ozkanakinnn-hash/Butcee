import { Game } from '../src/game/game';
import { playOut } from '../src/game/bot';
import { LEVELS } from '../src/levels';

const N = Number(process.argv[2] ?? 30);
for (const l of LEVELS) {
  let wins = 0; const mv: number[] = [];
  for (let i = 0; i < N; i++) {
    const free = new Game('adventure', { ...l, moves: 200 }, 1000 + i);
    const r = playOut(free, 200);
    if (r.won) mv.push(r.moves);
    const g = new Game('adventure', l, 5000 + i);
    if (playOut(g).won) wins++;
  }
  mv.sort((a, b) => a - b);
  const q = (p: number) => mv[Math.floor((mv.length - 1) * p)] ?? -1;
  console.log(`L${l.id} ${l.goal.type} limit=${l.moves} winRate=${(wins / N * 100).toFixed(0)}% need p50=${q(0.5)} p75=${q(0.75)} solved=${mv.length}/${N}`);
}
