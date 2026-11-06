'use strict';

/**
 * Piyasa verileri → Firestore `market/latest` tek belge (uygulamanın sağ üst paneli).
 *
 * Kaynak: Truncgil Finans (finans.truncgil.com/v4/today.json) — ücretsiz, anahtarsız, herkese açık.
 * Yahoo Finance GitHub sunucularından gelen istekleri HTTP 429 ile reddettiği için kullanılmıyor
 * (koruması aşılmaz). Truncgil'de:
 *  - USD, EUR, GRA (gram altın), XU100 (BIST 100): fiyat + günlük değişim
 *  - ONS: yalnızca değişim → fiyat gram altın ve dolardan hesaplanır (gram × 31,1035 ÷ USD/TL)
 *  - BRENT: çoğu zaman fiyat 0 gelir → fiyat yoksa gösterilmez
 * Yanıt zaman zaman tümüyle geçerli JSON olmadığından kalemler tek tek ayıklanır.
 */

const { Timestamp } = require('firebase-admin/firestore');

const SOURCE_URL = 'https://finans.truncgil.com/v4/today.json';
const UA = 'HaberAkisi/1.0 (+https://github.com/ozkanakinnn-hash/Butcee)';
const TROY_OUNCE_GRAMS = 31.1034768;

/** Yanıttaki `"ANAHTAR":{...}` kalemlerini tek tek okur; bozuk kalemleri atlar (saf fonksiyon). */
function parseTruncgil(text) {
  const out = {};
  for (const [, key, obj] of String(text).matchAll(/"([^"{}]+)":(\{[^{}]*\})/g)) {
    try {
      out[key] = JSON.parse(obj);
    } catch {
      // bozuk kalem — atla
    }
  }
  const date = /"Update_Date":"([^"]+)"/.exec(text);
  return { items: out, updateDate: date ? date[1] : null };
}

const num = (v) => (typeof v === 'number' && Number.isFinite(v) && v > 0 ? v : null);
const chg = (v) => (typeof v === 'number' && Number.isFinite(v) ? v : null);

/** Uygulamanın gösterdiği sıralı liste (saf fonksiyon). */
function buildMarketItems(t) {
  const items = [];
  const add = (id, label, unit, value, change, decimals) => {
    if (value === null) return;
    items.push({ id, label, unit, value: Number(value.toFixed(decimals)), change_pct: change, decimals });
  };
  const usd = num(t.USD && t.USD.Selling);
  const gram = num(t.GRA && t.GRA.Selling);
  add('usdtry', 'Dolar', '₺', usd, chg(t.USD && t.USD.Change), 4);
  add('eurtry', 'Euro', '₺', num(t.EUR && t.EUR.Selling), chg(t.EUR && t.EUR.Change), 4);
  add('gram_gold', 'Gram Altın', '₺', gram, chg(t.GRA && t.GRA.Change), 2);
  const ons = num(t.ONS && t.ONS.Selling) ?? (gram && usd ? (gram * TROY_OUNCE_GRAMS) / usd : null);
  add('gold_oz', 'Ons Altın', '$', ons, chg(t.ONS && t.ONS.Change), 2);
  add('bist100', 'BIST 100', '', num(t.XU100 && t.XU100.Selling), chg(t.XU100 && t.XU100.Change), 2);
  add('brent', 'Brent Petrol', '$', num(t.BRENT && t.BRENT.Selling), chg(t.BRENT && t.BRENT.Change), 2);
  return items;
}

async function updateMarket(db, { fetchImpl = fetch } = {}) {
  const res = await fetchImpl(SOURCE_URL, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(15_000) });
  if (!res.ok) return { count: 0, errors: [`Truncgil: HTTP ${res.status}`] };
  const { items: raw, updateDate } = parseTruncgil(await res.text());
  const items = buildMarketItems(raw);
  const missing = ['usdtry', 'eurtry', 'gram_gold', 'gold_oz', 'bist100', 'brent'].filter((id) => !items.some((i) => i.id === id));
  if (items.length) {
    await db.doc('market/latest').set({
      items,
      source: 'Truncgil Finans',
      source_updated: updateDate || '',
      updated_at: Timestamp.now(),
    });
  }
  return { count: items.length, missing };
}

module.exports = { parseTruncgil, buildMarketItems, updateMarket, TROY_OUNCE_GRAMS };
