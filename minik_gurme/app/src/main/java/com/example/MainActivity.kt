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
import com.example.ui.theme.MinikGurmeTheme
import com.example.ui.viewmodel.FeedingViewModel
import com.example.ui.viewmodel.FeedingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CrashReporter.install(applicationContext)
        AppLanguage.init(applicationContext)
        // Duman testi / ilk açılış için dil seçimi: adb shell am start ... --es lang en
        intent?.getStringExtra("lang")?.let { if (it == "tr" || it == "en") AppLanguage.set(applicationContext, it) }

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = FeedingRepository(
            mealDao = database.mealDao(),
            feedingDiaryDao = database.feedingDiaryDao(),
            growthRecordDao = database.growthRecordDao(),
            allergenTestDao = database.allergenTestDao(),
            babyProfileDao = database.babyProfileDao(),
            triedFoodDao = database.triedFoodDao(),
            favoriteRecipeDao = database.favoriteRecipeDao()
        )
        val viewModelFactory = FeedingViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[FeedingViewModel::class.java]

        setContent {
            MinikGurmeTheme {
                MainScreen(viewModel = viewModel)
                CrashReportDialog()
            }
        }
    }
}
