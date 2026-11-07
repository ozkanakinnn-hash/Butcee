package com.example.data

import java.util.Calendar
import java.util.Locale

data class PrayerTimes(
  val imsak: String,
  val gunes: String,
  val ogle: String,
  val ikindi: String,
  val aksam: String, // İftar vakti
  val yatsi: String,
  val dateStr: String,
  val city: String,
  val district: String
)

/** Ana sayfa sayacının o anki modu. */
enum class CountdownMode {
  SAHUR,    // İmsaka kalan (oruç günü öncesi)
  IFTAR,    // İftara kalan
  TERAVIH,  // İftardan sonra yatsıya (teravihe) kalan — yalnızca Ramazan geceleri
  PRAYER    // Oruç yoksa: sıradaki namaz vaktine kalan
}

data class CountdownResult(
  val title: String,         // "İftara Kalan Vakit", "Teravihe Kalan Vakit", "Öğle Vaktine Kalan"...
  val targetTimeName: String,// "Akşam (İftar)", "Yatsı (Teravih)", "Öğle"...
  val targetTimeString: String, // "18:45"
  val hours: Long,
  val minutes: Long,
  val seconds: Long,
  val totalSecondsRemaining: Long,
  val isIftarTimeNow: Boolean, // True when countdown reached 0 (cannon time!)
  val progress: Float,         // 0.0f to 1.0f progress through the current period
  val mode: CountdownMode = CountdownMode.PRAYER,
  val nextLabel: String = ""   // Hedeften sonraki vakit, ör. "Yatsı 20:05"
)

object PrayerTimeUtils {

  /**
   * Safely extracts HH:mm from strings like "18:30 (TRT)" or "18:30:00" or "05:15"
   * Always returns a normalized "HH:mm" string or a safe default. Never fails or throws.
   */
  fun cleanTimeString(raw: String?, fallback: String = "00:00"): String {
    if (raw.isNullOrBlank()) return fallback
    val regex = Regex("""\b(\d{1,2}):(\d{2})\b""")
    val match = regex.find(raw) ?: return fallback
    val (h, m) = match.destructured
    val hour = h.toIntOrNull() ?: 0
    val minute = m.toIntOrNull() ?: 0
    return String.format(Locale.ROOT, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
  }

  fun parseTimeToMinutes(timeStr: String): Int {
    val clean = cleanTimeString(timeStr)
    val parts = clean.split(":")
    if (parts.size != 2) return 0
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    return h * 60 + m
  }

  private val prayerNames = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")

  /**
   * Akıllı geri sayım.
   * Oruç günü (Ramazan ya da Oruç Takvimi'nde "tutuyorum" işaretli gün):
   *   imsaktan önce → İmsaka kalan, gündüz → İftara kalan.
   * İftardan sonra, ertesi gün Ramazan ise (teravih gecesi) → yatsıya / Teravihe kalan.
   * Yatsıdan sonra, ertesi gün oruç ise → İmsaka (sahura) kalan.
   * Diğer tüm durumlarda → sıradaki namaz vaktine kalan (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı).
   */
  fun calculateCountdown(
    prayerTimes: PrayerTimes,
    fastedDates: Set<String> = emptySet(),
    calendar: Calendar = Calendar.getInstance()
  ): CountdownResult {
    val nowSec = calendar.get(Calendar.HOUR_OF_DAY) * 3600L +
      calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND)

    val timeStrings = listOf(
      prayerTimes.imsak, prayerTimes.gunes, prayerTimes.ogle,
      prayerTimes.ikindi, prayerTimes.aksam, prayerTimes.yatsi
    ).map { cleanTimeString(it) }
    val secs = timeStrings.map { parseTimeToMinutes(it) * 60L }
    val imsakSec = secs[0]
    val aksamSec = secs[4]
    val yatsiSec = secs[5]
    val day = 24 * 3600L

    val todayKey = RamadanCalendar.dateKey(calendar)
    val tomorrowKey = RamadanCalendar.dateKey((calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) })
    val ramadanTomorrow = RamadanCalendar.isRamadanDay(tomorrowKey)
    val fastToday = RamadanCalendar.isRamadanDay(todayKey) || fastedDates.contains(todayKey)
    val fastTomorrow = ramadanTomorrow || fastedDates.contains(tomorrowKey)

    fun label(i: Int) = "${prayerNames[i % 6]} ${timeStrings[i % 6]}"

    // (mod, başlık, hedef adı, hedef saat metni, hedef sn, dönem başlangıcı sn, sonraki etiket)
    data class Plan(val mode: CountdownMode, val title: String, val name: String, val timeStr: String,
                    val target: Long, val start: Long, val next: String)

    val plan = when {
      nowSec < imsakSec && fastToday ->
        Plan(CountdownMode.SAHUR, "İmsaka Kalan Vakit", "İmsak (Sahur)", timeStrings[0], imsakSec, yatsiSec - day, label(1))
      nowSec in imsakSec until aksamSec && fastToday ->
        Plan(CountdownMode.IFTAR, "İftara Kalan Vakit", "Akşam (İftar)", timeStrings[4], aksamSec, imsakSec, label(5))
      nowSec in aksamSec until yatsiSec && ramadanTomorrow ->
        Plan(CountdownMode.TERAVIH, "Teravihe Kalan Vakit", "Yatsı (Teravih)", timeStrings[5], yatsiSec, aksamSec, label(0))
      nowSec >= yatsiSec && fastTomorrow ->
        Plan(CountdownMode.SAHUR, "İmsaka Kalan Vakit", "İmsak (Sahur)", timeStrings[0], imsakSec + day, yatsiSec, label(1))
      else -> {
        // Sıradaki namaz vakti
        val idx = secs.indexOfFirst { it > nowSec }
        val targetIdx = if (idx == -1) 0 else idx
        val target = if (idx == -1) imsakSec + day else secs[idx]
        val start = when (idx) {
          -1 -> yatsiSec
          0 -> yatsiSec - day
          else -> secs[idx - 1]
        }
        val name = prayerNames[targetIdx]
        Plan(CountdownMode.PRAYER, "$name Vaktine Kalan", name, timeStrings[targetIdx], target, start, label(targetIdx + 1))
      }
    }

    val remainingSec = (plan.target - nowSec).coerceAtLeast(0L)
    val duration = (plan.target - plan.start).coerceAtLeast(1L)
    val progress = ((nowSec - plan.start).toFloat() / duration).coerceIn(0f, 1f)

    return CountdownResult(
      title = plan.title,
      targetTimeName = plan.name,
      targetTimeString = plan.timeStr,
      hours = remainingSec / 3600L,
      minutes = (remainingSec % 3600L) / 60L,
      seconds = remainingSec % 60L,
      totalSecondsRemaining = remainingSec,
      isIftarTimeNow = fastToday && kotlin.math.abs(nowSec - aksamSec) < 30,
      progress = progress,
      mode = plan.mode,
      nextLabel = plan.next
    )
  }

  fun formatTimer(hours: Long, minutes: Long, seconds: Long): String {
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hours.coerceAtLeast(0L), minutes.coerceAtLeast(0L), seconds.coerceAtLeast(0L))
  }
}
