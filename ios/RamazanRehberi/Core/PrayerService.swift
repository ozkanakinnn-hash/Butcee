import Foundation
import CoreLocation
import WidgetKit

/// Namaz vakitleri: Aladhan (Diyanet yöntemi, method=13) aylık takvim + önbellek + çevrimdışı hesap.
final class PrayerService: ObservableObject {
  static let shared = PrayerService()

  @Published var today: PrayerTimes
  @Published var tomorrow: PrayerTimes
  @Published var isLoading = false

  private init() {
    today = PrayerCache.timesFor(date: Date())
    tomorrow = PrayerCache.timesFor(date: DateUtil.addDays(1, to: Date()))
  }

  static let countryEnglish: [String: String] = [
    "TR": "Turkey", "DE": "Germany", "NL": "Netherlands", "FR": "France", "AT": "Austria", "BE": "Belgium",
    "GB": "United Kingdom", "CH": "Switzerland", "SE": "Sweden", "DK": "Denmark", "NO": "Norway",
    "US": "United States", "CA": "Canada", "AU": "Australia", "AZ": "Azerbaijan", "SA": "Saudi Arabia",
    "BA": "Bosnia and Herzegovina", "CY": "Cyprus",
  ]

  func reload() {
    let st = AppData.shared
    let now = Date()
    today = PrayerCache.timesFor(date: now)
    tomorrow = PrayerCache.timesFor(date: DateUtil.addDays(1, to: now))
    let city = st.province, district = st.district, countryName = st.country
    let code = AppContent.shared.countries.first { $0.name == countryName }?.code ?? "TR"
    let country = Self.countryEnglish[code] ?? countryName
    // Bu ay + gelecek ay önbellekte yoksa indir
    let cal = DateUtil.cal
    let months: [Date] = [now, cal.date(byAdding: .month, value: 1, to: now)!]
    let missing = months.filter { m in
      let last = cal.date(byAdding: .day, value: -1, to: cal.date(byAdding: .month, value: 1, to: cal.date(from: cal.dateComponents([.year, .month], from: m))!)!)!
      return PrayerCache.get(city: city, district: district, dateKey: DateUtil.dateKey(last)) == nil
        || PrayerCache.get(city: city, district: district, dateKey: DateUtil.dateKey(m)) == nil
    }
    if missing.isEmpty { afterUpdate(); return }
    isLoading = true
    Task {
      await geocodeIfNeeded(city: city, district: district, country: country, code: code)
      for m in missing {
        let c = cal.dateComponents([.year, .month], from: m)
        await fetchMonth(year: c.year!, month: c.month!, city: city, district: district, country: country)
      }
      await MainActor.run {
        self.today = PrayerCache.timesFor(date: Date())
        self.tomorrow = PrayerCache.timesFor(date: DateUtil.addDays(1, to: Date()))
        self.isLoading = false
        self.afterUpdate()
      }
    }
  }

  private func afterUpdate() {
    NotificationScheduler.rescheduleAll()
    WidgetCenter.shared.reloadAllTimelines()
  }

  private func fetchMonth(year: Int, month: Int, city: String, district: String, country: String) async {
    let isTurkey = country == "Turkey"
    var comps: URLComponents
    if AppData.shared.useCoordinates, let c = Shared.defaults.array(forKey: "key_gps_coords") as? [Double], c.count == 2 {
      // GPS koordinatına göre (yurt dışı / ilçe eşleşmedi)
      comps = URLComponents(string: "https://api.aladhan.com/v1/calendar/\(year)/\(month)")!
      comps.queryItems = [
        URLQueryItem(name: "latitude", value: String(c[0])),
        URLQueryItem(name: "longitude", value: String(c[1])),
        URLQueryItem(name: "method", value: "13"),
      ]
    } else {
      let address = isTurkey ? "\(district), \(city), Turkey" : "\(city), \(country)"
      comps = URLComponents(string: "https://api.aladhan.com/v1/calendarByAddress/\(year)/\(month)")!
      comps.queryItems = [
        URLQueryItem(name: "address", value: address),
        URLQueryItem(name: "method", value: "13"),
      ]
    }
    guard let url = comps.url else { return }
    var req = URLRequest(url: url, timeoutInterval: 8)
    req.setValue("RamazanRehberi-iOS/1.0", forHTTPHeaderField: "User-Agent")
    do {
      let (data, resp) = try await URLSession.shared.data(for: req)
      guard (resp as? HTTPURLResponse)?.statusCode == 200,
            let root = try JSONSerialization.jsonObject(with: data) as? [String: Any],
            let days = root["data"] as? [[String: Any]] else { return }
      var list: [PrayerTimes] = []
      for d in days {
        guard let tm = d["timings"] as? [String: String],
              let date = (d["date"] as? [String: Any])?["gregorian"] as? [String: Any],
              let ds = date["date"] as? String else { continue }
        let p = ds.split(separator: "-")
        guard p.count == 3 else { continue }
        let key = "\(p[2])-\(p[1])-\(p[0])"
        list.append(PrayerTimes(
          imsak: PrayerTimeUtils.clean(tm["Imsak"], fallback: "05:45"),
          gunes: PrayerTimeUtils.clean(tm["Sunrise"], fallback: "07:10"),
          ogle: PrayerTimeUtils.clean(tm["Dhuhr"], fallback: "13:05"),
          ikindi: PrayerTimeUtils.clean(tm["Asr"], fallback: "16:20"),
          aksam: PrayerTimeUtils.clean(tm["Maghrib"], fallback: "18:45"),
          yatsi: PrayerTimeUtils.clean(tm["Isha"], fallback: "20:05"),
          dateStr: key, city: city, district: district))
      }
      if !list.isEmpty { PrayerCache.save(list) }
    } catch {
      // Çevrimdışı: yerel hesap kullanılır
    }
  }

  /// Yurt dışı şehirler için çevrimdışı hesapta kullanılacak koordinatı bir kez kaydeder.
  private func geocodeIfNeeded(city: String, district: String, country: String, code: String) async {
    if LocalPrayerCalc.provinceCoords[city] != nil { return }
    if Shared.defaults.array(forKey: "key_coords_\(city)") != nil { return }
    let geocoder = CLGeocoder()
    if let place = try? await geocoder.geocodeAddressString("\(city), \(country)").first, let loc = place.location {
      Shared.defaults.set([loc.coordinate.latitude, loc.coordinate.longitude], forKey: "key_coords_\(city)")
    }
  }

  func times(for date: Date) -> PrayerTimes { PrayerCache.timesFor(date: date) }

  /// Kıble ve camiler için şehir koordinatı.
  var cityCoordinate: CLLocationCoordinate2D {
    let c = LocalPrayerCalc.coords(city: AppData.shared.province)
    return CLLocationCoordinate2D(latitude: c.0, longitude: c.1)
  }
}
