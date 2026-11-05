import type { AnimalKind } from './game/types';
import type { Lang } from './i18n';
import { dueMilestones, GARDEN_SLOTS, REWARDS, type DecorId, type GardenSnapshot } from './economy';
export type { DecorId } from './economy';

const KEY = 'blokbahcesi.v1';


export interface SaveData {
  levels: Record<number, { stars: number; best: number }>;
  animals: Record<AnimalKind, number>;
  sprouts: number;
  daily: Record<string, number>;
  classic: number[];
  zenBest: number;
  garden: (DecorId | null)[];
  owned: DecorId[];
  house: number;
  diamonds: number;
  milestones: string[];
  ads: { day: string; count: number };
  rescues: Record<number, boolean>;
  player: { id: string; name: string } | null;
  settings: { sound: boolean; music: boolean; haptics: boolean; symbols: boolean; lang: Lang };
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
    owned: [],
    house: 0,
    diamonds: REWARDS.welcome,
    milestones: [],
    ads: { day: '', count: 0 },
    rescues: {},
    player: null,
    settings: { sound: true, music: true, haptics: true, symbols: true, lang: 'tr' },
  };
}

function load(): SaveData {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return defaults();
    const d = JSON.parse(raw);
    const base = defaults();
    const garden: (DecorId | null)[] = Array.from({ length: GARDEN_SLOTS }, (_, i) => d.garden?.[i] ?? null);
    // v0.1 kayıtları: yıldızla açılan dekorlar artık "sahip olunan" sayılır
    const owned: DecorId[] = Array.from(new Set([...(d.owned ?? []), ...garden.filter((x): x is DecorId => !!x)]));
    return {
      ...base, ...d,
      animals: { ...base.animals, ...d.animals },
      settings: { ...base.settings, ...d.settings },
      garden, owned,
      ads: { ...base.ads, ...d.ads },
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

export function levelsDone(): number {
  return Object.keys(save.levels).filter((k) => Number(k) < 100 && save.levels[Number(k)].stars > 0).length;
}

// ---------- elmas ----------
type Listener = () => void;
const listeners = new Set<Listener>();
export function onDiamonds(fn: Listener) { listeners.add(fn); return () => listeners.delete(fn); }

export function addDiamonds(n: number) {
  if (n <= 0) return;
  save.diamonds += n;
  persist();
  listeners.forEach((f) => f());
}

export function spendDiamonds(n: number): boolean {
  if (save.diamonds < n) return false;
  save.diamonds -= n;
  persist();
  listeners.forEach((f) => f());
  return true;
}

/** Açılmış ama henüz alınmamış kritik eşik ödülleri: alındı olarak işaretler ve toplamı döndürür. */
export function claimMilestones(): { key: string; reward: number }[] {
  const due = dueMilestones(save.milestones, totalStars(), levelsDone());
  if (!due.length) return [];
  save.milestones.push(...due.map((m) => m.key));
  addDiamonds(due.reduce((a, m) => a + m.reward, 0));
  return due;
}

export function adsLeftToday(today: string): number {
  return save.ads.day === today ? Math.max(0, REWARDS.adsPerDay - save.ads.count) : REWARDS.adsPerDay;
}
export function recordAd(today: string) {
  if (save.ads.day !== today) save.ads = { day: today, count: 0 };
  save.ads.count++;
  addDiamonds(REWARDS.ad);
}

export function gardenSnapshot(): GardenSnapshot {
  return { house: save.house, slots: save.garden, animals: save.animals, sprouts: save.sprouts };
}
