'use strict';

/**
 * Durum raporu (YALNIZCA OKUR): Make/sosyal ayarı, son paylaşımlar, bülten, canlı skor, piyasa.
 * Gizli değerler (webhook adresi, jetonlar) maskelenir.
 *
 *   GOOGLE_APPLICATION_CREDENTIALS=... node scripts/durum-raporu.js
 */

const { getFirestore } = require('firebase-admin/firestore');
const { initAdmin } = require('./init-admin');

const mask = (v) => {
  const s = String(v || '');
  if (!s) return '(boş)';
  try {
    const u = new URL(s);
    return `${u.protocol}//${u.host}/…${s.slice(-4)} (${s.length} karakter)`;
  } catch {
    return `…${s.slice(-4)} (${s.length} karakter)`;
  }
};
const when = (ts) => (ts && ts.toDate ? ts.toDate().toISOString().replace('T', ' ').slice(0, 16) + ' UTC' : '-');

async function main() {
  initAdmin();
  const db = getFirestore();
  const line = (...a) => console.log(...a);

  line('══ Make / sosyal paylaşım (app_meta/social) ══');
  const social = (await db.doc('app_meta/social').get()).data();
  if (!social) {
    line('  ✘ Belge YOK → Make\'e hiçbir şey gönderilmez.');
  } else {
    for (const k of ['enabled', 'dry_run', 'x_enabled', 'daily_cap', 'min_gap_min', 'test_ping', 'test_result']) {
      line(`  ${k}: ${JSON.stringify(social[k])}`);
    }
    line(`  x_webhook: ${mask(social.x_webhook)}`);
    line(`  channel (Telegram): ${social.channel ? 'var' : 'yok'} | test_at: ${when(social.test_at)}`);
    const problems = [];
    if (social.enabled !== true) problems.push('enabled true değil');
    if (social.x_enabled !== true) problems.push('x_enabled true değil');
    if (!/^https:\/\/\S+$/.test(String(social.x_webhook || ''))) problems.push('x_webhook geçerli bir https adresi değil');
    if (social.dry_run !== false) problems.push('dry_run açık (deneme modu: gönderilmez, yalnızca social_log)');
    line(problems.length ? `  ⚠ ${problems.join(' | ')}` : '  ✔ Ayar tamam');
  }
  const logs = await db.collection('social_log').orderBy('at', 'desc').limit(6).get();
  line(`  Son paylaşım kayıtları (${logs.size}):`);
  for (const d of logs.docs) {
    const x = d.data();
    line(`   - ${when(x.at)} ${x.dry_run ? '[deneme]' : ''} ${String(x.title || '').slice(0, 60)} ${x.errors ? `HATA: ${x.errors.join(', ')}` : ''}`);
  }
  const st = (await db.doc('notify_state/social').get()).data() || {};
  line(`  notify_state/social: gün=${st.day || '-'} sayı=${st.count || 0} son=${st.last_at ? new Date(st.last_at).toISOString() : '-'}`);

  line('\n══ Bülten ══');
  const latest = (await db.doc('briefings/latest').get()).data();
  line(`  briefings/latest: ${latest ? `${latest.id} (oluşturma ${when(latest.created_at)}, ${(latest.items || []).length} madde)` : 'YOK'}`);
  const news = (await db.doc('notify_state/news').get()).data() || {};
  line(`  notify_state/news: hikâye=${(news.stories || []).length} bülten.last=${(news.bulletin || {}).last || '-'}`);

  line('\n══ Canlı skor (football_meta/state) ══');
  const fm = (await db.doc('football_meta/state').get()).data() || {};
  for (const [k, v] of Object.entries(fm)) {
    line(`  ${k}: ${v && v.toDate ? when(v) : JSON.stringify(v).slice(0, 160)}`);
  }
  const recent = await db.collection('matches').orderBy('kickoff', 'desc').limit(3).get();
  for (const d of recent.docs) {
    const m = d.data();
    line(`  maç: ${when(m.kickoff)} ${m.home && m.home.name} ${m.goals ? `${m.goals.home}-${m.goals.away}` : ''} ${m.away && m.away.name} | canlı=${m.is_live} bitti=${m.is_finished} | güncelleme ${when(m.updated_at)}`);
  }

  line('\n══ Haber çekimi ══');
  const last = await db.collection('articles').orderBy('fetched_at', 'desc').limit(1).get().catch(() => null);
  if (last && !last.empty) line(`  son çekilen haber: ${when(last.docs[0].data().fetched_at)}`);
  const pub = await db.collection('articles').orderBy('published_at', 'desc').limit(1).get();
  if (!pub.empty) line(`  en yeni haber yayın zamanı: ${when(pub.docs[0].data().published_at)}`);
}

main().then(() => process.exit(0)).catch((err) => {
  console.error('Rapor hatası:', err.message);
  process.exit(1);
});
