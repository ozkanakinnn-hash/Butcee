'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { Timestamp } = require('firebase-admin/firestore');
const a = require('../src/akis');

const T = (iso) => Date.parse(iso);

test('dueSlot: hafta içi 4 akış, hafta sonu yalnızca haftasonu; saat geçince bir kez', () => {
  assert.equal(a.dueSlot(T('2026-11-27T04:20:00Z')), null); // Cuma 07:20 → henüz değil
  const s = a.dueSlot(T('2026-11-27T04:40:00Z')); // Cuma 07:40
  assert.equal(s.slot, 'sabah');
  assert.equal(s.id, '2026-11-27-sabah');
  assert.equal(a.dueSlot(T('2026-11-27T04:40:00Z'), { last_id: s.id }), null, 'aynı akış ikinci kez gitmez');
  assert.equal(a.dueSlot(T('2026-11-27T06:30:00Z')), null, '100 dk sonra atlanır');
  assert.equal(a.dueSlot(T('2026-11-27T20:45:00Z')).slot, 'gece'); // Cuma 23:45
  assert.equal(a.dueSlot(T('2026-11-28T04:40:00Z')), null, 'cumartesi sabah akışı yok');
  assert.equal(a.dueSlot(T('2026-11-28T17:40:00Z')).slot, 'haftasonu'); // Cmt 20:40
  assert.equal(a.dueSlot(T('2026-11-29T17:50:00Z')).slot, 'haftasonu'); // Paz 20:50
  assert.equal(a.dueSlot(T('2026-11-30T17:40:00Z')), null, 'pazartesi 20:40 akış yok');
});

test('slotWindow: aralıklar boşluksuz (cuma gece → cmt → paz → pzt sabah)', () => {
  const sat = a.dueSlot(T('2026-11-28T17:40:00Z'));
  assert.equal(new Date(sat.start).toISOString(), '2026-11-27T20:30:00.000Z'); // cuma 23:30
  const sun = a.dueSlot(T('2026-11-29T17:40:00Z'));
  assert.equal(new Date(sun.start).toISOString(), '2026-11-28T17:30:00.000Z'); // cmt 20:30
  const mon = a.dueSlot(T('2026-11-30T04:40:00Z'));
  assert.equal(new Date(mon.start).toISOString(), '2026-11-29T17:30:00.000Z'); // paz 20:30
  const tue = a.dueSlot(T('2026-12-01T04:40:00Z'));
  assert.equal(new Date(tue.start).toISOString(), '2026-11-30T20:30:00.000Z'); // pzt 23:30
  assert.match(sat.date, /28 Kasım/);
});

test('groupEvents aynı olayı birleştirir, son dakika ve çok kaynaklı önce gelir', () => {
  const ev = a.groupEvents([
    { title: '2027 bütçesi Plan ve Bütçe Komisyonu\'nda kabul edildi', source: 'A', summary: '', breaking: false },
    { title: 'Gürcistan Büyükelçisi Dışişleri Bakanlığı\'na çağrıldı', source: 'B', summary: 'x', breaking: false },
    { title: 'Gürcistan\'ın Ankara Büyükelçisi Dışişleri Bakanlığına çağrıldı', source: 'C', summary: '', breaking: true },
    { title: 'Şok eden olay! Bakın ne oldu', source: 'D', summary: '', breaking: false },
  ]);
  assert.equal(ev.length, 2);
  assert.deepEqual(ev[0].kaynaklar, ['B', 'C']);
  assert.equal(ev[0].son_dakika, true);
  assert.equal(ev[0].t, undefined);
});

test('composeText 280 sınırına sığar, sığmayan madde atlanır', () => {
  const info = { emoji: '🌅', label: 'Sabah Akışı', date: '27 Kasım' };
  const m = Array.from({ length: 6 }, (_, i) => ({ metin: `Madde ${i} ${'a'.repeat(70)}`, konu_anahtari: `k${i}` }));
  m[2].metin = '🔄 Gelişme var';
  const { metin, used } = a.composeText(info, m);
  const counted = metin.replace('https://haber-68a3c.web.app', 'x'.repeat(23));
  assert.ok(counted.length <= 280, `uzunluk ${counted.length}`);
  assert.ok(metin.startsWith('🌅 Sabah Akışı | 27 Kasım\n🔸 Madde 0'));
  assert.ok(metin.includes('\n🔄 Gelişme var'));
  assert.ok(used.length >= 2 && used.length < 6);
});

test('parseJson ve kopya kontrolü', () => {
  assert.deepEqual(a.parseJson('```json\n{"maddeler":[]}\n```'), { maddeler: [] });
  assert.throws(() => a.parseJson('yok'), /JSON/);
  const titles = ['Karadağ vatandaşlarının Türkiye\'ye vizesiz girişleri durduruldu bugün'];
  assert.equal(a.copiedPhrases(['Karadağ vatandaşları için vizesiz giriş askıya alındı'], titles).length, 0);
  assert.ok(a.copiedPhrases(['Karadağ vatandaşlarının Türkiye\'ye vizesiz girişleri durduruldu'], titles).length > 0);
  assert.equal(a.topicId('Gürcistan / Batum Olayları'), 'gurcistan-batum-olaylari');
});

