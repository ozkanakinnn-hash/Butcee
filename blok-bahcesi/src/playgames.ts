// Google Play Games: platform kimliği + "Bahçe Ligi" sıralaması.
// Yerel Android eklentisi: android/app/src/main/java/com/blokbahcesi/app/PlayGamesPlugin.java
import { Capacitor, registerPlugin } from '@capacitor/core';
import { decodeGarden, encodeGarden, type GardenSnapshot } from './economy';
import { gardenSnapshot, persist, save } from './storage';

export interface LbEntry { rank: number; name: string; score: number; playerId: string; garden: GardenSnapshot | null; me?: boolean }

interface PlayGamesNative {
  status(): Promise<{ configured: boolean; leaderboard: boolean }>;
  signIn(): Promise<{ id: string; name: string }>;
  submitScore(o: { score: number; tag: string }): Promise<void>;
  loadTop(o: { max: number }): Promise<{ entries: { rank: number; name: string; score: number; tag: string; playerId: string }[] }>;
  loadMine(): Promise<{ rank: number; score: number } | { rank: -1 }>;
}

const PG = registerPlugin<PlayGamesNative>('PlayGames');
const native = Capacitor.getPlatform() === 'android';

export type LbState = 'web' | 'not_configured' | 'signed_out' | 'ready';

export async function pgStatus(): Promise<LbState> {
  if (!native) return 'web';
  try {
    const s = await PG.status();
    if (!s.configured || !s.leaderboard) return 'not_configured';
    return save.player ? 'ready' : 'signed_out';
  } catch {
    return 'not_configured';
  }
}

/** Açılışta sessiz giriş (Play Games v2 otomatik giriş yapar). */
export async function signIn(): Promise<boolean> {
  if (!native) return false;
  try {
    const p = await PG.signIn();
    save.player = { id: p.id, name: p.name };
    persist();
    return true;
  } catch {
    return false;
  }
}

/** Sıralama puanı: tüm macera bölümlerindeki en iyi skorların toplamı. */
export function gardenScore(): number {
  return Object.entries(save.levels).reduce((a, [, l]) => a + (l.best || 0), 0);
}

export async function submitGardenScore(): Promise<void> {
  if (!native || !save.player) return;
  try { await PG.submitScore({ score: gardenScore(), tag: encodeGarden(gardenSnapshot()) }); } catch { /* çevrimdışı */ }
}

export async function loadLeaderboard(max = 25): Promise<{ top: LbEntry[]; mine: { rank: number; score: number } | null }> {
  const res = await PG.loadTop({ max });
  const top = res.entries.map((e) => ({
    rank: e.rank, name: e.name, score: e.score, playerId: e.playerId, garden: decodeGarden(e.tag), me: e.playerId === save.player?.id,
  }));
  let mine: { rank: number; score: number } | null = null;
  try { const m = await PG.loadMine(); if (m.rank > 0) mine = m as { rank: number; score: number }; } catch { /* yok */ }
  return { top, mine };
}

/** Yalnızca tarayıcı önizlemesi için açıkça "örnek veri" olarak etiketlenen liste. */
export function demoLeaderboard(): LbEntry[] {
  const g = (house: number, slots: (string | null)[], a: number[], sp: number): GardenSnapshot =>
    ({ house, slots: slots as GardenSnapshot['slots'], animals: { hedgehog: a[0], turtle: a[1], ladybug: a[2] }, sprouts: sp });
  return [
    { rank: 1, name: 'Örnek Oyuncu A', score: 182400, playerId: 'a', garden: g(4, ['tree', 'fountain', 'flowerbed', 'pond', 'gazebo', 'swing', 'lantern', 'bench'], [6, 6, 6], 70) },
    { rank: 2, name: 'Örnek Oyuncu B', score: 151900, playerId: 'b', garden: g(3, ['fence', 'tree', 'flowerbed', 'beehive', 'scarecrow', null, 'bench', 'lantern'], [5, 3, 6], 44) },
    { rank: 3, name: 'Örnek Oyuncu C', score: 120300, playerId: 'c', garden: g(2, ['flowerbed', 'fence', null, 'birdhouse', 'tree', null, null, 'bench'], [3, 2, 4], 30) },
    { rank: 4, name: 'Örnek Oyuncu D', score: 98100, playerId: 'd', garden: null },
    { rank: 5, name: 'Örnek Oyuncu E', score: 76500, playerId: 'e', garden: null },
  ];
}
