'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const eq = require('../src/earthquake');
const { toWire } = require('../src/notify');

const NOW = Date.parse('2026-11-21T12:00:00Z');

test('AFAD biçimi: büyüklük metin, tarih saat dilimsiz (UTC), il/ilçe', () => {
  const list = eq.parseAfad([
    { eventID: '123', magnitude: '4.8', latitude: '37.5', longitude: '37.1', depth: '7.2', date: '2026-11-21T11:55:00', province: 'Kahramanmaraş', district: 'Pazarcık', country: 'Türkiye' },
    { eventID: '124', magnitude: 'x', latitude: '37', longitude: '37', date: '2026-11-21T11:50:00' },
  ], NOW);
  assert.equal(list.length, 1);
  assert.equal(list[0].id, 'afad-123');
  assert.equal(list[0].mag, 4.8);
  assert.equal(list[0].time_ms, Date.parse('2026-11-21T11:55:00Z'));
  assert.equal(list[0].place, 'Kahramanmaraş / Pazarcık');
});

test('yerel saatle yazılmış (gelecekte görünen) tarih 3 saat geri alınır', () => {
  assert.equal(eq.parseTime('2026-11-21T14:55:00', NOW), Date.parse('2026-11-21T11:55:00Z'));
  assert.equal(eq.parseTime('2026-11-21T11:55:00Z', NOW), Date.parse('2026-11-21T11:55:00Z'));
});

test('EMSC biçimi', () => {
  const list = eq.parseEmsc({ features: [{ id: 'x', properties: { unid: 'u1', mag: 5.1, lat: 38.2, lon: 27.1, depth: 10, time: '2026-11-21T11:58:00.0Z', flynn_region: 'WESTERN TURKEY' } }] }, NOW);
  assert.equal(list[0].id, 'emsc-u1');
  assert.equal(list[0].place, 'WESTERN TURKEY');
});

test('uyarı seçimi: eşik, Türkiye kutusu, 20 dk, bir kez', () => {
  const q = (id, mag, ageMin, lat = 38, lon = 30) => ({ id, mag, lat, lon, depth: 5, time_ms: NOW - ageMin * 60_000, place: 'X', source: 'AFAD' });
  const quakes = [q('a', 4.6, 3), q('b', 4.2, 2), q('c', 5.5, 40), q('d', 6.0, 1, 50, 50), q('e', 4.9, 5)];
  const picks = eq.pickAlerts(quakes, ['e'], NOW);
  assert.deepEqual(picks.map((x) => x.id), ['a']);
});

test('bildirim: M≥5 son dakika abonelerine de gider; Android veri + iOS sesli uyarı', () => {
  const big = eq.buildQuakeMessage({ id: 'afad-1', mag: 5.3, depth: 9.6, time_ms: NOW, place: 'Malatya / Doğanşehir', source: 'AFAD' });
  assert.equal(big.condition, "'deprem' in topics || 'news_son-dakika' in topics");
  assert.match(big.notification.title, /5\.3 büyüklüğünde/);
  assert.match(big.notification.body, /Malatya \/ Doğanşehir · 10 km derinlik · 15:00\nKaynak: AFAD/);
  const w = toWire(big);
  assert.equal(w.data.n_channel, 'breaking_news');
  assert.equal(w.apns.payload.aps.sound, 'default');
  const small = eq.buildQuakeMessage({ id: 'afad-2', mag: 4.6, depth: NaN, time_ms: NOW, place: '', source: 'EMSC' });
  assert.equal(small.condition, "'deprem' in topics");
});

test('tur: AFAD hata verirse EMSC kullanılır; liste yazılır; aynı deprem ikinci turda gönderilmez', async () => {
  const store = {};
  const db = { doc: (p) => ({ get: async () => ({ data: () => store[p] }), set: async (v) => { store[p] = v; } }) };
  const sentMsgs = [];
  const messaging = { sendEach: async (m) => { sentMsgs.push(...m); return { successCount: m.length }; } };
  const fetchImpl = async (url) => {
    if (String(url).includes('afad')) return new Response('x', { status: 503 });
    return Response.json({ features: [{ properties: { unid: 'u9', mag: 4.7, lat: 39, lon: 28, depth: 8, time: '2026-11-21T11:57:00Z', flynn_region: 'WESTERN TURKEY' } }] });
  };
  const r = await eq.runEarthquake(db, messaging, { fetchImpl, now: NOW });
  assert.equal(r.provider, 'EMSC');
  assert.equal(r.sent, 1);
  assert.equal(store['quakes/latest'].items.length, 1);
  const r2 = await eq.runEarthquake(db, messaging, { fetchImpl, now: NOW + 120_000 });
  assert.equal(r2.sent, 0);
  assert.equal(sentMsgs.length, 1);
});
