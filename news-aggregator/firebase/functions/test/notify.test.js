'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { planNotifications, buildNewsMessage, sendNewsNotifications, toWire, PROFILES } = require('../src/notify');

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

const byLevel = (messages, level) => messages.filter((m) => (m.condition || '').endsWith(`'${level}' in topics`));
const cats = new Map([...categories, ['gundem', { name: 'Gündem' }], ['ekonomi', { name: 'Ekonomi' }], ['dunya', { name: 'Dünya' }]]);

// Aynı olayı farklı yayıncılar verdi → tek hikâye, birden çok kaynak (özete yalnız bunlar girer)
const multi = (id, cat, iso, title, sources) =>
  sources.map((src, i) => art(`${id}${i}`, cat, iso, { title, source_name: src }));

test('özet: yalnız 3+ kaynaklı hikâye girer, 2 kaynaklı haber gitmez, başlıkta haber sayısı YOK', () => {
  const results = [{ addedArticles: [
    ...multi('s', 'saglik', '2026-11-06T11:30:00Z', 'Mevsimsel grip vakaları geçen yıla kıyasla azaldı', ['AA', 'NTV', 'TRT']),
    art('t', 'saglik', '2026-11-06T11:40:00Z', { title: 'Tek kaynaklı rutin sağlık haberi yayımlandı' }),
  ] }];
  const { messages, nextState } = planNotifications({ results, categories, state: {}, now: NOW });
  const [all] = byLevel(messages, 'lvl_all');
  assert.equal(all.notification.title, 'Sağlık');
  assert.match(all.notification.body, /^Mevsimsel grip vakaları/);
  assert.match(all.notification.body, /3 kaynakta$/);
  assert.equal(byLevel(messages, 'lvl_balanced').length, 1);
  assert.ok(!messages.some((m) => /'lvl_important' in topics/.test(m.condition || '')));
  assert.equal(nextState.all.last_sent.saglik, NOW);
});

test('özet: yalnız tek kaynaklı haber varsa bildirim gitmez', () => {
  const results = [{ addedArticles: [art('a', 'saglik', '2026-11-06T11:30:00Z'), art('b', 'spor', '2026-11-06T11:31:00Z')] }];
  const { messages } = planNotifications({ results, categories, state: {}, now: NOW });
  assert.equal(messages.length, 0);
});

test('çok kategori → TEK özet "Gündemin öne çıkanları"; Tümü 90 dk, Dengeli 3 saat aralıkla', () => {
  const results = [{ addedArticles: [
    ...multi('g', 'gundem', '2026-11-06T11:58:00Z', 'Meclis yeni vergi paketini kabul etti', ['NTV', 'AA', 'TRT']),
    ...multi('e', 'ekonomi', '2026-11-06T11:57:00Z', 'Merkez Bankası politika faizini sabit tuttu', ['AA', 'Habertürk', 'NTV', 'TRT']),
  ] }];
  const first = planNotifications({ results, categories: cats, state: {}, now: NOW });
  const [d] = byLevel(first.messages, 'lvl_all');
  assert.equal(d.notification.title, '📰 Gündemin öne çıkanları');
  assert.ok(!/\d+ yeni haber/.test(d.notification.title));
  assert.match(d.notification.body, /^Ekonomi: Merkez Bankası/, 'en çok kaynaklı önce');
  const more = [{ addedArticles: multi('h', 'dunya', '2026-11-06T12:10:00Z', 'Avrupa Birliği zirvesi sona erdi', ['AA', 'BBC', 'TRT']) }];
  const t60 = planNotifications({ results: more, categories: cats, state: first.nextState, now: NOW + 60 * 60_000 });
  assert.equal(byLevel(t60.messages, 'lvl_all').length, 0, 'Tümü: 90 dk dolmadı');
  const t95 = planNotifications({ results: [], categories: cats, state: t60.nextState, now: NOW + 95 * 60_000 });
  assert.equal(byLevel(t95.messages, 'lvl_all').length, 1);
  assert.equal(byLevel(t95.messages, 'lvl_balanced').length, 0, 'Dengeli: 3 saat dolmadı');
});

