import type { AnimalKind } from './game/types';
import type { Lang } from './i18n';

const KEY = 'blokbahcesi.v1';

export type DecorId = 'fence' | 'fountain' | 'bench' | 'lantern' | 'birdhouse';
export const DECORS: { id: DecorId; stars: number }[] = [
  { id: 'fence', stars: 3 },
  { id: 'bench', stars: 9 },
  { id: 'lantern', stars: 18 },
  { id: 'birdhouse', stars: 30 },
  { id: 'fountain', stars: 45 },
];
export const GARDEN_SLOTS = 5;

export interface SaveData {
  levels: Record<number, { stars: number; best: number }>;
  animals: Record<AnimalKind, number>;
  sprouts: number;
  daily: Record<string, number>;
  classic: number[];
  zenBest: number;
  garden: (DecorId | null)[];
  settings: { sound: boolean; haptics: boolean; symbols: boolean; lang: Lang };
}

function defaults(): SaveData {
  return {
    levels: {},
    animals: { hedgehog: 0, turtle: 0, ladybug: 0 },
    sprouts: 0,
    daily: {},
    classic: [],
    zenBest: 0,
    garden: Array(GARDEN_SLOTS).fill(null),
    settings: { sound: true, haptics: true, symbols: true, lang: 'tr' },
  };
}

function load(): SaveData {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return defaults();
    const d = JSON.parse(raw);
    const base = defaults();
    return {
      ...base, ...d,
      animals: { ...base.animals, ...d.animals },
      settings: { ...base.settings, ...d.settings },
      garden: Array.from({ length: GARDEN_SLOTS }, (_, i) => d.garden?.[i] ?? null),
    };
  } catch {
    return defaults();
  }
}

export const save: SaveData = load();

export function persist() {
  try { localStorage.setItem(KEY, JSON.stringify(save)); } catch { /* depolama dolu/kapalı */ }
}

export function resetProgress() {
  const keepSettings = save.settings;
  Object.assign(save, defaults(), { settings: keepSettings });
  persist();
}

export function totalStars(): number {
  return Object.values(save.levels).reduce((a, l) => a + l.stars, 0);
}

/** Açık olan en yüksek bölüm (bir önceki bitirilmişse açılır). */
export function highestUnlocked(): number {
  let n = 1;
  while (save.levels[n]?.stars) n++;
  return n;
}

export function recordLevel(id: number, stars: number, score: number) {
  const cur = save.levels[id] ?? { stars: 0, best: 0 };
  save.levels[id] = { stars: Math.max(cur.stars, stars), best: Math.max(cur.best, score) };
  persist();
}

export function recordCollection(animals: Record<AnimalKind, number>, sprouts: number) {
  for (const k of Object.keys(animals) as AnimalKind[]) save.animals[k] += animals[k];
  save.sprouts += sprouts;
  persist();
}

export function recordClassic(score: number): number {
  save.classic = [...save.classic, score].sort((a, b) => b - a).slice(0, 10);
  persist();
  return save.classic.indexOf(score);
}

export function unlockedDecors(): DecorId[] {
  const s = totalStars();
  return DECORS.filter((d) => s >= d.stars).map((d) => d.id);
}
