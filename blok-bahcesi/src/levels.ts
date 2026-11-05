import type { LevelDef } from './game/types';
import { CALIBRATED, RESCUE_LEVELS } from './levels-extra';
import { RESCUE_MAPS } from './levels-base';

/*
 * Bölüm verisi:
 *  - src/levels-base.ts : el yapımı 1-20. bölümler + Bahçıvanı Kurtar haritaları (metin haritası)
 *  - src/levels-extra.ts: scripts/calibrate.ts çıktısı — 1-20'nin bot ile ayarlanmış hamle sınırları,
 *                         21-60 arası üretilmiş bölümler ve kurtarma bölümlerinin su hızları.
 */
export const LEVELS: LevelDef[] = CALIBRATED;
export { RESCUE_LEVELS };

/** Kurtarma bölümü hangi ana bölümden sonra açılır */
export const RESCUE_AFTER: Record<number, number> = Object.fromEntries(RESCUE_MAPS.map((r) => [r.id, r.after]));

export function getLevel(id: number): LevelDef | undefined {
  return LEVELS.find((l) => l.id === id) ?? RESCUE_LEVELS.find((l) => l.id === id);
}