test('özet gece (23:00–08:00) ve bülten saatine yakın gitmez; gece kuyruk boşaltılır', () => {
  const night = Date.parse('2026-11-06T21:30:00Z'); // İstanbul 00:30
  const results = [{ addedArticles: multi('n', 'saglik', '2026-11-06T21:20:00Z', 'Hastanelerde yeni randevu sistemi başladı', ['AA', 'NTV']) }];
  const r = planNotifications({ results, categories, state: {}, now: night });
  assert.equal(r.messages.filter((m) => m.condition && /lvl_(all|balanced)/.test(m.condition)).length, 0);
  assert.deepEqual(r.nextState.all.pending, {});
  const nearBulletin = Date.parse('2026-11-06T15:30:00Z'); // 18:30, akşam gündemine 30 dk
  const r2 = planNotifications({ results, categories, state: {}, now: nearBulletin });
  assert.equal(byLevel(r2.messages, 'lvl_all').length, 0);
});

test('"Dengeli" günde en fazla 3 özet; gün değişince sıfırlanır', () => {
  const st = { balanced: { day: '2026-11-06', count: 3 } };
  const results = [{ addedArticles: multi('y', 'saglik', '2026-11-06T11:59:00Z', 'Grip vakaları okullarda artış gösterdi', ['AA', 'NTV', 'TRT']) }];
  const r = planNotifications({ results, categories, state: st, now: NOW });
  assert.equal(byLevel(r.messages, 'lvl_balanced').length, 0);
  assert.equal(byLevel(r.messages, 'lvl_all').length, 1);
  const tomorrow = Date.parse('2026-11-07T09:00:00Z'); // 12:00
  const r2 = planNotifications({ results: [{ addedArticles: multi('z', 'spor', '2026-11-07T08:59:00Z', 'Milli takım aday kadrosu açıklandı', ['A Spor', 'NTV', 'AA']) }], categories, state: st, now: tomorrow });
  assert.equal(byLevel(r2.messages, 'lvl_balanced').length, 1);
  assert.equal(r2.nextState.balanced.count, 1);
});

test('kimler verdi: yayıncı sayısı artan hikâye için kaynak listesi güncellenir', () => {
  const results = [{ addedArticles: multi('k', 'gundem', '2026-11-06T11:30:00Z', 'Valilik kar tatili kararını duyurdu', ['AA', 'NTV', 'CNN Türk']) }];
  const r = planNotifications({ results, categories: cats, state: {}, now: NOW });
  assert.deepEqual(r.sourceUpdates, [{ id: 'k0', srcs: ['AA', 'NTV', 'CNN Türk'] }]);
  const again = planNotifications({ results: [], categories: cats, state: r.nextState, now: NOW + 60_000 });
  assert.deepEqual(again.sourceUpdates, [], 'değişmeyen hikâye yeniden yazılmaz');
});

test('tıklama tuzağı/magazin başlıkları bildirime girmez', () => {
  const results = [{ addedArticles: [art('m', 'saglik', '2026-11-06T11:59:00Z', { title: 'Ünlü oyuncu tatil pozuyla şoke etti' })] }];
  assert.equal(planNotifications({ results, categories, state: {}, now: NOW }).messages.length, 0);
});

test('son dakika ("Tümü"): hemen gider, aynı haber tekrar gönderilmez, eski haber gönderilmez', () => {
  const sd = art('x', 'gundem', '2026-11-06T11:50:00Z', { title: 'Meclis yeni yasayı kabul etti' });
  const results = [{ addedArticles: [], breakingArticles: [sd, art('old', 'gundem', '2026-11-06T06:00:00Z')] }];
  const first = planNotifications({ results, categories, state: {}, now: NOW });
  const minor = first.messages.filter((m) => m.condition === "'news_son-dakika' in topics && 'lvl_all' in topics");
  assert.equal(minor.length, 1);
  assert.equal(minor[0].notification.title, '🔴 SON DAKİKA');
  assert.equal(minor[0].android.priority, 'high');
  assert.equal(minor[0].android.notification.channelId, 'breaking_news');
  assert.equal(first.messages.length, 1, 'tek kaynak → önemli son dakika değil');
  const again = planNotifications({ results, categories, state: first.nextState, now: NOW + 20 * 60_000 });
  assert.equal(again.messages.length, 0);
});

