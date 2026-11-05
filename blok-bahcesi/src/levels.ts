import type { LevelDef } from './game/types';

/*
 * Macera bölümleri. Harita yalnızca alt satırlarla yazılabilir (üstü boş sayılır), her satır 8 karakter.
 *  .  boş            T  toprak          K  kaya (silinmez)
 *  H  kirpi          P  kaplumbağa      U  uğur böceği
 *  S  tohum          O  yabani ot       I  buz
 *  b su  g yaprak  y güneş  s taş  p mor  r kırmızı (önceden yerleştirilmiş blok)
 */
export const LEVELS: LevelDef[] = [
  // ---- 1-3: öğretici ----
  {
    id: 1, goal: { type: 'lines', count: 4 }, moves: 12,
    startHand: ['O', 'O', 'I', 'O', 'L'],
    hints: ['hint.select', 'hint.drop'],
    map: [
      'TTTT....',
      'TTTT....',
      'TTTTTT..',
      'TTTTTT..',
    ],
  },
  {
    id: 2, goal: { type: 'animals' }, moves: 12,
    startHand: ['L3', 'I', 'T', 'O', 'J'],
    hints: ['hint.rotate', 'hint.animals'],
    map: [
      'TTT.....',
      'TTTTHT..',
      'TTHTTTT.',
    ],
  },
  {
    id: 3, goal: { type: 'seeds', count: 2 }, moves: 12,
    startHand: ['D2', 'L3', 'O', 'I', 'T'],
    colors: { water: 25 },
    hints: ['hint.water', 'hint.undo'],
    map: [
      'TT......',
      'bbbbbb..',
      'TSTTTST.',
    ],
  },
  // ---- 4-9 ----
  {
    id: 4, goal: { type: 'lines', count: 6 }, moves: 18,
    hints: ['hint.combo'],
    map: [
      'T.......',
      'TT.....T',
      'TTT...TT',
      'TTTT.TTT',
      'TTTT.TTT',
    ],
  },
  {
    id: 5, goal: { type: 'animals' }, moves: 16,
    hints: ['hint.rock'],
    map: [
      'K.......',
      'TT....TT',
      'THT..TPT',
      'TTTU.TTK',
    ],
  },
  {
    id: 6, goal: { type: 'seeds', count: 3 }, moves: 18,
    colors: { water: 20 },
    map: [
      'T.....TS',
      'ST...TTT',
      'TST.TTST',
    ],
  },
  {
    id: 7, goal: { type: 'lines', count: 6 }, moves: 20,
    colors: { sun: 20 },
    hints: ['hint.sun'],
    map: [
      'T.....TT',
      'TT...TTT',
      'TTO.OTTT',
      'TOT.TOTT',
    ],
  },
  {
    id: 8, goal: { type: 'animals' }, moves: 22,
    map: [
      '.K....T.',
      '.TT..HT.',
      'TTP..TTU',
      'THTT.TTK',
    ],
  },
  {
    id: 9, goal: { type: 'seeds', count: 4 }, moves: 22,
    colors: { water: 16, sun: 14 },
    hints: ['hint.ice'],
    map: [
      'ST....TS',
      'TIS..TTI',
      'TSTI.TST',
    ],
  },
  // ---- 10: BOSS ----
  {
    id: 10, goal: { type: 'boss' }, moves: 40, bossHp: 10,
    colors: { stone: 10 },
    hints: ['hint.boss', 'hint.stone'],
    map: [
      'TT...TT.',
      'TTT.TTTT',
    ],
  },
  // ---- 11-19 ----
  {
    id: 11, goal: { type: 'lines', count: 10 }, moves: 24,
    map: [
      'T......T',
      'TTp....T',
      'TTT...yT',
      'TbTT..TT',
      'TTTT.TrT',
    ],
  },
  {
    id: 12, goal: { type: 'lines', count: 6 }, moves: 18,
    colors: { leaf: 32 },
    hints: ['hint.leaf'],
    map: [
      'g.......',
      'gggg..gg',
      'ggggg.gg',
    ],
  },
  {
    id: 13, goal: { type: 'animals' }, moves: 26,
    colors: { sun: 14 },
    map: [
      'H.....T.',
      'TOT...TT',
      'TTP..HTU',
      'THTO.TKT',
    ],
  },
  {
    id: 14, goal: { type: 'seeds', count: 4 }, moves: 24,
    colors: { water: 16 },
    map: [
      'T......S',
      'SKT...TS',
      'TSTT.STK',
    ],
  },
  {
    id: 15, goal: { type: 'lines', count: 12 }, moves: 30,
    colors: { sun: 14 },
    map: [
      'TT....TT',
      'TTO..OTT',
      'TTT..TTT',
      'IT.TT.TI',
      'TTTT.TTT',
    ],
  },
  {
    id: 16, goal: { type: 'animals' }, moves: 28,
    map: [
      'K......K',
      'TU....HT',
      'TTP..TTT',
      'THT..PTT',
      'TTTU.TTH',
    ],
  },
  {
    id: 17, goal: { type: 'seeds', count: 5 }, moves: 28,
    colors: { water: 16, sun: 14 },
    map: [
      'S.....IS',
      'TO...TTT',
      'TSI..STO',
      'TTTS.TTT',
    ],
  },
  {
    id: 18, goal: { type: 'lines', count: 14 }, moves: 32,
    colors: { leaf: 22 },
    map: [
      'g......g',
      'gg....gg',
      'TTT..TTT',
      'TKTT.TKT',
      'TTTT.TTT',
    ],
  },
  {
    id: 19, goal: { type: 'animals' }, moves: 32,
    colors: { sun: 14 },
    map: [
      '.......U',
      'H.....KT',
      'TK...TOT',
      'TTP..TTT',
      'TOTH.TPT',
      'TTTT.KTT',
    ],
  },
  // ---- 20: BOSS ----
  {
    id: 20, goal: { type: 'boss' }, moves: 50, bossHp: 16,
    colors: { stone: 10, leaf: 13 },
    hints: ['hint.boss2'],
    map: [
      'K......K',
      'TT....TT',
      'TTT..TTT',
      'TTTT.TTT',
    ],
  },
];

export function getLevel(id: number): LevelDef | undefined {
  return LEVELS.find((l) => l.id === id);
}
