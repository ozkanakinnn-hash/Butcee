'use strict';

/**
 * Haber önemi: aynı olayı kaç FARKLI yayıncının verdiği ("hikâye" kümeleri) + tıklama tuzağı filtresi.
 *
 * Başlıklar sadeleştirilip (Türkçe karakter, ek) anahtar kelime köklerine ayrılır; iki başlık ortak kök oranı
 * yeterliyse aynı hikâye sayılır. Hikâyeler `notify_state/stories` belgesinde tutulur (bülten ve önemli son dakika
 * için). Saf fonksiyonlar; Firestore'a dokunmaz.
 */

const { fold } = require('./quiet-hours');

const STEM_LEN = 5; // "depremde", "depremin" → "depre"; "faizi", "faizin" → "faizi"
const STOPWORDS = new Set(`
  ve ile icin gibi daha cok son dakika bir iki uc bu su o da de mi mu mi ne neden nasil kim hangi en
  olan oldu olarak olmak olur ama fakat ancak veya yada hem her tum bazi buyuk yeni ilk sonra once
  karar aciklama acikladi acikladi dedi soyledi iste gore kadar uzere yonelik ilgili hakkinda yaptigi
  turkiye turk bugun yarin dun haber haberi video foto galeri canli izle
  dan den tan ten nin nun nın in un ya ye yi yu
`.trim().split(/\s+/));

/** Başlık → anahtar kök kümesi (dizi; sırasız). */
// Kısaltmalar açık hâliyle eşleşsin.
const ALIASES = { tcmb: ['merke', 'banka'], mb: ['merke', 'banka'], tbmm: ['meclis'], abd: ['amerika'], ab: ['avrup', 'birli'] };

function storyTokens(title) {
  const words = fold(title).replace(/[^a-z0-9]+/g, ' ').split(' ').flatMap((w) => ALIASES[w] || [w]);
  const out = new Set();
  for (const w of words) {
    if (w.length < 3 && !Object.values(ALIASES).flat().includes(w) || STOPWORDS.has(w) || /^\d{1,2}$/.test(w)) continue;
    out.add(w.slice(0, STEM_LEN));
  }
  return [...out];
}

/** İki başlık aynı hikâye mi? Ortak kök sayısı ≥ 2 ve küçük kümenin en az yarısı. */
function sameStory(a, b) {
  if (!a.length || !b.length) return false;
  const B = new Set(b);
  let common = 0;
  for (const t of a) if (B.has(t)) common += 1;
  return common >= 2 && common / Math.min(a.length, b.length) >= 0.5;
}

// Tıklama tuzağı / magazin: bildirim ve bültene girmez (akışta görünmeye devam eder).
const CLICKBAIT = [
  /\bsok\b|\bsoke\b|sok eden|sok olacaksiniz/,
  /bakin ne (oldu|dedi|cikti)|gorenler sasirdi|herkes (bunu )?konusuyor|inanamayacaksiniz|merak edilen/,
  /\biste o\b|iste o an|iste detaylar|iste nedeni|iste cevabi/,
  /\bburc|\bfal\b|ruya tabiri|gunluk burc/,
  /\bmagazin|\bunlu (oyuncu|sarkici|isim)|sevgilisi|ask yasiyor|tatil pozu|bikinili|dekolte/,
  /\btarif|\bhangi gun\b.*\btatil/,
];
const isClickbait = (title) => CLICKBAIT.some((re) => re.test(fold(title)));

/**
 * Yeni haberleri hikâye listesine işler (saf fonksiyon). Hikâye:
 *  { t: kökler, srcs: [yayıncı adları], first_ms, last_ms, title, url, source, image, cat, br: son dakika kaynağı mı,
 *    id: temsilci haber kimliği, major_ms?: önemli son dakika gönderildiyse }
 * Temsilci haber: son dakika kaynağından gelen varsa o, yoksa ilk gelen.
 */
function mergeStories(stories, articles, { now = Date.now() } = {}) {
  const list = stories.map((s) => ({ ...s, srcs: [...s.srcs] }));
  const touched = new Set();
  for (const a of articles) {
    const d = a.data;
    const t = storyTokens(d.title || '');
    if (t.length < 2) continue;
    const src = d.source_name || d.source_id || '?';
    const ms = d.published_at && d.published_at.toMillis ? d.published_at.toMillis() : now;
    const rep = {
      id: a.id,
      title: d.title,
      url: d.original_url,
      source: d.source_name || '',
      image: d.image_url && d.has_original_image !== false ? d.image_url : '',
      cat: (d.category_id && d.category_id.id) || '',
    };
    let s = list.find((x) => sameStory(x.t, t));
    if (!s) {
      s = { t, srcs: [], first_ms: ms, last_ms: ms, br: false, ...rep };
      list.push(s);
    }
    if (!s.srcs.includes(src)) s.srcs.push(src);
    s.last_ms = Math.max(s.last_ms, ms);
    s.first_ms = Math.min(s.first_ms, ms);
    if (a.breaking && !s.br) Object.assign(s, rep, { br: true });
    touched.add(s);
  }
  return { stories: list, touched: [...touched] };
}

/** Eski/tek kaynaklı hikâyeleri budar; belge boyutu sınırlı kalsın. */
function pruneStories(stories, { now = Date.now(), keepSinceMs = 0, max = 600 } = {}) {
  const cutoff = Math.min(now - 3 * 3_600_000, keepSinceMs || now);
  let kept = stories.filter((s) => s.last_ms >= now - 14 * 3_600_000 && (s.srcs.length >= 2 || s.last_ms >= cutoff));
  if (kept.length > max) {
    kept = kept.sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms).slice(0, max);
  }
  return kept;
}

module.exports = { storyTokens, sameStory, isClickbait, mergeStories, pruneStories, STOPWORDS };
