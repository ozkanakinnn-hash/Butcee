'use strict';

/**
 * Canlı skor teşhisi (Cloud Shell'den, proje sahibinin kimliğiyle):
 *
 *   cd ~/haber/news-aggregator/firebase/functions
 *   GCLOUD_PROJECT=haber-68a3c HIGHLIGHTLY_API_KEY="$(gcloud secrets versions access latest --secret=HIGHLIGHTLY_API_KEY --project haber-68a3c)" \
 *     node scripts/mac-teshis.js [--duzelt]
 *
 * Yazdırır: football_meta/state özeti, bugünün maç kayıtları (İstanbul saatiyle), footballTick'in ŞU AN vereceği karar
 * ve sağlayıcının bugünkü HAM maç listesi (1 istek). Anahtar ekrana yazdırılmaz.
 * --duzelt: next_poll_at ve plan_blocked_* alanlarını siler, bugünün maçlarını sağlayıcıdan yeniden yazar
 *           → yüklü footballTick bir dakika içinde paneli gönderir (bu betik bildirim GÖNDERMEZ).
 */

const { getFirestore, FieldValue, Timestamp } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { initAdmin } = require('./init-admin');
const fb = require('../src/football');
const job = require('../src/football-job');
const { Highlightly } = require('../src/football-highlightly');

const ist = (ms) => new Intl.DateTimeFormat('tr-TR', { timeZone: 'Europe/Istanbul', dateStyle: 'short', timeStyle: 'medium' }).format(new Date(ms));
const tsMs = (t) => (t && t.toMillis ? t.toMillis() : null);

/** --deneme <konu>: haber bildirimiyle aynı biçimde (Android veri + iOS apns uyarısı) tek deneme bildirimi. */
async function testTopic(topic, token = null) {
  const { toWire } = require('../src/notify');
  const msg = toWire({
    topic: token ? undefined : topic,
    notification: { title: '🔔 Haber Akışı deneme', body: `Bu bir deneme bildirimidir (${topic}). Geldiyse bildirim zinciri çalışıyor.` },
    data: { type: 'test' },
    android: { priority: 'high', notification: { channelId: 'breaking_news', tag: 'deneme' } },
  });
  if (token) {
    delete msg.condition;
    msg.token = token;
  }
  const res = await getMessaging().sendEach([msg]);
  const r = res.responses[0];
  console.log(`\n=== Deneme bildirimi → ${token ? 'DOĞRUDAN cihaz jetonu' : `konu "${topic}"`} ===`);
  console.log(r.success ? `  ✔ FCM kabul etti (${r.messageId}). Konuya abone her cihaza gitmeli.` : `  ✘ FCM reddetti: ${r.error && r.error.code} ${r.error && r.error.message}`);
}

