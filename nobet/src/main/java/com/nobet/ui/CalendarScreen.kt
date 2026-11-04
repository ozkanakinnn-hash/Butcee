@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.nobet.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nobet.data.Builtins
import com.nobet.data.Pattern
import com.nobet.data.ShiftType
import com.nobet.data.Store
import com.nobet.i18n.dowShort
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDate
import com.nobet.i18n.fmtDur
import com.nobet.i18n.patternDesc
import com.nobet.i18n.patternName
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen() {
  var monthOffset by rememberSaveable { mutableIntStateOf(0) }
  var weekMode by rememberSaveable { mutableStateOf(false) }
  var weekOffset by rememberSaveable { mutableIntStateOf(0) }
  var selectMode by remember { mutableStateOf(false) }
  val selected = remember { mutableStateListOf<LocalDate>() }
  var bulkSheet by remember { mutableStateOf(false) }
  val today = Store.today()

  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(t("Takvim", "Calendar"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 4.dp))
      IconButton(onClick = {
        selectMode = !selectMode
        selected.clear()
      }) {
        Icon(Icons.Filled.ChecklistRtl, t("Çoklu seçim", "Multi-select"), tint = if (selectMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(onClick = { Nav.open("patterns") }) { Icon(Icons.Filled.AutoAwesome, t("Düzen uygula", "Apply rotation")) }
    }

    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
      SegmentedButton(selected = !weekMode, onClick = { weekMode = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text(t("Ay", "Month")) }
      SegmentedButton(selected = weekMode, onClick = { weekMode = true }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text(t("Hafta", "Week")) }
    }

    if (selectMode) {
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.primaryContainer) {
        Text(
          t("Günlere dokunarak seçin, sonra vardiya atayın.", "Tap days to select them, then assign a shift."),
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Gap(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(enabled = selected.isNotEmpty(), onClick = { bulkSheet = true }) {
            Text(t("${selected.size} güne ata", "Assign to ${selected.size}"))
          }
          OutlinedButton(onClick = {
            selectMode = false
            selected.clear()
          }) { Text(t("Bitti", "Done")) }
        }
      }
      Gap(8.dp)
    }

    val onDay: (LocalDate) -> Unit = { d ->
      if (selectMode) {
        if (d in selected) selected.remove(d) else selected.add(d)
      } else {
        Nav.daySheet = d
      }
    }

    if (!weekMode) {
      val ym = YearMonth.from(today).plusMonths(monthOffset.toLong())
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { monthOffset-- }) { Icon(Icons.Filled.ChevronLeft, t("Önceki ay", "Previous month")) }
        Text(
          fmtDate(ym.atDay(1), "LLLL yyyy").replaceFirstChar { it.titlecase(com.nobet.i18n.appLocale()) },
          Modifier.weight(1f),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = { monthOffset++ }) { Icon(Icons.Filled.ChevronRight, t("Sonraki ay", "Next month")) }
      }
      MonthGrid(ym, today, selected, onDay)
      if (monthOffset != 0) {
        TextButton(onClick = { monthOffset = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(t("Bugüne dön", "Back to today")) }
      }
      Gap(8.dp)
      MonthSummary(ym)
    } else {
      val weekStart = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { weekOffset-- }) { Icon(Icons.Filled.ChevronLeft, t("Önceki hafta", "Previous week")) }
        Text(
          "${fmtDate(weekStart, "d MMM")} – ${fmtDate(weekStart.plusDays(6), "d MMM")}",
          Modifier.weight(1f),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = { weekOffset++ }) { Icon(Icons.Filled.ChevronRight, t("Sonraki hafta", "Next week")) }
      }
      for (i in 0 until 7) {
        val d = weekStart.plusDays(i.toLong())
        WeekRow(d, d == today, d in selected) { onDay(d) }
      }
    }

    Gap()
    SectionTitle(t("Renkler", "Legend"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Store.types.forEach { ty ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          ShiftBadge(ty, 22.dp)
          Spacer(Modifier.width(6.dp))
          Text(typeName(ty), style = MaterialTheme.typography.bodySmall)
        }
      }
    }
    Spacer(Modifier.height(88.dp))
  }

  if (bulkSheet) {
    DaySheet(selected.toList()) {
      bulkSheet = false
      selected.clear()
      selectMode = false
    }
  }
}

