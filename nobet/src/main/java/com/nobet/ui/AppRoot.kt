@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.nobet.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobet.data.Builtins
import com.nobet.data.Store
import com.nobet.i18n.energyLabel
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDate
import com.nobet.i18n.fmtDur
import com.nobet.i18n.qualityEmoji
import com.nobet.i18n.qualityLabel
import com.nobet.i18n.t
import com.nobet.i18n.tagName
import com.nobet.i18n.typeName
import java.time.LocalDate
import kotlin.math.roundToInt

/** Basit, kütüphanesiz gezinme: sekmeler + üstte açılan tam ekran katmanlar + diyaloglar. */
object Nav {
  var tab by mutableIntStateOf(0)
  val stack = mutableStateListOf<String>()
  var paywallReason by mutableStateOf("")
  var dialog by mutableStateOf<String?>(null)
  var daySheet by mutableStateOf<LocalDate?>(null)

  fun open(o: String) {
    if (stack.lastOrNull() != o) stack.add(o)
  }

  fun pop() {
    if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
  }

  fun paywall(reason: String) {
    paywallReason = reason
    open("paywall")
  }
}

@Composable
fun AppRoot() {
  if (!Store.onboarded) {
    Onboarding()
    return
  }
  Box(Modifier.fillMaxSize()) {
    val items: List<Pair<ImageVector, String>> = listOf(
      Icons.Filled.Home to t("Bugün", "Today"),
      Icons.Filled.CalendarMonth to t("Takvim", "Calendar"),
      Icons.Filled.Bedtime to t("Uyku", "Sleep"),
      Icons.Filled.BarChart to t("İstatistik", "Stats"),
    )
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      bottomBar = {
        NavigationBar {
          items.forEachIndexed { i, (icon, label) ->
            NavigationBarItem(
              selected = Nav.tab == i,
              onClick = { Nav.tab = i },
              icon = { Icon(icon, null, Modifier.size(24.dp)) },
              label = { Text(label, maxLines = 1, softWrap = false) },
            )
          }
        }
      },
      floatingActionButton = {
        FloatingActionButton(onClick = { Nav.dialog = "quick" }, containerColor = MaterialTheme.colorScheme.primary) {
          Icon(Icons.Filled.Add, t("Hızlı ekle", "Quick add"), tint = MaterialTheme.colorScheme.onPrimary)
        }
      },
    ) { pad ->
      Box(Modifier.padding(pad).fillMaxSize()) {
        when (Nav.tab) {
          0 -> TodayScreen()
          1 -> CalendarScreen()
          2 -> SleepScreen()
          else -> StatsScreen()
        }
      }
    }

    val ov = Nav.stack.lastOrNull()
    if (ov != null) {
      BackHandler { Nav.pop() }
      Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (ov) {
          "settings" -> SettingsScreen()
          "paywall" -> PaywallScreen()
          "templates" -> TemplatesScreen()
          "patterns" -> PatternScreen()
          "sleepmode" -> SleepModeScreen()
          "share" -> ShareScreen()
          else -> Unit
        }
      }
    }
  }

  when (Nav.dialog) {
    "quick" -> QuickSheet()
    "energy" -> EnergyDialog()
    "sleeplog" -> SleepLogDialog()
    "wake" -> WakeDialog()
    else -> Unit
  }
  Nav.daySheet?.let { DaySheet(listOf(it)) { Nav.daySheet = null } }
}

@Composable
private fun QuickRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
      Icon(icon, null, Modifier.padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Spacer(Modifier.width(16.dp))
    Column {
      Text(title, style = MaterialTheme.typography.titleMedium)
      Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun QuickSheet() {
  ModalBottomSheet(onDismissRequest = { Nav.dialog = null }) {
    Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 24.dp)) {
      if (Store.sleepStart > 0L) {
        QuickRow(Icons.Filled.WbSunny, t("Uyandım", "I'm awake"), t("Uyku kaydını bitir", "Finish the sleep log")) { Nav.dialog = "wake" }
      } else {
        QuickRow(Icons.Filled.Bedtime, t("Uyumaya başla", "Start sleep"), t("Tek dokunuşla uyku sayacı", "One-tap sleep timer")) {
          Store.startSleep()
          Nav.dialog = null
          Nav.open("sleepmode")
        }
      }
      QuickRow(Icons.Filled.EditNote, t("Uyku ekle", "Log sleep"), t("Geçmiş bir uykuyu elle gir", "Enter a past sleep manually")) { Nav.dialog = "sleeplog" }
      QuickRow(Icons.Filled.Bolt, t("Enerji kaydı", "Energy check-in"), t("Bugün nasıl hissediyorsunuz?", "How do you feel today?")) { Nav.dialog = "energy" }
      QuickRow(Icons.Filled.EditCalendar, t("Vardiya ekle", "Add shift"), t("Bugüne vardiya ata", "Assign a shift to today")) {
        Nav.dialog = null
        Nav.daySheet = Store.today()
      }
    }
  }
}

