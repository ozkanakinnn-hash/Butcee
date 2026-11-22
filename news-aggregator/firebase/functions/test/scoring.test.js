'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { scoreStory, developmentKeys, newDevelopments, shouldRenotify, LEVEL } = require('../src/scoring');

const NOW = Date.parse('2026-11-23T12:00:00Z');
/** Hikâye kurucusu: başlık, kaynak sayısı, özet, yaş (dk), son dakika kaynağı. */
const story = (title, srcs = 1, { sum = '', ageMin = 10, br = false, ...rest } = {}) => ({
  title,
  sum,
  srcs: Array.from({ length: srcs }, (_, i) => `Kaynak${i}`),
  first_ms: NOW - ageMin * 60_000,
  last_ms: NOW - ageMin * 60_000,
  br,
  ...rest,
});

// ── Seviye 1: can güvenliği (gece dahil) ────────────────────────────────────
test('seviye 1 (gece dahil): deprem, savaş, saldırı tek kaynakla bile kritik', () => {
  for (const t of [
    'Elazığ\'da 5.2 büyüklüğünde deprem meydana geldi',
    'Sınır hattında savaş ilan edildi',
    'İstanbul\'da bombalı saldırı düzenlendi',
    'Orman yangını nedeniyle köyler tahliye edildi',
  ]) {
    const r = scoreStory(story(t, 2), { now: NOW });
    assert.equal(r.level, LEVEL.CRITICAL, t);
    assert.equal(r.night, true, `${t} gece de gitmeli`);
  }
});

test('gece: alıntı/yorum başlığı uyandırmaz (gerçek veride görülen yanlış pozitifler)', () => {
  const quote = story('İran Cumhurbaşkanı Pezeşkiyan: Savaş halindeki hiçbir ülkede kalkınma olmaz', 2);
  const r1 = scoreStory(quote, { now: NOW });
  assert.equal(r1.night, false, 'alıntı başlığı gece gitmemeli');
  const far = story('Filistin Kızılayı: Gazze\'den 23 hasta Refah Sınır Kapısı üzerinden tahliye edildi', 2);
  assert.equal(scoreStory(far, { now: NOW }).night, false);
  const opinion = story('Uzman uyardı: Beklenen deprem için hazırlık şart', 3);
  assert.equal(scoreStory(opinion, { now: NOW }).night, false);
});

test('gece: gerçek olay bildirimi geçer; "Son dakika:" öneki alıntı sayılmaz', () => {
  assert.equal(scoreStory(story('Son dakika: İstanbul\'da 5.1 büyüklüğünde deprem meydana geldi', 1), { now: NOW }).night, true);
  assert.equal(scoreStory(story('Hatay\'da deprem! Bölge sarsıldı', 1), { now: NOW }).night, true);
  // Depremde tek kaynak yeter; diğer olaylarda ikinci kaynak ya da son dakika kaynağı aranır
  assert.equal(scoreStory(story('Ankara\'da bombalı saldırı düzenlendi', 1), { now: NOW }).night, false);
  assert.equal(scoreStory(story('Ankara\'da bombalı saldırı düzenlendi', 2), { now: NOW }).night, true);
  assert.equal(scoreStory(story('Ankara\'da bombalı saldırı düzenlendi', 1, { br: true }), { now: NOW }).night, true);
});

test('küçük deprem gece bildirimi için kritik sayılmaz ama normal kalır', () => {
  const r = scoreStory(story('Muğla açıklarında 3.1 büyüklüğünde deprem', 1), { now: NOW });
  assert.notEqual(r.night, true);
});

// ── Seviye 1: büyük gelişme (gündüz) ────────────────────────────────────────
test('seviye 1 (gündüz): ekonomik/siyasi büyük karar 2 kaynakla anında, gece beklemeli', () => {
  for (const t of [
    'Merkez Bankası faiz kararını açıkladı',
    'Asgari ücret zammı belli oldu',
    'Kabine değişikliği kararı Resmi Gazete\'de',
    'Maden kazasında işçiler mahsur kaldı',
  ]) {
    const r = scoreStory(story(t, 2), { now: NOW });
    assert.equal(r.level, LEVEL.CRITICAL, t);
    assert.equal(r.night, false, `${t} gece gitmemeli`);
  }
});

