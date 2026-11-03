package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.IslamicDays
import com.example.data.RamadanCalendar
import com.example.data.SpecialCalendarDay
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.PrivacyFooter
import java.util.Calendar
import java.util.Locale

@Composable
fun FastingCalendarScreen(
  prefs: AppPreferences,
  onOpenFastingTracker: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var calendarYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
  var calendarMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-based
  var fastingDates by remember { mutableStateOf(prefs.getFastingDates()) }
  var selectedDaySpecialInfo by remember { mutableStateOf<SpecialCalendarDay?>(null) }

  val monthNames = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
  )
  val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

  // Special Religious Days & Kandils map for year 2026/2027
  val specialDaysMap = remember(calendarYear) {
    IslamicDays.forYear(calendarYear)
  }

  // Yıl listesi: dini haftalar tek satırda (tarih aralığıyla), diğer günler tek tek
  val specialList = remember(calendarYear) {
    val weekStarts = linkedMapOf<String, SpecialCalendarDay>()
    val weekEnds = mutableMapOf<String, String>()
    val singles = mutableListOf<SpecialCalendarDay>()
    specialDaysMap.values.sortedBy { it.dateKey }.forEach { day ->
      val parts = day.title.split(" • ")
      parts.filter { it.endsWith("Haftası") }.forEach { weekName ->
        if (weekName !in weekStarts) weekStarts[weekName] = SpecialCalendarDay(day.dateKey, weekName, "Dini Hafta", if (weekName.contains("Cami")) "🕌" else "🌹")
        weekEnds[weekName] = day.dateKey
      }
      val rest = parts.filterNot { it.endsWith("Haftası") }
      if (rest.isNotEmpty()) singles.add(day.copy(title = rest.joinToString(" • ")))
    }
    val weeks = weekStarts.map { (name, start) ->
      start.copy(category = "Dini Hafta • ${formatDateStr(start.dateKey)} – ${formatDateStr(weekEnds[name] ?: start.dateKey)}")
    }
    (singles + weeks).sortedBy { it.dateKey }
  }

  // Statistics Calculation
  val totalThisYear = fastingDates.count { it.startsWith("$calendarYear-") }
  // Bu yılın Ramazan günleri (Diyanet tablosu) içinde tutulan oruçlar
  val ramadanRange = RamadanCalendar.rangeForYear(calendarYear)
  val ramadanLength = ramadanRange?.let { (start, end) ->
    val st = start.split("-").map { it.toInt() }
    val en = end.split("-").map { it.toInt() }
    val c1 = Calendar.getInstance().apply { clear(); set(st[0], st[1] - 1, st[2]) }
    val c2 = Calendar.getInstance().apply { clear(); set(en[0], en[1] - 1, en[2]) }
    (((c2.timeInMillis - c1.timeInMillis) / 86_400_000L) + 1).toInt()
  } ?: 30
  val totalInRamadan = fastingDates.count { key ->
    ramadanRange != null && key >= ramadanRange.first && key <= ramadanRange.second
  }
  // Kaza borcu: Oruç Takibi'ndeki sayaç (tutamadım / bozuldu işaretleri)
  val kazaDebt = prefs.kazaDebtCount

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("fasting_calendar_screen")
  ) {
    // Header Title
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Oruç Takvimi & Dini Günler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Günlere dokunarak oruç işaretleyin; kandil, bayram ve dini günleri takip edin.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Summary Metric Cards
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MetricCard(
          title = "Bu Yıl Toplam",
          value = "$totalThisYear Gün",
          subtitle = "Tüm Oruçlar",
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Ramazan",
          value = "$totalInRamadan / $ramadanLength",
          subtitle = "Farz Oruç",
          color = Color(0xFF2D5A43),
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Kaza Borcu",
          value = "$kazaDebt Gün",
          subtitle = "Kalan Oruç",
          color = if (kazaDebt > 0) Color(0xFF8A3B2B) else Color(0xFF2D5A43),
          modifier = Modifier.weight(1f)
        )
      }
    }

    if (onOpenFastingTracker != null) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onOpenFastingTracker() },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("🤲", fontSize = 18.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Bugünkü Oruç Durumunu Kaydet / Kaza Sayacı",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Text("Aç ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // Interactive Calendar Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Month Navigator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = {
                if (calendarMonth == 0) {
                  calendarMonth = 11
                  calendarYear -= 1
                } else {
                  calendarMonth -= 1
                }
                selectedDaySpecialInfo = null
              }
            ) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Önceki Ay")
            }

            Text(
              text = "${monthNames[calendarMonth]} $calendarYear",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
              onClick = {
                if (calendarMonth == 11) {
                  calendarMonth = 0
                  calendarYear += 1
                } else {
                  calendarMonth += 1
                }
                selectedDaySpecialInfo = null
              }
            ) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sonraki Ay")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Day Names Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            dayNames.forEach { dayName ->
              Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Calendar Days Grid
          val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendarYear)
            set(Calendar.MONTH, calendarMonth)
            set(Calendar.DAY_OF_MONTH, 1)
          }

          val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
          val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
          val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

          val todayCal = Calendar.getInstance()
          val isCurrentMonthAndYear =
            todayCal.get(Calendar.YEAR) == calendarYear &&
              todayCal.get(Calendar.MONTH) == calendarMonth
          val todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)

          for (row in 0 until (totalCells / 7)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              for (col in 0 until 7) {
                val dayNumber = (row * 7 + col) - firstDayOfWeek + 1
                if (dayNumber in 1..daysInMonth) {
                  val dateKey = String.format(Locale.ROOT, "%04d-%02d-%02d", calendarYear, calendarMonth + 1, dayNumber)
                  val isFasted = fastingDates.contains(dateKey)
                  val isToday = isCurrentMonthAndYear && dayNumber == todayDayOfMonth
                  val specialDay = specialDaysMap[dateKey]

                  CalendarDayCell(
                    dayNumber = dayNumber,
                    isFasted = isFasted,
                    isToday = isToday,
                    specialEmoji = specialDay?.badgeEmoji,
                    onToggle = {
                      val nowFasted = prefs.toggleFastingDate(dateKey)
                      fastingDates = prefs.getFastingDates()
                      Toast.makeText(
                        context,
                        if (nowFasted) "$dayNumber ${monthNames[calendarMonth]} oruç tutuldu olarak işaretlendi ✓"
                        else "$dayNumber ${monthNames[calendarMonth]} işareti kaldırıldı",
                        Toast.LENGTH_SHORT
                      ).show()
                      if (specialDay != null) {
                        selectedDaySpecialInfo = specialDay
                      }
                    },
                    modifier = Modifier.weight(1f)
                  )
                } else {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
          }
        }
      }
    }

    // Special Day Meaning Detail Card
    selectedDaySpecialInfo?.let { special ->
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(special.badgeEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = special.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
              Text(text = special.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
          }
        }
      }
    }

    // Upcoming Special Days List for the Year
    item {
      Text(
        text = "$calendarYear Yılı Dini Günler, Haftalar & Bayramlar",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
      )
    }

    items(specialList) { item ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.badgeEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
              Text(text = item.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
          }
          Text(text = formatDateStr(item.dateKey), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    item {
      AdBannerComponent()
    }

    item {
      PrivacyFooter()
    }
  }
}

