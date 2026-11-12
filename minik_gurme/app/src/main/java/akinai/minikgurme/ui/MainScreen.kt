package akinai.minikgurme.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.widthIn
import akinai.minikgurme.ui.theme.*
import androidx.activity.compose.BackHandler
import akinai.minikgurme.i18n.t
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
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
import akinai.minikgurme.i18n.Text
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
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.BabyRecipe
import akinai.minikgurme.data.model.FoodDirectoryItem
import akinai.minikgurme.data.model.TriedFood
import akinai.minikgurme.ui.components.AddAllergenDialog
import akinai.minikgurme.ui.components.AddDiaryEntryBottomSheet
import akinai.minikgurme.ui.components.AddGrowthRecordDialog
import akinai.minikgurme.ui.components.AddMealBottomSheet
import akinai.minikgurme.ui.components.EditProfileDialog
import akinai.minikgurme.ui.components.FoodDetailBottomSheet
import akinai.minikgurme.ui.components.ReactionNoteDialog
import akinai.minikgurme.ui.components.RecipeDetailBottomSheet
import akinai.minikgurme.ui.screens.Foods100Screen
import akinai.minikgurme.ui.screens.GrowthTrackingScreen
import akinai.minikgurme.ui.screens.GuideScreen
import akinai.minikgurme.ui.screens.ProfileScreen
import akinai.minikgurme.ui.screens.RecommendationsScreen
import akinai.minikgurme.ui.screens.RecipesScreen
import akinai.minikgurme.ui.screens.ThreeDayRuleScreen
import akinai.minikgurme.ui.screens.TodayMealsScreen
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.viewmodel.FeedingViewModel
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
    viewModel: FeedingViewModel,
    onSwitchBaby: (String) -> Unit = {},
    onAddBaby: () -> Unit = {},
    onDeleteBaby: (String) -> Unit = {},
    onReload: () -> Unit = {}
) {
    val babyProfile by viewModel.babyProfile.collectAsStateWithLifecycle()
    LaunchedEffect(babyProfile.sex) { akinai.minikgurme.ui.components.WhoStandardsData.sex = babyProfile.sex }
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
    val context = androidx.compose.ui.platform.LocalContext.current
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
    var guideSection by remember { mutableStateOf("month") } // "month" (bu ayın önerileri) | "library" (rehber)
    var previousTab by remember { mutableStateOf("today") }

    // Dialog & Sheet States
    var showAddMealSheet by remember { mutableStateOf(false) }
    var showAddDiarySheet by remember { mutableStateOf(false) }
    var showAddGrowthDialog by remember { mutableStateOf(false) }
    var showAddAllergenDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var growthRecordToEdit by remember { mutableStateOf<akinai.minikgurme.data.model.GrowthRecord?>(null) }
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
    BackHandler(enabled = currentTab in setOf("profile", "growth", "shopping", "settings", "paywall", "planner") || currentTab.startsWith("legal_")) {
        currentTab = previousTab
    }

    val navItems = listOf(
        NavigationTabItem("today", "Bugün", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_today"),
        NavigationTabItem("foods100", "100 Gıda", Icons.Filled.Stars, Icons.Outlined.Stars, "nav_foods100"),
        NavigationTabItem("recipes", "Tarifler", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_recipes"),
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
                            painter = androidx.compose.ui.res.painterResource(akinai.minikgurme.R.drawable.logo_emblem),
                            contentDescription = "Minik Gurme",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .testTag("app_logo")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Minik Gurme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = java.text.SimpleDateFormat("d MMMM, EEE", java.util.Locale(akinai.minikgurme.i18n.AppLanguage.current)).format(java.util.Date()),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
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
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        androidx.compose.material3.IconButton(
                            onClick = {
                                if (currentTab != "shopping") { previousTab = currentTab; currentTab = "shopping" }
                            },
                            modifier = Modifier.size(40.dp).testTag("top_shopping")
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 104.dp),
                                color = if (currentTab == "profile") OnAccent else CoralDark
                            )
                        }
                    }
                    androidx.compose.material3.IconButton(
                        onClick = {
                            if (currentTab != "settings") { previousTab = currentTab; currentTab = "settings" }
                        },
                        modifier = Modifier.size(40.dp).testTag("top_settings")
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Settings,
                            contentDescription = t("Ayarlar"),
                            tint = if (currentTab == "settings") CoralPrimary else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = CardBorder)
            NavigationBar(
                containerColor = CardSurface,
                tonalElevation = 0.dp,
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
                        onEditGrowthRecord = { growthRecordToEdit = it },
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
                            viewModel.addRecipeToMeals(recipe)
                            scope.launch {
                                snackbarHostState.showSnackbar(t("${recipe.title} bugünkü öğün tablosuna eklendi!"))
                            }
                        }
                    )

                    "guide", "recommendations" -> Column(modifier = Modifier.fillMaxSize()) {
                        // Rehber: "Bu Ay" (yaşa göre öneriler + 3 gün kuralı) ve "Rehber" (bilgi bankası) tek sekmede
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardSurface)
                                .padding(4.dp)
                        ) {
                            listOf("month" to "Bu Ay", "library" to "Rehber").forEach { (key, label) ->
                                val sel = guideSection == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sel) CoralPrimary else Color.Transparent)
                                        .clickable { guideSection = key }
                                        .padding(vertical = 9.dp)
                                        .testTag("guide_section_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, color = if (sel) OnAccent else TextSecondary, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (guideSection == "month") {
                        RecommendationsScreen(
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
                        },
                        allTests = allAllergens,
                        onStartAllergen = { name ->
                            viewModel.addAllergenTest(name, "")
                            scope.launch { snackbarHostState.showSnackbar(t("3 günlük deneme başladı: $name")) }
                        }
                    )
                            } else {
                        GuideScreen(
                        selectedMonth = selectedGuideMonth,
                        selectedTab = selectedGuideTab,
                        onMonthSelect = { viewModel.setSelectedGuideMonth(it) },
                        onTabSelect = { viewModel.setSelectedGuideTab(it) }
                    )
                            }
                        }
                    }

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

                    "settings" -> akinai.minikgurme.ui.screens.SettingsScreen(
                        versionName = remember {
                            runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
                        },
                        onBack = { currentTab = previousTab },
                        onEditProfile = { showEditProfileDialog = true },
                        onResetStatistics = {
                            viewModel.resetFeedingStatistics()
                            scope.launch { snackbarHostState.showSnackbar(t("İstatistikler sıfırlandı.")) }
                        },
                        onClearShopping = { viewModel.clearAllShopping() },
                        onOpenPaywall = { currentTab = "paywall" },
                        onOpenPlanner = { currentTab = "planner" },
                        onOpenLegal = { currentTab = "legal_$it" },
                        onSwitchBaby = onSwitchBaby,
                        onAddBaby = onAddBaby,
                        onDeleteBaby = onDeleteBaby,
                        onDataImported = onReload
                    )
                    "paywall" -> akinai.minikgurme.ui.screens.PaywallScreen(
                        onBack = { currentTab = "settings" },
                        onOpenTerms = { currentTab = "legal_terms" },
                        onOpenPrivacy = { currentTab = "legal_privacy" }
                    )
                    "planner" -> akinai.minikgurme.ui.screens.WeeklyPlannerScreen(
                        ageMonths = babyProfile.ageMonths,
                        onBack = { currentTab = "settings" },
                        onOpenRecipe = { selectedRecipeForDetail = it },
                        onAddToShopping = { recipes ->
                            recipes.forEach { viewModel.addIngredientsToShopping(it, 1f) }
                            scope.launch { snackbarHostState.showSnackbar(t("Malzemeler alışveriş listesine eklendi")) }
                        }
                    )
                    "legal_privacy", "legal_terms", "legal_licenses" -> akinai.minikgurme.ui.screens.LegalScreen(
                        kind = tab.removePrefix("legal_"),
                        onBack = { currentTab = "settings" }
                    )
                    "shopping" -> akinai.minikgurme.ui.screens.ShoppingListScreen(
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
                        onEditRecord = { growthRecordToEdit = it },
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
                    snackbarHostState.showSnackbar(t("${food.name} bugünkü öğünlere eklendi!"))
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
                    snackbarHostState.showSnackbar(t("${recipe.title} bugünkü öğün tablosuna eklendi!"))
                }
            },
            onAddToShopping = { factor, ingredient ->
                viewModel.addIngredientsToShopping(recipe, factor, ingredient)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (ingredient == null) t("Malzemeler alışveriş listesine eklendi")
                        else t("Alışveriş listesine eklendi")
                    )
                }
            },
            onAddToDiary = {
                viewModel.addRecipeToDiary(recipe)
                selectedRecipeForDetail = null
                scope.launch {
                    snackbarHostState.showSnackbar(t("${recipe.title} Beslenme Günlüğüne kaydedildi!"))
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
                    snackbarHostState.showSnackbar(t("Beslenme günlüğüne kaydedildi!"))
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
            initialSex = babyProfile.sex,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, age, startDate, sex ->
                viewModel.updateProfile(name, age, startDate, sex = sex)
                showEditProfileDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(t("Bebek profili güncellendi!"))
                }
            }
        )
    }

    // Ölçümü düzenle (kilo/boy kutularındaki kalem)
    growthRecordToEdit?.let { rec ->
        AddGrowthRecordDialog(
            currentBabyAgeMonths = rec.ageMonths,
            editing = rec,
            onDismiss = { growthRecordToEdit = null },
            onSave = { date, weight, height, head, age, notes ->
                viewModel.updateGrowthRecord(
                    rec.copy(date = date, weightKg = weight, heightCm = height, headCircumferenceCm = head, ageMonths = age, notes = notes)
                )
                growthRecordToEdit = null
                scope.launch { snackbarHostState.showSnackbar(t("Ölçüm güncellendi.")) }
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
                    snackbarHostState.showSnackbar(t("Yeni gelişim ölçümü kaydedildi!"))
                }
            }
        )
    }
}
