/// Resmi bayramlar ve anma günleri — cihazda zamanlanan kutlama/anma bildirimleri ve ana sayfa şeridi.
///
/// Sabit tarihli günler her yıl aynı gün; dini bayramlar ay takvimine göre değiştiği için yıl tablosuyla
/// verilir (Diyanet takvimi). Dini bayram ve kandiller de tabloyla verilir; tablo bitince bildirim gönderilmez — yeni yılları eklemeyi unutmayın.
library;

import 'dart:ui' show Color;

enum ObservanceKind {
  /// Bayram / kutlama
  celebration,

  /// Anma (sade, saygılı görünüm)
  commemoration,

  /// Kandil / mübarek gece
  holyNight,
}

class Observance {
  const Observance({
    required this.id,
    required this.kind,
    required this.titleTr,
    required this.titleEn,
    required this.bodyTr,
    required this.bodyEn,
    this.month,
    this.day,
    this.dates = const [],
    this.hour = 9,
    this.minute = 0,
    this.sinceYear,
  });

  final String id;
  final ObservanceKind kind;

  /// Sabit tarihli günler için.
  final int? month;
  final int? day;

  /// Ay takvimine bağlı günler (dini bayramlar, kandiller): (yıl, ay, gün). Bir yılda birden fazla olabilir
  /// (ör. 2027'de iki Miraç Kandili).
  final List<(int, int, int)> dates;

  /// Bildirim saati (İstanbul).
  final int hour;
  final int minute;

  /// Metindeki `{n}` = yıl − [sinceYear] (ör. Cumhuriyet'in 103. yılı).
  final int? sinceYear;

  final String titleTr;
  final String titleEn;
  final String bodyTr;
  final String bodyEn;

  bool get isCommemoration => kind == ObservanceKind.commemoration;

  /// Şerit / bildirim rengi: anma siyah, kandil koyu yeşil, bayram kırmızı.
  Color get color => switch (kind) {
    ObservanceKind.commemoration => const Color(0xFF111111),
    ObservanceKind.holyNight => const Color(0xFF0B5D4B),
    ObservanceKind.celebration => const Color(0xFFE30A17),
  };

  /// Bu yıldaki tarihleri (yoksa boş).
  List<DateTime> datesIn(int year) {
    if (month != null && day != null) return [DateTime(year, month!, day!, hour, minute)];
    return [
      for (final d in dates)
        if (d.$1 == year) DateTime(year, d.$2, d.$3, hour, minute),
    ];
  }

  String _fill(String s, int year) => sinceYear == null ? s : s.replaceAll('{n}', '${year - sinceYear!}');

  String title(int year, {bool english = false}) => _fill(english ? titleEn : titleTr, year);
  String body(int year, {bool english = false}) => _fill(english ? bodyEn : bodyTr, year);
}

/// Belirli bir yıldaki gerçekleşme.
class ObservanceOccurrence {
  const ObservanceOccurrence(this.observance, this.date, [this.nth = 0]);

  final Observance observance;
  final DateTime date;

  /// Aynı gün bu yıl kaçıncı kez (0, 1, 2).
  final int nth;

  /// Bildirim kimliği: yıl + gün sırasına göre sabit (5000–5999 aralığı bayram/anma bildirimlerine ayrıldı).
  int get notificationId => 5000 + (date.year % 10) * 100 + observances.indexOf(observance) * 3 + nth;

  String title({bool english = false}) => observance.title(date.year, english: english);
  String body({bool english = false}) => observance.body(date.year, english: english);
}

