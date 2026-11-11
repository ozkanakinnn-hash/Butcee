import SwiftUI
import CoreLocation
import Combine

// MARK: - Hesaplar (Diyanet yöntemi)

/// Diyanet İşleri Başkanlığı yöntemi:
/// 1) Kâbe: 21°25'21" K, 39°49'34" D.
/// 2) Kıble açısı = Kâbe'ye büyük çember başlangıç açısı, GERÇEK kuzeyden saat yönünde.
/// 3) Diyanet'in yayımladığı "Kıble Açısı" el pusulası içindir: MANYETİK kuzeye göre, tam derece
///    (manyetik = gerçek − sapma).
/// 4) Kıble Saati: Güneş'in tam kıble istikametinde bulunduğu an.
enum QiblaCalc {
  static let kaabaLat = 21.0 + 25.0 / 60 + 21.0 / 3600
  static let kaabaLon = 39.0 + 49.0 / 60 + 34.0 / 3600

  static func norm(_ a: Double) -> Double { let r = a.truncatingRemainder(dividingBy: 360); return r < 0 ? r + 360 : r }
  /// + sağa, − sola; −180…180
  static func turn(_ target: Double, _ heading: Double) -> Double { let d = norm(target - heading); return d > 180 ? d - 360 : d }

  static func trueBearing(_ c: CLLocationCoordinate2D) -> Double {
    let p1 = c.latitude * .pi / 180, p2 = kaabaLat * .pi / 180
    let dl = (kaabaLon - c.longitude) * .pi / 180
    let y = sin(dl) * cos(p2)
    let x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
    return norm(atan2(y, x) * 180 / .pi)
  }

  static func distanceKm(_ c: CLLocationCoordinate2D) -> Double {
    let p1 = c.latitude * .pi / 180, p2 = kaabaLat * .pi / 180
    let dp = p2 - p1, dl = (kaabaLon - c.longitude) * .pi / 180
    let a = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
    return 2 * 6371.0088 * atan2(sqrt(a), sqrt(1 - a))
  }

  static func diyanetAngle(trueBearing: Double, declination: Double) -> Int { Int(norm(trueBearing - declination).rounded()) % 360 }

  /// Diyanet tabloları 2010 civarı manyetik sapmayla hesaplanmıştır (Saruhanlı 141°, İstanbul 147°,
  /// Ankara 155°). iOS'ta manyetik model API'si yok: bugünkü sapmadan Türkiye için ≈0,1°/yıl
  /// doğuya kayma çıkarılarak 2010 sapması bulunur (Android WMM ile doğrulandı).
  static func diyanetPublishedAngle(trueBearing: Double, currentDeclination: Double, now: Date = Date()) -> Int {
    let year = Double(Calendar(identifier: .gregorian).component(.year, from: now)) + 0.5
    return diyanetAngle(trueBearing: trueBearing, declination: currentDeclination - 0.1 * (year - 2010.5))
  }

  /// Güneşin azimut/yüksekliği (NOAA).
  static func sun(_ c: CLLocationCoordinate2D, _ date: Date) -> (azimuth: Double, elevation: Double) {
    func r(_ d: Double) -> Double { d * .pi / 180 }
    let ms = date.timeIntervalSince1970 * 1000
    let jd = ms / 86_400_000 + 2440587.5
    let t = (jd - 2451545) / 36525
    let l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)).truncatingRemainder(dividingBy: 360)
    let m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    let e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
    let cc = sin(r(m)) * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin(r(2 * m)) * (0.019993 - 0.000101 * t) + sin(r(3 * m)) * 0.000289
    let omega = 125.04 - 1934.136 * t
    let lambda = l0 + cc - 0.00569 - 0.00478 * sin(r(omega))
    let eps0 = 23 + (26 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60) / 60
    let eps = eps0 + 0.00256 * cos(r(omega))
    let decl = asin(sin(r(eps)) * sin(r(lambda)))
    let y = pow(tan(r(eps) / 2), 2)
    let eot = 4 * (y * sin(2 * r(l0)) - 2 * e * sin(r(m)) + 4 * e * y * sin(r(m)) * cos(2 * r(l0)) - 0.5 * y * y * sin(4 * r(l0)) - 1.25 * e * e * sin(2 * r(m))) * 180 / .pi
    var utcMin = ms.truncatingRemainder(dividingBy: 86_400_000) / 60_000
    if utcMin < 0 { utcMin += 1440 }
    var tst = (utcMin + eot + 4 * c.longitude).truncatingRemainder(dividingBy: 1440)
    if tst < 0 { tst += 1440 }
    let ha = tst / 4 - 180
    let lat = r(c.latitude)
    let zen = acos(min(1, max(-1, sin(lat) * sin(decl) + cos(lat) * cos(decl) * cos(r(ha)))))
    let azRaw = acos(min(1, max(-1, (sin(lat) * cos(zen) - sin(decl)) / (cos(lat) * sin(zen))))) * 180 / .pi
    let az = ha > 0 ? (azRaw + 180).truncatingRemainder(dividingBy: 360) : (540 - azRaw).truncatingRemainder(dividingBy: 360)
    return (az, 90 - zen * 180 / .pi)
  }

  /// Kıble Saati (o gün güneş azimutu = kıble açısı, güneş ufkun üzerinde) ya da nil.
  static func qiblaTime(_ c: CLLocationCoordinate2D, day: Date = Date(), calendar: Calendar = .current) -> Date? {
    let bearing = trueBearing(c)
    let start = calendar.startOfDay(for: day)
    var prev = sun(c, start)
    for minute in 1...1440 {
      let time = start.addingTimeInterval(Double(minute) * 60)
      let cur = sun(c, time)
      let before = turn(bearing, prev.azimuth), after = turn(bearing, cur.azimuth)
      if cur.elevation > 0 && before > 0 && after <= 0 && before - after < 30 {
        return time.addingTimeInterval(-60 + 60 * before / (before - after))
      }
      prev = cur
    }
    return nil
  }
}

