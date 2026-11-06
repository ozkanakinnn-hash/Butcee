import type { LevelDef } from './game/types';

/*
 * Macera bölümleri. Harita yalnızca alt satırlarla yazılabilir (üstü boş sayılır), her satır 8 karakter.
 *  .  boş            T  toprak          K  kaya (silinmez)
 *  H  kirpi          P  kaplumbağa      U  uğur böceği
 *  S  tohum          O  yabani ot       I  buz
 *  b su  g yaprak  y güneş  s taş  p mor  r kırmızı (önceden yerleştirilmiş blok)
 */
/** El yapımı 1-20. bölümler (hamle sınırları scripts/calibrate.ts ile yeniden ayarlanır). */
export const LEVELS_BASE: LevelDef[] = [
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
      'TK...KOU',
      'TTP..TTT',
      'HOTH.TPT',
      'TTTT.KTT',
    ],
  },
  // ---- 20: BOSS ----
  {
    id: 20, goal: { type: 'boss' }, moves: 50, bossHp: 14,
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

/** Bahçıvanı Kurtar: ana bölümlerin arasına yerleşen ekstra bölümler. Su hızı kalibrasyonla seçilir. */
/** Bahçıvanı Kurtar: ana bölümlerin arasına yerleşen ekstra bölümler. Su hızı kalibrasyonla seçilir. */
/** Bahçıvanı Kurtar: ana bölümlerin arasına yerleşen ekstra bölümler. Bahçıvanın satırındaki boşlukların
 *  üstü önce temizlenmesi gereken "çatı" satırlarıyla kapalıdır. Su hızı kalibrasyonla seçilir. */
export const RESCUE_MAPS: { id: number; after: number; title: string; map: string[]; colors?: LevelDef['colors'] }[] = [
  { id: 101, after: 5, title: 'rescue.t1', map: [
    'TT.TTTTT',
    'TGT.T.TT',
    'TTTT.TTT',
    'TT.TTTTT',
    'TTTTT.TT',
  ] },
  { id: 102, after: 15, title: 'rescue.t2', map: [
    'TTTT.TTT',
    'T.TTTTTT',
    'TT.GT.TT',
    'TTT.TTTT',
    'TTTTT.TT',
    'T.TTTTTT',
  ] },
  { id: 103, after: 25, title: 'rescue.t3', colors: { sun: 18 }, map: [
    'TOT.TOTT',
    'TTTGT.TT',
    'TT.TTTTT',
    'TTTT.TTT',
    'T.TTTTTT',
    'TTTTTT.T',
  ] },
  { id: 104, after: 35, title: 'rescue.t4', map: [
    'TT.TTTTT',
    'TTT.TKTT',
    'K.TGTT.K',
    'TTT.TTTT',
    'TT.TTTTT',
    'TTTTT.TT',
    'T.TTTTTT',
  ] },
  { id: 105, after: 45, title: 'rescue.t5', colors: { sun: 16 }, map: [
    'TTI.TTTT',
    'TTTTT.IT',
    'T.TTTTTT',
    'TTG.TTT.',
    'TTTT.TTT',
    'T.TTTTTT',
    'TTTTTT.T',
  ] },
  { id: 106, after: 55, title: 'rescue.t6', colors: { sun: 16 }, map: [
    'TOT.TOTT',
    'TTTTTT.T',
    'T.TGTT.T',
    'TTT.TTTT',
    'TT.TTTTT',
    'TTTTT.TT',
    'T.TTTTTT',
  ] },
  { id: 107, after: 65, title: 'rescue.t7', colors: { sun: 16 }, map: [
    'TTI.TTTT',
    'T.TTTTIT',
    'TTTGT.TT',
    'TT.TTTTT',
    'TTTT.TTT',
    'T.TTTTTT',
    'TTTTTT.T',
  ] },
  { id: 108, after: 75, title: 'rescue.t8', map: [
    'K.TTTTTK',
    'TTTT.TTT',
    'TT.TGT.T',
    'TTTT.TTT',
    'T.TTTTTT',
    'TTTTT.TT',
    'TT.TTTTT',
  ] },
  { id: 109, after: 85, title: 'rescue.t9', colors: { sun: 18 }, map: [
    'TOTT.TOT',
    'OTT.TTTO',
    'TT.TGTTT',
    'T.TTT.TT',
    'TTTT.TTT',
    'TT.TTTTT',
    'TTTTTT.T',
  ] },
  { id: 110, after: 95, title: 'rescue.t10', map: [
    'T.TTTTTT',
    'TTTTT.TT',
    'TT.TTTTK',
    'TTTTGT.T',
    'TTT.TTTT',
    'T.TTTTTT',
    'TTTTT.TT',
    'TT.TTTTT',
  ] },
];
