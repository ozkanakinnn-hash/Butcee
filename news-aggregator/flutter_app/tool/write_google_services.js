'use strict';

/**
 * NEWS_GOOGLE_SERVICES_JSON ortam değişkenini doğrulayıp android/app/google-services.json olarak yazar.
 * Tablet/telefonda kopyalanırken eklenen satır sonlarını (sarma) onarır, paket adını kontrol eder.
 * Dosya içeriği gizli sayılmasa da kayda yalnızca özet bilgi yazılır.
 *
 * Kullanım: node tool/write_google_services.js [hedef] [beklenen_paket]
 */
const fs = require('node:fs');

const target = process.argv[2] || 'android/app/google-services.json';
const expectedPackage = process.argv[3] || 'com.haberakisi.haber_akisi';
const raw = (process.env.NEWS_GOOGLE_SERVICES_JSON || '').replace(/^﻿/, '').trim();

if (!raw) {
  console.log('::notice::NEWS_GOOGLE_SERVICES_JSON yok; APK Firebase olmadan derlenecek.');
  process.exit(0);
}

/** Dizge içindeki ham satır sonlarını siler (sarma onarımı); dizge dışındakiler geçerli boşluktur. */
function removeBreaksInStrings(text) {
  let out = '';
  let inString = false;
  let escaped = false;
  for (const ch of text) {
    if (!inString) {
      if (ch === '"') inString = true;
      out += ch;
      continue;
    }
    if (ch === '\n' || ch === '\r') continue;
    if (escaped) { escaped = false; out += ch; continue; }
    if (ch === '\\') { escaped = true; out += ch; continue; }
    if (ch === '"') inString = false;
    out += ch;
  }
  return out;
}

let json;
let repaired = false;
try {
  json = JSON.parse(raw);
} catch {
  try {
    json = JSON.parse(removeBreaksInStrings(raw));
    repaired = true;
  } catch (err) {
    const pos = /position (\d+)/.exec(err.message);
    console.log(`::error::google-services.json okunamadı${pos ? ` (${pos[1]}. karakter)` : ''}. ` +
      `Uzunluk ${raw.length}, '{' ile başlıyor: ${raw.startsWith('{')}, '}' ile bitiyor: ${raw.endsWith('}')}. ` +
      'Firebase\'den dosyayı yeniden indirip içeriğin tamamını secret\'a yapıştırın.');
    process.exit(1);
  }
}

const projectId = json.project_info && json.project_info.project_id;
const packages = (json.client || [])
  .map((c) => c.client_info && c.client_info.android_client_info && c.client_info.android_client_info.package_name)
  .filter(Boolean);

if (!projectId || packages.length === 0) {
  console.log('::error::Bu bir google-services.json dosyası değil (project_info / client eksik).');
  process.exit(1);
}
if (!packages.includes(expectedPackage)) {
  console.log(`::error::google-services.json "${expectedPackage}" paketini içermiyor (bulunan: ${packages.join(', ')}). ` +
    'Yanlış Firebase projesinden indirilmiş olabilir; Haber projesinde bu paket adıyla Android uygulaması ekleyin.');
  process.exit(1);
}

fs.writeFileSync(target, JSON.stringify(json, null, 2) + '\n');
console.log(`google-services.json yazıldı${repaired ? ' (kopyalamadaki satır sonları onarıldı)' : ''}: proje=${projectId}, paket=${expectedPackage}`);
