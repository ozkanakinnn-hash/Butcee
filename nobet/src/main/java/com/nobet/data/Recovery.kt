package com.nobet.data

import com.nobet.i18n.fmtNum
import com.nobet.i18n.t
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Vardiya sonrası toparlanma kontrolü (uyku takibi ve uyku borcunun yerine).
 * Q1 uyku süresi, Q2 kalite, Q3 enerji isteğe bağlı; Q4 vardiyanın akışı ZORUNLU.
 */
data class CheckIn(
  val shiftDate: String, // kontrolün ait olduğu vardiyanın başlangıç günü (ISO)
  val smooth: Int, // Q4: 1..10 (zorunlu)
  val sleepStatus: String = "", // "" yanıtlanmadı · slept · notYet · nap
  val sleepMin: Int = 0, // Q1 (yalnızca slept)
  val quality: Int = 0, // Q2: 0 = yanıtlanmadı, 1..5
  val energy: Int = 0, // Q3: 0 = yanıtlanmadı, 1..10
  val at: Long = System.currentTimeMillis(),
) {
  val provisional: Boolean get() = sleepStatus == "notYet"
}

/** Vardiya günlüğü: vardiya başına kısa not + hızlı etiketler (asla paylaşılmaz). */
data class JournalNote(val text: String, val tags: List<String>)

object Recovery {
  val journalTags = listOf("busy", "calm", "exhausted", "overtime", "shortStaffed", "difficult")

  /** Toparlanma Puanı 0–100 (PRD Ek B.5). Yanıtlanmayan isteğe bağlı sorular dışarıda bırakılır. */
  fun score(c: CheckIn): Int {
    var sum = 0.0
    var w = 0.0
    val need = Store.sleepGoalMin.coerceAtLeast(60).toDouble()
    when (c.sleepStatus) {
      "slept" -> { sum += 0.35 * minOf(c.sleepMin / need, 1.0); w += 0.35 }
      "nap" -> { sum += 0.35 * minOf(90 / need, 1.0); w += 0.35 }
      "notYet" -> { w += 0.35 }
    }
    if (c.quality in 1..5) { sum += 0.20 * (c.quality - 1) / 4.0; w += 0.20 }
    if (c.energy in 1..10) { sum += 0.25 * (c.energy - 1) / 9.0; w += 0.25 }
    sum += 0.20 * (c.smooth.coerceIn(1, 10) - 1) / 9.0
    w += 0.20
    return (100 * sum / w).roundToInt().coerceIn(0, 100)
  }

  fun band(score: Int): String = when {
    score >= 75 -> "good"
    score >= 50 -> "mid"
    else -> "low"
  }

  fun bandLabel(score: Int): String = when (band(score)) {
    "good" -> t("İyi toparlanmışsınız", "Well recovered")
    "mid" -> t("Kısmen toparlandınız", "Partly recovered")
    else -> t("Biraz daha dinlenmeye ihtiyaç var", "Needs more rest")
  }

  /** Destekleyici, yargılamayan tek cümle (PRD Ek B.9). */
  fun message(score: Int): String = when (band(score)) {
    "good" -> t("Güzel toparlandınız. Bu vardiyadan sonra yaptıklarınız size iyi geliyor gibi.",
      "Nicely recovered. Whatever you did after this shift seems to work for you.")
    "mid" -> t("Yoldasınız. Bugün fırsat buldukça kendinize nazik davranın.",
      "You're getting there. Go easy on yourself today if you can.")
    else -> t("Yorucu bir vardiyaydı. Bu vardiyadan sonra biraz daha dinlenmeye ihtiyacınız olabilir — kısa bir kestirme ya da sakin bir akşam iyi gelebilir.",
      "That was a demanding one. You may need a bit more rest after this shift — a short nap or a calm evening can help.")
  }

  fun checkIn(d: LocalDate): CheckIn? = Store.checkins[Store.key(d)]
  fun scoreOn(d: LocalDate): Int? = checkIn(d)?.let { score(it) }

  /** Bitmiş, son 36 saatte sona eren ve henüz kontrolü yapılmamış vardiyalar (en yenisi önce). */
  fun pending(now: LocalDateTime = LocalDateTime.now(Store.zone)): List<ShiftInstance> {
    val out = mutableListOf<ShiftInstance>()
    for (i in 0..3) {
      val w = Store.workOn(now.toLocalDate().minusDays(i.toLong())) ?: continue
      if (w.end.isAfter(now)) continue
      if (Duration.between(w.end, now).toHours() >= 36) continue
      if (checkIn(w.date) == null) out += w
    }
    return out
  }

