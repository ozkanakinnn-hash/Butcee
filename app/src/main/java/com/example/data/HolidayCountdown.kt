package com.example.data

import java.util.Calendar
import java.util.Locale

data class HolidayCountdownInfo(
  val title: String,
  val subtitle: String,
  val days: Long,
  val hours: Long,
  val minutes: Long,
  val seconds: Long,
  val targetName: String
)

object HolidayCountdownCalculator {

  /**
   * Calculates smart holiday countdown:
   * 1. If before next Ramadan start -> "Ramazan-ı Şerif'e Kalan"
   * 2. If during Ramadan -> "Ramazan Bayramı'na Kalan"
   * 3. If during/after Ramadan Bayramı -> "Kurban Bayramı'na Kalan"
   */
  fun calculate(now: Calendar = Calendar.getInstance()): HolidayCountdownInfo {
    // Tarihler sabit değil: Diyanet Ramazan tablosu + hicri takvimden (IslamicDays) bulunur
    val todayKey = RamadanCalendar.dateKey(now)
    val year = now.get(Calendar.YEAR)
    val days = (IslamicDays.forYear(year).values + IslamicDays.forYear(year + 1).values).sortedBy { it.dateKey }
    fun firstAfter(prefix: String) = days.firstOrNull { it.title.startsWith(prefix) && it.dateKey > todayKey }?.dateKey
    val nextRamadan = listOfNotNull(RamadanCalendar.nextStartKey(todayKey), firstAfter("Ramazan Başlangıcı")).minOrNull()
    val fitr = firstAfter("Ramazan Bayramı 1.")
    val adha = firstAfter("Kurban Bayramı 1.")

    val (title, subtitle, targetKey, targetName) = when {
      RamadanCalendar.isRamadanDay(todayKey) && fitr != null ->
        Tuple4("🌙 Ramazan Bayramı'na Kalan", "Mübarek Ramazan Bayramı ve Bayram Namazı", fitr, "Ramazan Bayramı")
      adha != null && (nextRamadan == null || adha < nextRamadan) ->
        Tuple4("🐑 Kurban Bayramı'na Kalan", "Mübarek Kurban Bayramı 1. Günü", adha, "Kurban Bayramı")
      nextRamadan != null -> Tuple4(
        "🌙 Ramazan-ı Şerif'e Kalan",
        if (nextRamadan.startsWith("$year-")) "11 Ayın Sultanı Mübarek Ramazan Ayı Başlangıcı" else "Gelecek Yılın Mübarek Ramazan Ayı",
        nextRamadan, "Ramazan Başlangıcı"
      )
      else -> Tuple4("🌙 Ramazan-ı Şerif'e Kalan", "", todayKey, "Ramazan Başlangıcı")
    }
    val p = targetKey.split("-").map { it.toInt() }
    val target = Calendar.getInstance().apply { clear(); set(p[0], p[1] - 1, p[2], 0, 0, 0) }
    val diffMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    val totalSec = diffMillis / 1000L
    return HolidayCountdownInfo(
      title = title,
      subtitle = subtitle,
      days = totalSec / 86400L,
      hours = (totalSec % 86400L) / 3600L,
      minutes = (totalSec % 3600L) / 60L,
      seconds = totalSec % 60L,
      targetName = targetName
    )
  }

  fun format(info: HolidayCountdownInfo): String {
    return String.format(
      Locale.ROOT,
      "%d Gün %02d:%02d:%02d",
      info.days,
      info.hours,
      info.minutes,
      info.seconds
    )
  }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
