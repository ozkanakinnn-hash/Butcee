// App Review ekran kaydı için akış (simülatör). Kayıt: .github/workflows/minik-gurme-ios-review-video.yml
// Gerçek kullanıcı içeriğini etkilememek için şikâyet/engelleme pencereleri gösterilir ve "Vazgeç" ile kapatılır;
// akışta yazılan test gönderisi, sonda topluluk hesabı silinerek kaldırılır.
import XCTest

final class ReviewFlowUITests: XCTestCase {
    let app = XCUIApplication()

    private func pause(_ s: Double) { Thread.sleep(forTimeInterval: s) }

    private func el(_ id: String) -> XCUIElement { app.descendants(matching: .any)[id] }

    private func prefix(_ p: String) -> XCUIElement {
        app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH %@", p)).firstMatch
    }

    @discardableResult
    private func tap(_ e: XCUIElement, _ name: String, wait: Double = 2.5) -> Bool {
        if e.waitForExistence(timeout: 10) {
            e.tap(); print("REVIEW ✔ \(name)"); pause(wait); return true
        }
        print("REVIEW ✘ \(name) bulunamadı"); return false
    }

    private func cancelDialog() {
        let c = app.descendants(matching: .any).matching(NSPredicate(format: "label IN %@", ["Cancel", "Vazgeç"])).firstMatch
        tap(c, "Vazgeç", wait: 2)
    }

    /// Simülatörün "kaydırarak yazma" tanıtım ekranı klavyeyi kapatır; varsa "Continue" ile geçilir.
    private func dismissKeyboardTip() {
        let c = app.buttons["Continue"]
        if c.waitForExistence(timeout: 2) { c.tap(); print("REVIEW ✔ keyboard tip"); pause(1) }
    }

    func testReviewFlow() {
        continueAfterFailure = true
        app.launchArguments = ["-mgDemo", "1", "-mgLang", "en"]
        app.launch()
        pause(5)

        // Bugün → 100 Gıda → Tarifler → Rehber
        app.swipeUp(); pause(2); app.swipeDown(); pause(2)
        tap(el("nav_foods100"), "100 foods"); app.swipeUp(); pause(2); app.swipeDown(); pause(1.5)
        tap(el("nav_recipes"), "recipes"); app.swipeUp(); pause(2); app.swipeDown(); pause(1.5)
        tap(el("nav_guide"), "guide"); app.swipeUp(); pause(2); app.swipeDown(); pause(1.5)

        // Topluluk: katıl (takma ad + onay)
        tap(el("nav_community"), "community", wait: 4)
        if tap(el("community_open_join"), "join banner") {
            let nick = el("community_nick")
            if nick.waitForExistence(timeout: 8) { nick.tap(); pause(1.5); dismissKeyboardTip(); nick.typeText("AppReview"); pause(1.5) }
            dismissKeyboardTip()
            tap(el("community_accept"), "accept rules")
            tap(el("community_join"), "join", wait: 5)
        }

        // Kendi test gönderisi
        if tap(el("community_new_post"), "new post") {
            let input = el("community_post_input")
            if input.waitForExistence(timeout: 8) {
                input.tap(); pause(1.5); dismissKeyboardTip(); input.typeText("Test post from App Review demo. Our baby tried avocado today!"); pause(2)
                app.swipeUp(); pause(1)
                tap(el("community_post_send"), "send post", wait: 4)
            }
        }

        // Şikâyet ve engelleme (başka bir gönderide; pencereler gösterilip vazgeçilir)
        let other = el("menu_post_demo-sample")
        for _ in 0..<3 where !other.isHittable { app.swipeUp(); pause(1) }
        tap(other, "post menu", wait: 1.5)
        if tap(el("menu_report"), "report", wait: 3) { cancelDialog() }
        tap(other, "post menu", wait: 1.5)
        if tap(el("menu_block"), "block", wait: 3) { cancelDialog() }

        // Gönderi ayrıntısı ve yorumlar
        for _ in 0..<3 { app.swipeDown(); pause(0.8) }
        tap(prefix("community_post_"), "open post", wait: 3)
        tap(el("community_back"), "back", wait: 2)

        // Ayarlar: engellenenler ve topluluk hesabını silme
        tap(el("top_settings"), "settings", wait: 2.5)
        for _ in 0..<4 { app.swipeUp(); pause(1.2) }
        if tap(el("settings_delete_community"), "delete community account", wait: 3) {
            tap(el("settings_confirm"), "confirm delete", wait: 4)
        }
        app.swipeDown(); pause(2)
    }
}
