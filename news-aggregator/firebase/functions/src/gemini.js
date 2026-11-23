'use strict';

/**
 * Gemini API ile bülten özeti (Türkçe, 2–3 cümle).
 *
 * API anahtarı Firebase Secret olarak saklanır:
 *   firebase functions:secrets:set GEMINI_API_KEY
 * index.js'teki fonksiyon secrets: ['GEMINI_API_KEY'] ile deklare edilmelidir.
 *
 * Anahtar yoksa özet atlanır — bülten yine oluşturulur, ai_summary alanı boş kalır.
 */

const { logger } = require('firebase-functions');

const GEMINI_MODEL = 'gemini-1.5-flash';
const GEMINI_ENDPOINT = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;
const TIMEOUT_MS = 10_000;

/**
 * Bülten hikâyelerini 2–3 cümleyle özetler.
 * @param {Array<{title: string, source: string, srcs: string[]}>} stories
 * @returns {Promise<string>} Türkçe özet metni; hata durumunda boş string.
 */
async function summarizeBriefing(stories) {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) return '';

  const lines = stories.slice(0, 7).map((s, i) => {
    const src = s.source || (s.srcs && s.srcs[0]) || '';
    return `${i + 1}. ${s.title}${src ? ` (${src})` : ''}`;
  });

  const prompt = [
    'Aşağıdaki haber başlıklarını kullanarak bugünün gündemini 2–3 Türkçe cümleyle özetle.',
    'Yalnızca özet yaz, başlık veya açıklama ekleme. Bağlaçlar ve akıcı dil kullan.',
    '',
    ...lines,
  ].join('\n');

  try {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
    const res = await fetch(GEMINI_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'x-goog-api-key': apiKey },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: { maxOutputTokens: 150, temperature: 0.4 },
        safetySettings: [
          { category: 'HARM_CATEGORY_HARASSMENT', threshold: 'BLOCK_NONE' },
          { category: 'HARM_CATEGORY_HATE_SPEECH', threshold: 'BLOCK_NONE' },
        ],
      }),
      signal: ctrl.signal,
    });
    clearTimeout(timer);

    if (!res.ok) {
      logger.warn('Gemini yanıt hatası', { status: res.status });
      return '';
    }

    const json = await res.json();
    const text = json?.candidates?.[0]?.content?.parts?.[0]?.text || '';
    return text.trim();
  } catch (err) {
    if (err.name !== 'AbortError') logger.warn('Gemini özet alınamadı', { error: err.message });
    return '';
  }
}

module.exports = { summarizeBriefing };
