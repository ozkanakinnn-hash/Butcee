package akinai.minikgurme.shared

/** Uygulamanın çalıştığı platform ("android" | "ios"). */
expect val platformName: String

/** Mağaza adlarını platforma göre uyarlar (iOS: App Store ve Apple abonelik yolu). Çeviriden SONRA çağrılır. */
fun String.forStore(): String = if (platformName != "ios") this else this
    .replace("Google Play › Abonelikler", "Ayarlar › Apple Kimliği › Abonelikler")
    .replace("Google Play › Subscriptions", "Settings › Apple ID › Subscriptions")
    .replace("Google Play hesabınızdan", "Apple Kimliğinizden")
    .replace("your Google Play account", "your Apple ID")
    .replace("Google Play Billing", "App Store")
    .replace("Google Play", "App Store")
