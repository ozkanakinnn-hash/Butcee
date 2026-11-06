// Elmas ekonomisi ve bahçe kataloğu (saf veri + kurallar; DOM yok).
export type DecorId =
  | 'fence' | 'flowerbed' | 'bench' | 'lantern' | 'birdhouse' | 'scarecrow'
  | 'tree' | 'beehive' | 'swing' | 'pond' | 'fountain' | 'gazebo';

/** Satın alınabilir bahçe objeleri: elmas fiyatı + açılması için gereken toplam yıldız. */
export const CATALOG: { id: DecorId; price: number; stars: number }[] = [
  { id: 'fence', price: 15, stars: 0 },
  { id: 'flowerbed', price: 20, stars: 0 },
  { id: 'bench', price: 25, stars: 3 },
  { id: 'lantern', price: 30, stars: 6 },
  { id: 'birdhouse', price: 35, stars: 10 },
  { id: 'scarecrow', price: 40, stars: 15 },
  { id: 'tree', price: 45, stars: 20 },
  { id: 'beehive', price: 50, stars: 28 },
  { id: 'swing', price: 60, stars: 36 },
  { id: 'pond', price: 70, stars: 45 },
  { id: 'fountain', price: 90, stars: 55 },
  { id: 'gazebo', price: 120, stars: 70 },
];

/** Ev seviyeleri (0 = başlangıç kulübesi). upgrade[i] = i → i+1 için elmas ve yıldız şartı */
export const HOUSE_LEVELS = 5;
export const HOUSE_UPGRADE: { price: number; stars: number }[] = [
  { price: 40, stars: 5 },
  { price: 80, stars: 20 },
  { price: 140, stars: 45 },
  { price: 220, stars: 80 },
];
export const GARDEN_SLOTS = 8;

export const PRICES = {
  extraUndo: 10,
  extraMoves: 30, // +5 hamle
  recedeWater: 25, // su 2 satır çekilir
};
export const REWARDS = {
  welcome: 25,
  firstClear: 2,
  threeStars: 3,
  boss: 20,
  rescue: 30,
  ad: 15,
  adsPerDay: 5,
};

/** Kritik eşikler: toplam yıldız ve tamamlanan bölüm sayısı. */
export const MILESTONES: { key: string; kind: 'stars' | 'levels'; at: number; reward: number }[] = [
  ...[10, 25, 45, 70, 100, 130, 160, 200, 240, 280].map((at, i) => ({ key: `s${at}`, kind: 'stars' as const, at, reward: [10, 15, 20, 25, 35, 45, 60, 70, 85, 100][i] })),
  ...[10, 20, 30, 40, 50, 60, 70, 80, 90, 100].map((at) => ({ key: `l${at}`, kind: 'levels' as const, at, reward: 20 + at / 2 })),
];

export function dueMilestones(claimed: string[], stars: number, levelsDone: number) {
  return MILESTONES.filter((m) => !claimed.includes(m.key) && (m.kind === 'stars' ? stars : levelsDone) >= m.at);
}

/** Bölüm ilk kez bitince verilecek elmas. */
export function levelReward(kind: 'normal' | 'boss' | 'rescue', firstClear: boolean, firstThreeStars: boolean): number {
  let n = 0;
  if (firstClear) n += kind === 'boss' ? REWARDS.boss : kind === 'rescue' ? REWARDS.rescue : REWARDS.firstClear;
  if (firstThreeStars) n += REWARDS.threeStars;
  return n;
}

// ---------- bahçe kodlama (sıralamada başkalarının bahçesini göstermek için, ≤ 64 karakter) ----------
export interface GardenSnapshot {
  house: number;
  slots: (DecorId | null)[];
  animals: { hedgehog: number; turtle: number; ladybug: number };
  sprouts: number;
}

const b36 = (n: number, w: number) => Math.max(0, Math.min(36 ** w - 1, Math.floor(n))).toString(36).padStart(w, '0');

export function encodeGarden(g: GardenSnapshot): string {
  const slots = Array.from({ length: GARDEN_SLOTS }, (_, i) => {
    const id = g.slots[i];
    const idx = id ? CATALOG.findIndex((c) => c.id === id) : -1;
    return idx < 0 ? '-' : idx.toString(36);
  }).join('');
  return `g1${g.house}${slots}${b36(g.animals.hedgehog, 2)}${b36(g.animals.turtle, 2)}${b36(g.animals.ladybug, 2)}${b36(g.sprouts, 3)}`;
}

export function decodeGarden(tag: string | null | undefined): GardenSnapshot | null {
  if (!tag || !tag.startsWith('g1') || tag.length < 3 + GARDEN_SLOTS + 9) return null;
  const house = Math.min(HOUSE_LEVELS - 1, parseInt(tag[2], 10) || 0);
  const slots = [...tag.slice(3, 3 + GARDEN_SLOTS)].map((ch) => (ch === '-' ? null : CATALOG[parseInt(ch, 36)]?.id ?? null));
  let p = 3 + GARDEN_SLOTS;
  const rd = (w: number) => { const v = parseInt(tag.slice(p, p + w), 36) || 0; p += w; return v; };
  return { house, slots, animals: { hedgehog: rd(2), turtle: rd(2), ladybug: rd(2) }, sprouts: rd(3) };
}
