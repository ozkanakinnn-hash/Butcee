package akinai.minikgurme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import akinai.minikgurme.data.repository.FeedingRepository
import akinai.minikgurme.shared.KeyValueStore
import akinai.minikgurme.ui.platform.PlatformBackHandler
import akinai.minikgurme.ui.screens.LegalScreen
import akinai.minikgurme.ui.screens.OnboardingScreen
import akinai.minikgurme.ui.theme.CanvasBackground
import akinai.minikgurme.ui.viewmodel.FeedingViewModel
import akinai.minikgurme.util.Babies

/** Beslenme tarzı tercihi (püre / BLW / karma) — ilk açılışta seçilir. */
const val KEY_FEEDING_STYLE = "feeding_style"

/**
 * Uygulamanın kökü (Android ve iOS ortak): etkin bebek, ilk açılış, gizlilik ve ana ekran.
 * [onRestart] verilmezse bebek değişiminde ekran ağacı yeniden kurulur (iOS); Android etkinliği yeniden başlatır.
 */
@Composable
fun MinikGurmeApp(onRestart: (() -> Unit)? = null) {
    val prefs = remember { KeyValueStore("app_prefs") }
    var babyId by remember { mutableStateOf(Babies.activeId()) }
    var generation by remember { mutableIntStateOf(0) }
    val restart: () -> Unit = onRestart ?: { babyId = Babies.activeId(); generation++ }

    key(babyId, generation) {
        val dbName = Babies.dbName(babyId)
        // Her bebeğin kendi ViewModel'i
        val viewModel: FeedingViewModel = viewModel(key = "vm_${dbName}_$generation") {
            val db = AppDatabase.getDatabase(dbName)
            FeedingViewModel(
                FeedingRepository(
                    mealDao = db.mealDao(),
                    feedingDiaryDao = db.feedingDiaryDao(),
                    growthRecordDao = db.growthRecordDao(),
                    allergenTestDao = db.allergenTestDao(),
                    babyProfileDao = db.babyProfileDao(),
                    triedFoodDao = db.triedFoodDao(),
                    favoriteRecipeDao = db.favoriteRecipeDao(),
                    shoppingItemDao = db.shoppingItemDao()
                )
            ).also { it.applyFeedingStyle(prefs.getString(KEY_FEEDING_STYLE, null)) }
        }
        var onboarded by remember { mutableStateOf(Babies.isOnboarded(babyId)) }
        var showPrivacy by remember { mutableStateOf(false) }
        when {
            onboarded -> MainScreen(
                viewModel = viewModel,
                onSwitchBaby = { id -> Babies.setActive(id); restart() },
                onAddBaby = { Babies.addNew(); restart() },
                onDeleteBaby = { id -> AppDatabase.closeAll(); Babies.delete(id); restart() },
                onReload = restart
            )
            showPrivacy -> {
                PlatformBackHandler { showPrivacy = false }
                Box(Modifier.fillMaxSize().background(CanvasBackground).systemBarsPadding()) {
                    LegalScreen(kind = "privacy", onBack = { showPrivacy = false })
                }
            }
            else -> OnboardingScreen(onOpenPrivacy = { showPrivacy = true }) { name, months, weight, height, style, sex ->
                viewModel.completeOnboarding(name, months, weight, height, sex)
                viewModel.applyFeedingStyle(style)
                Babies.setName(babyId, name)
                Babies.setOnboarded(babyId)
                prefs.putString(KEY_FEEDING_STYLE, style)
                onboarded = true
            }
        }
    }
}