@Composable
fun EnergyDialog() {
  val ctx = LocalContext.current
  var level by remember { mutableFloatStateOf(6f) }
  val tags = remember { mutableStateListOf<String>() }
  var note by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = { Nav.dialog = null },
    title = { Text(t("Enerjiniz nasıl?", "How's your energy?")) },
    text = {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        val v = level.roundToInt()
        Row(verticalAlignment = Alignment.Bottom) {
          Text("$v", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          Text(" / 10  · ${energyLabel(v)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }
        Slider(value = level, onValueChange = { level = it }, valueRange = 1f..10f, steps = 8)
        Gap(8.dp)
        Text(t("Etiketler (isteğe bağlı)", "Tags (optional)"), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Builtins.energyTags.forEach { tg ->
            FilterChip(
              selected = tg in tags,
              onClick = { if (tg in tags) tags.remove(tg) else tags.add(tg) },
              label = { Text(tagName(tg)) },
            )
          }
        }
        OutlinedTextField(
          value = note,
          onValueChange = { if (it.length <= 280) note = it },
          label = { Text(t("Not (isteğe bağlı)", "Note (optional)")) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
        )
      }
    },
    confirmButton = {
      TextButton(onClick = {
        Store.addEnergy(level.roundToInt(), tags.toList(), note.trim())
        Nav.dialog = null
        Toast.makeText(ctx, t("Kaydedildi", "Saved"), Toast.LENGTH_SHORT).show()
      }) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = { TextButton(onClick = { Nav.dialog = null }) { Text(t("İptal", "Cancel")) } },
  )
}

@Composable
fun QualityPicker(value: Int, onChange: (Int) -> Unit) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    qualityEmoji.forEachIndexed { i, e ->
      val q = i + 1
      val sel = q == value
      Surface(
        onClick = { onChange(q) },
        shape = RoundedCornerShape(14.dp),
        color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(48.dp),
      ) {
        Box(contentAlignment = Alignment.Center) { Text(e, fontSize = 24.sp) }
      }
    }
  }
  Text(qualityLabel(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun WakeDialog() {
  val ctx = LocalContext.current
  var q by remember { mutableIntStateOf(4) }
  var nap by remember { mutableStateOf(false) }
  val dur = ((System.currentTimeMillis() - Store.sleepStart) / 60000L).toInt()
  AlertDialog(
    onDismissRequest = { Nav.dialog = null },
    title = { Text(t("Günaydın! Nasıl uyudunuz?", "Good morning! How did you sleep?")) },
    text = {
      Column {
        Text(t("Süre: ", "Duration: ") + fmtDur(dur), style = MaterialTheme.typography.titleMedium)
        Gap()
        QualityPicker(q) { q = it }
        Gap()
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(t("Bu bir şekerlemeydi", "This was a nap"), Modifier.weight(1f))
          Switch(checked = nap, onCheckedChange = { nap = it })
        }
        if (dur < 5) Text(t("5 dakikadan kısa uykular kaydedilmez.", "Sleeps under 5 minutes are not saved."), style = MaterialTheme.typography.bodySmall)
      }
    },
    confirmButton = {
      TextButton(onClick = {
        Store.stopSleep(q, nap)
        Nav.dialog = null
        if (Nav.stack.lastOrNull() == "sleepmode") Nav.pop()
        Toast.makeText(ctx, t("Uyku kaydedildi", "Sleep saved"), Toast.LENGTH_SHORT).show()
      }) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = {
      TextButton(onClick = {
        Store.cancelSleep()
        Nav.dialog = null
        if (Nav.stack.lastOrNull() == "sleepmode") Nav.pop()
      }) { Text(t("Kaydı sil", "Discard")) }
    },
  )
}

@Composable
fun SleepLogDialog() {
  val ctx = LocalContext.current
  val today = Store.today()
  var dayOffset by remember { mutableIntStateOf(1) } // yatış günü: 0 bugün, 1 dün, 2 önceki gün
  var startMin by remember { mutableIntStateOf(23 * 60) }
  var endMin by remember { mutableIntStateOf(7 * 60) }
  var q by remember { mutableIntStateOf(4) }
  var nap by remember { mutableStateOf(false) }
  var pick by remember { mutableStateOf<String?>(null) }

  val startDay = today.minusDays(dayOffset.toLong())
  val start = startDay.atStartOfDay().plusMinutes(startMin.toLong())
  var end = startDay.atStartOfDay().plusMinutes(endMin.toLong())
  if (!end.isAfter(start)) end = end.plusDays(1)
  val dur = java.time.Duration.between(start, end).toMinutes().toInt()
  val inFuture = Store.toMs(end) > System.currentTimeMillis() + 60_000L

  AlertDialog(
    onDismissRequest = { Nav.dialog = null },
    title = { Text(t("Uyku ekle", "Log sleep")) },
    text = {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(t("Yattığınız gün", "Day you went to sleep"), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(0 to t("Bugün", "Today"), 1 to t("Dün", "Yesterday"), 2 to t("2 gün önce", "2 days ago")).forEach { (o, l) ->
            FilterChip(selected = dayOffset == o, onClick = { dayOffset = o }, label = { Text(l) })
          }
        }
        Gap(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedButton(onClick = { pick = "s" }, modifier = Modifier.weight(1f).height(56.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(t("Yatış", "Bedtime"), style = MaterialTheme.typography.labelSmall)
              Text(fmtClock(startMin), fontWeight = FontWeight.Bold)
            }
          }
          OutlinedButton(onClick = { pick = "e" }, modifier = Modifier.weight(1f).height(56.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(t("Uyanış", "Wake up"), style = MaterialTheme.typography.labelSmall)
              Text(fmtClock(endMin), fontWeight = FontWeight.Bold)
            }
          }
        }
        Gap(8.dp)
        Text("${fmtDate(start.toLocalDate(), "d MMM")} ${fmtClock(startMin)} → ${fmtDate(end.toLocalDate(), "d MMM")} ${fmtClock(endMin)} · ${fmtDur(dur)}")
        if (inFuture) Text(t("Uyanış zamanı gelecekte olamaz.", "Wake-up time can't be in the future."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (dur > 18 * 60) Text(t("18 saatten uzun uyku kaydedilemez.", "Sleep longer than 18 hours can't be saved."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Gap()
        QualityPicker(q) { q = it }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(t("Şekerleme", "Nap"), Modifier.weight(1f))
          Switch(checked = nap, onCheckedChange = { nap = it })
        }
      }
    },
    confirmButton = {
      TextButton(
        enabled = !inFuture && dur <= 18 * 60,
        onClick = {
          Store.addSleep(Store.toMs(start), Store.toMs(end), q, nap)
          Nav.dialog = null
          Toast.makeText(ctx, t("Uyku kaydedildi", "Sleep saved"), Toast.LENGTH_SHORT).show()
        },
      ) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = { TextButton(onClick = { Nav.dialog = null }) { Text(t("İptal", "Cancel")) } },
  )
  when (pick) {
    "s" -> TimePickDialog(t("Yatış saati", "Bedtime"), startMin, { pick = null }) { startMin = it; pick = null }
    "e" -> TimePickDialog(t("Uyanış saati", "Wake-up time"), endMin, { pick = null }) { endMin = it; pick = null }
    else -> Unit
  }
}

/** Bir veya birden çok güne vardiya türü atama. */
@Composable
fun DaySheet(days: List<LocalDate>, onDismiss: () -> Unit) {
  if (days.isEmpty()) return
  val single = days.size == 1
  val d = days.first()
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
      Text(
        if (single) fmtDate(d) else t("${days.size} gün seçildi", "${days.size} days selected"),
        style = MaterialTheme.typography.titleLarge,
      )
      val cur = if (single) Store.shiftOn(d) else null
      if (cur != null) {
        Text(
          typeName(cur) + if (!cur.isOff) " · ${fmtClock(cur.startMin)}–${fmtClock(cur.endMin)}" else "",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Gap()
      FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Store.types.forEach { ty ->
          val sel = cur?.id == ty.id
          Surface(
            onClick = {
              days.forEach { Store.setShift(it, ty.id) }
              Store.changed()
              onDismiss()
            },
            shape = RoundedCornerShape(16.dp),
            color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
              ShiftBadge(ty, 30.dp)
              Spacer(Modifier.width(10.dp))
              Column {
                Text(typeName(ty), fontWeight = FontWeight.SemiBold)
                if (!ty.isOff) Text("${fmtClock(ty.startMin)}–${fmtClock(ty.endMin)}", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
      Gap()
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
          days.forEach { Store.setShift(it, null) }
          Store.changed()
          onDismiss()
        }) { Text(t("Temizle", "Clear")) }
        OutlinedButton(onClick = {
          onDismiss()
          Nav.open("templates")
        }) { Text(t("Vardiya türleri", "Shift types")) }
      }
      if (single) {
        val mins = Store.sleepMinOn(d)
        val en = Store.energyOn(d)
        if (mins > 0 || en != null) {
          Gap()
          SectionTitle(t("Bu gün", "This day"))
          if (mins > 0) Text("🛏 " + t("Uyku: ", "Sleep: ") + fmtDur(mins))
          if (en != null) Text("⚡ " + t("Enerji: ", "Energy: ") + "${en.roundToInt()}/10")
        }
      }
    }
  }
}
