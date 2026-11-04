package com.nobet.data

import com.nobet.i18n.t
import java.time.LocalDate

/**
 * Türkiye resmî tatilleri — çevrimdışı, uygulamayla birlikte gelir.
 * Dini bayramlar ay takvimine göre değiştiği için yıl tablosu kullanılır (yeni yıllar sürümlerle eklenir).
 */
object Holidays {
  data class Holiday(val date: LocalDate, val key: String, val half: Boolean = false)

  private fun fixed(year: Int) = listOf(
    Holiday(LocalDate.of(year, 1, 1), "newYear"),
    Holiday(LocalDate.of(year, 4, 23), "sovereignty"),
    Holiday(LocalDate.of(year, 5, 1), "labour"),
    Holiday(LocalDate.of(year, 5, 19), "youth"),
    Holiday(LocalDate.of(year, 7, 15), "democracy"),
    Holiday(LocalDate.of(year, 8, 30), "victory"),
    Holiday(LocalDate.of(year, 10, 28), "republicEve", half = true),
    Holiday(LocalDate.of(year, 10, 29), "republic"),
  )

  /** (arife, bayramın 1. günü, gün sayısı) — Ramazan 3 gün, Kurban 4 gün. */
  private val religious: Map<Int, List<Triple<String, LocalDate, Int>>> = mapOf(
    2025 to listOf(Triple("ramadan", LocalDate.of(2025, 3, 30), 3), Triple("sacrifice", LocalDate.of(2025, 6, 6), 4)),
    2026 to listOf(Triple("ramadan", LocalDate.of(2026, 3, 20), 3), Triple("sacrifice", LocalDate.of(2026, 5, 27), 4)),
    2027 to listOf(Triple("ramadan", LocalDate.of(2027, 3, 9), 3), Triple("sacrifice", LocalDate.of(2027, 5, 16), 4)),
  )

  private val cache = HashMap<Int, Map<LocalDate, Holiday>>()

  private fun year(y: Int): Map<LocalDate, Holiday> = cache.getOrPut(y) {
    val l = fixed(y).toMutableList()
    religious[y]?.forEach { (k, first, n) ->
      l += Holiday(first.minusDays(1), k + "Eve", half = true)
      for (i in 0 until n) l += Holiday(first.plusDays(i.toLong()), k)
    }
    l.associateBy { it.date }
  }

  fun on(d: LocalDate): Holiday? = if (!Store.showHolidays) null else year(d.year)[d]

  fun name(h: Holiday): String = when (h.key) {
    "newYear" -> t("Yılbaşı", "New Year's Day")
    "sovereignty" -> t("Ulusal Egemenlik ve Çocuk Bayramı", "National Sovereignty and Children's Day")
    "labour" -> t("Emek ve Dayanışma Günü", "Labour and Solidarity Day")
    "youth" -> t("Atatürk'ü Anma, Gençlik ve Spor Bayramı", "Commemoration of Atatürk, Youth and Sports Day")
    "democracy" -> t("Demokrasi ve Millî Birlik Günü", "Democracy and National Unity Day")
    "victory" -> t("Zafer Bayramı", "Victory Day")
    "republicEve" -> t("Cumhuriyet Bayramı arifesi (yarım gün)", "Republic Day eve (half day)")
    "republic" -> t("Cumhuriyet Bayramı", "Republic Day")
    "ramadanEve" -> t("Ramazan Bayramı arifesi (yarım gün)", "Eid al-Fitr eve (half day)")
    "ramadan" -> t("Ramazan Bayramı", "Eid al-Fitr")
    "sacrificeEve" -> t("Kurban Bayramı arifesi (yarım gün)", "Eid al-Adha eve (half day)")
    "sacrifice" -> t("Kurban Bayramı", "Eid al-Adha")
    else -> h.key
  }

  fun upcoming(from: LocalDate, count: Int): List<Holiday> =
    (year(from.year).values + year(from.year + 1).values).filter { !it.date.isBefore(from) }.sortedBy { it.date }.take(count)
}
