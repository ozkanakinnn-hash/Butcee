'use strict';

/**
 * Ücretsiz yapay zekâ sağlayıcıları:
 *   - gemini : Google AI Studio ücretsiz katmanı (GEMINI_API_KEY). Ücretsiz katmanda Google girdileri ürün
 *              geliştirmede kullanabilir — burada yalnızca herkese açık haber başlıkları gönderiliyor.
 *   - github : GitHub Models (Actions'taki GITHUB_TOKEN ile, anahtar gerekmez; günlük istek ve
 *              istek başına ~8 bin jeton giriş sınırı var → daha az haber gönderilir).
 * Sıra: GEMINI_API_KEY varsa gemini, hata verirse github'a düşer.
 */

const { SYSTEM_PROMPT } = require('./bulletin-prompt');

const MAX_PARTS = 4;
const MAX_PART_CHARS = 270; // " (4/4)" eki dahil X'in 280 sınırının altında kalır

const DEFAULTS = {
  gemini: { model: process.env.GEMINI_MODEL || 'gemini-2.5-flash', maxEvents: 60, summaryChars: 200 },
  github: { model: process.env.GITHUB_MODEL || 'openai/gpt-4.1-mini', maxEvents: 30, summaryChars: 100 },
};

/** Yapay zekâya gidecek kullanıcı mesajı. */
function buildUserMessage(payload, history, { maxEvents = 60, summaryChars = 200 } = {}) {
  const events = payload.events.slice(0, maxEvents).map((e) => ({
    baslik: e.title,
    ozet: (e.summary || '').slice(0, summaryChars),
    kaynaklar: e.sources,
    kategori: e.category,
    son_dakika: e.breaking || undefined,
  }));
  const hist = history.length
    ? history.map((h) => `${h.konu_anahtari} | ${h.son_durum}`).join('\n')
    : '(yok)';
  return [
    `AKIŞ: ${payload.slot_label} ${payload.emoji} — ${payload.date}`,
    `GÜNÜN BÜLTENİ MODU: ${payload.daily_recap}`,
    `UYGULAMA BAĞLANTISI: ${payload.app_link || '(yok)'}`,
    '',
    'DAHA ÖNCE PAYLAŞILAN KONULAR (son günler):',
    hist,
    '',
    'YENİ HABERLER (JSON):',
    JSON.stringify(events),
  ].join('\n');
}

/** Model çıktısındaki ```json çitlerini ve fazlalığı temizleyip JSON'u ayrıştırır. */
function parseModelJson(text) {
  const s = String(text || '').replace(/```(?:json)?/gi, '').trim();
  const a = s.indexOf('{');
  const b = s.lastIndexOf('}');
  if (a < 0 || b < a) throw new Error('Yapay zekâ JSON döndürmedi');
  return JSON.parse(s.slice(a, b + 1));
}

/** Satırları ≤ max karakterlik parçalara toplar (satır ortadan bölünmez, çok uzun satır kısaltılır). */
function packLines(lines, max) {
  const parts = [];
  let cur = '';
  for (let line of lines) {
    if (line.length > max) line = line.slice(0, max - 1).replace(/\s+\S*$/, '') + '…';
    const next = cur ? `${cur}\n${line}` : line;
    if (next.length > max && cur) {
      parts.push(cur);
      cur = line;
    } else cur = next;
  }
  if (cur.trim()) parts.push(cur);
  return parts;
}

/**
 * Zinciri X sınırlarına zorlar: en fazla 4 parça, her biri numarasıyla ≤ 280 karakter.
 * Model sınırı aşarsa satırlar yeniden paketlenir, sığmayan son maddeler atılır.
 */
function normalizeThread(zincir) {
  const parts = (Array.isArray(zincir) ? zincir : []).map((p) => String(p).trim()).filter(Boolean);
  if (parts.length === 0) return [];
  let out = parts;
  if (parts.length > MAX_PARTS || parts.some((p) => p.length > MAX_PART_CHARS)) {
    const lines = parts.join('\n').split('\n').map((l) => l.trimEnd()).filter((l) => l.trim());
    out = packLines(lines, MAX_PART_CHARS);
    if (out.length > MAX_PARTS) {
      // Son satır (bağlantı/hashtag) korunur, aradaki fazlalık atılır.
      const tail = lines[lines.length - 1];
      const keep = out.slice(0, MAX_PARTS);
      if (!keep[MAX_PARTS - 1].includes(tail) && (keep[MAX_PARTS - 1] + '\n' + tail).length <= MAX_PART_CHARS) {
        keep[MAX_PARTS - 1] += '\n' + tail;
      }
      out = keep;
    }
  }
  return out.length === 1 ? out : out.map((p, i) => `${p} (${i + 1}/${out.length})`);
}

/** Kaynak başlıklarından art arda 6+ kelime kopyalanmış mı? (telif uyarısı için) */
function findCopiedPhrases(texts, titles, n = 6) {
  const words = (s) => String(s).toLocaleLowerCase('tr').replace(/[^\p{L}\p{N}\s]/gu, ' ').split(/\s+/).filter(Boolean);
  const grams = new Set();
  for (const t of titles) {
    const w = words(t);
    for (let i = 0; i + n <= w.length; i++) grams.add(w.slice(i, i + n).join(' '));
  }
  const hits = new Set();
  for (const t of texts) {
    const w = words(t);
    for (let i = 0; i + n <= w.length; i++) {
      const g = w.slice(i, i + n).join(' ');
      if (grams.has(g)) hits.add(g);
    }
  }
  return [...hits];
}

async function callGemini(user, { apiKey, model }, fetchImpl = fetch) {
  const res = await fetchImpl(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`, {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'x-goog-api-key': apiKey },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: SYSTEM_PROMPT }] },
      contents: [{ role: 'user', parts: [{ text: user }] }],
      generationConfig: { temperature: 0.3, responseMimeType: 'application/json', maxOutputTokens: 8192 },
    }),
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(`Gemini HTTP ${res.status}: ${body.error?.message || ''}`.trim());
  const parts = body.candidates?.[0]?.content?.parts || [];
  return parts.map((p) => p.text || '').join('');
}