test('büyük gelişme tek kaynaklıysa seviye 1 olmaz (doğrulama bekler)', () => {
  const r = scoreStory(story('Asgari ücret zammı belli oldu', 1), { now: NOW });
  assert.notEqual(r.level, LEVEL.CRITICAL);
});

test('son dakika kaynağından gelen büyük gelişme tek kaynakla da seviye 1', () => {
  const r = scoreStory(story('Faiz kararı açıklandı', 1, { br: true }), { now: NOW });
  assert.equal(r.level, LEVEL.CRITICAL);
});

test('5+ yayıncı aynı olayı veriyorsa konusu ne olursa olsun seviye 1', () => {
  const r = scoreStory(story('Ünlü yönetmen yeni filmini duyurdu', 5), { now: NOW });
  assert.equal(r.level, LEVEL.CRITICAL);
  assert.match(r.reason, /5 kaynak/);
});

// ── Seviye 2 ────────────────────────────────────────────────────────────────
test('seviye 2: 3 kaynak ya da 2 kaynak + önemli kalıp', () => {
  assert.equal(scoreStory(story('Belediyeden yeni ulaşım düzenlemesi', 3), { now: NOW }).level, LEVEL.IMPORTANT);
  assert.equal(scoreStory(story('Enflasyon verisi açıklandı', 2), { now: NOW }).level, LEVEL.IMPORTANT);
  assert.equal(scoreStory(story('Galatasaray transfer anlaşmasını duyurdu', 2), { now: NOW }).level, LEVEL.IMPORTANT);
});

test('özet metni de puanlamaya girer (başlık yetersizken)', () => {
  const s = story('Bakanlıktan yeni adım', 2, { sum: 'Soruşturma başlatıldı ve üç kişi gözaltına alındı.' });
  assert.equal(scoreStory(s, { now: NOW }).level, LEVEL.IMPORTANT);
});

// ── Seviye 3 ve 4 ───────────────────────────────────────────────────────────
test('seviye 3: iki kaynaklı sıradan haber yalnızca özette', () => {
  assert.equal(scoreStory(story('Şehirde yeni park açıldı', 2), { now: NOW }).level, LEVEL.NORMAL);
});

test('seviye 4: tıklama tuzağı, rutin/servis içeriği ve eski tek kaynak bildirilmez', () => {
  assert.equal(scoreStory(story('Görenler şaşırdı! İşte o an', 4), { now: NOW }).level, LEVEL.LOW);
  assert.equal(scoreStory(story('Galatasaray - Samsunspor maçı hangi kanalda, saat kaçta?', 6), { now: NOW }).level, LEVEL.LOW);
  assert.equal(scoreStory(story('Muhtemel 11\'ler belli oldu', 5), { now: NOW }).level, LEVEL.LOW);
  assert.equal(scoreStory(story('Bugün hava durumu nasıl olacak', 3), { now: NOW }).level, LEVEL.LOW);
  assert.equal(scoreStory(story('Mercimek çorbası tarifi', 3), { now: NOW }).level, LEVEL.LOW);
  assert.equal(scoreStory(story('Küçük bir gelişme oldu', 1, { ageMin: 400 }), { now: NOW }).level, LEVEL.LOW);
});

test('rutin kalıp kritik haberi bastırmaz (deprem sonrası "ne zaman" başlığı hariç)', () => {
  // "saat kaçta" rutin; ama can güvenliği kalıbı rutinden ÖNCE değil SONRA değerlendirilir → rutin kazanır.
  // Bu bilinçli: "Deprem ne zaman olacak" gibi spekülasyon bildirimi istenmiyor.
  assert.equal(scoreStory(story('Beklenen İstanbul depremi ne zaman olacak?', 3), { now: NOW }).level, LEVEL.LOW);
  // Gerçek olay bildirimi etkilenmez
  assert.equal(scoreStory(story('İstanbul\'da 4.9 büyüklüğünde deprem meydana geldi', 3), { now: NOW }).level, LEVEL.CRITICAL);
});

