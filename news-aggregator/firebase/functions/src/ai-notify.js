'use strict';

/**
 * Yapay zekâ (Gemini) bildirim editörü — "Sadece önemli" ve "Dengeli" (calm) kullanıcılarına giden
 * son dakika / önemli gelişme bildirimlerini seçer. Anahtar kelime puanlaması (scoring.js) yalnızca ADAY üretir;
 * gönderme kararını Gemini verir. Örnek yanlış pozitifler (kelime eşleşmesiyle "kritik" sayılıyordu):
 *   "Deprem enkazından yayılan tozlar bilimsel araştırmaya konu oldu"  → deprem/enkaz ama olay değil
 *   "Fransa'nın en zengin ailesinde taht savaşı"                       → savaş mecaz
 *   "Türk Kızılay Kanunu yürürlüğe girdi: Afet alımları..."            → afet ama acil değil
 *
 * Karar biçimi (adaylar için, hikâye kimliğine göre saklanır):
 *   { ok: bool, kritik: bool, gece: bool, neden: string, at: ms, fallback?: true }
 *   ok     : calm kullanıcılarına gönderilsin mi
 *   kritik : "🔴 SON DAKİKA" (değilse "⚡ ÖNEMLİ GELİŞME")
 *   gece   : can güvenliği — gece sessiz saatte ve günlük sınırı aşsa da gönderilir
 *
 * Gemini yanıt vermezse eski kurallarla karar verilir (fallback), yine de günlük sınır uygulanır.
 */

const DECISION_TTL_MS = 24 * 3_600_000;
const MAX_CANDIDATES = 4;

function buildJudgePrompt(candidates, sentToday, { now = Date.now() } = {}) {
  const saat = new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', hour: '2-digit', minute: '2-digit' }).format(new Date(now));
  return [
    'Sen bir Türkçe haber uygulamasının bildirim editörüsün. Kullanıcılar "az ama önemli" bildirim istiyor:',
    'günde en fazla 4 son dakika/önemli gelişme bildirimi. Her bildirim telefonu titretir; gereksiz olanı GÖNDERME.',
    '',
    'GÖNDER (ok=true) yalnızca şunlar: ülke gündemini değiştiren karar ve olaylar (seçim, kabine, faiz/asgari ücret/zam',
    'kararı, büyük yasa), can güvenliği (gerçekleşmiş deprem, büyük kaza, saldırı, savaş başlaması, afet, salgın),',
    'milyonları doğrudan etkileyen gelişmeler, çok büyük uluslararası olaylar.',
    'GÖNDERME: araştırma/bilim haberleri, mecaz kullanımlar ("taht savaşı", "fiyat savaşı"), yorum/açıklama/demeç,',
    'magazin, spor (milli takım büyük başarı hariç), şirket haberleri (dev şirketlerin olağan davaları dahil),',
    'yerel adli olaylar, operasyon/gözaltı rutinleri, yabancı ülke iç siyaseti, rutin istatistik, ve bugün zaten',
    'bildirilen bir konunun yeni bilgi içermeyen tekrarı.',
    'kritik=true: son dakika niteliğinde, hemen bilinmesi gereken. gece=true YALNIZCA gerçekleşmiş can güvenliği',
    'olayı (deprem, büyük patlama/kaza, saldırı, savaş, afet) — insanları gece uyandırmaya değer.',
    '',
    `Saat (TSİ): ${saat}`,
    'BUGÜN ZATEN GÖNDERİLEN BİLDİRİMLER:',
    sentToday.length ? sentToday.map((t) => `- ${t}`).join('\n') : '(yok)',
    '',
    'ADAYLAR (JSON):',
    JSON.stringify(candidates.map((c) => ({
      id: c.id, baslik: c.title, ozet: String(c.sum || '').slice(0, 200), kaynak_sayisi: (c.srcs || []).length,
      son_dakika_kaynagi: c.br === true,
    }))),
    '',
    'Yalnızca JSON döndür: {"kararlar":[{"id":"...","ok":true,"kritik":false,"gece":false,"neden":"kısa gerekçe"}]}',
  ].join('\n');
}

function parseDecisions(text, now = Date.now()) {
  const s = String(text || '').replace(/```(?:json)?/gi, '');
  const o = JSON.parse(s.slice(s.indexOf('{'), s.lastIndexOf('}') + 1));
  const out = {};
  for (const k of Array.isArray(o.kararlar) ? o.kararlar : []) {
    if (!k || !k.id) continue;
    out[String(k.id)] = { ok: k.ok === true, kritik: k.kritik === true && k.ok === true, gece: k.gece === true && k.ok === true, neden: String(k.neden || '').slice(0, 120), at: now };
  }
  return out;
}

/** Eski kurallarla karar (Gemini yoksa). */
function fallbackDecision(score, now) {
  return { ok: score.level <= 2, kritik: score.level === 1, gece: score.night === true, neden: `kural: ${score.reason}`, at: now, fallback: true };
}

/** Süresi geçmiş kararları atar. */
function pruneDecisions(decisions = {}, now = Date.now()) {
  return Object.fromEntries(Object.entries(decisions).filter(([, d]) => d && now - (d.at || 0) < DECISION_TTL_MS));
}

/**
 * Adaylar için Gemini kararı alır. Hata durumunda fallback kararları döner.
 * @param {Array<{story, score}>} pending
 */
async function judgeCandidates(pending, sentToday, { db = null, now = Date.now(), generate } = {}) {
  const list = pending.slice(0, MAX_CANDIDATES);
  if (!list.length) return { decisions: {}, error: '' };
  let error = '';
  try {
    const gem = require('./gemini');
    const gen = generate || (async (p) => gem.generate(p, { config: await gem.loadAiConfig(db), maxOutputTokens: 4096, timeoutMs: 45_000, json: true }));
    const r = await gen(buildJudgePrompt(list.map((x) => x.story), sentToday, { now }));
    if (r.text) {
      const parsed = parseDecisions(r.text, now);
      const decisions = {};
      for (const { story, score } of list) decisions[story.id] = parsed[story.id] || fallbackDecision(score, now);
      return { decisions, error: '' };
    }
    error = r.error || 'boş yanıt';
  } catch (err) {
    error = err.message;
  }
  return { decisions: Object.fromEntries(list.map(({ story, score }) => [story.id, fallbackDecision(score, now)])), error };
}

module.exports = { buildJudgePrompt, parseDecisions, fallbackDecision, pruneDecisions, judgeCandidates, MAX_CANDIDATES };
