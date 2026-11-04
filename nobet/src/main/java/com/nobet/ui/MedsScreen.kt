@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.nobet.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nobet.data.Dose
import com.nobet.data.Med
import com.nobet.data.Meds
import com.nobet.data.Store
import com.nobet.i18n.dowShort
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDate
import com.nobet.i18n.t
import com.nobet.notify.MedAlarms
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

private fun medsChanged(ctx: android.content.Context) {
  Meds.save()
  MedAlarms.reschedule(ctx)
}

// ---------------- Sağlık bilgileri (isteğe bağlı) ----------------

/** Onboarding ve Ayarlar'da kullanılan isteğe bağlı sağlık alanları. */
@Composable
fun HealthFields() {
  SectionTitle(t("Uygulamayı kimin için kullanıyorsunuz? (isteğe bağlı)", "Who are you using the app for? (optional)"))
  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    listOf("self" to t("Kendim", "Myself"), "family" to t("Bir yakınım", "A family member"), "patient" to t("Bakımını üstlendiğim biri", "Someone I care for")).forEach { (k, l) ->
      FilterChip(
        selected = Meds.forWhom == k,
        onClick = {
          Meds.forWhom = if (Meds.forWhom == k) "" else k
          Meds.save()
        },
        label = { Text(l) },
      )
    }
  }
  if (Meds.forWhom == "family" || Meds.forWhom == "patient") {
    Gap(6.dp)
    OutlinedTextField(
      value = Meds.personName,
      onValueChange = {
        Meds.personName = it.take(30)
        Meds.save()
      },
      label = { Text(t("Kişinin adı (isteğe bağlı)", "Their name (optional)")) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
  Gap(8.dp)
  OutlinedTextField(
    value = Meds.chronic,
    onValueChange = {
      Meds.chronic = it.take(120)
      Meds.save()
    },
    label = { Text(t("Kronik rahatsızlık (isteğe bağlı)", "Chronic condition (optional)")) },
    modifier = Modifier.fillMaxWidth(),
    maxLines = 2,
  )
  Gap(8.dp)
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    NumField(t("Yaş", "Age"), Meds.age, Modifier.weight(1f)) { Meds.age = it }
    NumField(t("Boy (cm)", "Height (cm)"), Meds.height, Modifier.weight(1f)) { Meds.height = it }
    NumField(t("Kilo (kg)", "Weight (kg)"), Meds.weight, Modifier.weight(1f)) { Meds.weight = it }
  }
  Text(
    t("Bu bilgiler yalnızca bu cihazda saklanır, paylaşılmaz.", "This information stays on this device and is never shared."),
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun NumField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
  OutlinedTextField(
    value = value,
    onValueChange = { v ->
      onChange(v.filter { it.isDigit() || it == ',' || it == '.' }.take(5))
      Meds.save()
    },
    label = { Text(label, maxLines = 1) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    modifier = modifier,
  )
}

// ---------------- Ana sayfa: haftalık ilaç paneli ----------------

@Composable
private fun RequiredBadge() {
  Surface(color = Color(0xFFC8605A), shape = RoundedCornerShape(6.dp)) {
    Text(
      t("ZORUNLU", "REQUIRED"),
      Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
      color = Color.White,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
    )
  }
}

/** Pazartesi–Pazar günleri ve seçili günün ilaçları; zorunlu ilaçlarda kırmızı rozet. */
@Composable
fun MedWeekPanel() {
  val ctx = LocalContext.current
  val now = rememberNow()
  val today = now.toLocalDate()
  var sel by remember { mutableStateOf(today) }
  val monday = today.with(DayOfWeek.MONDAY)

  NCard(Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("💊 " + Meds.ownerLabel(::t), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
      TextButton(onClick = { Nav.open("meds") }) { Text(if (Meds.meds.isEmpty()) t("Ekle", "Add") else t("Yönet", "Manage")) }
    }
    // Pazartesi → Pazar
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      for (i in 0 until 7) {
        val d = monday.plusDays(i.toLong())
        val doses = Meds.dosesOn(d)
        val hasCritical = doses.any { it.med.critical }
        val allTaken = doses.isNotEmpty() && doses.all { Meds.isTaken(it) }
        val isSel = d == sel
        Column(
          Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { sel = d }
            .padding(horizontal = 6.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            dowShort(d.dayOfWeek),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 2.dp),
          )
          Text(
            "${d.dayOfMonth}",
            fontWeight = if (d == today) FontWeight.Bold else FontWeight.Normal,
            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          )
          Box(
            Modifier.padding(top = 3.dp).width(6.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(
              when {
                doses.isEmpty() -> Color.Transparent
                allTaken -> Success
                hasCritical -> Color(0xFFC8605A)
                else -> MaterialTheme.colorScheme.outline
              },
            ),
          )
        }
      }
    }
    Gap(8.dp)
    val doses = Meds.dosesOn(sel)
    Text(fmtDate(sel, "d MMMM EEEE"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (doses.isEmpty()) {
      Text(
        if (Meds.meds.isEmpty()) t("Henüz ilaç eklenmedi. \"Ekle\" ile günlük ilaçlarınızı ve saatlerini girin.", "No medicines yet. Use \"Add\" to enter your daily medicines and times.")
        else t("Bu gün için ilaç yok.", "No medicines on this day."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 6.dp),
      )
    }
    doses.forEach { d -> DoseRow(d, now) { medsChanged(ctx) } }
    if (Meds.meds.isNotEmpty() && !MedAlarms.canExact(ctx) && Build.VERSION.SDK_INT >= 31) {
      Gap(6.dp)
      TextButton(onClick = {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.packageName))) }
      }) {
        Text(t("⚠ İlaç saatlerinin tam zamanında çalması için 'Tam zamanlı alarm' iznini verin", "⚠ Allow 'exact alarms' so medicine reminders ring on time"), color = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
private fun DoseRow(d: Dose, now: LocalDateTime, onChange: () -> Unit) {
  val taken = Meds.isTaken(d)
  val past = d.at.isBefore(now)
  val canMark = !d.date.isAfter(now.toLocalDate())
  Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(fmtClock(d.minute), fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(d.med.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
        if (d.med.critical) {
          Spacer(Modifier.width(6.dp))
          RequiredBadge()
        }
      }
      val status = when {
        taken -> t("Alındı ✓", "Taken ✓")
        past -> t("Alınmadı", "Not taken")
        else -> t("Bekliyor", "Upcoming")
      }
      Text(
        (if (d.med.dose.isNotBlank()) d.med.dose + " · " else "") + status,
        style = MaterialTheme.typography.bodySmall,
        color = when {
          taken -> Success
          past && d.med.critical -> Color(0xFFC8605A)
          else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
      )
    }
    if (canMark) {
      IconButton(onClick = {
        if (taken) Meds.unmarkTaken(d.key) else Meds.markTaken(d.key)
        onChange()
      }) {
        Icon(
          if (taken) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
          if (taken) t("Alındı", "Taken") else t("Aldım olarak işaretle", "Mark as taken"),
          tint = if (taken) Success else MaterialTheme.colorScheme.outline,
        )
      }
    }
  }
}

// ---------------- Kritik uyarı (ana ekranda pop-up) ----------------

/** Zorunlu ilaç 3 ertelemeden sonra hâlâ alınmadıysa kapatılabilir kritik uyarı. */
@Composable
fun CriticalMedAlert() {
  val ctx = LocalContext.current
  val now = rememberNow(15_000L)
  val d = Meds.criticalMissed(now).firstOrNull() ?: return
  val who = if (Meds.forWhom != "self" && Meds.forWhom.isNotEmpty() && Meds.personName.isNotBlank()) Meds.personName.trim() + ": " else ""
  AlertDialog(
    onDismissRequest = {},
    icon = { Icon(Icons.Filled.Warning, null, tint = Color(0xFFC8605A)) },
    title = { Text(t("KRİTİK UYARI", "CRITICAL ALERT"), color = Color(0xFFC8605A), fontWeight = FontWeight.Bold) },
    text = {
      Column {
        Text(
          t("Hayati risk: ilacınızı almadınız!", "Vital risk: you have not taken your medicine!"),
          fontWeight = FontWeight.Bold,
        )
        Gap(6.dp)
        Text(who + "${d.med.name}" + (if (d.med.dose.isNotBlank()) " (${d.med.dose})" else "") + " · ${fmtClock(d.minute)}")
        Gap(6.dp)
        Text(
          t("Bu uyarı 3 ertelemeden sonra gösterilir. Acil bir durumda 112'yi arayın.", "Shown after 3 snoozes. In an emergency, call your local emergency number."),
          style = MaterialTheme.typography.bodySmall,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          Meds.markTaken(d.key)
          MedAlarms.cancel(ctx, d.key)
          medsChanged(ctx)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8605A), contentColor = Color.White),
      ) { Text(t("Şimdi aldım", "Taken now")) }
    },
    dismissButton = {
      TextButton(onClick = {
        Meds.dismiss(d.key)
        MedAlarms.cancel(ctx, d.key)
      }) { Text(t("Kapat", "Close")) }
    },
  )
}

// ---------------- İlaç yönetimi ----------------

@Composable
fun MedsScreen() {
  val ctx = LocalContext.current
  var editing by remember { mutableStateOf<Med?>(null) }
  var creating by remember { mutableStateOf(false) }
  var deleting by remember { mutableStateOf<Med?>(null) }
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(Meds.ownerLabel(::t)) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(
      Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        t("Her ilaç saatinde bildirim gelir. \"Aldım\" ile onaylanmazsa 5 dakika sonra tekrar hatırlatılır; 3 ertelemeden sonra zorunlu ilaçlar için ana ekranda kritik uyarı gösterilir.",
          "You get a notification at each medicine time. If not confirmed with \"Taken\", it reminds you again after 5 minutes; after 3 snoozes a critical alert appears on the home screen for required medicines."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Meds.meds.forEach { m ->
        NCard(Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.name, fontWeight = FontWeight.SemiBold)
                if (m.critical) {
                  Spacer(Modifier.width(6.dp))
                  RequiredBadge()
                }
              }
              Text(
                (if (m.dose.isNotBlank()) m.dose + " · " else "") + m.times.sorted().joinToString(", ") { fmtClock(it) },
                style = MaterialTheme.typography.bodySmall,
              )
              Text(daysLabel(m.days), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { editing = m }) { Icon(Icons.Filled.Edit, t("Düzenle", "Edit")) }
            IconButton(onClick = { deleting = m }) { Icon(Icons.Filled.Delete, t("Sil", "Delete")) }
          }
        }
      }
      BigButton(t("İlaç ekle", "Add medicine")) { creating = true }
      Text(
        t("Nöbet bir hatırlatma aracıdır; tıbbi cihaz değildir ve doktor ya da eczacı talimatlarının yerini tutmaz.",
          "Nöbet is a reminder tool, not a medical device, and does not replace instructions from your doctor or pharmacist."),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
    }
  }
  if (creating) MedEditDialog(null) { creating = false; medsChanged(ctx) }
  editing?.let { m -> MedEditDialog(m) { editing = null; medsChanged(ctx) } }
  deleting?.let { m ->
    ConfirmDialog(t("İlaç silinsin mi?", "Delete medicine?"), m.name, t("Sil", "Delete"), { deleting = null }) {
      Meds.delete(m.id)
      deleting = null
      medsChanged(ctx)
    }
  }
}

private fun daysLabel(days: List<Int>): String =
  if (days.size == 7) t("Her gün", "Every day")
  else days.sorted().joinToString(" ") { dowShort(DayOfWeek.of(it)) }

@Composable
private fun MedEditDialog(orig: Med?, onDone: () -> Unit) {
  var name by remember { mutableStateOf(orig?.name ?: "") }
  var dose by remember { mutableStateOf(orig?.dose ?: "") }
  val times = remember { mutableStateListOf<Int>().apply { addAll(orig?.times ?: listOf(9 * 60)) } }
  val days = remember { mutableStateListOf<Int>().apply { addAll(orig?.days ?: (1..7).toList()) } }
  var critical by remember { mutableStateOf(orig?.critical ?: false) }
  var pick by remember { mutableStateOf<Int?>(null) } // düzenlenen saat sırası; -1 = yeni
  AlertDialog(
    onDismissRequest = onDone,
    title = { Text(if (orig == null) t("İlaç ekle", "Add medicine") else t("İlacı düzenle", "Edit medicine")) },
    text = {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = name, onValueChange = { name = it.take(40) }, label = { Text(t("İlaç adı", "Medicine name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Gap(6.dp)
        OutlinedTextField(value = dose, onValueChange = { dose = it.take(30) }, label = { Text(t("Doz (ör. 1 tablet)", "Dose (e.g. 1 tablet)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Gap(8.dp)
        Text(t("Saatler", "Times"), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          times.sorted().forEach { m ->
            InputChip(
              selected = false,
              onClick = { pick = times.indexOf(m) },
              label = { Text(fmtClock(m)) },
            )
          }
          OutlinedButton(onClick = { pick = -1 }) {
            Icon(Icons.Filled.Add, null)
            Text(t("Saat", "Time"))
          }
        }
        if (times.size > 1) {
          TextButton(onClick = { times.removeAt(times.size - 1) }) { Text(t("Son saati kaldır", "Remove last time")) }
        }
        Gap(6.dp)
        Text(t("Günler", "Days"), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          (1..7).forEach { dnum ->
            FilterChip(
              selected = dnum in days,
              onClick = { if (dnum in days) { if (days.size > 1) days.remove(dnum) } else days.add(dnum) },
              label = { Text(dowShort(DayOfWeek.of(dnum))) },
            )
          }
        }
        Gap(6.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
            Text(t("Alınması zorunlu", "Required (vital)"), fontWeight = FontWeight.SemiBold)
            Text(t("Kırmızı rozet; 3 ertelemeden sonra kritik uyarı", "Red badge; critical alert after 3 snoozes"), style = MaterialTheme.typography.labelSmall)
          }
          Switch(checked = critical, onCheckedChange = { critical = it })
        }
      }
    },
    confirmButton = {
      TextButton(enabled = name.isNotBlank() && times.isNotEmpty() && days.isNotEmpty(), onClick = {
        Meds.upsert(
          Med(orig?.id ?: ("M" + System.currentTimeMillis()), name.trim(), dose.trim(), times.distinct().sorted(), days.sorted(), critical),
        )
        onDone()
      }) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = { TextButton(onClick = onDone) { Text(t("İptal", "Cancel")) } },
  )
  pick?.let { idx ->
    TimePickDialog(t("İlaç saati", "Medicine time"), if (idx >= 0) times[idx] else 9 * 60, { pick = null }) { m ->
      if (idx >= 0) times[idx] = m else if (m !in times) times.add(m)
      pick = null
    }
  }
}

@Suppress("unused")
private fun unusedDate(): LocalDate = LocalDate.now()
