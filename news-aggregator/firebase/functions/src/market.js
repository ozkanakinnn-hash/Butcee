'use strict';

/**
 * Piyasa verileri → Firestore `market/latest` tek belge (uygulamanın sağ üst paneli).
 *
 * Yayın öncesi lisans temizliği (bkz. docs/YAYIN_YOL_HARITASI.md):
 *  - Dolar / Euro: T.C. Merkez Bankası gösterge niteliğindeki döviz kurları (tcmb.gov.tr/kurlar/today.xml),
 *    resmî ve kamuya açık; kaynak gösterilerek kullanılır. Günde bir kez (≈15:30) güncellenir. Günlük değişim,
 *    bir önceki TCMB bülteniyle karşılaştırılarak hesaplanır (önceki değerler belgede saklanır).
 *  - Gram / ons altın: Truncgil Finans (ücretsiz, anahtarsız). Ons fiyatı gelmezse gram × 31,1035 ÷ USD/TL.
 *  - BIST 100 ve Brent kaldırıldı: Borsa İstanbul verisi yalnızca lisanslı veri dağıtıcılarıyla yeniden
 *    yayımlanabilir; Brent için güvenilir ücretsiz/lisanslı kaynak yok.
 * Bir kaynak hata verirse diğerinin verisi yine yazılır; hatalı kaynağın son bilinen değeri korunur.
 */

const { Timestamp } = require('firebase-admin/firestore');

const TRUNCGIL_URL = 'https://finans.truncgil.com/v4/today.json';
const TCMB_URL = 'https://www.tcmb.gov.tr/kurlar/today.xml';
const UA = 'HaberAkisi/1.0';
const TROY_OUNCE_GRAMS = 31.1034768;
const ITEM_IDS = ['usdtry', 'eurtry', 'gram_gold', 'gold_oz'];

/** Truncgil yanıtındaki `"ANAHTAR":{...}` kalemlerini tek tek okur; bozuk kalemleri atlar (saf fonksiyon). */
function parseTruncgil(text) {
  const out = {};
  for (const [, key, obj] of String(text).matchAll(/"([^"{}]+)":(\{[^{}]*\})/g)) {
    try {
      out[key] = JSON.parse(obj);
    } catch {
      // bozuk kalem — atla
    }
  }
  const date = /"Update_Date":"([^"]+)"/.exec(text);
  return { items: out, updateDate: date ? date[1] : null };
}

/** TCMB today.xml → { date: '11.11.2026', rates: { USD: 42.1, EUR: 48.7 } } (döviz satış, birim başına). */
function parseTcmb(xml) {
  const s = String(xml);
  const date = /<Tarih_Date[^>]*\bTarih="([^"]+)"/.exec(s);
  const rates = {};
  for (const [, code, body] of s.matchAll(/<Currency[^>]*\bCurrencyCode="([A-Z]{3})"[^>]*>([\s\S]*?)<\/Currency>/g)) {
    const unit = Number((/<Unit>\s*([\d.]+)\s*<\/Unit>/.exec(body) || [])[1]) || 1;
    const sell = Number((/<ForexSelling>\s*([\d.]+)\s*<\/ForexSelling>/.exec(body) || [])[1]);
    if (sell > 0) rates[code] = sell / unit;
  }
  return { date: date ? date[1] : null, rates };
}

const num = (v) => (typeof v === 'number' && Number.isFinite(v) && v > 0 ? v : null);
const chg = (v) => (typeof v === 'number' && Number.isFinite(v) ? v : null);
const pct = (now, prev) => (num(now) && num(prev) ? Number((((now - prev) / prev) * 100).toFixed(2)) : null);

/**
 * Uygulamanın gösterdiği sıralı liste (saf fonksiyon).
 * fx: { usd, eur, usd_prev, eur_prev } (TCMB), gold: Truncgil kalemleri.
 */