  /** Vardiya bittikten sonra 36 saat içinde düzenlenebilir. */
  fun editable(d: LocalDate, now: LocalDateTime = LocalDateTime.now(Store.zone)): Boolean {
    val w = Store.workOn(d) ?: return false
    return !w.end.isAfter(now) && Duration.between(w.end, now).toHours() < 36
  }

  /** Vardiya kategorisi: gece / gündüz / akşam / uzun (≥ 12 sa). */
  fun category(w: ShiftInstance): String = when {
    w.type.durationMin >= 12 * 60 && !w.type.isNight -> "long"
    w.type.isNight -> "night"
    w.type.id == Builtins.EVENING || w.start.hour >= 13 -> "evening"
    else -> "day"
  }

  fun categoryName(c: String): String = when (c) {
    "night" -> t("Gece vardiyaları", "Night shifts")
    "evening" -> t("Akşam vardiyaları", "Evening shifts")
    "long" -> t("Uzun vardiyalar (12 sa+)", "Long shifts (12 h+)")
    else -> t("Gündüz vardiyaları", "Day shifts")
  }

  /** Arka arkaya kaçıncı gece (gece değilse 0). */
  fun nightRunIndex(d: LocalDate): Int {
    var n = 0
    var x = d
    while (true) {
      val w = Store.workOn(x) ?: break
      if (!w.type.isNight) break
      n++
      x = x.minusDays(1)
    }
    return n
  }

  /** Önceki vardiyanın bitişi ile bu vardiyanın başlangıcı arasındaki saat (bulunamazsa null). */
  fun gapHoursBefore(d: LocalDate): Long? {
    val w = Store.workOn(d) ?: return null
    for (i in 1..3) {
      val p = Store.workOn(d.minusDays(i.toLong())) ?: continue
      return Duration.between(p.end, w.start).toHours()
    }
    return null
  }

  data class Scored(val w: ShiftInstance, val c: CheckIn, val score: Int)

  fun history(days: Int, today: LocalDate = Store.today()): List<Scored> =
    (0 until days).mapNotNull { i ->
      val d = today.minusDays(i.toLong())
      val c = checkIn(d) ?: return@mapNotNull null
      val w = Store.workOn(d) ?: return@mapNotNull null
      Scored(w, c, score(c))
    }

  fun byCategory(list: List<Scored>): Map<String, List<Int>> =
    list.groupBy { category(it.w) }.mapValues { (_, v) -> v.map { it.score } }

  /** Ücretsiz içgörü: vardiya türüne göre karşılaştırma (yeterli veri varsa). */
  fun basicInsight(list: List<Scored>): String? {
    val by = byCategory(list).filterValues { it.size >= 2 }
    if (by.size < 2) {
      val only = by.entries.firstOrNull() ?: return null
      return t("${categoryName(only.key)} sonrası toparlanmanız ortalama ${only.value.average().roundToInt()}. (${only.value.size} vardiya)",
        "After ${categoryName(only.key).lowercase()}, your recovery averages ${only.value.average().roundToInt()}. (${only.value.size} shifts)")
    }
    val best = by.maxBy { it.value.average() }
    val worst = by.minBy { it.value.average() }
    val diff = (best.value.average() - worst.value.average()).roundToInt()
    return t("En iyi ${categoryName(best.key).lowercase()} sonrasında toparlanıyorsunuz; ${categoryName(worst.key).lowercase()} sonrasında toparlanma genellikle $diff puan daha düşük.",
      "You recover best after ${categoryName(best.key).lowercase()}; after ${categoryName(worst.key).lowercase()} it's usually $diff points lower.")
  }