// MARK: - Titreme süzgeci (Android ile aynı)

struct HeadingFilter {
  var calmTau = 0.45, fastTau = 0.08, fastEnter = 15.0, fastExit = 2.0, deadband = 0.6
  private var sx = 0.0, sy = 0.0, last: TimeInterval = 0, initialized = false, fast = false
  private(set) var output = 0.0
  private(set) var jitter = 0.0

  mutating func reset() { initialized = false; fast = false }

  @discardableResult
  mutating func update(_ heading: Double, at t: TimeInterval) -> Double {
    let rad = heading * .pi / 180
    if !initialized { sx = cos(rad); sy = sin(rad); last = t; initialized = true; output = heading; jitter = 0; return output }
    let dt = min(0.5, max(0.001, t - last)); last = t
    let current = atan2(sy, sx) * 180 / .pi
    let innovation = abs(QiblaCalc.turn(heading, current))
    fast = innovation > fastEnter || (fast && innovation > fastExit)
    let alpha = 1 - exp(-dt / (fast ? fastTau : calmTau))
    sx += alpha * (cos(rad) - sx); sy += alpha * (sin(rad) - sy)
    let len = sqrt(sx * sx + sy * sy); if len > 1e-6 { sx /= len; sy /= len }
    jitter += (1 - exp(-dt / 1.0)) * (innovation - jitter)
    let smoothed = QiblaCalc.norm(atan2(sy, sx) * 180 / .pi)
    let delta = QiblaCalc.turn(smoothed, QiblaCalc.norm(output))
    if abs(delta) >= deadband { output += delta }
    return output
  }
}

// MARK: - Pusula (CoreLocation; iOS yönü kendisi eğim ve gerçek kuzey için düzeltir)

final class QiblaCompass: NSObject, ObservableObject, CLLocationManagerDelegate {
  private let manager = CLLocationManager()
  private var filter = HeadingFilter()
  /// Gerçek kuzeye göre kesintisiz yön (animasyon için sarılmaz)
  @Published var continuousHeading: Double = 0
  @Published var ready = false
  /// Gerçek kuzey yok (konum izni yok) → manyetik kullanılıyor
  @Published var usingMagnetic = false
  /// Derece cinsinden belirsizlik; < 0 geçersiz
  @Published var accuracy: Double = -1
  /// gerçek − manyetik (iOS'un hesapladığı sapma)
  @Published var declination: Double?
  @Published var jitter: Double = 0
  @Published var location: CLLocation?
  @Published var authorized = false

  var heading: Double { QiblaCalc.norm(continuousHeading) }
  var available: Bool { CLLocationManager.headingAvailable() }

  override init() {
    super.init()
    manager.delegate = self
    manager.desiredAccuracy = kCLLocationAccuracyBest
    manager.headingFilter = kCLHeadingFilterNone   // süzme bizde
    manager.headingOrientation = .portrait
  }

  func start() {
    filter.reset(); ready = false
    manager.requestWhenInUseAuthorization()
    manager.startUpdatingLocation()
    if available { manager.startUpdatingHeading() }
  }

  func stop() { manager.stopUpdatingHeading(); manager.stopUpdatingLocation() }

  func locationManagerDidChangeAuthorization(_ m: CLLocationManager) {
    authorized = m.authorizationStatus == .authorizedWhenInUse || m.authorizationStatus == .authorizedAlways
    if authorized { m.startUpdatingLocation() }
  }

