'use strict';

/**
 * RSS/Atom akışını indirir, karakter kodlamasını doğru çözer ve ayrıştırır.
 *
 * Türk haber sitelerinin bir kısmı hâlâ windows-1254 / ISO-8859-9 kullanır. Akış doğrudan
 * UTF-8 sanılarak okunursa "ş, ğ, ı" harfleri "�" olur. Bu yüzden ham baytları alıp
 * Content-Type başlığındaki veya <?xml encoding="..."?> bildirimindeki kodlamayla çözüyoruz.
 */

const Parser = require('rss-parser');

const FETCH_TIMEOUT_MS = 15_000;
const MAX_FEED_BYTES = 5 * 1024 * 1024; // 5 MB üstü akışlar reddedilir.
const USER_AGENT = 'Mozilla/5.0 (compatible; HaberToplayiciBot/1.0; RSS reader)';

const parser = new Parser({
  timeout: FETCH_TIMEOUT_MS,
  customFields: {
    item: [
      ['media:content', 'mediaContent', { keepArray: true }],
      ['media:thumbnail', 'mediaThumbnail', { keepArray: true }],
      ['content:encoded', 'contentEncoded'],
      ['dc:date', 'dc:date'],
      'image',
      'summary',
      'published',
      'updated',
    ],
  },
});

/** Content-Type başlığından veya XML bildiriminden kodlamayı bulur. */
function detectCharset(contentType, bytes) {
  const fromHeader = /charset\s*=\s*["']?([\w-]+)/i.exec(contentType || '');
  if (fromHeader) return fromHeader[1].toLowerCase();
  const head = Buffer.from(bytes.subarray(0, 300)).toString('latin1');
  const fromDecl = /<\?xml[^>]*encoding\s*=\s*["']([\w-]+)["']/i.exec(head);
  if (fromDecl) return fromDecl[1].toLowerCase();
  return 'utf-8';
}

/** Baytları metne çevirir. Tanınmayan kodlamada UTF-8'e düşer. */
function decodeBytes(bytes, charset) {
  let decoder;
  try {
    decoder = new TextDecoder(charset);
  } catch {
    decoder = new TextDecoder('utf-8');
  }
  return decoder.decode(bytes).replace(/^﻿/, '');
}

/**
 * Akışı indirir. Kaynak dokümanında saklanan ETag / Last-Modified gönderilir; site
 * "304 Not Modified" derse { notModified: true } döner ve hiç ayrıştırma yapılmaz.
 *
 * @returns {Promise<{notModified: boolean, feed?: object, etag?: string|null, lastModified?: string|null}>}
 */
async function fetchFeed(url, { etag, lastModified, fetchImpl = fetch } = {}) {
  const headers = {
    'User-Agent': USER_AGENT,
    Accept: 'application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8, */*;q=0.5',
  };
  if (etag) headers['If-None-Match'] = etag;
  if (lastModified) headers['If-Modified-Since'] = lastModified;

  const res = await fetchImpl(url, {
    headers,
    redirect: 'follow',
    signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
  });

  if (res.status === 304) return { notModified: true };
  if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText || ''}`.trim());

  const declaredLength = Number(res.headers.get('content-length') || 0);
  if (declaredLength > MAX_FEED_BYTES) throw new Error(`Akış çok büyük (${declaredLength} bayt)`);

  const bytes = new Uint8Array(await res.arrayBuffer());
  if (bytes.byteLength > MAX_FEED_BYTES) throw new Error(`Akış çok büyük (${bytes.byteLength} bayt)`);

  const charset = detectCharset(res.headers.get('content-type'), bytes);
  const xml = decodeBytes(bytes, charset);
  const feed = await parseFeedXml(xml);

  return {
    notModified: false,
    feed,
    etag: res.headers.get('etag'),
    lastModified: res.headers.get('last-modified'),
  };
}

/** XML metnini ayrıştırır (testlerde doğrudan kullanılır). */
async function parseFeedXml(xml) {
  // rss-parser metindeki encoding bildirimine bakmaz ama tutarlılık için UTF-8 yapıyoruz.
  const normalized = xml.replace(/(<\?xml[^>]*encoding\s*=\s*["'])[\w-]+(["'])/i, '$1utf-8$2');
  return parser.parseString(normalized);
}

module.exports = { fetchFeed, parseFeedXml, detectCharset, decodeBytes };