  /** Premium örüntü içgörüleri (PRD Ek B.4). */
  fun patternInsights(list: List<Scored>): List<String> {
    val out = mutableListOf<String>()
    // Arka arkaya gece vardiyaları
    val nights = list.filter { it.w.type.isNight }
    val first = nights.filter { nightRunIndex(it.w.date) == 1 }.map { it.score }
    val later = nights.filter { nightRunIndex(it.w.date) >= 2 }.map { it.score }
    if (first.size >= 2 && later.size >= 2 && first.average() - later.average() >= 5) {
      out += t("Arka arkaya gece vardiyalarından sonra toparlanmanız genellikle daha düşük (ilk gece ${first.average().roundToInt()}, sonraki geceler ${later.average().roundToInt()}).",
        "Your recovery is usually lower after consecutive night shifts (first night ${first.average().roundToInt()}, later nights ${later.average().roundToInt()}).")
    }
    // Kısa geçişler
    val quick = list.filter { (gapHoursBefore(it.w.date) ?: 99) < 11 }.map { it.score }
    val normal = list.filter { (gapHoursBefore(it.w.date) ?: 99) >= 11 }.map { it.score }
    if (quick.size >= 2 && normal.size >= 2 && normal.average() - quick.average() >= 5) {
      out += t("Vardiyalar arasında 11 saatten az olduğunda toparlanmanız yaklaşık ${(normal.average() - quick.average()).roundToInt()} puan düşüyor.",
        "When there are less than 11 hours between shifts, your recovery drops by about ${(normal.average() - quick.average()).roundToInt()} points.")
    }
    // Akış ile toparlanma
    val smooth = list.filter { it.c.smooth >= 8 }.map { it.score }
    val rough = list.filter { it.c.smooth <= 4 }.map { it.score }
    if (smooth.size >= 2 && rough.size >= 2 && smooth.average() - rough.average() >= 8) {
      out += t("Vardiyanız sorunsuz geçtiğinde (8–10) belirgin şekilde daha iyi toparlanıyorsunuz — sakin bir vardiya gerçekten fark yaratıyor.",
        "When your shift goes smoothly (8–10), you recover noticeably better — a calm shift really helps.")
    }
    // Uzun vardiyalardan sonra
    val longs = list.filter { it.w.type.durationMin >= 12 * 60 }.map { it.score }
    val shorts = list.filter { it.w.type.durationMin < 12 * 60 }.map { it.score }
    if (longs.size >= 2 && shorts.size >= 2 && shorts.average() - longs.average() >= 5) {
      out += t("12 saat ve üzeri vardiyalardan sonra toparlanmanız ortalama ${longs.average().roundToInt()}; daha kısa vardiyalarda ${shorts.average().roundToInt()}.",
        "After 12 h+ shifts your recovery averages ${longs.average().roundToInt()}, vs ${shorts.average().roundToInt()} after shorter ones.")
    }
    // Günlük etiketleri
    for (tag in journalTags) {
      val tagged = list.filter { Store.notes[Store.key(it.w.date)]?.tags?.contains(tag) == true }.map { it.score }
      val other = list.filter { Store.notes[Store.key(it.w.date)]?.tags?.contains(tag) != true }.map { it.score }
      if (tagged.size >= 2 && other.size >= 2 && other.average() - tagged.average() >= 8) {
        out += t("“${tagName(tag)}” olarak işaretlediğiniz vardiyalardan sonra toparlanmanız genellikle biraz daha düşük.",
          "After shifts you tagged “${tagName(tag)}”, your recovery is usually a little lower.")
      }
    }
    return out
  }

  /** Eğilime dayalı nazik öneri (tek bir güne değil, son 3 kontrole bakar). */
  fun suggestion(list: List<Scored>): String? {
    val last = list.take(3)
    if (last.size < 3) return null
    val avg = last.map { it.score }.average()
    val nightsRecent = last.count { it.w.type.isNight }
    return when {
      avg < 50 && nightsRecent >= 2 -> t("Son gece vardiyaları yorucu geçti. Programınız uygunsa bir sonraki gece serisinden sonra fazladan bir dinlenme günü planlamayı deneyebilirsiniz.",
        "Your recent nights have been demanding. If your schedule allows, you could plan an extra rest day after your next run of nights.")
      avg < 50 -> t("Son birkaç vardiya sonrası toparlanma düşük seyrediyor. Vardiya öncesi kısa bir kestirme ve vardiya sonrası sakin bir saat iyi gelebilir.",
        "Recovery has been low after your last few shifts. A short pre-shift nap and a calm hour after work could help.")
      avg >= 75 -> t("Son vardiyalardan sonra güzel toparlanıyorsunuz — mevcut rutininiz size iyi geliyor.",
        "You've been recovering well after recent shifts — your current routine seems to suit you.")
      else -> null
    }
  }

  fun tagName(tag: String): String = when (tag) {
    "busy" -> t("Çok yoğun", "Very busy")
    "calm" -> t("Sakin", "Calm")
    "exhausted" -> t("Çok yoruldum", "Felt exhausted")
    "overtime" -> t("Fazla mesai", "Overtime")
    "shortStaffed" -> t("Personel eksikti", "Short-staffed")
    "difficult" -> t("Zor hasta/müşteri", "Difficult patient/customer")
    else -> tag
  }

  fun avgText(v: List<Int>): String = if (v.isEmpty()) "–" else fmtNum(v.average().toFloat()).substringBefore(',').substringBefore('.')
}