@Composable
private fun MonthGrid(ym: YearMonth, today: LocalDate, selected: List<LocalDate>, onDay: (LocalDate) -> Unit) {
  val first = ym.atDay(1)
  val lead = first.dayOfWeek.value - 1 // Pazartesi başlangıç
  val days = ym.lengthOfMonth()
  val rows = (lead + days + 6) / 7
  Row(Modifier.fillMaxWidth()) {
    DayOfWeek.values().forEach {
      Text(
        dowShort(it),
        Modifier.weight(1f),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        maxLines = 1,
      )
    }
  }
  Gap(4.dp)
  for (r in 0 until rows) {
    Row(Modifier.fillMaxWidth()) {
      for (c in 0 until 7) {
        val idx = r * 7 + c - lead
        Box(Modifier.weight(1f).aspectRatio(0.78f).padding(2.dp)) {
          if (idx in 0 until days) {
            val d = first.plusDays(idx.toLong())
            DayCell(d, d == today, d in selected) { onDay(d) }
          }
        }
      }
    }
  }
}

@Composable
private fun DayCell(d: LocalDate, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit) {
  val ty = Store.shiftOn(d)
  val shape = RoundedCornerShape(12.dp)
  val bg = when {
    isSelected -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.surface
  }
  Column(
    Modifier.fillMaxSize().clip(shape).background(bg)
      .then(if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape))
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      "${d.dayOfMonth}",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
      color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    if (ty != null) ShiftBadge(ty, 26.dp) else Spacer(Modifier.size(26.dp))
    Box(
      Modifier.size(5.dp).clip(CircleShape)
        .background(if (Store.hasSleep(d)) MaterialTheme.colorScheme.tertiary else Color.Transparent),
    )
  }
}

