'use strict';

/**
 * Admin SDK'yı başlatır. Proje kimliği sırasıyla GCLOUD_PROJECT ortam değişkeninden
 * veya GOOGLE_APPLICATION_CREDENTIALS ile verilen hizmet hesabı JSON'undaki project_id'den alınır.
 *
 * JSON bozuksa (ör. tablette kopyalarken eksik kalmışsa) anahtarın İÇERİĞİNİ GÖSTERMEDEN
 * neyin yanlış olduğunu anlatan bir hata verir.
 */
const fs = require('node:fs');
const { initializeApp, cert } = require('firebase-admin/app');

/** Anahtar içeriğini sızdırmadan teşhis bilgisi üretir. */
function describeKeyText(text) {
  const t = text.trim();
  return [
    `uzunluk: ${t.length} karakter (normalde ~2300)`,
    `'{' ile başlıyor: ${t.startsWith('{') ? 'evet' : 'HAYIR'}`,
    `'}' ile bitiyor: ${t.endsWith('}') ? 'evet' : 'HAYIR'}`,
    `"private_key" alanı: ${t.includes('"private_key"') ? 'var' : 'YOK'}`,
    `"client_email" alanı: ${t.includes('"client_email"') ? 'var' : 'YOK'}`,
    `akıllı tırnak (“ ”): ${/[“”]/.test(t) ? 'VAR — düz tırnak (") olmalı' : 'yok'}`,
  ].join('\n  - ');
}

/**
 * Metin düzenleyiciler bazen private_key içindeki "\n" kaçışlarını gerçek satır sonuna çevirir;
 * JSON dizgesi içinde ham satır sonu geçersizdir. Yalnızca DİZGE İÇİNDEKİ satır sonlarını "\n" yapar.
 */
function escapeNewlinesInStrings(text) {
  let out = '';
  let inString = false;
  let escaped = false;
  for (const ch of text) {
    if (inString) {
      if (escaped) { escaped = false; out += ch; continue; }
      if (ch === '\\') { escaped = true; out += ch; continue; }
      if (ch === '"') { inString = false; out += ch; continue; }
      if (ch === '\n') { out += '\\n'; continue; }
      if (ch === '\r') continue;
      if (ch === '\t') { out += '\\t'; continue; }
      out += ch;
    } else {
      if (ch === '"') inString = true;
      out += ch;
    }
  }
  return out;
}

/** Hata türünü, metinden parça içermeden Türkçe açıklar. */
function describeParseError(err) {
  const m = err.message;
  if (/control character/i.test(m)) return 'metin içinde izin verilmeyen satır sonu/kontrol karakteri';
  if (/end of (json|data)|unterminated/i.test(m)) return 'metin yarıda kesilmiş';
  if (/unexpected token|unexpected character|expected/i.test(m)) return 'beklenmeyen karakter';
  return 'bilinmeyen biçim hatası';
}

/** Hizmet hesabı dosyasını okur; BOM, boşluklar ve dizge içindeki ham satır sonlarını tolere eder. */
function readServiceAccount(keyFile) {
  const text = fs.readFileSync(keyFile, 'utf8').replace(/^﻿/, '').trim();
  let json;
  try {
    json = JSON.parse(text);
  } catch (firstErr) {
    try {
      json = JSON.parse(escapeNewlinesInStrings(text));
      console.warn('Uyarı: anahtar metnindeki satır sonları onarıldı (kopyalama sırasında bozulmuş).');
    } catch {
      // JSON.parse mesajı metinden parça içerebilir; yalnızca konumu ve türü alıyoruz.
      const pos = /position (\d+)/.exec(firstErr.message);
      throw new Error(
        'FIREBASE_SERVICE_ACCOUNT geçerli bir JSON değil' +
          (pos ? ` (${pos[1]}. karakterde: ${describeParseError(firstErr)})` : ` (${describeParseError(firstErr)})`) +
        '.\n  - ' + describeKeyText(text) +
        '\nÇözüm: Firebase\'den indirdiğiniz .json dosyasının TAMAMINI kopyalayıp GitHub secret\'ını güncelleyin.'
      );
    }
  }
  if (json.type !== 'service_account' || !json.private_key || !json.client_email || !json.project_id) {
    throw new Error(
      'FIREBASE_SERVICE_ACCOUNT bir hizmet hesabı anahtarı değil (type/private_key/client_email/project_id eksik).\n  - ' +
        describeKeyText(text)
    );
  }
  return json;
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

module.exports = { initAdmin, readServiceAccount, describeKeyText, escapeNewlinesInStrings };
