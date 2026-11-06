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

/** Hizmet hesabı dosyasını okur; BOM ve baştaki/sondaki boşlukları tolere eder. */
function readServiceAccount(keyFile) {
  const text = fs.readFileSync(keyFile, 'utf8').replace(/^﻿/, '').trim();
  let json;
  try {
    json = JSON.parse(text);
  } catch (err) {
    // JSON.parse mesajı metinden parça içerebilir; yalnızca konumu alıyoruz.
    const pos = /position (\d+)/.exec(err.message);
    throw new Error(
      'FIREBASE_SERVICE_ACCOUNT geçerli bir JSON değil' +
        (pos ? ` (${pos[1]}. karakterde bozuk)` : '') +
        '.\n  - ' + describeKeyText(text) +
        '\nÇözüm: Firebase\'den indirdiğiniz .json dosyasının TAMAMINI kopyalayıp GitHub secret\'ını güncelleyin.'
    );
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

module.exports = { initAdmin, readServiceAccount, describeKeyText };
