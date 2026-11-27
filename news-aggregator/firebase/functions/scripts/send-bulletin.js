'use strict';

/**
 * Akışın haberlerini Firestore'dan toplar, ücretsiz yapay zekâ ile telifsiz özet zinciri yazdırır ve
 * yalnızca X'te paylaşılmak üzere Make webhook'una gönderir. Hafta sonu (Cmt/Paz) çalışmaz.
 *   GEMINI_API_KEY=... (veya GITHUB_TOKEN) MAKE_WEBHOOK_URL=... GOOGLE_APPLICATION_CREDENTIALS=sa.json \
 *     node scripts/send-bulletin.js [sabah|ogle|aksam|gece] [--dry-run] [--force]
 * --dry-run : metni yazar, Make'e göndermez, hafızaya kaydetmez
 * --force   : hafta sonu da çalıştırır (elle deneme için)
 */

const { initAdmin } = require('./init-admin');
const { getFirestore } = require('firebase-admin/firestore');
const b = require('../src/bulletin');
const { writeBulletin, findCopiedPhrases } = require('../src/ai');

const args = process.argv.slice(2);
const dryRun = args.includes('--dry-run') || process.env.DRY_RUN === 'true' || !process.env.MAKE_WEBHOOK_URL;
const force = args.includes('--force') || process.env.FORCE === 'true';
const slotArg = args.find((a) => !a.startsWith('--')) || process.env.SLOT;
const slot = slotArg && slotArg !== 'auto' ? slotArg : undefined;

(async () => {
  const now = new Date();
  if (b.isWeekend(now) && !force) {
    console.log('Hafta sonu: bülten gönderilmez.');
    return;
  }
  initAdmin();
  const db = getFirestore();

  const payload = await b.collectBulletin(db, { slot, now, appLink: process.env.APP_LINK || '' });
  console.log(`${payload.slot_label} (${payload.date}): ${payload.event_count} olay, gün toplamı ${payload.day_event_count}`);
  if (payload.event_count === 0) return console.log('Bu aralıkta haber yok; gönderilmedi.');

  const history = await b.loadHistory(db, now);
  const result = await writeBulletin(payload, history);
  console.log(`Yapay zekâ: ${result.provider} · ${result.konular.length} konu · atlanan: ${result.atlananlar.length}`);
  if (!result.paylas) return console.log('Yeni haber veya gelişme yok; gönderilmedi.');

  result.zincir.forEach((p, i) => console.log(`\n--- ${i + 1}. gönderi (${p.length} karakter) ---\n${p}`));
  const copied = findCopiedPhrases(result.zincir, payload.events.map((e) => e.title));
  if (copied.length) console.log(`::warning::Başlıktan kopyalanmış olabilecek ifadeler: ${copied.join(' / ')}`);

  if (dryRun) return console.log('\nDeneme (dry-run): Make\'e gönderilmedi, hafızaya kaydedilmedi.');

  const body = {
    slot: payload.slot,
    slot_label: payload.slot_label,
    date: payload.date,
    part_count: result.zincir.length,
    part1: result.zincir[0] || '',
    part2: result.zincir[1] || '',
    part3: result.zincir[2] || '',
    part4: result.zincir[3] || '',
  };
  const reply = (await b.sendToMake(process.env.MAKE_WEBHOOK_URL, body)).trim();
  console.log('Make yanıtı:', reply);
  // Make senaryosunun sonundaki "Webhook response" modülü X'e gönderdikten sonra "ok" döner.
  // "Accepted" = senaryoda yanıt modülü yok → gönderinin atıldığı doğrulanamaz, yine de kaydedilir.
  if (reply !== 'ok' && reply !== 'Accepted') throw new Error('Make gönderiyi doğrulamadı; konular kaydedilmedi.');
  console.log('Hafızaya kaydedilen konu:', await b.saveTopics(db, result.konular, { slot: payload.slot, now }));
  console.log('Silinen eski konu:', await b.cleanupTopics(db, now));
})().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});
