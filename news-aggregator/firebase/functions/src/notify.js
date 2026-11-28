'use strict';

/**
 * Haber bildirimleri — "az ama önemli" (Firebase Cloud Messaging, konu/koşul tabanlı).
 *
 * Kullanıcı uygulamada bildirim YOĞUNLUĞU seçer; uygulama buna göre konulara abone olur:
 *   Sadece önemli : lvl_important + calm (+ bulletin)        → önemli son dakika + günde 3 bülten  (~4–6/gün)
 *   Dengeli       : lvl_balanced  + calm (+ bulletin)        → + kategori özetleri saatte en fazla 1 (~8–12/gün)
 *   Tümü          : lvl_all            (+ bulletin)          → her son dakika + kategori özetleri 18 dk'da bir
 * Kategori konuları (news_<kategori>) seviye konusuyla birlikte koşulda kullanılır:
 *   "('news_ekonomi' in topics || 'news_spor' in topics) && 'lvl_balanced' in topics"
 *
 * Önem: aynı olayı kaç FARKLI yayıncının verdiği (src/importance.js hikâye kümeleri). 3+ yayıncı (ya da son dakika
 * kaynağından deprem/afet/savaş gibi kritik haber) → "önemli son dakika". Tıklama tuzağı/magazin başlıkları
 * bildirime girmez. Günlük üst sınırlar ve gece sessiz saatleri uygulanır.
 *
 * Mesajlar cihaza VERİ mesajı olarak gider (kind=news_alert); Android uygulaması bildirimi kendisi oluşturur ve
 * üzerine "Daha az bildirim" düğmesi ekler (LessNotificationsReceiver). iOS için apns uyarısı eklenir.
 * Durum `notify_state/news` belgesinde (1 okuma + 1 yazma / çalışma).
 */

const { getMessaging } = require('firebase-admin/messaging');
const { logger } = require('firebase-functions');
const { isQuietHour, isNightSilent, isCritical } = require('./quiet-hours');
const { scoreStory, developmentKeys, shouldRenotify, LEVEL } = require('./scoring');

const CATEGORY_COOLDOWN_MS = 2 * 60 * 60 * 1000;
const BREAKING_COOLDOWN_MS = 10 * 60 * 1000;
const BREAKING_CATEGORY_ID = 'son-dakika';
const MAX_BREAKING_AGE_MS = 3 * 60 * 60 * 1000; // eski haberi "son dakika" diye gönderme

const topicFor = (categoryId) => `news_${categoryId}`;

function trimText(text, max) {
  return text.length <= max ? text : text.slice(0, max - 1).trimEnd() + '…';
}

/**
 * Bildirim kanalları (uygulamadaki NotificationService.channels ile aynı):
 *  breaking_news  : Son Dakika — telefonun varsayılan bildirim sesi (kullanıcı kanal ayarından değiştirebilir), yüksek öncelik
 *  news           : kategori özetleri — varsayılan ses
 *  quiet          : gece 00:00–08:00 — sessiz (olağanüstü durum haberleri hariç)
 */
const BREAKING_TITLE = '🔴 SON DAKİKA';
const BRAND_COLOR = '#0B1F3A'; // lacivert (logo)

/** Tek bir kategori bildirimi için FCM mesajı oluşturur (saf fonksiyon; test edilir). */
function buildNewsMessage({ categoryId, categoryName, articles, breaking = false, now = new Date() }) {
  const sorted = [...articles].sort((a, b) => b.data.published_at.toMillis() - a.data.published_at.toMillis());
  const top = sorted[0];
  const more = sorted.length - 1;
  const title = breaking ? BREAKING_TITLE : categoryName;
  // Gece sessiz: deprem/afet/savaş/olağanüstü hal gibi son dakika haberleri hariç.
  const critical = breaking && isCritical(top.data.title, top.data.summary);
  const quiet = isQuietHour(now) && !critical;
  const channelId = quiet ? 'quiet' : breaking ? 'breaking_news' : 'news';
  // Telif ve şeffaflık: her bildirimde haberin kaynağı belirtilir (ör. "Kaynak: AA").
  const source = top.data.source_name ? `Kaynak: ${top.data.source_name}` : '';
  const extra = !breaking && more > 0 ? `ve ${more} haber daha` : '';
  const footer = [source, extra].filter(Boolean).join(' · ');
  const body = trimText(top.data.title, 160 - (footer ? footer.length + 1 : 0)) + (footer ? `\n${footer}` : '');
  const image = top.data.image_url && top.data.has_original_image !== false ? top.data.image_url : undefined;

  return {
    topic: topicFor(categoryId),
    notification: { title, body, ...(image ? { imageUrl: image } : {}) },
    data: {
      type: 'article',
      category_id: categoryId,
      article_id: top.id,
      url: top.data.original_url,
      title: top.data.title,
      source: top.data.source_name || '',
    },
    android: {
      priority: breaking && !quiet ? 'high' : 'normal',
      collapseKey: topicFor(categoryId), // aynı kategoriden gelen eski bildirim yenisiyle değişir
      notification: {
        channelId,
        tag: topicFor(categoryId),
        icon: 'ic_stat_news',
        color: BRAND_COLOR,
      },
    },
  };
}


