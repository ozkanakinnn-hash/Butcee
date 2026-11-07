'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { fetchFeed, detectCharset } = require('../src/feed');
const { buildArticle, dedupeWithinBatch } = require('../src/ingest');

const fixture = fs.readFileSync(path.join(__dirname, 'fixtures', 'windows-1254.xml'));
const NOW = new Date('2026-11-06T12:00:00Z');

function fakeFetch(body, headers = {}, status = 200) {
  return async (url, init) => {
    fakeFetch.lastInit = init;
    return new Response(status === 304 ? null : body, { status, headers });
  };
}

test('detectCharset başlık ve XML bildirimini okur', () => {
  assert.equal(detectCharset('text/xml; charset=ISO-8859-9', new Uint8Array()), 'iso-8859-9');
  assert.equal(detectCharset('text/xml', fixture), 'windows-1254');
  assert.equal(detectCharset(null, Buffer.from('<rss>')), 'utf-8');
});

test('windows-1254 akışında Türkçe karakterler bozulmaz', async () => {
  const res = await fetchFeed('https://ornek-haber.com.tr/rss', { fetchImpl: fakeFetch(fixture, { 'content-type': 'text/xml' }) });
  assert.equal(res.feed.title, 'Örnek Haber Sitesi');
  assert.equal(res.feed.items[0].title, "İstanbul'da şiddetli yağış uyarısı: Okullar ğüşıöç tatil");
});

test('ETag gönderilir, 304 yanıtında ayrıştırma yapılmaz', async () => {
  const f = fakeFetch(null, {}, 304);
  const res = await fetchFeed('https://x.com/rss', { etag: '"abc"', fetchImpl: f });
  assert.equal(res.notModified, true);
  assert.equal(fakeFetch.lastInit.headers['If-None-Match'], '"abc"');
});

test('HTTP hatası istisna fırlatır', async () => {
  await assert.rejects(fetchFeed('https://x.com/rss', { fetchImpl: fakeFetch('yok', {}, 404) }), /HTTP 404/);
});

test('buildArticle + dedupe: temizlik, görsel, placeholder, eski/başlıksız ve mükerrer eleme', async () => {
  const { feed } = await fetchFeed('https://ornek-haber.com.tr/rss', { fetchImpl: fakeFetch(fixture) });
  const ctx = {
    source: { id: 'ornek', source_name: 'Örnek', logo_url: '' },
    categoryRef: null,
    placeholderImageUrl: 'https://cdn.example.com/placeholder.jpg',
    feedLink: feed.link,
    now: NOW,
  };
  const built = feed.items.map((it) => buildArticle(it, ctx)).filter(Boolean);
  // 7 öğe: eski haber ve başlıksız haber elenir → 5
  assert.equal(built.length, 5);

  const [first, borsa, resimsiz, borsaMobil, telefon] = built.map((b) => b.data);
  assert.equal(first.original_url, 'https://ornek-haber.com.tr/gundem/yagis-uyarisi');
  assert.equal(first.summary, 'Meteoroloji uyardı: "Yarın" için turuncu kod. Detaylar haberde.');
  assert.equal(first.image_url, 'https://ornek-haber.com.tr/img/yagis.jpg');
  assert.equal(first.has_original_image, true);
  assert.equal(first.published_at.toDate().toISOString(), '2026-11-05T05:30:00.000Z');

  assert.equal(borsa.summary, 'Endeks & dolar kuru');
  assert.equal(borsa.image_url, 'https://cdn.ornek-haber.com.tr/borsa.jpg');

  assert.equal(resimsiz.original_url, 'https://ornek-haber.com.tr/gundem/resimsiz');
  assert.equal(resimsiz.image_url, 'https://cdn.example.com/placeholder.jpg');
  assert.equal(resimsiz.has_original_image, false);

  assert.equal(telefon.image_url, 'https://cdn.ornek-haber.com.tr/telefon.webp');

  // Mobil adresli borsa haberi aynı başlık + aynı gün → content_hash aynı → elenir.
  assert.equal(borsa.content_hash, borsaMobil.content_hash);
  const unique = dedupeWithinBatch(built);
  assert.equal(unique.length, 4);
  assert.ok(!unique.some((a) => a.data.original_url.startsWith('https://m.')));
});