function buildMarketItems(fx = {}, gold = {}) {
  const items = [];
  const add = (id, label, unit, value, change, decimals) => {
    if (value === null) return;
    items.push({ id, label, unit, value: Number(value.toFixed(decimals)), change_pct: change, decimals });
  };
  const usd = num(fx.usd);
  const gram = num(gold.GRA && gold.GRA.Selling);
  add('usdtry', 'Dolar', '₺', usd, pct(fx.usd, fx.usd_prev), 4);
  add('eurtry', 'Euro', '₺', num(fx.eur), pct(fx.eur, fx.eur_prev), 4);
  add('gram_gold', 'Gram Altın', '₺', gram, chg(gold.GRA && gold.GRA.Change), 2);
  const usdForOns = usd ?? num(gold.USD && gold.USD.Selling);
  const ons = num(gold.ONS && gold.ONS.Selling) ?? (gram && usdForOns ? (gram * TROY_OUNCE_GRAMS) / usdForOns : null);
  add('gold_oz', 'Ons Altın', '$', ons, chg(gold.ONS && gold.ONS.Change), 2);
  return items;
}

/** TCMB bülteni + önceki bültenle kıyas için saklanan durum. Yeni gün bülteninde "önceki" kaydırılır. */
function nextFxState(prev = {}, tcmb) {
  if (!tcmb || !tcmb.date || !num(tcmb.rates.USD)) return prev; // hata → son bilinen
  if (tcmb.date === prev.date) return { ...prev, usd: tcmb.rates.USD, eur: tcmb.rates.EUR ?? prev.eur };
  return {
    date: tcmb.date,
    usd: tcmb.rates.USD,
    eur: tcmb.rates.EUR ?? null,
    usd_prev: prev.usd ?? null,
    eur_prev: prev.eur ?? null,
  };
}

async function fetchText(fetchImpl, url) {
  const res = await fetchImpl(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(15_000) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.text();
}

async function updateMarket(db, { fetchImpl = fetch } = {}) {
  const errors = [];
  const ref = db.doc('market/latest');
  const old = (await ref.get()).data() || {};

  let tcmb = null;
  try {
    tcmb = parseTcmb(await fetchText(fetchImpl, TCMB_URL));
    if (!num(tcmb.rates.USD)) errors.push('TCMB: kur bulunamadı');
  } catch (err) {
    errors.push(`TCMB: ${err.message}`);
  }
  let gold = {};
  let goldDate = old.gold_updated || '';
  try {
    const t = parseTruncgil(await fetchText(fetchImpl, TRUNCGIL_URL));
    gold = t.items;
    goldDate = t.updateDate || goldDate;
  } catch (err) {
    errors.push(`Truncgil: ${err.message}`);
  }

  const fx = nextFxState(old.fx || {}, tcmb);
  const items = buildMarketItems(fx, gold);
  // Altın gelmediyse son bilinen altın kalemleri korunur.
  for (const id of ['gram_gold', 'gold_oz']) {
    if (!items.some((i) => i.id === id)) {
      const keep = (old.items || []).find((i) => i.id === id);
      if (keep) items.push(keep);
    }
  }
  items.sort((a, b) => ITEM_IDS.indexOf(a.id) - ITEM_IDS.indexOf(b.id));
  const missing = ITEM_IDS.filter((id) => !items.some((i) => i.id === id));
  if (items.length) {
    await ref.set({
      items,
      fx,
      source: 'Döviz: TCMB gösterge kurları · Altın: Truncgil Finans',
      source_updated: fx.date || '',
      gold_updated: goldDate,
      updated_at: Timestamp.now(),
    });
  }
  return { count: items.length, missing, ...(errors.length ? { errors } : {}) };
}

module.exports = { parseTruncgil, parseTcmb, buildMarketItems, nextFxState, updateMarket, TROY_OUNCE_GRAMS, TCMB_URL, TRUNCGIL_URL };
