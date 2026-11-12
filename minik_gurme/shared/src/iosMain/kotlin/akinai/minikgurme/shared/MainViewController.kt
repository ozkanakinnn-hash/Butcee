package akinai.minikgurme.shared

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import akinai.minikgurme.community.Community
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Translator
import akinai.minikgurme.platform.IosPlatformServices
import akinai.minikgurme.platform.Platform
import akinai.minikgurme.ui.MinikGurmeApp
import akinai.minikgurme.ui.theme.MinikGurmeTheme
import akinai.minikgurme.ui.theme.ThemePrefs

/** iPhone uygulamasının giriş noktası (Swift tarafı `MainViewControllerKt.MainViewController()` çağırır). */
fun MainViewController() = ComposeUIViewController {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Platform.services = IosPlatformServices()
        AppLanguage.init()
        ThemePrefs.init()
        Community.init()
        // Topluluk: Firebase REST (Android ile aynı proje ve kurallar; iOS anahtarı paket kimliğine kısıtlı olabilir)
        if (Community.backend === Community.Unconfigured) Community.backend = akinai.minikgurme.community.RestCommunityBackend(
            apiKey = "AIzaSyAJmShvVyEXC1HQgdV-6cnB7q79SeudmCU",
            projectId = "minik-gurme-f0db0",
            http = io.ktor.client.HttpClient(io.ktor.client.engine.darwin.Darwin),
            extraHeaders = mapOf("X-Ios-Bundle-Identifier" to "akinai.minikgurme")
        )
        Translator.load()
        akinai.minikgurme.reminders.IosReminders.install()
        akinai.minikgurme.reminders.IosReminders.reschedule()
        ready = true
    }
    if (ready) MinikGurmeTheme {
        MinikGurmeApp()
        akinai.minikgurme.ui.platform.InAppMessageHost()
    }
}
