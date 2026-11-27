'use strict';

/**
 * Haber önem puanlaması — 4 seviye (saf fonksiyonlar; Firestore'a ve RSS'e dokunmaz).
 *
 *  1 KRİTİK : Beklemeden gider, günlük sınıra takılmaz.
 *             `night: true` olanlar (deprem, savaş, saldırı, afet) gece 23:59–08:00 arasında DA gider.
 *             Gece izni olmayan seviye 1 (büyük siyasi/ekonomik karar, büyük kaza) sabahı bekler.
 *  2 ÖNEMLİ : Aralık ve günlük sınırla gider; gece gitmez.
 *  3 NORMAL : Yalnızca kategori özeti ve "5 dakikada gündem" içinde.
 *  4 DÜŞÜK  : Hiç bildirilmez (tıklama tuzağı, magazin, rutin/servis içeriği, tek kaynaklı eski haber).
 */

const { fold, isCritical } = require('./quiet-hours');
const { isClickbait } = require('./importance');

const LEVEL = { CRITICAL: 1, IMPORTANT: 2, NORMAL: 3, LOW: 4 };

/** Hikâyenin puanlanacak metni: temsilci başlık + özet (özet hikâyede saklanıyorsa). */
const storyText = (s) => fold([s.title, s.sum].filter(Boolean).join(' '));

const EVENT_HAPPENED = [
  /meydana geldi|\bsarsildi\b|buyuklugunde (deprem|sarsinti)|deprem oldu|\bdeprem!|\bzelzele/,
  /saldiri(si)? duzenlendi|\bvuruldu\b|\bbombalandi|roket (atildi|isabet)|fuze (atildi|isabet|vurdu)/,
  /patlama (oldu|meydana|sesi)|yangin cikti|\balev aldi|\binfilak/,
  /savas ilan edildi|savas basladi|seferberlik ilan|olaganustu hal ilan|\bohal ilan|darbe girisimi/,
  /\benkaz\b|\bafad\b|\bkandilli\b|\bakut\b|arama kurtarma (basladi|ekipleri)/,
  /tahliye (basladi|ediliyor|edildi)|siginaklara|hava sahasi kapatildi/,
];
const QUOTE_HEADLINE = /^(?!son dakika|flas|acil|canli|dikkat|uyari|resmi)[^:]{3,45}:\s/;
const OPINION = /\bdedi\b|\bsoyledi\b|\buyardi\b|\bcagrisi\b|degerlendirme|\byorumu\b|\banalizi\b|\broportaj|anma toreni|\byildonumu|\btepki gosterdi/;

function worthWakingUp(story, text) {
  if (QUOTE_HEADLINE.test(text) || OPINION.test(text)) return false;
  if (!EVENT_HAPPENED.some((re) => re.test(text))) return false;
  const quake = /\bdeprem|\bzelzele|\bsarsinti/.test(text);
  return quake || sourceCount(story) >= 2 || story.br === true;
}

const MAJOR_EVENT = [
  /\bmaden (kazasi|faciasi)|\bgocuk\b|\bucak dustu|\bucak kazasi|helikopter dustu|tren kazasi|\bbatti\b.*\bgemi|feribot batti/,
  /\bpatlama\b|infilak|dogalgaz patlamasi|fabrika yangini|\bfacia\b|\bcokme\b|bina coktu|coken bina/,
  /\btoplu (olum|zehirlenme)|\bsalgin\b|karantina ilan/,
  /secim karari|erken secim|\bsecim takvimi|referandum karari/,
  /kabine (degisikligi|revizyonu)|hukumet kuruldu|guven oyu|gensoru/,
  /(bakan|basbakan|cumhurbaskani|genel baskan|belediye baskani)[a-z ]{0,12}(istifa|gorevden al|tutukland|gozaltina al)/,
  /anayasa (degisikligi|mahkemesi karari)|meclis(ten| )?(kabul|onay)/,
  /\bkayyum atand|parti kapatma davasi|dokunulmazlik/,
  /faiz karari|politika faizi|faizi (artirdi|indirdi|sabit)|merke[a-z]* bank[a-z]* (karar|faiz)/,
  /asgari ucret (zammi|belli|aciklandi|karari)|memur (maas|zam) (zammi|karari|belli)|emekli (maas|zam)[a-z]* (karar|belli|aciklandi)/,
  /\bdolar\b.*\brekor|euro.*\brekor|\baltin\b.*\brekor kirdi|borsa.*(tarihi zirve|rekor)/,
  /akaryakit[a-z]* (zam|indirim)|dogalgaz[a-z]* zam|elektrige zam|\bzam yapildi|vergi (artisi|duzenlemesi)/,
  /\bkur korumali|\bmoratoryum|\bdevalu/,
  /\bkayip (ilani|cocuk)|\bihbar hatti|kamu spotu|valilik (yasakladi|karari)|sokaga cikma/,
  /\bokullar tatil|\begitime ara|\buzaktan egitim karari|\bizin verildi.*tahliye/,
  /siber saldiri|veri ihlali|\bdolandiricilik uyarisi/,
];

