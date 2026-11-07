'use strict';

/**
 * Süper Lig takımları — API anahtarı olmadan da uygulamada seçilebilsin diye sabit liste.
 *
 * Takım kimliği kendi kısa adımızdır (`besiktas`), API-Football'un sayısal kimliği değil. Böylece:
 *  - kullanıcı anahtar eklenmeden önce de takım seçebilir,
 *  - canlı skor botu API'deki adları ("Besiktas", "Istanbul Basaksehir", "Caykur Rizespor"…) `aliases`
 *    üzerinden aynı kimliğe eşler; bildirim konusu `team_<kimlik>` hiç değişmez.
 * Liste 2025-26 sezonu kadrosudur. Küme düşen/çıkan takımlar: API anahtarı varsa `sync-teams` güncel
 * sezonu yazar (listede olmayan yeni takımlar eklenir, ligden çıkanlar `active: false` olur); yoksa
 * bu dosyayı düzenleyip seed çalıştırın.
 * Renkler arma yerine gösterilen rozet içindir (kulüp logoları kullanılmaz).
 */

const SUPER_LIG_TEAMS = [
  { id: 'galatasaray', name: 'Galatasaray', short: 'GS', colors: ['#A90432', '#FDB912'], aliases: ['Galatasaray SK'] },
  { id: 'fenerbahce', name: 'Fenerbahçe', short: 'FB', colors: ['#0A1E5E', '#FFED00'], aliases: ['Fenerbahce SK'] },
  { id: 'besiktas', name: 'Beşiktaş', short: 'BJK', colors: ['#111111', '#FFFFFF'], aliases: ['Besiktas JK'] },
  { id: 'trabzonspor', name: 'Trabzonspor', short: 'TS', colors: ['#8B1D41', '#4DB6E8'], aliases: [] },
  { id: 'basaksehir', name: 'Başakşehir', short: 'İBFK', colors: ['#F26722', '#13294B'], aliases: ['Istanbul Basaksehir', 'Medipol Basaksehir', 'Rams Basaksehir', 'Basaksehir FK'] },
  { id: 'samsunspor', name: 'Samsunspor', short: 'SAM', colors: ['#E30613', '#FFFFFF'], aliases: ['Yilport Samsunspor'] },
  { id: 'goztepe', name: 'Göztepe', short: 'GÖZ', colors: ['#FFD200', '#E30613'], aliases: ['Goztepe SK'] },
  { id: 'eyupspor', name: 'Eyüpspor', short: 'EYP', colors: ['#5B2C83', '#FFD100'], aliases: ['Ikas Eyupspor'] },
  { id: 'kasimpasa', name: 'Kasımpaşa', short: 'KAS', colors: ['#1E2A5A', '#FFFFFF'], aliases: ['Kasimpasa SK'] },
  { id: 'alanyaspor', name: 'Alanyaspor', short: 'ALN', colors: ['#F58220', '#00843D'], aliases: ['Corendon Alanyaspor'] },
  { id: 'antalyaspor', name: 'Antalyaspor', short: 'ANT', colors: ['#E30613', '#FFFFFF'], aliases: ['Hesap.com Antalyaspor', 'Bitexen Antalyaspor'] },
  { id: 'konyaspor', name: 'Konyaspor', short: 'KON', colors: ['#0A8A3A', '#FFFFFF'], aliases: ['Tumosan Konyaspor'] },
  { id: 'kayserispor', name: 'Kayserispor', short: 'KAY', colors: ['#E30613', '#FFD100'], aliases: ['Zecorner Kayserispor'] },
  { id: 'rizespor', name: 'Çaykur Rizespor', short: 'RİZ', colors: ['#1B8B3A', '#1E5AA8'], aliases: ['Rizespor', 'Caykur Rizespor'] },
  { id: 'gaziantep', name: 'Gaziantep FK', short: 'GFK', colors: ['#D71920', '#111111'], aliases: ['Gaziantep', 'Gazisehir Gaziantep', 'Gaziantep FK'] },
  { id: 'kocaelispor', name: 'Kocaelispor', short: 'KOC', colors: ['#00843D', '#111111'], aliases: ['Bitexen Kocaelispor'] },
  { id: 'genclerbirligi', name: 'Gençlerbirliği', short: 'GB', colors: ['#E30613', '#111111'], aliases: ['Genclerbirligi SK', 'Genclerbirligi'] },
  { id: 'karagumruk', name: 'Fatih Karagümrük', short: 'FKG', colors: ['#C8102E', '#111111'], aliases: ['Karagumruk', 'Fatih Karagumruk'] },
];

/** Karşılaştırma için ad: Türkçe/aksan katlama, kulüp eklerini (FK, SK, JK, AS) atma. */
function teamKeyText(name) {
  const folded = String(name || '')
    .replace(/İ/g, 'i')
    .replace(/I/g, 'i')
    .toLowerCase()
    .replace(/[çğıöşü]/g, (c) => ({ ç: 'c', ğ: 'g', ı: 'i', ö: 'o', ş: 's', ü: 'u' })[c])
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
  return folded
    .split(' ')
    .filter((w) => !['fk', 'sk', 'jk', 'as', 'a', 's', 'kulubu'].includes(w))
    .join(' ');
}

/** API'deki takım adını kimliğe çevirir; listede yoksa addan kimlik üretir (yeni çıkan takımlar). */
function createTeamResolver(teams = SUPER_LIG_TEAMS) {
  const index = new Map();
  for (const t of teams) {
    for (const n of [t.id, t.name, ...(t.aliases || [])]) index.set(teamKeyText(n), t);
  }
  const resolve = (name) => {
    const key = teamKeyText(name);
    const t = index.get(key);
    return t ? t.id : key.replace(/ /g, '-') || 'bilinmeyen';
  };
  resolve.team = (id) => teams.find((t) => t.id === id) || null;
  return resolve;
}

module.exports = { SUPER_LIG_TEAMS, teamKeyText, createTeamResolver };
