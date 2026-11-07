import SwiftUI
import CoreLocation
import MapKit
import Combine

final class LocationHeading: NSObject, ObservableObject, CLLocationManagerDelegate {
  private let manager = CLLocationManager()
  @Published var heading: Double = 0           // gerçek kuzeye göre (0–360)
  /// Pusula kadranını döndürmek için kesintisiz açı: 359° → 1° geçişinde kadran ters yönde
  /// tam tur atmasın diye 360'a sarılmaz.
  @Published var continuousHeading: Double = 0
  /// Gerçek kuzey alınamazsa (konum yok) manyetik kuzey kullanılır; ibre o durumda sapma kadar kayar.
  @Published var usingMagnetic = false
  @Published var accuracy: Double = -1
  /// Manyetik sapma (gerçek − manyetik kuzey), pusula verisinden
  @Published var declination: Double?
  @Published var location: CLLocation?
  @Published var authorized = false

  override init() {
    super.init()
    manager.delegate = self
    manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    manager.headingFilter = 1
    manager.headingOrientation = .portrait
  }

  func start(heading: Bool = true) {
    manager.requestWhenInUseAuthorization()
    manager.startUpdatingLocation()
    if heading && CLLocationManager.headingAvailable() { manager.startUpdatingHeading() }
  }

  func stop() { manager.stopUpdatingHeading(); manager.stopUpdatingLocation() }

  func locationManagerDidChangeAuthorization(_ m: CLLocationManager) {
    authorized = m.authorizationStatus == .authorizedWhenInUse || m.authorizationStatus == .authorizedAlways
    if authorized { m.startUpdatingLocation() }
  }
  func locationManager(_ m: CLLocationManager, didUpdateHeading h: CLHeading) {
    let raw = h.trueHeading >= 0 ? h.trueHeading : h.magneticHeading
    usingMagnetic = h.trueHeading < 0
    let delta = ((raw - heading) + 540).truncatingRemainder(dividingBy: 360) - 180
    continuousHeading += delta
    heading = raw
    if h.trueHeading >= 0 { declination = ((h.trueHeading - h.magneticHeading + 540).truncatingRemainder(dividingBy: 360)) - 180 }
    accuracy = h.headingAccuracy
  }
  func locationManager(_ m: CLLocationManager, didUpdateLocations locs: [CLLocation]) { location = locs.last }
  func locationManagerShouldDisplayHeadingCalibration(_ m: CLLocationManager) -> Bool { true }
}

enum QiblaMath {
  static let kaaba = CLLocationCoordinate2D(latitude: 21.4225, longitude: 39.8262)

  static func bearing(from c: CLLocationCoordinate2D) -> Double {
    let la1 = c.latitude * .pi / 180, la2 = kaaba.latitude * .pi / 180
    let dl = (kaaba.longitude - c.longitude) * .pi / 180
    let y = sin(dl) * cos(la2)
    let x = cos(la1) * sin(la2) - sin(la1) * cos(la2) * cos(dl)
    return (atan2(y, x) * 180 / .pi + 360).truncatingRemainder(dividingBy: 360)
  }

  /// Güneşin kıble yönünde olduğu saat ("Kıble saati"), yoksa nil.
  static func qiblaHour(_ c: CLLocationCoordinate2D, bearing q: Double, date: Date = Date()) -> Date? {
    let start = DateUtil.startOfDay(date)
    var best: (Date, Double)?
    for m in stride(from: 300, through: 1260, by: 1) {
      let t = start.addingTimeInterval(Double(m) * 60)
      let (az, alt) = sunAzAlt(c, t)
      guard alt > 2 else { continue }
      let d = abs(((az - q) + 540).truncatingRemainder(dividingBy: 360) - 180)
      if best == nil || d < best!.1 { best = (t, d) }
    }
    if let b = best, b.1 < 1.5 { return b.0 }
    return nil
  }

  static func sunAzAlt(_ c: CLLocationCoordinate2D, _ date: Date) -> (Double, Double) {
    let jd = date.timeIntervalSince1970 / 86400 + 2440587.5
    let d = jd - 2451545
    let g = (357.529 + 0.98560028 * d) * .pi / 180
    let q = 280.459 + 0.98564736 * d
    let l = (q + 1.915 * sin(g) + 0.02 * sin(2 * g)) * .pi / 180
    let e = (23.439 - 0.00000036 * d) * .pi / 180
    let ra = atan2(cos(e) * sin(l), cos(l))
    let dec = asin(sin(e) * sin(l))
    let gmst = (280.46061837 + 360.98564736629 * d).truncatingRemainder(dividingBy: 360)
    let ha = ((gmst + c.longitude) * .pi / 180) - ra
    let lat = c.latitude * .pi / 180
    let alt = asin(sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(ha))
    let az = atan2(-sin(ha), tan(dec) * cos(lat) - sin(lat) * cos(ha))
    return ((az * 180 / .pi + 360).truncatingRemainder(dividingBy: 360), alt * 180 / .pi)
  }
}

// MARK: - Kıble bulucu

