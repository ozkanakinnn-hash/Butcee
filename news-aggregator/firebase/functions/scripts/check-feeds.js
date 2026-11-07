'use strict';

/**
 * Aday RSS adreslerini dener ve tablo halinde raporlar (Firestore'a YAZMAZ).
 *   node scripts/check-feeds.js [scripts/feed-candidates.json]
 * Her aday için: HTTP durumu, yönlendirme hedefi, haber sayısı, görselli haber oranı, son haber tarihi, örnek başlık.
 */
const path = require('node:path');
const { fetchFeed } = require('../src/feed');
const { buildArticle } = require('../src/ingest');

const file = process.argv[2] || path.join(__dirname, 'feed-candidates.json');
const { candidates } = require(path.resolve(file));

async function probeRedirect(url) {
  try {
    const res = await fetch(url, { redirect: 'manual', signal: AbortSignal.timeout(15000),
      headers: { 'User-Agent': 'Mozilla/5.0 (compatible; HaberToplayiciBot/1.0; RSS reader)' } });
    return { status: res.status, location: res.headers.get('location') };
  } catch (e) {
    return { status: 0, location: null, error: e.message };
  }
}

async function check(c) {
  const row = { ...c, ok: false };
  const probe = await probeRedirect(c.rss_url);
  row.http = probe.status;
  row.location = probe.location;
  try {
    const { feed } = await fetchFeed(c.rss_url);
    const items = feed.items || [];
    const now = new Date();
    const built = items
      .map((it) => buildArticle(it, { source: { id: 'x', source_name: c.source_name }, categoryRef: null, placeholderImageUrl: '', feedLink: feed.link, now }))
      .filter(Boolean);
    const dates = built.map((b) => b.data.published_at.toDate()).sort((a, b) => b - a);
    row.items = items.length;
    row.fresh = built.length;
    row.withImage = built.filter((b) => b.data.image_url).length;
    row.newest = dates[0] ? dates[0].toISOString().slice(0, 16) : '-';
    row.sample = built[0] ? built[0].data.title.slice(0, 60) : '-';
    row.ok = built.length > 0;
  } catch (e) {
    row.error = e.message.slice(0, 80);
  }
  return row;
}

(async () => {
  const rows = [];
  for (let i = 0; i < candidates.length; i += 6) {
    rows.push(...(await Promise.all(candidates.slice(i, i + 6).map(check))));
  }
  for (const r of rows) {
    const status = r.ok
      ? `OK  ${r.fresh}/${r.items} haber, ${r.withImage} görselli, en yeni ${r.newest} | ${r.sample}`
      : `HATA ${r.error || 'güncel haber yok'}${r.location ? ` → ${r.location}` : ''}`;
    console.log(`[${r.category}] ${r.source_name} (${r.http}) ${r.rss_url}\n    ${status}`);
  }
  console.log('\nJSON_SONUC=' + JSON.stringify(rows.map(({ category, source_name, rss_url, ok, fresh, withImage, http, location }) =>
    ({ category, source_name, rss_url, ok, fresh, withImage, http, location }))));
})();
