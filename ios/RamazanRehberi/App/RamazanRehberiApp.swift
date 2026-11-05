import SwiftUI
import BackgroundTasks
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
    UNUserNotificationCenter.current().delegate = self
    BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.ozkanakin.ramazanrehberi.refresh", using: nil) { task in
      // Arka planda vakitleri tazele ve önümüzdeki günlerin bildirimlerini yeniden planla
      DispatchQueue.main.async {
        PrayerService.shared.reload()
        AppDelegate.scheduleRefresh()
        DispatchQueue.main.asyncAfter(deadline: .now() + 10) { task.setTaskCompleted(success: true) }
      }
    }
    return true
  }

  static func scheduleRefresh() {
    let req = BGAppRefreshTaskRequest(identifier: "com.ozkanakin.ramazanrehberi.refresh")
    req.earliestBeginDate = Date(timeIntervalSinceNow: 6 * 3600)
    try? BGTaskScheduler.shared.submit(req)
  }

  // Uygulama açıkken de bildirimi göster
  func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification,
                              withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
    completionHandler([.banner, .sound, .list])
  }
}

@main
struct RamazanRehberiApp: App {
  @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
  @StateObject private var store = AppData.shared
  @StateObject private var prayers = PrayerService.shared
  @StateObject private var premium = PremiumManager.shared
  @Environment(\.scenePhase) private var phase

  init() {
    QuranText.shared.preload()
  }

  var body: some Scene {
    WindowGroup {
      ThemedRoot()
        .environmentObject(store)
        .environmentObject(prayers)
        .environmentObject(premium)
        .onAppear {
          prayers.reload()
          premium.start()
          Ads.gatherConsentAndStart()
          AppDelegate.scheduleRefresh()
        }
    }
    .onChange(of: phase) { p in
      if p == .active {
        prayers.reload()
        Task { await premium.refresh() }
      }
    }
  }
}

/// Seçilen tema (açık/koyu/sistem) ve renk paletini uygular; dil değişince arayüzü yeniden kurar.
struct ThemedRoot: View {
  @EnvironmentObject var store: AppData
  @Environment(\.colorScheme) var system

  var isDark: Bool {
    switch store.themeMode {
    case .DARK: return true
    case .LIGHT: return false
    case .SYSTEM: return system == .dark
    }
  }

  var body: some View {
    let palette = Palette.of(store.colorTheme, dark: isDark)
    Group {
      if store.onboardingCompleted {
        RootView()
      } else {
        OnboardingView()
      }
    }
    .id(store.language)
    .environment(\.palette, palette)
    .tint(palette.primary)
    .preferredColorScheme(store.themeMode == .SYSTEM ? nil : (isDark ? .dark : .light))
  }
}