/** Kategori özeti profilleri (seviye başına ayrı kuyruk). */
const PROFILES = {
  all: { level: 'lvl_all', gapMs: 90 * 60 * 1000, cooldownMs: 3 * 60 * 60 * 1000, dailyCap: 10 },
  // Dengeli bütçesi (günde en fazla 8): 2 bülten + en fazla 4 son dakika/önemli gelişme + en fazla 2 özet
  balanced: { level: 'lvl_balanced', gapMs: 3 * 60 * 60 * 1000, cooldownMs: 6 * 60 * 60 * 1000, dailyCap: 2 },
};
/** Özete girmek için bir hikâyeyi en az kaç farklı yayıncı vermiş olmalı (tek kaynaklı rutin haber akışta kalır). */
const DIGEST_MIN_SOURCES = 3;
/** Tek kategori gönderilebilmesi için gereken minimum yayıncı sayısı. */
const DIGEST_SINGLE_MIN_SOURCES = 3;
/** Gece özet gönderilmez (23:00–08:00); biriken önemli haberleri sabah bülteni toplar. */
const DIGEST_NIGHT_START_MIN = 23 * 60;
const DIGEST_NIGHT_END_MIN = 8 * 60;
/** Bülten saatine bu kadar yakınken ayrıca özet gönderilmez (bülten zaten gündemi verir). */
const DIGEST_BULLETIN_MARGIN_MIN = 90;
const DIGEST_GAP_MS = PROFILES.all.gapMs;
/** Tek özet bildirimde en fazla kategori (FCM koşulu en fazla 5 konu alır: 3 kategori + seviye). */
const DIGEST_MAX_CATEGORIES = 3;

const CALM_TOPIC = 'calm'; // "Sadece önemli" + "Dengeli"
const BULLETIN_TOPIC = 'bulletin';
const MAJOR_MIN_SOURCES = 3;
const MAJOR_COOLDOWN_MS = 30 * 60 * 1000;
const MAJOR_DAILY_CAP = 6;
/** calm (Sadece önemli + Dengeli) için günlük son dakika/önemli gelişme sınırı — KRİTİK dahil. Yalnızca yapay zekânın
 *  "gece=true" (gerçekleşmiş can güvenliği olayı) dediği haber bu sınırı aşabilir. */
const CALM_DAILY_CAP = 4;
const MAJOR_MAX_AGE_MS = 2 * 60 * 60 * 1000;
const MAJOR_SKIP_CATEGORIES = new Set(['spor', 'magazin']); // son dakika kaynağı değilse
const BULLETIN_SLOTS = [
  { minute: 8 * 60 + 30, title: '☀️ 5 dakikada gündem', key: 'am', name: 'Sabah gündemi' },
  { minute: 19 * 60, title: '🌙 Akşam gündemi', key: 'pm', name: 'Akşam gündemi' },
];
const BULLETIN_WINDOW_MIN = 120; // slot saatinden sonra en geç 2 saat içinde
const BULLETIN_ITEMS = 4; // bildirimde görünen satır
const BRIEFING_ITEMS = 7; // uygulamadaki "5 dakikada gündem" ekranı

const { mergeStories, pruneStories, isClickbait } = require('./importance');
const { ROUTINE } = require('./scoring');
const { fold } = require('./quiet-hours');

/** Metni normalize edip ROUTINE kalıplarından herhangi birine uyuyorsa true. */
const isRoutine = (text) => { const t = fold(text); return ROUTINE.some((re) => re.test(t)); };

const levelCondition = (categoryIds, level) => {
  const cats = categoryIds.map((id) => `'${topicFor(id)}' in topics`);
  const left = cats.length > 1 ? `(${cats.join(' || ')})` : cats[0];
  return `${left} && '${level}' in topics`;
};

