import SwiftUI
import CoreLocation
import MapKit
import Combine

/// Yakındaki camiler için konum (kıble pusulası Qibla.swift içindeki QiblaCompass'tadır).
final class LocationHeading: NSObject, ObservableObject, CLLocationManagerDelegate {
  private let manager = CLLocationManager()
  @Published var location: CLLocation?
  @Published var authorized = false

  override init() {
    super.init()
    manager.delegate = self
    manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
  }

  func start(heading: Bool = false) {
    manager.requestWhenInUseAuthorization()
    manager.startUpdatingLocation()
  }

  func stop() { manager.stopUpdatingLocation() }

  func locationManagerDidChangeAuthorization(_ m: CLLocationManager) {
    authorized = m.authorizationStatus == .authorizedWhenInUse || m.authorizationStatus == .authorizedAlways
    if authorized { m.startUpdatingLocation() }
  }
  func locationManager(_ m: CLLocationManager, didUpdateLocations locs: [CLLocation]) { location = locs.last }
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
