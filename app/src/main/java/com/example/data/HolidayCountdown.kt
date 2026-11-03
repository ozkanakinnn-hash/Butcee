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
    val currentYear = now.get(Calendar.YEAR)
    val nowMillis = now.timeInMillis

    // Key Hijri-Gregorian approximate anchor dates for the calendar
    // 2026/2027/2028 anchors
    val calRamadanStart = Calendar.getInstance().apply {
      set(Calendar.YEAR, currentYear)
      set(Calendar.MONTH, Calendar.FEBRUARY)
      set(Calendar.DAY_OF_MONTH, 18)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }

    val calEidFitr = Calendar.getInstance().apply {
      set(Calendar.YEAR, currentYear)
      set(Calendar.MONTH, Calendar.MARCH)
      set(Calendar.DAY_OF_MONTH, 20)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }

    val calEidAdha = Calendar.getInstance().apply {
      set(Calendar.YEAR, currentYear)
      set(Calendar.MONTH, Calendar.MAY)
      set(Calendar.DAY_OF_MONTH, 27)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }

    // If all dates in current year passed, project to next year's Ramadan
    val (title, subtitle, targetMillis, targetName) = when {
      nowMillis < calRamadanStart.timeInMillis -> {
        Tuple4(
          "🌙 Ramazan-ı Şerif'e Kalan",
          "11 Ayın Sultanı Mübarek Ramazan Ayı Başlangıcı",
          calRamadanStart.timeInMillis,
          "Ramazan Başlangıcı"
        )
      }
      nowMillis < calEidFitr.timeInMillis -> {
        Tuple4(
          "🌙 Ramazan Bayramı'na Kalan",
          "Mübarek Ramazan Bayramı ve Bayram Namazı",
          calEidFitr.timeInMillis,
          "Ramazan Bayramı"
        )
      }
      nowMillis < calEidAdha.timeInMillis -> {
        Tuple4(
          "🐑 Kurban Bayramı'na Kalan",
          "Mübarek Kurban Bayramı 1. Günü",
          calEidAdha.timeInMillis,
          "Kurban Bayramı"
        )
      }
      else -> {
        // Next year's Ramadan
        val nextRamadan = Calendar.getInstance().apply {
          set(Calendar.YEAR, currentYear + 1)
          set(Calendar.MONTH, Calendar.FEBRUARY)
          set(Calendar.DAY_OF_MONTH, 8)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
        }
        Tuple4(
          "🌙 Ramazan-ı Şerif'e Kalan",
          "Gelecek Yılın Mübarek Ramazan Ayı",
          nextRamadan.timeInMillis,
          "Ramazan Başlangıcı"
        )
      }
    }

    val diffMillis = (targetMillis - nowMillis).coerceAtLeast(0L)
    val totalSec = diffMillis / 1000L
    val days = totalSec / (24 * 3600L)
    val remSec = totalSec % (24 * 3600L)
    val hours = remSec / 3600L
    val minutes = (remSec % 3600L) / 60L
    val seconds = remSec % 60L

    return HolidayCountdownInfo(
      title = title,
      subtitle = subtitle,
      days = days,
      hours = hours,
      minutes = minutes,
      seconds = seconds,
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
