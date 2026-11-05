import SwiftUI
import StoreKit
import GoogleMobileAds
import UserMessagingPlatform

// MARK: - Premium (StoreKit 2): aylık / yıllık abonelik ve ömür boyu

@MainActor
final class PremiumManager: ObservableObject {
  static let shared = PremiumManager()
  static let productIds = ["premium_monthly", "premium_yearly", "premium_lifetime"]

  @Published var products: [Product] = []
  @Published var purchasing = false
  @Published var message: String?
  private var updates: Task<Void, Never>?

  func start() {
    updates?.cancel()
    updates = Task {
      for await result in Transaction.updates {
        if case .verified(let tr) = result { await tr.finish() }
        await refresh()
      }
    }
    Task {
      await refresh()
      products = ((try? await Product.products(for: Self.productIds)) ?? []).sorted { $0.price < $1.price }
    }
  }

  func refresh() async {
    var active = false
    for await result in Transaction.currentEntitlements {
      if case .verified(let tr) = result, Self.productIds.contains(tr.productID), tr.revocationDate == nil { active = true }
    }
    AppData.shared.isPremium = active
  }

  func buy(_ product: Product) async {
    purchasing = true
    defer { purchasing = false }
    do {
      let r = try await product.purchase()
      if case .success(let v) = r, case .verified(let tr) = v {
        await tr.finish()
        await refresh()
        message = t("Teşekkürler! Premium etkin.")
      }
    } catch {
      message = error.localizedDescription
    }
  }

  func restore() async {
    try? await StoreKit.AppStore.sync()
    await refresh()
    message = AppData.shared.isPremium ? t("Premium geri yüklendi.") : t("Geri yüklenecek satın alım bulunamadı.")
  }
}

// MARK: - Reklam (AdMob + UMP onayı)

enum Ads {
  static var started = false
  static var bannerId: String { Bundle.main.object(forInfoDictionaryKey: "AdMobBannerId") as? String ?? "ca-app-pub-3940256099942544/2934735716" }

  static func gatherConsentAndStart() {
    let params = UMPRequestParameters()
    params.tagForUnderAgeOfConsent = false
    UMPConsentInformation.sharedInstance.requestConsentInfoUpdate(with: params) { _ in
      DispatchQueue.main.async {
        if let root = UIApplication.shared.connectedScenes.compactMap({ ($0 as? UIWindowScene)?.keyWindow?.rootViewController }).first {
          UMPConsentForm.loadAndPresentIfRequired(from: root) { _ in startIfAllowed() }
        } else {
          startIfAllowed()
        }
      }
    }
    startIfAllowed()
  }

  private static func startIfAllowed() {
    guard !started, UMPConsentInformation.sharedInstance.canRequestAds else { return }
    started = true
    GADMobileAds.sharedInstance().requestConfiguration.maxAdContentRating = .parentalGuidance
    GADMobileAds.sharedInstance().start(completionHandler: nil)
  }
}

struct BannerAdView: UIViewRepresentable {
  func makeUIView(context: Context) -> GADBannerView {
    let v = GADBannerView(adSize: GADAdSizeBanner)
    v.adUnitID = Ads.bannerId
    v.rootViewController = UIApplication.shared.connectedScenes.compactMap { ($0 as? UIWindowScene)?.keyWindow?.rootViewController }.first
    v.load(GADRequest())
    return v
  }
  func updateUIView(_ uiView: GADBannerView, context: Context) {}
}

/// Premium değilse banner reklam gösterir.
struct AdBanner: View {
  @EnvironmentObject var store: AppData
  var body: some View {
    if !store.isPremium {
      BannerAdView().frame(width: 320, height: 50).frame(maxWidth: .infinity).padding(.vertical, 8)
    }
  }
}
