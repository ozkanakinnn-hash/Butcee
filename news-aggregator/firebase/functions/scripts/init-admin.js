'use strict';

/**
 * Admin SDK'yı başlatır. Proje kimliği sırasıyla GCLOUD_PROJECT ortam değişkeninden
 * veya GOOGLE_APPLICATION_CREDENTIALS ile verilen hizmet hesabı JSON'undaki project_id'den alınır.
 *
 * Tablet/telefonda kopyalanan anahtar sık bozulur:
 *   a) private_key içindeki "\n" kaçışları gerçek satır sonuna dönüşür,
 *   b) metin düzenleyici uzun satırları ekrana sığdırmak için rastgele yerlerden böler.
 * Her iki durum da onarılmaya çalışılır. Olmazsa anahtarın İÇERİĞİNİ GÖSTERMEDEN teşhis verilir.
 */
const crypto = require('node:crypto');
const fs = require('node:fs');
const { initializeApp, cert } = require('firebase-admin/app');

/** Anahtar içeriğini sızdırmadan teşhis bilgisi üretir. */
function describeKeyText(text) {
  const t = text.trim();
  return [
    `uzunluk: ${t.length} karakter (normalde ~2300)`,
    `'{' ile başlıyor: ${t.startsWith('{') ? 'evet' : 'HAYIR'}`,
    `'}' ile bitiyor: ${t.endsWith('}') ? 'evet' : 'HAYIR'}`,
    `satır sayısı: ${t.split(/\r?\n/).length} (normalde 13)`,
    `"private_key" alanı: ${t.includes('"private_key"') ? 'var' : 'YOK'}`,
    `"client_email" alanı: ${t.includes('"client_email"') ? 'var' : 'YOK'}`,
    `akıllı tırnak (“ ”): ${/[“”]/.test(t) ? 'VAR — düz tırnak (") olmalı' : 'yok'}`,
  ].join('\n  - ');
}

/**
 * JSON dizgeleri İÇİNDEKİ ham satır sonlarını işler (dizge dışındakiler zaten geçerli boşluktur).
 * mode 'escape': satır sonunu "\n" kaçışına çevirir (durum a).
 * mode 'remove': satır sonunu siler (durum b — sarma). Kaçışın ortasından bölünmüş "\" + satır sonu + "n" de onarılır.
 */
function fixNewlinesInStrings(text, mode) {
  let out = '';
  let inString = false;
  let escaped = false;
  for (const ch of text) {
    const isBreak = ch === '\n' || ch === '\r';
    if (!inString) {
      if (ch === '"') inString = true;
      out += ch;
      continue;
    }
    if (isBreak) {
      if (mode === 'escape' && ch === '\n' && !escaped) out += '\\n';
      continue; // 'remove' modunda ve \r'de satır sonu atlanır; kaçış durumu korunur
    }
    if (escaped) { escaped = false; out += ch; continue; }
    if (ch === '\\') { escaped = true; out += ch; continue; }
    if (ch === '"') { inString = false; out += ch; continue; }
    if (ch === '\t') { out += '\\t'; continue; }
    out += ch;
  }
  return out;
}

/** Geriye uyumluluk (eski ad). */
const escapeNewlinesInStrings = (text) => fixNewlinesInStrings(text, 'escape');

/**
 * private_key'i baştan kurar: BEGIN/END arasındaki base64 karakterlerini toplayıp 64'lük satırlara böler.
 * Böylece fazladan/eksik satır sonları ve "\n" kalıntıları anahtarı bozmaz. Geçersizse null döner.
 */
function normalizePrivateKey(raw) {
  if (typeof raw !== 'string') return null;
  const m = /-----BEGIN ([A-Z ]+)-----([\s\S]*?)-----END \1-----/.exec(raw.replace(/\\n/g, '\n'));
  if (!m) return null;
  const body = m[2].replace(/[^A-Za-z0-9+/=]/g, '');
  const lines = body.match(/.{1,64}/g) || [];
  const pem = `-----BEGIN ${m[1]}-----\n${lines.join('\n')}\n-----END ${m[1]}-----\n`;
  try {
    crypto.createPrivateKey(pem);
    return pem;
  } catch {
    return null;
  }
}