@Composable
private fun CalendarDayCell(
  dayNumber: Int,
  isFasted: Boolean,
  isToday: Boolean,
  specialEmoji: String?,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .padding(2.dp)
      .aspectRatio(1f)
      .clip(RoundedCornerShape(10.dp))
      .background(
        when {
          isFasted -> Color(0xFF2D5A43) // Green for fasted
          isToday -> MaterialTheme.colorScheme.primaryContainer
          specialEmoji != null -> MaterialTheme.colorScheme.surfaceVariant
          else -> Color.Transparent
        }
      )
      .border(
        width = if (isToday) 1.5.dp else 0.dp,
        color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onToggle() },
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "$dayNumber",
        fontSize = 12.sp,
        fontWeight = if (isToday || isFasted) FontWeight.Bold else FontWeight.Medium,
        color = if (isFasted) Color.White else MaterialTheme.colorScheme.onSurface
      )
      if (specialEmoji != null && !isFasted) {
        Text(specialEmoji, fontSize = 8.sp)
      } else if (isFasted) {
        Text("✓", fontSize = 9.sp, color = Color(0xFFE8D39E), fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

private fun formatDateStr(dateKey: String): String {
  val parts = dateKey.split("-")
  if (parts.size == 3) {
    val monthNum = parts[1].toIntOrNull() ?: 1
    val months = listOf("", "Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
    return "${parts[2]} ${months[monthNum]}"
  }
  return dateKey
}

