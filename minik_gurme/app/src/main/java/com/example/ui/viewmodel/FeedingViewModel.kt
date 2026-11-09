package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.guide.PediatricRecipesData
import com.example.data.model.AllergenTest
import com.example.data.model.BabyProfile
import com.example.data.model.BabyRecipe
import com.example.data.model.FeedingDiaryEntry
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
import com.example.data.model.TriedFood
import com.example.data.repository.FeedingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeedingViewModel(
    private val repository: FeedingRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedGuideMonth = MutableStateFlow(7)
    val selectedGuideMonth: StateFlow<Int> = _selectedGuideMonth.asStateFlow()

    private val _selectedGuideTab = MutableStateFlow("knowledge_base")
    val selectedGuideTab: StateFlow<String> = _selectedGuideTab.asStateFlow()

    private val _allergenFilter = MutableStateFlow("Tümü")
    val allergenFilter: StateFlow<String> = _allergenFilter.asStateFlow()

    // 100 Foods Challenge state
    private val _foodSearchQuery = MutableStateFlow("")
    val foodSearchQuery: StateFlow<String> = _foodSearchQuery.asStateFlow()

    private val _selectedFoodCategory = MutableStateFlow("Tümü")
    val selectedFoodCategory: StateFlow<String> = _selectedFoodCategory.asStateFlow()

    // Recipes state
    private val _recipeSearchQuery = MutableStateFlow("")
    val recipeSearchQuery: StateFlow<String> = _recipeSearchQuery.asStateFlow()

    private val _selectedRecipeAgeFilter = MutableStateFlow("Tüm Yaşlar")
    val selectedRecipeAgeFilter: StateFlow<String> = _selectedRecipeAgeFilter.asStateFlow()

    private val _selectedRecipeStyleFilter = MutableStateFlow("Tümü")
    val selectedRecipeStyleFilter: StateFlow<String> = _selectedRecipeStyleFilter.asStateFlow()

    private val _dailyMenu = MutableStateFlow(PediatricRecipesData.getDailyMenu())
    val dailyMenu: StateFlow<Map<String, BabyRecipe>> = _dailyMenu.asStateFlow()

    val babyProfile: StateFlow<BabyProfile> = repository.babyProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BabyProfile(name = "", ageMonths = 6, startDate = "")
        ).let { flow ->
            MutableStateFlow(BabyProfile(name = "", ageMonths = 6, startDate = "")).also { state ->
                viewModelScope.launch {
                    repository.babyProfile.collect { profile ->
                        if (profile != null) {
                            state.value = profile
                            _selectedGuideMonth.value = profile.ageMonths
                        }
                    }
                }
            }
        }

    val allMeals: StateFlow<List<Meal>> = repository.allMeals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentDayMeals: StateFlow<List<Meal>> = combine(allMeals, _selectedDate) { meals, date ->
        meals.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Beslenme Günlüğü (Feeding Diary Flows)
    val allDiaryEntries: StateFlow<List<FeedingDiaryEntry>> = repository.allDiaryEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentDayDiaryEntries: StateFlow<List<FeedingDiaryEntry>> = combine(allDiaryEntries, _selectedDate) { entries, date ->
        entries.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _diarySearchQuery = MutableStateFlow("")
    val diarySearchQuery: StateFlow<String> = _diarySearchQuery.asStateFlow()

    val filteredDiaryEntries: StateFlow<List<FeedingDiaryEntry>> = combine(allDiaryEntries, _diarySearchQuery) { entries, query ->
        if (query.isBlank()) {
            entries
        } else {
            entries.filter {
                it.foodItems.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.mealType.contains(query, ignoreCase = true) ||
                it.reactionNotes.contains(query, ignoreCase = true) ||
                it.appetite.contains(query, ignoreCase = true) ||
                it.date.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Growth Tracking (Gelişim Takibi) StateFlows
    val allGrowthRecords: StateFlow<List<GrowthRecord>> = repository.allGrowthRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestGrowthRecord: StateFlow<GrowthRecord?> = repository.latestGrowthRecord
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allAllergenTests: StateFlow<List<AllergenTest>> = repository.allAllergenTests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredAllergenTests: StateFlow<List<AllergenTest>> = combine(allAllergenTests, _allergenFilter) { tests, filter ->
        when (filter) {
            "Devam Edenler" -> tests.filter { it.status == "Devam Ediyor" }
            "Güvenli Olanlar" -> tests.filter { it.status == "Güvenli" }
            "Reaksiyon / Riskli" -> tests.filter { it.status == "Reaksiyon" }
            else -> tests
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 100 Foods Challenge
    val allTriedFoods: StateFlow<List<TriedFood>> = repository.allTriedFoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorite Recipes
    val favoriteRecipeIds: StateFlow<List<String>> = repository.favoriteRecipeIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun shiftDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {
            _selectedDate.value = dateFormat.format(Date())
        }
    }

    fun setToday() {
        _selectedDate.value = dateFormat.format(Date())
    }

    fun setAllergenFilter(filter: String) {
        _allergenFilter.value = filter
    }

    fun setSelectedGuideMonth(month: Int) {
        _selectedGuideMonth.value = month
    }

    fun setSelectedGuideTab(tab: String) {
        _selectedGuideTab.value = tab
    }

    fun setFoodSearchQuery(query: String) {
        _foodSearchQuery.value = query
    }

    fun setSelectedFoodCategory(category: String) {
        _selectedFoodCategory.value = category
    }

    fun setRecipeSearchQuery(query: String) {
        _recipeSearchQuery.value = query
    }

    fun setSelectedRecipeAgeFilter(filter: String) {
        _selectedRecipeAgeFilter.value = filter
    }

    fun setSelectedRecipeStyleFilter(filter: String) {
        _selectedRecipeStyleFilter.value = filter
    }

    fun shuffleDailyMenu() {
        _dailyMenu.value = PediatricRecipesData.shuffleMenu()
    }

    fun toggleFoodTried(foodId: String, foodName: String, isCurrentlyTried: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyTried) {
                repository.deleteTriedFood(foodId)
            } else {
                val todayStr = dateFormat.format(Date())
                repository.saveTriedFood(
                    TriedFood(
                        foodId = foodId,
                        foodName = foodName,
                        isTried = true,
                        reaction = "loved",
                        triedDate = todayStr,
                        reactionNotes = ""
                    )
                )
            }
        }
    }

    fun saveFoodReaction(
        foodId: String,
        foodName: String,
        reaction: String,
        symptoms: String,
        notes: String
    ) {
        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())
            repository.saveTriedFood(
                TriedFood(
                    foodId = foodId,
                    foodName = foodName,
                    isTried = true,
                    reaction = reaction,
                    symptoms = symptoms,
                    triedDate = todayStr,
                    reactionNotes = notes
                )
            )

            // If an allergy reaction is flagged, also automatically log or alert in 3 Gün Kuralı
            if (reaction.equals("allergy", ignoreCase = true) || symptoms.isNotBlank()) {
                val existingTest = allAllergenTests.value.find { it.food.equals(foodName, ignoreCase = true) }
                if (existingTest != null) {
                    repository.updateAllergenTest(
                        existingTest.copy(
                            status = "Reaksiyon",
                            notes = "Reaksiyon: $symptoms. $notes"
                        )
                    )
                } else {
                    repository.insertAllergenTest(
                        AllergenTest(
                            food = foodName,
                            startDate = todayStr,
                            day = 1,
                            status = "Reaksiyon",
                            notes = "Reaksiyon: $symptoms. $notes"
                        )
                    )
                }
            }
        }
    }

    fun toggleFavoriteRecipe(recipeId: String) {
        viewModelScope.launch {
            val isFav = favoriteRecipeIds.value.contains(recipeId)
            repository.toggleFavoriteRecipe(recipeId, !isFav)
        }
    }

    fun addRecipeToMeals(recipe: BabyRecipe) {
        viewModelScope.launch {
            val meal = Meal(
                date = _selectedDate.value,
                mealType = when {
                    recipe.mealType.contains("Kahvaltı", ignoreCase = true) -> "Sabah"
                    recipe.mealType.contains("Öğle", ignoreCase = true) -> "Öğle"
                    recipe.mealType.contains("Akşam", ignoreCase = true) -> "Akşam"
                    else -> "İkindi"
                },
                foodItems = recipe.title,
                texture = recipe.textureStyle,
                reaction = "Sevdi",
                notes = "Tariflerden hazırlandı: ${recipe.ingredients.take(3).joinToString { it.name }}.",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMeal(meal)
        }
    }

    fun addRecipeToDiary(
        recipe: BabyRecipe,
        date: String = _selectedDate.value,
        portionAmount: String = recipe.servings,
        appetite: String = "İştahla Bitirdi 😋",
        reaction: String = "Sorunsuz / Keyifli ✨",
        customNotes: String = ""
    ) {
        val ingredientNames = recipe.ingredients.take(3).joinToString(", ") { it.name }
        val foodSummary = "${recipe.title} ($ingredientNames)"
        val diaryNotes = buildString {
            append("Tarif: ${recipe.title}")
            append(" • Süre: ${recipe.prepTime}")
            if (customNotes.isNotBlank()) {
                append(" • $customNotes")
            }
        }
        val diaryMealType = when {
            recipe.mealType.contains("Kahvaltı", ignoreCase = true) -> "Kahvaltı / Sabah"
            recipe.mealType.contains("Öğle", ignoreCase = true) -> "Öğle Yemeği"
            recipe.mealType.contains("Akşam", ignoreCase = true) -> "Akşam Yemeği"
            else -> "İkindi / Ara Öğün"
        }

        addDiaryEntry(
            date = date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            mealType = diaryMealType,
            foodItems = foodSummary,
            portionAmount = portionAmount,
            texture = recipe.textureStyle,
            appetite = appetite,
            reaction = reaction,
            reactionNotes = "",
            notes = diaryNotes
        )
    }

    fun addMeal(
        date: String = _selectedDate.value,
        mealType: String,
        foodItems: String,
        texture: String,
        reaction: String,
        notes: String
    ) {
        viewModelScope.launch {
            val meal = Meal(
                date = date,
                mealType = mealType,
                foodItems = foodItems,
                texture = texture,
                reaction = reaction,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMeal(meal)
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteMealById(id)
        }
    }

    // Beslenme Günlüğü CRUD İşlemleri
    fun setDiarySearchQuery(query: String) {
        _diarySearchQuery.value = query
    }

    fun addDiaryEntry(
        date: String = _selectedDate.value,
        time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        mealType: String,
        foodItems: String,
        portionAmount: String = "1 Porsiyon",
        texture: String = "Çatalla Ezilmiş",
        appetite: String = "İştahla Bitirdi 😋",
        reaction: String = "Sorunsuz / Keyifli ✨",
        reactionNotes: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entry = FeedingDiaryEntry(
                date = date,
                time = time.ifBlank { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) },
                mealType = mealType,
                foodItems = foodItems,
                portionAmount = portionAmount,
                texture = texture,
                appetite = appetite,
                reaction = reaction,
                reactionNotes = reactionNotes,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insertDiaryEntry(entry)

            // Aynı zamanda standart Meal tablosunu da güncelleyelim
            val mappedReaction = when {
                reaction.contains("Sorunsuz", ignoreCase = true) -> "Sevdi"
                reaction.contains("Gaz", ignoreCase = true) -> "Nötr"
                reaction.contains("Kızarıklık", ignoreCase = true) || reaction.contains("Alerji", ignoreCase = true) -> "Alerji Sinyali"
                appetite.contains("Reddetti", ignoreCase = true) -> "İstemedi"
                else -> "Sevdi"
            }
            val mappedMealType = when {
                mealType.contains("Kahvaltı", ignoreCase = true) || mealType.contains("Sabah", ignoreCase = true) -> "Sabah"
                mealType.contains("Öğle", ignoreCase = true) -> "Öğle"
                mealType.contains("Akşam", ignoreCase = true) -> "Akşam"
                else -> "İkindi"
            }
            repository.insertMeal(
                Meal(
                    date = date,
                    mealType = mappedMealType,
                    foodItems = foodItems,
                    texture = texture,
                    reaction = mappedReaction,
                    notes = if (notes.isNotBlank()) "$portionAmount • $notes" else portionAmount,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteDiaryEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteDiaryEntryById(id)
        }
    }

    fun updateDiaryEntry(entry: FeedingDiaryEntry) {
        viewModelScope.launch {
            repository.updateDiaryEntry(entry)
        }
    }

    fun addAllergenTest(food: String, notes: String) {
        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())
            val test = AllergenTest(
                food = food,
                startDate = todayStr,
                day = 1,
                status = "Devam Ediyor",
                notes = notes.ifBlank { "1. gün tadımı başladı." }
            )
            repository.insertAllergenTest(test)
        }
    }

    fun updateAllergenDay(id: Long, newDay: Int) {
        viewModelScope.launch {
            val currentList = allAllergenTests.value
            val item = currentList.find { it.id == id } ?: return@launch
            val newStatus = if (newDay >= 3) "Güvenli" else "Devam Ediyor"
            val updated = item.copy(
                day = newDay,
                status = newStatus,
                notes = if (newDay >= 3) "${item.food} 3 gün güvenle tamamlandı." else item.notes
            )
            repository.updateAllergenTest(updated)
        }
    }

    fun updateAllergenStatus(id: Long, status: String) {
        viewModelScope.launch {
            val currentList = allAllergenTests.value
            val item = currentList.find { it.id == id } ?: return@launch
            val updated = item.copy(status = status)
            repository.updateAllergenTest(updated)
        }
    }

    fun deleteAllergenTest(id: Long) {
        viewModelScope.launch {
            repository.deleteAllergenTestById(id)
        }
    }

    fun updateProfile(name: String, ageMonths: Int, startDate: String, notes: String = "") {
        viewModelScope.launch {
            val profile = BabyProfile(
                id = 1,
                name = name.ifBlank { "Bebeğim" },
                ageMonths = ageMonths,
                startDate = startDate,
                notes = notes
            )
            repository.updateProfile(profile)
        }
    }

    // Alışveriş listesi
    val shoppingItems: StateFlow<List<com.example.data.model.ShoppingItem>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Tarifin malzemelerini (seçilen porsiyon katsayısıyla) alışveriş listesine ekler. */
    fun addIngredientsToShopping(recipe: BabyRecipe, factor: Float, only: com.example.data.model.RecipeIngredient? = null) {
        viewModelScope.launch {
            val list = (if (only != null) listOf(only) else recipe.ingredients).map {
                com.example.data.model.ShoppingItem(name = it.name, amount = it.amount, factor = factor, emoji = it.emoji, source = recipe.title)
            }
            repository.addShoppingItems(list)
        }
    }

    fun addManualShoppingItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addShoppingItems(listOf(com.example.data.model.ShoppingItem(name = name.trim()))) }
    }

    fun setShoppingChecked(id: Long, checked: Boolean) = viewModelScope.launch { repository.setShoppingChecked(id, checked) }
    fun deleteShoppingItem(id: Long) = viewModelScope.launch { repository.deleteShoppingItem(id) }
    fun clearCheckedShopping() = viewModelScope.launch { repository.clearCheckedShopping() }

    /** "puree" / "blw" / "mixed": tarifler ekranının varsayılan filtresi. */
    fun applyFeedingStyle(style: String?) {
        _selectedRecipeStyleFilter.value = when (style) {
            "puree" -> "Püre"
            "blw" -> "BLW"
            else -> "Tümü"
        }
    }

    /** Beslenme istatistiklerini sıfırlar (öğünler + alerjen testleri). Profil ve gelişim kayıtları korunur. */
    fun resetFeedingStatistics() {
        viewModelScope.launch { repository.resetFeedingStatistics() }
    }

    /** İlk açılış: bebek profilini ve ilk boy/kilo ölçümünü kaydeder. */
    fun completeOnboarding(name: String, ageMonths: Int, weightKg: Float, heightCm: Float) {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            repository.updateProfile(BabyProfile(id = 1, name = name.trim(), ageMonths = ageMonths, startDate = today))
            repository.insertGrowthRecord(
                GrowthRecord(date = today, weightKg = weightKg, heightCm = heightCm, ageMonths = ageMonths)
            )
            _selectedGuideMonth.value = ageMonths
        }
    }

    fun addGrowthRecord(
        date: String,
        weightKg: Float,
        heightCm: Float,
        headCircumferenceCm: Float? = null,
        ageMonths: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val record = GrowthRecord(
                date = date,
                weightKg = weightKg,
                heightCm = heightCm,
                headCircumferenceCm = headCircumferenceCm,
                ageMonths = ageMonths,
                notes = notes
            )
            repository.insertGrowthRecord(record)
        }
    }

    fun updateGrowthRecord(record: GrowthRecord) {
        viewModelScope.launch {
            repository.updateGrowthRecord(record)
        }
    }

    fun deleteGrowthRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteGrowthRecordById(id)
        }
    }
}

class FeedingViewModelFactory(
    private val repository: FeedingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedingViewModel::class.java)) {
            return FeedingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
