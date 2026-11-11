'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { parseTruncgil, parseTcmb, buildMarketItems, nextFxState, updateMarket, TROY_OUNCE_GRAMS, TCMB_URL } = require('../src/market');

// Truncgil gerçek yanıtının kısaltılmış hali (2026-11-06); sonunda bozuk bir kalem var.
const TRUNCGIL =
  '{"Update_Date":"2026-11-06 13:34:01",' +
  '"USD":{"Buying":50.1544,"Type":"Currency","Selling":50.1606,"Change":0.22},' +
  '"GRA":{"Selling":6563.24,"Type":"Gold","Name":"GRAMALTIN","Change":-0.5,"Buying":6562.48},' +
  '"XU100":{"Selling":11307.68,"Type":"Gold","Name":"XU100","Change":-0.23},' +
  '"ONS":{"Buying":0,"Type":"Gold","Name":"ONS","Selling":0,"Change":-0.71},' +
  '"PAL":{"Buying":1646.21,"Type":"Palladium","Name":"GRAMPALADYUM","Selling":1653.3 "bozuk":}}';

// TCMB today.xml biçimi (kısaltılmış).
const tcmbXml = (date, usd, eur) => `<?xml version="1.0" encoding="UTF-8"?>
<Tarih_Date Tarih="${date}" Date="x" Bulten_No="2026/210">
  <Currency CrossOrder="0" Kod="USD" CurrencyCode="USD"><Unit>1</Unit><Isim>ABD DOLARI</Isim>
    <ForexBuying>${usd - 0.1}</ForexBuying><ForexSelling>${usd}</ForexSelling><BanknoteSelling>99</BanknoteSelling></Currency>
  <Currency CrossOrder="9" Kod="EUR" CurrencyCode="EUR"><Unit>1</Unit><Isim>EURO</Isim>
    <ForexBuying>${eur - 0.1}</ForexBuying><ForexSelling>${eur}</ForexSelling></Currency>
  <Currency CrossOrder="12" Kod="JPY" CurrencyCode="JPY"><Unit>100</Unit><Isim>JAPON YENİ</Isim>
    <ForexBuying>27</ForexBuying><ForexSelling>28</ForexSelling></Currency>
</Tarih_Date>`;

test('Truncgil: bozuk JSON içinden kalemler tek tek ayıklanır', () => {
  const { items, updateDate } = parseTruncgil(TRUNCGIL);
  assert.equal(updateDate, '2026-11-06 13:34:01');
  assert.equal(items.GRA.Selling, 6563.24);
  assert.equal(items.PAL, undefined, 'bozuk kalem atlanır');
});

test('TCMB: döviz satış kuru birim başına okunur', () => {
  const r = parseTcmb(tcmbXml('11.11.2026', 42.5, 49.25));
  assert.equal(r.date, '11.11.2026');
  assert.equal(r.rates.USD, 42.5);
  assert.equal(r.rates.EUR, 49.25);
  assert.equal(r.rates.JPY, 0.28);
});

test('liste yalnızca dolar, euro, gram ve ons altın içerir (BIST 100 ve Brent yok)', () => {
  const fx = { usd: 42.5, eur: 49.25, usd_prev: 42, eur_prev: 50 };
  const items = buildMarketItems(fx, parseTruncgil(TRUNCGIL).items);
  assert.deepEqual(items.map((i) => i.id), ['usdtry', 'eurtry', 'gram_gold', 'gold_oz']);
  assert.equal(items[0].change_pct, 1.19);
  assert.equal(items[1].change_pct, -1.5);
  assert.equal(items[3].value, Number(((6563.24 * TROY_OUNCE_GRAMS) / 42.5).toFixed(2)));
});

test('kur durumu: yeni bültende önceki kaydırılır, aynı gün güncellenir, hata durumunda korunur', () => {
  const s1 = nextFxState({}, parseTcmb(tcmbXml('10.11.2026', 42, 50)));
  assert.deepEqual(s1, { date: '10.11.2026', usd: 42, eur: 50, usd_prev: null, eur_prev: null });
  const s2 = nextFxState(s1, parseTcmb(tcmbXml('11.11.2026', 42.5, 49)));
  assert.deepEqual(s2, { date: '11.11.2026', usd: 42.5, eur: 49, usd_prev: 42, eur_prev: 50 });
  assert.deepEqual(nextFxState(s2, parseTcmb(tcmbXml('11.11.2026', 42.5, 49))), s2);
  assert.deepEqual(nextFxState(s2, null), s2);
});

function fakeDb(initial) {
  const writes = [];
  return {
    writes,
    doc: () => ({ get: async () => ({ data: () => initial }), set: async (v) => writes.push(v) }),
  };
}

test('güncelleme: iki kaynak birleşir; TCMB çökerse son kur, Truncgil çökerse son altın korunur', async () => {
  const db = fakeDb(undefined);
  const ok = async (url) => new Response(url === TCMB_URL ? tcmbXml('11.11.2026', 42.5, 49) : TRUNCGIL);
  const r = await updateMarket(db, { fetchImpl: ok });
  assert.deepEqual(r, { count: 4, missing: [] });
  const doc = db.writes[0];
  assert.match(doc.source, /TCMB/);
  assert.equal(doc.source_updated, '11.11.2026');

  const db2 = fakeDb(doc);
  const r2 = await updateMarket(db2, {
    fetchImpl: async (url) => (url === TCMB_URL ? new Response('x', { status: 503 }) : new Response('{}')),
  });
  assert.deepEqual(r2.errors, ['TCMB: HTTP 503']);
  assert.deepEqual(db2.writes[0].items.map((i) => i.id), ['usdtry', 'eurtry', 'gram_gold', 'gold_oz']);
  assert.equal(db2.writes[0].items[0].value, 42.5);
});
