package com.nobet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobet.data.SleepEntry
import com.nobet.data.Store
import com.nobet.i18n.dowShort
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDur
import com.nobet.i18n.fmtHours
import com.nobet.i18n.qualityEmoji
import com.nobet.i18n.relDay
import com.nobet.i18n.t

@Composable
fun SleepScreen() {
  val now = rememberNow()
  val today = now.toLocalDate()
  var deleting by remember { mutableStateOf<SleepEntry?>(null) }

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(t("Uyku", "Sleep"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

    NCard(Modifier.fillMaxWidth()) {
      if (Store.sleepStart > 0L) {
        val el = ((System.currentTimeMillis() - Store.sleepStart) / 60000L).toInt()
        Text(t("Uyuyorsunuz", "You're asleep"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(fmtDur(el), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Gap()
        BigButton(t("Uyandım", "I'm awake")) { Nav.dialog = "wake" }
      } else {
        Text(t("Uykunuzu tek dokunuşla kaydedin", "Log your sleep with one tap"), style = MaterialTheme.typography.titleMedium)
        Gap()
        BigButton(t("Uyumaya başla", "Start sleep")) {
          Store.startSleep()
          Nav.open("sleepmode")
        }
        Gap(8.dp)
        BigButton(t("Geçmiş uyku ekle", "Add past sleep"), outlined = true) { Nav.dialog = "sleeplog" }
      }
    }

    // Uyku borcu
    val (debt, days) = Store.debt7(today)
    NCard(Modifier.fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        DebtRing(debt, size = 104.dp) {
          Text(if (days == 0) "–" else fmtDur(debt), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
          Text(t("7 günlük uyku borcu", "7-day sleep debt"), style = MaterialTheme.typography.titleMedium)
          Text(
            if (days == 0) t("Kayıt ekledikçe hesaplanır.", "Calculated as you log sleep.") else debtLabel(debt),
            color = if (days == 0) MaterialTheme.colorScheme.onSurfaceVariant else debtColor(debt),
          )
          Text(
            t("Hedef: ${fmtDur(Store.sleepGoalMin)} / gece · $days gün veri", "Goal: ${fmtDur(Store.sleepGoalMin)} / night · $days days of data"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    if (Store.premium) {
      NCard(Modifier.fillMaxWidth()) {
        val d14 = Store.debt14(today)
        Text(t("Gelişmiş analiz (14 gün)", "Advanced analysis (14 days)"), style = MaterialTheme.typography.titleMedium)
        Text(
          t("Toparlanma dahil borç: ", "Debt incl. recovery: ") + fmtDur(d14),
          fontWeight = FontWeight.SemiBold,
          color = debtColor(d14),
        )
        Gap(8.dp)
        // Vardiya türüne göre ortalama uyku
        val byType = mutableMapOf<String, MutableList<Int>>()
        for (i in 0 until 30) {
          val d = today.minusDays(i.toLong())
          if (!Store.hasSleep(d)) continue
          val key = when {
            Store.isPostNight(d) -> t("Gece vardiyası sonrası", "After night shift")
            Store.workOn(d) != null -> t("Çalışma günü", "Work day")
            else -> t("İzin günü", "Day off")
          }
          byType.getOrPut(key) { mutableListOf() }.add(Store.sleepMinOn(d))
        }
        if (byType.isEmpty()) {
          Text(t("Analiz için birkaç gün uyku kaydı gerekli.", "Log a few days of sleep to see the analysis."), style = MaterialTheme.typography.bodySmall)
        } else {
          val maxV = byType.values.maxOf { it.average() }.toFloat()
          byType.forEach { (k, v) ->
            val avg = v.average().toInt()
            HBar(k, avg.toFloat(), maxOf(maxV, Store.sleepGoalMin.toFloat()), fmtDur(avg), MaterialTheme.colorScheme.primary)
          }
        }
        Gap(8.dp)
        if (d14 > 0) {
          val perDay = (d14 / 3).coerceAtMost(120)
          Text(
            t("Toparlanma planı: önümüzdeki 3 izin gününüzde ~${fmtDur(perDay)} fazladan uyuyun veya 20–30 dk şekerlemeler yapın.",
              "Recovery plan: sleep ~${fmtDur(perDay)} extra on your next 3 days off, or take 20–30 min naps."),
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    } else {
      LockedCard(
        t("Gelişmiş uyku borcu analizi", "Advanced sleep debt analysis"),
        t("14 günlük toparlanmalı borç, vardiya türüne göre uyku ve toparlanma planı.", "14-day debt with recovery, sleep by shift type and a recovery plan."),
      ) { Nav.paywall(t("Gelişmiş uyku borcu analizi", "Advanced sleep debt analysis")) }
    }

    // Son 7 gün grafiği
    NCard(Modifier.fillMaxWidth()) {
      Text(t("Son 7 gün", "Last 7 days"), style = MaterialTheme.typography.titleMedium)
      Gap(8.dp)
      val ds = (6 downTo 0).map { today.minusDays(it.toLong()) }
      BarChart(
        values = ds.map { Store.sleepMinOn(it) / 60f },
        labels = ds.map { dowShort(it.dayOfWeek) },
        color = MaterialTheme.colorScheme.primary,
        goal = Store.sleepGoalMin / 60f,
      )
      Text(t("Kesikli çizgi: uyku hedefiniz", "Dashed line: your sleep goal"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    // Geçmiş
    SectionTitle(t("Geçmiş", "History"))
    val limit = if (Store.premium) Long.MAX_VALUE else 30L
    val list = Store.sleeps.filter { java.time.temporal.ChronoUnit.DAYS.between(Store.toDate(it.end), today) < limit }.sortedByDescending { it.end }
    if (list.isEmpty()) {
      Text(t("Henüz kayıt yok.", "No entries yet."), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    list.take(60).forEach { e ->
      val s = Store.toLdt(e.start)
      val en = Store.toLdt(e.end)
      NCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(qualityEmoji[(e.quality - 1).coerceIn(0, 4)], fontSize = 28.sp)
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(relDay(en.toLocalDate()) + if (e.nap) t(" · şekerleme", " · nap") else "", fontWeight = FontWeight.SemiBold)
            Text("${fmtClock(s.hour * 60 + s.minute)} – ${fmtClock(en.hour * 60 + en.minute)}", style = MaterialTheme.typography.bodySmall)
          }
          Text(fmtHours(e.minutes), fontWeight = FontWeight.Bold)
          IconButton(onClick = { deleting = e }) { Icon(Icons.Filled.Delete, t("Sil", "Delete"), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
      }
    }
    if (!Store.premium && Store.sleeps.size > list.size) {
      TextButton(onClick = { Nav.paywall(t("Sınırsız uyku geçmişi", "Unlimited sleep history")) }) {
        Text(t("30 günden eski kayıtlar için Premium", "Premium for history older than 30 days"))
      }
    }
    Spacer(Modifier.height(72.dp))
  }

  deleting?.let { e ->
    ConfirmDialog(t("Kayıt silinsin mi?", "Delete entry?"), fmtHours(e.minutes), t("Sil", "Delete"), { deleting = null }) {
      Store.deleteSleep(e.id)
      deleting = null
    }
  }
}

/** Uyku modu: çok loş, büyük "Uyandım" düğmesi. */
@Composable
fun SleepModeScreen() {
  val now = rememberNow(15_000L)
  val dim = Color(0xFF9E7468)
  Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding().padding(24.dp)) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
      Text(fmtClock(now.hour * 60 + now.minute), color = dim, fontSize = 72.sp, fontWeight = FontWeight.Light)
      if (Store.sleepStart > 0L) {
        val el = ((System.currentTimeMillis() - Store.sleepStart) / 60000L).toInt()
        Text(t("Uyku süresi: ", "Asleep for: ") + fmtDur(el), color = dim.copy(alpha = 0.8f))
      }
      Gap(12.dp)
      Text(t("İyi uykular 🌙  Telefonu bırakabilirsiniz.", "Sleep well 🌙  You can put the phone down."), color = dim.copy(alpha = 0.6f), textAlign = TextAlign.Center)
    }
    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
      if (Store.sleepStart > 0L) {
        Button(
          onClick = { Nav.dialog = "wake" },
          modifier = Modifier.fillMaxWidth().height(72.dp),
          shape = RoundedCornerShape(36.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A140F), contentColor = Color(0xFFE6B8A8)),
        ) { Text(t("Uyandım", "I'm awake"), fontSize = 20.sp) }
      }
      TextButton(onClick = { Nav.pop() }, modifier = Modifier.fillMaxWidth()) { Text(t("Kapat", "Close"), color = dim) }
    }
  }
}
