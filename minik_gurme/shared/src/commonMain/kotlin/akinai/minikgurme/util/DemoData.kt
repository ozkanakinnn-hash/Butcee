package akinai.minikgurme.util

import akinai.minikgurme.data.local.AppDatabase
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.data.model.TriedFood

/**
 * Yalnızca App Store ekran görüntüsü modunda (-mgDemo 2): gerçekçi örnek kayıtlar (8 aylık Defne).
 * Android mağaza görüntüleriyle (StoreScreenshotsTest) aynı içerik. Normal kullanımda hiç çağrılmaz.
 */
object DemoData {
    private fun day(offset: Int) = DateKit.isoPlusDays(DateKit.todayIso(), offset)

    suspend fun seed(db: AppDatabase, en: Boolean) {
        // Bugünün ölçümü onboarding'de girilir; öncekiler burada
        listOf(Triple(-60, 6.9f, 64.0f), Triple(-45, 7.2f, 65.5f), Triple(-30, 7.5f, 66.8f), Triple(-15, 7.8f, 68.0f))
            .forEachIndexed { i, (d, w, h) ->
                db.growthRecordDao().insertRecord(GrowthRecord(date = day(d), weightKg = w, heightCm = h, ageMonths = 6 + i / 2))
            }
        // Öğün metinlerini kullanıcı yazar (çevrilmez) → İngilizce çekimde İngilizce örnek
        val m = if (en) listOf("Oatmeal, Banana" to "Mashed", "Broccoli, Carrot" to "Finger food", "Zucchini, Lentils" to "Purée")
                else listOf("Yulaf lapası, Muz" to "Çatalla ezilmiş", "Brokoli, Havuç" to "Parmak gıda", "Kabak, Mercimek" to "Püre")
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Kahvaltı", foodItems = m[0].first, texture = m[0].second, reaction = "Sevdi"))
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Öğle", foodItems = m[1].first, texture = m[1].second, reaction = "Sevdi"))
        db.mealDao().insertMeal(Meal(date = day(0), mealType = "Akşam", foodItems = m[2].first, texture = m[2].second, reaction = "Nötr"))
        val tried = listOf("avokado" to "Avokado", "kabak" to "Yeşil Kabak", "havuc" to "Havuç", "tatli_patates" to "Tatlı Patates", "brokoli" to "Brokoli",
            "elma" to "Elma", "armut" to "Armut", "muz" to "Muz", "yulaf" to "Bebek Yulafı", "yogurt" to "Doğal Ev Yoğurdu (Tam Yağlı)",
            "kirmizi_mercimek" to "Kırmızı Mercimek", "seftali" to "Şeftali", "karnabahar" to "Karnabahar", "patates" to "Patates", "yumurta_sarisi" to "Yumurta Sarısı")
        tried.forEachIndexed { i, (id, name) ->
            db.triedFoodDao().insertOrUpdateTriedFood(TriedFood(foodId = id, foodName = name, reaction = if (i % 5 == 3) "neutral" else "loved", triedDate = day(-50 + i * 3)))
        }
        db.allergenTestDao().insertTest(AllergenTest(food = "Yumurta", startDate = day(-1), day = 2, status = "Devam Ediyor"))
        db.allergenTestDao().insertTest(AllergenTest(food = "Yoğurt", startDate = day(-20), day = 3, status = "Güvenli"))
    }
}