/** İstanbul tarihine göre gün anahtarı ve günün dakikası. */
function istanbulClock(ms) {
  const parts = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' })
      .formatToParts(new Date(ms)).map((p) => [p.type, p.value]),
  );
  return { day: `${parts.year}-${parts.month}-${parts.day}`, minute: Number(parts.hour) * 60 + Number(parts.minute) };
}

/** Bekleyen kategori kaydı (Firestore'da saklanır; yalnızca düz alanlar). */
function pendingEntry(categoryId, categoryName, articles, prev) {
  const sorted = [...articles].sort((a, b) => b.data.published_at.toMillis() - a.data.published_at.toMillis());
  const top = sorted[0];
  return {
    category_id: categoryId,
    name: categoryName,
    article_id: top.id,
    title: top.data.title,
    url: top.data.original_url,
    source: top.data.source_name || '',
    image: top.data.image_url && top.data.has_original_image !== false ? top.data.image_url : '',
    newest_ms: top.data.published_at.toMillis(),
    count: sorted.length + (prev ? prev.count : 0),
  };
}

/** Birden fazla kategori için TEK özet bildirim (başlıkta sayı yok: "📰 Gündemin öne çıkanları"). */
function buildDigestMessage(entries, now = new Date(), level = 'lvl_all') {
  const lines = entries.map((e) => `${e.name}: ${trimText(e.title, 70)}`);
  const sources = [...new Set(entries.map((e) => e.source).filter(Boolean))];
  const body = [...lines, sources.length ? `Kaynak: ${sources.join(', ')}` : ''].filter(Boolean).join('\n');
  const quiet = isQuietHour(now);
  const image = (entries.find((e) => e.image) || {}).image;
  return {
    condition: levelCondition(entries.map((e) => e.category_id), level),
    notification: { title: '📰 Gündemin öne çıkanları', body, ...(image ? { imageUrl: image } : {}) },
    data: {
      type: 'article',
      category_id: entries[0].category_id,
      article_id: entries[0].article_id,
      url: entries[0].url,
      title: entries[0].title,
      source: entries[0].source,
      digest: entries.map((e) => e.category_id).join(','),
    },
    android: {
      priority: 'normal',
      collapseKey: 'news_digest',
      notification: { channelId: quiet ? 'quiet' : 'news', tag: 'news_digest', icon: 'ic_stat_news', color: BRAND_COLOR },
    },
  };
}

const inBulletinMargin = (minute) =>
  BULLETIN_SLOTS.some((b) => Math.abs(minute - b.minute) < DIGEST_BULLETIN_MARGIN_MIN);
const digestNight = (minute) => minute >= DIGEST_NIGHT_START_MIN || minute < DIGEST_NIGHT_END_MIN;

/**
 * Bir profilin (seviye) kategori özetini planlar.
 * Kuyrukta yalnızca "bu kategoride yeni haber var" bilgisi tutulur; gönderirken her kategorinin, son gönderimden beri
 * EN ÇOK yayıncının verdiği hikâyesi seçilir (en az DIGEST_MIN_SOURCES). Böyle bir hikâye yoksa kategori özete girmez.
 * Başlıkta haber SAYISI yazmaz (eskiden "187 yeni haber" gibi şişen sayılar gidiyordu).
 */
