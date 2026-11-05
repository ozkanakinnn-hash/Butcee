import SwiftUI
import StoreKit
import UserNotifications

private func tr(_ a: String, _ b: String) -> String { L10n.isEnglish ? b : a }

// MARK: - Bildirim sihirbazı (iOS)

struct NotificationWizardView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var status: UNAuthorizationStatus = .notDetermined
  @State private var pending = 0
  @State private var testSent = false

  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 12) {
        Text(tr("Ezan ve iftar bildirimlerinin vaktinde gelmesi için adımları kontrol edin.", "Check these steps so adhan and iftar notifications arrive on time."))
          .font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
        step(1, tr("Bildirim izni", "Notification permission"),
             status == .authorized || status == .provisional ? tr("İzin verildi ✓", "Allowed ✓") : tr("İzin verilmedi", "Not allowed"),
             ok: status == .authorized || status == .provisional,
             action: status == .denied ? tr("Ayarları Aç", "Open Settings") : tr("İzin ver", "Allow")) {
          if status == .denied { openSettings() } else { NotificationScheduler.requestPermission { _ in refresh() } }
        }
        step(2, tr("Uygulama bildirimleri", "App notifications"),
             store.notificationsEnabled ? tr("Açık ✓", "On ✓") : tr("Kapalı", "Off"), ok: store.notificationsEnabled,
             action: store.notificationsEnabled ? nil : tr("Aç", "Turn on")) { store.notificationsEnabled = true; refresh() }
        step(3, tr("Planlanan bildirimler", "Scheduled notifications"),
             tr("Önümüzdeki günler için \(pending) bildirim hazır", "\(pending) notifications ready for the coming days"), ok: pending > 0,
             action: tr("Yenile", "Refresh")) { NotificationScheduler.rescheduleAll(); DispatchQueue.main.asyncAfter(deadline: .now() + 1) { refresh() } }
        step(4, tr("Odak / Rahatsız Etme", "Focus / Do Not Disturb"),
             tr("Odak modu açıksa Ramazan Rehberi'ni izin verilen uygulamalara ekleyin.", "If a Focus mode is on, add Ramazan Rehberi to allowed apps."), ok: nil,
             action: tr("Ayarları Aç", "Open Settings")) { openSettings() }
        step(5, tr("Uygulamayı ara sıra açın", "Open the app now and then"),
             tr("iOS bildirimleri önceden planlar; uygulamayı haftada bir açmak vakitleri tazeler.", "iOS schedules notifications in advance; opening the app weekly refreshes the times."), ok: nil, action: nil) {}
        Button {
          NotificationScheduler.sendTest(); testSent = true
        } label: {
          Text(testSent ? tr("✅ 10 saniye içinde test bildirimi gelecek", "✅ A test notification will arrive in 10 seconds") : tr("🔔 Bildirimi Test Et", "🔔 Test Notification"))
            .font(.system(size: 15, weight: .bold)).foregroundStyle(p.onPrimary).frame(maxWidth: .infinity).padding(15)
            .background(RoundedRectangle(cornerRadius: 16).fill(p.primary))
        }.buttonStyle(.plain).padding(.top, 6)
        Text(tr("Testten sonra uygulamayı kapatıp telefonu kilitleyin; bildirim ezan sesiyle gelmeli.", "After tapping, close the app and lock the phone; the notification should arrive with the adhan sound."))
          .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
      }.padding(16)
    }
    .screen()
    .navigationTitle(t("Bildirim Sihirbazı"))
    .onAppear { refresh() }
  }

  private func refresh() {
    NotificationScheduler.authorizationStatus { status = $0 }
    UNUserNotificationCenter.current().getPendingNotificationRequests { r in DispatchQueue.main.async { pending = r.count } }
  }

  private func openSettings() { if let u = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(u) } }

  private func step(_ n: Int, _ title: String, _ detail: String, ok: Bool?, action: String?, perform: @escaping () -> Void) -> some View {
    HStack(alignment: .top, spacing: 12) {
      Text(ok == true ? "✓" : "\(n)").font(.system(size: 14, weight: .bold)).foregroundStyle(p.onPrimary)
        .frame(width: 28, height: 28).background(Circle().fill(ok == true ? Color(hex: 0x4E9A68) : (ok == false ? p.tertiary : p.primary)))
      VStack(alignment: .leading, spacing: 4) {
        Text(title).font(.system(size: 15, weight: .bold))
        Text(detail).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
        if let action {
          Button(action, action: perform).font(.system(size: 13, weight: .semibold)).buttonStyle(.bordered).padding(.top, 2)
        }
      }
      Spacer()
    }
    .padding(14).card(radius: 18)
  }
}

// MARK: - Premium

struct PremiumView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var premium: PremiumManager

  var body: some View {
    ScrollView {
      VStack(spacing: 16) {
        Text("⭐").font(.system(size: 60))
        Text(tr("Reklamsız Premium", "Ad-free Premium")).font(AppFont.playfair(28))
        Text(tr("Reklamları kaldırın ve uygulamanın gelişimine destek olun.", "Remove ads and support the app's development."))
          .font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center)
        VStack(alignment: .leading, spacing: 10) {
          feature("🚫", tr("Tüm reklamlar kaldırılır", "All ads removed"))
          feature("💛", tr("Geliştiriciye destek olursunuz", "You support the developer"))
          feature("✨", tr("Gelecek özelliklere öncelikli erişim", "Early access to new features"))
        }.padding(16).frame(maxWidth: .infinity, alignment: .leading).card()
        if store.isPremium {
          Text(tr("✅ Premium etkin. Teşekkürler!", "✅ Premium is active. Thank you!")).font(.system(size: 16, weight: .bold)).foregroundStyle(Color(hex: 0x4E9A68))
        } else if premium.products.isEmpty {
          Text(tr("Ürünler App Store'dan yükleniyor… (Uygulama App Store'da yayınlandığında satın alma etkinleşir.)", "Loading products from the App Store… (Purchases activate once the app is published.)"))
            .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center)
        } else {
          ForEach(premium.products, id: \.id) { prod in
            Button { Task { await premium.buy(prod) } } label: {
              HStack {
                VStack(alignment: .leading, spacing: 2) {
                  Text(prod.displayName).font(.system(size: 16, weight: .bold))
                  Text(prod.description).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
                }
                Spacer()
                Text(prod.displayPrice).font(.system(size: 16, weight: .bold)).foregroundStyle(p.primary)
              }
              .padding(16).card(radius: 18)
            }.buttonStyle(.plain).disabled(premium.purchasing)
          }
        }
        Button(t("Satın Alımları Geri Yükle")) { Task { await premium.restore() } }.font(.system(size: 14, weight: .semibold))
        if let m = premium.message { Text(m).font(.system(size: 13)).foregroundStyle(p.primary) }
        Text(tr("Abonelikler, mevcut dönem bitmeden en az 24 saat önce iptal edilmezse otomatik yenilenir. Apple Kimliği ayarlarından yönetebilirsiniz.",
                "Subscriptions renew automatically unless cancelled at least 24 hours before the end of the period. Manage them in your Apple ID settings."))
          .font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center)
      }.padding(20)
    }
    .screen()
    .navigationTitle("Premium")
  }

  private func feature(_ e: String, _ s: String) -> some View { HStack(spacing: 10) { Text(e); Text(s).font(.system(size: 15)) } }
}
