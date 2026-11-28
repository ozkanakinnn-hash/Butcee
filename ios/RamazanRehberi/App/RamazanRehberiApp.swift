import SwiftUI
import BackgroundTasks
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
    UNUserNotificationCenter.current().delegate = self
    NotificationScheduler.registerCategories()
    BGTaskScheduler.shared.register(forTaskWithIdentifier: "akin.ai2.refresh", using: nil) { task in
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
    let req = BGAppRefreshTaskRequest(identifier: "akin.ai2.refresh")
    req.earliestBeginDate = Date(timeIntervalSinceNow: 6 * 3600)
    try? BGTaskScheduler.shared.submit(req)
  }

  // Bildirim eylemleri: "Kıldım" / "20 dk sonra hatırlat"
  func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse,
                              withCompletionHandler completionHandler: @escaping () -> Void) {
    let info = response.notification.request.content.userInfo
    let action = response.actionIdentifier
    DispatchQueue.main.async {
      NotificationScheduler.handle(action: action, userInfo: info)
      completionHandler()
    }
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
          AutoLocator.shared.refreshIfEnabled()
          prayers.reload()
          #if DEBUG
          if UserDefaults.standard.bool(forKey: "debug_seed_log") {
            PrayerLog.seedSample()
            if store.kazaTotalRemaining == 0 { store.kazaAdd(.sabah, 14); store.kazaAdd(.yatsi, 9); store.kazaAdd(.ikindi, 6) }
          }
          #endif
          store.applyAutoKaza()
          premium.start()
          #if DEBUG
          let skipAds = UserDefaults.standard.bool(forKey: "debug_no_ads")
          #else
          let skipAds = false
          #endif
          if !skipAds { Ads.gatherConsentAndStart() }
          AppDelegate.scheduleRefresh()
        }
    }
    .onChange(of: phase) { p in
      if p == .active {
        AutoLocator.shared.refreshIfEnabled()
        prayers.reload()
        store.applyAutoKaza()
        store.prayerLogVersion += 1  // widget/Siri'den gelen işaretleri göster
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