async function callGithubModels(user, { token, model }, fetchImpl = fetch) {
  const res = await fetchImpl('https://models.github.ai/inference/chat/completions', {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      accept: 'application/vnd.github+json',
      authorization: `Bearer ${token}`,
      'x-github-api-version': '2022-11-28',
    },
    body: JSON.stringify({
      model,
      temperature: 0.3,
      response_format: { type: 'json_object' },
      messages: [
        { role: 'system', content: SYSTEM_PROMPT },
        { role: 'user', content: user },
      ],
    }),
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(`GitHub Models HTTP ${res.status}: ${body.error?.message || body.message || ''}`.trim());
  return body.choices?.[0]?.message?.content || '';
}

/** Kullanılabilir sağlayıcıları sırayla dener. */
async function writeBulletin(payload, history, env = process.env, fetchImpl = fetch) {
  const providers = [];
  if (env.GEMINI_API_KEY) providers.push('gemini');
  if (env.GITHUB_TOKEN) providers.push('github');
  if (providers.length === 0) throw new Error('Yapay zekâ anahtarı yok: GEMINI_API_KEY veya GITHUB_TOKEN gerekli');

  const errors = [];
  for (const p of providers) {
    const cfg = DEFAULTS[p];
    const user = buildUserMessage(payload, history, cfg);
    try {
      const raw =
        p === 'gemini'
          ? await callGemini(user, { apiKey: env.GEMINI_API_KEY, model: cfg.model }, fetchImpl)
          : await callGithubModels(user, { token: env.GITHUB_TOKEN, model: cfg.model }, fetchImpl);
      const out = parseModelJson(raw);
      const zincir = out.paylas === false ? [] : normalizeThread(out.zincir);
      return {
        provider: `${p}:${cfg.model}`,
        paylas: zincir.length > 0,
        zincir,
        konular: (Array.isArray(out.konular) ? out.konular : []).filter((k) => k && k.konu_anahtari),
        atlananlar: Array.isArray(out.atlananlar) ? out.atlananlar : [],
      };
    } catch (err) {
      errors.push(`${p}: ${err.message}`);
    }
  }
  throw new Error(`Yapay zekâ başarısız — ${errors.join(' | ')}`);
}

module.exports = {
  MAX_PARTS,
  MAX_PART_CHARS,
  buildUserMessage,
  parseModelJson,
  normalizeThread,
  findCopiedPhrases,
  writeBulletin,
};
