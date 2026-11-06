'use strict';

/**
 * Piyasa verileri (Yahoo Finance, ~15 dk gecikmeli) → Firestore `market/latest` tek belge.
 * Uygulamanın sağ üst panelinde gösterilir. Yahoo'nun resmi olmayan "chart" arayüzü kullanılır;
 * değişirse bu modül hata verir ve son geçerli veri belgede kalır.
 */

const { Timestamp } = require('firebase-admin/firestore');

const TROY_OUNCE_GRAMS = 31.1034768;
const SYMBOLS = {
  usdtry: 'USDTRY=X',
  eurtry: 'EURTRY=X',
  bist100: 'XU100.IS',
  brent: 'BZ=F',
  gold_oz: 'GC=F',
};
const UA = 'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130 Mobile Safari/537.36';

async function fetchQuote(symbol, fetchImpl = fetch) {
  const url = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(symbol)}?interval=1d&range=5d`;
  const res = await fetchImpl(url, { headers: { 'User-Agent': UA, Accept: 'application/json' }, signal: AbortSignal.timeout(15_000) });
  if (!res.ok) throw new Error(`${symbol}: HTTP ${res.status}`);
  const json = await res.json();
  return parseChart(json, symbol);
}

/** Yahoo chart yanıtından fiyat ve önceki kapanış (saf fonksiyon). */
function parseChart(json, symbol = '') {
  const meta = json && json.chart && json.chart.result && json.chart.result[0] && json.chart.result[0].meta;
  if (!meta || typeof meta.regularMarketPrice !== 'number') throw new Error(`${symbol}: fiyat yok`);
  const prev = meta.chartPreviousClose ?? meta.previousClose ?? null;
  return { price: meta.regularMarketPrice, previousClose: typeof prev === 'number' ? prev : null, time: meta.regularMarketTime || null };
}

const pct = (price, prev) => (prev ? ((price - prev) / prev) * 100 : null);

/** Ham fiyatlardan uygulamanın gösterdiği listeyi kurar; gram altın = ons($) × USD/TRY ÷ 31,1035. */
function buildMarketItems(q) {
  const items = [];
  const add = (id, label, unit, price, prev, decimals) =>
    items.push({ id, label, unit, value: Number(price.toFixed(decimals)), change_pct: prev === null ? null : Number(pct(price, prev).toFixed(2)), decimals });
  if (q.usdtry) add('usdtry', 'Dolar', '₺', q.usdtry.price, q.usdtry.previousClose, 4);
  if (q.eurtry) add('eurtry', 'Euro', '₺', q.eurtry.price, q.eurtry.previousClose, 4);
  if (q.gold_oz && q.usdtry) {
    const gram = (q.gold_oz.price * q.usdtry.price) / TROY_OUNCE_GRAMS;
    const prevGram = q.gold_oz.previousClose && q.usdtry.previousClose
      ? (q.gold_oz.previousClose * q.usdtry.previousClose) / TROY_OUNCE_GRAMS
      : null;
    add('gram_gold', 'Gram Altın', '₺', gram, prevGram, 2);
  }
  if (q.gold_oz) add('gold_oz', 'Ons Altın', '$', q.gold_oz.price, q.gold_oz.previousClose, 2);
  if (q.bist100) add('bist100', 'BIST 100', '', q.bist100.price, q.bist100.previousClose, 2);
  if (q.brent) add('brent', 'Brent Petrol', '$', q.brent.price, q.brent.previousClose, 2);
  return items;
}

async function updateMarket(db, { fetchImpl = fetch } = {}) {
  const quotes = {};
  const errors = [];
  await Promise.all(Object.entries(SYMBOLS).map(async ([id, sym]) => {
    try {
      quotes[id] = await fetchQuote(sym, fetchImpl);
    } catch (err) {
      errors.push(err.message);
    }
  }));
  const items = buildMarketItems(quotes);
  if (items.length) {
    await db.doc('market/latest').set({ items, source: 'Yahoo Finance', delayed_minutes: 15, updated_at: Timestamp.now() });
  }
  return { count: items.length, errors };
}

module.exports = { parseChart, buildMarketItems, updateMarket, SYMBOLS };
