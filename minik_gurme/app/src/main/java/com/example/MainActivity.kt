package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.i18n.AppLanguage
import com.example.util.CrashReportDialog
import com.example.util.CrashReporter
import com.example.data.repository.FeedingRepository
import com.example.ui.MainScreen
import com.example.ui.screens.OnboardingScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.MinikGurmeTheme
import com.example.ui.viewmodel.FeedingViewModel
import com.example.ui.viewmodel.FeedingViewModelFactory

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
        AppLanguage.init(applicationContext)
        com.example.ui.theme.ThemePrefs.init(applicationContext)
        // Duman testi / ilk açılış için dil seçimi: adb shell am start ... --es lang en
        intent?.getStringExtra("lang")?.let { if (it == "tr" || it == "en") AppLanguage.set(applicationContext, it) }

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
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
        val viewModel = ViewModelProvider(this, viewModelFactory)[FeedingViewModel::class.java]
        viewModel.applyFeedingStyle(prefs.getString(KEY_FEEDING_STYLE, null))

        setContent {
            MinikGurmeTheme {
                var onboarded by remember { mutableStateOf(prefs.getBoolean(KEY_ONBOARDED, false)) }
                if (onboarded) {
                    MainScreen(viewModel = viewModel)
                } else {
                    OnboardingScreen { name, months, weight, height, style ->
                        viewModel.completeOnboarding(name, months, weight, height)
                        viewModel.applyFeedingStyle(style)
                        prefs.edit().putBoolean(KEY_ONBOARDED, true).putString(KEY_FEEDING_STYLE, style).apply()
                        onboarded = true
                    }
                }
                CrashReportDialog()
            }
        }
    }
}
