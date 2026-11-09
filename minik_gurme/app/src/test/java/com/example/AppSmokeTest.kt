package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.i18n.AppLanguage
import com.example.i18n.Translator
import com.example.i18n.trGenitive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Uygulamayı açar, ilk açılış ekranını, sekmeleri (TR/EN), dil seçimini ve istatistik sıfırlamayı dener. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()
    private val tabs = listOf("nav_today", "nav_foods100", "nav_recipes", "nav_recommendations", "nav_guide")

    private fun setOnboarded(done: Boolean) {
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean(MainActivity.KEY_ONBOARDED, done).commit()
    }

    private var scenario: ActivityScenario<MainActivity>? = null

    private fun launch(lang: String): ActivityScenario<MainActivity> =
        ActivityScenario.launch<MainActivity>(Intent(ctx, MainActivity::class.java).putExtra("lang", lang)).also { scenario = it }

    private fun has(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun shot(name: String) {
        val dir = File(System.getProperty("shots.dir") ?: "build/smoke-shots").apply { mkdirs() }
        compose.waitForIdle()
        scenario?.onActivity { a ->
            val v = a.window.decorView
            val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(android.graphics.Canvas(bmp))
            File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun visitAllTabs(prefix: String) {
        for (tag in tabs) {
            compose.onNodeWithTag(tag).performClick()
            compose.waitForIdle()
            shot("${prefix}_${tag.removePrefix("nav_")}")
        }
        compose.onNodeWithTag("top_profile_pill").performClick()
        compose.waitForIdle()
        shot("${prefix}_profile")
    }

    @Test
    fun onboardingAsksBabyInfoAndStartsEmpty() {
        setOnboarded(false)
        launch("tr").use {
            compose.waitForIdle()
            assertTrue(has("Minik Gurme'ye hoş geldiniz"))
            shot("tr_light_00_onboarding")
            // Boş gönderim kabul edilmez
            compose.onNodeWithTag("onb_start").performScrollTo().performClick()
            compose.waitForIdle()
            assertTrue(has("Minik Gurme'ye hoş geldiniz"))
            compose.onNodeWithTag("onb_name").performTextInput("Defne")
            compose.onNodeWithTag("onb_month_plus").performClick()
            compose.onNodeWithTag("onb_weight").performTextInput("7,8")
            compose.onNodeWithTag("onb_height").performTextInput("68")
            compose.onNodeWithTag("onb_start").performScrollTo().performClick()
            compose.waitForIdle()
            compose.waitUntil(5000) { has("Defne • 7 Ay") }
            val db = AppDatabase.getDatabase(ctx, kotlinx.coroutines.GlobalScope)
            runBlocking {
                assertEquals(0, db.mealDao().getMealCount().first())
                assertEquals(1, db.growthRecordDao().getRecordCount().first())
                assertEquals(7.8f, db.growthRecordDao().getLatestGrowthRecord().first()!!.weightKg)
            }
            compose.onNodeWithTag("top_profile_pill").performClick()
            compose.waitForIdle()
            shot("tr_light_01_profile_after_onboarding")
        }
    }

    @Test
    fun turkishTabsOpen() {
        setOnboarded(true)
        launch("tr").use { compose.waitForIdle(); assertTrue(has("Öğünler")); visitAllTabs("tr_light") }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun darkThemeTabsOpen() {
        setOnboarded(true)
        launch("tr").use { compose.waitForIdle(); visitAllTabs("tr_dark") }
    }

    @Test
    fun englishTabsOpenAndTranslate() {
        setOnboarded(true)
        launch("en").use {
            compose.waitForIdle()
            assertEquals("en", AppLanguage.current)
            assertTrue(has("Meals")); assertTrue(has("Recipes"))
            visitAllTabs("en_light")
            assertTrue(has("Baby Profile & Statistics"))
        }
    }

    @Test
    fun languageSwitchAndResetStatistics() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_profile_pill").performClick()
            compose.waitForIdle()
            compose.onNodeWithTag("lang_en").performClick(); compose.waitForIdle()
            assertTrue(has("Meals"))
            compose.onNodeWithTag("lang_tr").performClick(); compose.waitForIdle()
            assertTrue(has("Öğünler"))
            // Sıfırlama: önce uyarı gelir, onaylanınca silinir
            compose.onNodeWithTag("reset_stats_button").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("İstatistikler sıfırlansın mı?"))
            shot("tr_light_02_reset_dialog")
            compose.onNodeWithTag("reset_confirm").performClick(); compose.waitForIdle()
            assertTrue(!has("İstatistikler sıfırlansın mı?"))
        }
    }

    @Test
    fun turkishGenitiveAndPatterns() {
        assertEquals("Can'ın", trGenitive("Can"))
        assertEquals("Ali'nin", trGenitive("Ali"))
        assertEquals("Defne'nin", trGenitive("Defne"))
        assertEquals("Umut'un", trGenitive("Umut"))
        assertEquals("Ömür'ün", trGenitive("Ömür"))
        assertEquals("Kaya'nın", trGenitive("Kaya"))
        launch("en").use {
            assertEquals("Day 5 saved.", Translator.toEnglish("5. gün kaydedildi."))
            assertEquals("Defne's Menu 👶", Translator.toEnglish("Defne'nin Menüsü 👶"))
        }
    }

    @Test
    fun portionScaling() {
        assertEquals("1/2 adet", com.example.util.scaleAmount("1/4 adet", 2f))
        assertEquals("2-4 tatlı kaşığı", com.example.util.scaleAmount("1-2 tatlı kaşığı", 2f))
        assertEquals("1 1/2 su bardağı", com.example.util.scaleAmount("1/2 su bardağı", 3f))
        assertEquals("4 servings", com.example.util.scaleAmount("2 servings", 2f))
        assertEquals("1 pinch", com.example.util.scaleAmount("1 pinch", 1f))
        assertEquals("Taze nane", com.example.util.scaleAmount("Taze nane", 2f))
        assertEquals(2, com.example.util.baseServings("2 porsiyon"))
    }

    @Test
    fun shoppingListAddCheckClear() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_shopping").performClick(); compose.waitForIdle()
            assertTrue(has("Alışveriş Listesi 🛒"))
            compose.onNodeWithTag("shopping_input").performTextInput("Avokado")
            compose.onNodeWithTag("shopping_add").performClick(); compose.waitForIdle()
            compose.waitUntil(3000) { has("1 kalem alınacak") }
            shot("tr_light_03_shopping")
            compose.onNodeWithTag("shopping_item").performClick()
            compose.onAllNodes(androidx.compose.ui.test.isToggleable())[0].performClick(); compose.waitForIdle()
            compose.waitUntil(3000) { has("Alınanları temizle") }
            compose.onNodeWithTag("shopping_clear_checked").performClick(); compose.waitForIdle()
            compose.waitUntil(3000) { has("Tariflerden malzeme ekleyin veya aşağıya yazın.") }
        }
    }

    @Test
    fun settingsGearThemeAndReset() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            shot("tr_light_04_topbar")
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            assertTrue(has("Ayarlar"))
            shot("tr_light_05_settings")
            compose.onNodeWithTag("theme_dark").performClick(); compose.waitForIdle()
            assertEquals("dark", com.example.ui.theme.ThemePrefs.mode)
            assertTrue(com.example.ui.theme.ThemeState.dark)
            shot("tr_dark_05_settings")
            compose.onNodeWithTag("theme_light").performClick(); compose.waitForIdle()
            assertTrue(!com.example.ui.theme.ThemeState.dark)
            compose.onNodeWithTag("settings_reset_stats").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("İstatistikler sıfırlansın mı?"))
            compose.onNodeWithTag("settings_confirm").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("theme_system").performScrollTo().performClick(); compose.waitForIdle()
            assertEquals("system", com.example.ui.theme.ThemePrefs.mode)
        }
    }
}
