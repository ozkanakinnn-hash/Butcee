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
  var available: Bool { demo || CLLocationManager.headingAvailable() }
  /// Simülatörde mağaza görüntüsü için sabit yön (yalnızca DEBUG, `-debug_heading 145`)
  private var demo = false

  override init() {
    super.init()
    manager.delegate = self
    manager.desiredAccuracy = kCLLocationAccuracyBest
    manager.headingFilter = kCLHeadingFilterNone   // süzme bizde
    manager.headingOrientation = .portrait
  }

  func start() {
    #if DEBUG
    if UserDefaults.standard.object(forKey: "debug_heading") != nil {
      demo = true
      continuousHeading = UserDefaults.standard.double(forKey: "debug_heading")
      accuracy = 5; declination = 5.5; ready = true
      return
    }
    #endif
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

// MARK: - Ekran (sade)

struct QiblaView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var prayers: PrayerService
  @StateObject private var compass = QiblaCompass()
  @AppStorage("qiblaCalibrationIntro") private var calibrationIntro = true
  @State private var showCalibration = false
  @State private var showDetails = false

  private var en: Bool { L10n.isEnglish }
  private var coord: CLLocationCoordinate2D { compass.location?.coordinate ?? prayers.cityCoordinate }
  private let tolerance = 3.0
  private let green = Color(hex: 0x2E7D57), gold = Color(hex: 0xC59B3C)

  /// iOS derece cinsinden belirsizlik verir; Android'deki 0–3 düzeyine çevrilir.
  private var accuracyLevel: Int {
    guard compass.ready, compass.accuracy >= 0 else { return -1 }
    return compass.accuracy > 25 ? 1 : (compass.accuracy > 12 ? 2 : 3)
  }

  private var qiblaTimeText: String {
    (compass.location == nil ? DiyanetSource.kibleSaati(Date()) : nil)
      ?? QiblaCalc.qiblaTime(coord).map { $0.formatted(date: .omitted, time: .shortened) } ?? "—"
  }

  var body: some View {
    let q = QiblaCalc.trueBearing(coord)
    let turn = QiblaCalc.turn(q, compass.heading)
    let aligned = compass.ready && abs(turn) <= tolerance
    let official = compass.declination.map { QiblaCalc.diyanetPublishedAngle(trueBearing: q, currentDeclination: $0) } ?? Int(q.rounded())
    ZStack {
      ScrollView {
        VStack(spacing: 12) {
          Text(compass.location != nil ? (en ? "📍 Device location (GPS)" : "📍 Cihaz konumu (GPS)") : store.locationLabel)
            .font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)

          // Tek satır büyük durum
          VStack(spacing: 4) {
            Text(statusText(turn: turn, aligned: aligned))
              .font(.system(size: aligned ? 22 : 24, weight: .bold))
              .foregroundStyle(aligned ? green : p.onBackground)
              .multilineTextAlignment(.center)
            if aligned {
              Text(en ? "You may begin your prayer." : "Huşû ile namaza durabilirsiniz.").font(.system(size: 14)).foregroundStyle(green)
            }
          }
          .frame(maxWidth: .infinity, minHeight: 72)
          .animation(.easeInOut(duration: 0.25), value: aligned)

          dial(q: q, aligned: aligned).frame(width: 310, height: 310)

          // En önemli tek uyarı
          ZStack {
            if let w = warning {
              Text(w).font(.system(size: 13)).multilineTextAlignment(.center)
                .padding(.horizontal, 12).padding(.vertical, 8)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.red.opacity(0.15)))
            }
          }.frame(minHeight: 44)

          Text(en ? "Qibla angle \(official)° • Qibla time \(qiblaTimeText)" : "Kıble açısı \(official)° • Kıble saati \(qiblaTimeText)")
            .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          Button(showDetails ? (en ? "Hide details ▴" : "Ayrıntıları gizle ▴") : (en ? "Details ▾" : "Ayrıntılar ▾")) {
            withAnimation(.easeInOut(duration: 0.2)) { showDetails.toggle() }
          }.font(.system(size: 13, weight: .semibold))

          if showDetails { details(q: q, official: official) }
        }
        .padding(16)
      }
      .screen()

      if showCalibration {
        CalibrationOverlay(level: accuracyLevel, en: en,
                           onDone: { withAnimation { showCalibration = false } },
                           onNeverShow: { calibrationIntro = false; withAnimation { showCalibration = false } })
          .transition(.opacity)
      }
    }
    .navigationTitle(t("Kıble Bulucu"))
    .toolbar(showCalibration ? .hidden : .visible, for: .navigationBar)
    .toolbar {
      ToolbarItem(placement: .navigationBarTrailing) {
        Button(en ? "🔄 Calibrate" : "🔄 Kalibre et") { withAnimation { showCalibration = true } }
      }
    }
    .onAppear {
      compass.start()
      if calibrationIntro { showCalibration = true }
    }
    .onDisappear { compass.stop() }
    .onChange(of: aligned) { a in if a { UIImpactFeedbackGenerator(style: .medium).impactOccurred() } }
  }

  private func statusText(turn: Double, aligned: Bool) -> String {
    if !compass.available { return en ? "Your device has no compass" : "Cihazınızda pusula yok" }
    if !compass.ready { return en ? "Starting compass…" : "Pusula hazırlanıyor…" }
    if aligned { return en ? "🕋 YOU ARE FACING THE QIBLA" : "🕋 KIBLEYE DÖNDÜNÜZ" }
    let d = Int(abs(turn).rounded())
    return turn > 0 ? (en ? "Turn right \(d)°  ⟳" : "Sağa doğru \(d)° dönün  ⟳") : (en ? "⟲  Turn left \(d)°" : "⟲  Sola doğru \(d)° dönün")
  }

  private var warning: String? {
    guard compass.ready else { return nil }
    if compass.usingMagnetic { return en ? "📍 Allow location access for true north (≈6° difference)." : "📍 Gerçek kuzey için konum iznini açın (≈6° fark)." }
    if accuracyLevel == 1 { return en ? "🔄 Compass needs calibration: tap \"Calibrate\"." : "🔄 Pusula kalibrasyon istiyor: \"Kalibre et\"e dokunun." }
    if compass.jitter > 8 { return en ? "〰️ Hold the phone still." : "〰️ Telefonu sabit tutun." }
    return nil
  }

  private func details(q: Double, official: Int) -> some View {
    VStack(alignment: .leading, spacing: 8) {
      row(en ? "Qibla angle (official calendar)" : "Kıble açısı (resmî takvim)", "\(official)°", bold: true)
      row(en ? "Qibla angle (true north)" : "Kıble açısı (gerçek kuzey)", String(format: "%.1f°", q))
      if let d = compass.declination {
        row(en ? "Today's hand-compass angle" : "Bugünkü el pusulası açısı", "\(QiblaCalc.diyanetAngle(trueBearing: q, declination: d))°")
        row(en ? "Magnetic declination (today)" : "Manyetik sapma (bugün)", String(format: "%+.1f°", d))
      }
      row(en ? "Distance to the Kaaba" : "Kâbe'ye uzaklık", String(format: "%.0f km", QiblaCalc.distanceKm(coord)))
      row(en ? "Today's qibla time" : "Bugünkü Kıble Saati", qiblaTimeText, bold: true)
      Text(en ? "At the qibla time, facing the sun means facing the qibla. Unlike a compass, the sun is not affected by magnets or metal."
              : "Kıble Saati'nde güneşe dönen kişi kıbleye dönmüş olur. Güneş, pusulanın aksine mıknatıs ve metalden etkilenmez.")
        .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
    }
    .padding(16).frame(maxWidth: .infinity, alignment: .leading).card(radius: 18)
  }

  private func row(_ l: String, _ v: String, bold: Bool = false) -> some View {
    HStack {
      Text(l).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
      Spacer()
      Text(v).font(.system(size: 14, weight: bold ? .bold : .semibold)).foregroundStyle(p.primary)
    }
  }

  private func dial(q: Double, aligned: Bool) -> some View {
    let accent = aligned ? green : gold
    return ZStack {
      ZStack {
        Circle().fill(p.surface)
        Circle().stroke(accent, lineWidth: aligned ? 6 : 3)
        // Yalnızca 30°'lik ana çizgiler
        ForEach(0..<12, id: \.self) { i in
          Capsule().fill(p.onSurfaceVariant.opacity(0.35)).frame(width: 2, height: 10)
            .offset(y: -136).rotationEffect(.degrees(Double(i) * 30))
        }
        ForEach(Array(zip(en ? ["N", "E", "S", "W"] : ["K", "D", "G", "B"], [0.0, 90, 180, 270])), id: \.0) { l, a in
          Text(l).font(.system(size: a == 0 ? 18 : 15, weight: a == 0 ? .bold : .semibold))
            .foregroundStyle(a == 0 ? Color.red : p.onBackground.opacity(0.7))
            .rotationEffect(.degrees(-a + compass.continuousHeading))
            .offset(y: -112).rotationEffect(.degrees(a))
        }
        // Büyük kıble ibresi + Kâbe rozeti
        ZStack {
          QiblaArrow().fill(accent).frame(width: 32, height: 112).offset(y: -50)
          Image("ic_kaaba").resizable().scaledToFit().frame(width: 26, height: 26)
            .padding(6).background(Circle().fill(accent))
            .rotationEffect(.degrees(-q + compass.continuousHeading))
            .offset(y: -140)
        }
        .rotationEffect(.degrees(q))
      }
      .rotationEffect(.degrees(-compass.continuousHeading))
      .animation(.interpolatingSpring(stiffness: 120, damping: 22), value: compass.continuousHeading)
      Circle().fill(accent).frame(width: 14, height: 14)
      Capsule().fill(aligned ? green : .red).frame(width: 5, height: 22).offset(y: -147)
    }
  }
}

