import Foundation

enum DateUtil {
  static var cal: Calendar = {
    var c = Calendar(identifier: .gregorian)
    c.timeZone = .current
    return c
  }()

  static func dateKey(_ d: Date) -> String {
    let c = cal.dateComponents([.year, .month, .day], from: d)
    return String(format: "%04d-%02d-%02d", c.year!, c.month!, c.day!)
  }

  static func date(fromKey key: String) -> Date? {
    let p = key.split(separator: "-").compactMap { Int($0) }
    guard p.count == 3 else { return nil }
    return cal.date(from: DateComponents(year: p[0], month: p[1], day: p[2], hour: 12))
  }

  static func dayOfYear(_ d: Date) -> Int { cal.ordinality(of: .day, in: .year, for: d) ?? 1 }

  static func addDays(_ n: Int, to d: Date) -> Date { cal.date(byAdding: .day, value: n, to: d)! }

  static func startOfDay(_ d: Date) -> Date { cal.startOfDay(for: d) }

  static func trDate(_ d: Date) -> String {
    let c = cal.dateComponents([.year, .month, .day], from: d)
    return String(format: "%02d.%02d.%04d", c.day!, c.month!, c.year!)
  }

  static let monthsTr = ["Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"]
  static let monthsEn = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
  static func monthName(_ m: Int) -> String { (L10n.isEnglish ? monthsEn : monthsTr)[m - 1] }
  static var weekdaysShort: [String] { L10n.isEnglish ? ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"] : ["Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"] }
}

/// Türkiye (Diyanet) Ramazan oruç günleri.
enum RamadanCalendar {
  static var ranges: [RamadanRange] { AppContent.shared.ramadanRanges }

  static func isRamadanDay(_ key: String) -> Bool { ranges.contains { key >= $0.start && key <= $0.end } }
  static func isRamadanDay(date: Date) -> Bool { isRamadanDay(DateUtil.dateKey(date)) }

  static func dayOfRamadan(_ date: Date = Date()) -> Int? {
    let key = DateUtil.dateKey(date)
    guard let r = ranges.first(where: { key >= $0.start && key <= $0.end }),
          let start = DateUtil.date(fromKey: r.start), let today = DateUtil.date(fromKey: key) else { return nil }
    return (DateUtil.cal.dateComponents([.day], from: start, to: today).day ?? 0) + 1
  }

  static func rangeForYear(_ year: Int) -> RamadanRange? { ranges.first { $0.start.hasPrefix("\(year)-") } }

  /// Bugünden sonraki ilk Ramazan başlangıcı.
  static func nextStart(after date: Date = Date()) -> Date? {
    let key = DateUtil.dateKey(date)
    return ranges.first { $0.start > key }.flatMap { DateUtil.date(fromKey: $0.start) }
  }
}

struct SpecialCalendarDay: Hashable {
  let dateKey: String
  var title: String
  let category: String // "Mübarek Gece", "Dini Gün", "Dini Bayram", "Dini Hafta", "İslam Büyükleri", "Resmi Tatil"
  let badgeEmoji: String
}

/// Kandiller, bayramlar, dini günler (Ümmü'l-Kura + Diyanet Ramazan düzeltmesi).
enum IslamicDays {
  private static var cache: [Int: [String: SpecialCalendarDay]] = [:]
  private static let hijri: Calendar = {
    var c = Calendar(identifier: .islamicUmmAlQura)
    c.timeZone = .current
    return c
  }()

  // Hicri aylar 1 tabanlı
  private static let MUHARREM = 1, REBIULEVVEL = 3, CEMAZIYELAHIR = 6, RECEP = 7, SABAN = 8, RAMAZAN = 9, SEVVAL = 10, ZILHICCE = 12

