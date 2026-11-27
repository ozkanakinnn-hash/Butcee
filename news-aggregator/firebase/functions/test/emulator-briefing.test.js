'use strict';

/** Bülten güvencesi uçtan uca (emülatör + sahte FCM): dünkü bülten kalmaz, bildirim bir kez gider. */
const test = require('node:test');
const assert = require('node:assert/strict');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

// 08:40 İstanbul (sabah penceresi)
const MORNING = Date.parse('2026-11-28T05:40:00Z');
// 12:00 İstanbul (pencere dışı)
const NOON = Date.parse('2026-11-28T09:00:00Z');

test('dünkü bülten kalmışsa bugünün sabah bülteni üretilir, bildirim tek kez gider', { skip, timeout: 30_000 }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore, Timestamp } = require('firebase-admin/firestore');
  const { ensureBriefing } = require('../src/briefing-job');
  initializeApp({ projectId: 'demo-briefing' });
  const db = getFirestore();

  // Dünkü akşam bülteni "latest"te kalmış; hikâye listesi boş (haber çekimi aksamış)
  await db.doc('briefings/latest').set({ id: '2026-11-27-pm', slot: 'pm', items: [{ id: 'eski', title: 'Dünkü haber', url: 'u' }] });
  await db.doc('notify_state/news').set({ stories: [], bulletin: { last: '2026-11-27#pm', briefing_key: '2026-11-27#pm' } });

  // Son 14 saatte haberler: iki ayrı olay, biri 3 yayıncıda
  const art = (id, title, source, minsAgo) => db.doc(`articles/${id}`).set({
    title, source_name: source, original_url: `https://ornek.com/${id}`, summary: `${title} ayrıntıları`,
    published_at: Timestamp.fromMillis(MORNING - minsAgo * 60_000), category_id: db.doc('categories/gundem'),
  });
  await art('a1', 'Merkez Bankası faiz kararını açıkladı', 'AA', 50);
  await art('a2', 'Merkez Bankası faiz kararını açıkladı: yüzde 40', 'NTV', 45);
  await art('a3', 'Merkez Bankası faiz kararı açıklandı', 'TRT Haber', 40);
  await art('b1', 'Meclis bütçe görüşmelerine başladı', 'Hürriyet', 30);
  await art('old', 'Çok eski bir haber başlığı burada', 'AA', 20 * 60);

  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  const r = await ensureBriefing(db, { messaging, now: MORNING, summarize: async () => 'Bugün faiz kararı öne çıktı.' });
  assert.equal(r.status, 'gönderildi');
  assert.equal(r.id, '2026-11-28-am');
  const latest = (await db.doc('briefings/latest').get()).data();
  assert.equal(latest.id, '2026-11-28-am');
  assert.equal(latest.items[0].source_count, 3, 'en çok kaynaklı haber önde');
  assert.ok(latest.items[0].summary, 'manşet spotu için özet');
  assert.ok(!latest.items.some((i) => i.id === 'old'), '14 saatten eski haber girmez');
  assert.equal(sent.length, 1);
  assert.equal(sent[0].topic, 'bulletin');
  assert.equal(sent[0].data.briefing_id, '2026-11-28-am');
  assert.match(sent[0].data.n_body, /^Bugün faiz kararı öne çıktı\./, 'AI özeti bildirimde');
  const after = (await db.doc('briefings/latest').get()).data();
  assert.equal(after.ai_summary, 'Bugün faiz kararı öne çıktı.');
  assert.equal(after.notified, true, 'Make için işaretli');

  // İkinci tur: belge var, bildirim gitmiş → hiçbir şey yapmaz
  const r2 = await ensureBriefing(db, { messaging, now: MORNING + 10 * 60_000 });
  assert.equal(r2.status, 'güncel');
  assert.equal(sent.length, 1, 'bildirim ikinci kez gitmez');
  const st = (await db.doc('notify_state/news').get()).data();
  assert.equal(st.bulletin.last, '2026-11-28#am');
});

test('belge haber çekimiyle yazılmış ama bildirim gitmemişse yalnızca bildirim gider; pencere dışında bildirim yok', { skip }, async () => {
  const { getFirestore } = require('firebase-admin/firestore');
  const { ensureBriefing } = require('../src/briefing-job');
  const db = getFirestore();
  await db.doc('briefings/2026-11-28-am').set({
    id: '2026-11-28-am', slot: 'am', items: [
      { id: 'x', title: 'Birinci', url: 'https://ornek.com/x', source: 'AA', category_id: 'gundem' },
      { id: 'y', title: 'İkinci', url: 'https://ornek.com/y', source: 'NTV', category_id: 'gundem' },
    ],
  });
  await db.doc('notify_state/news').set({ stories: [], bulletin: { last: '2026-11-27#pm' } });
  const sent = [];
  const messaging = { sendEach: async (m) => { sent.push(...m); return { successCount: m.length, failureCount: 0, responses: m.map(() => ({ success: true })) }; } };

  const r = await ensureBriefing(db, { messaging, now: MORNING, summarize: async () => 'Bugün faiz kararı öne çıktı.' });
  assert.equal(r.status, 'gönderildi');
  assert.equal(sent.length, 1);

  await db.doc('notify_state/news').set({ stories: [], bulletin: { last: '2026-11-27#pm' } });
  const r2 = await ensureBriefing(db, { messaging, now: NOON });
  assert.equal(r2.status, 'güncel', 'öğlen: belge var, pencere dışı');
  assert.equal(sent.length, 1);
});
