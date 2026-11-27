'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const {
  planNotifications, buildBulletinMessage, withAiSummary, sendNewsNotifications, toWire, PROFILES, BULLETIN_SLOTS,
} = require('../src/notify');

// 08:40 İstanbul (sabah bülteni penceresi içinde)
const MORNING = Date.parse('2026-11-06T05:40:00Z');
// 12:00 İstanbul (pencere dışında)
const NOON = Date.parse('2026-11-06T09:00:00Z');

const ts = (ms) => ({ toMillis: () => ms });
const ref = (id) => ({ id });
const art = (id, cat, ms, extra = {}) => ({
  id,
  data: {
    title: `Başlık ${id}`,
    original_url: `https://ornek.com/${id}`,
    source_name: 'Kaynak',
    image_url: `https://cdn.ornek.com/${id}.jpg`,
    has_original_image: true,
    category_id: ref(cat),
    published_at: ts(ms),
    ...extra,
  },
});
const categories = new Map([['ekonomi', { name: 'Ekonomi' }], ['gundem', { name: 'Gündem' }]]);

/** Birbirinden farklı üç hikâye (başlık kökleri ortak değil). */
const threeStories = (now) => [{
  addedArticles: [
    art('a', 'ekonomi', now - 3_600_000, { title: 'Merkez Bankası faiz kararını açıkladı', source_name: 'AA' }),
    art('b', 'ekonomi', now - 3_500_000, { title: 'Merkez Bankası faiz kararı piyasaları etkiledi', source_name: 'NTV' }),
    art('c', 'gundem', now - 3_000_000, { title: 'Meclis bütçe görüşmelerine başladı', source_name: 'Hürriyet' }),
    art('d', 'gundem', now - 2_000_000, { title: 'İstanbul trafiğinde yeni metro hattı açılıyor', source_name: 'Sabah' }),
  ],
}];

test('sabah penceresinde tek bülten gider; aynı bülten ikinci turda tekrar gitmez', () => {
  const r1 = planNotifications({ results: threeStories(MORNING), categories, state: {}, now: MORNING });
  assert.equal(r1.messages.length, 1);
  const m = r1.messages[0];
  assert.equal(m.topic, 'bulletin');
  assert.equal(m.notification.title, BULLETIN_SLOTS[0].title);
  assert.ok(r1.briefing, 'gündem belgesi oluşur');
  assert.equal(r1.briefing.id, '2026-11-06-am');
  assert.equal(m.data.briefing_id, '2026-11-06-am');
  // En çok kaynaklı hikâye (iki yayıncı: faiz) ilk sırada
  assert.match(r1.briefing.items[0].title, /faiz/);
  assert.equal(r1.briefing.items[0].source_count, 2);

  const r2 = planNotifications({ results: [], categories, state: r1.nextState, now: MORNING + 15 * 60_000 });
  assert.equal(r2.messages.length, 0);
});

test('pencere dışında bildirim gitmez; tek hikâye bülten için yetmez', () => {
  const r = planNotifications({ results: threeStories(NOON), categories, state: {}, now: NOON });
  assert.equal(r.messages.length, 0);

  const one = [{ addedArticles: [art('x', 'gundem', MORNING - 60_000, { title: 'Tek başına bir haber başlığı burada' })] }];
  const r2 = planNotifications({ results: one, categories, state: {}, now: MORNING });
  assert.equal(r2.messages.length, 0);
  assert.equal(r2.briefing, null);
});

