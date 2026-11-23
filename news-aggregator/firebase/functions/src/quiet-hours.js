'use strict';

/**
 * Gece sessiz saatleri ve olağanüstü durum istisnası.
 *
 * 00:00–08:00 (İstanbul) arasında bildirimler `quiet` kanalından SESSİZ gider (bildirim yine düşer,
 * sabah görülür). İstisna: deprem, afet, savaş, olağanüstü hal gibi can güvenliğini ilgilendiren son dakika
 * haberleri gece de sesli gider (`breaking_news` kanalı).
 */

const QUIET_START_HOUR = 0; // gece yarısı
const QUIET_END_HOUR = 8;

/** İstanbul saatine göre saat ve dakika. */
function istanbulHourMinute(now = new Date()) {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Istanbul', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' })
      .formatToParts(now).map((x) => [x.type, x.value]),
  );
  return { hour: Number(p.hour), minute: Number(p.minute) };
}

/**
 * Haber bildirimlerinin TAMAMEN susturulduğu gece aralığı: 23:59 – 08:00 (İstanbul).
 * Bu aralıkta yalnızca can güvenliğini ilgilendiren haberler (deprem, savaş, saldırı, afet) gönderilir.
 * `isQuietHour` ise bildirimin SESSİZ kanaldan gitmesini belirler (00:00–08:00) — ikisi ayrı amaçlıdır.
 */
function isNightSilent(now = new Date()) {
  const { hour, minute } = istanbulHourMinute(now);
  return (hour === 23 && minute >= 59) || hour < QUIET_END_HOUR;
}

/** İstanbul saatine göre saat (0–23). */
function istanbulHour(now = new Date()) {
  return Number(new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Istanbul', hour: '2-digit', hourCycle: 'h23' }).format(now));
}

function isQuietHour(now = new Date()) {
  const h = istanbulHour(now);
  return QUIET_START_HOUR < QUIET_END_HOUR
    ? h >= QUIET_START_HOUR && h < QUIET_END_HOUR // ör. 00–08
    : h >= QUIET_START_HOUR || h < QUIET_END_HOUR; // ör. 23–08 (gece yarısını aşan)
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
 * sallandı") kritik sayılır. "Deprem mi oldu?" tarzı sorgulama başlıkları kritik sayılmaz.
 */
/** "Deprem mi oldu?", "Nerede deprem oldu?", "son depremler listesi" gibi merak/sorgu başlıkları (katlanmış metin). */
const EARTHQUAKE_QUERY = /\bdeprem mi (?:oldu|var)|\bnerede deprem|\bson depremler\b|\bdeprem sorgula/;

function isCriticalEarthquake(t) {
  if (!/\bdeprem|\bzelzele|\bsarsinti/.test(t)) return false;
  const m = /(\d+(?:[.,]\d)?)\s*(?:buyuklugunde|siddetinde|magnitude|mw|ml)/.exec(t);
  // Büyüklük yazıyorsa karar onundur: "Az önce 5,8 büyüklüğünde deprem" sorgu kalıbı içerse de kritiktir.
  if (m) return Number(m[1].replace(',', '.')) >= 4.5;
  // Büyüklüksüz sorgulama/merak başlıkları ("Deprem mi oldu?", "Nerede deprem oldu?") kritik değil.
  if (EARTHQUAKE_QUERY.test(t)) return false;
  return true;
}

/** Başlık + özet olağanüstü durum mu? */
function isCritical(...texts) {
  const t = fold(texts.filter(Boolean).join(' '));
  return isCriticalEarthquake(t) || CRITICAL.some((re) => re.test(t));
}

module.exports = { isQuietHour, isNightSilent, istanbulHourMinute, istanbulHour, isCritical, fold, EARTHQUAKE_QUERY, QUIET_START_HOUR, QUIET_END_HOUR };
