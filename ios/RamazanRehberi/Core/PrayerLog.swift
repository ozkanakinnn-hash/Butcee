import Foundation

/// Beş vakit farz namaz (takip için). Güneş vakti namaz değildir.
enum FarzVakit: Int, CaseIterable, Identifiable, Codable {
  case sabah = 0, ogle, ikindi, aksam, yatsi
  var id: Int { rawValue }

  /// PrayerTimes.all içindeki başlangıç saatinin sırası (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı).
  var timeIndex: Int { [0, 2, 3, 4, 5][rawValue] }

  var name: String {
    let tr = ["Sabah", "Öğle", "İkindi", "Akşam", "Yatsı"][rawValue]
    let en = ["Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"][rawValue]
    return L10n.isEnglish ? en : tr
  }

  /// Kaza modülündeki anahtar (KazaVakit.rawValue ile aynı).
  var kazaKey: String { ["sabah", "ogle", "ikindi", "aksam", "yatsi"][rawValue] }

  /// PrayerTimes.all sırasından (0…5) farz vakte; Güneş (1) için nil.
  static func from(timeIndex i: Int) -> FarzVakit? { [0: .sabah, 2: .ogle, 3: .ikindi, 4: .aksam, 5: .yatsi][i] }
}

/// "Kıldım" kayıtları. Uygulama, widget ve Siri aynı kaydı kullanır (App Group).
/// Biçim: [ "YYYY-MM-DD": [0, 2, 3] ] — o gün kılınan farz vakitlerin sırası.
enum PrayerLog {
  static let key = "prayer_log_v1"
  static let reconciledKey = "prayer_log_reconciled_until"
  static let autoKazaKey = "key_auto_kaza"
  static let changedNotification = Notification.Name("PrayerLogChanged")

  static var defaults: UserDefaults { Shared.defaults }

  static func all() -> [String: [Int]] {
    (defaults.dictionary(forKey: key) as? [String: [Int]]) ?? [:]
  }

  static func prayed(on dateKey: String) -> Set<FarzVakit> {
    Set((all()[dateKey] ?? []).compactMap(FarzVakit.init(rawValue:)))
  }

  static func isPrayed(_ v: FarzVakit, on dateKey: String) -> Bool { prayed(on: dateKey).contains(v) }

  static func count(on dateKey: String) -> Int { prayed(on: dateKey).count }

  static func set(_ v: FarzVakit, on dateKey: String, prayed: Bool) {
    var m = all()
    var s = Set(m[dateKey] ?? [])
    if prayed { s.insert(v.rawValue) } else { s.remove(v.rawValue) }
    m[dateKey] = s.isEmpty ? nil : s.sorted()
    // 400 günden eski kayıtları at (UserDefaults küçük kalsın)
    if let cutoff = DateUtil.cal.date(byAdding: .day, value: -400, to: Date()) {
      let ck = DateUtil.dateKey(cutoff)
      m = m.filter { $0.key >= ck }
    }
    defaults.set(m, forKey: key)
    NotificationCenter.default.post(name: changedNotification, object: nil)
  }

  static func toggle(_ v: FarzVakit, on dateKey: String) {
    set(v, on: dateKey, prayed: !isPrayed(v, on: dateKey))
  }

  /// Şu anda vakti girmiş en son farz (Sabah: imsaktan itibaren). İmsaktan önce nil (gece: dünkü yatsı sayılır).
  static func currentVakit(_ times: PrayerTimes, now: Date = Date()) -> FarzVakit? {
    let c = DateUtil.cal.dateComponents([.hour, .minute], from: now)
    let m = (c.hour ?? 0) * 60 + (c.minute ?? 0)
    var current: FarzVakit?
    for v in FarzVakit.allCases where m >= PrayerTimeUtils.minutes(times.all[v.timeIndex]) { current = v }
    return current
  }

  /// Vakti girmiş (işaretlenebilir) farz vakitler.
  static func enteredVakits(_ times: PrayerTimes, now: Date = Date()) -> [FarzVakit] {
    guard let c = currentVakit(times, now: now) else { return [] }
    return FarzVakit.allCases.filter { $0.rawValue <= c.rawValue }
  }

