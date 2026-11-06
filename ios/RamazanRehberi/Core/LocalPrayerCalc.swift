import Foundation

/// İnternet yokken: Diyanet yöntemine yakın astronomik hesap (imsak 18°, yatsı 17°, Şafii ikindi, temkinler).
enum LocalPrayerCalc {
  /// 81 il merkezinin yaklaşık koordinatları (enlem, boylam).
  static let provinceCoords: [String: (Double, Double)] = [
    "Adana": (37.00, 35.32), "Adıyaman": (37.76, 38.28), "Afyonkarahisar": (38.76, 30.54), "Ağrı": (39.72, 43.05),
    "Amasya": (40.65, 35.83), "Ankara": (39.93, 32.86), "Antalya": (36.89, 30.71), "Artvin": (41.18, 41.82),
    "Aydın": (37.85, 27.85), "Balıkesir": (39.65, 27.88), "Bilecik": (40.14, 29.98), "Bingöl": (38.88, 40.50),
    "Bitlis": (38.40, 42.11), "Bolu": (40.74, 31.61), "Burdur": (37.72, 30.29), "Bursa": (40.19, 29.06),
    "Çanakkale": (40.15, 26.41), "Çankırı": (40.60, 33.62), "Çorum": (40.55, 34.95), "Denizli": (37.78, 29.09),
    "Diyarbakır": (37.91, 40.24), "Edirne": (41.68, 26.56), "Elazığ": (38.67, 39.22), "Erzincan": (39.75, 39.49),
    "Erzurum": (39.90, 41.27), "Eskişehir": (39.78, 30.52), "Gaziantep": (37.07, 37.38), "Giresun": (40.91, 38.39),
    "Gümüşhane": (40.46, 39.48), "Hakkari": (37.58, 43.74), "Hatay": (36.20, 36.16), "Isparta": (37.76, 30.55),
    "Mersin": (36.80, 34.63), "İstanbul": (41.01, 28.98), "İzmir": (38.42, 27.14), "Kars": (40.60, 43.10),
    "Kastamonu": (41.38, 33.78), "Kayseri": (38.73, 35.48), "Kırklareli": (41.73, 27.22), "Kırşehir": (39.15, 34.16),
    "Kocaeli": (40.77, 29.94), "Konya": (37.87, 32.48), "Kütahya": (39.42, 29.98), "Malatya": (38.35, 38.31),
    "Manisa": (38.61, 27.43), "Kahramanmaraş": (37.58, 36.94), "Mardin": (37.31, 40.74), "Muğla": (37.22, 28.36),
    "Muş": (38.74, 41.49), "Nevşehir": (38.62, 34.71), "Niğde": (37.97, 34.68), "Ordu": (40.98, 37.88),
    "Rize": (41.02, 40.52), "Sakarya": (40.78, 30.40), "Samsun": (41.29, 36.33), "Siirt": (37.93, 41.94),
    "Sinop": (42.03, 35.15), "Sivas": (39.75, 37.02), "Tekirdağ": (40.98, 27.51), "Tokat": (40.31, 36.55),
    "Trabzon": (41.00, 39.72), "Tunceli": (39.11, 39.55), "Şanlıurfa": (37.16, 38.80), "Uşak": (38.68, 29.41),
    "Van": (38.50, 43.38), "Yozgat": (39.82, 34.81), "Zonguldak": (41.45, 31.79), "Aksaray": (38.37, 34.03),
    "Bayburt": (40.26, 40.23), "Karaman": (37.18, 33.22), "Kırıkkale": (39.85, 33.51), "Batman": (37.88, 41.13),
    "Şırnak": (37.52, 42.46), "Bartın": (41.63, 32.34), "Ardahan": (41.11, 42.70), "Iğdır": (39.92, 44.04),
    "Yalova": (40.65, 29.27), "Karabük": (41.20, 32.62), "Kilis": (36.72, 37.12), "Osmaniye": (37.07, 36.25),
    "Düzce": (40.84, 31.16),
  ]

  static func coords(city: String) -> (Double, Double) {
    if let c = provinceCoords[city] { return c }
    if let saved = Shared.defaults.array(forKey: "key_coords_\(city)") as? [Double], saved.count == 2 { return (saved[0], saved[1]) }
    return provinceCoords["İstanbul"]!
  }

  static func times(city: String, district: String, date: Date) -> PrayerTimes {
    let (lat, lon) = coords(city: city)
    // Türkiye illeri her zaman TSİ (UTC+3); yurt dışı için cihaz saat dilimi
    let tz = provinceCoords[city] != nil ? 3.0 : Double(TimeZone.current.secondsFromGMT(for: date)) / 3600
    let jd = julian(date)
    let (decl, eqt) = sun(jd)
    let noon = 12 + tz - lon / 15 - eqt
    func hourAngle(_ angle: Double) -> Double {
      let a = (-sin(rad(angle)) - sin(rad(lat)) * sin(rad(decl))) / (cos(rad(lat)) * cos(rad(decl)))
      return deg(acos(max(-1, min(1, a)))) / 15
    }
    let fajr = noon - hourAngle(18)
    let sunrise = noon - hourAngle(0.833)
    let asrAlt = -deg(atan(1 / (1 + tan(rad(abs(lat - decl))))))
    let asr = noon + hourAngle(asrAlt)
    let maghrib = noon + hourAngle(0.833)
    let isha = noon + hourAngle(17)
    // Diyanet temkinleri (dk)
    func fmt(_ h: Double, _ temkin: Double) -> String {
      var m = Int((h * 60 + temkin).rounded())
      m = ((m % 1440) + 1440) % 1440
      return String(format: "%02d:%02d", m / 60, m % 60)
    }
    return PrayerTimes(
      imsak: fmt(fajr, 0), gunes: fmt(sunrise, -7), ogle: fmt(noon, 5), ikindi: fmt(asr, 4),
      aksam: fmt(maghrib, 6), yatsi: fmt(isha, -1),
      dateStr: DateUtil.dateKey(date), city: city, district: district
    )
  }

  private static func rad(_ d: Double) -> Double { d * .pi / 180 }
  private static func deg(_ r: Double) -> Double { r * 180 / .pi }

  private static func julian(_ date: Date) -> Double {
    let c = DateUtil.cal.dateComponents([.year, .month, .day], from: date)
    var y = Double(c.year!), m = Double(c.month!)
    let d = Double(c.day!)
    if m <= 2 { y -= 1; m += 12 }
    let a = floor(y / 100), b = 2 - a + floor(a / 4)
    return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5
  }

  /// Güneşin deklinasyonu ve zaman denklemi (saat).
  private static func sun(_ jd: Double) -> (Double, Double) {
    let d = jd - 2451545.0
    let g = fix(357.529 + 0.98560028 * d)
    let q = fix(280.459 + 0.98564736 * d)
    let l = fix(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)))
    let e = 23.439 - 0.00000036 * d
    let ra = deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15
    let decl = deg(asin(sin(rad(e)) * sin(rad(l))))
    var eqt = q / 15 - fixHour(ra)
    if eqt > 12 { eqt -= 24 }; if eqt < -12 { eqt += 24 }
    return (decl, eqt)
  }
  private static func fix(_ a: Double) -> Double { let r = a.truncatingRemainder(dividingBy: 360); return r < 0 ? r + 360 : r }
  private static func fixHour(_ a: Double) -> Double { let r = a.truncatingRemainder(dividingBy: 24); return r < 0 ? r + 24 : r }
}
