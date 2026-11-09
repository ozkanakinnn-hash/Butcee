package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke
import com.example.i18n.t
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
import com.example.i18n.Text
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
import com.example.R
import com.example.data.model.BabyProfile
import com.example.data.model.FeedingDiaryEntry
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
import androidx.compose.material.icons.filled.ChevronRight
import com.example.ui.components.FeedingCalendarView
import com.example.ui.components.FeedingDiaryView
import com.example.ui.components.GrowthChartCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.OchreBorder
import com.example.ui.theme.OchreDark
import com.example.ui.theme.OchreSoft
import com.example.ui.theme.OchreWarm
import com.example.ui.theme.ReactionAllergy
import com.example.ui.theme.ReactionAllergySoft
import com.example.ui.theme.ReactionLoved
import com.example.ui.theme.ReactionLovedSoft
import com.example.ui.theme.ReactionNeutral
import com.example.ui.theme.ReactionNeutralSoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onOpenRecipes: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    onDeleteMeal: (Long) -> Unit,
    onDeleteDiaryEntry: (Long) -> Unit = onDeleteMeal
) {
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }
    var viewMode by remember { mutableStateOf("timeline") } // "timeline" vs "calendar"

    val isToday = remember(selectedDate) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDate == todayStr
    }

    val formattedDisplayDate = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                SimpleDateFormat("d MMMM EEEE", Locale(com.example.i18n.AppLanguage.current)).format(date)
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

        // 1. Realistic Baby Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Realistic Baby Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_baby_hero),
                            contentDescription = t("Bebeğin Ek Gıda Macerası"),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                        // Overlay Text & Age Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Surface(
                                    color = CoralPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "GÜNLÜK BESLENME REHBERİ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${com.example.i18n.trGenitive(profile.name)} Menüsü 👶",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${profile.ageMonths}. Ay",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CoralDark
                                    )
                                }
                            }
                        }
                    }

                    // Sub-info Bar
                    Surface(
                        color = CoralSoft.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (meals.isNotEmpty()) SuccessGreen else OchreWarm)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (meals.isEmpty()) "Henüz bu güne ait öğün girilmedi" else "Bugün toplam ${meals.size} öğün kaydedildi",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Haftalık Pediatrik Bilgi Bankası Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenGuide() }
                    .testTag("dashboard_kb_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OchreBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(OchreSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📚", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Haftalık Pediatrik Bilgi Bankası",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SageSoft,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${profile.ageMonths}. Ay",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bu haftanın klinik hedefleri, kıvam aşaması ve güvenli tarifleri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = t("Bilgi Bankası"),
                        tint = OchreDark
                    )
                }
            }
        }

        // Growth Chart Summary Card on Dashboard
        item {
            GrowthChartCard(
                records = growthRecords,
                meals = allMeals,
                diaryEntries = diaryEntries,
                isCompact = true,
                onOpenFullTracking = onOpenGrowthTracking,
                onAddNewRecord = onOpenAddGrowthRecord
            )
        }

        // 2. View Mode Toggle (Günlük Akış vs. Beslenme Günlüğü vs. Beslenme Takvimi)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Akış
                    Surface(
                        onClick = { viewMode = "timeline" },
                        shape = RoundedCornerShape(14.dp),
                        color = if (viewMode == "timeline") CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_timeline_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = null,
                                tint = if (viewMode == "timeline") Color.White else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Akış",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (viewMode == "timeline") FontWeight.Bold else FontWeight.Medium,
                                color = if (viewMode == "timeline") Color.White else TextPrimary
                            )
                        }
                    }

                    // Beslenme Günlüğü
                    Surface(
                        onClick = { viewMode = "diary" },
                        shape = RoundedCornerShape(14.dp),
                        color = if (viewMode == "diary") CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("mode_diary_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = if (viewMode == "diary") Color.White else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Günlük 📖",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (viewMode == "diary") FontWeight.Bold else FontWeight.Medium,
                                color = if (viewMode == "diary") Color.White else TextPrimary
                            )
                        }
                    }

                    // Takvim
                    Surface(
                        onClick = { viewMode = "calendar" },
                        shape = RoundedCornerShape(14.dp),
                        color = if (viewMode == "calendar") CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("mode_calendar_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (viewMode == "calendar") Color.White else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Takvim",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (viewMode == "calendar") FontWeight.Bold else FontWeight.Medium,
                                color = if (viewMode == "calendar") Color.White else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Conditional Content: Diary vs Calendar vs Timeline View
        if (viewMode == "diary") {
            item {
                FeedingDiaryView(
                    profile = profile,
                    diaryEntries = diaryEntries,
                    selectedDate = selectedDate,
                    searchQuery = diarySearchQuery,
                    onSearchChange = onDiarySearchChange,
                    onDateSelect = onDateSelect,
                    onOpenAddEntry = onOpenAddDiaryEntry,
                    onDeleteEntry = onDeleteDiaryEntry,
                    onOpenRecipes = onOpenRecipes
                )
            }
        } else if (viewMode == "calendar") {
            item {
                FeedingCalendarView(
                    allMeals = allMeals,
                    selectedDate = selectedDate,
                    onDateSelect = onDateSelect,
                    onAddMealForDate = { onOpenAddMeal() },
                    onDeleteMeal = onDeleteMeal
                )
            }
        } else {
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
                        contentColor = Color.White
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
                                Text(text = "🥣", fontSize = 30.sp)
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
                                text = "Doktor Tavsiyesi",
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
                    Text("Sil", color = Color.White)
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
