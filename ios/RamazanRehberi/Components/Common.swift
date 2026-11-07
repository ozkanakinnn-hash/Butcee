import SwiftUI

/// Paketteki webp/png görselini yükler.
enum BundleImage {
  private static var cache = NSCache<NSString, UIImage>()
  static func ui(_ name: String) -> UIImage? {
    if let c = cache.object(forKey: name as NSString) { return c }
    for ext in ["webp", "png", "jpg"] {
      if let path = Bundle.main.path(forResource: name, ofType: ext), let img = UIImage(contentsOfFile: path) {
        cache.setObject(img, forKey: name as NSString)
        return img
      }
    }
    return nil
  }
  static func image(_ name: String) -> Image {
    if let u = ui(name) { return Image(uiImage: u) }
    return Image(systemName: "photo")
  }
}

struct SectionTitle: View {
  @Environment(\.palette) var p
  let icon: String
  let title: String
  var trailing: AnyView? = nil
  var body: some View {
    HStack(spacing: 10) {
      Image(systemName: icon).foregroundStyle(p.primary).font(.system(size: 18, weight: .semibold))
      Text(title).font(.system(size: 19, weight: .bold)).foregroundStyle(p.onBackground)
      Spacer()
      if let trailing { trailing }
    }
  }
}

/// Üç sekmeli seçim kutusu (Ayet/Hadis/Dua, Arapça/Okunuş/Meal...).
struct PillTabs: View {
  @Environment(\.palette) var p
  let items: [String]
  @Binding var selection: Int
  var body: some View {
    HStack(spacing: 4) {
      ForEach(items.indices, id: \.self) { i in
        Button { withAnimation(.easeInOut(duration: 0.2)) { selection = i } } label: {
          Text(items[i])
            .font(.system(size: 14, weight: .semibold))
            .frame(maxWidth: .infinity).padding(.vertical, 11)
            .foregroundStyle(selection == i ? p.onPrimary : p.onBackground.opacity(0.85))
            .background(RoundedRectangle(cornerRadius: 12).fill(selection == i ? p.primary : Color.clear))
        }.buttonStyle(.plain)
      }
    }
    .padding(4)
    .background(RoundedRectangle(cornerRadius: 16).fill(p.surfaceVariant.opacity(p.dark ? 1 : 0.7)))
  }
}

struct ScreenHeader: View {
  @Environment(\.palette) var p
  let title: String
  var subtitle: String? = nil
  var body: some View {
    VStack(alignment: .leading, spacing: 4) {
      Text(title).font(.system(size: 26, weight: .bold)).foregroundStyle(p.onBackground)
      if let subtitle { Text(subtitle).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant) }
    }
    .frame(maxWidth: .infinity, alignment: .leading)
  }
}

/// Paylaşım menüsü (UIActivityViewController).
enum ShareHelper {
  static func share(items: [Any]) {
    guard let root = UIApplication.shared.connectedScenes.compactMap({ ($0 as? UIWindowScene)?.keyWindow?.rootViewController }).first else { return }
    var top = root
    while let presented = top.presentedViewController { top = presented }
    let vc = UIActivityViewController(activityItems: items, applicationActivities: nil)
    vc.popoverPresentationController?.sourceView = top.view
    top.present(vc, animated: true)
  }
}

struct PrivacyFooter: View {
  @Environment(\.palette) var p
  var body: some View {
    VStack(spacing: 8) {
      Label(t("Gizlilik & Güvenlik Güvencesi"), systemImage: "lock.fill")
        .font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)
      Text(t("🔒 Notlarınız ve kayıtlarınız cihazınızda kalır; hesap gerekmez. Reklam ve bildirim hizmetleri için Gizlilik Politikası'na bakın."))
        .font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center)
      Link(t("Gizlilik Politikası") + " ›", destination: URL(string: "https://ozkanakinnn-hash.github.io/Butcee/privacy-policy.html")!)
        .font(.system(size: 12, weight: .semibold)).foregroundStyle(p.primary)
    }
    .padding(.horizontal, 24).padding(.vertical, 20)
  }
}

/// Saniyede bir yenilenen zaman (geri sayımlar için).
final class Ticker: ObservableObject {
  @Published var now = Date()
  private var timer: Timer?
  init() {
    timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in self?.now = Date() }
  }
  deinit { timer?.invalidate() }
}
