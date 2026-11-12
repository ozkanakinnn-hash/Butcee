package akinai.minikgurme

import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.util.CrashReportDialog
import akinai.minikgurme.util.CrashReporter
import akinai.minikgurme.data.repository.FeedingRepository
import akinai.minikgurme.ui.MainScreen
import akinai.minikgurme.billing.PlusManager
import akinai.minikgurme.ui.screens.LegalScreen
import akinai.minikgurme.util.Babies
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import akinai.minikgurme.ui.screens.OnboardingScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import akinai.minikgurme.ui.theme.MinikGurmeTheme
import akinai.minikgurme.ui.viewmodel.FeedingViewModel
import akinai.minikgurme.ui.viewmodel.FeedingViewModelFactory

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

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val babyId = Babies.activeId()
        val dbName = Babies.dbName(babyId)
        val database = AppDatabase.getDatabase(applicationContext, dbName)
        val repository = FeedingRepository(
            mealDao = database.mealDao(),
            feedingDiaryDao = database.feedingDiaryDao(),
            growthRecordDao = database.growthRecordDao(),
            allergenTestDao = database.allergenTestDao(),
            babyProfileDao = database.babyProfileDao(),
            triedFoodDao = database.triedFoodDao(),
            favoriteRecipeDao = database.favoriteRecipeDao(),
            shoppingItemDao = database.shoppingItemDao()
        )
        val viewModelFactory = FeedingViewModelFactory(repository)
        // Her bebeğin kendi ViewModel'i (bebek değişince etkinlik yeniden başlar)
        val viewModel = ViewModelProvider(this, viewModelFactory)["vm_$dbName", FeedingViewModel::class.java]
        viewModel.applyFeedingStyle(prefs.getString(KEY_FEEDING_STYLE, null))

        akinai.minikgurme.billing.GooglePlayBilling.activity = java.lang.ref.WeakReference(this)
        akinai.minikgurme.billing.GooglePlayBilling.init(this)
        akinai.minikgurme.reminders.AndroidReminders.ensureChannel(this)
        akinai.minikgurme.reminders.AndroidReminders.reschedule(this)

        setContent {
            MinikGurmeTheme {
                var onboarded by remember { mutableStateOf(prefs.getBoolean(Babies.onboardedKey(babyId), false)) }
                var showPrivacy by remember { mutableStateOf(false) }
                if (onboarded) {
                    MainScreen(
                        viewModel = viewModel,
                        onSwitchBaby = { id -> Babies.setActive(id); recreate() },
                        onAddBaby = { Babies.addNew(); recreate() },
                        onDeleteBaby = { id -> AppDatabase.closeAll(); Babies.delete(id); recreate() },
                        onReload = { recreate() }
                    )
                } else if (showPrivacy) {
                    androidx.activity.compose.BackHandler { showPrivacy = false }
                    androidx.compose.foundation.layout.Box(
                        androidx.compose.ui.Modifier.fillMaxSize().background(akinai.minikgurme.ui.theme.CanvasBackground).systemBarsPadding()
                    ) { LegalScreen(kind = "privacy", onBack = { showPrivacy = false }) }
                } else {
                    OnboardingScreen(onOpenPrivacy = { showPrivacy = true }) { name, months, weight, height, style, sex ->
                        viewModel.completeOnboarding(name, months, weight, height, sex)
                        viewModel.applyFeedingStyle(style)
                        Babies.setName(babyId, name)
                        prefs.edit().putBoolean(Babies.onboardedKey(babyId), true).putString(KEY_FEEDING_STYLE, style).apply()
                        onboarded = true
                    }
                }
                CrashReportDialog()
            }
        }
    }
}