test('AI özeti bülten gövdesine eklenir, ilk iki başlık altta kalır; özet yoksa değişmez', () => {
  const stories = [
    { id: '1', title: 'Birinci başlık', source: 'AA', url: 'u1', cat: 'gundem', srcs: ['AA'] },
    { id: '2', title: 'İkinci başlık', source: 'NTV', url: 'u2', cat: 'gundem', srcs: ['NTV'] },
    { id: '3', title: 'Üçüncü başlık', source: 'TRT', url: 'u3', cat: 'gundem', srcs: ['TRT'] },
  ];
  const m = buildBulletinMessage(BULLETIN_SLOTS[0], stories, MORNING, 'b1');
  const withAi = withAiSummary(m, 'Bugün gündemde üç önemli gelişme var.');
  assert.equal(withAi.notification.body, 'Bugün gündemde üç önemli gelişme var.\n• Birinci başlık\n• İkinci başlık');
  assert.equal(withAiSummary(m, ''), m);
  assert.equal(toWire(withAi).data.n_body, withAi.notification.body);
});

test('gönderim: AI özeti bildirime ve gündem belgesine yazılır, belge notified olarak işaretlenir', async () => {
  const docs = {};
  const db = {
    doc: (path) => ({ get: async () => ({ data: () => docs[path] }), set: async (v) => { docs[path] = v; } }),
    collection: () => ({ doc: (id) => ({ id }) }),
    batch: () => ({ set: () => {}, commit: async () => {} }),
  };
  let sentMsgs = [];
  const messaging = {
    sendEach: async (msgs) => {
      sentMsgs = msgs;
      return { successCount: msgs.length, failureCount: 0, responses: msgs.map(() => ({ success: true })) };
    },
  };
  const summarize = async (items) => `Özet (${items.length} haber).`;
  const r = await sendNewsNotifications(db, threeStories(MORNING), categories, { messaging, now: MORNING, summarize });
  assert.equal(r.sent, 1);
  assert.match(sentMsgs[0].data.n_body, /^Özet \(\d haber\)\./);
  assert.equal(docs['briefings/latest'].notified, true);
  assert.match(docs['briefings/latest'].ai_summary, /^Özet/);
  assert.equal(docs['briefings/2026-11-06-am'].id, '2026-11-06-am');
  assert.ok(docs['notify_state/news'].bulletin.last, 'durum kaydedilir');
});

test('gönderim: FCM hatası çekimi bozmaz, özet hatası bülteni durdurmaz', async () => {
  const db = {
    doc: () => ({ get: async () => ({ data: () => ({}) }), set: async () => {} }),
    collection: () => ({ doc: (id) => ({ id }) }),
    batch: () => ({ set: () => {}, commit: async () => {} }),
  };
  const bad = { sendEach: async () => { throw new Error('ağ hatası'); } };
  const r = await sendNewsNotifications(db, threeStories(MORNING), categories, {
    messaging: bad, now: MORNING, summarize: async () => { throw new Error('gemini'); },
  });
  assert.equal(r.sent, 0);
  assert.match(r.error, /ağ hatası/);
});

test('bildirim seviyeleri: düşük 5, dengeli 9, yüksek 15', () => {
  assert.equal(PROFILES.low.dailyCap, 5);
  assert.equal(PROFILES.balanced.dailyCap, 9);
  assert.equal(PROFILES.all.dailyCap, 15);
});

test('bildirim kaydı: yeniler önce sıralanır, koşullu (iki takım) mesajın konuları çıkarılır', () => {
  const { logEntryFromMessage, logId } = require('../src/notification-log');
  assert.ok(logId(2000, 'a') < logId(1000, 'a'), 'yeni kayıt kimliği alfabetik olarak önce gelmeli');
  const e = logEntryFromMessage({
    condition: "'team_galatasaray' in topics || 'team_fenerbahce' in topics",
    notification: { title: '⚽ GOL!', body: "67' Icardi" },
    data: { type: 'match', fixture_id: '77' },
    android: { notification: { channelId: 'goals' } },
  }, { now: MORNING });
  assert.deepEqual(e.data.topics, ['team_galatasaray', 'team_fenerbahce']);
  assert.equal(e.data.type, 'match');
  assert.equal(e.data.fixture_id, '77');
  assert.equal(e.data.channel, 'goals');
});
