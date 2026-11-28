import Foundation

/// Diyanet İşleri Başkanlığı'nın ilçe bazlı resmi namaz vakitleri (yaklaşık 30 günlük tablo).
/// Kaynak: ezanvakti.emushaf.net — Diyanet'in yayımladığı değerleri birebir aktarır
/// (ör. Saruhanlı 07.11.2026: 06:12 07:36 12:58 15:46 18:11 19:30, Kıble Saati 10:46).
/// Astronomik hesap (Aladhan) bazı günler 1 dk sapar; bu yüzden önce Diyanet tablosu kullanılır.
enum DiyanetSource {
  private static let base = "https://ezanvakti.emushaf.net"

  struct Day { let dateKey: String; let times: [String]; let kibleSaati: String? }

  static func norm(_ s: String) -> String {
    let lower = s.trimmingCharacters(in: .whitespaces).lowercased(with: Locale(identifier: "tr_TR"))
    let map: [Character: Character] = ["ç": "c", "ğ": "g", "ı": "i", "ö": "o", "ş": "s", "ü": "u", "â": "a", "î": "i", "û": "u"]
    return String(lower.map { map[$0] ?? $0 }.filter { $0.isLetter || $0.isNumber })
  }

  /// Diyanet'in resmi sitesi: ilçenin bütün yılı tek sayfada (365 gün). emushaf ile 32/32 gün birebir aynı
  /// olduğu doğrulandı (İstanbul 9541, Saruhanlı 9719 — 28.11.2026). İlçe numaraları emushaf ile aynıdır.
  private static let officialBase = "https://namazvakitleri.diyanet.gov.tr/tr-TR/"

  /// Önce resmi yıllık tablo (yılda bir + ayda bir tazeleme), sonra emushaf 30 günlük tablo (yıl geçişi ve
  /// Kıble Saati için). İkisinden biri başarılıysa true. Aladhan/yerel hesap yalnızca hiç veri yokken kullanılır.
  static func fetchAndStore(country: String, province: String, district: String) async -> Bool {
    let idKey = "diyanet_id|\(norm(country))|\(norm(province))|\(norm(district))"
    var ilceId = Shared.defaults.string(forKey: idKey)
    if ilceId == nil {
      ilceId = await resolve(country: country, province: province, district: district)
      if let id = ilceId { Shared.defaults.set(id, forKey: idKey) }
    }
    guard let id = ilceId else { return false }
    func store(_ days: [Day]) {
      PrayerCache.save(days.map {
        PrayerTimes(imsak: $0.times[0], gunes: $0.times[1], ogle: $0.times[2], ikindi: $0.times[3],
                    aksam: $0.times[4], yatsi: $0.times[5], dateStr: $0.dateKey, city: province, district: district,
                    source: PrayerTimes.sourceDiyanet)
      })
    }
    var ok = false
    if yearTableIsStale(id: id), let html = await getText(officialBase + id) {
      let year = parseYearTable(html)
      if year.count >= 300 {
        store(year)
        Shared.defaults.set(["year": DateUtil.cal.component(.year, from: Date()), "at": Date().timeIntervalSince1970],
                            forKey: "diyanet_year|\(id)")
        ok = true
      }
    }
    guard let arr = await getArray("\(base)/vakitler/\(id)") else { return ok }
    let days = parse(arr)
    guard !days.isEmpty else { return ok }
    store(days)
    var kible = Shared.defaults.dictionary(forKey: "diyanet_kible") as? [String: String] ?? [:]
    for d in days { if let k = d.kibleSaati { kible[d.dateKey] = k } }
    Shared.defaults.set(kible, forKey: "diyanet_kible")
    return true
  }

  /// Yıllık tablo bu yıl için hiç alınmadıysa ya da 30 günden eskiyse yeniden indirilir.
  static func yearTableIsStale(id: String, now: Date = Date()) -> Bool {
    guard let m = Shared.defaults.dictionary(forKey: "diyanet_year|\(id)"),
          let y = m["year"] as? Int, let at = m["at"] as? Double else { return true }
    return y != DateUtil.cal.component(.year, from: now) || now.timeIntervalSince1970 - at > 30 * 86400
  }

  /// namazvakitleri.diyanet.gov.tr ilçe sayfasındaki "Yıllık Namaz Vakti" tablosu.
  /// Satır: <td>09 Şubat 2026 Pazartesi</td><td>hicri</td><td>İmsak</td>…<td>Yatsı</td>
  static func parseYearTable(_ html: String) -> [Day] {
    guard let cap = html.range(of: "table-caption-yearly") else { return [] }
    let end = html.range(of: "</table>", range: cap.upperBound..<html.endIndex)?.lowerBound ?? html.endIndex
    let seg = String(html[cap.upperBound..<end])
    let t = #"\s*<td>\s*(\d{2}:\d{2})\s*</td>"#
    let pattern = #"<td>\s*(\d{1,2})\s+([^\s<]+)\s+(\d{4})[^<]*</td>\s*<td>[^<]*</td>"# + String(repeating: t, count: 6)
    guard let re = try? NSRegularExpression(pattern: pattern) else { return [] }
    let ns = seg as NSString
    return re.matches(in: seg, range: NSRange(location: 0, length: ns.length)).compactMap { m in
      func g(_ i: Int) -> String { ns.substring(with: m.range(at: i)) }
      guard let day = Int(g(1)), let month = monthNumber(g(2)), let year = Int(g(3)) else { return nil }
      return Day(dateKey: String(format: "%04d-%02d-%02d", year, month, day), times: (4...9).map(g), kibleSaati: nil)
    }
  }