struct QiblaView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var prayers: PrayerService
  @StateObject private var loc = LocationHeading()

  var coord: CLLocationCoordinate2D { loc.location?.coordinate ?? prayers.cityCoordinate }

  var body: some View {
    let q = QiblaMath.bearing(from: coord)
    let diff = ((q - loc.heading) + 540).truncatingRemainder(dividingBy: 360) - 180
    let aligned = abs(diff) < 4
    ScrollView {
      VStack(spacing: 14) {
        Text("\(store.locationLabel) • \(t("Kıble")): \(Int(q))° (\(L10n.isEnglish ? "true north" : "gerçek kuzey"))"
             + (loc.declination.map { " • \(Int(((q - $0).truncatingRemainder(dividingBy: 360) + 360).truncatingRemainder(dividingBy: 360)))° (" + (L10n.isEnglish ? "magnetic compass" : "manyetik pusula") + ")" } ?? ""))
          .font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)
        HStack(spacing: 14) {
          Image("ic_kaaba").resizable().scaledToFit().frame(width: 40, height: 40)
          VStack(alignment: .leading, spacing: 4) {
            Text(aligned ? (L10n.isEnglish ? "YOU ARE FACING THE QIBLA" : "KIBLEYE DÖNDÜNÜZ") : (L10n.isEnglish ? "FIND THE QIBLA" : "KIBLE YÖNÜNÜ BULUN"))
              .font(.system(size: 15, weight: .bold)).foregroundStyle(aligned ? Color(hex: 0x4E9A68) : p.onBackground)
            Text(aligned ? "✓" : (L10n.isEnglish ? "Turn \(diff > 0 ? "right" : "left") \(Int(abs(diff)))°" : "Telefonu \(diff > 0 ? "sağa" : "sola") doğru \(Int(abs(diff)))° çevirin"))
              .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          }
          Spacer()
        }.padding(14).card(radius: 18)
        Text(L10n.isEnglish ? "⚠️ Hold the phone flat (parallel to the ground) and away from metal objects." : "⚠️ Telefonu yere paralel (yatay) tutun; metal eşyalardan uzak durun.")
          .font(.system(size: 13)).padding(12).frame(maxWidth: .infinity, alignment: .leading)
          .background(RoundedRectangle(cornerRadius: 14).fill(p.tertiary.opacity(0.12)))
        if loc.usingMagnetic {
          Text(L10n.isEnglish ? "📍 Allow location access for true-north accuracy; the compass is using magnetic north (≈5–6° off in Türkiye)." : "📍 Gerçek kuzey için konum iznini açın; pusula şu an manyetik kuzeyi kullanıyor (Türkiye'de ≈5–6° sapma).")
            .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
        }
        compass(q: q, aligned: aligned)
          .frame(width: 300, height: 300).padding(.vertical, 10)
        if let h = QiblaMath.qiblaHour(coord, bearing: q) {
          let f = h.formatted(date: .omitted, time: .shortened)
          VStack(alignment: .leading, spacing: 6) {
            Text((L10n.isEnglish ? "☀️ Today's Qibla Hour: " : "☀️ Bugünün Kıble Saati: ") + f).font(.system(size: 16, weight: .bold))
            Text(L10n.isEnglish ? "At \(f), when you face the sun you are facing the qibla. The sun is not affected by magnets or metal; use it to check your compass."
                                : "Saat \(f)'de güneşe döndüğünüzde kıbleye dönmüş olursunuz. Güneş mıknatıstan, metalden veya kalibrasyondan etkilenmez; pusulayı bu yöntemle kontrol edebilirsiniz.")
              .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          }.padding(14).frame(maxWidth: .infinity, alignment: .leading).card(radius: 18)
        }
        HStack(spacing: 8) {
          info(L10n.isEnglish ? "Heading" : "Mevcut Yön", "\(Int(loc.heading))°")
          info(L10n.isEnglish ? "Qibla" : "Hedef Kıble", "\(Int(q))°")
          info(L10n.isEnglish ? "Sensor" : "Sensör Durumu", loc.accuracy < 0 ? "—" : (loc.accuracy < 15 ? (L10n.isEnglish ? "High" : "Yüksek") : (L10n.isEnglish ? "Low" : "Düşük")))
        }
      }
      .padding(16)
    }
    .screen()
    .navigationTitle(t("Kıble Bulucu"))
    .onAppear { loc.start() }
    .onDisappear { loc.stop() }
    .onChange(of: aligned) { a in if a { UIImpactFeedbackGenerator(style: .medium).impactOccurred() } }
  }

  private func info(_ t: String, _ v: String) -> some View {
    VStack(spacing: 4) {
      Text(t).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
      Text(v).font(.system(size: 18, weight: .bold)).foregroundStyle(p.primary)
    }.frame(maxWidth: .infinity).padding(12).card(radius: 16)
  }

  private func compass(q: Double, aligned: Bool) -> some View {
    ZStack {
      Circle().stroke(p.primary, lineWidth: 2)
      ForEach(0..<72, id: \.self) { i in
        Rectangle().fill(p.onSurfaceVariant.opacity(i % 6 == 0 ? 0.9 : 0.4)).frame(width: 1.5, height: i % 6 == 0 ? 12 : 6)
          .offset(y: -140).rotationEffect(.degrees(Double(i) * 5))
      }
      ForEach(Array(zip(["K", "D", "G", "B"], [0.0, 90, 180, 270])), id: \.0) { l, a in
        Text(L10n.isEnglish ? ["K": "N", "D": "E", "G": "S", "B": "W"][l]! : l)
          .font(.system(size: 16, weight: .bold)).foregroundStyle(l == "K" ? .red : p.onBackground)
          .rotationEffect(.degrees(-a + loc.continuousHeading))
          .offset(y: -112).rotationEffect(.degrees(a))
      }
      // Kıble ibresi
      ZStack {
        Capsule().fill(LinearGradient(colors: [p.primary.opacity(0.2), p.primary], startPoint: .bottom, endPoint: .top)).frame(width: 5, height: 110).offset(y: -55)
        Image("ic_kaaba").resizable().scaledToFit().frame(width: 30, height: 30)
          .padding(6).background(Circle().fill(aligned ? Color(hex: 0x4E9A68) : p.primary.opacity(0.85))).offset(y: -125)
      }
      .rotationEffect(.degrees(q))
      Circle().fill(p.background).frame(width: 18, height: 18).overlay(Circle().stroke(p.primary, lineWidth: 3))
    }
    .rotationEffect(.degrees(-loc.continuousHeading))
    .animation(.easeOut(duration: 0.25), value: loc.continuousHeading)
  }
}