function planDigest(profile, st, categoryAdds, now, stories = []) {
  const lastSent = { ...(st.last_sent || {}) };
  const pending = { ...(st.pending || {}) };
  const { day, minute } = istanbulClock(now);
  let count = st.day === day ? st.count || 0 : 0;
  for (const [categoryId, { name, articles }] of categoryAdds) {
    pending[categoryId] = pendingEntry(categoryId, name, articles, pending[categoryId]);
  }
  const messages = [];
  let lastDigestAt = st.last_digest_at || 0;
  // Gece: kuyruk boşaltılır (sabah bülteni gündemi toplar; sabah eski haber özeti gitmesin).
  if (digestNight(minute)) {
    return { messages, state: { last_sent: lastSent, pending: {}, last_digest_at: lastDigestAt, day, count } };
  }
  if (now - lastDigestAt >= profile.gapMs && count < profile.dailyCap && !inBulletinMargin(minute)) {
    const ready = Object.values(pending)
      .filter((e) => now - (lastSent[e.category_id] || 0) >= profile.cooldownMs)
      .map((e) => {
        const best = stories
          .filter((x) => x.cat === e.category_id && x.srcs.length >= DIGEST_MIN_SOURCES)
          .filter((x) => x.last_ms > (lastSent[e.category_id] || 0) && !isClickbait(x.title))
          .sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms)[0];
        return best
          ? { ...e, article_id: best.id, title: best.title, url: best.url, source: best.source || e.source,
            image: best.image || '', newest_ms: best.last_ms, sources: best.srcs.length }
          : null;
      })
      .filter(Boolean)
      .sort((a, b) => b.sources - a.sources || b.newest_ms - a.newest_ms)
      .slice(0, DIGEST_MAX_CATEGORIES);
    if (ready.length === 1 && ready[0].sources >= DIGEST_SINGLE_MIN_SOURCES) {
      const e = ready[0];
      const articles = [{ id: e.article_id, data: {
        title: e.title, original_url: e.url, source_name: e.source, image_url: e.image,
        has_original_image: Boolean(e.image), published_at: { toMillis: () => e.newest_ms },
      } }];
      const msg = buildNewsMessage({ categoryId: e.category_id, categoryName: e.name, articles, now: new Date(now) });
      delete msg.topic;
      msg.condition = levelCondition([e.category_id], profile.level);
      msg.notification.body += ` · ${e.sources} kaynakta`;
      msg.ttlMs = 2 * 3_600_000; // 2sa — eski bildirim gece teslim edilmesin
      messages.push(msg);
    } else if (ready.length > 1) {
      const msg = buildDigestMessage(ready, new Date(now), profile.level);
      msg.ttlMs = 2 * 3_600_000; // 2sa — eski bildirim gece teslim edilmesin
      messages.push(msg);
    }
    for (const e of ready) {
      lastSent[e.category_id] = now;
      delete pending[e.category_id];
    }
    if (ready.length) {
      lastDigestAt = now;
      count += 1;
    }
  }
  return { messages, state: { last_sent: lastSent, pending, last_digest_at: lastDigestAt, day, count } };
}

/** Önemli son dakika mesajı (hikâyeden). */
function buildMajorMessage(story, now, score = null) {
  const articles = [{ id: story.id, data: {
    title: story.title, original_url: story.url, source_name: story.source, image_url: story.image,
    has_original_image: Boolean(story.image), published_at: { toMillis: () => story.last_ms },
  } }];
  const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles, breaking: true, now: new Date(now) });
  delete msg.topic;
  msg.condition = `'${topicFor(BREAKING_CATEGORY_ID)}' in topics && '${CALM_TOPIC}' in topics`;
  // Başlık seviyeye göre: kritik → SON DAKİKA, önemli → ÖNEMLİ GELİŞME
  const critical = score ? score.level === LEVEL.CRITICAL : story.br;
  if (!critical) msg.notification.title = '⚡ ÖNEMLİ GELİŞME';
  else if (!story.br) msg.notification.title = BREAKING_TITLE;
  // Gece gönderilen can güvenliği haberi sessiz kanala düşmesin (buildNewsMessage saat 00–08'de 'quiet' seçer)
  if (score && score.night) {
    msg.android.notification.channelId = 'breaking_news';
    msg.android.priority = 'high';
  }
  if (score) msg.data.level = String(score.level);
  if (story.srcs.length >= 2) msg.notification.body += ` · ${story.srcs.length} kaynakta`;
  msg.android.collapseKey = 'news_major';
  msg.android.notification.tag = `major_${story.id}`;
  msg.data.importance = String(story.srcs.length);
  return msg;
}

/**
 * Şu anki gündem dilimi: 08:30–19:00 sabah, 19:00–08:30 akşam (gece yarısından sonra önceki günün akşamı).
 * Uygulama da aynı kuralla "beklenen bülten" kimliğini hesaplar (eski bülteni göstermemek için).
 */
function bulletinSlot(now) {
  const clock = istanbulClock(now);
  const slot = clock.minute >= BULLETIN_SLOTS[1].minute ? BULLETIN_SLOTS[1]
    : clock.minute >= BULLETIN_SLOTS[0].minute ? BULLETIN_SLOTS[0] : BULLETIN_SLOTS[1];
  const slotDay = clock.minute < BULLETIN_SLOTS[0].minute ? istanbulClock(now - 12 * 3_600_000).day : clock.day;
  const inWindow = slotDay === clock.day && clock.minute >= slot.minute && clock.minute < slot.minute + BULLETIN_WINDOW_MIN;
  return { slot, slotDay, key: `${slotDay}#${slot.key}`, id: `${slotDay}-${slot.key}`, inWindow };
}