/// Ok ucu biçimli kıble ibresi.
private struct QiblaArrow: Shape {
  func path(in r: CGRect) -> Path {
    var path = Path()
    path.move(to: CGPoint(x: r.midX, y: r.minY))
    path.addLine(to: CGPoint(x: r.maxX, y: r.maxY))
    path.addLine(to: CGPoint(x: r.midX, y: r.maxY - r.height * 0.14))
    path.addLine(to: CGPoint(x: r.minX, y: r.maxY))
    path.closeSubpath()
    return path
  }
}

// MARK: - Kalibrasyon anlatımı (telefon havada 8 çizer)

struct CalibrationOverlay: View {
  /// -1 bekleniyor, 1 düşük, 2 orta, 3 hazır
  let level: Int
  let en: Bool
  let onDone: () -> Void
  let onNeverShow: () -> Void
  private let gold = Color(hex: 0xC59B3C), green = Color(hex: 0x2E7D57)

  var body: some View {
    ZStack {
      Color(hex: 0x12110E).ignoresSafeArea()
      VStack(spacing: 16) {
        Text(en ? "Calibrate your compass" : "Pusulayı kalibre edin").font(.system(size: 24, weight: .bold)).foregroundStyle(.white)
        Text(en ? "For an accurate direction, slowly move the phone in a figure-8 two or three times."
                : "Doğru yön için telefonu havada yavaşça 8 çizerek 2–3 kez çevirin.")
          .font(.system(size: 15)).foregroundStyle(Color(hex: 0xE8DFC8)).multilineTextAlignment(.center)
        TimelineView(.animation) { tl in
          let t = tl.date.timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: 3.2) / 3.2
          Canvas { ctx, size in FigureEight.draw(ctx, size: size, progress: t, gold: gold) }
        }
        .frame(width: 300, height: 220)
        VStack(alignment: .leading, spacing: 8) {
          step("1", en ? "Hold the phone in your hand." : "Telefonu avucunuza alın.")
          step("2", en ? "Draw a large 8 in the air, turning your wrist." : "Bileğinizi çevirerek havada büyük bir 8 çizin.")
          step("3", en ? "Keep away from magnetic cases, metal and speakers." : "Mıknatıslı kılıf, metal ve hoparlörden uzak durun.")
        }
        HStack(spacing: 8) {
          Circle().fill(levelColor).frame(width: 10, height: 10)
          Text(levelText).font(.system(size: 15, weight: .semibold)).foregroundStyle(levelColor)
        }
        Button(action: onDone) {
          Text(level >= 3 ? (en ? "Find the qibla" : "Kıbleyi bul") : (en ? "OK" : "Tamam"))
            .font(.system(size: 17, weight: .bold)).foregroundStyle(.white)
            .frame(maxWidth: .infinity, minHeight: 50)
            .background(RoundedRectangle(cornerRadius: 14).fill(level >= 3 ? green : gold))
        }
        Button(action: onNeverShow) {
          Text(en ? "Don't show on start again" : "Açılışta bir daha gösterme").font(.system(size: 13)).foregroundStyle(Color(hex: 0xBDBDBD))
        }
      }
      .padding(24)
    }
  }

  private var levelText: String {
    switch level {
    case 3...: return en ? "✓ Compass ready" : "✓ Pusula hazır"
    case 2: return en ? "Accuracy: medium, keep going" : "Hassasiyet: orta, biraz daha çizin"
    case 0, 1: return en ? "Accuracy: low" : "Hassasiyet: düşük"
    default: return en ? "Waiting for compass…" : "Pusula bekleniyor…"
    }
  }
  private var levelColor: Color {
    switch level { case 3...: return green; case 2: return gold; case 0, 1: return Color(hex: 0xE57373); default: return Color(hex: 0xBDBDBD) }
  }

  private func step(_ n: String, _ text: String) -> some View {
    HStack(spacing: 10) {
      Text(n).font(.system(size: 13, weight: .bold)).foregroundStyle(gold)
        .frame(width: 24, height: 24).background(Circle().fill(gold.opacity(0.2)))
      Text(text).font(.system(size: 14)).foregroundStyle(.white)
    }
  }
}

