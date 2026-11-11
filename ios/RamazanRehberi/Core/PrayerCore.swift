import Foundation

/// Uygulama ile widget arasında paylaşılan depolama.
enum Shared {
  static let appGroup = "group.akin.ai2"
  static var defaults: UserDefaults = UserDefaults(suiteName: appGroup) ?? .standard
}

struct PrayerTimes: Codable, Hashable {
  var imsak: String
  var gunes: String
  var ogle: String
  var ikindi: String
  var aksam: String
  var yatsi: String
  var dateStr: String   // "YYYY-MM-DD"
  var city: String
  var district: String

  var all: [String] { [imsak, gunes, ogle, ikindi, aksam, yatsi] }
  static let names = ["İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"]
}

enum CountdownMode { case sahur, iftar, teravih, prayer }

struct CountdownResult {
  let title: String
  let targetTimeName: String
  let targetTimeString: String
  let hours: Int, minutes: Int, seconds: Int
  let totalSecondsRemaining: Int
  let isIftarTimeNow: Bool
  let progress: Double
  let mode: CountdownMode
  let nextLabel: String
  let targetDate: Date
}

enum PrayerTimeUtils {
  static func clean(_ raw: String?, fallback: String = "00:00") -> String {
    guard let raw, let r = raw.range(of: #"\d{1,2}:\d{2}"#, options: .regularExpression) else { return fallback }
    let p = raw[r].split(separator: ":").compactMap { Int($0) }
    guard p.count == 2 else { return fallback }
    return String(format: "%02d:%02d", min(max(p[0], 0), 23), min(max(p[1], 0), 59))
  }

  static func minutes(_ s: String) -> Int {
    let p = clean(s).split(separator: ":").compactMap { Int($0) }
    return p.count == 2 ? p[0] * 60 + p[1] : 0
  }

  /// Verilen günün (gün başlangıcı) üzerine "HH:mm" saatini ekler.
  static func date(_ time: String, on day: Date) -> Date {
    DateUtil.startOfDay(day).addingTimeInterval(TimeInterval(minutes(time) * 60))
  }

  /// Akıllı geri sayım (Android ile aynı kurallar).
  static func countdown(_ pt: PrayerTimes, fastingDates: Set<String>, now: Date = Date()) -> CountdownResult {
    let comps = DateUtil.cal.dateComponents([.hour, .minute, .second], from: now)
    let nowSec = comps.hour! * 3600 + comps.minute! * 60 + comps.second!
    let ts = pt.all.map { clean($0) }
    let secs = ts.map { minutes($0) * 60 }
    let imsakSec = secs[0], aksamSec = secs[4], yatsiSec = secs[5]
    let day = 86400
    let todayKey = DateUtil.dateKey(now)
    let tomorrowKey = DateUtil.dateKey(DateUtil.addDays(1, to: now))
    let ramadanTomorrow = RamadanCalendar.isRamadanDay(tomorrowKey)
    let fastToday = RamadanCalendar.isRamadanDay(todayKey) || fastingDates.contains(todayKey)
    let fastTomorrow = ramadanTomorrow || fastingDates.contains(tomorrowKey)
    func label(_ i: Int) -> String { "\(PrayerTimes.names[i % 6]) \(ts[i % 6])" }

    var mode = CountdownMode.prayer
    var title = "", name = "", timeStr = "", next = ""
    var target = 0, start = 0
    if nowSec < imsakSec && fastToday {
      mode = .sahur; title = "İmsaka Kalan Vakit"; name = "İmsak (Sahur)"; timeStr = ts[0]; target = imsakSec; start = yatsiSec - day; next = label(1)
    } else if nowSec >= imsakSec && nowSec < aksamSec && fastToday {
      mode = .iftar; title = "İftara Kalan Vakit"; name = "Akşam (İftar)"; timeStr = ts[4]; target = aksamSec; start = imsakSec; next = label(5)
    } else if nowSec >= aksamSec && nowSec < yatsiSec && ramadanTomorrow {
      mode = .teravih; title = "Teravihe Kalan Vakit"; name = "Yatsı (Teravih)"; timeStr = ts[5]; target = yatsiSec; start = aksamSec; next = label(0)
    } else if nowSec >= yatsiSec && fastTomorrow {
      mode = .sahur; title = "İmsaka Kalan Vakit"; name = "İmsak (Sahur)"; timeStr = ts[0]; target = imsakSec + day; start = yatsiSec; next = label(1)
    } else {
      let idx = secs.firstIndex { $0 > nowSec }
      let ti = idx ?? 0
      target = idx == nil ? imsakSec + day : secs[ti]
      if let i = idx { start = i == 0 ? yatsiSec - day : secs[i - 1] } else { start = yatsiSec }
      name = PrayerTimes.names[ti]
      title = "\(name) Vaktine Kalan"; timeStr = ts[ti]; next = label(ti + 1)
    }
    let remaining = max(0, target - nowSec)
    let duration = max(1, target - start)
    let progress = min(1, max(0, Double(nowSec - start) / Double(duration)))
    return CountdownResult(
      title: t(title), targetTimeName: t(name), targetTimeString: timeStr,
      hours: remaining / 3600, minutes: (remaining % 3600) / 60, seconds: remaining % 60,
      totalSecondsRemaining: remaining,
      isIftarTimeNow: fastToday && abs(nowSec - aksamSec) < 30,
      progress: progress, mode: mode, nextLabel: t(next),
      targetDate: now.addingTimeInterval(TimeInterval(remaining))
    )
  }
}

/// Önbellek: "il/ilçe" için günlük vakitler (widget da okur).
enum PrayerCache {
  private static let key = "prayer_cache_v3"

  static func all() -> [String: PrayerTimes] {
    guard let data = Shared.defaults.data(forKey: key),
          let m = try? JSONDecoder().decode([String: PrayerTimes].self, from: data) else { return [:] }
    return m
  }

  /// [overwrite] false: yalnızca boş günleri doldurur (Aladhan, Diyanet tablosunun üzerine yazmasın).
  static func save(_ list: [PrayerTimes], overwrite: Bool = true) {
    var m = all()
    let today = DateUtil.dateKey(DateUtil.addDays(-2, to: Date()))
    m = m.filter { $0.value.dateStr >= today }
    for p in list {
      let k = cacheKey(p.city, p.district, p.dateStr)
      if overwrite || m[k] == nil { m[k] = p }
    }
    if let data = try? JSONEncoder().encode(m) { Shared.defaults.set(data, forKey: key) }
  }

  static func get(city: String, district: String, dateKey: String) -> PrayerTimes? {
    all()[cacheKey(city, district, dateKey)]
  }

  static func cacheKey(_ c: String, _ d: String, _ k: String) -> String { "\(c)_\(d)_\(k)" }

  /// Widget ve bildirimler için: o konumun bugünkü/yarınki vakitleri (yoksa yaklaşık hesap).
  static func timesFor(date: Date) -> PrayerTimes {
    let s = Shared.defaults
    let city = s.string(forKey: "key_province") ?? "İstanbul"
    let district = s.string(forKey: "key_district") ?? "Fatih"
    let key = DateUtil.dateKey(date)
    return get(city: city, district: district, dateKey: key) ?? LocalPrayerCalc.times(city: city, district: district, date: date)
  }
}