/** Bülten için haberler: son 14 saatte en çok yayıncının verdiği (tıklama tuzağı hariç). */
function pickBriefingStories(stories, now) {
  const since = now - 14 * 3_600_000;
  const fresh = stories.filter((x) => x.last_ms > since && !isClickbait(x.title));
  const picks = [...fresh].sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms).slice(0, BRIEFING_ITEMS);
  return { picks, fresh };
}

/** "5 dakikada gündem" belgesi (briefings/{gün}-{am|pm}); uygulama ekranı bunu okur. */
function buildBriefing(slot, day, stories, now, pool = stories) {
  const item = (x) => ({
    id: x.id, title: x.title, url: x.url, source: x.source || '', image: x.image || '', category_id: x.cat || '',
    sources: x.srcs.slice(0, 12), source_count: x.srcs.length, summary: x.sum || '',
  });
  // İlgi alanlarına göre kısa bölümler: her kategoriden en çok kaynaklı 3 başlık (uygulama kullanıcının seçtiklerini gösterir)
  const byCategory = {};
  for (const x of [...pool].sort((a, b) => b.srcs.length - a.srcs.length || b.last_ms - a.last_ms)) {
    if (!x.cat || x.cat === BREAKING_CATEGORY_ID) continue;
    const list = (byCategory[x.cat] = byCategory[x.cat] || []);
    if (list.length < 3) list.push(item(x));
  }
  return {
    by_category: byCategory,
    id: `${day}-${slot.key}`,
    slot: slot.key,
    title: slot.name,
    day,
    created_ms: now,
    items: stories.map(item),
  };
}

/** Günün bülteni (en çok kaynağın verdiği haberler). */
function buildBulletinMessage(slot, stories, now, briefingId = '') {
  const lines = stories.map((s) => `• ${trimText(s.title, 80)}`);
  const sources = [...new Set(stories.map((s) => s.source).filter(Boolean))].slice(0, 4);
  const top = stories[0];
  return {
    topic: BULLETIN_TOPIC,
    notification: {
      title: slot.title,
      body: [...lines, sources.length ? `Kaynak: ${sources.join(', ')}` : ''].filter(Boolean).join('\n'),
      ...(top.image ? { imageUrl: top.image } : {}),
    },
    data: {
      type: 'article', category_id: top.cat || '', article_id: top.id, url: top.url, title: top.title, source: top.source,
      bulletin: '1', briefing_id: briefingId,
    },
    android: {
      priority: 'normal',
      collapseKey: 'news_bulletin',
      notification: { channelId: isQuietHour(new Date(now)) ? 'quiet' : 'news', tag: 'news_bulletin', icon: 'ic_stat_news', color: BRAND_COLOR },
    },
  };
}

/**
 * Çalışma sonuçlarından gönderilecek bildirimleri seçer (saf fonksiyon; test edilir).
 * @returns {{messages: object[], nextState: object}}
 */
