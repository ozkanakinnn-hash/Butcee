'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { planNotifications, buildNewsMessage, CATEGORY_COOLDOWN_MS, sendNewsNotifications } = require('../src/notify');

const NOW = Date.parse('2026-11-06T12:00:00Z');
const ts = (iso) => ({ toMillis: () => Date.parse(iso) });
const ref = (id) => ({ id });
const art = (id, cat, iso, extra = {}) => ({
  id,
  data: {
    title: `Başlık ${id}`,
    original_url: `https://ornek.com/${id}`,
    source_name: 'Kaynak',
    image_url: `https://cdn.ornek.com/${id}.jpg`,
    has_original_image: true,
    category_id: ref(cat),
    published_at: ts(iso),
    ...extra,
  },
});
const categories = new Map([
  ['saglik', { name: 'Sağlık' }],
  ['spor', { name: 'Spor' }],
  ['son-dakika', { name: 'Son Dakika' }],
]);

test('kategori başına tek özet: en yeni haber + "ve N haber daha"', () => {
  const results = [
    { addedArticles: [art('a', 'saglik', '2026-11-06T10:00:00Z'), art('b', 'saglik', '2026-11-06T11:30:00Z')] },
    { addedArticles: [art('c', 'saglik', '2026-11-06T11:00:00Z'), art('d', 'spor', '2026-11-06T11:59:00Z')] },
  ];
  const { messages, nextState } = planNotifications({ results, categories, state: {}, now: NOW });
  assert.equal(messages.length, 2);
  const saglik = messages.find((m) => m.topic === 'news_saglik');
  assert.equal(saglik.notification.title, 'Sağlık');
  assert.equal(saglik.notification.body, 'Başlık b\nve 2 haber daha');
  assert.equal(saglik.data.url, 'https://ornek.com/b');
  assert.equal(saglik.android.notification.channelId, 'news');
  assert.equal(nextState.last_sent.saglik, NOW);
});

test('2 saat dolmadan aynı kategoriye tekrar bildirim gitmez', () => {
  const results = [{ addedArticles: [art('a', 'saglik', '2026-11-06T11:00:00Z')] }];
  const state = { last_sent: { saglik: NOW - CATEGORY_COOLDOWN_MS + 60_000 } };
  assert.equal(planNotifications({ results, categories, state, now: NOW }).messages.length, 0);
  const later = { last_sent: { saglik: NOW - CATEGORY_COOLDOWN_MS - 1 } };
  assert.equal(planNotifications({ results, categories, state: later, now: NOW }).messages.length, 1);
});

test('Son Dakika hemen gider, aynı haber tekrar gönderilmez, eski haber gönderilmez', () => {
  const sd = art('x', 'gundem', '2026-11-06T11:50:00Z');
  const results = [{ addedArticles: [], breakingArticles: [sd, art('old', 'gundem', '2026-11-06T06:00:00Z')] }];
  const first = planNotifications({ results, categories, state: {}, now: NOW });
  assert.equal(first.messages.length, 1);
  const msg = first.messages[0];
  assert.equal(msg.topic, 'news_son-dakika');
  assert.equal(msg.notification.title, '⚡ Son Dakika');
  assert.equal(msg.android.priority, 'high');
  assert.equal(msg.android.notification.channelId, 'breaking');
  assert.equal(first.nextState.last_breaking_id, 'x');

  const again = planNotifications({ results, categories, state: first.nextState, now: NOW + 20 * 60_000 });
  assert.equal(again.messages.length, 0, 'aynı haber ikinci kez gönderilmemeli');

  const onlyOld = [{ breakingArticles: [art('old', 'gundem', '2026-11-06T06:00:00Z')] }];
  assert.equal(planNotifications({ results: onlyOld, categories, state: {}, now: NOW }).messages.length, 0);
});

test('placeholder görsel bildirime eklenmez, uzun başlık kısaltılır', () => {
  const long = 'Ç'.repeat(300);
  const m = buildNewsMessage({
    categoryId: 'saglik',
    categoryName: 'Sağlık',
    articles: [art('p', 'saglik', '2026-11-06T11:00:00Z', { title: long, has_original_image: false })],
  });
  assert.equal(m.notification.imageUrl, undefined);
  assert.ok(m.notification.body.length <= 160);
});

test('gönderim: durum kaydedilir, FCM hatası çekimi bozmaz', async () => {
  const saved = [];
  const logged = [];
  const db = {
    doc: () => ({ get: async () => ({ data: () => ({}) }), set: async (v) => saved.push(v) }),
    collection: () => ({ doc: (id) => ({ id }) }),
    batch: () => ({ set: (ref, data) => logged.push({ id: ref.id, data }), commit: async () => {} }),
  };
  const results = [{ addedArticles: [art('a', 'saglik', '2026-11-06T11:00:00Z')] }];
  const okMessaging = { sendEach: async (msgs) => ({ successCount: msgs.length, failureCount: 0, responses: msgs.map(() => ({ success: true })) }) };
  const r = await sendNewsNotifications(db, results, categories, { messaging: okMessaging, now: NOW });
  assert.equal(r.sent, 1);
  assert.equal(saved.length, 1);
  assert.equal(logged.length, 1, 'gönderilen bildirim Bildirimler sekmesi için kaydedilir');
  assert.deepEqual(logged[0].data.topics, ['news_saglik']);
  assert.equal(logged[0].data.url, 'https://ornek.com/a');

  const badMessaging = { sendEach: async () => { throw new Error('ağ hatası'); } };
  const r2 = await sendNewsNotifications(db, results, categories, { messaging: badMessaging, now: NOW });
  assert.equal(r2.sent, 0);
  assert.match(r2.error, /ağ hatası/);
});

test('bildirim kaydı: yeniler önce sıralanır, koşullu (iki takım) mesajın konuları çıkarılır', () => {
  const { logEntryFromMessage, logId } = require('../src/notification-log');
  assert.ok(logId(2000, 'a') < logId(1000, 'a'), 'yeni kayıt kimliği alfabetik olarak önce gelmeli');
  const e = logEntryFromMessage({
    condition: "'team_645' in topics || 'team_611' in topics",
    notification: { title: '⚽ GOL!', body: "67' Icardi" },
    data: { type: 'match', fixture_id: '77' },
    android: { notification: { channelId: 'goals' } },
  }, { now: NOW });
  assert.deepEqual(e.data.topics, ['team_645', 'team_611']);
  assert.equal(e.data.type, 'match');
  assert.equal(e.data.fixture_id, '77');
  assert.equal(e.data.channel, 'goals');
});
