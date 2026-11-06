'use strict';

/**
 * Metin temizleme, URL normalleştirme, görsel bulma ve hash yardımcıları.
 * Hepsi saf (yan etkisiz) fonksiyonlardır; test/text.test.js ile test edilir.
 */

const crypto = require('node:crypto');
const { decode } = require('html-entities');

const SUMMARY_MAX_LENGTH = 300;

/** Takip parametreleri: aynı haberin farklı URL'lerle iki kez kaydedilmesini önlemek için silinir. */
const TRACKING_PARAMS = [
  /^utm_/i,
  /^fbclid$/i,
  /^gclid$/i,
  /^yclid$/i,
  /^mc_(cid|eid)$/i,
  /^ref(_src)?$/i,
  /^cmpid$/i,
  /^ito$/i,
];

/**
 * RSS özetindeki HTML'i sade metne çevirir.
 * <p>, <br>, <img>, <script> vb. etiketleri siler, &amp; gibi HTML varlıklarını çözer,
 * bozuk/kontrol karakterlerini atar ve boşlukları tekilleştirir.
 */
function stripHtml(input) {
  if (input == null) return '';
  let s = String(input);

  // CDATA sarmalayıcısı bazen metnin içinde kalır.
  s = s.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, '$1');
  // Bazı kaynaklar HTML'i kaçışlı (&lt;p&gt;) gönderir: önce bir kez çöz.
  if (/&lt;\/?[a-z][\s\S]*?&gt;/i.test(s)) s = decode(s);

  s = s
    .replace(/<(script|style|iframe|noscript)[^>]*>[\s\S]*?<\/\1>/gi, ' ')
    .replace(/<!--[\s\S]*?-->/g, ' ')
    .replace(/<br\s*\/?>/gi, ' ')
    .replace(/<\/(p|div|li|h[1-6]|tr|blockquote)>/gi, ' ')
    .replace(/<[^>]+>/g, ' ');

  // Varlıkları çöz; "&amp;quot;" gibi çift kodlanmış metinler için iki tur.
  s = decode(decode(s));

  s = s
    // U+FFFD (�) ve kontrol karakterleri — bozuk kodlamanın izleri.
    .replace(/[�\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F-\u009F​-‍﻿]/g, '')
    .replace(/\s+/g, ' ')
    .trim();

  return s;
}

/** Metni kelime sınırında keser ve sonuna "…" ekler. */
function truncate(text, max = SUMMARY_MAX_LENGTH) {
  if (!text || text.length <= max) return text || '';
  const cut = text.slice(0, max);
  const lastSpace = cut.lastIndexOf(' ');
  const base = lastSpace > max * 0.6 ? cut.slice(0, lastSpace) : cut;
  return base.replace(/[\s.,;:!?\-–—]+$/u, '') + '…';
}

/** Başlıktaki HTML/varlıkları temizler (başlıklar kesilmez). */
function cleanTitle(title) {
  return stripHtml(title);
}

/**
 * URL'yi karşılaştırılabilir hale getirir: boşlukları kırpar, http→https yükseltir,
 * #fragment ve utm_* gibi takip parametrelerini siler. Geçersizse null döner.
 */
function normalizeUrl(raw) {
  if (!raw || typeof raw !== 'string') return null;
  let url;
  try {
    url = new URL(raw.trim());
  } catch {
    return null;
  }
  if (url.protocol !== 'http:' && url.protocol !== 'https:') return null;
  url.hash = '';
  for (const key of [...url.searchParams.keys()]) {
    if (TRACKING_PARAMS.some((re) => re.test(key))) url.searchParams.delete(key);
  }
  url.hostname = url.hostname.toLowerCase();
  return url.toString();
}

/**
 * Görsel adresini uygulamada güvenle gösterilebilir yapar.
 * Android varsayılan olarak şifresiz (http) trafiği engellediği için https'e yükseltilir.
 */
function normalizeImageUrl(raw, baseUrl) {
  if (!raw || typeof raw !== 'string') return null;
  let url;
  try {
    url = new URL(decode(raw.trim()), baseUrl || undefined);
  } catch {
    return null;
  }
  if (url.protocol === 'http:') url.protocol = 'https:';
  if (url.protocol !== 'https:') return null;
  return url.toString();
}

const IMAGE_EXT = /\.(jpe?g|png|webp|gif|avif)(\?|$)/i;

function firstAttr(value, attr = 'url') {
  const list = Array.isArray(value) ? value : value ? [value] : [];
  for (const entry of list) {
    if (!entry) continue;
    if (typeof entry === 'string') return entry;
    const attrs = entry.$ || entry;
    if (attrs && typeof attrs[attr] === 'string') {
      const medium = attrs.medium || '';
      const type = attrs.type || '';
      if (medium && medium !== 'image') continue;
      if (type && !type.startsWith('image/')) continue;
      return attrs[attr];
    }
  }
  return null;
}

