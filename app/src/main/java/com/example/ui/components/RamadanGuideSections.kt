package com.example.ui.components

import com.example.i18n.t
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.core.content.ContextCompat
import com.example.data.AppPreferences
import com.example.data.RamadanCalendar
import com.example.reminder.ReminderScheduler
import java.util.Calendar

// ─────────────────────────────── ORUÇ TAKİBİ ───────────────────────────────

@Composable
fun FastingTrackerSection(
  prefs: AppPreferences,
  onOpenFastingTracker: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val todayKey = remember { RamadanCalendar.dateKey(Calendar.getInstance()) }
  var todayStatus by remember { mutableStateOf(prefs.getFastingStatus(todayKey)) }
  var fastedCount by remember { mutableIntStateOf(prefs.getFastingDates().size) }
  var kazaCount by remember { mutableIntStateOf(prefs.kazaDebtCount) }

  fun mark(status: String, message: String) {
    if (todayStatus == status) return
    // Daha önce kaza eklenmiş bir durumdan "tuttum"a dönülürse kazayı geri al
    val wasKaza = todayStatus == "unfasted" || todayStatus == "broken"
    val isKaza = status == "unfasted" || status == "broken"
    if (isKaza && !wasKaza) kazaCount++
    if (!isKaza && wasKaza) kazaCount = (kazaCount - 1).coerceAtLeast(0)
    prefs.kazaDebtCount = kazaCount
    prefs.setFastingStatus(todayKey, status)
    todayStatus = status
    fastedCount = prefs.getFastingDates().size
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("guide_fasting_tracker"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(t("📿 Oruç Takibi"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(t("Bugünkü orucunuzu işaretleyin"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(12.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusChip("🟢", "Tuttum", todayStatus == "fasted", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
          mark("fasted", "Allah kabul eylesin! 🤲")
        }
        StatusChip("🔴", "Tutamadım", todayStatus == "unfasted", Color(0xFFB0473A), Modifier.weight(1f)) {
          mark("unfasted", "Tutulmadı olarak kaydedildi (1 kaza eklendi).")
        }
        StatusChip("🟠", "Bozuldu", todayStatus == "broken", Color(0xFFC77700), Modifier.weight(1f)) {
          mark("broken", "Bozuldu olarak kaydedildi (1 kaza eklendi).")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatBox("Tutulan oruç", "$fastedCount", Modifier.weight(1f))
        StatBox("Kaza borcu", "$kazaCount", Modifier.weight(1f))
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = t("Detaylı oruç takibi ve takvim ›"),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .clickable { onOpenFastingTracker() }
          .padding(vertical = 6.dp),
        textAlign = TextAlign.End
      )
    }
  }
}

@Composable
private fun StatusChip(
  emoji: String,
  label: String,
  selected: Boolean,
  selectedColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(emoji, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
      maxLines = 1
    )
  }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.primaryContainer)
      .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

// ─────────────────────────── İFTAR / SAHUR HATIRLATICILARI ───────────────────────────

private val minuteOptions = listOf(10, 15, 30, 45, 60)

@Composable
fun RamadanRemindersSection(
  prefs: AppPreferences,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var sahurOn by remember { mutableStateOf(prefs.remindBeforeImsak) }
  var sahurMin by remember { mutableIntStateOf(prefs.remindBeforeImsakMinutes) }
  var iftarOn by remember { mutableStateOf(prefs.remindBeforeIftar) }
  var iftarMin by remember { mutableIntStateOf(prefs.remindBeforeIftarMinutes) }

  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (!granted) {
      Toast.makeText(context, "Bildirim izni verilmedi; hatırlatıcılar gösterilemez.", Toast.LENGTH_LONG).show()
    }
  }

  fun ensurePermissionAndSchedule() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    ReminderScheduler.rescheduleAll(context)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("guide_reminders"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(t("⏰ Sahur & İftar Hatırlatıcıları"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(
        t("Oruç günlerinde vaktinden önce motive edici bir bildirim alırsınız."),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(10.dp))

      ReminderRow(
        title = t("🌙 Sahur Hatırlatıcısı"),
        subtitle = "İmsaktan $sahurMin dk önce",
        enabled = sahurOn,
        minutes = sahurMin,
        onToggle = {
          sahurOn = it
          prefs.remindBeforeImsak = it
          ensurePermissionAndSchedule()
        },
        onMinutes = {
          sahurMin = it
          prefs.remindBeforeImsakMinutes = it
          ReminderScheduler.rescheduleAll(context)
        }
      )

      HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

      ReminderRow(
        title = t("🍽️ İftar Hatırlatıcısı"),
        subtitle = "Akşam ezanından $iftarMin dk önce",
        enabled = iftarOn,
        minutes = iftarMin,
        onToggle = {
          iftarOn = it
          prefs.remindBeforeIftar = it
          ensurePermissionAndSchedule()
        },
        onMinutes = {
          iftarMin = it
          prefs.remindBeforeIftarMinutes = it
          ReminderScheduler.rescheduleAll(context)
        }
      )
    }
  }
}

@Composable
private fun ReminderRow(
  title: String,
  subtitle: String,
  enabled: Boolean,
  minutes: Int,
  onToggle: (Boolean) -> Unit,
  onMinutes: (Int) -> Unit
) {
  Column {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Switch(checked = enabled, onCheckedChange = onToggle)
    }
    if (enabled) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        minuteOptions.forEach { m ->
          val selected = m == minutes
          Text(
            text = "$m dk",
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { onMinutes(m) }
              .padding(vertical = 7.dp)
          )
        }
      }
    }
  }
}
