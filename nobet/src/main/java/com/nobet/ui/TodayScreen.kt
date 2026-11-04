package com.nobet.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
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
  var askName by remember { mutableStateOf(false) }
  if (askName) NameDialog { askName = false }

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
        // Üst karşılama: "Merhaba, <ad> 👋" — ad yoksa eklemek için kısayol
        Text(
          if (nm.isEmpty()) t("Merhaba 👋", "Hello 👋") else t("Merhaba, $nm 👋", "Hello, $nm 👋"),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Text("$greet · ${fmtDate(today)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (nm.isEmpty()) {
          TextButton(onClick = { askName = true }, contentPadding = PaddingValues(0.dp)) {
            Text(t("Adınızı ekleyin", "Add your name"))
          }
        }
      }
      IconButton(onClick = { Nav.open("settings") }) { Icon(Icons.Filled.Settings, t("Ayarlar", "Settings")) }
    }

    // Sıradaki vardiya — canlı geri sayım (en üstte)
    NCard(Modifier.fillMaxWidth(), onClick = { Nav.tab = 1 }, container = MaterialTheme.colorScheme.primaryContainer) {
      if (next == null) {
        Text(t("Yaklaşan vardiya yok", "No upcoming shifts"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(t("Takvimden vardiya ekleyin veya bir düzen uygulayın.", "Add shifts in the calendar or apply a rotation."), color = MaterialTheme.colorScheme.onPrimaryContainer)
        Gap(8.dp)
        FilledTonalButton(onClick = { Nav.open("patterns") }) { Text(t("Düzen uygula", "Apply rotation")) }
      } else {
        val running = !next.start.isAfter(now)
        Row(verticalAlignment = Alignment.CenterVertically) {
          ShiftBadge(next.type, 40.dp)
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(
              (if (running) t("ŞU AN", "NOW") else relDay(next.date).uppercase(com.nobet.i18n.appLocale())) + " · " + typeName(next.type),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.Bold,
            )
            Text(
              "${fmtClock(next.type.startMin)} – ${fmtClock(next.type.endMin)}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
        Gap(10.dp)
        Text(
          if (running) t("Vardiyanın bitmesine", "Shift ends in") else t("Vardiyanın başlamasına", "Shift starts in"),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Countdown(if (running) next.end else next.start)
      }
    }

    // Resmî tatil / izin bilgisi
    com.nobet.data.Holidays.on(today)?.let { h ->
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Text("🎌 " + t("Bugün resmî tatil: ", "Public holiday today: ") + com.nobet.data.Holidays.name(h), color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
    }
    Store.leaves[Store.key(today)]?.let { l ->
      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Text("🏖 " + t("Bugün: ", "Today: ") + leaveName(l) + t(" — iyi dinlenmeler!", " — enjoy your time off!"), color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
    }

    // Bekleyen toparlanma kontrolü (en yeni vardiya)
    com.nobet.data.Recovery.pending(now).firstOrNull()?.let { PendingCheckInCard(it) }

    PartnerTodayCard()

    // Aylık takvim: çalışma günleri ve izin günleri farklı renkte
    HomeCalendar(today)
    Spacer(Modifier.height(72.dp))
  }
}

@Composable
private fun NameDialog(onDone: () -> Unit) {
  var v by remember { mutableStateOf(Store.name) }
  AlertDialog(
    onDismissRequest = onDone,
    title = { Text(t("Size nasıl hitap edelim?", "What should we call you?")) },
    text = {
      OutlinedTextField(
        value = v,
        onValueChange = { v = it.take(30) },
        label = { Text(t("Ad veya takma ad", "Name or nickname")) },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(onClick = {
        Store.name = v.trim()
        Store.save()
        onDone()
      }) { Text(t("Kaydet", "Save")) }
    },
    dismissButton = { TextButton(onClick = onDone) { Text(t("İptal", "Cancel")) } },
  )
}

private fun tipFor(now: LocalDateTime): String? {
  val next = Store.nextShift(now)
  val last = Store.lastEndedShift(now)
  if (last != null && last.type.isNight && Duration.between(last.end, now).toMinutes() in 0..240) {
    return t("Gece vardiyası bitti. Eve dönerken güneş gözlüğü takın, odanızı karanlık ve serin tutun.",
      "Night shift done. Wear sunglasses on the way home and keep your room dark and cool.")
  }
  if (next != null && next.start.isAfter(now)) {
    val h = Duration.between(now, next.start).toMinutes() / 60.0
    if (next.type.isNight && h in 2.0..8.0) {
      return t("Bu gece vardiyanız var. Vardiyadan önce kısa bir kestirme dikkatinizi korumanıza yardımcı olabilir.",
        "Night shift tonight. A short nap beforehand can help you stay sharp.")
    }
  }
  val recent = com.nobet.data.Recovery.history(7, now.toLocalDate()).take(3)
  if (recent.size >= 2 && recent.map { it.score }.average() < 50) {
    return t("Son vardiyalar yorucu geçti. Fırsat buldukça kendinize dinlenme zamanı ayırın — bu da işin bir parçası.",
      "Your recent shifts have been demanding. Make time to rest when you can — it's part of the job too.")
  }
  return professionTip(now.dayOfYear)
}

/** Mesleğe göre ipucu (profil boşsa genel ipuçları); her gün değişir. */
private fun professionTip(seed: Int): String {
  val general = listOf(
    t("Her gün aynı saatlerde 3–4 saatlik bir 'çapa uykusu' vardiya değişimlerinde ritminizi korur.",
      "A consistent 3–4 hour 'anchor sleep' at the same time every day helps your rhythm across shift changes."),
    t("Vardiya boyunca küçük yudumlarla su için; susuzluk yorgunluk gibi hissettirebilir.",
      "Sip water throughout your shift; dehydration can feel like tiredness."),
  )
  val byRole = when (Store.role) {
    "nurse" -> listOf(
      t("Uzun koğuş vardiyalarında mola anlarında omuz ve bacaklarınızı esnetin; kısa hareketler yorgunluğu azaltır.",
        "On long ward shifts, stretch your shoulders and legs during breaks; short movement eases fatigue."),
      t("İlaç hazırlama gibi dikkat isteyen işleri mümkünse vardiyanın ilk yarısına planlayın.",
        "If you can, plan attention-heavy tasks like medication prep for the first half of the shift."),
    )
    "security" -> listOf(
      t("Tek başına gece devriyesinde dikkat düştüğünde kısa bir yürüyüş ve parlak ışık uyanık kalmanıza yardımcı olur.",
        "On solo night patrols, a short walk and bright light help when alertness drops."),
      t("24 saatlik nöbetlerde izin veriliyorsa 20 dakikalık kısa kestirmeler planlayın.",
        "On 24-hour duties, plan 20-minute naps if your role allows it."),
    )
    "factory" -> listOf(
      t("Gece vardiyasından sonra eve araçla dönüyorsanız yorgunsanız kısa bir mola verin ya da toplu taşımayı düşünün.",
        "Driving home after a night shift? If you feel drowsy, take a short break or consider public transport."),
      t("Gürültülü ortamdan sonra uyumadan önce sessiz bir yarım saat zihninizi sakinleştirir.",
        "After a noisy shift, a quiet half hour before bed helps your mind settle."),
    )
    "hospitality" -> listOf(
      t("Gece resepsiyonunda ekran parlaklığını azaltıp ortamı aydınlık tutmak dikkatinizi korur.",
        "On night reception, dim your screen and keep the room well lit to stay alert."),
      t("Ayakta geçen vardiyalardan sonra bacaklarınızı birkaç dakika yukarı kaldırmak iyi gelir.",
        "After shifts on your feet, raising your legs for a few minutes helps."),
    )
    else -> emptyList()
  }
  val all = byRole + general
  return all[seed % all.size]
}

/** Kafein ve öğün zamanlaması önerileri (PRD Ek B.6) — genel sağlıklı yaşam bilgisi. */
private fun nutritionAdvice(now: LocalDateTime): Pair<String, String>? {
  val next = Store.nextShift(now) ?: return null
  val hoursTo = Duration.between(now, next.start).toMinutes() / 60.0
  fun clk(x: LocalDateTime) = com.nobet.i18n.fmtClock(x.hour * 60 + x.minute)
  if (next.type.isNight) {
    if (hoursTo > 14) {
      return t("Yarın gece vardiyanız var — bu gece iyi uyuyabilmek için 14:00'ten sonra kafeini sınırlamaya çalışın.",
        "Night shift tomorrow — try to limit caffeine after 14:00 so you sleep well tonight.") to
        t("Öğünlerinizi normal saatlerde yiyin; akşam yemeğini hafif tutun.", "Eat at your usual times; keep dinner light.")
    }
    val sleepAfter = next.end.plusHours(1)
    return t("Kafeini vardiyanın ilk yarısında tüketin; ${clk(sleepAfter.minusHours(6))} sonrası almamaya çalışın.",
      "Keep caffeine to the first half of the shift; try to skip it after ${clk(sleepAfter.minusHours(6))}.") to
      t("Ana öğünü ${clk(next.start.minusHours(3))}–${clk(next.start.minusHours(2))} arası yemeyi deneyin; 00:00–06:00 arası hafif atıştırmalık, uyumadan önce küçük bir kahvaltı.",
        "Try to eat your main meal between ${clk(next.start.minusHours(3))} and ${clk(next.start.minusHours(2))}; keep it light between 00:00 and 06:00, and have a small breakfast before sleep.")
  }
  if (hoursTo > 30 || !next.start.isAfter(now)) return null
  val bed = next.start.minusMinutes(90L + Store.sleepGoalMin)
  return t("Uykunuzu korumak için yaklaşık ${clk(bed.minusHours(6))}'ten sonra kafein almamaya çalışın.",
    "To protect your sleep, try to skip caffeine after about ${clk(bed.minusHours(6))}.") to
    t("Akşam yemeğini ${clk(bed.minusHours(2))} öncesinde bitirmeye çalışın; uyandıktan sonraki bir saat içinde kahvaltı edin.",
      "Try to finish dinner before ${clk(bed.minusHours(2))}; have breakfast within an hour of waking.")
}

/** Gün türü: çalışma · izin (düzen izni ya da izin günü) · boş. */
private fun dayKind(d: java.time.LocalDate): String {
  val leave = Store.leaves[Store.key(d)]
  if (leave != null && leave != "annualHalf") return "off"
  val ty = Store.shiftOn(d) ?: return ""
  return if (ty.isOff) "off" else "work"
}

@Composable
private fun HomeCalendar(today: java.time.LocalDate) {
  var offset by remember { mutableStateOf(0L) }
  val ym = java.time.YearMonth.from(today).plusMonths(offset)
  val first = ym.atDay(1)
  val lead = first.dayOfWeek.value - 1
  val len = ym.lengthOfMonth()
  val rows = (lead + len + 6) / 7
  val workC = MaterialTheme.colorScheme.primary
  val offC = Success
  NCard(Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = { offset-- }) { Icon(Icons.Filled.ChevronLeft, t("Önceki ay", "Previous month")) }
      Text(
        fmtDate(first, "LLLL yyyy").replaceFirstChar { it.titlecase(com.nobet.i18n.appLocale()) },
        Modifier.weight(1f),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      IconButton(onClick = { offset++ }) { Icon(Icons.Filled.ChevronRight, t("Sonraki ay", "Next month")) }
    }
    Row(Modifier.fillMaxWidth()) {
      java.time.DayOfWeek.values().forEach {
        Text(
          com.nobet.i18n.dowShort(it),
          Modifier.weight(1f),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }
    Gap(4.dp)
    for (r in 0 until rows) {
      Row(Modifier.fillMaxWidth()) {
        for (c in 0 until 7) {
          val idx = r * 7 + c - lead
          Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
            if (idx in 0 until len) {
              val d = first.plusDays(idx.toLong())
              val kind = dayKind(d)
              val hol = com.nobet.data.Holidays.on(d) != null
              val bg = when (kind) {
                "work" -> workC
                "off" -> offC
                else -> Color.Transparent
              }
              val fg = when {
                kind.isNotEmpty() -> Color.White
                hol -> Alert
                else -> MaterialTheme.colorScheme.onSurface
              }
              Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(bg)
                  .then(if (d == today) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp)) else Modifier)
                  .clickable { Nav.daySheet = d },
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  "${d.dayOfMonth}",
                  color = fg,
                  fontWeight = if (d == today || hol) FontWeight.Bold else FontWeight.Normal,
                  style = MaterialTheme.typography.bodyMedium,
                  textDecoration = if (hol) androidx.compose.ui.text.style.TextDecoration.Underline else null,
                )
              }
            }
          }
        }
      }
    }
    Gap(8.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      LegendDot(workC, t("Çalışma", "Work"))
      LegendDot(offC, t("İzin", "Off"))
      Text(t("Altı çizili: resmî tatil", "Underlined: holiday"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun LegendDot(c: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(c))
    Spacer(Modifier.width(6.dp))
    Text(label, style = MaterialTheme.typography.labelMedium)
  }
}

/** Canlı geri sayım: 24 saatten azsa SS:DD:SN, fazlaysa gün + saat. */
@Composable
private fun Countdown(target: LocalDateTime) {
  val now = rememberNow(1_000L)
  val sec = Duration.between(now, target).seconds.coerceAtLeast(0)
  val txt = if (sec >= 86_400) {
    val d = sec / 86_400
    val h = (sec % 86_400) / 3600
    t("$d gün $h sa", "${d}d ${h}h")
  } else {
    String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60)
  }
  Text(
    txt,
    fontSize = 44.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
  )
}

/** Son toparlanma skoruna (ve cevaplara) göre değişen tavsiyeler. */
private fun scoreAdvice(latest: com.nobet.data.Recovery.Scored?): List<String> {
  if (latest == null) {
    return listOf(t("Bir sonraki vardiyanızdan sonra 4 kısa soruyu yanıtlayın; tavsiyeler skorunuza göre kişiselleşecek.",
      "Answer the 4 quick questions after your next shift; advice will adapt to your score."))
  }
  val out = mutableListOf<String>()
  val c = latest.c
  when (com.nobet.data.Recovery.band(latest.score)) {
    "low" -> {
      out += t("Skorunuz düşük: bugün mümkünse ağır işleri erteleyin ve kendinize dinlenme zamanı ayırın.",
        "Your score is low: if you can, postpone heavy tasks today and make time to rest.")
      out += t("20–30 dakikalık kısa bir kestirme ya da erken bir yatış toparlanmanıza yardımcı olabilir.",
        "A 20–30 minute nap or an early night can help you recover.")
      out += t("Bol su için; ek kafein yerine kısa bir yürüyüş ve gün ışığı deneyin.",
        "Drink plenty of water; try a short walk and daylight instead of extra caffeine.")
    }
    "mid" -> {
      out += t("Kısmen toparlandınız: hafif bir yürüyüş ve düzenli öğünler gününüzü dengeler.",
        "Partly recovered: a light walk and regular meals will help balance your day.")
      out += t("Bir sonraki vardiyadan önce yatış saatinizi biraz öne almayı deneyin.",
        "Try going to bed a little earlier before your next shift.")
    }
    else -> {
      out += t("İyi toparlandınız: rutininiz işe yarıyor, aynı uyku saatlerini korumaya çalışın.",
        "Well recovered: your routine works — try to keep the same sleep times.")
    }
  }
  if (c.sleepStatus == "notYet") out += t("Henüz uyumadınız: karanlık, serin ve sessiz bir odada dinlenmeye çalışın.",
    "You haven't slept yet: try resting in a dark, cool, quiet room.")
  if (c.smooth <= 4) out += t("Zor bir vardiyaydı; vardiya sonrası 15 dakikayı yalnızca kendinize ayırın.",
    "That was a tough shift; give yourself 15 minutes just for you afterwards.")
  if (c.energy in 1..3) out += t("Enerjiniz düşük: araç kullanmadan önce kısa bir mola verin.",
    "Your energy is low: take a short break before driving.")
  return out
}

@Composable
fun AdviceCard(now: LocalDateTime, latest: com.nobet.data.Recovery.Scored?) {
  val adv = nutritionAdvice(now)
  NCard(Modifier.fillMaxWidth()) {
    Text(t("Tavsiyeler", "Advice"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (latest != null) {
      Text(
        t("Skorunuza göre (${latest.score})", "Based on your score (${latest.score})"),
        style = MaterialTheme.typography.labelSmall,
        color = scoreColor(latest.score),
      )
    }
    Gap(6.dp)
    scoreAdvice(latest).forEach {
      Text("• $it", style = MaterialTheme.typography.bodyMedium)
      Gap(4.dp)
    }
    if (adv != null) {
      Text("☕ " + adv.first, style = MaterialTheme.typography.bodyMedium)
      Gap(4.dp)
      Text("🍽 " + adv.second, style = MaterialTheme.typography.bodyMedium)
      Gap(4.dp)
    }
    Text(t("Genel öneridir, tıbbi tavsiye değildir.", "General guidance, not medical advice."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }

}
