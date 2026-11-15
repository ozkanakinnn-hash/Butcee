package akinai.minikgurme.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import akinai.minikgurme.ui.theme.*
import androidx.compose.foundation.BorderStroke
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import akinai.minikgurme.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.data.guide.PediatricRecipesData
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.BabyRecipe
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.OchreDark
import akinai.minikgurme.ui.theme.OchreSoft
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.SageBorder
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipesScreen(
    profile: BabyProfile,
    dailyMenu: Map<String, BabyRecipe>,
    favoriteRecipeIds: List<String>,
    searchQuery: String,
    selectedAgeFilter: String,
    selectedStyleFilter: String,
    onSearchChange: (String) -> Unit,
    onAgeFilterChange: (String) -> Unit,
    onStyleFilterChange: (String) -> Unit,
    onShuffleMenu: () -> Unit,
    onSelectRecipe: (BabyRecipe) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuickAddToDiary: (BabyRecipe) -> Unit = {},
    shoppingCount: Int = 0,
    onOpenShopping: () -> Unit = {},
    isPlus: Boolean = false
) {
    var showFilters by remember { mutableStateOf(false) }
    var menuSeed by rememberSaveable { mutableIntStateOf(0) }
    // Günün menüsü: Plus'ta haftalık planın bugünü varsa o, yoksa yaşa uygun günlük seçim (Yenile ile değişir)
    val todayMenu = remember(profile.ageMonths, menuSeed, isPlus) {
        val fromPlan = if (isPlus) akinai.minikgurme.ui.screens.WeeklyPlan.today() else emptyMap()
        if (fromPlan.size == 3) fromPlan else akinai.minikgurme.data.guide.PediatricRecipesData.menuFor(profile.ageMonths, menuSeed)
    }
    val menuFromPlan = isPlus && akinai.minikgurme.ui.screens.WeeklyPlan.today().size == 3
    val filteredRecipes by remember(searchQuery, selectedAgeFilter, selectedStyleFilter, favoriteRecipeIds, profile.ageMonths) {
        derivedStateOf {
            PediatricRecipesData.allRecipes.filter { recipe ->
                val matchesAge = when (selectedAgeFilter) {
                    "Bebeğime Uygun" -> recipe.ageMonths <= profile.ageMonths
                    "Tüm Yaşlar" -> true
                    "6+ Aylık" -> recipe.ageMonths == 6
                    "7+ Aylık" -> recipe.ageMonths == 7
                    "8+ Aylık" -> recipe.ageMonths == 8
                    "9+ Aylık" -> recipe.ageMonths == 9
                    "10+ Aylık" -> recipe.ageMonths == 10
                    "11+ Aylık" -> recipe.ageMonths == 11
                    "12+ Aylık" -> recipe.ageMonths >= 12
                    "Favorilerim" -> favoriteRecipeIds.contains(recipe.id)
                    else -> true
                }

                val matchesStyle = when (selectedStyleFilter) {
                    "Tümü" -> true
                    "Püre" -> recipe.textureStyle.contains("Püre", ignoreCase = true)
                    "BLW" -> recipe.textureStyle.contains("BLW", ignoreCase = true) || recipe.tags.any { it.contains("BLW", ignoreCase = true) }
                    "Kahvaltı" -> recipe.mealType.contains("Kahvaltı", ignoreCase = true)
                    "Çorba & Öğle" -> recipe.mealType.contains("Öğle", ignoreCase = true) || recipe.tags.any { it.contains("Çorba", ignoreCase = true) }
                    else -> true
                }

                val matchesSearch = searchQuery.isBlank() ||
                        recipe.title.contains(searchQuery, ignoreCase = true) ||
                        recipe.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                        recipe.ingredients.any { it.name.contains(searchQuery, ignoreCase = true) }

                matchesAge && matchesStyle && matchesSearch
            }
        }
    }

    val ageFilters = listOf("Bebeğime Uygun", "Tüm Yaşlar", "6+ Aylık", "7+ Aylık", "8+ Aylık", "9+ Aylık", "10+ Aylık", "11+ Aylık", "12+ Aylık", "Favorilerim")
    val styleFilters = listOf("Tümü", "BLW", "Püre", "Kahvaltı", "Çorba & Öğle")

    if (showFilters) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFilters = false },
            confirmButton = { TextButton(onClick = { showFilters = false }, modifier = Modifier.testTag("recipe_filter_done")) { Text("Tamam", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { onAgeFilterChange("Bebeğime Uygun"); onStyleFilterChange("Tümü"); showFilters = false }) { Text("Sıfırla") } },
            title = { Text("Filtrele", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Yaş", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ageFilters.filter { it != "Favorilerim" }.forEach { f -> FilterOption(f, selectedAgeFilter == f, "recipe_age_opt_$f") { onAgeFilterChange(f) } }
                    }
                    Text("Tür", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        styleFilters.forEach { f -> FilterOption(f, selectedStyleFilter == f, "recipe_style_opt_$f") { onStyleFilterChange(f) } }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recipes_screen"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. "Günün Menüsü" Personalized Menu Section (Matching Screenshot 8)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_menu_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        akinai.minikgurme.ui.components.FoodImage("🍽️", 26.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Başlık kalan alanı alır; düğmeler sabit boyutta kalır (büyük yazı boyutunda da bozulmaz)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Günün Menüsü",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = if (menuFromPlan) t("Haftalık menünüzden") else "${profile.name} için yaşa uygun örnek menü",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        // Alışveriş listesi
                        androidx.compose.material3.BadgedBox(
                            badge = { if (shoppingCount > 0) androidx.compose.material3.Badge(containerColor = CoralPrimary) { androidx.compose.material3.Text("$shoppingCount") } }
                        ) {
                            Surface(
                                onClick = onOpenShopping,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp).testTag("open_shopping")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = t("Alışveriş Listesi"), tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        // Menüyü yenile
                        Surface(
                            onClick = { menuSeed++; onShuffleMenu() },
                            shape = CircleShape,
                            color = CoralSoft,
                            modifier = Modifier.size(40.dp).testTag("shuffle_menu_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Refresh, contentDescription = t("Yenile"), tint = CoralDark, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Daily Menu Items Grid (Kahvaltı, Öğle, İkindi/Akşam)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Kahvaltı", "Öğle", "Akşam").forEach { mealTime ->
                            val recipe = todayMenu[mealTime]
                            if (recipe != null) {
                                Surface(
                                    onClick = { onSelectRecipe(recipe) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = when (mealTime) {
                                        "Kahvaltı" -> OchreSoft
                                        "Öğle" -> CoralSoft
                                        else -> SageSoft
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when (mealTime) {
                                            "Kahvaltı" -> OchreBorder
                                            "Öğle" -> CoralBorder
                                            else -> SageBorder
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = mealTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (mealTime) {
                                                "Kahvaltı" -> OchreDark
                                                "Öğle" -> CoralDark
                                                else -> SageDark
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        akinai.minikgurme.ui.components.FoodImage(recipe.emoji, 36.dp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = recipe.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = recipe.prepTime,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Search Bar (Matching Screenshot 7)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Tariflerde ara (örnek: kabak, brokoli, köfte)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = t("Ara"),
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = t("Temizle"),
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recipe_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // 3. Filtreler: hızlı seçimler + "Filtrele" (yaş ve tür seçenekleri iletişim kutusunda)
        item {
            val extra = (if (selectedAgeFilter !in setOf("Bebeğime Uygun", "Favorilerim")) 1 else 0) + (if (selectedStyleFilter != "Tümü") 1 else 0)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Bebeğime Uygun", "Favorilerim").forEach { quick ->
                    val isSelected = selectedAgeFilter == quick
                    Surface(
                        onClick = { onAgeFilterChange(if (isSelected) "Tüm Yaşlar" else quick) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else CardBorder),
                        modifier = Modifier.testTag("recipe_age_chip_$quick")
                    ) {
                        Text(
                            text = quick,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAccent else TextPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = { showFilters = true },
                    shape = RoundedCornerShape(14.dp),
                    color = if (extra > 0) SlateBlue else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (extra > 0) Color.Transparent else CardBorder),
                    modifier = Modifier.testTag("recipe_filter_btn")
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = if (extra > 0) OnAccent else TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            t("Filtrele") + if (extra > 0) " ($extra)" else "", translate = false,
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                            color = if (extra > 0) OnAccent else TextPrimary, maxLines = 1
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEBEK DOSTU TARİFLER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${filteredRecipes.size} Tarif",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // 5. Recipe Cards List (Matching Screenshot 7: Görsel, Başlık, Yaş, Süre, Kalp ikonu)
        items(filteredRecipes, key = { it.id }) { recipe ->
            val isFavorite = favoriteRecipeIds.contains(recipe.id)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRecipe(recipe) }
                    .testTag("recipe_card_${recipe.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CoralLight),
                                contentAlignment = Alignment.Center
                            ) {
                                akinai.minikgurme.ui.components.FoodImage(recipe.emoji, 36.dp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = CoralSoft,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = recipe.ageLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (recipe.ageMonths <= profile.ageMonths && selectedAgeFilter != "Bebeğime Uygun") {
                                        Surface(
                                            color = SageSoft,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, SageBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SageDark,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Bebeğinize Uygun",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageDark,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = recipe.prepTime,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Favorite heart button
                        IconButton(
                            onClick = { onToggleFavorite(recipe.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = t("Favori"),
                                tint = if (isFavorite) ReactionAllergy else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Texture & Tags & Quick Add to Diary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlateBlueSoft
                            ) {
                                Text(
                                    text = recipe.textureStyle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            recipe.tags.take(1).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Quick 1-tap "Günlüğe Ekle 📖" button
                        Surface(
                            onClick = { onQuickAddToDiary(recipe) },
                            shape = RoundedCornerShape(10.dp),
                            color = CoralSoft,
                            border = BorderStroke(1.dp, CoralBorder),
                            modifier = Modifier.testTag("quick_add_to_diary_${recipe.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Öğüne Ekle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FilterOption(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CoralPrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else CardBorder),
        modifier = Modifier.testTag(tag)
    ) {
        Text(
            label, style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) OnAccent else TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
