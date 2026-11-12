package akinai.minikgurme.ui.screens

import akinai.minikgurme.util.DateKit
import akinai.minikgurme.util.fmt
import akinai.minikgurme.ui.theme.*
import androidx.compose.foundation.BorderStroke
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import akinai.minikgurme.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.R
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.FeedingDiaryEntry
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import androidx.compose.material.icons.filled.ChevronRight
import akinai.minikgurme.ui.components.FeedingCalendarView
import akinai.minikgurme.ui.components.FeedingDiaryView
import akinai.minikgurme.ui.components.GrowthChartCard
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.OchreBorder
import akinai.minikgurme.ui.theme.OchreDark
import akinai.minikgurme.ui.theme.OchreSoft
import akinai.minikgurme.ui.theme.OchreWarm
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.ReactionAllergySoft
import akinai.minikgurme.ui.theme.ReactionLoved
import akinai.minikgurme.ui.theme.ReactionLovedSoft
import akinai.minikgurme.ui.theme.ReactionNeutral
import akinai.minikgurme.ui.theme.ReactionNeutralSoft
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary
import java.util.Date

@Composable
fun TodayMealsScreen(
    profile: BabyProfile,
    meals: List<Meal>,
    allMeals: List<Meal> = meals,
    diaryEntries: List<FeedingDiaryEntry> = emptyList(),
    diarySearchQuery: String = "",
    growthRecords: List<GrowthRecord> = emptyList(),
    selectedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onDateSelect: (String) -> Unit = {},
    onDiarySearchChange: (String) -> Unit = {},
    onOpenAddMeal: () -> Unit,
    onOpenAddDiaryEntry: () -> Unit = onOpenAddMeal,
    onOpenGrowthTracking: () -> Unit = {},
    onOpenAddGrowthRecord: () -> Unit = {},
    onEditGrowthRecord: (akinai.minikgurme.data.model.GrowthRecord) -> Unit = {},
    onOpenRecipes: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    onDeleteMeal: (Long) -> Unit,
    onDeleteDiaryEntry: (Long) -> Unit = onDeleteMeal
) {
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }

    val isToday = remember(selectedDate) {
        val todayStr = DateKit.todayIso()
        selectedDate == todayStr
    }

    val formattedDisplayDate = remember(selectedDate) {
        try {
            val formatted = DateKit.format(selectedDate, "d MMMM EEEE")
            if (formatted != null) {
                formatted
            } else {
                selectedDate
            }
        } catch (_: Exception) {
            selectedDate
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 1. Kişisel karşılama kartı (bugünün özeti)
        item {
            val latest = growthRecords.maxByOrNull { it.date }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(HeroGradient))
                    .padding(20.dp)
                    .testTag("today_greeting")
            ) {
                Column {
                    Text(
                        text = if (profile.name.isNotBlank()) "Merhaba, ${profile.name}!" else "Merhaba!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnHero
                    )
                    Text(
                        text = "${profile.ageMonths}. ay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnHero.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GreetingStat("Bugün", "${meals.size} öğün", Modifier.weight(1f))
                        GreetingStat("Kilo", latest?.let { "${(it.weightKg).fmt(2)} kg" } ?: "–", Modifier.weight(1f).clickable { onOpenGrowthTracking() })
                        GreetingStat("Boy", latest?.let { "${(it.heightCm).fmt(1)} cm" } ?: "–", Modifier.weight(1f).clickable { onOpenGrowthTracking() })
                    }
                }
            }
        }

        run {
            // 3. Date Navigation Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousDay,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("prev_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = t("Önceki Gün"),
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onToday() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedDisplayDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (!isToday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = CoralSoft,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Bugün",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CoralDark,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNextDay,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("next_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = t("Sonraki Gün"),
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. Elevated Primary Action Button
            item {
                Button(
                    onClick = onOpenAddMeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("add_meal_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralPrimary,
                        contentColor = OnAccent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yeni Öğün / Tadım Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                        text = "GÜNÜN ÖĞÜN DETAYLARI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${meals.size} Kayıt",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // Meals List or Empty State
            if (meals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(CoralSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                akinai.minikgurme.ui.components.FoodImage("🥣", 39.dp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Bu tarihte henüz öğün girilmedi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bebeğinize sunduğunuz tadımları ekleyerek alerjen, kıvam ve sevme durumunu kolayca kaydedin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onOpenAddMeal,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                            ) {
                                Text("İlk Öğünü Kaydet", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealItemCard(
                        meal = meal,
                        onDelete = { mealToDelete = meal }
                    )
                }
            }

            // 5. Feeding Tip Box
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = OchreSoft,
                    border = BorderStroke(1.dp, OchreWarm.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OchreWarm.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OchreDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "İpucu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OchreDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Yeni besinleri daima sabah veya öğle saatlerinde deneyin; böylece olası alerji belirtilerini gün boyunca gözlemleyebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OchreText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    mealToDelete?.let { meal ->
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            title = {
                Text(
                    text = "Öğünü Sil",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "${meal.mealType} - ${meal.foodItems} öğünü silinecek. Emin misiniz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMeal(meal.id)
                        mealToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReactionAllergy)
                ) {
                    Text("Sil", color = OnAccent)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mealToDelete = null }) {
                    Text("Vazgeç", color = TextPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun MealItemCard(
    meal: Meal,
    onDelete: () -> Unit
) {
    val mealColor = when (meal.mealType) {
        "Sabah" -> OchreSoft
        "Öğle" -> CoralSoft
        "Akşam" -> SageSoft
        else -> SlateBlueSoft
    }

    val mealIcon = when (meal.mealType) {
        "Sabah" -> "☀️"
        "Öğle" -> "🥣"
        "Akşam" -> "🌙"
        else -> "🍎"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_card_${meal.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(mealColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mealIcon, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CoralSoft,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = meal.mealType,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = meal.foodItems,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = meal.texture,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(text = "•", color = CardBorder)
                        Text(
                            text = when (meal.reaction) {
                                "Sevdi" -> "❤️ Çok Sevdi"
                                "İstemedi" -> "😣 İstemedi"
                                "Alerji Sinyali" -> "🚨 Alerji"
                                else -> "😐 Nötr"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (meal.reaction) {
                                "Sevdi" -> SageDark
                                "Alerji Sinyali" -> ReactionAllergy
                                else -> CoralDark
                            }
                        )
                    }

                    if (meal.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${meal.notes}”",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = t("Sil"),
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GreetingStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(OnHero.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnHero.copy(alpha = 0.85f), maxLines = 1)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnHero, maxLines = 1, softWrap = false)
    }
}