enum FigureEight {
  /// Bernoulli lemniskatı (sonsuzluk / 8 eğrisi)
  static func point(_ t: Double, center c: CGPoint, width w: Double) -> CGPoint {
    let d = 1 + sin(t) * sin(t)
    return CGPoint(x: c.x + cos(t) / d * w, y: c.y + sin(t) * cos(t) / d * w * 1.9)
  }

  static func draw(_ ctx: GraphicsContext, size: CGSize, progress: Double, gold: Color) {
    let c = CGPoint(x: size.width / 2, y: size.height / 2), w = Double(size.width) * 0.42
    var path = Path()
    for i in 0...120 {
      let pt = point(2 * .pi * Double(i) / 120, center: c, width: w)
      if i == 0 { path.move(to: pt) } else { path.addLine(to: pt) }
    }
    ctx.stroke(path, with: .color(gold.opacity(0.35)), style: StrokeStyle(lineWidth: 3, lineCap: .round, dash: [7, 6]))

    let t = 2 * .pi * progress
    var trail = Path()
    for i in 0...30 {
      let pt = point(t - 1.4 * Double(30 - i) / 30, center: c, width: w)
      if i == 0 { trail.move(to: pt) } else { trail.addLine(to: pt) }
    }
    ctx.stroke(trail, with: .color(gold), style: StrokeStyle(lineWidth: 5, lineCap: .round))

    // Telefon simgesi: yolun yönüne göre hafifçe yatar
    let p = point(t, center: c, width: w), q = point(t + 0.02, center: c, width: w)
    let tilt = atan2(q.y - p.y, q.x - p.x) * 0.35
    var phone = ctx
    phone.translateBy(x: p.x, y: p.y)
    phone.rotate(by: .radians(tilt))
    let body = Path(roundedRect: CGRect(x: -21, y: -37, width: 42, height: 74), cornerRadius: 9)
    phone.fill(body, with: .color(Color(hex: 0x2A2620)))
    phone.stroke(body, with: .color(gold), lineWidth: 2.5)
    var needle = Path()
    needle.move(to: CGPoint(x: 0, y: -16)); needle.addLine(to: CGPoint(x: 6, y: 4)); needle.addLine(to: CGPoint(x: -6, y: 4)); needle.closeSubpath()
    phone.fill(needle, with: .color(.red))
    phone.fill(Path(ellipseIn: CGRect(x: -3, y: 9, width: 6, height: 6)), with: .color(gold))
  }
}
