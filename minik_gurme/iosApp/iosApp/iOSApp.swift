import SwiftUI
import Shared

/// Minik Gurme iPhone giriş noktası: Kotlin/Compose ortak arayüzünü tam ekran açar.
@main
struct MinikGurmeApp: App {
    init() {
        // Plus aboneliği köprüsü (StoreKit 2); Kotlin tarafı açılışta IosStore.start() ile başlatır
        IosStore.shared.bridge = StoreBridge()
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all) // Compose kendi kenar boşluklarını yönetir
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
