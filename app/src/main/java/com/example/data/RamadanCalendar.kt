package com.example.data

import java.util.Calendar
import java.util.Locale

/**
 * Türkiye (Diyanet) Ramazan ayı oruç günleri: ilk oruç günü .. son oruç günü (dahil).
 * NOT: 2027 ve sonrası astronomik hesaba dayalı yaklaşık tarihlerdir; Diyanet takvimi
 * yayımlandığında güncelleyin.
 */
object RamadanCalendar {

  private data class Range(val start: String, val end: String)

  private val ranges = listOf(
    Range("2026-02-19", "2026-03-19"),
    Range("2027-02-08", "2027-03-08"),
    Range("2028-01-28", "2028-02-25"),
    Range("2029-01-16", "2029-02-13"),
    Range("2030-01-05", "2030-02-03")
  )

  fun dateKey(cal: Calendar): String = String.format(
    Locale.ROOT, "%04d-%02d-%02d",
    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
  )

  /** "YYYY-MM-DD" anahtarı Ramazan oruç günü mü? (ISO biçim sözlük sırasıyla karşılaştırılabilir) */
  fun isRamadanDay(dateKey: String): Boolean = ranges.any { dateKey >= it.start && dateKey <= it.end }

  fun isRamadanDay(cal: Calendar): Boolean = isRamadanDay(dateKey(cal))
}