test('eskiyen haber üst seviyeye çıkmaz', () => {
  const r = scoreStory(story('Merkez Bankası faiz kararını açıkladı', 4, { ageMin: 300 }), { now: NOW });
  assert.equal(r.level, LEVEL.NORMAL);
});

test('süren olay yeni haber geldikçe canlı kalır; 12 saati aşınca düşer', () => {
  const live = { ...story('Fabrikada patlama', 4, { ageMin: 300 }), last_ms: NOW - 20 * 60_000 };
  assert.equal(scoreStory(live, { now: NOW }).level, LEVEL.CRITICAL, 'son gelişme 20 dk önce → hâlâ gündem');
  const tooOld = { ...live, first_ms: NOW - 20 * 3_600_000 };
  assert.equal(scoreStory(tooOld, { now: NOW }).level, LEVEL.NORMAL, '20 saat önce başlayan olay artık son dakika değil');
});

// ── Gelişme tespiti ─────────────────────────────────────────────────────────
test('gelişme anahtarları başlık ve özetten çıkarılır', () => {
  assert.deepEqual(developmentKeys('Kazada 3 kişi hayatını kaybetti'), ['olum']);
  assert.deepEqual(developmentKeys('Şüpheli gözaltına alındı', 'Tutuklandı'), ['gozalti', 'tutuklama']);
  assert.deepEqual(developmentKeys('Normal bir haber'), []);
});

test('yeni gelişme: hikâyede daha önce görülmemiş anahtarlar', () => {
  const s = { dev: ['yarali'] };
  assert.deepEqual(newDevelopments(s, 'Yaralı sayısı açıklandı'), []);
  assert.deepEqual(newDevelopments(s, 'Olay yerinde bir kişi hayatını kaybetti'), ['olum']);
});

// ── Tekrar bildirimi ────────────────────────────────────────────────────────
const sent = (extra) => ({ ...story('Fabrikada patlama', 4), notified_ms: NOW - 2 * 3_600_000, notified_level: LEVEL.IMPORTANT, notified_srcs: 4, notify_count: 1, ...extra });

test('tekrar: gelişme yoksa ikinci bildirim gitmez', () => {
  const s = sent();
  assert.equal(shouldRenotify(s, { level: LEVEL.IMPORTANT }, { now: NOW }).ok, false);
});

test('tekrar: yeni gelişme + kaynak artışı varsa gider', () => {
  const s = sent({ pending_dev: ['olum'], srcs: ['a', 'b', 'c', 'd', 'e', 'f', 'g'] });
  const r = shouldRenotify(s, { level: LEVEL.IMPORTANT }, { now: NOW });
  assert.equal(r.ok, true);
  assert.equal(r.reason, 'yeni gelişme');
});

test('tekrar: kritik seviyeye yükselen hikâye gelişme ile hemen gider', () => {
  const s = sent({ pending_dev: ['olum'] });
  assert.equal(shouldRenotify(s, { level: LEVEL.CRITICAL }, { now: NOW }).ok, true);
});

test('tekrar: 90 dakika dolmadan gitmez', () => {
  const s = sent({ notified_ms: NOW - 30 * 60_000, pending_dev: ['olum'], srcs: ['a', 'b', 'c', 'd', 'e', 'f', 'g'] });
  assert.equal(shouldRenotify(s, { level: LEVEL.CRITICAL }, { now: NOW }).reason, 'çok erken');
});

test('tekrar: hikâye başına en fazla 2 tekrar', () => {
  const s = sent({ notify_count: 3, pending_dev: ['olum'], srcs: ['a', 'b', 'c', 'd', 'e', 'f', 'g'] });
  assert.equal(shouldRenotify(s, { level: LEVEL.CRITICAL }, { now: NOW }).reason, 'tekrar sınırı');
});

test('tekrar: hiç bildirilmemiş hikâye her zaman gönderilebilir', () => {
  assert.equal(shouldRenotify(story('x', 3), { level: LEVEL.CRITICAL }, { now: NOW }).ok, true);
});

test('tekrar: eski major_ms alanı da son bildirim sayılır (geriye uyum)', () => {
  const s = { ...story('x', 3), major_ms: NOW - 10 * 60_000 };
  assert.equal(shouldRenotify(s, { level: LEVEL.CRITICAL }, { now: NOW }).ok, false);
});