  static func forYear(_ year: Int) -> [String: SpecialCalendarDay] {
    if let c = cache[year] { return c }
    var map: [String: SpecialCalendarDay] = [:]
    var order: [String] = []

    func put(_ d: Date, _ title: String, _ category: String, _ emoji: String) {
      guard DateUtil.cal.component(.year, from: d) == year else { return }
      let key = DateUtil.dateKey(d)
      if var e = map[key] {
        e.title += " • " + title
        map[key] = e
      } else {
        map[key] = SpecialCalendarDay(dateKey: key, title: title, category: category, badgeEmoji: emoji)
        order.append(key)
      }
    }
    func fixed(_ m: Int, _ d: Int, _ title: String, _ cat: String, _ emoji: String) {
      if let date = DateUtil.cal.date(from: DateComponents(year: year, month: m, day: d, hour: 12)) { put(date, title, cat, emoji) }
    }
    let hYears = hijriYearsOverlapping(year)
    let shift = diyanetShift(year)
    func h(_ month: Int, _ day: Int, _ offset: Int, _ title: String, _ cat: String, _ emoji: String) {
      for hy in hYears {
        guard let d = hijriToGregorian(hy, month, day) else { continue }
        put(DateUtil.addDays(offset + shift, to: d), title, cat, emoji)
      }
    }

    h(SEVVAL, 1, -1, "Ramazan Bayramı Arefesi", "Dini Gün", "🕊️")
    for d in 1...3 { h(SEVVAL, d, 0, "Ramazan Bayramı \(d). Gün", "Dini Bayram", "🍬") }
    h(ZILHICCE, 9, 0, "Kurban Bayramı Arefesi", "Dini Gün", "🕊️")
    for d in 1...4 { h(ZILHICCE, 9 + d, 0, "Kurban Bayramı \(d). Gün", "Dini Bayram", "🐑") }

    for hy in hYears {
      guard var first = hijriToGregorian(hy, RECEP, 1) else { continue }
      first = DateUtil.addDays(shift, to: first)
      while DateUtil.cal.component(.weekday, from: first) != 6 { first = DateUtil.addDays(1, to: first) }
      put(DateUtil.addDays(-1, to: first), "Regaip Kandili", "Mübarek Gece", "✨")
    }
    h(RECEP, 27, -1, "Miraç Kandili", "Mübarek Gece", "✨")
    h(SABAN, 15, -1, "Berat Kandili", "Mübarek Gece", "✨")
    h(RAMAZAN, 27, -1, "Kadir Gecesi", "Mübarek Gece", "🌟")
    h(REBIULEVVEL, 12, -1, "Mevlid Kandili", "Mübarek Gece", "✨")

    h(RECEP, 1, 0, "Üç Ayların Başlangıcı", "Dini Gün", "🌙")
    h(RAMAZAN, 1, 0, "Ramazan Başlangıcı (1 Ramazan)", "Dini Gün", "🌙")
    h(MUHARREM, 1, 0, "Hicri Yılbaşı (1 Muharrem) • Hicret'in yıl dönümü", "Dini Gün", "🌙")
    h(MUHARREM, 10, 0, "Aşure Günü (10 Muharrem)", "Dini Gün", "🥣")

    for k in 0...6 { h(REBIULEVVEL, 12, -1 + k, "Mevlid-i Nebi (Kutlu Doğum) Haftası", "Dini Hafta", "🌹") }
    for d in 1...7 { fixed(10, d, "Camiler ve Din Görevlileri Haftası", "Dini Hafta", "🕌") }

    h(REBIULEVVEL, 12, 0, "Hz. Muhammed (s.a.v.)'in Doğumu", "İslam Büyükleri", "🌹")
    h(RAMAZAN, 3, 0, "Hz. Fatıma'nın Vefatı (11 H.)", "İslam Büyükleri", "🤍")
    h(RAMAZAN, 10, 0, "Hz. Hatice'nin Vefatı", "İslam Büyükleri", "🤍")
    h(RAMAZAN, 17, 0, "Bedir Zaferi (2 H.)", "İslam Büyükleri", "⚔️")
    h(RAMAZAN, 20, 0, "Mekke'nin Fethi (8 H.)", "İslam Büyükleri", "🕋")
    h(RAMAZAN, 21, 0, "Hz. Ali'nin Şehadeti (40 H.)", "İslam Büyükleri", "🤍")
    h(CEMAZIYELAHIR, 22, 0, "Hz. Ebubekir'in Vefatı (13 H.)", "İslam Büyükleri", "🤍")
    h(ZILHICCE, 18, 0, "Hz. Osman'ın Şehadeti (35 H.)", "İslam Büyükleri", "🤍")
    h(ZILHICCE, 26, 0, "Hz. Ömer'in Şehadeti (23 H.)", "İslam Büyükleri", "🤍")
    h(MUHARREM, 10, 0, "Hz. Hüseyin'in Kerbela'da Şehadeti (61 H.)", "İslam Büyükleri", "🤍")
    fixed(12, 17, "Şeb-i Arûs: Hz. Mevlânâ'nın Vefat Yıl Dönümü (1273)", "İslam Büyükleri", "🌹")

    fixed(1, 1, "Yılbaşı", "Resmi Tatil", "🇹🇷")
    fixed(4, 23, "23 Nisan Ulusal Egemenlik ve Çocuk Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(5, 1, "1 Mayıs Emek ve Dayanışma Günü", "Resmi Tatil", "🇹🇷")
    fixed(5, 19, "19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(7, 15, "15 Temmuz Demokrasi ve Milli Birlik Günü", "Resmi Tatil", "🇹🇷")
    fixed(8, 30, "30 Ağustos Zafer Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(10, 29, "29 Ekim Cumhuriyet Bayramı", "Resmi Tatil", "🇹🇷")

    cache[year] = map
    return map
  }

  static func sortedDays(_ year: Int) -> [SpecialCalendarDay] { forYear(year).values.sorted { $0.dateKey < $1.dateKey } }

  static func special(on date: Date) -> SpecialCalendarDay? {
    forYear(DateUtil.cal.component(.year, from: date))[DateUtil.dateKey(date)]
  }

  private static func hijriYearsOverlapping(_ year: Int) -> [Int] {
    guard let jan1 = DateUtil.cal.date(from: DateComponents(year: year, month: 1, day: 1, hour: 12)) else { return [] }
    let start = hijri.component(.year, from: jan1)
    return [start, start + 1]
  }

  private static func diyanetShift(_ year: Int) -> Int {
    guard let r = RamadanCalendar.rangeForYear(year), let diyanet = DateUtil.date(fromKey: r.start) else { return 0 }
    for hy in hijriYearsOverlapping(year) {
      guard let computed = hijriToGregorian(hy, RAMAZAN, 1), DateUtil.cal.component(.year, from: computed) == year else { continue }
      let diff = DateUtil.cal.dateComponents([.day], from: DateUtil.startOfDay(computed), to: DateUtil.startOfDay(diyanet)).day ?? 0
      return max(-2, min(2, diff))
    }
    return 0
  }

  private static func hijriToGregorian(_ y: Int, _ m: Int, _ d: Int) -> Date? {
    hijri.date(from: DateComponents(year: y, month: m, day: d, hour: 12))
  }
}

enum Festival {
  case ramazanBayrami, kurbanBayrami
  var greeting: String { self == .ramazanBayrami ? t("Ramazan Bayramınız Mübarek Olsun") : t("Kurban Bayramınız Mübarek Olsun") }
  var emoji: String { self == .ramazanBayrami ? "🍬" : "🐑" }

  static func today(_ date: Date = Date()) -> Festival? {
    guard let s = IslamicDays.special(on: date), s.category == "Dini Bayram" else { return nil }
    return s.title.contains("Ramazan") ? .ramazanBayrami : .kurbanBayrami
  }

  /// Cami görselinde minareler arasına gerilecek mahya.
  static func mahya(_ date: Date = Date()) -> [String]? {
    if today(date) != nil { return ["BAYRAMINIZ", "MÜBAREK OLSUN"] }
    if RamadanCalendar.isRamadanDay(date: date) {
      return RamadanCalendar.isRamadanDay(date: DateUtil.addDays(3, to: date)) ? ["HOŞ GELDİN", "YA ŞEHR-İ RAMAZAN"] : ["ELVEDA", "YA ŞEHR-İ RAMAZAN"]
    }
    if IslamicDays.special(on: date)?.category == "Mübarek Gece" { return ["KANDİLİNİZ", "MÜBAREK OLSUN"] }
    if DateUtil.cal.component(.weekday, from: date) == 6 { return ["HAYIRLI", "CUMALAR"] }
    return nil
  }
}

struct HolidayCountdownInfo {
  let title: String
  let subtitle: String
  let days: Int, hours: Int, minutes: Int, seconds: Int
}

/// Bir sonraki Ramazan / bayram geri sayımı (takvimden).
enum HolidayCountdown {
  static func calculate(_ now: Date = Date()) -> HolidayCountdownInfo {
    let todayKey = DateUtil.dateKey(now)
    var target: Date
    var title: String
    var subtitle: String
    let year = DateUtil.cal.component(.year, from: now)
    // Bayram günlerini bu ve gelecek yıldan bul
    let days = IslamicDays.sortedDays(year) + IslamicDays.sortedDays(year + 1)
    let fitr = days.first { $0.title.hasPrefix("Ramazan Bayramı 1.") && $0.dateKey > todayKey }
    let adha = days.first { $0.title.hasPrefix("Kurban Bayramı 1.") && $0.dateKey > todayKey }
    if RamadanCalendar.isRamadanDay(todayKey), let f = fitr, let d = DateUtil.date(fromKey: f.dateKey) {
      target = d; title = "🌙 Ramazan Bayramı'na Kalan"; subtitle = "Mübarek Ramazan Bayramı ve Bayram Namazı"
    } else if let rs = RamadanCalendar.nextStart(after: now), let a = adha, let ad = DateUtil.date(fromKey: a.dateKey), ad < rs {
      target = ad; title = "🐑 Kurban Bayramı'na Kalan"; subtitle = "Mübarek Kurban Bayramı 1. Günü"
    } else if let rs = RamadanCalendar.nextStart(after: now) {
      target = rs
      title = "🌙 Ramazan-ı Şerif'e Kalan"
      subtitle = DateUtil.cal.component(.year, from: rs) > year ? "Gelecek Yılın Mübarek Ramazan Ayı" : "11 Ayın Sultanı Mübarek Ramazan Ayı Başlangıcı"
    } else {
      target = DateUtil.addDays(30, to: now); title = "🌙 Ramazan-ı Şerif'e Kalan"; subtitle = ""
    }
    target = DateUtil.startOfDay(target)
    let total = max(0, Int(target.timeIntervalSince(now)))
    return HolidayCountdownInfo(title: t(title), subtitle: t(subtitle), days: total / 86400, hours: (total % 86400) / 3600, minutes: (total % 3600) / 60, seconds: total % 60)
  }
}
