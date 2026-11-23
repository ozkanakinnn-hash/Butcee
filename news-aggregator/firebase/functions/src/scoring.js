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
 *
 * Puan yalnız kategoriye bakmaz: başlık + özet metni, kaç farklı yayıncının verdiği, haberin yaşı ve
 * "son dakika kaynağından mı geldiği" birlikte değerlendirilir.
 *
 * Tekrar bildirimi: bir hikâye bildirildikten sonra ancak GERÇEK bir gelişme olursa yeniden bildirilir
 * (bkz. newDevelopments / shouldRenotify).
 */

const { fold, isCritical } = require('./quiet-hours');
const { isClickbait } = require('./importance');

const LEVEL = { CRITICAL: 1, IMPORTANT: 2, NORMAL: 3, LOW: 4 };

/** Hikâyenin puanlanacak metni: temsilci başlık + özet (özet hikâyede saklanıyorsa). */
const storyText = (s) => fold([s.title, s.sum].filter(Boolean).join(' '));

// ── Gece bildirimi süzgeci ──────────────────────────────────────────────────
// quiet-hours.isCritical anahtar kelimeye bakar; bu gece uykusunu bölmek için TEK BAŞINA yeterli değildir.
// Gerçek veride iki yanlış pozitif görüldü:
//   "İran Cumhurbaşkanı Pezeşkiyan: Savaş halindeki hiçbir ülkede kalkınma..."  (yorum/alıntı)
//   "Filistin Kızılayı: Gazze'den 23 hasta ... tahliye edildi"                   (uzak ülke, acil değil)
// Bu yüzden gece için iki şart daha aranır: olayın GERÇEKLEŞTİĞİNİ gösteren bir ifade + alıntı/yorum olmaması.

/** Olayın gerçekleştiğini gösteren ifadeler. */
const EVENT_HAPPENED = [
  /meydana geldi|\bsarsildi\b|buyuklugunde (deprem|sarsinti)|deprem oldu|\bdeprem!|\bzelzele/,
  /saldiri(si)? duzenlendi|\bvuruldu\b|\bbombalandi|roket (atildi|isabet)|fuze (atildi|isabet|vurdu)/,
  /patlama (oldu|meydana|sesi)|yangin cikti|\balev aldi|\binfilak/,
  /savas ilan edildi|savas basladi|seferberlik ilan|olaganustu hal ilan|\bohal ilan|darbe girisimi/,
  /\benkaz\b|\bafad\b|\bkandilli\b|\bakut\b|arama kurtarma (basladi|ekipleri)/,
  /tahliye (basladi|ediliyor|edildi)|siginaklara|hava sahasi kapatildi/,
];
/** Alıntı/yorum başlığı: "Kurum/Kişi: ..." (haber öneki olan "Son dakika:" gibi kalıplar hariç). */
const QUOTE_HEADLINE = /^(?!son dakika|flas|acil|canli|dikkat|uyari|resmi)[^:]{3,45}:\s/;
const OPINION = /\bdedi\b|\bsoyledi\b|\buyardi\b|\bcagrisi\b|degerlendirme|\byorumu\b|\banalizi\b|\broportaj|anma toreni|\byildonumu|\btepki gosterdi/;

/** Gece 23:59–08:00 arasında uyandırmaya değer mi? */
function worthWakingUp(story, text) {
  if (QUOTE_HEADLINE.test(text) || OPINION.test(text)) return false;
  if (!EVENT_HAPPENED.some((re) => re.test(text))) return false;
  // Deprem hızı kritiktir: tek kaynak yeter. Diğer olaylar için ikinci bir yayıncı ya da son dakika kaynağı aranır.
  const quake = /\bdeprem|\bzelzele|\bsarsinti/.test(text);
  return quake || sourceCount(story) >= 2 || story.br === true;
}

