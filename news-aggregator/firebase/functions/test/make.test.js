'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { pickStories, bulletinDue, xText, bulletinPayload, runMake, makeSamples, DEFAULTS } = require('../src/make');

// 14:00 İstanbul (sessiz saat değil)
const NOW = Date.parse('2026-11-06T11:00:00Z');
const story = (id, title, n, extra = {}) => ({
  id, title, url: `https://ornek.com/${id}`, source: 'AA', srcs: Array.from({ length: n }, (_, i) => `Y${i}`),
  last_ms: NOW - 10 * 60_000, cat: 'gundem', br: false, ...extra,
});
const config = { ...DEFAULTS, enabled: true, x_enabled: true, x_webhook: 'https://hook.eu1.make.com/abc', dry_run: false };

test('gündem: en az 5 kaynaklı haber seçilir, 4 kaynaklı seçilmez', () => {
  const r = pickStories({
    stories: [story('a', 'Meclis bütçe görüşmelerine başladı', 4), story('b', 'Merkez Bankası faiz kararını açıkladı', 6)],
    config, now: NOW,
  });
  assert.deepEqual(r.picks.map((p) => [p.tur, p.story.id]), [['gundem', 'b']]);
  const none = pickStories({ stories: [story('a', 'Meclis bütçe görüşmelerine başladı', 4)], config, now: NOW });
  assert.equal(none.picks.length, 0);
  assert.equal(none.reasons.gundem, 'aday yok');
});

test('son dakika tek kaynakla gider; aynı turda ayrı bir gündem haberi de gidebilir', () => {
  const r = pickStories({
    stories: [
      story('s', 'Ankara’da patlama sesi duyuldu', 1, { br: true }),
      story('g', 'Merkez Bankası faiz kararını açıkladı', 5),
    ],
    config, now: NOW,
  });
  assert.deepEqual(r.picks.map((p) => p.tur), ['son_dakika', 'gundem']);
  assert.equal(r.nextState.count, 2);
});

test('paylaşılan hikâye başka kimlikle tekrar gelse de gitmez; aralık ve günlük sınır uygulanır', () => {
  const first = pickStories({ stories: [story('g1', 'Merkez Bankası faiz kararını açıkladı', 5)], config, now: NOW });
  assert.equal(first.picks.length, 1);
  const again = pickStories({
    stories: [story('g2', 'Merkez Bankası faiz kararını açıkladı: yüzde 40', 7)],
    state: { ...first.nextState, last_at: 0 }, config, now: NOW + 60 * 60_000,
  });
  assert.equal(again.picks.length, 0, 'aynı olay ikinci kez paylaşılmaz');

  const gap = pickStories({ stories: [story('x', 'Meclis bütçe görüşmelerine başladı', 6)], state: first.nextState, config, now: NOW + 5 * 60_000 });
  assert.equal(gap.reasons.gundem, 'aralık');

  const cap = pickStories({
    stories: [story('y', 'Meclis bütçe görüşmelerine başladı', 6)],
    state: { day: '2026-11-06', count: 12 }, config, now: NOW,
  });
  assert.equal(cap.reasons.gundem, 'günlük sınır');
});

test('ayar: gündem eşiği ve türler uzaktan değiştirilebilir; tıklama tuzağı elenir', () => {
  const s = [story('a', 'Meclis bütçe görüşmelerine başladı', 3)];
  assert.equal(pickStories({ stories: s, config: { ...config, gundem_min_sources: 3 }, now: NOW }).picks.length, 1);
  assert.equal(pickStories({ stories: s, config: { ...config, gundem_min_sources: 3, send_gundem: false }, now: NOW }).picks.length, 0);
  const bait = [story('b', 'Şok eden olay! Bakın ne oldu', 8)];
  assert.equal(pickStories({ stories: bait, config, now: NOW }).picks.length, 0);
});

test('X metni 280 karakteri aşmaz, son dakika öneki taşır', () => {
  const t = xText(story('a', 'A'.repeat(400), 6), 'son_dakika');
  assert.ok(t.startsWith('🔴 SON DAKİKA: '));
  assert.ok(t.length - 'https://ornek.com/a'.length + 23 <= 280);
});

test('bülten: yalnızca bildirimi gönderilmiş, taze ve daha önce iletilmemiş bülten gider', () => {
  const b = { id: '2026-11-06-am', notified: true, created_ms: NOW - 30 * 60_000, title: 'Sabah gündemi', ai_summary: 'Özet.', items: [] };
  assert.equal(bulletinDue({ briefing: b, state: {}, config, now: NOW }), true);
  assert.equal(bulletinDue({ briefing: b, state: { bulletin_last: b.id }, config, now: NOW }), false);
  assert.equal(bulletinDue({ briefing: { ...b, notified: false }, state: {}, config, now: NOW }), false);
  assert.equal(bulletinDue({ briefing: { ...b, created_ms: NOW - 5 * 3_600_000 }, state: {}, config, now: NOW }), false);
  const p = bulletinPayload({ ...b, items: [{ title: 'M1', url: 'https://ornek.com/1', source: 'AA', source_count: 5 }] }, NOW);
  assert.equal(p.tur, 'bulten');
  assert.equal(p.ozet, 'Özet.');
  assert.deepEqual(p.maddeler, [{ baslik: 'M1', link: 'https://ornek.com/1', kaynak: 'AA', kaynak_sayisi: 5 }]);
  assert.match(p.metin, /^Sabah gündemi\n\nÖzet\./);
});

