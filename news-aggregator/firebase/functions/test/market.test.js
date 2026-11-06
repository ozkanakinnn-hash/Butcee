'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { parseChart, buildMarketItems, updateMarket } = require('../src/market');

const chart = (price, prev) => ({ chart: { result: [{ meta: { regularMarketPrice: price, chartPreviousClose: prev, regularMarketTime: 1 } }] } });

test('Yahoo chart yanıtı ayrıştırılır; fiyat yoksa hata', () => {
  assert.deepEqual(parseChart(chart(42.5, 42)), { price: 42.5, previousClose: 42, time: 1 });
  assert.throws(() => parseChart({ chart: { result: [] } }, 'X'), /X: fiyat yok/);
});

test('liste: gram altın hesaplanır, değişim yüzdesi doğru, sıralama sabit', () => {
  const items = buildMarketItems({
    usdtry: parseChart(chart(42.0, 41.58)),
    eurtry: parseChart(chart(48.5, 48.5)),
    gold_oz: parseChart(chart(4000, 3960)),
    bist100: parseChart(chart(10500.456, 10400)),
    brent: parseChart(chart(65.2, 66)),
  });
  assert.deepEqual(items.map((i) => i.id), ['usdtry', 'eurtry', 'gram_gold', 'gold_oz', 'bist100', 'brent']);
  const gram = items.find((i) => i.id === 'gram_gold');
  assert.equal(gram.value, Number(((4000 * 42) / 31.1034768).toFixed(2)));
  assert.equal(items[0].change_pct, 1.01);
  assert.equal(items[1].change_pct, 0);
  assert.equal(items.find((i) => i.id === 'brent').change_pct, -1.21);
});

test('bir sembol başarısızsa diğerleri yine yazılır', async () => {
  const writes = [];
  const db = { doc: () => ({ set: async (v) => writes.push(v) }) };
  const fetchImpl = async (url) => {
    if (String(url).includes('BZ%3DF')) return new Response('hata', { status: 429 });
    return new Response(JSON.stringify(chart(10, 9)));
  };
  const r = await updateMarket(db, { fetchImpl });
  assert.equal(r.errors.length, 1);
  assert.match(r.errors[0], /BZ=F: HTTP 429/);
  assert.equal(writes[0].items.length, 5);
  assert.equal(writes[0].source, 'Yahoo Finance');
});
