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
  val ctx = LocalContext.current
  var month by rememberSaveable { mutableStateOf(false) }
  val today = Store.today()
  val n = if (month) 30 else 7
  val days = (n - 1 downTo 0).map { today.minusDays(it.toLong()) }

  val sleepVals = days.map { Store.sleepMinOn(it) }
  val dataDays = days.count { Store.hasSleep(it) }
  val totalSleep = sleepVals.sum()
  val avgSleep = if (dataDays == 0) 0 else totalSleep / dataDays
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
      StatTile(t("Ort. uyku", "Avg sleep"), if (dataDays == 0) "–" else fmtDur(avgSleep), t("$dataDays gün veri", "$dataDays days of data"), Modifier.weight(1f))
      StatTile(t("Toplam uyku", "Total sleep"), fmtHours(totalSleep), t("son $n gün", "last $n days"), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      StatTile(t("Gece vardiyası", "Night shifts"), "$nights", t("${works.size} çalışma günü", "${works.size} work days"), Modifier.weight(1f))
      StatTile(t("Ort. enerji", "Avg energy"), avgEnergy?.let { fmtNum(it) } ?: "–", t("${fmtHours(workMin)} çalışma", "${fmtHours(workMin)} worked"), Modifier.weight(1f))
    }

    NCard(Modifier.fillMaxWidth()) {
      Text(t("Uyku saatleri", "Sleep hours"), style = MaterialTheme.typography.titleMedium)
      Gap(8.dp)
      BarChart(
        values = sleepVals.map { it / 60f },
        labels = days.mapIndexed { i, d -> if (!month) dowShort(d.dayOfWeek) else if (i % 5 == 4) "${d.dayOfMonth}" else "" },
        color = MaterialTheme.colorScheme.primary,
        goal = Store.sleepGoalMin / 60f,
      )
    }

    SleepShiftCard()

    if (Store.premium) {
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Vardiya türüne göre enerji", "Energy by shift type"), style = MaterialTheme.typography.titleMedium)
        Text(t("Son 60 gün", "Last 60 days"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Gap(8.dp)
        val byType = energyByType(today)
        if (byType.isEmpty()) {
          Text(t("Enerji kaydı ekledikçe burada görünecek.", "This fills in as you log energy."), style = MaterialTheme.typography.bodySmall)
        } else {
          byType.forEach { (id, avg) ->
            val ty = Store.typeById(id)
            HBar(ty?.let { typeName(it) } ?: t("Vardiyasız", "No shift"), avg, 10f, fmtNum(avg) + " / 10", ty?.let { shiftColor(it) } ?: MaterialTheme.colorScheme.secondary)
          }
        }
      }
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
        t("Vardiya türüne göre enerji, vardiya dağılımı, aylık eğilimler ve dışa aktarma.", "Energy by shift type, shift mix, monthly trends and export."),
      ) { Nav.paywall(t("Detaylı istatistikler", "Detailed statistics")) }
    }

    SectionTitle(t("İçgörüler", "Insights"))
    val ins = insights(today)
    val shown = if (Store.premium) ins else ins.take(1)
    if (shown.isEmpty()) {
      Text(
        t("Birkaç gün uyku ve enerji kaydettikten sonra kişisel içgörüler burada görünecek.", "Personal insights will appear after a few days of sleep and energy logs."),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    shown.forEach { s ->
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Text(s, color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
    }
    if (!Store.premium && ins.size > 1) {
      Text(
        t("+${ins.size - 1} içgörü daha Premium'da", "+${ins.size - 1} more insights with Premium"),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp),
      )
    }

    BigButton(t("Verileri dışa aktar (CSV)", "Export data (CSV)") + if (Store.premium) "" else " 🔒", outlined = true) {
      if (Store.premium) shareCsv(ctx) else Nav.paywall(t("Veri dışa aktarma", "Data export"))
    }
    Spacer(Modifier.height(72.dp))
  }
}

fun categoryName(c: String): String = when (c) {
  "postNight" -> t("Gece vardiyası sonrası", "After a night shift")
  "preNight" -> t("Gece vardiyası günü", "Night-shift day")
  "work" -> t("Gündüz/akşam çalışma günü", "Day/evening work day")
  else -> t("İzin günü", "Day off")
}

