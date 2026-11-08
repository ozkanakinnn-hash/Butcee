'use strict';

/**
 * Gece sessiz saatleri ve olağanüstü durum istisnası.
 *
 * 23:00–08:00 (İstanbul) arasında bildirimler `quiet` kanalından SESSİZ gider (bildirim yine düşer,
 * sabah görülür). İstisna: deprem, afet, savaş, olağanüstü hal gibi can güvenliğini ilgilendiren son dakika
 * haberleri gece de sesli gider (`breaking_alert` kanalı).
 */

const QUIET_START_HOUR = 23;
const QUIET_END_HOUR = 8;

/** İstanbul saatine göre saat (0–23). */
function istanbulHour(now = new Date()) {
  return Number(new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Istanbul', hour: '2-digit', hourCycle: 'h23' }).format(now));
}

function isQuietHour(now = new Date()) {
  const h = istanbulHour(now);
  return h >= QUIET_START_HOUR || h < QUIET_END_HOUR;
}

/** Türkçe karakterleri sadeleştirir (İ/ı/ş/ğ/ç/ö/ü → i/s/g/c/o/u), küçük harf. */
const fold = (s) => String(s || '')
  .replace(/İ/g, 'i').replace(/I/g, 'ı')
  .toLowerCase()
  .replace(/ı/g, 'i').replace(/ş/g, 's').replace(/ğ/g, 'g').replace(/ç/g, 'c').replace(/ö/g, 'o').replace(/ü/g, 'u');

// Can güvenliğini ilgilendiren olaylar (sadeleştirilmiş metinde aranır).
const CRITICAL = [
  /\btsunami/,
  /\bafet\b|\bafad\b|dogal afet|afet bolgesi/,
  /\bsel felaketi|\bsel baskini|\bheyelan/,
  /orman yangini|yangin felaketi|tahliye ediliyor|tahliye edildi/,
  /\bsavas(i|in|ta|a)?\b|savas ilan|savasa gir|seferberlik/,
  /olaganustu hal|\bohal\b|sikiyonetim|darbe girisimi/,
  /fuze saldirisi|hava saldirisi|hava sahasi kapatildi|siginaklara/,
  /teror saldirisi|bombali saldiri|canli bomba/,
  /nukleer (saldiri|sizinti|kaza)/,
];

/**
 * Deprem: küçük sarsıntılar geceyi bölmesin → büyüklük yazıyorsa 4,5 ve üstü; yazmıyorsa (ör. "Deprem! İstanbul
 * sallandı") kritik sayılır.
 */
function isCriticalEarthquake(t) {
  if (!/\bdeprem|\bzelzele|\bsarsinti/.test(t)) return false;
  const m = /(\d+(?:[.,]\d)?)\s*(?:buyuklugunde|siddetinde|magnitude|mw|ml)/.exec(t);
  if (!m) return true;
  return Number(m[1].replace(',', '.')) >= 4.5;
}

/** Başlık + özet olağanüstü durum mu? */
function isCritical(...texts) {
  const t = fold(texts.filter(Boolean).join(' '));
  return isCriticalEarthquake(t) || CRITICAL.some((re) => re.test(t));
}

module.exports = { isQuietHour, istanbulHour, isCritical, fold, QUIET_START_HOUR, QUIET_END_HOUR };