  func locationManager(_ m: CLLocationManager, didUpdateHeading h: CLHeading) {
    guard h.headingAccuracy >= 0 else { accuracy = -1; return }
    let hasTrue = h.trueHeading >= 0
    let raw = hasTrue ? h.trueHeading : h.magneticHeading
    usingMagnetic = !hasTrue
    if hasTrue { declination = QiblaCalc.turn(h.trueHeading, h.magneticHeading) }
    filter.update(raw, at: h.timestamp.timeIntervalSince1970)
    continuousHeading = filter.output
    jitter = filter.jitter
    accuracy = h.headingAccuracy
    ready = true
  }

  func locationManager(_ m: CLLocationManager, didUpdateLocations locs: [CLLocation]) {
    if let l = locs.last, l.horizontalAccuracy >= 0 { location = l }
  }

  func locationManagerShouldDisplayHeadingCalibration(_ m: CLLocationManager) -> Bool { true }
}

// MARK: - Ekran

struct QiblaView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var prayers: PrayerService
  @StateObject private var compass = QiblaCompass()
  @State private var lastAligned = false

  private var en: Bool { L10n.isEnglish }
  private var coord: CLLocationCoordinate2D { compass.location?.coordinate ?? prayers.cityCoordinate }
  private let tolerance = 3.0

  var body: some View {
    let q = QiblaCalc.trueBearing(coord)
    let turn = QiblaCalc.turn(q, compass.heading)
    let aligned = compass.ready && abs(turn) <= tolerance
    let green = Color(hex: 0x2E7D57), gold = Color(hex: 0xC59B3C)
    ScrollView {
      VStack(spacing: 14) {
        Text(compass.location != nil ? (en ? "📍 Device location (GPS)" : "📍 Cihaz konumu (GPS)") : store.locationLabel)
          .font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)

        // Durum
        VStack(spacing: 4) {
          Text(!compass.available ? (en ? "Your device has no compass" : "Cihazınızda pusula yok")
               : !compass.ready ? (en ? "Starting compass…" : "Pusula hazırlanıyor…")
               : aligned ? (en ? "🕋 YOU ARE FACING THE QIBLA" : "🕋 KIBLEYE DÖNDÜNÜZ")
               : (en ? "FIND THE QIBLA" : "KIBLE YÖNÜNÜ BULUN"))
            .font(.system(size: 17, weight: .bold))
          if compass.ready && !aligned {
            let d = Int(abs(turn).rounded())
            Text(turn > 0 ? (en ? "Turn right \(d)°  ⟳" : "Sağa doğru \(d)° dönün  ⟳") : (en ? "⟲  Turn left \(d)°" : "⟲  Sola doğru \(d)° dönün"))
              .font(.system(size: 14))
          }
          if aligned { Text(en ? "Aligned. You may begin your prayer." : "Hizalama tamam. Huşû ile namaza durabilirsiniz.").font(.system(size: 13)) }
        }
        .foregroundStyle(aligned ? .white : p.onSurfaceVariant)
        .frame(maxWidth: .infinity).padding(14)
        .background(RoundedRectangle(cornerRadius: 18).fill(aligned ? green : p.onSurfaceVariant.opacity(0.12)))
        .animation(.easeInOut(duration: 0.25), value: aligned)

        ForEach(warnings, id: \.self) { w in
          Text(w).font(.system(size: 13)).padding(10).frame(maxWidth: .infinity, alignment: .leading)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.red.opacity(0.15)))
        }

        dial(q: q, aligned: aligned, green: green, gold: gold).frame(width: 300, height: 300).padding(.vertical, 6)
        Text((en ? "Heading: " : "Telefon yönü: ") + "\(Int(compass.heading.rounded()) % 360)°")
          .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)

        // Diyanet bilgileri
        VStack(alignment: .leading, spacing: 8) {
          Text(en ? "Qibla Data" : "Kıble Bilgileri").font(.system(size: 16, weight: .bold))
          if let d = compass.declination {
            row(en ? "Qibla angle (official calendar)" : "Kıble açısı (resmî takvim)", "\(QiblaCalc.diyanetPublishedAngle(trueBearing: q, currentDeclination: d))°", bold: true)
          }
          row(en ? "Qibla angle (true north)" : "Kıble açısı (gerçek kuzey)", String(format: "%.1f°", q))
          if let d = compass.declination {
            row(en ? "Today's hand-compass angle" : "Bugünkü el pusulası açısı", "\(QiblaCalc.diyanetAngle(trueBearing: q, declination: d))°")
            row(en ? "Magnetic declination (today)" : "Manyetik sapma (bugün)", String(format: "%+.1f°", d))
          }
          row(en ? "Distance to the Kaaba" : "Kâbe'ye uzaklık", String(format: "%.0f km", QiblaCalc.distanceKm(coord)))
          row(en ? "Today's qibla time" : "Bugünkü Kıble Saati",
              (compass.location == nil ? DiyanetSource.kibleSaati(Date()) : nil)
                ?? QiblaCalc.qiblaTime(coord).map { $0.formatted(date: .omitted, time: .shortened) } ?? "—", bold: true)
          Text(en ? "At the qibla time, facing the sun means facing the qibla. Unlike a compass, the sun is not affected by magnets or metal."
                  : "Kıble Saati'nde güneşe dönen kişi kıbleye dönmüş olur. Güneş, pusulanın aksine mıknatıs ve metalden etkilenmez; pusulayı bu saatte kontrol edebilirsiniz.")
            .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
        }.padding(16).frame(maxWidth: .infinity, alignment: .leading).card(radius: 18)

        Text(en ? "Tips: hold the phone flat • remove magnetic cases • stay away from metal, speakers and cars • calibrate with a figure-8 if warned."
                : "İpuçları: Telefonu avucunuzda yatay tutun • Mıknatıslı kılıfı çıkarın • Metal eşya, hoparlör ve araç içinden uzak durun • Uyarı çıkarsa 8 çizerek kalibre edin.")
          .font(.system(size: 12)).multilineTextAlignment(.center).foregroundStyle(p.onSurfaceVariant)
      }
      .padding(16)
    }
    .screen()
    .navigationTitle(t("Kıble Bulucu"))
    .onAppear { compass.start() }
    .onDisappear { compass.stop() }
    .onChange(of: aligned) { a in if a { UIImpactFeedbackGenerator(style: .medium).impactOccurred() } }
  }

  private var warnings: [String] {
    var w: [String] = []
    if compass.usingMagnetic { w.append(en ? "📍 Allow location access: without it the compass uses magnetic north (≈6° off in Türkiye)." : "📍 Konum iznini açın: izin yokken pusula manyetik kuzeyi kullanır (Türkiye'de ≈6° sapma).") }
    if compass.ready && compass.accuracy > 20 { w.append(en ? "🔄 Low compass accuracy (±\(Int(compass.accuracy))°): move away from metal/magnets and calibrate with a figure-8." : "🔄 Pusula hassasiyeti düşük (±\(Int(compass.accuracy))°): metal ve mıknatıstan uzaklaşın, 8 çizerek kalibre edin.") }
    if compass.ready && compass.jitter > 8 { w.append(en ? "〰️ Reading is unsteady; hold the phone still." : "〰️ Ölçüm dalgalanıyor; telefonu sabit tutun.") }
    return w
  }

  private func row(_ l: String, _ v: String, bold: Bool = false) -> some View {
    HStack {
      Text(l).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
      Spacer()
      Text(v).font(.system(size: 14, weight: bold ? .bold : .semibold)).foregroundStyle(p.primary)
    }
  }

  private func dial(q: Double, aligned: Bool, green: Color, gold: Color) -> some View {
    let ring = aligned ? green : gold
    return ZStack {
      ZStack {
        Circle().fill(p.surface)
        Circle().stroke(ring, lineWidth: 4)
        ForEach(0..<72, id: \.self) { i in
          Rectangle().fill(i % 6 == 0 ? gold : p.onSurfaceVariant.opacity(0.35))
            .frame(width: i % 6 == 0 ? 2.5 : 1.2, height: i % 6 == 0 ? 14 : (i % 2 == 0 ? 9 : 5))
            .offset(y: -138).rotationEffect(.degrees(Double(i) * 5))
        }
        ForEach(Array(zip(en ? ["N", "E", "S", "W"] : ["K", "D", "G", "B"], [0.0, 90, 180, 270])), id: \.0) { l, a in
          Text(l).font(.system(size: 17, weight: .bold)).foregroundStyle(a == 0 ? Color.red : p.onBackground)
            .rotationEffect(.degrees(-a + compass.continuousHeading))
            .offset(y: -108).rotationEffect(.degrees(a))
        }
        // Kıble ibresi + Kâbe rozeti
        ZStack {
          Capsule().fill(LinearGradient(colors: [ring.opacity(0.3), ring], startPoint: .bottom, endPoint: .top)).frame(width: 7, height: 112).offset(y: -56)
          Image("ic_kaaba").resizable().scaledToFit().frame(width: 24, height: 24)
            .padding(5).background(Circle().fill(ring))
            .rotationEffect(.degrees(-q + compass.continuousHeading))
            .offset(y: -134)
        }
        .rotationEffect(.degrees(q))
      }
      .rotationEffect(.degrees(-compass.continuousHeading))
      .animation(.interpolatingSpring(stiffness: 120, damping: 22), value: compass.continuousHeading)
      Circle().fill(ring).frame(width: 18, height: 18)
      Capsule().fill(aligned ? green : .red).frame(width: 4, height: 18).offset(y: -141)
    }
  }
}
