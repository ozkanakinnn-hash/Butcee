package com.example.data.repository

import com.example.data.local.AllergenTestDao
import com.example.data.local.BabyProfileDao
import com.example.data.local.FavoriteRecipeDao
import com.example.data.local.FeedingDiaryDao
import com.example.data.local.GrowthRecordDao
import com.example.data.local.MealDao
import com.example.data.local.TriedFoodDao
import com.example.data.model.AllergenTest
import com.example.data.model.BabyProfile
import com.example.data.model.FavoriteRecipe
import com.example.data.model.FeedingDiaryEntry
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
import com.example.data.model.TriedFood
import kotlinx.coroutines.flow.Flow

class FeedingRepository(
    private val mealDao: MealDao,
    private val feedingDiaryDao: FeedingDiaryDao,
    private val growthRecordDao: GrowthRecordDao,
    private val allergenTestDao: AllergenTestDao,
    private val babyProfileDao: BabyProfileDao,
    private val triedFoodDao: TriedFoodDao,
    private val favoriteRecipeDao: FavoriteRecipeDao,
    private val shoppingItemDao: com.example.data.local.ShoppingItemDao
) {
    // Alışveriş listesi
    val shoppingItems: Flow<List<com.example.data.model.ShoppingItem>> = shoppingItemDao.getAll()
    suspend fun addShoppingItems(items: List<com.example.data.model.ShoppingItem>) = shoppingItemDao.insertAll(items)
    suspend fun setShoppingChecked(id: Long, checked: Boolean) = shoppingItemDao.setChecked(id, checked)
    suspend fun deleteShoppingItem(id: Long) = shoppingItemDao.deleteById(id)
    suspend fun clearCheckedShopping() = shoppingItemDao.deleteChecked()
    suspend fun clearAllShopping() = shoppingItemDao.deleteAll()

    // Growth Tracking (Gelişim Takibi)
    val allGrowthRecords: Flow<List<GrowthRecord>> = growthRecordDao.getAllGrowthRecords()
    val latestGrowthRecord: Flow<GrowthRecord?> = growthRecordDao.getLatestGrowthRecord()
    val growthRecordCount: Flow<Int> = growthRecordDao.getRecordCount()

    suspend fun insertGrowthRecord(record: GrowthRecord): Long =
        growthRecordDao.insertRecord(record)

    suspend fun updateGrowthRecord(record: GrowthRecord) =
        growthRecordDao.updateRecord(record)

    suspend fun deleteGrowthRecordById(id: Long) =
        growthRecordDao.deleteRecordById(id)
    // Feeding Diary (Beslenme Günlüğü)
    val allDiaryEntries: Flow<List<FeedingDiaryEntry>> = feedingDiaryDao.getAllDiaryEntries()
    val diaryTotalCount: Flow<Int> = feedingDiaryDao.getTotalCount()
    val diaryDistinctDates: Flow<List<String>> = feedingDiaryDao.getDistinctDates()

    fun getDiaryEntriesForDate(date: String): Flow<List<FeedingDiaryEntry>> =
        feedingDiaryDao.getEntriesForDate(date)

    fun searchDiaryEntries(query: String): Flow<List<FeedingDiaryEntry>> =
        feedingDiaryDao.searchEntries(query)

    suspend fun insertDiaryEntry(entry: FeedingDiaryEntry): Long =
        feedingDiaryDao.insertEntry(entry)

    suspend fun updateDiaryEntry(entry: FeedingDiaryEntry) =
        feedingDiaryDao.updateEntry(entry)

    suspend fun deleteDiaryEntryById(id: Long) =
        feedingDiaryDao.deleteEntryById(id)
    // Meals
    val allMeals: Flow<List<Meal>> = mealDao.getAllMeals()
    val mealCount: Flow<Int> = mealDao.getMealCount()

    fun getMealsForDate(date: String): Flow<List<Meal>> = mealDao.getMealsForDate(date)

    suspend fun insertMeal(meal: Meal): Long = mealDao.insertMeal(meal)

    suspend fun updateMeal(meal: Meal) = mealDao.updateMeal(meal)

    suspend fun deleteMealById(id: Long) = mealDao.deleteMealById(id)

    // Allergen Tests
    val allAllergenTests: Flow<List<AllergenTest>> = allergenTestDao.getAllTests()

    suspend fun insertAllergenTest(test: AllergenTest): Long = allergenTestDao.insertTest(test)

    suspend fun updateAllergenTest(test: AllergenTest) = allergenTestDao.updateTest(test)

    suspend fun deleteAllergenTestById(id: Long) = allergenTestDao.deleteTestById(id)

    // Baby Profile
    val babyProfile: Flow<BabyProfile?> = babyProfileDao.getProfile()

    suspend fun updateProfile(profile: BabyProfile) = babyProfileDao.insertOrUpdateProfile(profile)

    /** Beslenme istatistiklerini sıfırlar: tüm öğün kayıtları ve alerjen testleri silinir. */
    suspend fun resetFeedingStatistics() {
        mealDao.deleteAllMeals()
        allergenTestDao.deleteAllTests()
    }

    // 100 Foods Challenge & Food Tracking
    val allTriedFoods: Flow<List<TriedFood>> = triedFoodDao.getAllTriedFoods()
    val triedCount: Flow<Int> = triedFoodDao.getTriedCount()

    suspend fun saveTriedFood(triedFood: TriedFood) {
        triedFoodDao.insertOrUpdateTriedFood(triedFood)
    }

    suspend fun deleteTriedFood(foodId: String) {
        triedFoodDao.deleteTriedFood(foodId)
    }

    // Favorite Recipes
    val favoriteRecipeIds: Flow<List<String>> = favoriteRecipeDao.getAllFavoriteIds()

    suspend fun toggleFavoriteRecipe(recipeId: String, shouldBeFav: Boolean) {
        if (shouldBeFav) {
            favoriteRecipeDao.addFavorite(FavoriteRecipe(recipeId))
        } else {
            favoriteRecipeDao.removeFavorite(recipeId)
        }
    }
}
