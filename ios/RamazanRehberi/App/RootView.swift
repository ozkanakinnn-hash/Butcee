import SwiftUI

/// Alt alt sekmelerden açılan alt ekranlar.
enum Route: Hashable {
  case qibla, mosques, zikirmatik, esma, kaza, kandil, hatim, notificationWizard, premium
  case greetingCards, ramadanGuide, notes, settings, widgets
  case quranList(tab: Int)
  case quranReader(page: Int)
  case surah(Int)
}

struct RootView: View {
  @EnvironmentObject var store: AppData
  @Environment(\.palette) var p
  // Test/ekran görüntüsü için başlatma argümanları: -debug_start_tab N, -debug_route qibla
  #if DEBUG
  @State private var tab = UserDefaults.standard.integer(forKey: "debug_start_tab")
  #else
  @State private var tab = 0
  #endif
  @State private var debugPath: [Route] = RootView.debugRoute()

  static func debugRoute() -> [Route] {
    #if !DEBUG
    return []  // Yayın sürümünde test argümanları devre dışı
    #endif
    switch UserDefaults.standard.string(forKey: "debug_route") ?? "" {
    case "qibla": return [.qibla]
    case "zikirmatik": return [.zikirmatik]
    case "esma": return [.esma]
    case "kaza": return [.kaza]
    case "kandil": return [.kandil]
    case "hatim": return [.hatim]
    case "wizard": return [.notificationWizard]
    case "premium": return [.premium]
    case "cards": return [.greetingCards]
    case "guide": return [.ramadanGuide]
    case "settings": return [.settings]
    case "reader": return [.quranReader(page: 2)]
    case "mosques": return [.mosques]
    default: return []
    }
  }

  var isRamadan: Bool { RamadanCalendar.isRamadanDay(date: Date()) }

  var body: some View {
    TabView(selection: $tab) {
      NavigationStack(path: $debugPath) { HomeView(selectTab: { tab = $0 }).withRoutes() }
        .tabItem { Label(t("Ana Sayfa"), systemImage: "house.fill") }.tag(0)
      NavigationStack {
        Group { if isRamadan { RamadanGuideView() } else { QuranListView(initialTab: 0) } }.withRoutes()
      }
      .tabItem {
        if isRamadan { Label(t("Ramazan Rehberi"), image: "ic_ramadan_guide") } else { Label(t("Kur'an-ı Kerim"), image: "ic_quran_tab") }
      }.tag(1)
      NavigationStack { FastingCalendarView().withRoutes() }
        .tabItem { Label(t("Takvim"), systemImage: "calendar") }.tag(2)
      NavigationStack { SpiritualView().withRoutes() }
        .tabItem { Label(t("Ayet & Dua"), systemImage: "book") }.tag(3)
      NavigationStack { MoreHubView().withRoutes() }
        .tabItem { Label(t("Dahası"), systemImage: "ellipsis") }.tag(4)
    }
    .onAppear { styleTabBar() }
  }

  private func styleTabBar() {
    let a = UITabBarAppearance()
    a.configureWithOpaqueBackground()
    a.backgroundColor = UIColor(p.surface)
    UITabBar.appearance().standardAppearance = a
    UITabBar.appearance().scrollEdgeAppearance = a
  }
}

extension View {
  func withRoutes() -> some View {
    navigationDestination(for: Route.self) { r in
      Group {
        switch r {
        case .qibla: QiblaView()
        case .mosques: NearbyMosquesView()
        case .zikirmatik: ZikirmatikView()
        case .esma: EsmaView()
        case .kaza: KazaView()
        case .kandil: KandilView()
        case .hatim: QuranListView(initialTab: 1)
        case .notificationWizard: NotificationWizardView()
        case .premium: PremiumView()
        case .greetingCards: GreetingCardsView()
        case .ramadanGuide: RamadanGuideView()
        case .notes: NotesView()
        case .settings: SettingsView()
        case .widgets: WidgetsInfoView()
        case .quranList(let tab): QuranListView(initialTab: tab)
        case .quranReader(let page): QuranReaderView(startPage: page)
        case .surah(let n): QuranReaderView(startPage: QuranText.shared.firstPage(ofSurah: n))
        }
      }
    }
  }
}

/// Ekran arka planı + başlık stili.
struct ScreenBackground: ViewModifier {
  @Environment(\.palette) var p
  func body(content: Content) -> some View {
    content.background(p.background.ignoresSafeArea()).foregroundStyle(p.onBackground)
  }
}

extension View {
  func screen() -> some View { modifier(ScreenBackground()) }
}
