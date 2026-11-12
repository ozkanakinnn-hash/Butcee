package akinai.minikgurme

import akinai.minikgurme.resources.emoji_1f952
import akinai.minikgurme.resources.emoji_cucumber
import akinai.minikgurme.resources.food_karnabahar
import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Translator
import akinai.minikgurme.i18n.trGenitive
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
    private val tabs = listOf("nav_today", "nav_foods100", "nav_recipes", "nav_guide", "nav_community")

    @org.junit.Before
    fun reset() {
        TestResources.init()
        AppDatabase.closeAll()
        listOf("minik_gurme_db").forEach { ctx.deleteDatabase(it) }
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().clear().commit()
        akinai.minikgurme.community.Community.backend = FakeCommunityBackend() // Firebase'e bağlanma
        org.robolectric.Shadows.shadowOf(ctx as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun setPlusDebug(on: Boolean) {
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putBoolean("plus_debug_override", on).commit()
    }

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
            compose.onNodeWithTag("onb_sex_girl").performScrollTo().performClick()
            // Onay olmadan başlamaz
            compose.onNodeWithTag("onb_start").performScrollTo().performClick()
            compose.waitForIdle()
            assertTrue(has("Minik Gurme'ye hoş geldiniz"))
            compose.onNodeWithTag("onb_consent").performScrollTo().performClick()
            shot("tr_light_00b_onboarding_filled")
            compose.onNodeWithTag("onb_start").performScrollTo().performClick()
            compose.waitForIdle()
            compose.waitUntil(5000) { has("Defne • 7 Ay") }
            compose.waitUntil(5000) { has("Merhaba, Defne!") }
            shot("tr_light_01_today_greeting")
            val db = AppDatabase.getDatabase(ctx, kotlinx.coroutines.GlobalScope)
            runBlocking {
                assertEquals(0, db.mealDao().getMealCount().first())
                assertEquals(1, db.growthRecordDao().getRecordCount().first())
                assertEquals(7.8f, db.growthRecordDao().getLatestGrowthRecord().first()!!.weightKg)
                assertEquals("girl", db.babyProfileDao().getProfile().first()!!.sex)
                assertTrue("İlk açılışta bildirimler açık gelmeli", akinai.minikgurme.reminders.Reminders.allergenOn())
            }
            compose.onNodeWithTag("top_profile_pill").performClick()
            compose.waitForIdle()
            shot("tr_light_02_profile_after_onboarding")
        }
    }

    @Test
    fun turkishTabsOpen() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle(); assertTrue(has("Bugün")); assertTrue(!has("Öneriler"))
            visitAllTabs("tr_light")
            compose.onNodeWithTag("nav_guide").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("guide_section_library").performClick(); compose.waitForIdle()
            shot("tr_light_guide_library")
        }
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
            assertTrue(has("Today")); assertTrue(has("Recipes"))
            visitAllTabs("en_light")
            assertTrue(has("Baby Profile & Statistics"))
        }
    }

    @Test
    fun languageSwitchAndResetStatistics() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("settings_lang_en").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("Settings"))
            compose.onNodeWithTag("settings_lang_tr").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("Ayarlar"))
            compose.onNodeWithTag("settings_reset_stats").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("İstatistikler sıfırlansın mı?"))
            compose.onNodeWithTag("settings_confirm").performClick(); compose.waitForIdle()
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
            assertEquals("Defne's Menu", Translator.toEnglish("Defne'nin Menüsü"))
        }
    }

    @Test
    fun portionScaling() {
        assertEquals("1/2 adet", akinai.minikgurme.util.scaleAmount("1/4 adet", 2f))
        assertEquals("2-4 tatlı kaşığı", akinai.minikgurme.util.scaleAmount("1-2 tatlı kaşığı", 2f))
        assertEquals("1 1/2 su bardağı", akinai.minikgurme.util.scaleAmount("1/2 su bardağı", 3f))
        assertEquals("4 servings", akinai.minikgurme.util.scaleAmount("2 servings", 2f))
        assertEquals("1 pinch", akinai.minikgurme.util.scaleAmount("1 pinch", 1f))
        assertEquals("Taze nane", akinai.minikgurme.util.scaleAmount("Taze nane", 2f))
        assertEquals(2, akinai.minikgurme.util.baseServings("2 porsiyon"))
    }

    @Test
    fun shoppingListAddCheckClear() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_shopping").performClick(); compose.waitForIdle()
            assertTrue(has("Alışveriş Listesi"))
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
            assertEquals("dark", akinai.minikgurme.ui.theme.ThemePrefs.mode)
            assertTrue(akinai.minikgurme.ui.theme.ThemeState.dark)
            shot("tr_dark_05_settings")
            compose.onNodeWithTag("theme_light").performClick(); compose.waitForIdle()
            assertTrue(!akinai.minikgurme.ui.theme.ThemeState.dark)
            compose.onNodeWithTag("settings_reset_stats").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("İstatistikler sıfırlansın mı?"))
            compose.onNodeWithTag("settings_confirm").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("theme_system").performScrollTo().performClick(); compose.waitForIdle()
            assertEquals("system", akinai.minikgurme.ui.theme.ThemePrefs.mode)
        }
    }

    /** Büyük yazı boyutunda (telefondaki gibi) taşma olmadan kilo/boy düzenlenebilmeli. */
    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi", fontScale = 1.3f)
    fun growthBoxesEditableWithLargeFont() {
        setOnboarded(false)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("onb_name").performTextInput("Kübra")
            compose.onNodeWithTag("onb_weight").performTextInput("5,5")
            compose.onNodeWithTag("onb_height").performTextInput("65")
            compose.onNodeWithTag("onb_consent").performScrollTo().performClick()
            compose.onNodeWithTag("onb_start").performScrollTo().performClick()
            compose.waitUntil(5000) { has("Kübra • 6 Ay") }
            compose.waitUntil(5000) { has("Merhaba, Kübra!") }
            shot("tr_dark_large_font_today")
            compose.onAllNodesWithText("Kilo")[0].performClick(); compose.waitForIdle()
            compose.waitUntil(10000) { compose.onAllNodes(androidx.compose.ui.test.hasTestTag("metric_weight_edit")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("metric_weight").performScrollTo()
            compose.waitForIdle()
            shot("tr_dark_large_font_growth")
            compose.onNodeWithTag("metric_height_edit").assertExists()
        }
    }



    @Test
    fun plusIsLockedWithoutSubscription() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("settings_planner").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("Minik Gurme Plus")); assertTrue(has("Plus'a abone ol"))
            shot("tr_light_06_paywall")
        }
    }

    @Test
    fun plusPlannerFillsAndSendsToShopping() {
        setOnboarded(true); setPlusDebug(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            shot("tr_light_07_settings_plus")
            compose.onNodeWithTag("settings_planner").performScrollTo().performClick(); compose.waitForIdle()
            assertTrue(has("Haftalık Menü"))
            compose.onNodeWithTag("planner_autofill").performClick(); compose.waitForIdle()
            assertTrue(!has("+ Tarif seç"))
            shot("tr_light_08_planner")
            compose.onNodeWithTag("planner_to_shopping").performClick(); compose.waitForIdle()
            val db = AppDatabase.getDatabase(ctx)
            compose.waitUntil(5000) { runBlocking { db.shoppingItemDao().getAll().first().isNotEmpty() } }
        }
    }

    @Test
    fun allergenPlanStartsTrial() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_guide").performClick(); compose.waitForIdle()
            compose.onAllNodesWithText("3 Gün Kuralı (0)")[0].performClick(); compose.waitForIdle()
            compose.onNodeWithTag("allergen_plan").performScrollTo()
            shot("tr_light_09_allergen_plan")
            compose.onNodeWithTag("allergen_plan_start").performScrollTo().performClick(); compose.waitForIdle()
            val db = AppDatabase.getDatabase(ctx)
            compose.waitUntil(5000) { runBlocking { db.allergenTestDao().getAllTests().first().any { it.food == "Yumurta" } } }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun darkPlusScreens() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            shot("tr_dark_07_settings")
            compose.onNodeWithTag("settings_plus").performClick(); compose.waitForIdle()
            shot("tr_dark_06_paywall")
        }
    }

    @Test
    fun whoMediansBySex() {
        val w = akinai.minikgurme.ui.components.WhoStandardsData
        assertEquals(7.9f, w.getWhoWeight(6, "boy"), 0.01f)
        assertEquals(7.3f, w.getWhoWeight(6, "girl"), 0.01f)
        assertEquals(65.7f, w.getWhoHeight(6, "girl"), 0.01f)
        assertEquals(7.6f, w.getWhoWeight(6, ""), 0.01f)
        assertTrue(w.getWhoWeight(14, "boy") in 9.6f..10.3f)
    }

    @Test
    fun plusAddsSecondBabyWithSeparateData() {
        setOnboarded(true); setPlusDebug(true)
        launch("tr").use { sc ->
            compose.waitForIdle()
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("settings_add_baby").performScrollTo().performClick()
            compose.waitUntil(8000) { has("Minik Gurme'ye hoş geldiniz") }
            val id = akinai.minikgurme.util.Babies.activeId()
            assertTrue(id != akinai.minikgurme.util.Babies.MAIN)
            assertEquals(2, akinai.minikgurme.util.Babies.all().size)
        }
    }

    @Test
    fun hundredFoodsCompleteAndTranslated() {
        val foods = akinai.minikgurme.data.guide.FoodDirectoryData.allFoods
        assertEquals(100, foods.size)
        assertEquals(100, foods.map { it.id }.toSet().size)
        assertEquals(100, foods.map { it.name }.toSet().size)
        launch("en").use {
            compose.waitForIdle()
            val missing = foods.flatMap { f ->
                listOf(f.name, f.summary, f.serving6m, f.serving9m, f.serving12m, f.safetyAlert, f.ageDisplay, f.category) + f.nutrients
            }.filter { Translator.toEnglish(it) == it && it.any { c -> c in "çğıöşüÇĞİÖŞÜ" } }
            assertTrue("Çevrilmemiş: ${missing.take(10)}", missing.isEmpty())
        }
    }

    @Test
    fun foods100ShowsHundred() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_foods100").performClick(); compose.waitForIdle()
            assertTrue(has("0 / 100"))
            assertTrue(has("100 Besin Listelendi"))
            shot("tr_light_10_foods100_full")
        }
    }

    @Test
    fun zucchiniAndCucumberHaveOwnImages() {
        val kabak = akinai.minikgurme.data.guide.FoodDirectoryData.getFoodById("kabak")!!
        val salatalik = akinai.minikgurme.data.guide.FoodDirectoryData.getFoodById("salatalik")!!
        assertEquals(akinai.minikgurme.resources.Res.drawable.emoji_1f952, akinai.minikgurme.ui.components.emojiArtFor(kabak.emoji))
        assertEquals(akinai.minikgurme.resources.Res.drawable.emoji_cucumber, akinai.minikgurme.ui.components.emojiArtFor(salatalik.emoji))
    }

    @Test
    fun everyFoodHasItsOwnImage() {
        val foods = akinai.minikgurme.data.guide.FoodDirectoryData.allFoods
        val art = foods.associate { it.name to akinai.minikgurme.ui.components.emojiArtFor(it.emoji) }
        assertTrue("Görseli olmayan: " + art.filterValues { it == null }.keys, art.values.none { it == null })
        val dup = art.entries.groupBy({ it.value }, { it.key }).filterValues { it.size > 1 }.values
        assertTrue("Aynı görseli paylaşanlar: $dup", dup.isEmpty())
        assertEquals(akinai.minikgurme.resources.Res.drawable.food_karnabahar, akinai.minikgurme.ui.components.emojiArtFor(akinai.minikgurme.data.guide.FoodDirectoryData.getFoodById("karnabahar")!!.emoji))
    }

    @Test
    fun foodImagesInFoods100List() {
        setOnboarded(true)
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_foods100").performClick(); compose.waitForIdle()
            // Her ekran görüntüsü listenin farklı bir bölümünü gösterir
            for (id in listOf("pirasa", "bamya", "ayva", "yesil_mercimek", "sardalya", "badem")) {
                compose.onNodeWithTag("foods_100_screen").performScrollToNode(androidx.compose.ui.test.hasTestTag("food_item_$id"))
                compose.waitForIdle()
                shot("tr_light_foods_$id")
            }
        }
    }
}
