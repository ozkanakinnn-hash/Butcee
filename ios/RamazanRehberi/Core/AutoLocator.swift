import Foundation
import CoreLocation

/// GPS ile otomatik konum: uygulama her öne geldiğinde il/ilçe bulunur; değiştiyse vakitler ve bildirimler güncellenir.
/// Türkiye'de il/ilçe listedeki adlarla eşleştirilir (Diyanet ilçe vakitleri); yurt dışında koordinata göre hesaplanır.
final class AutoLocator: NSObject, ObservableObject, CLLocationManagerDelegate {
  static let shared = AutoLocator()
  private let manager = CLLocationManager()
  private var busy = false
  @Published var lastMessage: String?

  override init() {
    super.init()
    manager.delegate = self
    manager.desiredAccuracy = kCLLocationAccuracyKilometer
  }

  var status: CLAuthorizationStatus { manager.authorizationStatus }
  var authorized: Bool { status == .authorizedWhenInUse || status == .authorizedAlways }

  /// Otomatik konum açıksa: izin yoksa bir kez ister, varsa konumu yeniler.
  func refreshIfEnabled() {
    guard AppData.shared.autoLocation else { return }
    if status == .notDetermined { manager.requestWhenInUseAuthorization(); return }
    guard authorized, !busy else { return }
    busy = true
    manager.requestLocation()
  }

  func locationManagerDidChangeAuthorization(_ m: CLLocationManager) {
    if authorized { refreshIfEnabled() }
    else if m.authorizationStatus == .denied || m.authorizationStatus == .restricted {
      DispatchQueue.main.async { AppData.shared.autoLocation = false }
    }
  }

  func locationManager(_ m: CLLocationManager, didFailWithError error: Error) { busy = false }

  func locationManager(_ m: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.last else { busy = false; return }
    CLGeocoder().reverseGeocodeLocation(loc, preferredLocale: Locale(identifier: "tr_TR")) { places, _ in
      DispatchQueue.main.async {
        self.busy = false
        guard let pl = places?.first else { return }
        self.apply(pl, loc.coordinate)
      }
    }
  }

  static func norm(_ s: String) -> String {
    var r = s.lowercased(with: Locale(identifier: "tr_TR"))
    for (a, b) in [("ç", "c"), ("ğ", "g"), ("ı", "i"), ("ö", "o"), ("ş", "s"), ("ü", "u"), ("â", "a"), ("î", "i"), ("û", "u"),
                   (" ili", ""), (" province", ""), (" merkez", "")] { r = r.replacingOccurrences(of: a, with: b) }
    return String(r.filter { $0.isLetter })
  }

  private func apply(_ pl: CLPlacemark, _ c: CLLocationCoordinate2D) {
    let st = AppData.shared
    var country = "Türkiye", province = "", district = "", matched = false
    if pl.isoCountryCode?.uppercased() == "TR", let admin = pl.administrativeArea,
       let prov = AppContent.shared.provinces.first(where: { Self.norm($0.name) == Self.norm(admin) })
         ?? AppContent.shared.provinces.first(where: { Self.norm(admin).contains(Self.norm($0.name)) }) {
      province = prov.name
      let candidates = [pl.subAdministrativeArea, pl.locality, pl.subLocality].compactMap { $0 }
      district = candidates.lazy.compactMap { cand in prov.districts.first { Self.norm($0) == Self.norm(cand) } }.first
        ?? candidates.first { !$0.isEmpty && Self.norm($0) != Self.norm(prov.name) }
        ?? (prov.districts.contains("Merkez") ? "Merkez" : prov.districts.first ?? prov.name)
      matched = true
    } else {
      let code = pl.isoCountryCode?.uppercased() ?? ""
      country = AppContent.shared.countries.first { $0.code == code }?.name ?? pl.country ?? "Türkiye"
      guard let city = pl.locality ?? pl.subAdministrativeArea ?? pl.administrativeArea else { return }
      province = city
      district = pl.subLocality ?? city
    }
    Shared.defaults.set([c.latitude, c.longitude], forKey: "key_gps_coords")
    if !matched { Shared.defaults.set([c.latitude, c.longitude], forKey: "key_coords_\(province)") }
    let changed = province != st.province || district != st.district || country != st.country || st.useCoordinates != !matched
    guard changed else { return }
    st.applyAutoLocation(country: country, province: province, district: district, useCoordinates: !matched)
    lastMessage = (L10n.isEnglish ? "Location updated: " : "Konum güncellendi: ") + "\(province) / \(district)"
    DispatchQueue.main.asyncAfter(deadline: .now() + 4) { self.lastMessage = nil }
  }
}
