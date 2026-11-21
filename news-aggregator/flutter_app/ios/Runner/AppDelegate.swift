import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate, FlutterImplicitEngineDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  func didInitializeImplicitFlutterEngine(_ engineBridge: FlutterImplicitEngineBridge) {
    GeneratedPluginRegistrant.register(with: engineBridge.pluginRegistry)
  }

  // Bildirim tanılama: Apple'ın push kaydı cevabı (shared_preferences "flutter." önekiyle okur).
  // Asıl işlem değişmez: jeton super ile Firebase eklentisine iletilir.
  override func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
  ) {
    UserDefaults.standard.set("kayıt başarılı (\(deviceToken.count) bayt) \(Date())", forKey: "flutter.apns_status")
    super.application(application, didRegisterForRemoteNotificationsWithDeviceToken: deviceToken)
  }

  override func application(
    _ application: UIApplication,
    didFailToRegisterForRemoteNotificationsWithError error: Error
  ) {
    UserDefaults.standard.set("HATA: \(error.localizedDescription) \(Date())", forKey: "flutter.apns_status")
    super.application(application, didFailToRegisterForRemoteNotificationsWithError: error)
  }
}
