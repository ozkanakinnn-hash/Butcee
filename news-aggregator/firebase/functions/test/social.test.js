'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { pickStory, telegramText, xText, runSocial, DEFAULTS } = require('../src/social');

const NOW = Date.parse('2026-11-17T09:00:00Z'); // İstanbul 12:00
const story = (id, over = {}) => ({
  id, title: `Merkez Bankası faizi indirdi ${id}`, url: `https://yayinci.example/${id}`, source: 'AA',
  srcs: ['AA', 'NTV', 'Habertürk'], first_ms: NOW - 1_800_000, last_ms: NOW - 600_000, cat: 'ekonomi', br: false, ...over,
});
const cfg = { ...DEFAULTS, enabled: true, daily_cap: 3, min_gap_min: 45 };

test('seçim: 3+ kaynaklı, yeni, tıklama tuzağı olmayan haber; en çok kaynaklı önce', () => {
  const stories = [
    story('a', { srcs: ['AA', 'NTV'] }), // 2 kaynak → yetmez
    story('b'),
    story('c', { srcs: ['1', '2', '3', '4', '5'] }),
    story('d', { title: 'Ünlü oyuncu tatil pozuyla şoke etti' }),
    story('e', { last_ms: NOW - 5 * 3_600_000 }), // eski
    story('f', { cat: 'spor' }), // spor: son dakika kaynağından değilse paylaşılmaz
  ];
  const r = pickStory({ stories, config: cfg, now: NOW });
  assert.equal(r.story.id, 'c');
  assert.deepEqual(r.nextState.posted, ['c']);
});

test('seçim: aynı haber ikinci kez paylaşılmaz; günlük sınır ve aralık', () => {
  const stories = [story('b'), story('c', { srcs: ['1', '2', '3', '4'] })];
  const first = pickStory({ stories, config: cfg, now: NOW });
  assert.equal(first.story.id, 'c');
  assert.equal(pickStory({ stories, state: first.nextState, config: cfg, now: NOW + 10 * 60_000 }).reason, 'aralık');
  const later = pickStory({ stories, state: first.nextState, config: cfg, now: NOW + 50 * 60_000 });
  assert.equal(later.story.id, 'b', 'paylaşılan atlanır');
  const third = pickStory({ stories, state: later.nextState, config: cfg, now: NOW + 100 * 60_000 });
  assert.equal(third.reason, 'aday yok');
  const capped = pickStory({ stories, state: { day: '2026-11-17', count: 3, last_at: 0, posted: [] }, config: cfg, now: NOW });
  assert.equal(capped.reason, 'günlük sınır');
  // Ertesi gün sayaç sıfırlanır
  assert.ok(pickStory({ stories, state: { day: '2026-11-16', count: 3, last_at: 0, posted: [] }, config: cfg, now: NOW }).story);
});

test('seçim: gece sessiz saatte paylaşılmaz; yalnızca son dakika kaynağından kritik haber paylaşılır', () => {
  const night = Date.parse('2026-11-17T23:30:00Z'); // İstanbul 02:30
  const normal = story('n', { last_ms: night - 60_000 });
  assert.equal(pickStory({ stories: [normal], config: cfg, now: night }).reason, 'aday yok');
  const quake = story('q', { title: "Malatya'da 6,1 büyüklüğünde deprem", br: true, last_ms: night - 60_000 });
  assert.equal(pickStory({ stories: [quake], config: cfg, now: night }).story.id, 'q');
});

test('metinler: Telegram HTML kaçışlı ve bağlantılı; X metni 280 sınırında (bağlantı 23 sayılır)', () => {
  const s = story('x', { title: 'A & B <test> haberi' });
  const t = telegramText(s);
  assert.match(t, /^<b>A &amp; B &lt;test&gt; haberi<\/b>/);
  assert.match(t, /Kaynak: AA · 3 kaynakta/);
  assert.ok(t.endsWith('https://yayinci.example/x'));

  const long = story('y', { title: 'Çok uzun başlık '.repeat(30) });
  const x = xText(long);
  const counted = x.replace(long.url, 'u'.repeat(23)).length;
  assert.ok(counted <= 280, `X karakter sayısı ${counted}`);
  assert.ok(x.includes('…') && x.endsWith(long.url));
  assert.equal(xText(story('z')).split('\n').pop(), 'https://yayinci.example/z', 'kısa başlık kesilmez');
});

// Sahte Firestore (yalnızca kullanılan yüzey)
function fakeDb(docs) {
  const written = [];
  const logs = [];
  const ref = (path) => ({
    get: async () => ({ exists: path in docs, data: () => docs[path], get: (k) => (docs[path] || {})[k] }),
    set: async (v) => { docs[path] = v; written.push([path, v]); },
  });
  return { doc: ref, collection: () => ({ add: async (v) => logs.push(v) }), written, logs };
}
const fakeFetch = (calls, { fail } = {}) => async (url, opts) => {
  calls.push({ url, body: JSON.parse(opts.body) });
  return fail
    ? new Response(JSON.stringify({ ok: false, description: 'chat not found' }), { status: 400 })
    : new Response(JSON.stringify({ ok: true }));
};
const world = (config) => ({
  'app_meta/social': config,
  'notify_state/news': { stories: [story('s1')] },
  'articles/s1': { title: 'x' },
});