  static func monthNumber(_ raw: String) -> Int? {
    // HTML sayısal karakterlerini çöz (ör. "Eyl&#252;l" → "Eylül")
    var s = raw
    if let re = try? NSRegularExpression(pattern: #"&#(\d+);"#) {
      for m in re.matches(in: s, range: NSRange(s.startIndex..., in: s)).reversed() {
        let r = Range(m.range, in: s)!, code = Int(s[Range(m.range(at: 1), in: s)!]) ?? 63
        s.replaceSubrange(r, with: String(UnicodeScalar(UInt32(code)).map(Character.init) ?? "?"))
      }
    }
    let months = ["ocak", "subat", "mart", "nisan", "mayis", "haziran", "temmuz", "agustos", "eylul", "ekim", "kasim", "aralik"]
    return months.firstIndex(of: norm(s)).map { $0 + 1 }
  }

  /// Diyanet'in o günkü Kıble Saati (önbellekten).
  static func kibleSaati(_ date: Date) -> String? {
    (Shared.defaults.dictionary(forKey: "diyanet_kible") as? [String: String])?[DateUtil.dateKey(date)]
  }

  static func parse(_ arr: [[String: Any]]) -> [Day] {
    let re = try! NSRegularExpression(pattern: #"^\d{2}:\d{2}$"#)
    func ok(_ s: String) -> Bool { re.firstMatch(in: s, range: NSRange(s.startIndex..., in: s)) != nil }
    return arr.compactMap { o in
      guard let d = o["MiladiTarihKisa"] as? String else { return nil }
      let p = d.split(separator: ".")
      guard p.count == 3 else { return nil }
      let t = ["Imsak", "Gunes", "Ogle", "Ikindi", "Aksam", "Yatsi"].map { o[$0] as? String ?? "" }
      guard t.allSatisfy(ok) else { return nil }
      let k = o["KibleSaati"] as? String
      return Day(dateKey: "\(p[2])-\(p[1])-\(p[0])", times: t, kibleSaati: (k.map(ok) ?? false) ? k : nil)
    }
  }

  private static func resolve(country: String, province: String, district: String) async -> String? {
    var c = norm(country); if c == "turkey" { c = "turkiye" }
    guard let countries = await getArray("\(base)/ulkeler"),
          let ulke = pick(countries, "UlkeAdi", "UlkeAdiEn", c), let uid = ulke["UlkeID"] as? String,
          let cities = await getArray("\(base)/sehirler/\(uid)"),
          let sehir = pick(cities, "SehirAdi", "SehirAdiEn", norm(province)), let sid = sehir["SehirID"] as? String,
          let districts = await getArray("\(base)/ilceler/\(sid)") else { return nil }
    // Merkez ilçeler (ör. Yunusemre) Diyanet'te il adıyla geçer
    let ilce = pick(districts, "IlceAdi", "IlceAdiEn", norm(district))
      ?? pick(districts, "IlceAdi", "IlceAdiEn", norm(province))
      ?? (districts.count == 1 ? districts.first : nil)
    return ilce?["IlceID"] as? String
  }

  private static func pick(_ arr: [[String: Any]], _ k1: String, _ k2: String, _ target: String) -> [String: Any]? {
    arr.first { norm($0[k1] as? String ?? "") == target || norm($0[k2] as? String ?? "") == target }
  }

  private static func getText(_ url: String) async -> String? {
    for attempt in 0..<2 {
      if attempt > 0 { try? await Task.sleep(nanoseconds: 700_000_000) }
      guard let u = URL(string: url) else { return nil }
      var req = URLRequest(url: u, timeoutInterval: 15)
      req.setValue("RamazanRehberi-iOS/1.0", forHTTPHeaderField: "User-Agent")
      if let result = try? await URLSession.shared.data(for: req),
         (result.1 as? HTTPURLResponse)?.statusCode == 200,
         let s = String(data: result.0, encoding: .utf8) { return s }
    }
    return nil
  }

  private static func getArray(_ url: String) async -> [[String: Any]]? {
    for attempt in 0..<2 {
      if attempt > 0 { try? await Task.sleep(nanoseconds: 700_000_000) }
      guard let u = URL(string: url) else { return nil }
      var req = URLRequest(url: u, timeoutInterval: 8)
      req.setValue("RamazanRehberi-iOS/1.0", forHTTPHeaderField: "User-Agent")
      if let result = try? await URLSession.shared.data(for: req),
         (result.1 as? HTTPURLResponse)?.statusCode == 200,
         let arr = (try? JSONSerialization.jsonObject(with: result.0)) as? [[String: Any]] { return arr }
    }
    return nil
  }
}
