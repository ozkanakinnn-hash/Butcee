'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const b = require('../src/bulletin');

test('slotForTime İstanbul saatine göre en yakın akışı seçer', () => {
  assert.equal(b.slotForTime(new Date('2026-11-27T04:31:00Z')), 'sabah'); // 07:31
  assert.equal(b.slotForTime(new Date('2026-11-27T09:40:00Z')), 'ogle'); // 12:40
  assert.equal(b.slotForTime(new Date('2026-11-27T15:30:00Z')), 'aksam'); // 18:30
  assert.equal(b.slotForTime(new Date('2026-11-27T20:35:00Z')), 'gece'); // 23:35
});

test('slotWindow aralıkları doğru kurar', () => {
  const now = new Date('2026-11-27T04:35:00Z'); // 07:35
  const w = b.slotWindow('sabah', now);
  assert.equal(w.start.toISOString(), '2026-11-26T20:30:00.000Z'); // dün 23:30
  assert.equal(w.end.toISOString(), '2026-11-27T04:30:00.000Z');
  assert.match(w.dateLabel, /27 Kasım 2026/);
  // Gece yarısından sonra çalıştırılan gece akışı önceki güne aittir.
  const g = b.slotWindow('gece', new Date('2026-11-27T22:10:00Z')); // 28 Kasım 01:10
  assert.match(g.dateLabel, /27 Kasım 2026/);
  assert.throws(() => b.slotWindow('ikindi'), /Bilinmeyen/);
});

test('groupEvents aynı olayı farklı kaynaklardan tek olayda toplar', () => {
  const ev = b.groupEvents([
    { title: 'Gürcistan\'ın Ankara Büyükelçisi Dışişleri Bakanlığı\'na çağrıldı', source: 'A', summary: '', breaking: false, published_at: '2' },
    { title: 'Gürcistan Büyükelçisi Dışişleri Bakanlığına çağrıldı', source: 'B', summary: 'Ayrıntı', breaking: true, published_at: '1' },
    { title: '2027 bütçesi Plan ve Bütçe Komisyonu\'nda kabul edildi', source: 'A', summary: '', breaking: false, published_at: '1' },
  ]);
  assert.equal(ev.length, 2);
  assert.deepEqual(ev[0].sources, ['A', 'B']);
  assert.equal(ev[0].count, 2);
  assert.equal(ev[0].breaking, true);
  assert.equal(ev[0].summary, 'Ayrıntı');
  assert.equal(ev[0].tokens, undefined);
});

test('buildPayload gece yoğun günde Günün Bülteni moduna geçer', () => {
  const window = { start: new Date(0), end: new Date(1000), dateLabel: '27 Kasım 2026' };
  const slotArticles = [{ title: 'Deneme haberi başlığı', source: 'A', summary: '', breaking: false, published_at: '1' }];
  const normal = b.buildPayload({ slot: 'gece', window, slotArticles, dayEventCount: 10 });
  assert.equal(normal.daily_recap, false);
  assert.equal(normal.slot_label, 'Gece Akışı');
  const dense = b.buildPayload({ slot: 'gece', window, slotArticles, dayEventCount: b.DENSE_DAY_THRESHOLD });
  assert.equal(dense.daily_recap, true);
  assert.equal(dense.slot_label, 'Günün Bülteni');
  assert.equal(b.buildPayload({ slot: 'ogle', window, slotArticles, dayEventCount: 99 }).daily_recap, false);
});

test('toPlainArticle Firestore alanlarını sadeleştirir', () => {
  const a = b.toPlainArticle({
    title: 'T', summary: 'S', source_name: 'Kaynak', category_id: { id: 'ekonomi' },
    original_url: 'https://x.com/a', published_at: { toDate: () => new Date('2026-11-27T10:00:00Z') }, breaking_at: {},
  });
  assert.deepEqual(a, {
    title: 'T', summary: 'S', source: 'Kaynak', category: 'ekonomi', url: 'https://x.com/a',
    published_at: '2026-11-27T10:00:00.000Z', breaking: true,
  });
});

test('sendToMake hata durumunda istisna atar', async () => {
  const ok = await b.sendToMake('u', { a: 1 }, async () => ({ ok: true, status: 200, text: async () => 'Accepted' }));
  assert.equal(ok, 'Accepted');
  await assert.rejects(
    b.sendToMake('u', {}, async () => ({ ok: false, status: 410, text: async () => 'gone' })),
    /HTTP 410/
  );
});