const observances = <Observance>[
  Observance(
    id: 'yilbasi',
    kind: ObservanceKind.celebration,
    month: 1,
    day: 1,
    hour: 10,
    titleTr: '🎉 Mutlu Yıllar!',
    titleEn: '🎉 Happy New Year!',
    bodyTr: 'Yeni yılınız sağlık, huzur ve mutluluk getirsin.',
    bodyEn: 'Wishing you a healthy, peaceful and happy new year.',
  ),
  Observance(
    id: 'canakkale',
    kind: ObservanceKind.commemoration,
    month: 3,
    day: 18,
    sinceYear: 1915,
    titleTr: '🇹🇷 18 Mart Çanakkale Zaferi',
    titleEn: '🇹🇷 March 18 – Çanakkale Victory',
    bodyTr: 'Çanakkale Zaferi\'nin {n}. yıl dönümünde aziz şehitlerimizi rahmet, minnet ve saygıyla anıyoruz.',
    bodyEn: 'On the {n}th anniversary of the Çanakkale Victory, we remember our martyrs with gratitude and respect.',
  ),
  Observance(
    id: '23-nisan',
    kind: ObservanceKind.celebration,
    month: 4,
    day: 23,
    sinceYear: 1920,
    titleTr: '🇹🇷 23 Nisan Ulusal Egemenlik ve Çocuk Bayramı',
    titleEn: '🇹🇷 April 23 – National Sovereignty and Children\'s Day',
    bodyTr: 'Türkiye Büyük Millet Meclisi\'nin açılışının {n}. yıl dönümü ve Çocuk Bayramımız kutlu olsun!',
    bodyEn: 'Happy National Sovereignty and Children\'s Day, the {n}th anniversary of the Grand National Assembly.',
  ),
  Observance(
    id: '1-mayis',
    kind: ObservanceKind.celebration,
    month: 5,
    day: 1,
    titleTr: '1 Mayıs Emek ve Dayanışma Günü',
    titleEn: 'May 1 – Labour and Solidarity Day',
    bodyTr: 'Tüm emekçilerin Emek ve Dayanışma Günü kutlu olsun.',
    bodyEn: 'Happy Labour and Solidarity Day to all workers.',
  ),
  Observance(
    id: '19-mayis',
    kind: ObservanceKind.celebration,
    month: 5,
    day: 19,
    sinceYear: 1919,
    titleTr: '🇹🇷 19 Mayıs Atatürk\'ü Anma, Gençlik ve Spor Bayramı',
    titleEn: '🇹🇷 May 19 – Commemoration of Atatürk, Youth and Sports Day',
    bodyTr: 'Millî Mücadele\'nin başlangıcının {n}. yıl dönümünde Atatürk\'ü saygıyla anıyor, bayramımızı kutluyoruz.',
    bodyEn: 'On the {n}th anniversary of the start of the War of Independence, we commemorate Atatürk.',
  ),
  Observance(
    id: '15-temmuz',
    kind: ObservanceKind.commemoration,
    month: 7,
    day: 15,
    titleTr: '🇹🇷 15 Temmuz Demokrasi ve Millî Birlik Günü',
    titleEn: '🇹🇷 July 15 – Democracy and National Unity Day',
    bodyTr: '15 Temmuz şehitlerimizi rahmetle, gazilerimizi minnetle anıyoruz.',
    bodyEn: 'We remember the martyrs of July 15 with mercy and our veterans with gratitude.',
  ),
  Observance(
    id: '30-agustos',
    kind: ObservanceKind.celebration,
    month: 8,
    day: 30,
    sinceYear: 1922,
    titleTr: '🇹🇷 30 Ağustos Zafer Bayramı',
    titleEn: '🇹🇷 August 30 – Victory Day',
    bodyTr: 'Büyük Taarruz\'un {n}. yıl dönümünde Zafer Bayramımız kutlu olsun! Şehitlerimizi rahmetle anıyoruz.',
    bodyEn: 'Happy Victory Day on the {n}th anniversary of the Great Offensive.',
  ),
  Observance(
    id: '29-ekim',
    kind: ObservanceKind.celebration,
    month: 10,
    day: 29,
    sinceYear: 1923,
    titleTr: '🇹🇷 29 Ekim Cumhuriyet Bayramı',
    titleEn: '🇹🇷 October 29 – Republic Day',
    bodyTr: 'Cumhuriyetimizin {n}. yılı kutlu olsun! Ne mutlu Türküm diyene!',
    bodyEn: 'Happy {n}th anniversary of the Republic of Türkiye!',
  ),
  Observance(
    id: '10-kasim',
    kind: ObservanceKind.commemoration,
    month: 11,
    day: 10,
    hour: 9,
    minute: 0, // saygı duruşu 09.05'te; bildirim birkaç dakika sapabileceği için 09.00'da gönderilir
    sinceYear: 1938,
    titleTr: '🖤 10 Kasım — Atatürk\'ü Anıyoruz',
    titleEn: '🖤 November 10 – Remembering Atatürk',
    bodyTr:
        'Cumhuriyetimizin kurucusu Gazi Mustafa Kemal Atatürk\'ü aramızdan ayrılışının {n}. yılında saygı, '
        'rahmet ve özlemle anıyoruz. Saat 09.05\'te tüm yurtta saygı duruşunda bulunulacak.',
    bodyEn:
        'We remember Mustafa Kemal Atatürk, founder of the Republic, on the {n}th anniversary of his passing. '
        'A moment of silence will be held nationwide at 09:05.',
  ),
  Observance(
    id: 'ogretmenler-gunu',
    kind: ObservanceKind.celebration,
    month: 11,
    day: 24,
    titleTr: '24 Kasım Öğretmenler Günü',
    titleEn: 'November 24 – Teachers\' Day',
    bodyTr: 'Başöğretmen Atatürk başta olmak üzere tüm öğretmenlerimizin Öğretmenler Günü kutlu olsun.',
    bodyEn: 'Happy Teachers\' Day to all our teachers.',
  ),
  // Dini bayramlar (Diyanet takvimi; bayramın 1. günü). 2028 tarihleri yayımlanınca doğrulayın.
  Observance(
    id: 'ramazan-bayrami',
    kind: ObservanceKind.celebration,
    dates: [(2026, 3, 20), (2027, 3, 9), (2028, 2, 26)],
    titleTr: '🌙 Ramazan Bayramınız Mübarek Olsun',
    titleEn: '🌙 Happy Eid al-Fitr',
    bodyTr: 'Ramazan Bayramınızı en içten dileklerimizle kutlar, sağlık ve huzur dolu bir bayram dileriz.',
    bodyEn: 'Wishing you a blessed and peaceful Eid al-Fitr.',
  ),
  Observance(
    id: 'kurban-bayrami',
    kind: ObservanceKind.celebration,
    dates: [(2026, 5, 27), (2027, 5, 16), (2028, 5, 4)],
    titleTr: '🌙 Kurban Bayramınız Mübarek Olsun',
    titleEn: '🌙 Happy Eid al-Adha',
    bodyTr: 'Kurban Bayramınızı en içten dileklerimizle kutlar, sağlık ve huzur dolu bir bayram dileriz.',
    bodyEn: 'Wishing you a blessed and peaceful Eid al-Adha.',
  ),
  // Kandiller (Diyanet dinî günler takvimi; kandil gecesinin başladığı gün). Bildirim gün içinde gider.
  Observance(
    id: 'regaip',
    kind: ObservanceKind.holyNight,
    dates: [(2026, 12, 10), (2027, 12, 2)],
    hour: 10,
    titleTr: '🕌 Regaip Kandiliniz Mübarek Olsun',
    titleEn: '🕌 Blessed Raghaib Night',
    bodyTr: 'Üç ayların ilk kandili Regaip Kandili bu gece. Dualarınızın kabul olması dileğiyle.',
    bodyEn: 'Tonight is the Night of Raghaib, the first holy night of the three sacred months.',
  ),
  Observance(
    id: 'mirac',
    kind: ObservanceKind.holyNight,
    dates: [(2026, 1, 15), (2027, 1, 4), (2027, 12, 24)],
    hour: 10,
    titleTr: '🕌 Miraç Kandiliniz Mübarek Olsun',
    titleEn: '🕌 Blessed Night of Miraj',
    bodyTr: 'Miraç Kandili bu gece. Hayırlara vesile olması dileğiyle.',
    bodyEn: 'Tonight is the Night of Miraj (the Ascension).',
  ),
  Observance(
    id: 'berat',
    kind: ObservanceKind.holyNight,
    dates: [(2026, 2, 2), (2027, 1, 22)],
    hour: 10,
    titleTr: '🕌 Berat Kandiliniz Mübarek Olsun',
    titleEn: '🕌 Blessed Night of Bara\'at',
    bodyTr: 'Berat Kandili bu gece. Huzur ve bereket dileriz.',
    bodyEn: 'Tonight is the Night of Bara\'at. Wishing you peace and blessings.',
  ),
  Observance(
    id: 'kadir-gecesi',
    kind: ObservanceKind.holyNight,
    dates: [(2026, 3, 16), (2027, 3, 5)],
    hour: 10,
    titleTr: '🕌 Kadir Geceniz Mübarek Olsun',
    titleEn: '🕌 Blessed Laylat al-Qadr',
    bodyTr: 'Bin aydan hayırlı Kadir Gecesi bu gece. Dualarınızın kabul olması dileğiyle.',
    bodyEn: 'Tonight is Laylat al-Qadr, the Night of Power.',
  ),
  Observance(
    id: 'mevlid',
    kind: ObservanceKind.holyNight,
    dates: [(2026, 8, 24), (2027, 8, 13)],
    hour: 10,
    titleTr: '🕌 Mevlid Kandiliniz Mübarek Olsun',
    titleEn: '🕌 Blessed Mawlid Night',
    bodyTr: 'Peygamber Efendimizin doğumunun yıl dönümü Mevlid Kandili bu gece.',
    bodyEn: 'Tonight is Mawlid, marking the birth of the Prophet Muhammad.',
  ),
];

/// [from] anından sonraki [days] gün içindeki günler (tarihe göre sıralı).
List<ObservanceOccurrence> upcomingObservances(DateTime from, {int days = 365}) {
  final until = from.add(Duration(days: days));
  final out = <ObservanceOccurrence>[];
  for (final year in [from.year, from.year + 1]) {
    for (final o in observances) {
      final ds = o.datesIn(year);
      for (var i = 0; i < ds.length; i++) {
        if (ds[i].isAfter(from) && ds[i].isBefore(until)) out.add(ObservanceOccurrence(o, ds[i], i));
      }
    }
  }
  out.sort((a, b) => a.date.compareTo(b.date));
  return out;
}

/// Bugün (takvim günü) olan gün, yoksa null.
ObservanceOccurrence? observanceOn(DateTime day) {
  for (final o in observances) {
    final ds = o.datesIn(day.year);
    for (var i = 0; i < ds.length; i++) {
      if (ds[i].month == day.month && ds[i].day == day.day) return ObservanceOccurrence(o, ds[i], i);
    }
  }
  return null;
}
