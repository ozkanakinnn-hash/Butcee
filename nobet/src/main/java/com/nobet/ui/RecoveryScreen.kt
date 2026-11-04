@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.nobet.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobet.data.CheckIn
import com.nobet.data.Recovery
import com.nobet.data.ShiftInstance
import com.nobet.data.Store
import com.nobet.i18n.energyLabel
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDur
import com.nobet.i18n.qualityEmoji
import com.nobet.i18n.qualityLabel
import com.nobet.i18n.relDay
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.LocalDate
import kotlin.math.roundToInt

fun scoreColor(score: Int): Color = when (Recovery.band(score)) {
  "good" -> Success
  "mid" -> Warning
  else -> Alert
}

/** Toparlanma Puanı halkası (0–100). */
@Composable
fun ScoreRing(score: Int?, size: Dp = 96.dp, provisional: Boolean = false) {
  val frac = ((score ?: 0) / 100f).coerceIn(0f, 1f)
  val c = if (score == null) MaterialTheme.colorScheme.outline else scoreColor(score)
  val track = MaterialTheme.colorScheme.surfaceVariant
  Box(Modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(Modifier.size(size)) {
      val sw = this.size.minDimension * 0.1f
      val inset = sw / 2
      val s = Size(this.size.width - sw, this.size.height - sw)
      drawArc(track, 135f, 270f, false, topLeft = Offset(inset, inset), size = s, style = Stroke(sw, cap = StrokeCap.Round))
      if (score != null) {
        drawArc(c, 135f, 270f * frac.coerceAtLeast(0.02f), false, topLeft = Offset(inset, inset), size = s, style = Stroke(sw, cap = StrokeCap.Round))
      }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(score?.toString() ?: "–", fontSize = (size.value * 0.28f).sp, fontWeight = FontWeight.Bold)
      if (provisional) Text(t("geçici", "provisional"), style = MaterialTheme.typography.labelSmall)
    }
  }
}

fun shiftLine(w: ShiftInstance): String =
  "${typeName(w.type)} · ${relDay(w.date)} ${fmtClock(w.type.startMin)}–${fmtClock(w.type.endMin)}"

@Composable
fun PendingCheckInCard(w: ShiftInstance) {
  NCard(Modifier.fillMaxWidth(), onClick = { Nav.openCheckin(w.date) }, container = MaterialTheme.colorScheme.primaryContainer) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      ShiftBadge(w.type, 40.dp)
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(
          t("${typeName(w.type)} vardiyası sonrası nasıl toparlanıyorsunuz?", "How are you recovering after your ${typeName(w.type)} shift?"),
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(t("4 kısa soru · ", "4 quick questions · ") + shiftLine(w), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
      }
    }
    Gap(8.dp)
    FilledTonalButton(onClick = { Nav.openCheckin(w.date) }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text(t("Başla", "Start")) }
  }
}

/** Puan çubukları: her çubuk vardiya türünün rengiyle. */
@Composable
private fun ScoreBars(items: List<Recovery.Scored>) {
  val grid = MaterialTheme.colorScheme.outlineVariant
  val colors = items.map { shiftColor(it.w.type) }
  Column(Modifier.fillMaxWidth()) {
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
      val n = items.size.coerceAtLeast(1)
      val slot = size.width / n
      val bw = slot * 0.6f
      for (i in 0..4) {
        val y = size.height * i / 4f
        drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f)
      }
      items.forEachIndexed { i, it ->
        val h = (size.height * it.score / 100f).coerceAtLeast(4f)
        drawRoundRect(colors[i], topLeft = Offset(i * slot + (slot - bw) / 2f, size.height - h), size = Size(bw, h), cornerRadius = CornerRadius(bw / 4f, bw / 4f))
      }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
      items.forEach {
        Text("${it.w.date.dayOfMonth}", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, maxLines = 1)
      }
    }
  }
}

