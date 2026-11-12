package akinai.minikgurme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.util.CrashReportDialog
import akinai.minikgurme.util.CrashReporter
import akinai.minikgurme.ui.theme.MinikGurmeTheme

class MainActivity : ComponentActivity() {
    companion object {
        /** İlk açılıştaki bebek bilgisi ekranı tamamlandı mı. */
        const val KEY_ONBOARDED = "onboarding_done"
        /** Püre / BLW / Karma tercihi (tariflerin varsayılan filtresi). */
        const val KEY_FEEDING_STYLE = "feeding_style"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CrashReporter.install(applicationContext)
        akinai.minikgurme.data.local.PlatformContext.init(applicationContext)
        AppLanguage.init()
        kotlinx.coroutines.runBlocking { akinai.minikgurme.i18n.Translator.load(akinai.minikgurme.i18n.composeFileReader(applicationContext)) }
        akinai.minikgurme.community.Community.init()
        // Platforma özel servisler (ortak ekranlar bunları kullanır)
        akinai.minikgurme.platform.Platform.services = akinai.minikgurme.platform.AndroidPlatformServices(applicationContext)
        if (akinai.minikgurme.community.Community.backend === akinai.minikgurme.community.Community.Unconfigured)
            akinai.minikgurme.community.Community.backend = akinai.minikgurme.community.AndroidCommunityBackend
        akinai.minikgurme.reminders.AndroidReminders.install(applicationContext)
        akinai.minikgurme.ui.theme.ThemePrefs.init()
        // Duman testi / ilk açılış için dil seçimi: adb shell am start ... --es lang en
        intent?.getStringExtra("lang")?.let { if (it == "tr" || it == "en") AppLanguage.set(it) }

        akinai.minikgurme.billing.GooglePlayBilling.activity = java.lang.ref.WeakReference(this)
        akinai.minikgurme.billing.GooglePlayBilling.init(this)
        akinai.minikgurme.reminders.AndroidReminders.ensureChannel(this)
        akinai.minikgurme.reminders.AndroidReminders.reschedule(this)

        setContent {
            MinikGurmeTheme {
                // Ortak kök (iOS ile aynı); bebek değişince Android etkinliği yeniden başlatır
                akinai.minikgurme.ui.MinikGurmeApp(onRestart = { recreate() })
                CrashReportDialog()
            }
        }
    }
}