const IMPORTANT = [
  /\baciklama yapti|\baciklamasi\b|\bkarar aldi|\byasa teklifi|\bduzenleme\b|\bgenelge\b|\byonetmelik/,
  /\bsorusturma baslatildi|\bdava acildi|\bmahkeme karari|\bceza verildi|\bgozaltina alindi|\btutuklandi/,
  /\bprotesto|\bgrev\b|\beylem yapildi|\bmiting/,
  /\benflasyon\b|\btufe\b|\byufe\b|issizlik orani|buyume orani|cari acik|butce acigi/,
  /\bihracat\b|\bithalat\b|\byatirim karari|\bteshvik|\bkredi (faizi|paketi)|\bdestek paketi/,
  /\biflas\b|\bkonkordato|\bsatin aldi|\bbirlesme anlasmasi|\bhalka arz/,
  /\btanitildi\b|\bduyurdu\b|\bpiyasaya surdu|\bguncelleme yayinlandi|\berisim engeli|\bkapatildi\b/,
  /yapay zeka|\bchatgpt|\bstarlink|\buydu firlat/,
  /\btransfer (anlasmasi|bombasi)|\bimzayi atti|\bayrilik karari|\bteknik direktor (ayrildi|gorevden|atandi)/,
  /\bsampiyon oldu|\bkupayi kaldirdi|\bligden dusme|\bmilli takim (kadrosu|aday kadro)/,
  /\bpfdk\b|\bceza kurulu|\bhukmen maglup|\bpuan silme/,
];

const ROUTINE = [
  /\bhangi kanalda|\bsaat kacta|\bne zaman\b|\bkacta baslayacak|\bcanli izle|\bcanli yayin link|\bmuhtemel 11|\bilk 11|\b11ler belli/,
  /\bpuan durumu|\bfikstur|\bmac ozeti|\bgolleri izle|\bmac sonucu nedir/,
  /\bhava durumu|\bnamaz vakitleri|\bimsakiye|\biftar saati|\bnobetci eczane|\btrafige kapali/,
  /\bburclar|\bgunluk burc|\bruya tabiri|\bfal\b/,
  /\btarifi\b|\bnasil yapilir|\bpuf noktalari|\bkac kalori/,
  /\bsorgulama ekrani|\btc kimlik ile|\bbasvuru nasil|\be-devlet uzerinden|\bsonuclari aciklandi mi/,
  /\bzamani geldi mi|\bne kadar oldu|\bguncel fiyat listesi|\bfiyatlari ne kadar/,
  /\bdogum gunu|\bkim kimdir|\bkac yasinda|\bhayati ve|\bbiyografisi/,
  /aktuel firsatlar|\bbim aktuel|\ba101 aktuel|\bsok aktuel|market firsatlari|haftalik firsatlar|indirimli urunler/,
  /\bdeprem mi oldu\b|az once.*\bdeprem\b|nerede.*\bdeprem.*\?|\bdeprem sorgula|\bdeprem.*mi \?|son depremler listesi/,
  /izleme linki|bolum izleme|izle.*link|dizi.*kac bolum|yeni bolum ne zaman|yayin tarihi belli mi|dizi baslangic tarihi/,
];

/** Hikâye kaç farklı yayıncıda yer aldı? */
const sourceCount = (s) => (Array.isArray(s.srcs) ? s.srcs.length : 0);
const anyMatch = (list, text) => list.some((re) => re.test(text));