/**
 * RSS öğesinden kapak görseli bulur. Sıra:
 * enclosure → media:content → media:thumbnail → itunes:image → <image> → içerikteki ilk <img>.
 */
function extractImage(item, baseUrl) {
  const candidates = [];

  const enc = item.enclosure;
  if (enc && enc.url && ((enc.type && enc.type.startsWith('image/')) || IMAGE_EXT.test(enc.url))) {
    candidates.push(enc.url);
  }
  candidates.push(firstAttr(item.mediaContent));
  candidates.push(firstAttr(item.mediaThumbnail));
  if (item.itunes && item.itunes.image) candidates.push(item.itunes.image);
  if (item.image) {
    const img = item.image;
    candidates.push(typeof img === 'string' ? img : img.url || (img.$ && img.$.href) || null);
  }

  for (const html of [item.contentEncoded, item.content, item.description, item.summary]) {
    if (typeof html !== 'string') continue;
    const m = html.match(/<img[^>]+src\s*=\s*["']([^"']+)["']/i)
      || decode(html).match(/<img[^>]+src\s*=\s*["']([^"']+)["']/i);
    if (m) candidates.push(m[1]);
  }

  for (const c of candidates) {
    const url = normalizeImageUrl(c, baseUrl);
    if (url) return url;
  }
  return null;
}

function sha256(text) {
  return crypto.createHash('sha256').update(text, 'utf8').digest('hex');
}

/** Firestore belge kimliği: normalleştirilmiş URL'nin hash'i (aynı URL = aynı belge). */
function articleIdForUrl(normalizedUrl) {
  return sha256(normalizedUrl).slice(0, 40);
}

/**
 * content_hash = hash(küçük harf başlık + yayın günü).
 * Aynı haber farklı URL ile (ör. mobil/masaüstü adresi) gelse bile yakalanır.
 */
function contentHash(title, publishedAt) {
  const normalizedTitle = title
    .toLocaleLowerCase('tr-TR')
    .replace(/[^\p{L}\p{N}]+/gu, ' ')
    .trim();
  const day = publishedAt.toISOString().slice(0, 10);
  return sha256(`${normalizedTitle}|${day}`);
}

/**
 * Yayın tarihini çözer. Geçersiz veya 10 dakikadan fazla ileri tarihli ise "şimdi" kullanılır
 * (bazı kaynaklar saat dilimini yanlış yazar; haber listenin en üstüne yapışmasın).
 */
function parsePublishedAt(item, now = new Date()) {
  const raw = item.isoDate || item.pubDate || item.published || item.updated || item['dc:date'];
  const d = raw ? new Date(raw) : null;
  if (!d || Number.isNaN(d.getTime())) return now;
  if (d.getTime() - now.getTime() > 10 * 60 * 1000) return now;
  return d;
}

/**
 * Arama için metni sadeleştirir: Türkçe büyük/küçük harf, aksan katlama (ç→c, ğ→g, ı/İ→i, ö→o, ş→s, ü→u),
 * harf/rakam dışını boşluğa çevirir. Uygulamadaki Dart karşılığı (lib/utils/search_text.dart) ile AYNI olmalı.
 */
function foldForSearch(text) {
  return String(text || '')
    .replace(/İ/g, 'i')
    .replace(/I/g, 'i')
    .toLowerCase()
    .replace(/[çğıöşüâîû]/g, (c) => ({ ç: 'c', ğ: 'g', ı: 'i', ö: 'o', ş: 's', ü: 'u', â: 'a', î: 'i', û: 'u' })[c])
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

const SEARCH_PREFIX_MAX = 10;
const SEARCH_KEYWORDS_MAX = 150;

/**
 * Başlıktan arama anahtarları: her kelimenin 3..10 harflik ön ekleri ("galatasaray" → gal, gala, … galatasara).
 * Uygulama ilk kelimenin (en fazla 10 harf) ön ekiyle `array-contains` sorgusu yapar, kalan kelimeleri
 * istemcide süzer. Firestore'da tam metin arama olmadığı için bu yöntem kullanılır.
 */
function searchKeywords(...texts) {
  const out = new Set();
  for (const word of foldForSearch(texts.join(' ')).split(' ')) {
    if (word.length < 3) continue;
    for (let n = 3; n <= Math.min(word.length, SEARCH_PREFIX_MAX); n++) {
      out.add(word.slice(0, n));
      if (out.size >= SEARCH_KEYWORDS_MAX) return [...out];
    }
  }
  return [...out];
}

module.exports = {
  foldForSearch,
  searchKeywords,
  SEARCH_PREFIX_MAX,
  SUMMARY_MAX_LENGTH,
  stripHtml,
  truncate,
  cleanTitle,
  normalizeUrl,
  normalizeImageUrl,
  extractImage,
  articleIdForUrl,
  contentHash,
  parsePublishedAt,
  sha256,
};
