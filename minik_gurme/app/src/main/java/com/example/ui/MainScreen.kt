package com.example.ui

import com.example.ui.theme.*
import androidx.activity.compose.BackHandler
import com.example.i18n.t
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AllergenTest
import com.example.data.model.BabyRecipe
import com.example.data.model.FoodDirectoryItem
import com.example.data.model.TriedFood
import com.example.ui.components.AddAllergenDialog
import com.example.ui.components.AddDiaryEntryBottomSheet
import com.example.ui.components.AddGrowthRecordDialog
import com.example.ui.components.AddMealBottomSheet
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.FoodDetailBottomSheet
import com.example.ui.components.ReactionNoteDialog
import com.example.ui.components.RecipeDetailBottomSheet
import com.example.ui.screens.Foods100Screen
import com.example.ui.screens.GrowthTrackingScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecommendationsScreen
import com.example.ui.screens.RecipesScreen
import com.example.ui.screens.ThreeDayRuleScreen
import com.example.ui.screens.TodayMealsScreen
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FeedingViewModel
import kotlinx.coroutines.launch

data class NavigationTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    viewModel: FeedingViewModel
) {
    val babyProfile by viewModel.babyProfile.collectAsStateWithLifecycle()
    val allMeals by viewModel.allMeals.collectAsStateWithLifecycle()
    val currentDayMeals by viewModel.currentDayMeals.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val allergenTests by viewModel.filteredAllergenTests.collectAsStateWithLifecycle()
    val allAllergens by viewModel.allAllergenTests.collectAsStateWithLifecycle()
    val allergenFilter by viewModel.allergenFilter.collectAsStateWithLifecycle()
    val selectedGuideMonth by viewModel.selectedGuideMonth.collectAsStateWithLifecycle()
    val selectedGuideTab by viewModel.selectedGuideTab.collectAsStateWithLifecycle()

    // 100 Foods state
    val allTriedFoods by viewModel.allTriedFoods.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val foodSearchQuery by viewModel.foodSearchQuery.collectAsStateWithLifecycle()
    val selectedFoodCategory by viewModel.selectedFoodCategory.collectAsStateWithLifecycle()

    // Recipes state
    val dailyMenu by viewModel.dailyMenu.collectAsStateWithLifecycle()
    val favoriteRecipeIds by viewModel.favoriteRecipeIds.collectAsStateWithLifecycle()
    val recipeSearchQuery by viewModel.recipeSearchQuery.collectAsStateWithLifecycle()
    val selectedRecipeAgeFilter by viewModel.selectedRecipeAgeFilter.collectAsStateWithLifecycle()
    val selectedRecipeStyleFilter by viewModel.selectedRecipeStyleFilter.collectAsStateWithLifecycle()

    // Feeding Diary State
    val allDiaryEntries by viewModel.allDiaryEntries.collectAsStateWithLifecycle()
    val diarySearchQuery by viewModel.diarySearchQuery.collectAsStateWithLifecycle()

    // Growth Tracking State
    val allGrowthRecords by viewModel.allGrowthRecords.collectAsStateWithLifecycle()
    val latestGrowthRecord by viewModel.latestGrowthRecord.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("today") }
    var previousTab by remember { mutableStateOf("today") }

    // Dialog & Sheet States
    var showAddMealSheet by remember { mutableStateOf(false) }
    var showAddDiarySheet by remember { mutableStateOf(false) }
    var showAddGrowthDialog by remember { mutableStateOf(false) }
    var showAddAllergenDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var selectedFoodForDetail by remember { mutableStateOf<FoodDirectoryItem?>(null) }
    var selectedRecipeForDetail by remember { mutableStateOf<BabyRecipe?>(null) }
    var foodForReactionNote by remember { mutableStateOf<Triple<String, String, TriedFood?>?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val diarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val foodDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val recipeDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Android system back gesture when in Profile or Growth screen
    BackHandler(enabled = currentTab == "profile" || currentTab == "growth" || currentTab == "shopping") {
        currentTab = previousTab
    }

    val navItems = listOf(
        NavigationTabItem("today", "Öğünler", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_today"),
        NavigationTabItem("foods100", "100 Gıda", Icons.Filled.Stars, Icons.Outlined.Stars, "nav_foods100"),
        NavigationTabItem("recipes", "Tarifler", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_recipes"),
        NavigationTabItem("recommendations", "Öneriler", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, "nav_recommendations"),
        NavigationTabItem("guide", "Rehber", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety, "nav_guide")
    )

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.logo_emblem),
                            contentDescription = "Minik Gurme",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LogoBackdrop)
                                .testTag("app_logo")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Minik Gurme",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = java.text.SimpleDateFormat("d MMMM, EEEE", java.util.Locale(com.example.i18n.AppLanguage.current)).format(java.util.Date()),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    androidx.compose.material3.BadgedBox(
                        badge = {
                            val open = shoppingItems.count { !it.checked }
                            if (open > 0) androidx.compose.material3.Badge(containerColor = CoralPrimary) {
                                androidx.compose.material3.Text("$open")
                            }
                        },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        androidx.compose.material3.IconButton(
                            onClick = {
                                if (currentTab != "shopping") { previousTab = currentTab; currentTab = "shopping" }
                            },
                            modifier = Modifier.testTag("top_shopping")
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.ShoppingCart,
                                contentDescription = t("Alışveriş Listesi"),
                                tint = if (currentTab == "shopping") CoralPrimary else TextSecondary
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                if (currentTab != "profile") {
                                    previousTab = currentTab
                                    currentTab = "profile"
                                }
                            }
                            .testTag("top_profile_pill"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentTab == "profile") CoralPrimary else CoralSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = if (currentTab == "profile") OnAccent else CoralDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${babyProfile.name} • ${babyProfile.ageMonths} Ay",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "profile") OnAccent else CoralDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = currentTab == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            previousTab = currentTab
                            currentTab = item.route
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralPrimary,
                            selectedTextColor = CoralPrimary,
                            indicatorColor = CoralSoft,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    "today" -> TodayMealsScreen(
                        profile = babyProfile,
                        meals = currentDayMeals,
                        allMeals = allMeals,
                        diaryEntries = allDiaryEntries,
                        diarySearchQuery = diarySearchQuery,
                        growthRecords = allGrowthRecords,
                        selectedDate = selectedDate,
                        onPreviousDay = { viewModel.shiftDate(-1) },
                        onNextDay = { viewModel.shiftDate(1) },
                        onToday = { viewModel.setToday() },
                        onDateSelect = { date -> viewModel.setSelectedDate(date) },
                        onDiarySearchChange = { viewModel.setDiarySearchQuery(it) },
                        onOpenAddMeal = { showAddMealSheet = true },
                        onOpenAddDiaryEntry = { showAddDiarySheet = true },
                        onOpenGrowthTracking = {
                            previousTab = currentTab
                            currentTab = "growth"
                        },
                        onOpenAddGrowthRecord = { showAddGrowthDialog = true },
                        onOpenRecipes = {
                            previousTab = currentTab
                            currentTab = "recipes"
                        },
                        onOpenGuide = {
                            previousTab = currentTab
                            currentTab = "guide"
                            viewModel.setSelectedGuideTab("knowledge_base")
                        },
                        onDeleteMeal = { id ->
                            viewModel.deleteMeal(id)
                            scope.launch { snackbarHostState.showSnackbar(t("Öğün silindi.")) }
                        },
                        onDeleteDiaryEntry = { id ->
                            viewModel.deleteDiaryEntry(id)
                            scope.launch { snackbarHostState.showSnackbar(t("Beslenme günlüğü kaydı silindi.")) }
                        }
                    )

                    "foods100" -> Foods100Screen(
                        profile = babyProfile,
                        triedFoods = allTriedFoods,
                        searchQuery = foodSearchQuery,
                        selectedCategory = selectedFoodCategory,
                        onSearchChange = { viewModel.setFoodSearchQuery(it) },
                        onCategoryChange = { viewModel.setSelectedFoodCategory(it) },
                        onToggleTried = { foodId, foodName, isCurrentlyTried ->
                            viewModel.toggleFoodTried(foodId, foodName, isCurrentlyTried)
                            scope.launch {
                                val msg = if (isCurrentlyTried) "$foodName denenmedi olarak güncellendi." else "$foodName 100 Gıda listesinde denendi olarak işaretlendi! 🎉"
                                snackbarHostState.showSnackbar(t(msg))
                            }
                        },
                        onSelectFood = { food ->
                            selectedFoodForDetail = food
                        },
                        onOpenReactionDialog = { foodId, foodName, currReaction, currSymptoms, currNotes ->
                            val tried = allTriedFoods.find { it.foodId == foodId }
                            foodForReactionNote = Triple(foodId, foodName, tried)
                        }
                    )

                    "recipes" -> RecipesScreen(
                        profile = babyProfile,
                        dailyMenu = dailyMenu,
                        favoriteRecipeIds = favoriteRecipeIds,
                        searchQuery = recipeSearchQuery,
                        selectedAgeFilter = selectedRecipeAgeFilter,
                        selectedStyleFilter = selectedRecipeStyleFilter,
                        onSearchChange = { viewModel.setRecipeSearchQuery(it) },
                        onAgeFilterChange = { viewModel.setSelectedRecipeAgeFilter(it) },
                        onStyleFilterChange = { viewModel.setSelectedRecipeStyleFilter(it) },
                        onShuffleMenu = {
                            viewModel.shuffleDailyMenu()
                            scope.launch { snackbarHostState.showSnackbar(t("Günün menüsü yenilendi!")) }
                        },
                        onSelectRecipe = { recipe ->
                            selectedRecipeForDetail = recipe
                        },
                        onToggleFavorite = { recipeId ->
                            viewModel.toggleFavoriteRecipe(recipeId)
                        },
                        onQuickAddToDiary = { recipe ->
                            viewModel.addRecipeToDiary(recipe)
                            scope.launch {
                                snackbarHostState.showSnackbar(t("${recipe.title} Beslenme Günlüğüne kaydedildi! 📖✨"))
                            }
                        }
                    )

                    "recommendations" -> RecommendationsScreen(
                        profile = babyProfile,
                        tests = allergenTests,
                        selectedFilter = allergenFilter,
                        onFilterChange = { viewModel.setAllergenFilter(it) },
                        onOpenAddTest = { showAddAllergenDialog = true },
                        onUpdateDay = { id, day ->
                            viewModel.updateAllergenDay(id, day)
                            scope.launch { snackbarHostState.showSnackbar(t("$day. gün kaydedildi.")) }
                        },
                        onUpdateStatus = { id, status ->
                            viewModel.updateAllergenStatus(id, status)
                            scope.launch { snackbarHostState.showSnackbar(t("Durum güncellendi: $status")) }
                        },
                        onDeleteTest = { id ->
                            viewModel.deleteAllergenTest(id)
                            scope.launch { snackbarHostState.showSnackbar(t("Besin denemesi silindi.")) }
                        },
                        onOpenReactionNote = { test ->
                            val tried = allTriedFoods.find { it.foodName.equals(test.food, ignoreCase = true) }
                            foodForReactionNote = Triple(test.food.lowercase().replace(" ", "_"), test.food, tried)
                        },
                        onSelectFood = { food ->
                            selectedFoodForDetail = food
                        }
                    )

                    "guide" -> GuideScreen(
                        selectedMonth = selectedGuideMonth,
                        selectedTab = selectedGuideTab,
                        onMonthSelect = { viewModel.setSelectedGuideMonth(it) },
                        onTabSelect = { viewModel.setSelectedGuideTab(it) }
                    )

                    "profile" -> ProfileScreen(
                        profile = babyProfile,
                        allMeals = allMeals,
                        allAllergens = allAllergens,
                        growthRecords = allGrowthRecords,
                        onOpenEditProfile = { showEditProfileDialog = true },
                        onOpenGrowthTracking = {
                            previousTab = currentTab
                            currentTab = "growth"
                        },
                        onResetStatistics = { viewModel.resetFeedingStatistics() }
                    )

                    "shopping" -> com.example.ui.screens.ShoppingListScreen(
                        items = shoppingItems,
                        onBack = { currentTab = previousTab },
                        onAdd = { viewModel.addManualShoppingItem(it) },
                        onToggle = { viewModel.setShoppingChecked(it.id, !it.checked) },
                        onDelete = { viewModel.deleteShoppingItem(it.id) },
                        onClearChecked = { viewModel.clearCheckedShopping() }
                    )
                    "growth" -> GrowthTrackingScreen(
                        profile = babyProfile,
                        records = allGrowthRecords,
                        meals = allMeals,
                        diaryEntries = allDiaryEntries,
                        onBack = { currentTab = previousTab },
                        onOpenAddRecord = { showAddGrowthDialog = true },
                        onDeleteRecord = { id ->
                            viewModel.deleteGrowthRecord(id)
                            scope.launch { snackbarHostState.showSnackbar(t("Ölçüm kaydı silindi.")) }
                        }
                    )
                }
            }
        }
    }

    // Food Detail Bottom Sheet (Images 2, 3, 4)
    selectedFoodForDetail?.let { food ->
        val tried = allTriedFoods.find { it.foodId == food.id }
        FoodDetailBottomSheet(
            food = food,
            triedInfo = tried,
            sheetState = foodDetailSheetState,
            onDismiss = { selectedFoodForDetail = null },
            onOpenReactionDialog = {
                foodForReactionNote = Triple(food.id, food.name, tried)
            },
            onAddToMeal = {
                viewModel.addMeal(
                    date = selectedDate,
                    mealType = "Öğle",
                    foodItems = food.name,
                    texture = food.serving6m.take(30),
                    reaction = "Sevdi",
                    notes = "${food.name} tadımı yapıldı."
                )
                selectedFoodForDetail = null
                scope.launch {
                    snackbarHostState.showSnackbar(t("${food.name} bugünkü öğünlere eklendi! 🥣"))
                }
            }
        )
    }

    // Recipe Detail Bottom Sheet (Images 6, 7)
    selectedRecipeForDetail?.let { recipe ->
        val isFav = favoriteRecipeIds.contains(recipe.id)
        RecipeDetailBottomSheet(
            recipe = recipe,
            babyAgeMonths = babyProfile.ageMonths,
            isFavorite = isFav,
            sheetState = recipeDetailSheetState,
            onDismiss = { selectedRecipeForDetail = null },
            onToggleFavorite = { viewModel.toggleFavoriteRecipe(recipe.id) },
            onAddToMeals = {
                viewModel.addRecipeToMeals(recipe)
                selectedRecipeForDetail = null
                scope.launch {
                    snackbarHostState.showSnackbar(t("${recipe.title} bugünkü öğün tablosuna eklendi! 🍽️"))
                }
            },
            onAddToShopping = { factor, ingredient ->
                viewModel.addIngredientsToShopping(recipe, factor, ingredient)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (ingredient == null) t("Malzemeler alışveriş listesine eklendi 🛒")
                        else t("Alışveriş listesine eklendi 🛒")
                    )
                }
            },
            onAddToDiary = {
                viewModel.addRecipeToDiary(recipe)
                selectedRecipeForDetail = null
                scope.launch {
                    snackbarHostState.showSnackbar(t("${recipe.title} Beslenme Günlüğüne kaydedildi! 📖✨"))
                }
            }
        )
    }

    // Reaction & Symptoms Note Dialog (Image 1)
    foodForReactionNote?.let { (foodId, foodName, tried) ->
        ReactionNoteDialog(
            foodName = foodName,
            initialReaction = tried?.reaction ?: "loved",
            initialSymptoms = tried?.symptoms ?: "",
            initialNotes = tried?.reactionNotes ?: "",
            onDismiss = { foodForReactionNote = null },
            onSave = { reaction, symptoms, notes ->
                viewModel.saveFoodReaction(foodId, foodName, reaction, symptoms, notes)
                foodForReactionNote = null
                scope.launch {
                    snackbarHostState.showSnackbar(t("$foodName reaksiyon ve belirtileri kaydedildi."))
                }
            }
        )
    }

    // Add Meal Bottom Sheet
    if (showAddMealSheet) {
        AddMealBottomSheet(
            sheetState = sheetState,
            onDismiss = { showAddMealSheet = false },
            onSaveMeal = { type, food, texture, reaction, notes ->
                viewModel.addMeal(
                    date = selectedDate,
                    mealType = type,
                    foodItems = food,
                    texture = texture,
                    reaction = reaction,
                    notes = notes
                )
                showAddMealSheet = false
                scope.launch {
                    snackbarHostState.showSnackbar(t("Öğün başarıyla kaydedildi!"))
                }
            }
        )
    }

    // Add Diary Entry Bottom Sheet (Beslenme Günlüğü)
    if (showAddDiarySheet) {
        AddDiaryEntryBottomSheet(
            sheetState = diarySheetState,
            initialDate = selectedDate,
            onDismiss = { showAddDiarySheet = false },
            onSaveEntry = { date, time, mealType, foodItems, portion, texture, appetite, reaction, reactionNotes, notes ->
                viewModel.addDiaryEntry(
                    date = date,
                    time = time,
                    mealType = mealType,
                    foodItems = foodItems,
                    portionAmount = portion,
                    texture = texture,
                    appetite = appetite,
                    reaction = reaction,
                    reactionNotes = reactionNotes,
                    notes = notes
                )
                showAddDiarySheet = false
                scope.launch {
                    snackbarHostState.showSnackbar(t("Beslenme günlüğüne kaydedildi! 📖"))
                }
            }
        )
    }

    // Add Allergen Dialog
    if (showAddAllergenDialog) {
        AddAllergenDialog(
            onDismiss = { showAddAllergenDialog = false },
            onConfirm = { food, notes ->
                viewModel.addAllergenTest(food, notes)
                showAddAllergenDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(t("$food için 3 gün kuralı takibi başladı."))
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = babyProfile.name,
            initialAge = babyProfile.ageMonths,
            initialStartDate = babyProfile.startDate,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, age, startDate ->
                viewModel.updateProfile(name, age, startDate)
                showEditProfileDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(t("Bebek profili güncellendi!"))
                }
            }
        )
    }

    // Add Growth Record Dialog (Gelişim / Boy & Kilo Ölçümü Ekle)
    if (showAddGrowthDialog) {
        AddGrowthRecordDialog(
            currentBabyAgeMonths = babyProfile.ageMonths,
            latestWeightKg = latestGrowthRecord?.weightKg,
            latestHeightCm = latestGrowthRecord?.heightCm,
            onDismiss = { showAddGrowthDialog = false },
            onSave = { date, weight, height, head, age, notes ->
                viewModel.addGrowthRecord(
                    date = date,
                    weightKg = weight,
                    heightCm = height,
                    headCircumferenceCm = head,
                    ageMonths = age,
                    notes = notes
                )
                scope.launch {
                    snackbarHostState.showSnackbar(t("Yeni gelişim ölçümü kaydedildi! 📈"))
                }
            }
        )
    }
}