function scoreStory(story, { now = Date.now(), maxAgeMs = 3 * 3_600_000, maxTotalMs = 12 * 3_600_000 } = {}) {
  const text = storyText(story);
  const n = sourceCount(story);
  const breaking = story.br === true;

  if (isClickbait(story.title || '')) return { level: LEVEL.LOW, night: false, reason: 'tıklama tuzağı' };
  if (anyMatch(ROUTINE, text)) return { level: LEVEL.LOW, night: false, reason: 'rutin/servis içeriği' };

  if (isCritical(story.title, story.sum)) {
    const night = worthWakingUp(story, text);
    return { level: LEVEL.CRITICAL, night, reason: night ? 'can güvenliği' : 'can güvenliği (gündüz)' };
  }

  const last = story.last_ms || story.first_ms || now;
  const old = now - last > maxAgeMs || now - (story.first_ms || now) > maxTotalMs;

  if (anyMatch(MAJOR_EVENT, text) && (n >= 2 || breaking) && !old) {
    return { level: LEVEL.CRITICAL, night: false, reason: 'büyük gelişme' };
  }
  if (n >= 5 && !old) return { level: LEVEL.CRITICAL, night: false, reason: `${n} kaynak` };

  if (n >= 3 && !old) return { level: LEVEL.IMPORTANT, night: false, reason: `${n} kaynak` };
  if (n >= 2 && anyMatch(IMPORTANT, text) && !old) return { level: LEVEL.IMPORTANT, night: false, reason: 'önemli gelişme' };
  if (breaking && anyMatch(IMPORTANT, text) && !old) return { level: LEVEL.IMPORTANT, night: false, reason: 'son dakika kaynağı' };

  if (n < 2 && old) return { level: LEVEL.LOW, night: false, reason: 'tek kaynak, eski' };

  return { level: LEVEL.NORMAL, night: false, reason: 'normal' };
}

const DEVELOPMENTS = [
  ['olum', /hayatini kaybetti|\bolu sayisi|can kaybi|\bvefat etti|\bsehit/],
  ['yarali', /\byarali sayisi|\byaralandi\b|\byaralilar/],
  ['gozalti', /\bgozaltina alindi|\byakalandi\b|\betkisiz hale/],
  ['tutuklama', /\btutuklandi\b|\badli kontrol|\bcezaevine/],
  ['istifa', /\bistifa etti|\bgorevden alindi|\bistifasi kabul/],
  ['sorusturma', /\bsorusturma baslatildi|\bgozalti karari|\byakalama karari/],
  ['iptal', /\biptal edildi|\bertelendi\b|\bdurduruldu\b|\baskiya alindi/],
  ['karar', /\bkarar aciklandi|\bkarar verildi|\bonaylandi\b|\bimzalandi\b|\byururluge girdi/],
  ['aciklama', /\bresmi aciklama|\bvalilik aciklamasi|\bbakanliktan aciklama/],
  ['sonuc', /\bsona erdi|\btamamlandi\b|\bkurtarildi\b|\benkazdan cikarildi/],
  ['artis', /\bsayisi yukseldi|\bsayisi artti|\bbuyuklugu .*(yukseltildi|revize)/],
];

function developmentKeys(...texts) {
  const t = fold(texts.filter(Boolean).join(' '));
  return DEVELOPMENTS.filter(([, re]) => re.test(t)).map(([k]) => k);
}

function newDevelopments(story, ...texts) {
  const seen = new Set(story.dev || []);
  return developmentKeys(...texts).filter((k) => !seen.has(k));
}

const RENOTIFY_GAP_MS = 90 * 60_000;
const RENOTIFY_MAX = 2;
const RENOTIFY_MIN_NEW_SOURCES = 3;

function shouldRenotify(story, score, { now = Date.now() } = {}) {
  const last = story.notified_ms || story.major_ms || 0;
  if (!last) return { ok: true, reason: 'ilk bildirim' };
  if ((story.notify_count || 1) >= 1 + RENOTIFY_MAX) return { ok: false, reason: 'tekrar sınırı' };
  if (now - last < RENOTIFY_GAP_MS) return { ok: false, reason: 'çok erken' };
  if (score.level > LEVEL.IMPORTANT) return { ok: false, reason: 'önemi düşük' };

  const upgraded = score.level < (story.notified_level || LEVEL.NORMAL);
  const grew = sourceCount(story) - (story.notified_srcs || 0) >= RENOTIFY_MIN_NEW_SOURCES;
  const developed = (story.pending_dev || []).length > 0;
  if (score.level === LEVEL.CRITICAL && (upgraded || developed)) return { ok: true, reason: 'kritik gelişme' };
  if (developed && grew) return { ok: true, reason: 'yeni gelişme' };
  if (developed && score.level === LEVEL.IMPORTANT && upgraded) return { ok: true, reason: 'önem arttı' };
  return { ok: false, reason: 'yeni gelişme yok' };
}

module.exports = {
  LEVEL,
  scoreStory,
  developmentKeys,
  newDevelopments,
  shouldRenotify,
  storyText,
  MAJOR_EVENT,
  IMPORTANT,
  ROUTINE,
  worthWakingUp,
  RENOTIFY_GAP_MS,
  RENOTIFY_MAX,
  RENOTIFY_MIN_NEW_SOURCES,
};