// MARK: - Yakındaki camiler

struct MosqueItem: Identifiable {
  let id = UUID()
  let item: MKMapItem
  let distance: Double
}

struct NearbyMosquesView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var prayers: PrayerService
  @StateObject private var loc = LocationHeading()
  @State private var mosques: [MosqueItem] = []
  @State private var region = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 41.01, longitude: 28.98), span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05))
  @State private var searching = false
  @State private var searchedOnce = false

  var body: some View {
    VStack(spacing: 0) {
      Map(coordinateRegion: $region, showsUserLocation: true, annotationItems: mosques) { m in
        MapAnnotation(coordinate: m.item.placemark.coordinate) {
          Image(systemName: "building.columns.circle.fill").font(.system(size: 26)).foregroundStyle(p.primary).background(Circle().fill(.white))
        }
      }
      .frame(height: 300)
      List {
        if searching { HStack { ProgressView(); Text(L10n.isEnglish ? "Searching mosques…" : "Camiler aranıyor…") } }
        if !searching && searchedOnce && mosques.isEmpty { Text(L10n.isEnglish ? "No mosques found nearby." : "Yakında cami bulunamadı.") }
        ForEach(mosques) { m in
          HStack {
            VStack(alignment: .leading, spacing: 3) {
              Text(m.item.name ?? "Cami").font(.system(size: 15, weight: .semibold))
              Text(m.item.placemark.title ?? "").font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant).lineLimit(1)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 4) {
              Text(m.distance < 1000 ? "\(Int(m.distance)) m" : String(format: "%.1f km", m.distance / 1000)).font(.system(size: 13, weight: .bold)).foregroundStyle(p.primary)
              Button(L10n.isEnglish ? "Directions" : "Yol tarifi") {
                m.item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeWalking])
              }.font(.system(size: 12, weight: .semibold)).buttonStyle(.bordered)
            }
          }
        }
      }
      .listStyle(.plain)
      .scrollContentBackground(.hidden)
    }
    .screen()
    .navigationTitle(t("Yakındaki Camiler"))
    .onAppear {
      loc.start(heading: false)
      DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { if !searchedOnce { search(around: loc.location?.coordinate ?? prayers.cityCoordinate) } }
    }
    .onReceive(loc.$location.compactMap { $0 }.first()) { l in search(around: l.coordinate) }
    .onDisappear { loc.stop() }
  }

  private func search(around c: CLLocationCoordinate2D) {
    searching = true; searchedOnce = true
    region = MKCoordinateRegion(center: c, span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04))
    let group = DispatchGroup()
    var found: [MKMapItem] = []
    for q in ["cami", "mosque", "mescit"] {
      let req = MKLocalSearch.Request()
      req.naturalLanguageQuery = q
      req.region = MKCoordinateRegion(center: c, latitudinalMeters: 5000, longitudinalMeters: 5000)
      group.enter()
      MKLocalSearch(request: req).start { resp, _ in
        DispatchQueue.main.async { found += resp?.mapItems ?? []; group.leave() }
      }
    }
    group.notify(queue: .main) {
      let here = CLLocation(latitude: c.latitude, longitude: c.longitude)
      var seen = Set<String>()
      mosques = found.compactMap { it -> MosqueItem? in
        let key = "\(it.name ?? "")_\(Int(it.placemark.coordinate.latitude * 1e4))"
        guard !seen.contains(key) else { return nil }
        seen.insert(key)
        let d = here.distance(from: CLLocation(latitude: it.placemark.coordinate.latitude, longitude: it.placemark.coordinate.longitude))
        return MosqueItem(item: it, distance: d)
      }.sorted { $0.distance < $1.distance }.prefix(30).map { $0 }
      searching = false
    }
  }
}