test('önemli son dakika: 3 farklı yayıncı aynı olayı verince "Sadece önemli/Dengeli"ye bir kez gider', () => {
  const t = (id, src, title, min) => art(id, 'gundem', `2026-11-06T11:${min}:00Z`, { source_name: src, title });
  const r1 = planNotifications({ results: [{ addedArticles: [t('a', 'NTV', 'Merkez Bankası faizi yüzde 35\'e indirdi', 40)] }], categories: cats, state: {}, now: NOW });
  assert.ok(!r1.messages.some((m) => /'calm'/.test(m.condition || '')));
  const r2 = planNotifications({ results: [
    { addedArticles: [t('b', 'AA', 'TCMB politika faizini 250 baz puan indirdi', 45)] },
    { addedArticles: [t('c', 'Habertürk', 'Merkez Bankası faiz kararını açıkladı', 50)] },
  ], categories: cats, state: r1.nextState, now: NOW + 20 * 60_000 });
  const major = r2.messages.filter((m) => m.condition === "'news_son-dakika' in topics && 'calm' in topics");
  assert.equal(major.length, 1);
  // Faiz kararı = SEVİYE 1 (ülkeyi etkileyen ekonomik karar) → "SON DAKİKA" başlığı, data.level = 1
  assert.equal(major[0].notification.title, '🔴 SON DAKİKA');
  assert.equal(major[0].data.level, '1');
  assert.match(major[0].notification.body, /3 kaynakta$/);
  // Aynı hikâyeye 4. kaynak eklense de tekrar gitmez
  const r3 = planNotifications({ results: [{ addedArticles: [t('d', 'Sözcü', 'Merkez Bankası faizi indirdi: yüzde 35', 55)] }], categories: cats, state: r2.nextState, now: NOW + 40 * 60_000 });
  assert.equal(r3.messages.filter((m) => /'calm'/.test(m.condition || '')).length, 0);
});

test('önemli son dakika: son dakika kaynağından kritik haber (deprem) tek kaynakla da hemen gider; sınır uygulanmaz', () => {
  const quake = art('q', 'gundem', '2026-11-06T11:58:00Z', { source_name: 'AA', title: 'Malatya\'da 6,1 büyüklüğünde deprem' });
  const state = { breaking: { day: '2026-11-06', major_count: 99, last_major_at: NOW - 60_000 } };
  const r = planNotifications({ results: [{ breakingArticles: [quake] }], categories, state, now: NOW });
  const major = r.messages.filter((m) => /'calm'/.test(m.condition || ''));
  assert.equal(major.length, 1);
  assert.equal(major[0].notification.title, '🔴 SON DAKİKA');
});

test('bülten: sabah 08:30 sonrası ilk çalışmada en çok kaynağın verdiği haberlerle bir kez gider', () => {
  const at = (iso) => Date.parse(iso);
  const morning = at('2026-11-07T05:35:00Z'); // İstanbul 08:35
  const stories = [
    { t: ['merke', 'banka', 'faizi'], srcs: ['AA', 'NTV', 'Habertürk'], first_ms: morning - 3_600_000, last_ms: morning - 1_800_000, title: 'Merkez Bankası faizi indirdi', url: 'u1', source: 'AA', image: '', cat: 'ekonomi', br: false, id: 'm1' },
    { t: ['galat', 'fener', 'derbi'], srcs: ['A Spor', 'NTV'], first_ms: morning - 7_200_000, last_ms: morning - 7_000_000, title: 'Derbide kazanan Galatasaray', url: 'u2', source: 'A Spor', image: '', cat: 'spor', br: false, id: 'm2' },
    { t: ['unlu', 'oyunc', 'tatil'], srcs: ['X', 'Y', 'Z', 'W'], first_ms: morning - 3_600_000, last_ms: morning - 3_000_000, title: 'Ünlü oyuncu tatil pozuyla şoke etti', url: 'u3', source: 'X', image: '', cat: 'magazin', br: false, id: 'm3' },
  ];
  const r = planNotifications({ results: [], categories, state: { stories, bulletin: { last_ms: morning - 12 * 3_600_000 } }, now: morning });
  const b = r.messages.filter((m) => m.topic === 'bulletin');
  assert.equal(b.length, 1);
  assert.equal(b[0].notification.title, '☀️ 5 dakikada gündem');
  assert.equal(b[0].notification.body, '• Merkez Bankası faizi indirdi\n• Derbide kazanan Galatasaray\nKaynak: AA, A Spor');
  assert.equal(r.nextState.bulletin.last, '2026-11-07#am');
  assert.equal(b[0].data.briefing_id, '2026-11-07-am');
  assert.equal(r.briefing.id, '2026-11-07-am');
  assert.equal(r.briefing.items[0].source_count, 3);
  assert.deepEqual(r.briefing.items.map((x) => x.id), ['m1', 'm2'], 'magazin tıklama tuzağı gündeme girmez');
  const again = planNotifications({ results: [], categories, state: r.nextState, now: morning + 20 * 60_000 });
  assert.equal(again.messages.filter((m) => m.topic === 'bulletin').length, 0, 'aynı bülten ikinci kez gitmez');
  const late = planNotifications({ results: [], categories, state: { stories }, now: at('2026-11-07T08:00:00Z') }); // 11:00
  assert.equal(late.messages.filter((m) => m.topic === 'bulletin').length, 0, 'saat aralığı dışında bülten yok');
});

