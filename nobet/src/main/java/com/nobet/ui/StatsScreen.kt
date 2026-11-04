@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.nobet.i18n.fmtDur
import com.nobet.i18n.fmtHours
import com.nobet.i18n.fmtNum
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.LocalDate

@Composable
fun StatsScreen() {
  var month by rememberSaveable { mutableStateOf(false) }
  val today = Store.today()
  val n = if (month) 30 else 7
  val days = (n - 1 downTo 0).map { today.minusDays(it.toLong()) }

  val scores = days.map { com.nobet.data.Recovery.scoreOn(it) }
  val scored = scores.filterNotNull()
  val avgRec = if (scored.isEmpty()) null else scored.average().toInt()
  val works = days.mapNotNull { Store.workOn(it) }
  val nights = works.count { it.type.isNight }
  val workMin = works.sumOf { it.type.durationMin }
  val energies = days.mapNotNull { Store.energyOn(it) }
  val avgEnergy = if (energies.isEmpty()) null else energies.average().toFloat()

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(t("İstatistikler", "Statistics"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
      SegmentedButton(selected = !month, onClick = { month = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text(t("Hafta", "Week")) }
      SegmentedButton(
        selected = month,
        onClick = { if (Store.premium) month = true else Nav.paywall(t("Aylık istatistikler", "Monthly statistics")) },
        shape = SegmentedButtonDefaults.itemShape(1, 2),
      ) { Text(t("Ay", "Month") + if (Store.premium) "" else " 🔒") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      StatTile(t("Ort. toparlanma", "Avg recovery"), avgRec?.toString() ?: "–", t("${scored.size} kontrol", "${scored.size} check-ins"), Modifier.weight(1f))
      StatTile(t("Vardiya", "Shifts"), "${works.size}", t("$nights gece · ${fmtHours(workMin)}", "$nights nights · ${fmtHours(workMin)}"), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      StatTile(t("Ort. enerji", "Avg energy"), avgEnergy?.let { fmtNum(it) } ?: "–", t("son $n gün", "last $n days"), Modifier.weight(1f))
      val rem = Store.leaveRemaining()
      StatTile(t("Kalan izin", "Leave left"), fmtNum(rem).removeSuffix(",0").removeSuffix(".0"), t("${fmtNum(Store.leaveUsed()).removeSuffix(",0").removeSuffix(".0")} gün kullanıldı", "${fmtNum(Store.leaveUsed()).removeSuffix(",0").removeSuffix(".0")} days used"), Modifier.weight(1f))
    }

    NCard(Modifier.fillMaxWidth()) {
      Text(t("Vardiya sonrası toparlanma", "Post-shift recovery"), style = MaterialTheme.typography.titleMedium)
      Gap(8.dp)
      BarChart(
        values = scores.map { (it ?: 0).toFloat() },
        labels = days.mapIndexed { i, d -> if (!month) dowShort(d.dayOfWeek) else if (i % 5 == 4) "${d.dayOfMonth}" else "" },
        color = MaterialTheme.colorScheme.primary,
        goal = 75f,
      )
      Text(t("Kesikli çizgi: 75 (iyi toparlanma)", "Dashed line: 75 (well recovered)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    JournalCard()

    if (Store.premium) {
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Vardiya dağılımı", "Shift distribution"), style = MaterialTheme.typography.titleMedium)
        Gap(8.dp)
        val counts = days.mapNotNull { Store.shiftOn(it) }.groupingBy { it.id }.eachCount()
        val total = counts.values.sum().coerceAtLeast(1)
        if (counts.isEmpty()) Text(t("Bu aralıkta vardiya yok.", "No shifts in this range."), style = MaterialTheme.typography.bodySmall)
        counts.entries.sortedByDescending { it.value }.forEach { (id, c) ->
          val ty = Store.typeById(id) ?: return@forEach
          HBar(typeName(ty), c.toFloat(), total.toFloat(), "$c", shiftColor(ty))
        }
      }
    } else {
      LockedCard(
        t("Detaylı istatistikler", "Detailed statistics"),
        t("Vardiya dağılımı ve aylık görünüm.", "Shift distribution and monthly view."),
      ) { Nav.paywall(t("Detaylı istatistikler", "Detailed statistics")) }
    }

    Spacer(Modifier.height(72.dp))
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