@Composable
private fun WeekRow(d: LocalDate, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit) {
  val ty = Store.shiftOn(d)
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    border = BorderStroke(if (isToday) 2.dp else 1.dp, if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.width(56.dp)) {
        Text(dowShort(d.dayOfWeek), style = MaterialTheme.typography.labelMedium)
        Text("${d.dayOfMonth}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
      if (ty != null) ShiftBadge(ty, 36.dp) else Spacer(Modifier.size(36.dp))
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(ty?.let { typeName(it) } ?: t("Vardiya yok", "No shift"), fontWeight = FontWeight.SemiBold)
        if (ty != null && !ty.isOff) Text("${fmtClock(ty.startMin)} – ${fmtClock(ty.endMin)} · ${fmtDur(ty.durationMin)}", style = MaterialTheme.typography.bodySmall)
      }
      val mins = Store.sleepMinOn(d)
      if (mins > 0) Text("🛏 ${fmtDur(mins)}", style = MaterialTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun MonthSummary(ym: YearMonth) {
  var work = 0
  var nights = 0
  var minutes = 0
  for (i in 0 until ym.lengthOfMonth()) {
    val w = Store.workOn(ym.atDay(1).plusDays(i.toLong())) ?: continue
    work++
    if (w.type.isNight) nights++
    minutes += w.type.durationMin
  }
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    StatTile(t("Çalışma günü", "Work days"), "$work", modifier = Modifier.weight(1f))
    StatTile(t("Gece", "Nights"), "$nights", modifier = Modifier.weight(1f))
    StatTile(t("Saat", "Hours"), "${minutes / 60}", modifier = Modifier.weight(1f))
  }
}

// ---------------- Düzen (rotasyon) uygulama ----------------

@Composable
fun PatternScreen() {
  val ctx = LocalContext.current
  var chosen by remember { mutableStateOf<Pattern?>(null) }
  var startOffset by remember { mutableIntStateOf(0) } // bugünden kaç gün sonra başlasın
  var cycleDay by remember { mutableIntStateOf(0) }
  var months by remember { mutableIntStateOf(3) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (chosen == null) t("Vardiya düzenleri", "Rotation patterns") else patternName(chosen!!)) },
        navigationIcon = {
          IconButton(onClick = { if (chosen != null) chosen = null else Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) }
        },
      )
    },
  ) { pad ->
    Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
      val p = chosen
      if (p == null) {
        Builtins.patterns.forEach { pt ->
          val locked = pt.premium && !Store.premium
          NCard(Modifier.fillMaxWidth().padding(vertical = 5.dp), onClick = {
            if (locked) Nav.paywall(t("Tüm vardiya düzenleri", "Full pattern library")) else {
              chosen = pt
              cycleDay = 0
            }
          }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text(patternName(pt), style = MaterialTheme.typography.titleMedium)
                Text(patternDesc(pt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (locked) Icon(Icons.Filled.Lock, t("Premium", "Premium"), tint = MaterialTheme.colorScheme.primary)
            }
            Gap(8.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
              pt.seq.take(16).forEach { id -> Store.typeById(id)?.let { ShiftBadge(it, 16.dp) } }
              if (pt.seq.size > 16) Text("…", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      } else {
        val start = Store.today().plusDays(startOffset.toLong())
        SectionTitle(t("Başlangıç tarihi", "Start date"))
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { if (startOffset > -30) startOffset-- }) { Icon(Icons.Filled.ChevronLeft, null) }
          Text(fmtDate(start), Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
          IconButton(onClick = { if (startOffset < 60) startOffset++ }) { Icon(Icons.Filled.ChevronRight, null) }
        }
        SectionTitle(t("Bu tarih döngünün hangi günü?", "Which cycle day is this date?"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          p.seq.forEachIndexed { i, id ->
            val ty = Store.typeById(id) ?: return@forEachIndexed
            Surface(
              onClick = { cycleDay = i },
              shape = RoundedCornerShape(10.dp),
              color = if (cycleDay == i) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${i + 1}", style = MaterialTheme.typography.labelSmall)
                ShiftBadge(ty, 26.dp)
              }
            }
          }
        }
        SectionTitle(t("Süre", "Duration"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(1, 3, 6, 12).forEach { m ->
            FilterChip(selected = months == m, onClick = { months = m }, label = { Text(t("$m ay", "$m mo")) })
          }
        }
        val days = java.time.temporal.ChronoUnit.DAYS.between(start, start.plusMonths(months.toLong())).toInt()
        val existing = (0 until days).count { Store.shifts.containsKey(Store.key(start.plusDays(it.toLong()))) }
        Gap()
        if (existing > 0) {
          Text(
            t("Bu aralıktaki $existing günün vardiyası değiştirilecek.", "$existing existing days in this range will be overwritten."),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
          Gap(8.dp)
        }
        BigButton(t("Uygula", "Apply")) {
          Store.applyPattern(p, start, cycleDay, days)
          Toast.makeText(ctx, t("$days güne uygulandı", "Applied to $days days"), Toast.LENGTH_SHORT).show()
          Nav.pop()
        }
      }
    }
  }
}

// ---------------- Vardiya türleri / şablonlar ----------------

@Composable
fun TemplatesScreen() {
  var editing by remember { mutableStateOf<ShiftType?>(null) }
  var creating by remember { mutableStateOf(false) }
  var deleting by remember { mutableStateOf<ShiftType?>(null) }
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(t("Vardiya türleri", "Shift types")) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
      Store.types.forEach { ty ->
        NCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            ShiftBadge(ty, 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
              Text(typeName(ty), fontWeight = FontWeight.SemiBold)
              Text(
                if (ty.isOff) t("İzin günü", "Day off") else "${fmtClock(ty.startMin)} – ${fmtClock(ty.endMin)} · ${fmtDur(ty.durationMin)}" +
                  if (ty.isNight) t(" · gece", " · night") else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (!ty.isOff) IconButton(onClick = { editing = ty }) { Icon(Icons.Filled.Edit, t("Düzenle", "Edit")) }
            if (!ty.builtIn) IconButton(onClick = { deleting = ty }) { Icon(Icons.Filled.Delete, t("Sil", "Delete")) }
          }
        }
      }
      Gap()
      val limitReached = !Store.premium && Store.customCount() >= 3
      BigButton(t("Özel vardiya ekle", "Add custom shift") + if (limitReached) " 🔒" else "") {
        if (limitReached) Nav.paywall(t("Sınırsız vardiya şablonu", "Unlimited shift templates")) else creating = true
      }
      if (!Store.premium) {
        Text(
          t("Ücretsiz sürümde 3 özel vardiya. Şu an: ${Store.customCount()}/3", "Free plan: 3 custom shifts. Now: ${Store.customCount()}/3"),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 8.dp),
        )
      }
    }
  }
  if (creating) {
    TypeEditDialog(null) { creating = false }
  }
  editing?.let { ty -> TypeEditDialog(ty) { editing = null } }
  deleting?.let { ty ->
    ConfirmDialog(
      t("Silinsin mi?", "Delete?"),
      t("“${typeName(ty)}” ve takvimdeki tüm kullanımları silinecek.", "“${typeName(ty)}” and all its calendar days will be removed."),
      t("Sil", "Delete"),
      { deleting = null },
    ) {
      Store.deleteType(ty.id)
      deleting = null
    }
  }
}

@Composable
private fun TypeEditDialog(orig: ShiftType?, onDone: () -> Unit) {
  var name by remember { mutableStateOf(orig?.let { typeName(it) } ?: "") }
  var color by remember { mutableStateOf(orig?.color ?: Builtins.customPalette[Store.customCount() % Builtins.customPalette.size]) }
  var start by remember { mutableIntStateOf(orig?.startMin ?: (9 * 60)) }
  var end by remember { mutableIntStateOf(orig?.endMin ?: (17 * 60)) }
  var pick by remember { mutableStateOf<String?>(null) }
  val builtIn = orig?.builtIn == true

  AlertDialog(
    onDismissRequest = onDone,
    title = { Text(if (orig == null) t("Yeni vardiya", "New shift") else typeName(orig)) },
    text = {
      Column {
        if (!builtIn) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20) },
            label = { Text(t("Ad", "Name")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Gap(8.dp)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Builtins.customPalette.forEach { c ->
              Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color(c))
                  .then(if (c == color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                  .clickable { color = c },
              )
            }
          }
          Gap(8.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedButton(onClick = { pick = "s" }, modifier = Modifier.weight(1f)) { Text(t("Başlangıç ", "Start ") + fmtClock(start)) }
          OutlinedButton(onClick = { pick = "e" }, modifier = Modifier.weight(1f)) { Text(t("Bitiş ", "End ") + fmtClock(end)) }
        }
      }
    },
    confirmButton = {
      TextButton(enabled = builtIn || name.isNotBlank(), onClick = {
        val nm = name.trim()
        val ty = if (orig != null) {
          if (builtIn) orig.copy(startMin = start, endMin = end)
          else orig.copy(nameTr = nm, nameEn = nm, letterTr = letters(nm), letterEn = letters(nm), color = color, startMin = start, endMin = end)
        } else {
          ShiftType("C" + System.currentTimeMillis(), nm, nm, letters(nm), letters(nm), color, start, end, isOff = false, builtIn = false)
        }
        Store.upsertType(ty)
        onDone()
      }) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = { TextButton(onClick = onDone) { Text(t("İptal", "Cancel")) } },
  )
  when (pick) {
    "s" -> TimePickDialog(t("Başlangıç", "Start"), start, { pick = null }) { start = it; pick = null }
    "e" -> TimePickDialog(t("Bitiş", "End"), end, { pick = null }) { end = it; pick = null }
    else -> Unit
  }
}

private fun letters(name: String): String {
  val parts = name.trim().split(" ").filter { it.isNotEmpty() }
  val s = if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}" else name.trim().take(2)
  return s.uppercase(com.nobet.i18n.appLocale()).ifEmpty { "?" }
}

@Suppress("unused")
private val unusedAdd = Icons.Filled.Add
