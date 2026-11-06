'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { parseTruncgil, buildMarketItems, updateMarket, TROY_OUNCE_GRAMS } = require('../src/market');

// GitHub sunucusundan alınan gerçek yanıtın kısaltılmış hali (2026-11-06); sonunda bozuk bir kalem var.
const SAMPLE =
  '{"Update_Date":"2026-11-06 13:34:01",' +
  '"USD":{"Buying":50.1544,"Type":"Currency","Selling":50.1606,"Change":0.22},' +
  '"EUR":{"Buying":56.0812,"Type":"Currency","Selling":56.0938,"Change":-0.33},' +
  '"GRA":{"Selling":6563.24,"Type":"Gold","Name":"GRAMALTIN","Change":-0.5,"Buying":6562.48},' +
  '"XU100":{"Selling":11307.68,"Type":"Gold","Name":"XU100","Change":-0.23},' +
  '"BRENT":{"Selling":0,"Type":"Gold","Name":"BRENT","Change":-0.37},' +
  '"ONS":{"Buying":0,"Type":"Gold","Name":"ONS","Selling":0,"Change":-0.71},' +
  '"PAL":{"Buying":1646.21,"Type":"Palladium","Name":"GRAMPALADYUM","Selling":1653.3 "bozuk":}}';

test('bozuk JSON içinden kalemler tek tek ayıklanır', () => {
  const { items, updateDate } = parseTruncgil(SAMPLE);
  assert.equal(updateDate, '2026-11-06 13:34:01');
  assert.equal(items.USD.Selling, 50.1606);
  assert.equal(items.XU100.Change, -0.23);
  assert.equal(items.PAL, undefined, 'bozuk kalem atlanır');
});

test('liste: ons fiyatı gram altın ve dolardan hesaplanır, fiyatı olmayan Brent gösterilmez', () => {
  const items = buildMarketItems(parseTruncgil(SAMPLE).items);
  assert.deepEqual(items.map((i) => i.id), ['usdtry', 'eurtry', 'gram_gold', 'gold_oz', 'bist100']);
  const ons = items.find((i) => i.id === 'gold_oz');
  assert.equal(ons.value, Number(((6563.24 * TROY_OUNCE_GRAMS) / 50.1606).toFixed(2)));
  assert.equal(ons.change_pct, -0.71);
  assert.equal(items.find((i) => i.id === 'usdtry').change_pct, 0.22);
  assert.equal(items.find((i) => i.id === 'bist100').value, 11307.68);
});

test('Brent fiyatı gelirse eklenir', () => {
  const items = buildMarketItems({ BRENT: { Selling: 64.3, Change: 1.1 } });
  assert.deepEqual(items, [{ id: 'brent', label: 'Brent Petrol', unit: '$', value: 64.3, change_pct: 1.1, decimals: 2 }]);
});

test('güncelleme: belge yazılır, eksikler raporlanır; HTTP hatasında yazılmaz', async () => {
  const writes = [];
  const db = { doc: () => ({ set: async (v) => writes.push(v) }) };
  const r = await updateMarket(db, { fetchImpl: async () => new Response(SAMPLE) });
  assert.equal(r.count, 5);
  assert.deepEqual(r.missing, ['brent']);
  assert.equal(writes[0].source, 'Truncgil Finans');
  assert.equal(writes[0].source_updated, '2026-11-06 13:34:01');

  const r2 = await updateMarket(db, { fetchImpl: async () => new Response('x', { status: 503 }) });
  assert.deepEqual(r2, { count: 0, errors: ['Truncgil: HTTP 503'] });
  assert.equal(writes.length, 1);
});
