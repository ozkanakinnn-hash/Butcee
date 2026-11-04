@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.nobet.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nobet.data.Store
import com.nobet.i18n.dowShort
import com.nobet.i18n.fmtDate
import com.nobet.i18n.fmtDur
import com.nobet.i18n.fmtHours
import com.nobet.i18n.fmtNum
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.LocalDate

@Composable
fun StatsScreen() {
  var period by rememberSaveable { mutableStateOf("week") }
  val today = Store.today()
  val (from, to) = when (period) {
    "month" -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
    "year" -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
    else -> today.with(java.time.DayOfWeek.MONDAY) to today.with(java.time.DayOfWeek.SUNDAY)
  }
  val works = generateSequence(from) { it.plusDays(1) }.takeWhile { !it.isAfter(to) }.mapNotNull { Store.workOn(it) }.toList()
  val total = works.sumOf { it.type.durationMin }
  val done = works.filter { !it.end.isAfter(java.time.LocalDateTime.now(Store.zone)) }.sumOf { it.type.durationMin }

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(t("İstatistikler", "Statistics"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
      listOf("week" to t("Bu hafta", "This week"), "month" to t("Bu ay", "This month"), "year" to t("Bu yıl", "This year")).forEachIndexed { i, (k, l) ->
        SegmentedButton(selected = period == k, onClick = { period = k }, shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(l, maxLines = 1) }
      }
    }
    NCard(Modifier.fillMaxWidth()) {
      Text(t("Toplam çalışma saati", "Total working hours"), style = MaterialTheme.typography.titleMedium)
      Text(
        "${fmtDate(from, "d MMM")} – ${fmtDate(to, "d MMM")}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Gap(12.dp)
      Text(fmtDur(total), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      Text(
        t("${works.size} vardiya · tamamlanan ${fmtDur(done)}", "${works.size} shifts · completed ${fmtDur(done)}"),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    // Toparlanma skoru (dönem ortalaması)
    val scores = generateSequence(from) { it.plusDays(1) }.takeWhile { !it.isAfter(to) }
      .mapNotNull { com.nobet.data.Recovery.scoreOn(it) }.toList()
    NCard(Modifier.fillMaxWidth()) {
      Text(t("Toparlanma skoru", "Recovery score"), style = MaterialTheme.typography.titleMedium)
      Text(t("Dönem ortalaması", "Average for the period"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Gap(12.dp)
      if (scores.isEmpty()) {
        Text("–", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(t("Bu dönemde toparlanma kontrolü yok", "No recovery check-ins in this period"), color = MaterialTheme.colorScheme.onSurfaceVariant)
      } else {
        val avg = scores.average().toInt()
        Text("$avg", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = scoreColor(avg))
        Text(com.nobet.data.Recovery.bandLabel(avg) + t(" · ${scores.size} kontrol", " · ${scores.size} check-ins"), color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    // Yıllık kullanılan izin
    NCard(Modifier.fillMaxWidth()) {
      Text(t("Kullanılan yıllık izin (${today.year})", "Annual leave used (${today.year})"), style = MaterialTheme.typography.titleMedium)
      Gap(12.dp)
      Text(
        fmtDays(Store.leaveUsed()) + t(" gün", " days"),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        t("Kalan ${fmtDays(Store.leaveRemaining())} / ${fmtDays(Store.leaveAllowance + Store.leaveCarry)} gün",
          "${fmtDays(Store.leaveRemaining())} of ${fmtDays(Store.leaveAllowance + Store.leaveCarry)} days left"),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Spacer(Modifier.height(24.dp))
  }
}

/** Vardiya günlüğü: son notlar, etikete göre filtre. */
@Composable
fun JournalCard() {
  var filter by rememberSaveable { mutableStateOf("") }
  val entries = Store.notes.entries.sortedByDescending { it.key }
    .filter { filter.isEmpty() || filter in it.value.tags }
  NCard(Modifier.fillMaxWidth()) {
    Text(t("Vardiya günlüğü", "Shift journal"), style = MaterialTheme.typography.titleMedium)
    Text(t("Notlar yalnızca sizde kalır, paylaşılmaz.", "Notes stay with you and are never shared."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Gap(8.dp)
    androidx.compose.foundation.layout.FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      com.nobet.data.Recovery.journalTags.forEach { tg ->
        androidx.compose.material3.FilterChip(
          selected = filter == tg,
          onClick = { filter = if (filter == tg) "" else tg },
          label = { Text(com.nobet.data.Recovery.tagName(tg)) },
        )
      }
    }
    Gap(8.dp)
    if (entries.isEmpty()) {
      Text(
        t("Takvimde bir güne dokunup vardiyanıza kısa bir not ekleyebilirsiniz.", "Tap a day in the calendar to add a short note to your shift."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    entries.take(8).forEach { (k, n) ->
      val d = runCatching { LocalDate.parse(k) }.getOrNull() ?: return@forEach
      Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
          com.nobet.i18n.relDay(d) + (Store.shiftOn(d)?.let { " · " + com.nobet.i18n.typeName(it) } ?: ""),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (n.tags.isNotEmpty()) Text(n.tags.joinToString(" · ") { com.nobet.data.Recovery.tagName(it) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        if (n.text.isNotBlank()) Text(n.text, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

fun shareCsv(ctx: Context) {
  val i = Intent(Intent.ACTION_SEND).setType("text/plain")
    .putExtra(Intent.EXTRA_SUBJECT, "Nöbet export")
    .putExtra(Intent.EXTRA_TEXT, Store.exportCsv())
  ctx.startActivity(Intent.createChooser(i, t("Dışa aktar", "Export")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun energyByType(today: LocalDate): List<Pair<String, Float>> {
  val m = mutableMapOf<String, MutableList<Float>>()
  for (i in 0 until 60) {
    val d = today.minusDays(i.toLong())
    val e = Store.energyOn(d) ?: continue
    val id = Store.shiftOn(d)?.id ?: "-"
    m.getOrPut(id) { mutableListOf() }.add(e)
  }
  return m.map { (k, v) -> k to v.average().toFloat() }.sortedByDescending { it.second }
}

/** Yerel, kural tabanlı içgörüler — toparlanma kontrollerinden üretilir. */
fun insights(today: LocalDate): List<String> {
  val out = mutableListOf<String>()
  val hist = com.nobet.data.Recovery.history(90, today)
  com.nobet.data.Recovery.basicInsight(hist)?.let { out += it }
  out += com.nobet.data.Recovery.patternInsights(hist)

  val pairs = hist.filter { it.c.energy > 0 }
  if (pairs.size >= 5) {
    val avg = pairs.map { it.c.energy }.average().toFloat()
    out += t("Vardiya sonrası ortalama enerjiniz ${fmtNum(avg)}/10. (${pairs.size} kontrol)", "Your average post-shift energy is ${fmtNum(avg)}/10. (${pairs.size} check-ins)")
  }

  val thisMonth = (0 until 30).count { Store.workOn(today.minusDays(it.toLong()))?.type?.isNight == true }
  val lastMonth = (30 until 60).count { Store.workOn(today.minusDays(it.toLong()))?.type?.isNight == true }
  if (thisMonth > 0) {
    val delta = thisMonth - lastMonth
    out += t(
      "Son 30 günde $thisMonth gece vardiyası" + if (lastMonth > 0) " (önceki 30 güne göre ${if (delta >= 0) "+" else ""}$delta)." else ".",
      "Last 30 days: $thisMonth night shifts" + if (lastMonth > 0) " (${if (delta >= 0) "+" else ""}$delta vs the previous 30 days)." else ".",
    )
  }
  return out
}