  /// Seri: bugünden geriye 5/5 kılınan art arda gün sayısı. Bugün henüz tamamlanmadıysa dünden başlar.
  static func streak(today: Date = Date()) -> Int {
    let m = all()
    var n = 0
    var d = today
    if (m[DateUtil.dateKey(d)]?.count ?? 0) < 5 { d = DateUtil.addDays(-1, to: d) }
    while (m[DateUtil.dateKey(d)]?.count ?? 0) >= 5 {
      n += 1
      d = DateUtil.addDays(-1, to: d)
      if n > 400 { break }
    }
    return n
  }

  static func longestStreak() -> Int {
    let days = all().filter { $0.value.count >= 5 }.keys.sorted()
    var best = 0, run = 0
    var prev: Date?
    for k in days {
      guard let d = DateUtil.date(fromKey: k) else { continue }
      if let p = prev, DateUtil.dateKey(DateUtil.addDays(1, to: p)) == k { run += 1 } else { run = 1 }
      best = max(best, run)
      prev = d
    }
    return best
  }

  /// [from, to] arası (dahil): kılınan / toplam ve vakit bazında kılınma sayısı. Bugün yalnız vakti girenler sayılır.
  struct Stats { var prayed = 0; var total = 0; var perVakit: [FarzVakit: (prayed: Int, total: Int)] = [:] }

  static func stats(from: Date, to: Date, todayTimes: PrayerTimes? = nil, now: Date = Date()) -> Stats {
    let m = all()
    var st = Stats()
    for v in FarzVakit.allCases { st.perVakit[v] = (0, 0) }
    let todayKey = DateUtil.dateKey(now)
    var d = DateUtil.startOfDay(from)
    let end = DateUtil.startOfDay(to)
    while d <= end {
      let k = DateUtil.dateKey(d)
      if k > todayKey { break }
      let set = Set(m[k] ?? [])
      let counted: [FarzVakit] = k == todayKey ? (todayTimes.map { enteredVakits($0, now: now) } ?? Array(FarzVakit.allCases)) : FarzVakit.allCases
      for v in counted {
        let ok = set.contains(v.rawValue)
        st.total += 1; if ok { st.prayed += 1 }
        let cur = st.perVakit[v]!
        st.perVakit[v] = (cur.prayed + (ok ? 1 : 0), cur.total + 1)
      }
      d = DateUtil.addDays(1, to: d)
    }
    return st
  }

  #if DEBUG
  /// Ekran görüntüleri için örnek takip verisi (-debug_seed_log YES). Yayın derlemesinde yoktur.
  static func seedSample(today: Date = Date()) {
    var m: [String: [Int]] = [:]
    let pattern = [5, 5, 4, 5, 5, 3, 5, 5, 5, 5, 4, 2, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 5, 3]
    for (i, n) in pattern.enumerated() {
      let k = DateUtil.dateKey(DateUtil.addDays(-(i + 1), to: today))
      m[k] = n == 5 ? [0, 1, 2, 3, 4] : (n == 4 ? [1, 2, 3, 4] : (n == 3 ? [1, 2, 3] : [2, 3]))
    }
    m[DateUtil.dateKey(today)] = [0, 1, 2]
    defaults.set(m, forKey: key)
    defaults.set(DateUtil.dateKey(DateUtil.addDays(-1, to: today)), forKey: reconciledKey)
  }
  #endif

  /// Otomatik kaza: dünden geriye (en çok 30 gün), henüz işlenmemiş ve EN AZ BİR vakit işaretlenmiş günlerde
  /// işaretlenmeyen vakitleri döndürür. Hiç işaret yoksa o gün takip edilmemiş sayılır (kaza eklenmez).
  /// Döndükten sonra işlenen son gün kaydedilir; aynı gün iki kez eklenmez.
  static func reconcileMissed(today: Date = Date()) -> [FarzVakit: Int] {
    let yesterday = DateUtil.dateKey(DateUtil.addDays(-1, to: today))
    let floor = DateUtil.dateKey(DateUtil.addDays(-30, to: today))
    let last = defaults.string(forKey: reconciledKey) ?? ""
    defer { defaults.set(yesterday, forKey: reconciledKey) }
    guard defaults.object(forKey: autoKazaKey) == nil || defaults.bool(forKey: autoKazaKey) else { return [:] }
    var out: [FarzVakit: Int] = [:]
    for (k, list) in all() where k > last && k > floor && k <= yesterday && !list.isEmpty {
      let s = Set(list)
      for v in FarzVakit.allCases where !s.contains(v.rawValue) { out[v, default: 0] += 1 }
    }
    return out
  }
}