// ── runAkis: küçük bellek içi Firestore taklidi ──
function fakeDb(seed = {}) {
  const docs = new Map(Object.entries(seed));
  const logs = [];
  const docRef = (path) => ({
    path,
    get: async () => ({ exists: docs.has(path), data: () => docs.get(path), get: (k) => (docs.get(path) || {})[k], id: path.split('/').pop(), ref: docRef(path) }),
    set: async (v, opt) => { docs.set(path, opt && opt.merge ? { ...(docs.get(path) || {}), ...v } : v); },
    delete: async () => docs.delete(path),
  });
  const query = (col, filters = []) => ({
    where: (f, op, v) => query(col, [...filters, [f, op, v]]),
    orderBy: () => query(col, filters),
    limit: () => query(col, filters),
    get: async () => {
      const out = [...docs.entries()]
        .filter(([p]) => p.startsWith(`${col}/`) && p.split('/').length === 2)
        .filter(([, d]) => filters.every(([f, op, v]) => {
          const x = d[f] && d[f].toMillis ? d[f].toMillis() : d[f];
          const y = v && v.toMillis ? v.toMillis() : v;
          return op === '>=' ? x >= y : op === '<' ? x < y : true;
        }))
        .map(([p, d]) => ({ id: p.split('/')[1], data: () => d, get: (k) => d[k], ref: docRef(p) }));
      return { docs: out, empty: out.length === 0, size: out.length };
    },
  });
  return {
    docs, logs,
    doc: docRef,
    collection: (col) => ({
      ...query(col),
      doc: (id) => docRef(`${col}/${id}`),
      add: async (v) => { if (col === 'social_log') logs.push(v); },
    }),
    batch: () => {
      const ops = [];
      return {
        set: (ref, v) => ops.push(() => {
          const cur = docs.get(ref.path) || {};
          const n = { ...cur, ...v };
          if (v.paylasim_sayisi && typeof v.paylasim_sayisi === 'object') n.paylasim_sayisi = (cur.paylasim_sayisi || 0) + 1;
          docs.set(ref.path, n);
        }),
        delete: (ref) => ops.push(() => docs.delete(ref.path)),
        commit: async () => ops.forEach((f) => f()),
      };
    },
  };
}

const NOW = T('2026-11-27T15:40:00Z'); // Cuma 18:40 → akşam akışı
const art = (id, title, minAgo, extra = {}) => ({
  title, summary: '', source_name: 'Kaynak', published_at: Timestamp.fromMillis(NOW - minAgo * 60_000), ...extra,
});
const baseCfg = { enabled: true, x_enabled: true, x_webhook: 'https://hook.eu1.make.com/abc', dry_run: false, send_akis: true };

test('runAkis: kapalıyken hiçbir şey yapmaz', async () => {
  const db = fakeDb({ 'app_meta/social': { ...baseCfg, send_akis: false } });
  assert.equal((await a.runAkis(db, { now: NOW })).status, 'kapalı');
});