@Composable
fun RecoveryScreen() {
  val now = rememberNow()
  val pending = Recovery.pending(now)
  val all = Recovery.history(if (Store.premium) 365 else 30, now.toLocalDate())
  val latest = all.firstOrNull()

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(t("Toparlanma", "Recovery"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    // Skor (en üstte)
    NCard(Modifier.fillMaxWidth()) {
      Text(t("Skor", "Score"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Gap(8.dp)
      if (latest == null) {
        Text(t("Henüz toparlanma kontrolü yok", "No recovery check-ins yet"), style = MaterialTheme.typography.titleMedium)
        Text(
          t("Her vardiyadan sonra 4 kısa soru soracağız; yalnızca vardiyanın nasıl geçtiği zorunlu. Puanınız ve içgörüler burada görünecek.",
            "After each shift we'll ask 4 quick questions — only how your shift went is required. Your score and insights will appear here."),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ScoreRing(latest.score, 104.dp, latest.c.provisional)
          Spacer(Modifier.width(16.dp))
          Column(Modifier.weight(1f)) {
            Text(Recovery.bandLabel(latest.score), style = MaterialTheme.typography.titleMedium, color = scoreColor(latest.score))
            Text(t("Son kontrol: ", "Latest: ") + shiftLine(latest.w), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Gap(4.dp)
            Text(Recovery.message(latest.score), style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }

    pending.forEach { PendingCheckInCard(it) }
    AdviceCard(now, latest)

    if (all.size >= 2) {
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Son vardiyalar", "Recent shifts"), style = MaterialTheme.typography.titleMedium)
        Text(t("Çubuk rengi vardiya türünü gösterir", "Bar colour shows the shift type"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Gap(8.dp)
        ScoreBars(all.take(14).reversed())
      }
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Vardiya türüne göre toparlanma", "Recovery by shift type"), style = MaterialTheme.typography.titleMedium)
        Gap(8.dp)
        Recovery.byCategory(all).entries.sortedByDescending { it.value.average() }.forEach { (c, v) ->
          val avg = v.average().roundToInt()
          HBar(Recovery.categoryName(c) + t(" (${v.size})", " (${v.size})"), avg.toFloat(), 100f, "$avg", scoreColor(avg))
        }
      }
    }

    // İçgörüler
    SectionTitle(t("Analizler", "Analyses")) 
    val basic = Recovery.basicInsight(all)
    if (basic == null) {
      Text(
        t("Birkaç vardiya sonrası kontrolden sonra vardiyaların toparlanmanızı nasıl etkilediğine dair analizler burada görünecek.",
          "After a few post-shift check-ins, analyses of how your shifts affect your recovery will appear here."),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    } else {
      InsightCard(basic)
    }
    if (Store.premium) {
      Recovery.patternInsights(all).forEach { InsightCard(it) }
      Recovery.suggestion(all)?.let { s ->
        NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.primaryContainer) {
          Text(t("Nazik öneri", "Gentle suggestion"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(s, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
    } else {
      LockedCard(
        t("Örüntüler ve öneriler", "Patterns & suggestions"),
        t("Arka arkaya geceler, kısa geçişler, uzun vardiyalar ve günlük etiketlerine göre içgörüler; eğilime dayalı nazik öneriler.",
          "Insights on consecutive nights, quick turnarounds, long shifts and journal tags; gentle suggestions based on trends."),
      ) { Nav.paywall(t("Toparlanma örüntüleri", "Recovery patterns")) }
    }

    // Geçmiş
    SectionTitle(t("Geçmiş", "History"))
    if (all.isEmpty()) Text(t("Henüz kayıt yok.", "No entries yet."), color = MaterialTheme.colorScheme.onSurfaceVariant)
    all.take(60).forEach { sc ->
      NCard(Modifier.fillMaxWidth(), onClick = { Nav.openCheckin(sc.w.date) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ShiftBadge(sc.w.type, 34.dp)
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(shiftLine(sc.w), fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
              t("Akış ${sc.c.smooth}/10", "Smoothness ${sc.c.smooth}/10") +
                (if (sc.c.energy > 0) t(" · enerji ${sc.c.energy}/10", " · energy ${sc.c.energy}/10") else "") +
                (if (sc.c.sleepStatus == "slept") " · 🛏 ${fmtDur(sc.c.sleepMin)}" else ""),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text("${sc.score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = scoreColor(sc.score))
        }
      }
    }
    if (!Store.premium && Recovery.history(365).size > all.size) {
      TextButton(onClick = { Nav.paywall(t("Sınırsız toparlanma geçmişi", "Unlimited recovery history")) }) {
        Text(t("30 günden eski kayıtlar için Premium", "Premium for history older than 30 days"))
      }
    }
    Spacer(Modifier.height(24.dp))
  }
}

@Composable
fun InsightCard(text: String) {
  NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
    Text(text, color = MaterialTheme.colorScheme.onSecondaryContainer)
  }
}

@Composable
private fun Question(n: Int, title: String, optional: Boolean) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
      Text("$n", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Spacer(Modifier.width(10.dp))
    Column {
      Text(title, style = MaterialTheme.typography.titleMedium)
      Text(
        if (optional) t("İsteğe bağlı", "Optional") else t("Zorunlu", "Required"),
        style = MaterialTheme.typography.labelSmall,
        color = if (optional) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
      )
    }
  }
}

/** Vardiya sonrası 4 soruluk toparlanma kontrolü (tam ekran katman). */
@Composable
fun CheckInScreen() {
  val date = Nav.checkinDate ?: Store.today()
  val w = Store.workOn(date)
  val old = Recovery.checkIn(date)
  val oldNote = Store.notes[Store.key(date)]

  var status by remember { mutableStateOf(old?.sleepStatus ?: "") }
  var sleepMin by remember { mutableIntStateOf(old?.sleepMin?.takeIf { it > 0 } ?: Store.sleepGoalMin) }
  var quality by remember { mutableIntStateOf(old?.quality ?: 0) }
  var energy by remember { mutableFloatStateOf((old?.energy ?: 0).toFloat().coerceAtLeast(1f)) }
  var energySet by remember { mutableStateOf((old?.energy ?: 0) > 0) }
  var smooth by remember { mutableFloatStateOf((old?.smooth ?: 6).toFloat()) }
  var smoothSet by remember { mutableStateOf(old != null) }
  var note by remember { mutableStateOf(oldNote?.text ?: "") }
  val tags = remember { mutableStateListOf<String>().apply { addAll(oldNote?.tags ?: emptyList()) } }
  var result by remember { mutableStateOf<CheckIn?>(null) }
  var confirmDelete by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(t("Toparlanma kontrolü", "Recovery check-in")) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(
      Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      val r = result
      if (r != null) {
        // Sonuç
        val sc = Recovery.score(r)
        Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          ScoreRing(sc, 160.dp, r.provisional)
          Gap()
          Text(Recovery.bandLabel(sc), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = scoreColor(sc))
          Gap(8.dp)
          Text(Recovery.message(sc), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
          if (r.provisional) {
            Gap(8.dp)
            Text(
              t("Puan geçici — dinlendikten sonra güncelleyebilirsiniz.", "This score is provisional — update it after you've rested."),
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Gap()
        BigButton(t("Tamam", "Done")) { Nav.pop() }
        TextButton(onClick = { result = null }, modifier = Modifier.fillMaxWidth()) { Text(t("Yanıtları düzenle", "Edit answers")) }
      } else {

      if (w != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ShiftBadge(w.type, 36.dp)
          Spacer(Modifier.width(10.dp))
          Text(shiftLine(w), style = MaterialTheme.typography.titleMedium)
        }
      }
      Text(
        t("İlk üç soru isteğe bağlı; yalnızca vardiyanızın nasıl geçtiğini sormak zorundayız. Doğru ya da yanlış cevap yok.",
          "The first three questions are optional; only how your shift went is required. There are no right or wrong answers."),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
      )

      // Q1
      NCard(Modifier.fillMaxWidth()) {
        Question(1, t("Bu vardiyadan sonra ne kadar uyudunuz?", "How long did you sleep after this shift?"), true)
        Gap(8.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("slept" to t("Uyudum", "I slept"), "nap" to t("Sadece kısa uyku", "Only a short nap"), "notYet" to t("Henüz uyumadım", "Haven't slept yet")).forEach { (k, l) ->
            FilterChip(selected = status == k, onClick = { status = if (status == k) "" else k }, label = { Text(l) })
          }
        }
        if (status == "slept") {
          Gap(8.dp)
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { sleepMin = (sleepMin - 30).coerceAtLeast(0) }, modifier = Modifier.height(48.dp)) { Text("−30") }
            Text(fmtDur(sleepMin), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { sleepMin = (sleepMin + 30).coerceAtMost(14 * 60) }, modifier = Modifier.height(48.dp)) { Text("+30") }
          }
        }
      }

      // Q2
      NCard(Modifier.fillMaxWidth()) {
        Question(2, t("Uyku kaliteniz nasıldı?", "How was your sleep quality?"), true)
        Gap(8.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          qualityEmoji.forEachIndexed { i, e ->
            val q = i + 1
            Surface(
              onClick = { quality = if (quality == q) 0 else q },
              shape = RoundedCornerShape(14.dp),
              color = if (quality == q) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.size(52.dp),
            ) { Box(contentAlignment = Alignment.Center) { Text(e, fontSize = 26.sp) } }
          }
        }
        Text(
          if (quality == 0) t("Yanıtlanmadı", "Not answered") else qualityLabel(quality),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Q3
      NCard(Modifier.fillMaxWidth()) {
        Question(3, t("Şu anki enerji seviyeniz nedir?", "What is your current energy level?"), true)
        Gap(4.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            if (energySet) "${energy.roundToInt()}/10 · ${energyLabel(energy.roundToInt())}" else t("Yanıtlanmadı — kaydırın", "Not answered — slide to answer"),
            Modifier.weight(1f),
            fontWeight = if (energySet) FontWeight.SemiBold else FontWeight.Normal,
          )
          if (energySet) TextButton(onClick = { energySet = false }) { Text(t("Temizle", "Clear")) }
        }
        Slider(value = energy, onValueChange = { energy = it; energySet = true }, valueRange = 1f..10f, steps = 8)
      }

      // Q4 (zorunlu)
      NCard(Modifier.fillMaxWidth()) {
        Question(4, t("Vardiyanız ne kadar sorunsuz geçti?", "How smoothly did your shift go?"), false)
        Gap(4.dp)
        Text(
          if (smoothSet) "${smooth.roundToInt()}/10 · ${smoothLabel(smooth.roundToInt())}" else t("Lütfen kaydırarak yanıtlayın", "Please slide to answer"),
          fontWeight = if (smoothSet) FontWeight.SemiBold else FontWeight.Normal,
          color = if (smoothSet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
        )
        Slider(value = smooth, onValueChange = { smooth = it; smoothSet = true }, valueRange = 1f..10f, steps = 8)
        Row(Modifier.fillMaxWidth()) {
          Text(t("1 · Çok zor", "1 · Very hard"), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
          Text(t("10 · Çok sorunsuz", "10 · Very smooth"), style = MaterialTheme.typography.labelSmall)
        }
      }

      // Not + günlük etiketleri
      NCard(Modifier.fillMaxWidth()) {
        Text(t("Vardiya notu (isteğe bağlı)", "Shift note (optional)"), style = MaterialTheme.typography.titleSmall)
        Gap(6.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Recovery.journalTags.forEach { tg ->
            FilterChip(selected = tg in tags, onClick = { if (tg in tags) tags.remove(tg) else tags.add(tg) }, label = { Text(Recovery.tagName(tg)) })
          }
        }
        OutlinedTextField(
          value = note,
          onValueChange = { note = it.take(280) },
          label = { Text(t("Örn. Çok yoğun bir gündü", "e.g. Very busy day")) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
        )
      }

      BigButton(t("Kaydet", "Save"), enabled = smoothSet) {
        val c = CheckIn(
          shiftDate = Store.key(date),
          smooth = smooth.roundToInt(),
          sleepStatus = status,
          sleepMin = if (status == "slept") sleepMin else 0,
          quality = quality,
          energy = if (energySet) energy.roundToInt() else 0,
        )
        Store.saveCheckIn(c)
        Store.saveNote(date, note, tags.toList())
        result = c
      }
      if (!smoothSet) {
        Text(
          t("Kaydetmek için yalnızca 4. soruyu yanıtlamanız yeterli.", "Answering question 4 is all you need to save."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      if (old != null) {
        TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
          Text(t("Bu kontrolü sil", "Delete this check-in"), color = MaterialTheme.colorScheme.error)
        }
      }
      Spacer(Modifier.height(24.dp))
      }
    }
  }
  if (confirmDelete) {
    ConfirmDialog(t("Kontrol silinsin mi?", "Delete check-in?"), shiftLineOrDate(date), t("Sil", "Delete"), { confirmDelete = false }) {
      Store.deleteCheckIn(date)
      confirmDelete = false
      Nav.pop()
    }
  }
}

private fun shiftLineOrDate(d: LocalDate): String = Store.workOn(d)?.let { shiftLine(it) } ?: relDay(d)

fun smoothLabel(v: Int): String = when {
  v <= 2 -> t("Çok zordu", "Very hard")
  v <= 4 -> t("Zorlayıcıydı", "Tough")
  v <= 6 -> t("İdare eder", "Okay")
  v <= 8 -> t("Rahat geçti", "Smooth")
  else -> t("Çok sorunsuz", "Very smooth")
}
