package akinai.minikgurme.ui

/**
 * Yalnızca otomatik ekran görüntüsü/duman testleri için başlatma kancaları (normal kullanımda hepsi boş).
 * iOS: `xcrun simctl launch <udid> akinai.minikgurme -mgDemo 1 -mgTab foods100 -mgLang en`
 * (başlatma argümanları NSUserDefaults argüman alanına düşer, kalıcı değildir).
 */
object TestHooks {
    /** Doluysa ilk açılış formu bu örnek profille otomatik tamamlanır. */
    var demoProfile: Boolean = false
    /** -mgDemo 2: App Store ekran görüntüsü modu — örnek kayıtlar ve örnek topluluk gönderileri. */
    var screenshotMode: Boolean = false
    /** Açılışta gösterilecek sekme: today, foods100, recipes, guide, community, settings, profile ... */
    var startTab: String? = null
}
