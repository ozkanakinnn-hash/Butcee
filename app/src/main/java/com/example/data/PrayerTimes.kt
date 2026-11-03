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

data class CountdownResult(
  val title: String,         // "İftara Kalan Vakit" or "İmsaka Kalan Vakit"
  val targetTimeName: String,// "Akşam (İftar)" or "İmsak (Sahur)"
  val targetTimeString: String, // "18:45"
  val hours: Long,
  val minutes: Long,
  val seconds: Long,
  val totalSecondsRemaining: Long,
  val isIftarTimeNow: Boolean, // True when countdown reached 0 (cannon time!)
  val progress: Float          // 0.0f to 1.0f progress through the fasting/night period
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

  /**
   * Calculates dynamic countdown:
   * - Before Maghrib (Aksam): Counts down to Iftar! Title: "İftara Kalan Vakit"
   * - After Maghrib (Aksam): Automatically flips to Imsak (next morning sahur)! Title: "İmsaka Kalan Vakit"
   * Guarantees non-negative, non-NaN values.
   */
  fun calculateCountdown(prayerTimes: PrayerTimes, calendar: Calendar = Calendar.getInstance()): CountdownResult {
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentSecond = calendar.get(Calendar.SECOND)
    val currentTotalSec = (currentHour * 3600L) + (currentMinute * 60L) + currentSecond

    val imsakMinutes = parseTimeToMinutes(prayerTimes.imsak)
    val aksamMinutes = parseTimeToMinutes(prayerTimes.aksam)

    val imsakSec = imsakMinutes * 60L
    val aksamSec = aksamMinutes * 60L

    val isBeforeIftar = currentTotalSec < aksamSec && currentTotalSec >= imsakSec
    val isJustIftar = kotlin.math.abs(currentTotalSec - aksamSec) < 30 // within 30 seconds of iftar

    val (title, targetName, targetTimeStr, targetTotalSec, periodStartSec, periodDurationSec) = when {
      currentTotalSec < imsakSec -> {
        // Night after midnight, before Imsak -> counting down to Imsak
        val prevAksamSec = aksamSec - (24 * 3600L)
        val duration = (imsakSec - prevAksamSec).coerceAtLeast(1L)
        val remaining = (imsakSec - currentTotalSec).coerceAtLeast(0L)
        Tuple6("İmsaka Kalan Vakit", "İmsak (Sahur)", prayerTimes.imsak, imsakSec, currentTotalSec, remaining)
      }
      currentTotalSec < aksamSec -> {
        // Fasting period -> counting down to Iftar (Aksam)
        val duration = (aksamSec - imsakSec).coerceAtLeast(1L)
        val remaining = (aksamSec - currentTotalSec).coerceAtLeast(0L)
        Tuple6("İftara Kalan Vakit", "Akşam (İftar)", prayerTimes.aksam, aksamSec, imsakSec, remaining)
      }
      else -> {
        // After Iftar -> counting down to tomorrow's Imsak
        val nextImsakSec = (24 * 3600L) + imsakSec
        val duration = (nextImsakSec - aksamSec).coerceAtLeast(1L)
        val remaining = (nextImsakSec - currentTotalSec).coerceAtLeast(0L)
        Tuple6("İmsaka Kalan Vakit", "İmsak (Sahur)", prayerTimes.imsak, nextImsakSec, aksamSec, remaining)
      }
    }

    val remainingSec = periodDurationSec.coerceAtLeast(0L)
    val hours = remainingSec / 3600L
    val minutes = (remainingSec % 3600L) / 60L
    val seconds = remainingSec % 60L

    val progress = when {
      isBeforeIftar -> {
        val totalFasting = (aksamSec - imsakSec).toFloat().coerceAtLeast(1f)
        val elapsed = (currentTotalSec - imsakSec).toFloat().coerceAtLeast(0f)
        (elapsed / totalFasting).coerceIn(0f, 1f)
      }
      else -> {
        0.85f
      }
    }

    return CountdownResult(
      title = title,
      targetTimeName = targetName,
      targetTimeString = targetTimeStr,
      hours = hours,
      minutes = minutes,
      seconds = seconds,
      totalSecondsRemaining = remainingSec,
      isIftarTimeNow = isJustIftar,
      progress = progress
    )
  }

  fun formatTimer(hours: Long, minutes: Long, seconds: Long): String {
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hours.coerceAtLeast(0L), minutes.coerceAtLeast(0L), seconds.coerceAtLeast(0L))
  }
}

private data class Tuple6<A, B, C, D, E, F>(
  val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
