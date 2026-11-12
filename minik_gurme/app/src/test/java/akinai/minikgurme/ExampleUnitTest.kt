package akinai.minikgurme

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun foodDirectory_hasValidData() {
    val foods = akinai.minikgurme.data.guide.FoodDirectoryData.allFoods
    assertTrue("Gıda listesi en az 10 besin içermelidir", foods.size >= 10)
    val avocado = akinai.minikgurme.data.guide.FoodDirectoryData.getFoodById("avokado")
    assertNotNull(avocado)
    assertEquals("Avokado", avocado?.name)
    assertFalse(avocado!!.isAllergen)
    assertTrue(avocado.nutrients.isNotEmpty())
  }

  @Test
  fun recipes_hasValidDailyMenu() {
    val menu = akinai.minikgurme.data.guide.PediatricRecipesData.getDailyMenu()
    assertTrue(menu.containsKey("Kahvaltı"))
    assertTrue(menu.containsKey("Öğle"))
    val recipes = akinai.minikgurme.data.guide.PediatricRecipesData.allRecipes
    assertTrue("Tarifler listesi boş olmamalı", recipes.isNotEmpty())
  }

  @Test
  fun feedingDiaryEntry_creationAndDefaults() {
    val entry = akinai.minikgurme.data.model.FeedingDiaryEntry(
        date = "2026-11-08",
        time = "09:30",
        mealType = "Kahvaltı / Sabah",
        foodItems = "Avokado, Yumurta sarısı",
        portionAmount = "3 tatlı kaşığı",
        texture = "Çatalla Ezilmiş",
        appetite = "İştahla Bitirdi 😋",
        reaction = "Sorunsuz / Keyifli ✨",
        notes = "Harika bir kahvaltı"
    )
    assertEquals("2026-11-08", entry.date)
    assertEquals("09:30", entry.time)
    assertEquals("Kahvaltı / Sabah", entry.mealType)
    assertEquals("Avokado, Yumurta sarısı", entry.foodItems)
    assertEquals("3 tatlı kaşığı", entry.portionAmount)
    assertEquals("Çatalla Ezilmiş", entry.texture)
    assertEquals("İştahla Bitirdi 😋", entry.appetite)
    assertEquals("Sorunsuz / Keyifli ✨", entry.reaction)
    assertTrue(entry.timestamp > 0)
  }

  @Test
  fun foodStartMonthData_hasValidAgeRecommendations() {
    val foods = akinai.minikgurme.data.guide.FoodStartMonthData.allRecommendations
    assertTrue("En az 20 besin başlangıç önerisi bulunmalı", foods.size >= 20)

    val avocado = foods.find { it.id == "avokado" }
    assertNotNull(avocado)
    assertEquals(6, avocado?.startMonth)

    val beef = foods.find { it.id == "dana_kiyma" }
    assertNotNull(beef)
    assertEquals(7, beef?.startMonth)

    val fish = foods.find { it.id == "balik" }
    assertNotNull(fish)
    assertEquals(9, fish?.startMonth)

    val forbidden = akinai.minikgurme.data.guide.FoodStartMonthData.forbiddenFoodsBeforeOneYear
    assertTrue("Yasaklı gıdalar listesi dolu olmalı", forbidden.isNotEmpty())
    val honey = forbidden.find { it.name.contains("Bal") }
    assertNotNull(honey)
  }

  @Test
  fun growthRecord_creationAndGainCalculation() {
    val birthRecord = akinai.minikgurme.data.model.GrowthRecord(
        id = 1,
        date = "2026-03-15",
        weightKg = 3.35f,
        heightCm = 50.0f,
        headCircumferenceCm = 35.0f,
        ageMonths = 0,
        notes = "Doğum ölçümü"
    )
    val monthSevenRecord = akinai.minikgurme.data.model.GrowthRecord(
        id = 6,
        date = "2026-10-15",
        weightKg = 8.45f,
        heightCm = 69.5f,
        headCircumferenceCm = 44.0f,
        ageMonths = 7,
        notes = "7. ay kontrolü"
    )

    assertEquals(3.35f, birthRecord.weightKg, 0.01f)
    assertEquals(8.45f, monthSevenRecord.weightKg, 0.01f)

    val weightGain = monthSevenRecord.weightKg - birthRecord.weightKg
    assertEquals(5.10f, weightGain, 0.01f)

    val heightGain = monthSevenRecord.heightCm - birthRecord.heightCm
    assertEquals(19.5f, heightGain, 0.01f)
    assertTrue("7. ay ağırlığı WHO standartlarında sağlıklı olmalıdır", monthSevenRecord.weightKg in 6.5f..10.5f)
  }

  @Test
  fun recipes_expandedCatalog_coversAllAgeStages() {
    val allRecipes = akinai.minikgurme.data.guide.PediatricRecipesData.allRecipes
    assertTrue("En az 10 tarif bulunmalı", allRecipes.size >= 10)

    val recipes6m = allRecipes.filter { it.ageMonths == 6 }
    val recipes7m = allRecipes.filter { it.ageMonths == 7 }
    val recipes8m = allRecipes.filter { it.ageMonths == 8 }
    val recipes9m = allRecipes.filter { it.ageMonths == 9 }
    val recipes10m = allRecipes.filter { it.ageMonths >= 10 }

    assertTrue("6+ ay tarifleri mevcut olmalı", recipes6m.isNotEmpty())
    assertTrue("7+ ay tarifleri mevcut olmalı", recipes7m.isNotEmpty())
    assertTrue("8+ ay tarifleri mevcut olmalı", recipes8m.isNotEmpty())
    assertTrue("9+ ay tarifleri mevcut olmalı", recipes9m.isNotEmpty())
    assertTrue("10+ ay tarifleri mevcut olmalı", recipes10m.isNotEmpty())

    // Her tarif doktor tavsiyesi ve malzeme içermeli
    allRecipes.forEach { recipe ->
      assertTrue("${recipe.id} başlığı boş olmamalı", recipe.title.isNotBlank())
      assertTrue("${recipe.id} doktor notu boş olmamalı", recipe.doctorTip.isNotBlank())
      assertTrue("${recipe.id} malzeme listesi boş olmamalı", recipe.ingredients.isNotEmpty())
      assertTrue("${recipe.id} yapılış adımları boş olmamalı", recipe.instructions.isNotEmpty())
    }
  }

  @Test
  fun pediatricKnowledgeBase_hasValidWeeklyGuidesAndSearch() {
    val guides = akinai.minikgurme.data.guide.PediatricKnowledgeBaseData.allWeeklyGuides
    assertTrue("Haftalık kılavuz sayısı en az 10 olmalıdır", guides.size >= 10)

    val guide6m1 = akinai.minikgurme.data.guide.PediatricKnowledgeBaseData.getGuideForBaby(6, 1)
    assertNotNull(guide6m1)
    assertEquals(6, guide6m1.month)
    assertEquals(1, guide6m1.weekNumber)
    assertTrue(guide6m1.weeklyFoods.isNotEmpty())
    assertTrue(guide6m1.goldenRules.isNotEmpty())
    assertTrue(guide6m1.faqs.isNotEmpty())

    // 7. ay kılavuz kontrolü
    val guides7m = akinai.minikgurme.data.guide.PediatricKnowledgeBaseData.getGuidesForMonth(7)
    assertTrue("7. ay kılavuzları bulunmalı", guides7m.isNotEmpty())

    // Arama testi
    val searchResults = akinai.minikgurme.data.guide.PediatricKnowledgeBaseData.searchGuides("demir")
    assertTrue("Demir araması sonuç döndürmeli", searchResults.isNotEmpty())
  }
}
