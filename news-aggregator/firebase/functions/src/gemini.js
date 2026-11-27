'use strict';

/**
 * Gemini API ile Türkçe haber özetleri (bülten özeti + tek haber özeti).
 *
 * API anahtarı Firebase Secret olarak saklanır:
 *   firebase functions:secrets:set GEMINI_API_KEY
 * index.js'teki fonksiyon secrets: ['GEMINI_API_KEY'] ile deklare edilir.
 *
 * Ayarlar `app_meta/ai` belgesinde (deploy gerekmez):
 *   { enabled: true, model: "gemini-flash-latest", story_summary: true }
 * - `model` kapatılırsa/yanıt vermezse FALLBACK_MODELS sırayla denenir; çalışan model `active_model`
 *   alanına, son hata `last_error` alanına yazılır (Firestore Console'dan izlenir).
 *
 * Anahtar yoksa veya tüm modeller hata verirse özet boş döner — bülten yine oluşturulur.
 */

const { logger } = require('firebase-functions');

const DEFAULT_MODEL = 'gemini-flash-latest';
const FALLBACK_MODELS = ['gemini-flash-latest', 'gemini-2.5-flash', 'gemini-flash-lite-latest', 'gemini-2.0-flash'];
const ENDPOINT = (model) => `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;
const TIMEOUT_MS = 20_000;
const CONFIG_DOC = 'app_meta/ai';

const AI_DEFAULTS = { enabled: true, model: DEFAULT_MODEL, story_summary: true };

/** Ayar belgesini okur; okunamazsa varsayılanlar. */
async function loadAiConfig(db) {
  if (!db) return { ...AI_DEFAULTS };
  try {
    return { ...AI_DEFAULTS, ...((await db.doc(CONFIG_DOC).get()).data() || {}) };
  } catch {
    return { ...AI_DEFAULTS };
  }
}

/** Denenecek modeller: önce ayardaki, sonra yedekler (tekrarsız). */
function modelOrder(config) {
  return [...new Set([String(config.model || DEFAULT_MODEL).trim(), ...FALLBACK_MODELS].filter(Boolean))];
}

/** Gemini yanıtından metni çıkarır (saf fonksiyon). */
function extractText(json) {
  const parts = (json && json.candidates && json.candidates[0] && json.candidates[0].content && json.candidates[0].content.parts) || [];
  return parts.filter((p) => p && typeof p.text === 'string' && !p.thought).map((p) => p.text).join('').trim();
}

/**
 * Bir istem için özet üretir; model hatasında sıradaki modeli dener.
 * @returns {Promise<{text: string, model: string, error: string}>}
 */
async function generate(prompt, { config = AI_DEFAULTS, fetchImpl = fetch, apiKey = process.env.GEMINI_API_KEY } = {}) {
  if (!apiKey) return { text: '', model: '', error: 'GEMINI_API_KEY yok' };
  if (config.enabled === false) return { text: '', model: '', error: 'kapalı' };
  let lastError = '';
  for (const model of modelOrder(config)) {
    try {
      const res = await fetchImpl(ENDPOINT(model), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'x-goog-api-key': apiKey },
        body: JSON.stringify({
          contents: [{ role: 'user', parts: [{ text: prompt }] }],
          // Düşünen modeller düşünme jetonlarını da bu sınırdan harcar; kısa sınır boş yanıt döndürür.
          generationConfig: { maxOutputTokens: 2048, temperature: 0.3 },
        }),
        signal: AbortSignal.timeout(TIMEOUT_MS),
      });
      if (!res.ok) {
        const body = (await res.text().catch(() => '')).slice(0, 160).replace(/\s+/g, ' ');
        lastError = `${model}: HTTP ${res.status} ${body}`.trim();
        // Anahtar hatası (400/401/403) diğer modellerde de aynıdır; boşuna deneme.
        if ([400, 401, 403].includes(res.status) && !/model/i.test(body)) break;
        continue;
      }
      const text = extractText(await res.json());
      if (text) return { text, model, error: '' };
      lastError = `${model}: boş yanıt`;
    } catch (err) {
      lastError = `${model}: ${err.name === 'TimeoutError' || err.name === 'AbortError' ? 'zaman aşımı' : err.message}`;
    }
  }
  return { text: '', model: '', error: lastError };
}

/** Sonucu ayar belgesine işler (izleme için); hata çekimi bozmaz. */
async function recordStatus(db, result) {
  if (!db || result.error === 'kapalı') return;
  const { Timestamp } = require('firebase-admin/firestore');
  try {
    await db.doc(CONFIG_DOC).set(
      result.text
        ? { active_model: result.model, last_ok_at: Timestamp.now(), last_error: '' }
        : { last_error: result.error, last_error_at: Timestamp.now() },
      { merge: true },
    );
  } catch {
    // izleme kaydı yazılamazsa önemli değil
  }
  if (!result.text) logger.warn('Gemini özet alınamadı', { error: result.error });
}

const clean = (text, max) => {
  const t = String(text || '').replace(/\*\*|__|^#+\s*/gm, '').replace(/\s+/g, ' ').trim();
  return t.length <= max ? t : `${t.slice(0, max - 1).trimEnd()}…`;
};

/** Bülten istemi (saf fonksiyon). */
function briefingPrompt(stories) {
  const lines = stories.slice(0, 7).map((s, i) => {
    const src = s.source || (s.srcs && s.srcs[0]) || (s.sources && s.sources[0]) || '';
    return `${i + 1}. ${s.title}${src ? ` (${src})` : ''}`;
  });
  return [
    'Aşağıdaki haber başlıklarından bugünün gündemini 2-3 kısa Türkçe cümleyle özetle.',
    'Yalnızca verilen bilgileri kullan, yorum ve tahmin ekleme. Başlık, madde işareti veya emoji yazma.',
    '',
    ...lines,
  ].join('\n');
}

/** Tek haber istemi (saf fonksiyon). */
function storyPrompt(story) {
  return [
    'Aşağıdaki haberi tek, tarafsız ve kısa bir Türkçe cümleyle özetle (en fazla 200 karakter).',
    'Yalnızca verilen bilgileri kullan; yorum, tahmin, emoji veya başlık ekleme.',
    '',
    `Başlık: ${story.title}`,
    story.sum ? `Özet: ${story.sum}` : '',
    story.srcs && story.srcs.length ? `Haberi veren yayıncılar: ${story.srcs.slice(0, 8).join(', ')}` : '',
  ].filter(Boolean).join('\n');
}

/**
 * Bülten hikâyelerini 2–3 cümleyle özetler.
 * @returns {Promise<string>} Türkçe özet; hata durumunda boş string.
 */
async function summarizeBriefing(stories, { db = null, fetchImpl, apiKey } = {}) {
  if (!stories || !stories.length) return '';
  const config = await loadAiConfig(db);
  const r = await generate(briefingPrompt(stories), { config, fetchImpl, apiKey });
  await recordStatus(db, r);
  return clean(r.text, 600);
}

/** Tek haberi bir cümleyle özetler (Make paylaşımları için). Kapalıysa boş string. */
async function summarizeStory(story, { db = null, fetchImpl, apiKey } = {}) {
  const config = await loadAiConfig(db);
  if (config.story_summary === false) return '';
  const r = await generate(storyPrompt(story), { config, fetchImpl, apiKey });
  await recordStatus(db, r);
  return clean(r.text, 240);
}

module.exports = {
  summarizeBriefing, summarizeStory, generate, extractText, modelOrder, briefingPrompt, storyPrompt,
  AI_DEFAULTS, FALLBACK_MODELS,
};