function planNotifications({ results, categories, state = {}, now = Date.now(), ai = null }) {
  const messages = [];
  const next = {};

  // 1) Yeni haberler → kategori kuyrukları (son dakika kategorisi hariç) ve hikâyeler
  const byCategory = new Map();
  const forStories = [];
  for (const r of results) {
    for (const a of r.addedArticles || []) {
      forStories.push(a);
      const id = a.data.category_id && a.data.category_id.id;
      if (!id || id === BREAKING_CATEGORY_ID || isClickbait(a.data.title || '')) continue;
      const category = categories.get(id);
      if (!category) continue;
      if (!byCategory.has(id)) byCategory.set(id, { name: category.name || id, articles: [] });
      byCategory.get(id).articles.push(a);
    }
    for (const a of r.breakingArticles || []) forStories.push({ ...a, breaking: true });
  }
  // 2) Hikâyeler (önem) — aynı haber farklı akışlarda tekrar gelirse yayıncı bir kez sayılır
  const { stories } = mergeStories(state.stories || [], forStories, { now, devKeys: developmentKeys });
  for (const [key, profile] of Object.entries(PROFILES)) {
    const { messages: m, state: s } = planDigest(profile, state[key] || {}, byCategory, now, stories);
    messages.push(...m);
    next[key] = s;
  }

  // "Bu haberi kimler verdi?": bu çalışmada yayıncı sayısı artan (2+) hikâyeler → haber belgesine kaynak listesi
  const before = new Map((state.stories || []).map((x) => [x.id, x.srcs.length]));
  const sourceUpdates = stories
    .filter((x) => x.srcs.length >= 2 && x.srcs.length !== before.get(x.id))
    .map((x) => ({ id: x.id, srcs: x.srcs.slice(0, 12) }));

  // 3) Puanlanmış bildirim (src/scoring.js): seviye 1 beklemeden ve sınırsız, seviye 2 aralık + günlük sınırla.
  //    Gece 23:59–08:00: yalnızca can güvenliği (deprem/savaş/saldırı/afet) gönderilir, diğerleri sabahı bekler.
  //    Seviye 3–4 hiç bildirilmez; özet ve "5 dakikada gündem" içinde görünürler.
  const br = { ...(state.breaking || {}) };
  const { day } = istanbulClock(now);
  if (br.day !== day) Object.assign(br, { day, major_count: 0, sent_titles: [] });
  const night = isNightSilent(new Date(now));
  const ranked = stories
    .map((s) => ({ s, score: scoreStory(s, { now, maxAgeMs: MAJOR_MAX_AGE_MS }) }))
    .filter(({ score }) => score.level <= LEVEL.IMPORTANT)
    .filter(({ score }) => !night || score.night)
    .map((e) => ({ ...e, gate: shouldRenotify(e.s, e.score, { now }) }))
    .filter((e) => e.gate.ok)
    .sort((a, b) => a.score.level - b.score.level
      || Number(b.score.night) - Number(a.score.night)
      || b.s.srcs.length - a.s.srcs.length
      || b.s.last_ms - a.s.last_ms);
  // Karar: yapay zekâ açıksa (ai.enabled) calm bildirimini Gemini seçer (src/ai-notify.js); kararı olmayan adaylar
  // aiPending ile döner, çağıran Gemini'ye sorup planı yeniden kurar. Kapalıysa eski kurallar.
  // Her iki durumda calm için günlük sınır KRİTİK dahil CALM_DAILY_CAP; yalnız gece/can güvenliği kararı aşabilir.
  const { pruneDecisions, MAX_CANDIDATES } = require('./ai-notify');
  const decisions = { ...pruneDecisions(state.ai_decisions, now), ...((ai && ai.decisions) || {}) };
  next.ai_decisions = decisions;
  const capOk = () => (br.major_count || 0) < CALM_DAILY_CAP && now - (br.last_major_at || 0) >= MAJOR_COOLDOWN_MS;
  let pick = null;
  if (ai && ai.enabled) {
    next.aiPending = ranked.filter((e) => !decisions[e.s.id]).slice(0, MAX_CANDIDATES).map((e) => ({ story: e.s, score: e.score }));
    const chosen = ranked
      .filter((e) => decisions[e.s.id] && decisions[e.s.id].ok)
      .filter((e) => !night || decisions[e.s.id].gece)
      .find((e) => decisions[e.s.id].gece || capOk());
    if (chosen) {
      const d = decisions[chosen.s.id];
      pick = { ...chosen, score: { ...chosen.score, level: d.kritik || d.gece ? LEVEL.CRITICAL : LEVEL.IMPORTANT, night: d.gece, reason: `AI: ${d.neden}` } };
    }
  } else {
    pick = ranked.find(({ score }) => (score.level === LEVEL.CRITICAL && score.night) || capOk()) || null;
  }
  if (pick) {
    const { s, score, gate } = pick;
    messages.push(buildMajorMessage(s, now, score));
    // Bildirim geçmişi: tekrar yalnız gerçek gelişmede açılır (scoring.shouldRenotify).
    s.notified_ms = now;
    s.notified_level = score.level;
    s.notified_srcs = s.srcs.length;
    s.notify_count = (s.notify_count || 0) + 1;
    s.pending_dev = [];
    s.major_ms = now; // eski alan: geriye uyum
    br.last_major_at = now;
    br.major_count = (br.major_count || 0) + 1;
    br.sent_titles = [...(br.sent_titles || []), s.title].slice(-12);
    next.last_pick = { id: s.id, level: score.level, reason: score.reason, why: gate.reason };
  }

  // 4) Tüm son dakikalar ("Tümü") — eskisi gibi hemen, 10 dk aralıkla
  const minor = results
    .flatMap((r) => r.breakingArticles || [])
    .filter((a) => now - a.data.published_at.toMillis() < MAX_BREAKING_AGE_MS)
    .filter((a) => a.id !== br.last_minor_id && !isClickbait(a.data.title || '') && !isRoutine(a.data.title || ''));
  if (!night && minor.length && now - (br.last_minor_at || 0) >= BREAKING_COOLDOWN_MS) {
    const msg = buildNewsMessage({ categoryId: BREAKING_CATEGORY_ID, categoryName: 'Son Dakika', articles: minor, breaking: true, now: new Date(now) });
    delete msg.topic;
    msg.condition = `'${topicFor(BREAKING_CATEGORY_ID)}' in topics && '${PROFILES.all.level}' in topics`;
    messages.push(msg);
    br.last_minor_at = now;
    br.last_minor_id = msg.data.article_id;
  }
  next.breaking = br;

  // 5) Bülten (sabah / akşam)
  const bul = { ...(state.bulletin || {}) };
  const { slot, slotDay, key, inWindow } = bulletinSlot(now);
  let briefing = null;
  if (bul.briefing_key !== key || (inWindow && bul.last !== key)) {
    const { picks, fresh } = pickBriefingStories(stories, now);
    if (picks.length >= 2) {
      briefing = buildBriefing(slot, slotDay, picks, now, fresh);
      bul.briefing_key = key;
      // Bildirim yalnızca bülten saatinde (kaçırılan gündem sessizce belge olarak hazırlanır)
      if (inWindow && bul.last !== key) {
        messages.push(buildBulletinMessage(slot, picks.slice(0, BULLETIN_ITEMS), now, briefing.id));
        bul.last = key;
        bul.last_ms = now;
      }
    }
  }
  next.bulletin = bul;
  next.stories = pruneStories(stories, { now, keepSinceMs: bul.last_ms });

  return { messages, nextState: next, briefing, sourceUpdates };
}