/** Hata türünü, metinden parça içermeden Türkçe açıklar. */
function describeParseError(err) {
  const m = err.message;
  if (/control character/i.test(m)) return 'metin içinde izin verilmeyen satır sonu/kontrol karakteri';
  if (/end of (json|data)|unterminated/i.test(m)) return 'metin yarıda kesilmiş';
  if (/unexpected token|unexpected character|expected/i.test(m)) return 'beklenmeyen karakter';
  return 'bilinmeyen biçim hatası';
}

function errorPosition(err) {
  const pos = /position (\d+)/.exec(err.message);
  return pos ? `${pos[1]}. karakterde: ` : '';
}

const isServiceAccount = (j) =>
  j && j.type === 'service_account' && j.private_key && j.client_email && j.project_id;

/** Hizmet hesabı dosyasını okur ve gerekirse onarır. */
function readServiceAccount(keyFile) {
  const text = fs.readFileSync(keyFile, 'utf8').replace(/^﻿/, '').trim();

  const attempts = [
    ['olduğu gibi', text],
    ['satır sonları kaçışlandı', fixNewlinesInStrings(text, 'escape')],
    ['sarma satır sonları silindi', fixNewlinesInStrings(text, 'remove')],
  ];

  const failures = [];
  for (const [label, candidate] of attempts) {
    let json;
    try {
      json = JSON.parse(candidate);
    } catch (err) {
      // JSON.parse mesajı metinden parça içerebilir; yalnızca konum ve tür raporlanır.
      failures.push(`${label} → ${errorPosition(err)}${describeParseError(err)}`);
      continue;
    }
    // private_key dışındaki alanlarda (e-posta, kimlik, adresler) satır sonu olamaz: sarma kalıntılarını sil.
    for (const [k, v] of Object.entries(json)) {
      if (k !== 'private_key' && typeof v === 'string') json[k] = v.replace(/\\?[\r\n]+/g, '').trim();
    }
    if (!isServiceAccount(json)) {
      failures.push(`${label} → JSON okundu ama type/private_key/client_email/project_id eksik`);
      continue;
    }
    const pem = normalizePrivateKey(json.private_key);
    if (!pem) {
      failures.push(`${label} → JSON okundu ama private_key geçerli bir anahtar değil (eksik/bozuk kopya)`);
      continue;
    }
    if (label !== 'olduğu gibi' || pem !== json.private_key || candidate !== text) {
      console.warn(`Uyarı: anahtar metni onarıldı (${label}). Kopyalama sırasında bozulmuş.`);
    }
    return { ...json, private_key: pem };
  }

  throw new Error(
    'FIREBASE_SERVICE_ACCOUNT okunamadı.\n  Denemeler:\n    * ' +
      failures.join('\n    * ') +
      '\n  Metin özeti:\n  - ' +
      describeKeyText(text) +
      '\nÇözüm: Firebase\'den yeni bir anahtar (.json) indirip içeriğinin TAMAMINI GitHub secret\'ına yapıştırın.'
  );
}

function initAdmin() {
  const keyFile = process.env.GOOGLE_APPLICATION_CREDENTIALS;
  if (keyFile && fs.existsSync(keyFile)) {
    const sa = readServiceAccount(keyFile);
    return initializeApp({ credential: cert(sa), projectId: process.env.GCLOUD_PROJECT || sa.project_id });
  }
  const projectId = process.env.GCLOUD_PROJECT;
  return initializeApp(projectId ? { projectId } : undefined);
}

module.exports = {
  initAdmin,
  readServiceAccount,
  describeKeyText,
  fixNewlinesInStrings,
  escapeNewlinesInStrings,
  normalizePrivateKey,
};