/** Vardiya türlerinin uyku süresi ve kalitesine etkisi. */
@Composable
fun SleepShiftCard() {
  val stats = Store.sleepByCategory(60)
  NCard(Modifier.fillMaxWidth()) {
    Text(t("Vardiyalar uykunuzu nasıl etkiliyor?", "How shifts affect your sleep"), style = MaterialTheme.typography.titleMedium)
    Text(t("Son 60 gün · ortalama süre ve kalite", "Last 60 days · average duration and quality"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Gap(8.dp)
    if (stats.isEmpty()) {
      Text(t("Uyku kaydettikçe vardiya türlerine göre karşılaştırma burada görünür.", "As you log sleep, a comparison by shift type appears here."), style = MaterialTheme.typography.bodySmall)
      return@NCard
    }
    val maxMin = maxOf(stats.values.maxOf { it.avgMin }, Store.sleepGoalMin).toFloat()
    listOf("postNight", "preNight", "work", "off").forEach { c ->
      val st = stats[c] ?: return@forEach
      val q = if (st.avgQuality > 0f) " · " + com.nobet.i18n.qualityEmoji[(st.avgQuality.toInt() - 1).coerceIn(0, 4)] + " " + fmtNum(st.avgQuality) + "/5" else ""
      HBar(
        categoryName(c) + t(" (${st.days} gün)", " (${st.days} days)"),
        st.avgMin.toFloat(),
        maxMin,
        fmtDur(st.avgMin) + q,
        when (c) {
          "postNight", "preNight" -> MaterialTheme.colorScheme.primary
          "work" -> Warning
          else -> Success
        },
      )
    }
    val pn = stats["postNight"]
    val off = stats["off"]
    if (pn != null) {
      Gap(8.dp)
      Text(
        t("Gece vardiyalarından sonra ortalama ${fmtHours(pn.avgMin)} uyuyorsunuz", "After night shifts, you sleep an average of ${fmtHours(pn.avgMin)}") +
          if (off != null) t(" (izin günlerinde ${fmtHours(off.avgMin)}).", " (${fmtHours(off.avgMin)} on days off).") else ".",
        fontWeight = FontWeight.SemiBold,
      )
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

/** Yerel, kural tabanlı içgörüler — her biri yeterli veri varsa üretilir. */
fun insights(today: LocalDate): List<String> {
  val out = mutableListOf<String>()
  val range = (0 until 60).map { today.minusDays(it.toLong()) }.filter { Store.hasSleep(it) }

  val post = range.filter { Store.isPostNight(it) }.map { Store.sleepMinOn(it) }
  val other = range.filter { !Store.isPostNight(it) }.map { Store.sleepMinOn(it) }
  if (post.size >= 2) {
    out += t(
      "Gece vardiyalarından sonra ortalama ${fmtHours(post.average().toInt())} uyuyorsunuz. (${post.size} gece)",
      "After night shifts, you sleep an average of ${fmtHours(post.average().toInt())}. (${post.size} nights)",
    )
  }
  val cat = Store.sleepByCategory(60, today)
  val qn = cat["postNight"]
  val qo = cat["off"]
  if (qn != null && qo != null && qn.days >= 2 && qo.days >= 2 && qo.avgQuality - qn.avgQuality >= 0.5f) {
    out += t(
      "Gece sonrası uyku kaliteniz izin günlerine göre daha düşük (${fmtNum(qn.avgQuality)} vs ${fmtNum(qo.avgQuality)} / 5). Karartma perdesi ve kulak tıkacı yardımcı olabilir.",
      "Your sleep quality after nights is lower than on days off (${fmtNum(qn.avgQuality)} vs ${fmtNum(qo.avgQuality)} / 5). Blackout curtains and earplugs can help.",
    )
  }
  if (post.size >= 3 && other.size >= 3) {
    val diff = other.average() - post.average()
    if (diff > 20) {
      out += t(
        "Gece vardiyalarından sonra ortalama ${fmtDur(diff.toInt())} daha az uyuyorsunuz. (${post.size} gece verisi)",
        "After night shifts you sleep ${fmtDur(diff.toInt())} less on average. (based on ${post.size} nights)",
      )
    }
  }

  val pairs = (0 until 60).map { today.minusDays(it.toLong()) }.mapNotNull { d ->
    Store.energyOn(d)?.let { e -> if (Store.hasSleep(d)) Store.sleepMinOn(d) to e else null }
  }
  if (pairs.size >= 7) {
    val good = pairs.filter { it.first >= Store.sleepGoalMin }.map { it.second }
    val bad = pairs.filter { it.first < Store.sleepGoalMin }.map { it.second }
    if (good.size >= 2 && bad.size >= 2) {
      val d = good.average() - bad.average()
      if (d >= 0.5) {
        out += t(
          "Hedefinize ulaştığınız günlerde enerjiniz ${fmtNum(d.toFloat())} puan daha yüksek. (${pairs.size} gün)",
          "Your energy is ${fmtNum(d.toFloat())} points higher on days you meet your sleep goal. (${pairs.size} days)",
        )
      }
    }
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

  val naps = Store.sleeps.count { it.nap && Store.toDate(it.end).isAfter(today.minusDays(14)) }
  if (naps >= 3) {
    out += t("Son 2 haftada $naps şekerleme yaptınız — vardiya öncesi kısa uykular dikkati artırır.", "You took $naps naps in the last 2 weeks — pre-shift naps boost alertness.")
  }
  return out
}