/**
 * FCM'e giden biçim: Android'de VERİ mesajı (uygulama bildirimi kendisi kurar, "Daha az bildirim" düğmesiyle),
 * iOS'ta apns uyarısı. İç biçim (notification alanlı) kayıt ve testler için korunur.
 */
function toWire(m) {
  const n = m.notification;
  const an = (m.android && m.android.notification) || {};
  const data = {};
  for (const [k, v] of Object.entries(m.data || {})) data[k] = String(v ?? '');
  Object.assign(data, {
    kind: 'news_alert',
    n_title: n.title,
    n_body: n.body || '',
    n_image: n.imageUrl || '',
    n_channel: an.channelId || 'news',
    n_tag: an.tag || '',
  });
  return {
    ...(m.topic ? { topic: m.topic } : { condition: m.condition }),
    data,
    android: { priority: (m.android && m.android.priority) || 'normal', collapseKey: m.android && m.android.collapseKey, ttl: m.ttlMs || 6 * 3_600_000 },
    apns: {
      headers: { 'apns-priority': an.channelId === 'quiet' ? '5' : '10' },
      payload: {
        aps: {
          alert: { title: n.title, body: n.body || '' },
          // iPhone: son dakika ayrı, diğer haber bildirimleri tek grupta (yığın) toplanır.
          'thread-id': an.channelId === 'breaking_news' ? 'breaking' : an.tag === 'news_bulletin' ? 'bulletin' : 'news',
          // Gece (quiet kanalı): sessiz ve pasif; aksi halde varsayılan ses.
          ...(an.channelId === 'quiet' ? { 'interruption-level': 'passive' } : { sound: 'default' }),
        },
      },
    },
  };
}

/** Bülten gövdesi: AI özeti varsa özet + ilk iki başlık, yoksa başlık listesi (saf fonksiyon). */
function withAiSummary(message, aiSummary) {
  if (!message || !aiSummary) return message;
  const lines = String(message.notification.body || '').split('\n').filter((l) => l.startsWith('• ')).slice(0, 2);
  return { ...message, notification: { ...message.notification, body: [trimText(aiSummary, 320), ...lines].join('\n') } };
}

/**
 * Gündem belgesi (+ Gemini AI özeti) ve "kimler verdi" listeleri. Haber silinmişse (telif/temizlik) güncelleme atlanır —
 * belge yeniden oluşturulmaz (update kullanılır). Hata bildirimleri bozmaz.
 * `notified`: bu bülten bildirim olarak gönderildi → social.js Make'e de bir kez iletir.
 * @returns {Promise<string>} AI özeti (yoksa boş)
 */
async function writeExtras(db, briefing, sourceUpdates = [], { notified = false, summarize } = {}) {
  let aiSummary = '';
  try {
    if (briefing) {
      const { Timestamp } = require('firebase-admin/firestore');
      try {
        aiSummary = await (summarize || require('./gemini').summarizeBriefing)(briefing.items, { db });
      } catch (err) {
        logger.warn('AI özeti alınamadı', { error: err.message });
      }
      const doc = { ...briefing, ai_summary: aiSummary, notified, created_at: Timestamp.fromMillis(briefing.created_ms) };
      await db.doc(`briefings/${briefing.id}`).set(doc);
      await db.doc('briefings/latest').set(doc);
    }
    await Promise.allSettled(
      sourceUpdates.map((u) => db.doc(`articles/${u.id}`).update({ also_by: u.srcs, source_count: u.srcs.length })),
    );
  } catch (err) {
    logger.warn('Gündem/kaynak listesi yazılamadı', { error: err.message });
  }
  return aiSummary;
}

