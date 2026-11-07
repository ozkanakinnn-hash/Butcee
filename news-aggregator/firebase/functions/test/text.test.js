'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const t = require('../src/text');

test('stripHtml etiketleri, varlıkları ve bozuk karakterleri temizler', () => {
  const html = '<p><img src="a.jpg"/>Merhaba&nbsp;<b>dünya</b><br/>&quot;Alıntı&quot; &amp;amp; devam�</p><script>alert(1)</script>';
  assert.equal(t.stripHtml(html), 'Merhaba dünya "Alıntı" & devam');
});

test('stripHtml kaçışlı HTML ve CDATA ile çalışır', () => {
  assert.equal(t.stripHtml('&lt;p&gt;Kaçışlı &lt;b&gt;metin&lt;/b&gt;&lt;/p&gt;'), 'Kaçışlı metin');
  assert.equal(t.stripHtml('<![CDATA[<p>CDATA içi</p>]]>'), 'CDATA içi');
  assert.equal(t.stripHtml(null), '');
});

test('truncate kelime sınırında keser', () => {
  const text = 'kelime '.repeat(100).trim();
  const out = t.truncate(text, 50);
  assert.ok(out.length <= 51);
  assert.ok(out.endsWith('kelime…'));
  assert.equal(t.truncate('kısa', 50), 'kısa');
});

test('normalizeUrl takip parametrelerini ve fragment\'i siler', () => {
  assert.equal(
    t.normalizeUrl(' https://Site.com/haber?id=5&utm_source=rss&fbclid=x#yorum '),
    'https://site.com/haber?id=5'
  );
  assert.equal(t.normalizeUrl('javascript:alert(1)'), null);
  assert.equal(t.normalizeUrl('bozuk adres'), null);
});

test('extractImage öncelik sırasını izler ve http→https yükseltir', () => {
  assert.equal(
    t.extractImage({ enclosure: { url: 'http://a.com/x.jpg', type: 'image/jpeg' } }),
    'https://a.com/x.jpg'
  );
  assert.equal(
    t.extractImage({ mediaContent: [{ $: { url: 'https://a.com/v.mp4', medium: 'video' } }, { $: { url: 'https://a.com/m.jpg' } }] }),
    'https://a.com/m.jpg'
  );
  assert.equal(
    t.extractImage({ description: '<p><img src="/rel/img.png"></p>' }, 'https://site.com/haber/1'),
    'https://site.com/rel/img.png'
  );
  assert.equal(t.extractImage({ enclosure: { url: 'https://a.com/ses.mp3', type: 'audio/mpeg' } }), null);
});

test('contentHash büyük/küçük harf ve noktalamadan etkilenmez, gün bazlıdır', () => {
  const d1 = new Date('2026-11-05T08:00:00Z');
  const d2 = new Date('2026-11-05T09:30:00Z');
  assert.equal(t.contentHash('Borsa güne YÜKSELİŞLE başladı!', d1), t.contentHash('borsa güne yükselişle başladı', d2));
  assert.notEqual(t.contentHash('Borsa', d1), t.contentHash('Borsa', new Date('2026-11-06T09:00:00Z')));
});

test('parsePublishedAt geçersiz ve gelecek tarihlerde şimdiyi kullanır', () => {
  const now = new Date('2026-11-06T12:00:00Z');
  assert.equal(t.parsePublishedAt({ pubDate: 'saçma' }, now), now);
  assert.equal(t.parsePublishedAt({ isoDate: '2027-01-01T00:00:00Z' }, now), now);
  assert.equal(t.parsePublishedAt({ isoDate: '2026-11-06T10:00:00Z' }, now).toISOString(), '2026-11-06T10:00:00.000Z');
});
