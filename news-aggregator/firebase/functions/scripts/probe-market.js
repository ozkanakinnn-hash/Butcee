'use strict';

/**
 * Herkese açık piyasa veri kaynaklarını GitHub sunucularından dener (Firestore'a yazmaz).
 * Yalnızca verisini açıkça sunan kaynaklar; kendini tanıtan User-Agent ile.
 *   node scripts/probe-market.js
 */
const UA = 'HaberAkisi/1.0 (+https://github.com/ozkanakinnn-hash/Butcee)';

async function get(url) {
  try {
    const r = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(15000) });
    return { status: r.status, text: await r.text() };
  } catch (e) {
    return { status: 0, text: e.message };
  }
}

(async () => {
  console.log('--- TCMB (resmi, günlük)');
  const tcmb = await get('https://www.tcmb.gov.tr/kurlar/today.xml');
  for (const k of ['USD', 'EUR']) {
    console.log('tcmb', k, tcmb.status, (tcmb.text.match(new RegExp(`Kod="${k}"[\\s\\S]*?<ForexSelling>([\\d.]+)`)) || [])[1]);
  }

  console.log('--- Stooq CSV (herkese açık indirme)');
  for (const s of ['usdtry', 'eurtry', 'xauusd', 'cb.f', '^xu100', 'xu100']) {
    const r = await get(`https://stooq.com/q/l/?s=${encodeURIComponent(s)}&f=sd2t2ohlcp&h&e=csv`);
    console.log('stooq', s, r.status, r.text.replace(/\s+/g, ' ').slice(0, 140));
  }

  console.log('--- Truncgil (ücretsiz finans API)');
  const tr = await get('https://finans.truncgil.com/v4/today.json');
  console.log('truncgil', tr.status, tr.text.slice(0, 160));
  try {
    const j = JSON.parse(tr.text.replace(/^\uFEFF/, ''));
    console.log('  toplam anahtar:', Object.keys(j).length);
    for (const [k, v] of Object.entries(j)) {
      if (k === 'Update_Date') continue;
      const t = v && v.Type;
      if (t !== 'Currency' || ['USD', 'EUR'].includes(k)) console.log('  ', k, JSON.stringify(v).slice(0, 140));
    }
    for (const k of ['USD', 'EUR', 'GRA', 'gram-altin', 'ONS', 'ons', 'BIST100', 'XU100', 'BRENT']) {
      if (j[k]) console.log('  ', k, JSON.stringify(j[k]).slice(0, 160));
    }
    console.log('  anahtarlar:', Object.keys(j).slice(0, 80).join(','));
  } catch (e) {
    console.log('  JSON okunamadı:', e.message);
  }
})();
