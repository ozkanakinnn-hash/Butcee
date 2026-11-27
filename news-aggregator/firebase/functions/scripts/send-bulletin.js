'use strict';

/**
 * Akışın haberlerini Firestore'dan toplayıp Make webhook'una gönderir.
 *   MAKE_WEBHOOK_URL=https://hook.eu2.make.com/... GOOGLE_APPLICATION_CREDENTIALS=sa.json \
 *     node scripts/send-bulletin.js [sabah|ogle|aksam|gece] [--dry-run]
 * Akış verilmezse saate göre seçilir. --dry-run: göndermeden paketi yazdırır.
 */

const { initAdmin } = require('./init-admin');
const { getFirestore } = require('firebase-admin/firestore');
const { collectBulletin, sendToMake } = require('../src/bulletin');

const args = process.argv.slice(2);
const dryRun = args.includes('--dry-run') || !process.env.MAKE_WEBHOOK_URL;
const slot = args.find((a) => !a.startsWith('--')) || process.env.SLOT || undefined;

initAdmin();

(async () => {
  const payload = await collectBulletin(getFirestore(), {
    slot: slot && slot !== 'auto' ? slot : undefined,
    appLink: process.env.APP_LINK || '',
  });
  console.log(`${payload.slot_label} (${payload.date}): ${payload.event_count} olay, gün toplamı ${payload.day_event_count}`);
  if (payload.event_count === 0) {
    console.log('Bu aralıkta haber yok; gönderilmedi.');
    return;
  }
  if (dryRun) {
    console.log(process.env.MAKE_WEBHOOK_URL ? '--dry-run: gönderilmedi.' : 'MAKE_WEBHOOK_URL yok: gönderilmedi.');
    console.log(JSON.stringify(payload, null, 2).slice(0, 4000));
    return;
  }
  console.log('Make yanıtı:', await sendToMake(process.env.MAKE_WEBHOOK_URL, payload));
})().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});