async function main() {
  const ji = process.argv.indexOf('--jeton');
  if (ji > 0) {
    initAdmin();
    return testTopic('jeton', process.argv[ji + 1]);
  }
  const di = process.argv.indexOf('--deneme');
  if (di > 0) {
    initAdmin();
    return testTopic(process.argv[di + 1] || 'team_galatasaray');
  }
  const fix = process.argv.includes('--duzelt');
  const send = process.argv.includes('--gonder');
  initAdmin();
  const db = getFirestore();
  const now = Date.now();
  const today = fb.istanbulDate();
  const meta = (await db.doc('football_meta/state').get()).data() || {};

  console.log(`\n=== football_meta/state (şimdi ${ist(now)}, bugün ${today}) ===`);
  for (const k of ['fixtures_date', 'league_count_today', 'national_date', 'req_day', 'req_used', 'last_remaining', 'last_remaining_header', 'provider_league_id', 'provider_base', 'season', 'followed_teams', 'plan_blocked_reason']) {
    console.log(`  ${k}: ${JSON.stringify(meta[k])}`);
  }
  for (const k of ['next_poll_at', 'plan_blocked_until', 'follows_at', 'teams_synced_at']) {
    console.log(`  ${k}: ${tsMs(meta[k]) ? ist(tsMs(meta[k])) : '-'}${tsMs(meta[k]) > now ? '  ← GELECEKTE' : ''}`);
  }

  console.log('\n=== matches (tüm kayıtlar) ===');
  const all = await db.collection('matches').get();
  for (const d of all.docs) {
    const m = d.data();
    console.log(`  ${d.id} | ${m.home && m.home.id} - ${m.away && m.away.id} | başlama ${tsMs(m.kickoff) ? ist(tsMs(m.kickoff)) : '?'} | ${m.status && m.status.short} ${m.status && m.status.elapsed != null ? m.status.elapsed + "'" : ''} | canlı=${m.is_live} bitti=${m.is_finished} | skor ${m.goals ? `${m.goals.home}-${m.goals.away}` : '?'}${m.national ? ' | milli' : ''}`);
  }

  console.log('\n=== footballTick şu an ne yapardı? ===');
  const steps = [];
  if (tsMs(meta.plan_blocked_until) > now) steps.push('DURUR: plan_blocked_until gelecekte');
  const active = await job.activeMatches(db, now);
  steps.push(`başlama penceresindeki (−4 sa … +35 dk) bitmemiş maçlar: ${active.map((m) => `${m.home.id}-${m.away.id}`).join(', ') || 'YOK → idle'}`);
  const followed = Array.isArray(meta.followed_teams) ? meta.followed_teams : null;
  const inv = active.filter((m) => m.national || !followed || followed.includes(m.home.id) || followed.includes(m.away.id));
  steps.push(`takip edilenlerle kesişen: ${inv.map((m) => `${m.home.id}-${m.away.id}`).join(', ') || 'YOK → idle-unfollowed'}`);
  if (inv.length) {
    const anyLive = inv.some((m) => m.is_live);
    const next = Math.min(...inv.map((m) => tsMs(m.kickoff)));
    if (!anyLive && next - now > 90_000) steps.push(`waiting-kickoff: ilk başlama ${ist(next)}`);
    if (tsMs(meta.next_poll_at) > now) steps.push(`throttled: next_poll_at ${ist(tsMs(meta.next_poll_at))}`);
    const knownLeft = meta.req_day === today && typeof meta.last_remaining_header === 'number' ? meta.last_remaining_header : null;
    if (knownLeft !== null && knownLeft <= 1) steps.push(`out-of-requests: last_remaining_header=${knownLeft}`);
    if (meta.req_day === today && (meta.req_used || 0) >= 100) steps.push(`out-of-requests: req_used=${meta.req_used}`);
  }
  steps.forEach((s) => console.log(`  - ${s}`));

  // --gonder: takip edilen canlı/başlamak üzere maçların panelini ŞİMDİ gönder ve FCM cevabını yazdır
  if (send) {
    const live = inv.length ? inv : active;
    if (!live.length) console.log('\n(--gonder: gönderilecek maç yok)');
    for (const m of live) {
      const msg = fb.buildLiveMessage(m, Date.now());
      try {
        const res = await getMessaging().sendEach([msg]);
        const r = res.responses[0];
        console.log(`\n=== Panel gönderimi ${m.home.id}-${m.away.id} → koşul ${msg.condition} ===`);
        console.log(r.success ? `  ✔ FCM kabul etti (mesaj ${r.messageId})` : `  ✘ FCM reddetti: ${r.error && r.error.code} ${r.error && r.error.message}`);
      } catch (err) {
        console.log(`  ✘ Gönderilemedi: ${err.message}`);
      }
    }
  }

  const key = (process.env.HIGHLIGHTLY_API_KEY || '').trim();
  if (!key || key === '-') {
    console.log('\n(HIGHLIGHTLY_API_KEY verilmedi: sağlayıcı yanıtı atlandı)');
  } else {
    const api = new Highlightly({ key });
    if (meta.provider_league_id) api.leagueId = meta.provider_league_id;
    if (meta.provider_base) api.base = meta.provider_base;
    console.log(`\n=== Sağlayıcı: bugünün lig maçları (lig ${api.leagueId || '?'}) ===`);
    const raw = await api.raw('/matches', { leagueId: api.leagueId || (await api.findLeague()), date: today, timezone: 'Europe/Istanbul', limit: 100 });
    const list = Array.isArray(raw) ? raw : raw.data || [];
    for (const m of list) {
      const st = m.state || {};
      console.log(`  ${m.id} | ${(m.homeTeam || {}).name} - ${(m.awayTeam || {}).name} | date=${m.date} | durum=${st.description} dk=${st.clock} skor=${JSON.stringify(st.score)}`);
    }
    console.log(`  (kalan istek başlıktan: ${api.remaining ?? 'bildirilmedi'})`);

    if (fix) {
      const fixtures = list.map((m) => require('../src/football-highlightly').toFixture(m));
      const batch = db.batch();
      for (const f of fixtures) {
        const doc = fb.matchDocFromFixture(f);
        batch.set(db.doc(`matches/${doc.fixture_id}`), { ...doc, updated_at: Timestamp.now() }, { merge: true });
      }
      batch.set(db.doc('football_meta/state'), {
        next_poll_at: FieldValue.delete(), plan_blocked_until: FieldValue.delete(), plan_blocked_reason: FieldValue.delete(),
        follows_at: FieldValue.delete(), fixtures_date: today, league_count_today: fixtures.length,
      }, { merge: true });
      await batch.commit();
      console.log(`\n✔ Düzeltildi: ${fixtures.length} maç yeniden yazıldı, bekleme/engel alanları silindi. footballTick 1 dk içinde paneli göndermeli.`);
    }
  }
}

main().catch((err) => {
  console.error('HATA:', err.message);
  process.exit(1);
});