test('tur: kapalıyken hiçbir şey yapmaz; deneme modunda yalnızca kayıt tutar', async () => {
  const calls = [];
  assert.equal((await runSocial(fakeDb(world({ enabled: false })), { token: 't', fetchImpl: fakeFetch(calls), now: NOW })).status, 'kapalı');
  const db = fakeDb(world({ enabled: true, dry_run: true }));
  const r = await runSocial(db, { fetchImpl: fakeFetch(calls), now: NOW });
  assert.equal(r.status, 'deneme');
  assert.equal(calls.length, 0, 'deneme modunda Telegram çağrılmaz');
  assert.equal(db.logs.length, 1);
  assert.match(db.logs[0].telegram, /<b>Merkez Bankası/);
  assert.match(db.logs[0].x, /Kaynak: AA/);
});

test('tur: gerçek gönderim — kanala haber, özel sohbete X taslağı; durum kaydedilir', async () => {
  const calls = [];
  const db = fakeDb(world({ enabled: true, dry_run: false, channel: '@haberakisi', owner_chat_id: '42' }));
  const r = await runSocial(db, { token: 'TOKEN', fetchImpl: fakeFetch(calls), now: NOW });
  assert.equal(r.status, 'gönderildi');
  assert.equal(calls.length, 2);
  assert.equal(calls[0].url, 'https://api.telegram.org/botTOKEN/sendMessage');
  assert.equal(calls[0].body.chat_id, '@haberakisi');
  assert.equal(calls[0].body.parse_mode, 'HTML');
  assert.equal(calls[1].body.chat_id, '42');
  assert.match(calls[1].body.text, /^X için hazır metin:/);
  assert.ok(!calls[1].body.parse_mode, 'X taslağı düz metin');
  assert.deepEqual(db.written.find(([p]) => p === 'notify_state/social')[1].posted, ['s1']);
});

test('tur: kanal hatası sayaç ilerletmez; jeton/kanal eksikse göndermez; silinmiş haber paylaşılmaz', async () => {
  const calls = [];
  const bad = fakeDb(world({ enabled: true, dry_run: false, channel: '@x' }));
  const r = await runSocial(bad, { token: 'T', fetchImpl: fakeFetch(calls, { fail: true }), now: NOW });
  assert.equal(r.status, 'hata');
  assert.match(r.errors[0], /chat not found/);
  assert.ok(!bad.written.some(([p]) => p === 'notify_state/social'), 'başarısız gönderim kaydedilmez → yeniden denenir');

  assert.equal((await runSocial(fakeDb(world({ enabled: true, dry_run: false, channel: '@x' })), { token: '', fetchImpl: fakeFetch(calls), now: NOW })).status, 'ayar eksik');

  const gone = world({ enabled: true, dry_run: false, channel: '@x' });
  delete gone['articles/s1']; // telif talebiyle silinmiş
  const calls2 = [];
  assert.equal((await runSocial(fakeDb(gone), { token: 'T', fetchImpl: fakeFetch(calls2), now: NOW })).status, 'haber kaldırılmış');
  assert.equal(calls2.length, 0);
});

test('telif önlemleri: önizleme varsayılan kapalı (açılabilir); izin listesi yalnızca listedeki yayıncıları paylaşır', async () => {
  const calls = [];
  const db = fakeDb(world({ enabled: true, dry_run: false, channel: '@x' }));
  await runSocial(db, { token: 'T', fetchImpl: fakeFetch(calls), now: NOW });
  assert.equal(calls[0].body.disable_web_page_preview, true, 'varsayılan: yayıncı görseli gösterilmez');
  const calls2 = [];
  await runSocial(fakeDb(world({ enabled: true, dry_run: false, channel: '@x', link_preview: true })), { token: 'T', fetchImpl: fakeFetch(calls2), now: NOW });
  assert.equal(calls2[0].body.disable_web_page_preview, false);

  const stories = [story('a', { source: 'Ekonomi Gazetesi' }), story('b', { source: 'Haber Ajansı', srcs: ['1', '2', '3', '4'] })];
  const only = { ...cfg, allowed_sources: ['ekonomi gazetesi'] }; // büyük/küçük harf farkı gözetilmez
  assert.equal(pickStory({ stories, config: only, now: NOW }).story.id, 'a');
  assert.equal(pickStory({ stories: [stories[1]], config: only, now: NOW }).reason, 'aday yok');
  assert.equal(pickStory({ stories, config: cfg, now: NOW }).story.id, 'b', 'liste boşsa tüm kaynaklar');
});
