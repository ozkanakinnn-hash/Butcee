package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import com.example.i18n.t
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.guide.FoodDirectoryData
import com.example.data.model.BabyProfile
import com.example.data.model.FoodDirectoryItem
import com.example.data.model.TriedFood
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.OchreDark
import com.example.ui.theme.OchreSoft
import com.example.ui.theme.ReactionAllergy
import com.example.ui.theme.ReactionAllergySoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun Foods100Screen(
    profile: BabyProfile,
    triedFoods: List<TriedFood>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onToggleTried: (foodId: String, foodName: String, isTried: Boolean) -> Unit,
    onSelectFood: (FoodDirectoryItem) -> Unit,
    onOpenReactionDialog: (foodId: String, foodName: String, currentReaction: String, currentSymptoms: String, currentNotes: String) -> Unit
) {
    val triedMap = remember(triedFoods) {
        triedFoods.associateBy { it.foodId }
    }

    val triedCount = triedFoods.count { it.isTried }
    val totalGoal = 100
    val progressFraction = (triedCount.toFloat() / totalGoal.toFloat()).coerceIn(0f, 1f)

    // Suggest next food (untried)
    val nextSuggestion = remember(triedMap) {
        FoodDirectoryData.allFoods.firstOrNull { !triedMap.containsKey(it.id) || !triedMap[it.id]!!.isTried }
            ?: FoodDirectoryData.allFoods.first()
    }

    val filteredFoods by remember(searchQuery, selectedCategory, triedMap) {
        derivedStateOf {
            FoodDirectoryData.allFoods.filter { food ->
                val matchesCategory = when (selectedCategory) {
                    "Tümü" -> true
                    "Denenenler" -> triedMap[food.id]?.isTried == true
                    "Denenmeyenler" -> triedMap[food.id]?.isTried != true
                    else -> food.category.contains(selectedCategory, ignoreCase = true)
                }

                val matchesSearch = searchQuery.isBlank() ||
                        food.name.contains(searchQuery, ignoreCase = true) ||
                        food.nutrients.any { it.contains(searchQuery, ignoreCase = true) } ||
                        food.summary.contains(searchQuery, ignoreCase = true)

                matchesCategory && matchesSearch
            }
        }
    }

    val categories = listOf(
        "Tümü",
        "Denenenler",
        "Sebzeler",
        "Meyveler",
        "Tahıllar & Baklagiller",
        "Protein & Et",
        "Süt Ürünleri",
        "Tohumlar & Sağlıklı Yağlar"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("foods_100_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. "1 Yaşına Kadar 100 Gıda" ilerleme kartı
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rainbow_progress_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1 Yaşına Kadar 100 Gıda",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Bebeğinizin tattığı besinleri işaretleyin",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                maxLines = 1,
                                softWrap = false,
                                text = "$triedCount / $totalGoal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = SageDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Rainbow Gradient Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        RainbowGradient
                                    )
                                )
                        )
                    }

                    // "Sonra bunu dene:" recommendation chip (Matching Screenshot 9)
                    Surface(
                        onClick = { onSelectFood(nextSuggestion) },
                        shape = RoundedCornerShape(16.dp),
                        color = OchreSoft,
                        border = BorderStroke(1.dp, OchreBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.example.ui.components.FoodImage(nextSuggestion.emoji, 34.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Sonra bunu dene:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OchreDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${nextSuggestion.name} (${nextSuggestion.ageDisplay})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = OchreDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Bar (Matching Screenshot 10)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Besin ara",
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
                    .testTag("food_search_field"),
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

        // 3. Category Filter Chips (Matching Screenshot 10)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        onClick = { onCategoryChange(category) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.Transparent else CardBorder
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAccent else TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        // Section Title with count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TADILAN VE TADILACAK BESİNLER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${filteredFoods.size} Besin Listelendi",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // 4. Food List Items (Matching Screenshot 10 with Checkbox, Emoji, Reaction badge and Info button)
        items(filteredFoods, key = { it.id }) { food ->
            val triedItem = triedMap[food.id]
            val isChecked = triedItem?.isTried == true

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) SageSoft.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isChecked) SageBorder else CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFood(food) }
                    .testTag("food_item_${food.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            onToggleTried(food.id, food.name, isChecked)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SageGreen,
                            uncheckedColor = CardBorder,
                            checkmarkColor = OnAccent
                        ),
                        modifier = Modifier.testTag("checkbox_${food.id}")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Food Emoji + Name + Category + Age
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ui.components.FoodImage(food.emoji, 31.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (food.isAllergen) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = ReactionAllergySoft,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Alerjen",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ReactionAllergy,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${food.category} • ${food.ageDisplay}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Reaction status button / emoji (Matching Screenshot 10: heart, poop, allergy...)
                    if (isChecked) {
                        Surface(
                            onClick = {
                                onOpenReactionDialog(
                                    food.id,
                                    food.name,
                                    triedItem?.reaction ?: "loved",
                                    triedItem?.symptoms ?: "",
                                    triedItem?.reactionNotes ?: ""
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = when (triedItem?.reaction) {
                                "loved" -> SageSoft
                                "allergy" -> ReactionAllergySoft
                                "disliked" -> OchreSoft
                                else -> SlateBlueSoft
                            },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = when (triedItem?.reaction) {
                                    "loved" -> "❤️ Sevdi"
                                    "allergy" -> "🚨 Alerji"
                                    "disliked" -> "😣 Sevmedi"
                                    else -> "😐 Nötr"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (triedItem?.reaction) {
                                    "loved" -> SageDark
                                    "allergy" -> ReactionAllergy
                                    "disliked" -> OchreDark
                                    else -> SlateBlue
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Info button to open full serving & safety sheet
                    IconButton(
                        onClick = { onSelectFood(food) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = t("Nasıl Sunulur"),
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
