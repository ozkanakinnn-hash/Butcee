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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
  var checkinDate by mutableStateOf<LocalDate?>(null)

  fun openCheckin(d: LocalDate) {
    checkinDate = d
    daySheet = null
    if (stack.lastOrNull() == "checkin") stack.removeAt(stack.size - 1)
    open("checkin")
  }

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
      Icons.Filled.Spa to t("Toparlanma", "Recovery"),
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
    ) { pad ->
      Box(Modifier.padding(pad).fillMaxSize()) {
        when (Nav.tab) {
          0 -> TodayScreen()
          1 -> CalendarScreen()
          2 -> RecoveryScreen()
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
          "checkin" -> CheckInScreen()
          "tools" -> HourlyRateScreen()
          "meds" -> MedsScreen()
          "share" -> ShareScreen()
          else -> Unit
        }
      }
    }
  }

  when (Nav.dialog) {
    "energy" -> EnergyDialog()
    else -> Unit
  }
  Nav.daySheet?.let { DaySheet(listOf(it)) { Nav.daySheet = null } }
  // Zorunlu ilaç 3 ertelemeden sonra alınmadıysa: kapatılabilir kritik uyarı
  CriticalMedAlert()
}

@Composable
fun QuickRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
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

fun leaveName(k: String): String = when (k) {
  "annual" -> t("Yıllık izin", "Annual leave")
  "annualHalf" -> t("Yarım gün izin", "Half-day leave")
  "sick" -> t("Rapor", "Sick leave")
  "unpaid" -> t("Ücretsiz izin", "Unpaid leave")
  else -> t("Diğer izin", "Other leave")
}

/** Bir veya birden çok güne vardiya türü atama. */
@Composable
fun DaySheet(days: List<LocalDate>, onDismiss: () -> Unit) {
  if (days.isEmpty()) return
  val single = days.size == 1
  val d = days.first()
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
      Text(
        if (single) fmtDate(d) else t("${days.size} gün seçildi", "${days.size} days selected"),
        style = MaterialTheme.typography.titleLarge,
      )
      if (single) {
        com.nobet.data.Holidays.on(d)?.let { h ->
          Text("🎌 " + com.nobet.data.Holidays.name(h), color = Alert, fontWeight = FontWeight.SemiBold)
        }
      }
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
      // İzin (tek ya da çoklu gün)
      Gap()
      SectionTitle(t("İzin", "Leave"))
      val curLeave = if (single) Store.leaves[Store.key(d)] else null
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("annual", "annualHalf", "sick", "unpaid", "other").forEach { k ->
          FilterChip(
            selected = curLeave == k,
            onClick = {
              val v = if (curLeave == k) null else k
              days.forEach { Store.setLeave(it, v) }
              if (!single) onDismiss()
            },
            label = { Text(leaveName(k)) },
          )
        }
        if (!single || curLeave != null) {
          FilterChip(selected = false, onClick = {
            days.forEach { Store.setLeave(it, null) }
            if (!single) onDismiss()
          }, label = { Text(t("İzni kaldır", "Remove leave")) })
        }
      }
      if (single) {
        // Toparlanma kontrolü
        val w = Store.workOn(d)
        val now = java.time.LocalDateTime.now(Store.zone)
        if (w != null && !w.end.isAfter(now)) {
          Gap()
          SectionTitle(t("Toparlanma", "Recovery"))
          val sc = com.nobet.data.Recovery.scoreOn(d)
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (sc != null) {
              ScoreRing(sc, 56.dp)
              Spacer(Modifier.width(12.dp))
              Text(com.nobet.data.Recovery.bandLabel(sc), Modifier.weight(1f), color = scoreColor(sc))
            } else {
              Text(t("Bu vardiya için kontrol yok", "No check-in for this shift"), Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = { Nav.openCheckin(d) }) { Text(if (sc == null) t("Yanıtla", "Check in") else t("Düzenle", "Edit")) }
          }
        }
        // Vardiya günlüğü
        Gap()
        SectionTitle(t("Vardiya notu", "Shift note"))
        val old = Store.notes[Store.key(d)]
        var text by remember(d) { mutableStateOf(old?.text ?: "") }
        val tags = remember(d) { mutableStateListOf<String>().apply { addAll(old?.tags ?: emptyList()) } }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          com.nobet.data.Recovery.journalTags.forEach { tg ->
            FilterChip(
              selected = tg in tags,
              onClick = {
                if (tg in tags) tags.remove(tg) else tags.add(tg)
                Store.saveNote(d, text, tags.toList())
              },
              label = { Text(com.nobet.data.Recovery.tagName(tg)) },
            )
          }
        }
        OutlinedTextField(
          value = text,
          onValueChange = {
            text = it.take(280)
            Store.saveNote(d, text, tags.toList())
          },
          label = { Text(t("Örn. Çok yoğun bir gündü", "e.g. Very busy day")) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
        )
        val en = Store.energyOn(d)
        if (en != null) {
          Gap(8.dp)
          Text("⚡ " + t("Enerji: ", "Energy: ") + "${en.roundToInt()}/10")
        }
      }
    }
  }
}