test('kablo biçimi: Android veri mesajı (düğmeli bildirim için) + iOS uyarısı; tüm değerler metin', () => {
  // Saat sabit (gündüz): gece sessiz saatte kanal 'quiet' olur ve test günün saatine bağlı kalırdı.
  const m = buildNewsMessage({ categoryId: 'saglik', categoryName: 'Sağlık', articles: [art('a', 'saglik', '2026-11-06T11:00:00Z')], now: new Date('2026-11-06T12:00:00Z') });
  const w = toWire({ ...m, topic: undefined, condition: "'news_saglik' in topics && 'lvl_all' in topics" });
  assert.equal(w.notification, undefined);
  assert.equal(w.condition, "'news_saglik' in topics && 'lvl_all' in topics");
  assert.equal(w.data.kind, 'news_alert');
  assert.equal(w.data.n_title, 'Sağlık');
  assert.equal(w.data.n_channel, 'news');
  assert.equal(w.apns.payload.aps.alert.title, 'Sağlık');
  for (const v of Object.values(w.data)) assert.equal(typeof v, 'string');
});

test('gönderim: durum kaydedilir, FCM hatası çekimi bozmaz', async () => {
  const saved = [];
  const logged = [];
  const db = {
    doc: () => ({ get: async () => ({ data: () => ({}) }), set: async (v) => saved.push(v) }),
    collection: () => ({ doc: (id) => ({ id }) }),
    batch: () => ({ set: (ref, data) => logged.push({ id: ref.id, data }), commit: async () => {} }),
  };
  const results = [{ addedArticles: multi('a', 'saglik', '2026-11-06T11:00:00Z', 'Araştırmacılar mevsimlik grip aşısını test etti', ['AA', 'NTV', 'TRT']) }];
  const okMessaging = { sendEach: async (msgs) => ({ successCount: msgs.length, failureCount: 0, responses: msgs.map(() => ({ success: true })) }) };
  const r = await sendNewsNotifications(db, results, categories, { messaging: okMessaging, now: NOW });
  assert.ok(r.sent >= 2, 'En az Tümü + Dengeli gönderildi');
  assert.equal(saved.length, 1);
  assert.ok(logged.length >= 2, 'gönderilen bildirim Bildirimler sekmesi için kaydedilir');
  assert.deepEqual(logged[0].data.topics, ['news_saglik'], 'seviye konusu kayda girmez');
  assert.equal(logged[0].data.url, 'https://ornek.com/a0');

  const badMessaging = { sendEach: async () => { throw new Error('ağ hatası'); } };
  const r2 = await sendNewsNotifications(db, results, categories, { messaging: badMessaging, now: NOW });
  assert.equal(r2.sent, 0);
  assert.match(r2.error, /ağ hatası/);
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

test('bildirim kaydı: yeniler önce sıralanır, koşullu (iki takım) mesajın konuları çıkarılır', () => {
  const { logEntryFromMessage, logId } = require('../src/notification-log');
  assert.ok(logId(2000, 'a') < logId(1000, 'a'), 'yeni kayıt kimliği alfabetik olarak önce gelmeli');
  const e = logEntryFromMessage({
    condition: "'team_galatasaray' in topics || 'team_fenerbahce' in topics",
    notification: { title: '⚽ GOL!', body: "67' Icardi" },
    data: { type: 'match', fixture_id: '77' },
    android: { notification: { channelId: 'goals' } },
  }, { now: NOW });
  assert.deepEqual(e.data.topics, ['team_galatasaray', 'team_fenerbahce']);
  assert.equal(e.data.type, 'match');
  assert.equal(e.data.fixture_id, '77');
  assert.equal(e.data.channel, 'goals');
});

test('bildirimin altında kaynak yazar (son dakika dahil)', () => {
  const m = buildNewsMessage({
    categoryId: 'son-dakika',
    categoryName: 'Son Dakika',
    breaking: true,
    articles: [art('x', 'son-dakika', '2026-11-06T11:00:00Z', { title: 'Deprem', source_name: 'AA' })],
  });
  assert.equal(m.notification.body, 'Deprem\nKaynak: AA');
});

test('gece 00:00–08:00 bildirimler sessiz kanaldan; deprem/afet/savaş son dakikası sesli', () => {
  const { buildNewsMessage } = require('../src/notify');
  const night = new Date('2026-11-06T23:30:00Z'); // İstanbul 02:30
  const morning = new Date('2026-11-07T05:10:00Z'); // İstanbul 08:10
  const msg = (title, opts = {}) => buildNewsMessage({
    categoryId: opts.breaking ? 'son-dakika' : 'gundem', categoryName: 'Gündem', breaking: !!opts.breaking,
    articles: [art('n', 'gundem', '2026-11-06T23:20:00Z', { title })], now: opts.now || night,
  });
  // Olağan haber gece → sessiz, normal öncelik
  const plain = msg('Meclis yeni yasayı görüştü', { breaking: true });
  assert.equal(plain.android.notification.channelId, 'quiet');
  assert.equal(plain.android.priority, 'normal');
  assert.equal(plain.android.notification.sound, undefined);
  assert.equal(msg('Hava yarın yağışlı').android.notification.channelId, 'quiet');
  // Olağanüstü durumlar gece de sesli
  for (const t of [
    'Malatya\'da 6,1 büyüklüğünde deprem', 'DEPREM! İstanbul sallandı', 'AFAD: Sel felaketi bölgesinde tahliye',
    'Türkiye\'ye yönelik füze saldırısı', 'Olağanüstü hal ilan edildi', 'Savaş ilan edildi',
  ]) {
    const m = msg(t, { breaking: true });
    assert.equal(m.android.notification.channelId, 'breaking_news', t);
    assert.equal(m.android.priority, 'high', t);
  }
  // Küçük deprem geceyi bölmez; olağanüstü olsa bile kategori özeti (son dakika değil) sessiz
  assert.equal(msg('Ege Denizi\'nde 3.2 büyüklüğünde deprem', { breaking: true }).android.notification.channelId, 'quiet');
  assert.equal(msg('Savaş ilan edildi').android.notification.channelId, 'quiet');
  // Sabah 08:00'den sonra normal
  assert.equal(msg('Meclis yeni yasayı görüştü', { breaking: true, now: morning }).android.notification.channelId, 'breaking_news');
});

test('sessiz saat sınırları İstanbul saatine göre', () => {
  const { isQuietHour, isCritical } = require('../src/quiet-hours');
  assert.equal(isQuietHour(new Date('2026-11-06T20:30:00Z')), false); // 23:30 — henüz sesli
  assert.equal(isQuietHour(new Date('2026-11-06T20:59:00Z')), false); // 23:59
  assert.equal(isQuietHour(new Date('2026-11-06T21:00:00Z')), true); // 00:00
  assert.equal(isQuietHour(new Date('2026-11-07T04:59:00Z')), true); // 07:59
  assert.equal(isQuietHour(new Date('2026-11-07T05:00:00Z')), false); // 08:00
  assert.equal(isCritical('İSTANBUL\'DA DEPREM'), true);
  assert.equal(isCritical('Ekonomide savaşım sürüyor'), false, 'kelime içinde geçen "savaş" sayılmaz');
  assert.equal(isCritical('Borsa güne yükselişle başladı'), false);
});

test('gündem: bülten saati kaçırılsa da o dilimin gündemi bildirimsiz hazırlanır; ilgi alanı bölümleri', () => {
  const now = Date.parse('2026-11-22T17:30:00Z'); // İstanbul 20:30 (akşam bülteni penceresi içinde)
  const late = Date.parse('2026-11-22T20:00:00Z'); // 23:00 — pencere geçti
  const stories = [
    { t: ['a1', 'b1'], srcs: ['AA', 'NTV', 'TRT'], first_ms: late - 3_600_000, last_ms: late - 1_800_000, title: 'Asgari ücret görüşmeleri başladı', url: 'u1', source: 'AA', image: '', cat: 'ekonomi', br: false, id: 's1' },
    { t: ['a2', 'b2'], srcs: ['AA', 'NTV'], first_ms: late - 3_600_000, last_ms: late - 1_000_000, title: 'Meclis bütçe görüşmelerine başladı', url: 'u2', source: 'NTV', image: '', cat: 'gundem', br: false, id: 's2' },
  ];
  const r = planNotifications({ results: [], categories, state: { stories }, now: late });
  assert.equal(r.messages.filter((m) => m.topic === 'bulletin').length, 0, 'pencere dışında bildirim yok');
  assert.equal(r.briefing.id, '2026-11-22-pm');
  assert.equal(r.briefing.by_category.ekonomi[0].id, 's1');
  const again = planNotifications({ results: [], categories, state: r.nextState, now: late + 20 * 60_000 });
  assert.equal(again.briefing, null, 'aynı dilim tekrar üretilmez');
  // Gece yarısından sonra önceki günün akşam gündemi geçerli
  const night = Date.parse('2026-11-22T22:30:00Z'); // 01:30 (23 Kasım)
  const n = planNotifications({ results: [], categories, state: r.nextState, now: night });
  assert.equal(n.briefing, null);
  const inWin = planNotifications({ results: [], categories, state: { stories }, now });
  assert.equal(inWin.messages.filter((m) => m.topic === 'bulletin').length, 1, 'pencere içinde bildirim gider');
});

test('seviye 2: sıradan 3 kaynaklı haber "ÖNEMLİ GELİŞME" başlığıyla ve günlük sınırla gider', () => {
  const t = (id, src, title) => art(id, 'gundem', '2026-11-06T11:40:00Z', { source_name: src, title });
  const r = planNotifications({
    results: [{ addedArticles: [
      t('a', 'NTV', 'Şehirde yeni metro hattı hizmete açıldı'),
      t('b', 'AA', 'Yeni metro hattı bugün hizmete girdi'),
      t('c', 'TRT', 'Metro hattının açılışı yapıldı'),
    ] }],
    categories: cats, state: {}, now: NOW,
  });
  const major = r.messages.filter((m) => /'calm'/.test(m.condition || ''));
  assert.equal(major.length, 1);
  assert.equal(major[0].notification.title, '⚡ ÖNEMLİ GELİŞME');
  assert.equal(major[0].data.level, '2');
  assert.equal(r.nextState.breaking.major_count, 1, 'seviye 2 günlük sınıra sayılır');
});

test('seviye 4 (rutin/servis) hiç bildirilmez, hikâye akışta kalır', () => {
  const t = (id, src) => art(id, 'spor', '2026-11-06T11:40:00Z', { source_name: src, title: 'Galatasaray - Samsunspor maçı hangi kanalda, saat kaçta?' });
  const r = planNotifications({
    results: [{ addedArticles: [t('a', 'NTV'), t('b', 'AA'), t('c', 'TRT'), t('d', 'Habertürk')] }],
    categories: cats, state: {}, now: NOW,
  });
  assert.equal(r.messages.filter((m) => /'calm'/.test(m.condition || '')).length, 0);
  assert.equal(r.nextState.stories.length, 1, 'hikâye yine kaydedilir (akış ve gündem için)');
});

test('gece 23:59–08:00: yalnız can güvenliği gider, diğerleri sabahı bekler', () => {
  const night = Date.parse('2026-11-06T21:30:00Z'); // İstanbul 00:30
  const quake = (id, src) => art(id, 'son-dakika', '2026-11-06T21:25:00Z', { source_name: src, title: 'Kahramanmaraş\'ta 5.4 büyüklüğünde deprem' });
  const rQuake = planNotifications({
    results: [{ breakingArticles: [quake('q1', 'AA')] }],
    categories: cats, state: {}, now: night,
  });
  const sentQuake = rQuake.messages.filter((m) => /'calm'/.test(m.condition || ''));
  assert.equal(sentQuake.length, 1, 'deprem gece de gider');
  assert.equal(sentQuake[0].android.notification.channelId, 'breaking_news', 'sessiz kanala düşmez');

  const normal = (id, src) => art(id, 'gundem', '2026-11-06T21:25:00Z', { source_name: src, title: 'Merkez Bankası faiz kararını açıkladı' });
  const rNormal = planNotifications({
    results: [{ addedArticles: [normal('n1', 'AA'), normal('n2', 'NTV'), normal('n3', 'TRT')] }],
    categories: cats, state: {}, now: night,
  });
  assert.equal(rNormal.messages.length, 0, 'gece başka hiçbir bildirim gitmez');
  // Sabah: gece bekleyen haber artık eskidiği için ANİ bildirim olarak gitmez; "5 dakikada gündem"e girer.
  const morning = Date.parse('2026-11-07T06:30:00Z'); // 09:30
  const rMorning = planNotifications({ results: [], categories: cats, state: rNormal.nextState, now: morning });
  assert.equal(rMorning.messages.filter((m) => /'calm'/.test(m.condition || '')).length, 0, 'bayat haber sabah push olmaz');
  assert.ok(rMorning.nextState.stories.some((x) => /Merkez Bankası/.test(x.title)), 'hikâye korunur (gündem özetine girer)');
});

test('tekrar: aynı olay yeni gelişmeyle (ölü sayısı) ve kaynak artışıyla bir kez daha bildirilir', () => {
  const t = (id, src, title, min) => art(id, 'gundem', `2026-11-06T11:${min}:00Z`, { source_name: src, title });
  const first = planNotifications({
    results: [{ addedArticles: [
      t('a', 'NTV', 'Fabrikada patlama meydana geldi', 40),
      t('b', 'AA', 'Fabrikada patlama: ekipler sevk edildi', 41),
      t('c', 'TRT', 'Fabrikadaki patlamanın ardından inceleme', 42),
    ] }],
    categories: cats, state: {}, now: NOW,
  });
  assert.equal(first.messages.filter((m) => /'calm'/.test(m.condition || '')).length, 1);

  // 30 dk sonra yeni kaynak eklense de tekrar YOK (gelişme yok)
  const soon = planNotifications({
    results: [{ addedArticles: [t('d', 'Sözcü', 'Fabrikada patlama haberi', 45)] }],
    categories: cats, state: first.nextState, now: NOW + 30 * 60_000,
  });
  assert.equal(soon.messages.filter((m) => /'calm'/.test(m.condition || '')).length, 0);

  // 2 saat sonra: ölüm haberi + 3 yeni kaynak → tekrar bildirim
  const later = NOW + 2 * 3_600_000; // 14:00
  const t2 = (id, src, title, min) => art(id, 'gundem', `2026-11-06T13:${min}:00Z`, { source_name: src, title });
  const dev = planNotifications({
    results: [{ addedArticles: [
      t2('e', 'Habertürk', 'Fabrikadaki patlamada 2 kişi hayatını kaybetti', 50),
      t2('f', 'CNN Türk', 'Fabrika patlamasında can kaybı', 51),
      t2('g', 'Milliyet', 'Patlamanın olduğu fabrikada ölü sayısı açıklandı', 52),
    ] }],
    categories: cats, state: soon.nextState, now: later,
  });
  const again = dev.messages.filter((m) => /'calm'/.test(m.condition || ''));
  assert.equal(again.length, 1, 'gerçek gelişme → tekrar bildirim');
  assert.match(dev.nextState.last_pick.why, /gelişme/, 'tekrarın gerekçesi gerçek bir gelişme olmalı');
});
