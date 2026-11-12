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
        Translator.load()
        ready = true
    }
    if (ready) MinikGurmeTheme { MinikGurmeApp() }
}
