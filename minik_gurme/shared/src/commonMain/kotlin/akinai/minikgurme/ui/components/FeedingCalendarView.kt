package akinai.minikgurme.ui.components

import akinai.minikgurme.util.DateKit
import akinai.minikgurme.util.pad
import akinai.minikgurme.ui.theme.*
import androidx.compose.foundation.BorderStroke
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import akinai.minikgurme.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary

@Composable
fun FeedingCalendarView(
    allMeals: List<Meal>,
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    onAddMealForDate: (String) -> Unit,
    onDeleteMeal: (Long) -> Unit
) {
    val todayDate = remember { DateKit.today() }
    val todayStr = remember { DateKit.todayIso() }

    // Parse initial year and month from selectedDate
    var displayedYear by remember {
        mutableIntStateOf(
            (DateKit.parseIso(selectedDate) ?: todayDate).year
        )
    }

    var displayedMonth by remember {
        mutableIntStateOf(
            (DateKit.parseIso(selectedDate) ?: todayDate).month.ordinal
        )
    }

    // Map of dates to meals for the displayed month
    val mealsByDate = remember(allMeals) {
        allMeals.groupBy { it.date }
    }

    val selectedDayMeals = remember(selectedDate, allMeals) {
        allMeals.filter { it.date == selectedDate }
    }

    // Month & Year title formatted in Turkish (e.g., "Kasım 2026")
    val monthTitle = remember(displayedYear, displayedMonth) {
        DateKit.format(DateKit.of(displayedYear, displayedMonth + 1, 1), "MMMM yyyy").replaceFirstChar { it.uppercase() }
    }

    // Selected date formatted in Turkish (e.g., "7 Kasım 2026, Cumartesi")
    val formattedSelectedDate = remember(selectedDate) {
        DateKit.format(selectedDate, "d MMMM yyyy, EEEE") ?: selectedDate
    }

    // Calculate grid items: offset of first day (Monday = 0) and total days in month
    val calendarDays = remember(displayedYear, displayedMonth) {
        // Monday = 0, Sunday = 6
        val firstDayOfWeek = DateKit.mondayIndex(DateKit.of(displayedYear, displayedMonth + 1, 1))
        val daysInMonth = DateKit.daysInMonth(displayedYear, displayedMonth + 1)

        buildList {
            // Empty slots for previous month
            repeat(firstDayOfWeek) { add(null) }
            // Days of current month
            for (day in 1..daysInMonth) {
                val dateStr = "${displayedYear.pad(4)}-${(displayedMonth + 1).pad(2)}-${day.pad(2)}"
                add(Pair(day, dateStr))
            }
        }
    }

    val weekDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    // Monthly stats
    val monthlyMealsCount = remember(displayedYear, displayedMonth, allMeals) {
        val prefix = "${displayedYear.pad(4)}-${(displayedMonth + 1).pad(2)}"
        allMeals.count { it.date.startsWith(prefix) }
    }

    val monthlyDistinctFoods = remember(displayedYear, displayedMonth, allMeals) {
        val prefix = "${displayedYear.pad(4)}-${(displayedMonth + 1).pad(2)}"
        allMeals.filter { it.date.startsWith(prefix) }
            .flatMap { it.foodItems.split(",", "+").map { item -> item.trim().lowercase() } }
            .filter { it.isNotBlank() }
            .distinct()
            .size
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feeding_calendar_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Calendar Header Card (Month Navigation + Grid)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_month_card"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear -= 1
                            } else {
                                displayedMonth -= 1
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("Önceki Ay"),
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick "Bugün" button
                        Surface(
                            onClick = {
                                displayedYear = todayDate.year
                                displayedMonth = todayDate.month.ordinal
                                onDateSelect(todayStr)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = CoralSoft,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Bugün",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (displayedMonth == 11) {
                                displayedMonth = 0
                                displayedYear += 1
                            } else {
                                displayedMonth += 1
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = t("Sonraki Ay"),
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Day of Week Header Row (Pzt, Sal, Çar, Per, Cum, Cmt, Paz)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Grid Rows (7 columns)
            val rows = calendarDays.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 7) {
                            val dayItem = week.getOrNull(i)
                            if (dayItem != null) {
                                val (dayNum, dateStr) = dayItem
                                val isSelected = dateStr == selectedDate
                                val isCurrentDay = dateStr == todayStr
                                val dayMeals = mealsByDate[dateStr] ?: emptyList()
                                val hasMeals = dayMeals.isNotEmpty()
                                val hasAllergy = dayMeals.any { it.reaction.contains("Alerji", ignoreCase = true) }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> CoralPrimary
                                                isCurrentDay -> CoralSoft
                                                hasMeals -> SageSoft.copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onDateSelect(dateStr) }
                                        .testTag("cal_day_$dateStr"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected || isCurrentDay) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = when {
                                                isSelected -> OnAccent
                                                isCurrentDay -> CoralDark
                                                else -> TextPrimary
                                            }
                                        )

                                        // Indicator Dots for Meals & Allergies
                                        if (hasMeals) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isSelected -> OnAccent
                                                                hasAllergy -> ReactionAllergy
                                                                else -> SageGreen
                                                            }
                                                        )
                                                )
                                                if (dayMeals.size > 1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) OnAccent.copy(alpha = 0.7f) else CoralPrimary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // Legend / Açıklamalar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(SageGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Öğün Var", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ReactionAllergy))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Alerji Sinyali", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(CoralPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Seçili Gün", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }

    // 2. Monthly Summary Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$monthlyMealsCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = CoralDark
                )
                Text(
                    text = "Aylık Öğün",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(CardBorder)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$monthlyDistinctFoods",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SageDark
                )
                Text(
                    text = "Farklı Besin",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // 3. Selected Date's Meals Detail Header + Action
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "GÜNÜN BESLENME GÜNLÜĞÜ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = formattedSelectedDate,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        Button(
            onClick = { onAddMealForDate(selectedDate) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            modifier = Modifier.testTag("calendar_add_meal_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = OnAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Öğün Ekle",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OnAccent
            )
        }
    }

    // 4. List of Meals for Selected Date
    if (selectedDayMeals.isEmpty()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                akinai.minikgurme.ui.components.FoodImage("🥣", 42.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bu tarihte kayıtlı öğün yok",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Yukarıdaki 'Öğün Ekle' butonuna basarak bu tarihe besin ve porsiyon kaydedebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            selectedDayMeals.forEach { meal ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when (meal.mealType) {
                                            "Sabah" -> OchreSoft
                                            "Öğle" -> CoralSoft
                                            "Akşam" -> SageSoft
                                            else -> SlateBlueSoft
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (meal.mealType) {
                                        "Sabah" -> "☀️"
                                        "Öğle" -> "🥣"
                                        "Akşam" -> "🌙"
                                        else -> "🍎"
                                    },
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = meal.foodItems,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

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
                                            "Sevdi" -> "❤️ Sevdi"
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
                                    Spacer(modifier = Modifier.height(2.dp))
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
                            onClick = { onDeleteMeal(meal.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = t("Sil"),
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