// ── Seviye 1 ────────────────────────────────────────────────────────────────
// (a) Can güvenliği: gece de bildirilir. quiet-hours.isCritical zaten deprem/afet/savaş/saldırı/OHAL kapsar.
// (b) Ülke gündemini değiştiren karar ve olaylar: anında ama gündüz.
const MAJOR_EVENT = [
  // Büyük kazalar ve facialar
  /\bmaden (kazasi|faciasi)|\bgocuk\b|\bucak dustu|\bucak kazasi|helikopter dustu|tren kazasi|\bbatti\b.*\bgemi|feribot batti/,
  /\bpatlama\b|infilak|dogalgaz patlamasi|fabrika yangini|\bfacia\b|\bcokme\b|bina coktu|coken bina/,
  /\btoplu (olum|zehirlenme)|\bsalgin\b|karantina ilan/,
  // Büyük siyasi gelişmeler
  /secim karari|erken secim|\bsecim takvimi|referandum karari/,
  /kabine (degisikligi|revizyonu)|hukumet kuruldu|guven oyu|gensoru/,
  /(bakan|basbakan|cumhurbaskani|genel baskan|belediye baskani)[a-z ]{0,12}(istifa|gorevden al|tutukland|gozaltina al)/,
  /anayasa (degisikligi|mahkemesi karari)|meclis(ten| )?(kabul|onay)/,
  /\bkayyum atand|parti kapatma davasi|dokunulmazlik/,
  // Ülkeyi etkileyen ekonomik kararlar
  /faiz karari|politika faizi|faizi (artirdi|indirdi|sabit)|merke[a-z]* bank[a-z]* (karar|faiz)/,
  /asgari ucret (zammi|belli|aciklandi|karari)|memur (maas|zam) (zammi|karari|belli)|emekli (maas|zam)[a-z]* (karar|belli|aciklandi)/,
  /\bdolar\b.*\brekor|euro.*\brekor|\baltin\b.*\brekor kirdi|borsa.*(tarihi zirve|rekor)/,
  /akaryakit[a-z]* (zam|indirim)|dogalgaz[a-z]* zam|elektrige zam|\bzam yapildi|vergi (artisi|duzenlemesi)/,
  /\bkur korumali|\bmoratoryum|\bdevalu/,
  // Kamu güvenliği
  /\bkayip (ilani|cocuk)|\bihbar hatti|kamu spotu|valilik (yasakladi|karari)|sokaga cikma/,
  /\bokullar tatil|\begitime ara|\buzaktan egitim karari|\bizin verildi.*tahliye/,
  /siber saldiri|veri ihlali|\bdolandiricilik uyarisi/,
];

// ── Seviye 2 ────────────────────────────────────────────────────────────────
const IMPORTANT = [
  // Siyaset ve toplum
  /\baciklama yapti|\baciklamasi\b|\bkarar aldi|\byasa teklifi|\bduzenleme\b|\bgenelge\b|\byonetmelik/,
  /\bsorusturma baslatildi|\bdava acildi|\bmahkeme karari|\bceza verildi|\bgozaltina alindi|\btutuklandi/,
  /\bprotesto|\bgrev\b|\beylem yapildi|\bmiting/,
  // Ekonomi
  /\benflasyon\b|\btufe\b|\byufe\b|issizlik orani|buyume orani|cari acik|butce acigi/,
  /\bihracat\b|\bithalat\b|\byatirim karari|\bteshvik|\bkredi (faizi|paketi)|\bdestek paketi/,
  /\biflas\b|\bkonkordato|\bsatin aldi|\bbirlesme anlasmasi|\bhalka arz/,
  // Teknoloji ve şirketler
  /\btanitildi\b|\bduyurdu\b|\bpiyasaya surdu|\bguncelleme yayinlandi|\berisim engeli|\bkapatildi\b/,
  /yapay zeka|\bchatgpt|\bstarlink|\buydu firlat/,
  // Spor (büyük gelişmeler)
  /\btransfer (anlasmasi|bombasi)|\bimzayi atti|\bayrilik karari|\bteknik direktor (ayrildi|gorevden|atandi)/,
  /\bsampiyon oldu|\bkupayi kaldirdi|\bligden dusme|\bmilli takim (kadrosu|aday kadro)/,
  /\bpfdk\b|\bceza kurulu|\bhukmen maglup|\bpuan silme/,
];

// ── Seviye 4 ────────────────────────────────────────────────────────────────
// Servis/rutin içerik: bilgilendirici ama gündem değiştirmez, bildirime girmez.
const ROUTINE = [
  /\bhangi kanalda|\bsaat kacta|\bne zaman\b|\bkacta baslayacak|\bcanli izle|\bcanli yayin link|\bmuhtemel 11|\bilk 11|\b11ler belli/,
  /\bpuan durumu|\bfikstur|\bmac ozeti|\bgolleri izle|\bmac sonucu nedir/,
  /\bhava durumu|\bnamaz vakitleri|\bimsakiye|\biftar saati|\bnobetci eczane|\btrafige kapali/,
  /\bburclar|\bgunluk burc|\bruya tabiri|\bfal\b/,
  /\btarifi\b|\bnasil yapilir|\bpuf noktalari|\bkac kalori/,
  /\bsorgulama ekrani|\btc kimlik ile|\bbasvuru nasil|\be-devlet uzerinden|\bsonuclari aciklandi mi/,
  /\bzamani geldi mi|\bne kadar oldu|\bguncel fiyat listesi|\bfiyatlari ne kadar/,
  /\bdogum gunu|\bkim kimdir|\bkac yasinda|\bhayati ve|\bbiyografisi/,
  // Perakende/promosyon içeriği (BIM, A101, Şok vb. aktüel ürünler)
  /aktuel firsatlar|\bbim aktuel|\ba101 aktuel|\bsok aktuel|market firsatlari|haftalik firsatlar|indirimli urunler/,
  // "Deprem mi oldu?" tarzı spekülatif/sorgulama başlıkları (gerçek deprem bildirimi değil)
  /\bdeprem mi oldu\b|az once.*\bdeprem\b|nerede.*\bdeprem.*\?|\bdeprem sorgula|\bdeprem.*mi \?|son depremler listesi/,
  // Dizi/film izleme linkleri ve TV rehberi
  /izleme linki|bolum izleme|izle.*link|dizi.*kac bolum|yeni bolum ne zaman|yayin tarihi belli mi|dizi baslangic tarihi/,
];

