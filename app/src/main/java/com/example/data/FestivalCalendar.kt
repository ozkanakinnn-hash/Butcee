package com.example.data

import java.util.Calendar

enum class Festival(val greeting: String, val emoji: String) {
  RAMAZAN_BAYRAMI("Ramazan Bayramınız Mübarek Olsun", "🍬"),
  KURBAN_BAYRAMI("Kurban Bayramınız Mübarek Olsun", "🐑")
}

/** Bugünün bayram / kandil / Ramazan durumu ve buna uygun mahya yazısı. */
object FestivalCalendar {

  private var cachedYear = -1
  private var cachedDays: Map<String, SpecialCalendarDay> = emptyMap()

  private fun daysFor(year: Int): Map<String, SpecialCalendarDay> {
    if (year != cachedYear) {
      cachedDays = IslamicDays.forYear(year)
      cachedYear = year
    }
    return cachedDays
  }

  private fun todaySpecial(cal: Calendar): SpecialCalendarDay? =
    daysFor(cal.get(Calendar.YEAR))[RamadanCalendar.dateKey(cal)]

  /** Bugün bayramsa bayram türü (bayram günlerinde süsleme otomatik açılır). */
  @Suppress("UNUSED_PARAMETER")
  fun today(prefs: AppPreferences, cal: Calendar = Calendar.getInstance()): Festival? {
    val special = todaySpecial(cal) ?: return null
    if (special.category != "Dini Bayram") return null
    return if (special.title.contains("Ramazan")) Festival.RAMAZAN_BAYRAMI else Festival.KURBAN_BAYRAMI
  }

  /** Cami görselinde minareler arasına gerilecek mahya (yoksa null). */
  fun mahya(prefs: AppPreferences, cal: Calendar = Calendar.getInstance()): List<String>? {
    today(prefs, cal)?.let { return listOf("BAYRAMINIZ", "MÜBAREK OLSUN") }
    val todayKey = RamadanCalendar.dateKey(cal)
    if (RamadanCalendar.isRamadanDay(todayKey)) {
      // Ramazan'ın son üç gününde veda mahyası
      val in3 = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 3) }
      return if (!RamadanCalendar.isRamadanDay(in3)) listOf("ELVEDA", "YA ŞEHR-İ RAMAZAN")
      else listOf("HOŞ GELDİN", "YA ŞEHR-İ RAMAZAN")
    }
    if (todaySpecial(cal)?.category == "Mübarek Gece") return listOf("KANDİLİNİZ", "MÜBAREK OLSUN")
    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) return listOf("HAYIRLI", "CUMALAR")
    return null
  }
}