test('runAkis: yazar, Make\'e mevcut alanlarla gönderir, hafızaya kaydeder, ikinci kez göndermez', async () => {
  const db = fakeDb({
    'app_meta/social': { ...baseCfg },
    'articles/1': art('1', 'Karadağ vatandaşlarının vizesiz girişleri durduruldu', 60),
    'articles/2': art('2', '2027 bütçesi komisyonda kabul edildi', 90),
    'articles/9': art('9', 'Eski haber', 600),
  });
  const sent = [];
  const fetchImpl = async (url, opt) => { sent.push(JSON.parse(opt.body)); return { ok: true, status: 200 }; };
  let prompt = '';
  const generate = async (p) => {
    prompt = p;
    return { text: JSON.stringify({ maddeler: [
      { metin: 'Karadağ vatandaşları için vizesiz giriş askıya alındı.', konu_anahtari: 'karadag-vize', durum: 'yeni', son_durum: 'Vizesiz giriş askıda' },
      { metin: '🔄 2027 bütçesi komisyondan geçti.', konu_anahtari: 'butce-2027', durum: 'gelisme', son_durum: 'Komisyonda kabul' },
    ], atlananlar: ['x'] }), model: 'm', error: '' };
  };
  const r = await a.runAkis(db, { now: NOW, fetchImpl, generate });
  assert.match(r.status, /^gönderildi \(2 madde/);
  assert.ok(prompt.includes('Karadağ') && !prompt.includes('Eski haber'), 'yalnızca akış aralığındaki haberler');
  assert.equal(sent.length, 1);
  const p = sent[0];
  for (const k of ['tur', 'zaman', 'baslik', 'kaynak', 'kaynak_sayisi', 'link', 'metin', 'haber_id', 'deneme']) assert.ok(k in p, k);
  assert.equal(p.tur, 'akis');
  assert.equal(p.deneme, false);
  assert.match(p.metin, /^🌆 Akşam Akışı \| 27 Kasım/);
  assert.equal(db.docs.get('bulletin_topics/karadag-vize').son_durum, 'Vizesiz giriş askıda');
  assert.equal(db.docs.get('notify_state/akis').last_id, '2026-11-27-aksam');
  assert.match(db.docs.get('app_meta/social').akis_status, /gönderildi/);

  const again = await a.runAkis(db, { now: NOW + 20 * 60_000, fetchImpl, generate });
  assert.equal(again.status, 'bekle');
  assert.equal(sent.length, 1);

  // Sonraki akışta geçmiş yapay zekâya verilir
  db.docs.set('articles/3', art('3', 'Karadağ vize kararı sonrası yeni açıklama yapıldı', -200));
  await a.runAkis(db, { now: T('2026-11-27T20:40:00Z'), fetchImpl, generate });
  assert.ok(prompt.includes('karadag-vize | Vizesiz giriş askıda'));
});

test('runAkis: dry_run göndermez, social_log\'a yazar; gelişme yoksa göndermez', async () => {
  const db = fakeDb({ 'app_meta/social': { ...baseCfg, dry_run: true }, 'articles/1': art('1', 'Meclis bütçe görüşmelerine başladı', 30) });
  let calls = 0;
  const fetchImpl = async () => { calls += 1; return { ok: true }; };
  const gen = async () => ({ text: '{"maddeler":[{"metin":"Meclis bütçe görüşmelerine başladı.","konu_anahtari":"butce"}]}', model: 'm', error: '' });
  const r = await a.runAkis(db, { now: NOW, fetchImpl, generate: gen });
  assert.match(r.status, /^deneme/);
  assert.equal(calls, 0);
  assert.equal(db.logs.length, 1);
  assert.equal(db.docs.has('bulletin_topics/butce'), false, 'denemede hafızaya yazılmaz');
  const dbA = fakeDb({ 'app_meta/social': { ...baseCfg, akis_dry_run: true }, 'articles/1': art('1', 'Meclis bütçe görüşmelerine başladı', 30) });
  assert.match((await a.runAkis(dbA, { now: NOW, fetchImpl, generate: gen })).status, /^deneme/);
  assert.equal(calls, 0, 'akis_dry_run yalnızca akışı deneme yapar');

  const db2 = fakeDb({ 'app_meta/social': { ...baseCfg }, 'articles/1': art('1', 'Meclis bütçe görüşmelerine başladı', 30) });
  const r2 = await a.runAkis(db2, { now: NOW, fetchImpl, generate: async () => ({ text: '{"maddeler":[],"atlananlar":["butce"]}', model: 'm', error: '' }) });
  assert.match(r2.status, /yeni gelişme yok/);
  assert.equal(calls, 0);
});

test('runAkis: yapay zekâ hatasında akış ilerlemez (sonraki turda yeniden dener)', async () => {
  const db = fakeDb({ 'app_meta/social': { ...baseCfg }, 'articles/1': art('1', 'Meclis bütçe görüşmelerine başladı', 30) });
  const r = await a.runAkis(db, { now: NOW, fetchImpl: async () => ({ ok: true }), generate: async () => ({ text: '', model: '', error: 'HTTP 429' }) });
  assert.equal(r.status, 'ai hata');
  assert.equal(db.docs.has('notify_state/akis'), false);
});

test('forcedSlot: gece yarısından sonra gece akışı önceki günün 18:30–23:30 aralığıdır', () => {
  const f = a.forcedSlot('gece', T('2026-11-27T23:08:00Z')); // Cmt 02:08 TSİ
  assert.equal(f.id, '2026-11-27-gece');
  assert.equal(new Date(f.start).toISOString(), '2026-11-27T15:30:00.000Z');
  assert.equal(new Date(f.end).toISOString(), '2026-11-27T20:30:00.000Z');
  assert.throws(() => a.forcedSlot('ikindi', Date.now()), /bilinmeyen/);
});

test('runAkis elle: send_akis kapalı ve akis_dry_run açık olsa da gönderir (genel dry_run hariç)', async () => {
  const now = T('2026-11-27T23:08:00Z');
  const db = fakeDb({
    'app_meta/social': { ...baseCfg, send_akis: false, akis_dry_run: true },
    'articles/1': { title: 'Meclis bütçe görüşmelerine başladı', summary: '', source_name: 'K', published_at: Timestamp.fromMillis(T('2026-11-27T18:00:00Z')) },
  });
  let sent = 0;
  const r = await a.runAkis(db, {
    now, slot: 'gece', ignoreDryRun: true, fetchImpl: async () => { sent += 1; return { ok: true }; },
    generate: async () => ({ text: '{"maddeler":[{"metin":"Meclis bütçe görüşmeleri başladı.","konu_anahtari":"butce"}]}', model: 'm', error: '' }),
  });
  assert.match(r.status, /^gönderildi/);
  assert.equal(sent, 1);
  assert.match(r.metin, /^🌙 Gece Akışı \| 27 Kasım/);
});