/** Hikâye kaç farklı yayıncıda yer aldı? */
const sourceCount = (s) => (Array.isArray(s.srcs) ? s.srcs.length : 0);
const anyMatch = (list, text) => list.some((re) => re.test(text));

/**
 * Bir hikâyenin önem seviyesi.
 * @returns {{level: 1|2|3|4, night: boolean, reason: string}}
 *   night: gece 23:59–08:00 arasında da gönderilebilir mi (yalnız can güvenliği).
 */
function scoreStory(story, { now = Date.now(), maxAgeMs = 3 * 3_600_000, maxTotalMs = 12 * 3_600_000 } = {}) {
  const text = storyText(story);
  const n = sourceCount(story);
  const breaking = story.br === true;

  // 4 — hiç bildirilmez
  if (isClickbait(story.title || '')) return { level: LEVEL.LOW, night: false, reason: 'tıklama tuzağı' };
  if (anyMatch(ROUTINE, text)) return { level: LEVEL.LOW, night: false, reason: 'rutin/servis içeriği' };

  // 1a — can güvenliği. Seviye 1'dir; GECE gönderimi için ayrıca worthWakingUp şartı aranır.
  if (isCritical(story.title, story.sum)) {
    const night = worthWakingUp(story, text);
    return { level: LEVEL.CRITICAL, night, reason: night ? 'can güvenliği' : 'can güvenliği (gündüz)' };
  }

  // Tazelik SON gelişmeye göre ölçülür: süren olaylarda (yangın, kaza, kriz) yeni haber geldikçe hikâye
  // canlı kalır. Yine de bir üst sınır var: olayın başlangıcı 12 saati geçtiyse artık "son dakika" değildir.
  const last = story.last_ms || story.first_ms || now;
  const old = now - last > maxAgeMs || now - (story.first_ms || now) > maxTotalMs;

  // 1b — ülke gündemini değiştiren karar/olay (gündüz, beklemeden)
  if (anyMatch(MAJOR_EVENT, text) && (n >= 2 || breaking) && !old) {
    return { level: LEVEL.CRITICAL, night: false, reason: 'büyük gelişme' };
  }
  // 1c — çok sayıda yayıncı aynı olayı veriyorsa konusu ne olursa olsun gündemdir
  if (n >= 5 && !old) return { level: LEVEL.CRITICAL, night: false, reason: `${n} kaynak` };

  // 2 — önemli
  if (n >= 3 && !old) return { level: LEVEL.IMPORTANT, night: false, reason: `${n} kaynak` };
  if (n >= 2 && anyMatch(IMPORTANT, text) && !old) return { level: LEVEL.IMPORTANT, night: false, reason: 'önemli gelişme' };
  if (breaking && anyMatch(IMPORTANT, text) && !old) return { level: LEVEL.IMPORTANT, night: false, reason: 'son dakika kaynağı' };

  // 4 — tek kaynaklı ve eskimiş: bildirime değmez
  if (n < 2 && old) return { level: LEVEL.LOW, night: false, reason: 'tek kaynak, eski' };

  // 3 — özet ve gündemde
  return { level: LEVEL.NORMAL, night: false, reason: 'normal' };
}

// ── Gelişme tespiti ─────────────────────────────────────────────────────────
/**
 * "Yeni gelişme" işaretleri. Aynı olayın tekrar yayımlanması değil, olayın İLERLEMESİ aranır
 * (ölü sayısı, gözaltı, istifa, iptal…). Her anahtar hikâyede bir kez sayılır.
 */
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

/** Metinde geçen gelişme anahtarları. */
function developmentKeys(...texts) {
  const t = fold(texts.filter(Boolean).join(' '));
  return DEVELOPMENTS.filter(([, re]) => re.test(t)).map(([k]) => k);
}

/** Hikâyede daha önce görülmemiş gelişme anahtarları. */
function newDevelopments(story, ...texts) {
  const seen = new Set(story.dev || []);
  return developmentKeys(...texts).filter((k) => !seen.has(k));
}

const RENOTIFY_GAP_MS = 90 * 60_000;
const RENOTIFY_MAX = 2;
const RENOTIFY_MIN_NEW_SOURCES = 3;

/**
 * Daha önce bildirilmiş bir hikâye yeniden bildirilmeli mi?
 * Üç şarttan biri + zaman aralığı + tekrar sınırı aranır:
 *   a) seviye 1'e yükseldi (ör. yaralı haberi ölüm haberine döndü),
 *   b) hikâyede yeni bir gelişme anahtarı belirdi,
 *   c) yayıncı sayısı son bildirimden beri en az 3 arttı (olay büyüdü).
 * @returns {{ok: boolean, reason: string}}
 */
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
