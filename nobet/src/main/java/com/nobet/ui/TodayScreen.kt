package com.nobet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nobet.data.Store
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDate
import com.nobet.i18n.fmtDur
import com.nobet.i18n.fmtNum
import com.nobet.i18n.relDay
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun rememberNow(periodMs: Long = 30_000L): LocalDateTime {
  var now by remember { mutableStateOf(LocalDateTime.now(Store.zone)) }
  LaunchedEffect(periodMs) {
    while (true) {
      delay(periodMs)
      now = LocalDateTime.now(Store.zone)
    }
  }
  return now
}

@Composable
fun TodayScreen() {
  val now = rememberNow()
  val today = now.toLocalDate()
  val next = Store.nextShift(now)
  val (debt, debtDays) = Store.debt7(today)

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        val greet = when (now.hour) {
          in 5..11 -> t("Günaydın", "Good morning")
          in 12..17 -> t("İyi günler", "Good afternoon")
          in 18..21 -> t("İyi akşamlar", "Good evening")
          else -> t("İyi geceler", "Good night")
        }
        val nm = Store.name.trim()
        Text(if (nm.isEmpty()) greet else "$greet, $nm", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(fmtDate(today), color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(onClick = { Nav.open("settings") }) { Icon(Icons.Filled.Settings, t("Ayarlar", "Settings")) }
    }

    // Uyku sayacı açıksa
    if (Store.sleepStart > 0L) {
      val el = ((System.currentTimeMillis() - Store.sleepStart) / 60000L).toInt()
      NCard(Modifier.fillMaxWidth(), onClick = { Nav.open("sleepmode") }, container = MaterialTheme.colorScheme.primaryContainer) {
        Text(t("Uyku kaydı açık", "Sleep timer running"), color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(fmtDur(el), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Gap(8.dp)
        Button(onClick = { Nav.dialog = "wake" }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(t("Uyandım", "I'm awake")) }
      }
    }

    // Sıradaki vardiya
    NCard(Modifier.fillMaxWidth(), onClick = { Nav.tab = 1 }) {
      if (next == null) {
        Text(t("Yaklaşan vardiya yok", "No upcoming shifts"), style = MaterialTheme.typography.titleMedium)
        Text(t("Takvimden vardiya ekleyin veya bir düzen uygulayın.", "Add shifts in the calendar or apply a rotation."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Gap(8.dp)
        FilledTonalButton(onClick = { Nav.open("patterns") }) { Text(t("Düzen uygula", "Apply rotation")) }
      } else {
        val running = !next.start.isAfter(now)
        Row(verticalAlignment = Alignment.CenterVertically) {
          ShiftBadge(next.type, 44.dp)
          Spacer(Modifier.width(14.dp))
          Column(Modifier.weight(1f)) {
            Text(
              (if (running) t("ŞU AN", "NOW") else relDay(next.date).uppercase(com.nobet.i18n.appLocale())) + " · " + typeName(next.type),
              style = MaterialTheme.typography.labelLarge,
              color = shiftColor(next.type),
              fontWeight = FontWeight.Bold,
            )
            Text("${fmtClock(next.type.startMin)} – ${fmtClock(next.type.endMin)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val txt = if (running) {
              t("Bitmesine ", "Ends in ") + fmtDur(Duration.between(now, next.end).toMinutes().toInt())
            } else {
              t("Başlamasına ", "Starts in ") + fmtDur(Duration.between(now, next.start).toMinutes().toInt())
            }
            Text(txt, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    // İpucu
    val tip = tipFor(now, debt)
    if (tip != null) {
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Row {
          Icon(Icons.Filled.Lightbulb, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
          Spacer(Modifier.width(10.dp))
          Text(tip, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      NCard(Modifier.weight(1f), onClick = { Nav.tab = 2 }) {
        Text(t("Uyku borcu", "Sleep debt"), style = MaterialTheme.typography.labelLarge)
        Gap(8.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          DebtRing(debt, size = 96.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(if (debtDays == 0) "–" else fmtDur(debt), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
          }
        }
        Gap(4.dp)
        Text(
          if (debtDays == 0) t("Henüz uyku kaydı yok", "No sleep logged yet") else debtLabel(debt),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      NCard(Modifier.weight(1f)) {
        Text(t("Bugünkü enerji", "Energy today"), style = MaterialTheme.typography.labelLarge)
        Gap(8.dp)
        val en = Store.energyOn(today)
        Text(
          if (en == null) "–" else fmtNum(en) + " / 10",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Gap(8.dp)
        FilledTonalButton(onClick = { Nav.dialog = "energy" }, modifier = Modifier.fillMaxWidth()) {
          Text(if (en == null) t("Kaydet", "Check in") else t("Güncelle", "Add"), maxLines = 1)
        }
      }
    }

    PartnerTodayCard()

    // Önümüzdeki günler
    NCard(Modifier.fillMaxWidth(), onClick = { Nav.tab = 1 }) {
      Text(t("Önümüzdeki günler", "Next days"), style = MaterialTheme.typography.labelLarge)
      Gap(10.dp)
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (i in 0 until 7) {
          val d = today.plusDays(i.toLong())
          val ty = Store.shiftOn(d)
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(com.nobet.i18n.dowShort(d.dayOfWeek), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            Gap(4.dp)
            if (ty != null) ShiftBadge(ty, 34.dp) else Text("·", Modifier.height(34.dp), color = MaterialTheme.colorScheme.outline)
            Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
    Spacer(Modifier.height(72.dp))
  }
}

private fun tipFor(now: LocalDateTime, debt: Int): String? {
  if (Store.sleepStart > 0L) return null
  val next = Store.nextShift(now)
  val last = Store.lastEndedShift(now)
  if (last != null && last.type.isNight && Duration.between(last.end, now).toMinutes() in 0..240) {
    return t("Gece vardiyası bitti. Eve dönerken güneş gözlüğü takın, odanızı karanlık ve serin tutun.",
      "Night shift done. Wear sunglasses on the way home and keep your room dark and cool.")
  }
  if (next != null && next.start.isAfter(now)) {
    val h = Duration.between(now, next.start).toMinutes() / 60.0
    if (next.type.isNight && h in 2.0..8.0) {
      return t("Bu gece vardiyanız var. Şimdi 60–90 dakikalık bir kestirme dikkatinizi korumanıza yardımcı olur.",
        "Night shift tonight. A 60–90 minute nap now helps you stay sharp.")
    }
    if (next.type.isNight && h in 8.0..30.0) {
      return t("Gece vardiyasından önce sabah biraz geç kalkmaya çalışın; kafeini vardiyanın ilk yarısında tüketin.",
        "Before a night shift, try to sleep in; keep caffeine to the first half of your shift.")
    }
  }
  if (debt > 420) {
    return t("Uyku borcunuz yüksek. İzin gününüzde 1–2 saat fazladan uyumayı planlayın.",
      "Your sleep debt is high. Plan 1–2 extra hours of sleep on your next day off.")
  }
  return t("Her gün aynı saatlerde 3–4 saatlik bir 'çapa uykusu' vardiya değişimlerinde ritminizi korur.",
    "A consistent 3–4 hour 'anchor sleep' at the same time every day helps your rhythm across shift changes.")
}