/** Bellek içi Firestore taklidi. */
function fakeDb(docs) {
  const log = [];
  return {
    log,
    docs,
    doc: (path) => ({
      get: async () => ({ exists: docs[path] !== undefined, data: () => docs[path], get: (k) => (docs[path] || {})[k] }),
      set: async (v, opt) => { docs[path] = opt && opt.merge ? { ...(docs[path] || {}), ...v } : v; },
    }),
    collection: () => ({ add: async (v) => log.push(v) }),
  };
}

test('tur: bülten + son dakika + gündem Make webhook’una gider; durum ve kayıt yazılır', async () => {
  const docs = {
    'app_meta/social': config,
    'notify_state/news': { stories: [story('s', 'Ankara’da patlama sesi duyuldu', 1, { br: true }), story('g', 'Merkez Bankası faiz kararını açıkladı', 5)] },
    'briefings/latest': { id: '2026-11-06-am', notified: true, created_ms: NOW - 60_000, title: 'Sabah gündemi', ai_summary: 'Özet.', items: [] },
    'articles/s': {}, 'articles/g': {},
  };
  const db = fakeDb(docs);
  const posted = [];
  const fetchImpl = async (url, opts) => { posted.push(JSON.parse(opts.body)); return new Response('Accepted'); };
  const r = await runMake(db, { now: NOW, fetchImpl, summarize: async (s) => `AI: ${s.id}` });
  assert.equal(r.status, 'gönderildi');
  assert.deepEqual(posted.map((p) => p.tur), ['bulten', 'son_dakika', 'gundem']);
  assert.equal(posted[1].ozet, 'AI: s');
  assert.equal(docs['notify_state/make'].bulletin_last, '2026-11-06-am');
  assert.match(docs['app_meta/social'].make_status, /gönderilen: bulten, son_dakika, gundem/);
  assert.equal(db.log.length, 3);

  // İkinci tur: aynı şeyler tekrar gitmez
  posted.length = 0;
  await runMake(db, { now: NOW + 60 * 60_000, fetchImpl, summarize: async () => '' });
  assert.equal(posted.length, 0);
});

test('tur: deneme modunda gönderilmez, webhook hatası kaydedilir; deneme örnekleri her türden', async () => {
  const base = {
    'notify_state/news': { stories: [story('g', 'Merkez Bankası faiz kararını açıkladı', 5)] }, 'articles/g': {},
  };
  const dry = fakeDb({ ...base, 'app_meta/social': { ...config, dry_run: true } });
  let calls = 0;
  const r = await runMake(dry, { now: NOW, fetchImpl: async () => { calls += 1; return new Response(''); }, summarize: async () => '' });
  assert.equal(r.status, 'deneme');
  assert.equal(calls, 0);
  assert.equal(dry.log[0].dry_run, true);

  const failing = fakeDb({ ...base, 'app_meta/social': config });
  const r2 = await runMake(failing, { now: NOW, fetchImpl: async () => new Response('x', { status: 410 }), summarize: async () => '' });
  assert.equal(r2.status, 'hata');
  assert.match(failing.docs['app_meta/social'].make_status, /webhook HTTP 410/);

  assert.deepEqual(makeSamples(NOW).map((p) => p.tur), ['son_dakika', 'gundem', 'bulten']);
  assert.ok(makeSamples(NOW).every((p) => p.deneme === true));
});

test('akışlar açıkken bülten Make\'e ayrıca gitmez (akis_bulten_birlikte ile açılabilir)', async () => {
  const docs = {
    'app_meta/social': { ...config, send_akis: true },
    'notify_state/news': { stories: [] },
    'notify_state/make': {},
    'briefings/latest': { id: '2026-11-06-am', notified: true, created_ms: NOW - 10 * 60_000, title: 'Sabah', ai_summary: 'Ö', items: [] },
  };
  const mk = () => {
    const d = JSON.parse(JSON.stringify(docs));
    return {
      doc: (p) => ({ get: async () => ({ exists: p in d, data: () => d[p] }), set: async (v) => { d[p] = { ...(d[p] || {}), ...v }; } }),
      collection: () => ({ add: async () => {} }),
    };
  };
  let sent = 0;
  const fetchImpl = async () => { sent += 1; return { ok: true }; };
  await runMake(mk(), { now: NOW, fetchImpl, summarize: async () => '' });
  assert.equal(sent, 0);
  docs['app_meta/social'].akis_bulten_birlikte = true;
  await runMake(mk(), { now: NOW, fetchImpl, summarize: async () => '' });
  assert.equal(sent, 1);
});
