package akinai.minikgurme

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.community.Community
import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.local.PlatformContext
import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.data.model.TriedFood
import akinai.minikgurme.util.Babies
import akinai.minikgurme.util.DateKit
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Google Play mağaza ekran görüntüleri: gerçekçi örnek verilerle (8 aylık Defne) TR ve EN.
 * Çalıştır: ./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotsTest' → app/build/store-shots
 * Çerçeveleme: python3 tool/store_graphics.py
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h800dp-xxhdpi")
class StoreScreenshotsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()
    private var scenario: ActivityScenario<MainActivity>? = null

    private fun day(offset: Int) = DateKit.isoPlusDays(DateKit.todayIso(), offset)

    private fun seed(en: Boolean) = runBlocking {
        TestResources.init()
        AppDatabase.closeAll(); ctx.deleteDatabase("minik_gurme_db")
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear()
            .putBoolean(MainActivity.KEY_ONBOARDED, true).putBoolean("plus_debug_override", true).commit()
        ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().clear()
            .putString("nick", if (en) "Ada's mum" else "Defne'nin annesi").putBoolean("joined", true).commit()
        PlatformContext.init(ctx)
        Babies.setName(Babies.MAIN, "Defne")
        val db = AppDatabase.getDatabase(ctx)
        db.babyProfileDao().insertOrUpdateProfile(BabyProfile(id = 1, name = "Defne", ageMonths = 8, startDate = day(-60), sex = "girl"))
        listOf(
            Triple(-60, 6.9f, 64.0f), Triple(-45, 7.2f, 65.5f), Triple(-30, 7.5f, 66.8f), Triple(-15, 7.8f, 68.0f), Triple(0, 8.1f, 69.2f)
        ).forEachIndexed { i, (d, w, h) ->
            db.growthRecordDao().insertRecord(GrowthRecord(date = day(d), weightKg = w, heightCm = h, headCircumferenceCm = null, ageMonths = 6 + i / 2))
        }
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Kahvaltı", foodItems = "Yulaf lapası, Muz", texture = "Çatalla ezilmiş", reaction = "Sevdi", notes = ""))
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Öğle", foodItems = "Brokoli, Havuç", texture = "Parmak gıda", reaction = "Sevdi", notes = ""))
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Akşam", foodItems = "Kabak, Mercimek", texture = "Püre", reaction = "Nötr", notes = ""))
        val tried = listOf("avokado" to "Avokado", "kabak" to "Yeşil Kabak", "havuc" to "Havuç", "tatli_patates" to "Tatlı Patates", "brokoli" to "Brokoli",
            "elma" to "Elma", "armut" to "Armut", "muz" to "Muz", "yulaf" to "Bebek Yulafı", "yogurt" to "Doğal Ev Yoğurdu (Tam Yağlı)",
            "kirmizi_mercimek" to "Kırmızı Mercimek", "seftali" to "Şeftali", "karnabahar" to "Karnabahar", "patates" to "Patates", "yumurta_sarisi" to "Yumurta Sarısı")
        tried.forEachIndexed { i, (id, name) ->
            db.triedFoodDao().insertOrUpdateTriedFood(TriedFood(foodId = id, foodName = name, reaction = if (i % 5 == 3) "neutral" else "loved", triedDate = day(-50 + i * 3)))
        }
        db.allergenTestDao().insertTest(AllergenTest(food = "Yumurta", startDate = day(-1), day = 2, status = "Devam Ediyor"))
        db.allergenTestDao().insertTest(AllergenTest(food = "Yoğurt", startDate = day(-20), day = 3, status = "Güvenli"))
        AppDatabase.closeAll()
        Community.backend = FakeCommunityBackend().apply {
            if (en) {
                seed("m7_9", "u1", "Lily's dad", "We steamed broccoli for 8 minutes and served finger-sized florets — she loved it! What veggie did you start with?", minutesAgo = 25, likes = listOf("a", "b", "c", "d"), commentCount = 3)
                seed("m7_9", "u2", "Emma", "Starting eggs at 7 months: whole egg or yolk first? Our paediatrician said whole egg is fine.", minutesAgo = 140, likes = listOf("a"), commentCount = 2)
                seed("m7_9", "u3", "Noah's mum", "Tip: freeze purées in ice-cube trays, perfect portions for busy days.", minutesAgo = 60 * 26, likes = listOf("a", "b"))
            } else {
                seed("m7_9", "u1", "Elif (Ada'nın annesi)", "Brokoliyi buharda 8 dakika pişirip parmak boyunda verdim, çok sevdi! Siz hangi sebzeyle başladınız?", minutesAgo = 25, likes = listOf("a", "b", "c", "d"), commentCount = 3)
                seed("m7_9", "u2", "Burak", "7. ayda yumurtaya başlarken önce sarısını mı vermeli, bütün olarak mı? Doktorumuz bütün olabilir dedi.", minutesAgo = 140, likes = listOf("a"), commentCount = 2)
                seed("m7_9", "u3", "Zeynep", "İpucu: püreleri buz kalıbında dondurun, yoğun günler için hazır porsiyon oluyor.", minutesAgo = 60 * 26, likes = listOf("a", "b"))
            }
        }
    }

    private fun shot(name: String) {
        val dir = File("build/store-shots").apply { mkdirs() }
        compose.waitForIdle(); Thread.sleep(300); compose.waitForIdle()
        scenario?.onActivity { a ->
            val v = a.window.decorView
            val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(android.graphics.Canvas(bmp))
            File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun capture(lang: String) {
        seed(lang == "en")
        ActivityScenario.launch<MainActivity>(Intent(ctx, MainActivity::class.java).putExtra("lang", lang)).also { scenario = it }.use {
            compose.waitUntil(8000) { compose.onAllNodesWithText("Defne", substring = true).fetchSemanticsNodes().isNotEmpty() }
            shot("${lang}_1_today")
            compose.onNodeWithTag("nav_foods100").performClick(); shot("${lang}_2_foods100")
            compose.onNodeWithTag("nav_recipes").performClick(); shot("${lang}_3_recipes")
            compose.onNodeWithTag("nav_guide").performClick(); shot("${lang}_4_guide")
            compose.onNodeWithTag("nav_community").performClick()
            compose.onNodeWithTag("community_topic_m7_9").performClick(); shot("${lang}_6_community")
            compose.onNodeWithTag("nav_today").performClick(); compose.waitForIdle()
            compose.onAllNodesWithText(if (lang == "en") "Weight" else "Kilo")[0].performClick()
            compose.waitUntil(8000) { compose.onAllNodes(androidx.compose.ui.test.hasTestTag("metric_weight")).fetchSemanticsNodes().isNotEmpty() }
            shot("${lang}_5_growth")
        }
    }

    @Test fun turkish() = capture("tr")
    @Test fun english() = capture("en")
}