/** Bildirimleri gönderir ve durumu kaydeder. Hata bildirimi çekimi bozmaz. */
/** app_meta/ai.notify_ai (vars. true): calm bildirimlerini Gemini seçsin mi. */
async function aiNotifyEnabled(db) {
  try {
    const d = (await db.doc('app_meta/ai').get()).data() || {};
    return d.enabled !== false && d.notify_ai !== false;
  } catch {
    return false;
  }
}

async function sendNewsNotifications(db, results, categories, { messaging = getMessaging(), now = Date.now(), summarize, judge } = {}) {
  const ref = db.doc('notify_state/news');
  try {
    const snap = await ref.get();
    const state = snap.data() || {};
    const aiEnabled = (await aiNotifyEnabled(db)) && Boolean(process.env.GEMINI_API_KEY || judge);
    let planned = planNotifications({ results, categories, state, now, ai: { enabled: aiEnabled } });
    const pending = planned.nextState.aiPending || [];
    if (aiEnabled && pending.length) {
      // Gemini'ye bugün gönderilenlerle birlikte adayları sor, kararlarla planı yeniden kur (plan saf fonksiyondur).
      const { judgeCandidates } = require('./ai-notify');
      const sentToday = ((state.breaking || {}).day === istanbulClock(now).day && (state.breaking || {}).sent_titles) || [];
      const { decisions, error } = await judgeCandidates(pending, sentToday, { db, now, generate: judge });
      if (error) logger.warn('AI bildirim editörü yanıt vermedi, kurallar kullanıldı', { error });
      planned = planNotifications({ results, categories, state, now, ai: { enabled: true, decisions } });
      logger.info('AI bildirim kararları', { decisions: Object.entries(decisions).map(([id, d]) => `${id.slice(0, 6)}:${d.ok ? 'GÖNDER' : 'hayır'}${d.kritik ? '/kritik' : ''} ${d.neden}`) });
    }
    delete planned.nextState.aiPending;
    const { nextState, briefing, sourceUpdates } = planned;
    const hasBulletin = planned.messages.some((m) => m.topic === BULLETIN_TOPIC);
    const aiSummary = await writeExtras(db, briefing, sourceUpdates, { notified: hasBulletin, summarize });
    // Bülten bildirimine AI özeti eklenir (özet yoksa başlık listesi kalır).
    const messages = planned.messages.map((m) => (m.topic === BULLETIN_TOPIC ? withAiSummary(m, aiSummary) : m));
    if (messages.length === 0) {
      await ref.set(nextState); // kuyruk ve hikâyeler her çalışmada güncellenir (birleştirme değil: silinenler gitsin)
      return { sent: 0 };
    }
    const res = await messaging.sendEach(messages.map(toWire));
    await ref.set(nextState);
    await require('./notification-log').logSentMessages(db, messages, res.responses, { now });
    const failed = res.responses.filter((r) => !r.success).map((r) => r.error && r.error.message);
    logger.info('Haber bildirimleri gönderildi', {
      sent: res.successCount,
      failed: res.failureCount,
      targets: messages.map((m) => m.topic || m.condition),
      ...(failed.length ? { errors: failed } : {}),
    });
    return { sent: res.successCount, failed: res.failureCount, targets: messages.map((m) => m.topic || m.condition) };
  } catch (err) {
    logger.error('Haber bildirimi gönderilemedi', { error: err.message });
    return { sent: 0, error: err.message };
  }
}

module.exports = {
  BREAKING_TITLE,
  BULLETIN_SLOTS,
  BULLETIN_ITEMS,
  bulletinSlot,
  pickBriefingStories,
  buildBriefing,
  buildBulletinMessage,
  writeExtras,
  withAiSummary,
  CALM_TOPIC,
  BULLETIN_TOPIC,
  PROFILES,
  buildDigestMessage,
  DIGEST_GAP_MS,
  buildNewsMessage,
  planNotifications,
  sendNewsNotifications,
  toWire,
  topicFor,
  istanbulClock,
  CATEGORY_COOLDOWN_MS,
  BREAKING_COOLDOWN_MS,
  MAJOR_DAILY_CAP,
  CALM_DAILY_CAP,
};
