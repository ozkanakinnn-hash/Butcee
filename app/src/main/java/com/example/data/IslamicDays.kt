package com.example.data

import android.icu.util.IslamicCalendar
import java.util.Calendar
import java.util.Locale

data class SpecialCalendarDay(
  val dateKey: String, // "YYYY-MM-DD"
  val title: String,
  val category: String, // "Mübarek Gece", "Dini Gün", "Dini Bayram", "Dini Hafta", "İslam Büyükleri", "Resmi Tatil"
  val badgeEmoji: String
)

/**
 * Takvimdeki dini günler, kandiller, bayramlar, dini haftalar ve İslam büyüklerinin
 * vefat/doğum yıl dönümleri. Hicri tarihler her yıl için Ümmü'l-Kura takvimiyle hesaplanır ve
 * Diyanet'in Ramazan başlangıcına göre düzeltilir (RamadanCalendar). Diyanet takvimiyle
 * ±1 gün fark olabilir.
 */
object IslamicDays {

  // Hicri aylar (0 tabanlı): 0 Muharrem, 1 Safer, 2 Rebiülevvel, 3 Rebiülahir, 4 Cemaziyelevvel,
  // 5 Cemaziyelahir, 6 Recep, 7 Şaban, 8 Ramazan, 9 Şevval, 10 Zilkade, 11 Zilhicce
  private const val MUHARREM = 0
  private const val REBIULEVVEL = 2
  private const val CEMAZIYELAHIR = 5
  private const val RECEP = 6
  private const val SABAN = 7
  private const val RAMAZAN = 8
  private const val SEVVAL = 9
  private const val ZILHICCE = 11

  private val cache = java.util.concurrent.ConcurrentHashMap<Int, Map<String, SpecialCalendarDay>>()

  /** Yıllık liste önbellekli (geri sayımlar her saniye çağırır). */
  fun forYear(year: Int): Map<String, SpecialCalendarDay> = cache.getOrPut(year) { compute(year) }

  private fun compute(year: Int): Map<String, SpecialCalendarDay> {
    val map = linkedMapOf<String, SpecialCalendarDay>()

    fun put(cal: Calendar, title: String, category: String, emoji: String) {
      if (cal.get(Calendar.YEAR) != year) return
      val key = RamadanCalendar.dateKey(cal)
      val existing = map[key]
      map[key] = if (existing == null) {
        SpecialCalendarDay(key, title, category, emoji)
      } else {
        // Aynı güne denk gelen olaylar birleştirilir; önceliği ilk eklenen (daha önemli) korur
        existing.copy(title = existing.title + " • " + title)
      }
    }

    fun fixed(month: Int, day: Int, title: String, category: String, emoji: String) {
      put(Calendar.getInstance().apply { clear(); set(year, month - 1, day) }, title, category, emoji)
    }

    // Diyanet düzeltmesi: tablodaki Ramazan başlangıcı ile hesaplanan 1 Ramazan arasındaki fark
    val hijriYears = hijriYearsOverlapping(year)

    fun hijri(hMonth: Int, hDay: Int, offsetDays: Int, title: String, category: String, emoji: String) {
      for (hy in hijriYears) {
        val cal = anchoredDate(hy, hMonth, hDay) ?: continue
        cal.add(Calendar.DAY_OF_MONTH, offsetDays)
        put(cal, title, category, emoji)
      }
    }

    // ── Bayramlar (en önce eklenir, aynı günde başlık önceliği alsın) ──
    hijri(SEVVAL, 1, -1, "Ramazan Bayramı Arefesi", "Dini Gün", "🕊️")
    for (d in 1..3) hijri(SEVVAL, d, 0, "Ramazan Bayramı $d. Gün", "Dini Bayram", "🍬")
    hijri(ZILHICCE, 9, 0, "Kurban Bayramı Arefesi", "Dini Gün", "🕊️")
    for (d in 1..4) hijri(ZILHICCE, 9 + d, 0, "Kurban Bayramı $d. Gün", "Dini Bayram", "🐑")

    // ── Kandiller (kandil gecesi, hicri günden önceki akşamdır) ──
    for (hy in hijriYears) {
      // Regaip: Recep ayının ilk cumasını bağlayan gece (perşembe akşamı)
      val firstRecep = anchoredDate(hy, RECEP, 1) ?: continue
      while (firstRecep.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) firstRecep.add(Calendar.DAY_OF_MONTH, 1)
      firstRecep.add(Calendar.DAY_OF_MONTH, -1)
      put(firstRecep, "Regaip Kandili", "Mübarek Gece", "✨")
    }
    hijri(RECEP, 27, -1, "Miraç Kandili", "Mübarek Gece", "✨")
    hijri(SABAN, 15, -1, "Berat Kandili", "Mübarek Gece", "✨")
    hijri(RAMAZAN, 27, -1, "Kadir Gecesi", "Mübarek Gece", "🌟")
    hijri(REBIULEVVEL, 12, -1, "Mevlid Kandili", "Mübarek Gece", "✨")

    // ── Dini günler ──
    hijri(RECEP, 1, 0, "Üç Ayların Başlangıcı", "Dini Gün", "🌙")
    hijri(RAMAZAN, 1, 0, "Ramazan Başlangıcı (1 Ramazan)", "Dini Gün", "🌙")
    hijri(MUHARREM, 1, 0, "Hicri Yılbaşı (1 Muharrem) • Hicret'in yıl dönümü", "Dini Gün", "🌙")
    hijri(MUHARREM, 10, 0, "Aşure Günü (10 Muharrem)", "Dini Gün", "🥣")

    // ── Dini haftalar ──
    // Mevlid-i Nebi (Kutlu Doğum) Haftası: Mevlid Kandili ile başlayan hafta
    for (k in 0..6) hijri(REBIULEVVEL, 12, -1 + k, "Mevlid-i Nebi (Kutlu Doğum) Haftası", "Dini Hafta", "🌹")
    // Camiler ve Din Görevlileri Haftası: her yıl Ekim'in ilk haftası
    for (d in 1..7) fixed(10, d, "Camiler ve Din Görevlileri Haftası", "Dini Hafta", "🕌")

    // ── İslam büyüklerinin vefat / doğum yıl dönümleri (geleneksel olarak kabul edilen tarihler) ──
    hijri(REBIULEVVEL, 12, 0, "Hz. Muhammed (s.a.v.)'in Doğumu", "İslam Büyükleri", "🌹")
    hijri(RAMAZAN, 3, 0, "Hz. Fatıma'nın Vefatı (11 H.)", "İslam Büyükleri", "🤍")
    hijri(RAMAZAN, 10, 0, "Hz. Hatice'nin Vefatı", "İslam Büyükleri", "🤍")
    hijri(RAMAZAN, 17, 0, "Bedir Zaferi (2 H.)", "İslam Büyükleri", "⚔️")
    hijri(RAMAZAN, 20, 0, "Mekke'nin Fethi (8 H.)", "İslam Büyükleri", "🕋")
    hijri(RAMAZAN, 21, 0, "Hz. Ali'nin Şehadeti (40 H.)", "İslam Büyükleri", "🤍")
    hijri(CEMAZIYELAHIR, 22, 0, "Hz. Ebubekir'in Vefatı (13 H.)", "İslam Büyükleri", "🤍")
    hijri(ZILHICCE, 18, 0, "Hz. Osman'ın Şehadeti (35 H.)", "İslam Büyükleri", "🤍")
    hijri(ZILHICCE, 26, 0, "Hz. Ömer'in Şehadeti (23 H.)", "İslam Büyükleri", "🤍")
    hijri(MUHARREM, 10, 0, "Hz. Hüseyin'in Kerbela'da Şehadeti (61 H.)", "İslam Büyükleri", "🤍")
    fixed(12, 17, "Şeb-i Arûs: Hz. Mevlânâ'nın Vefat Yıl Dönümü (1273)", "İslam Büyükleri", "🌹")

    // ── Resmi tatiller ──
    fixed(1, 1, "Yılbaşı", "Resmi Tatil", "🇹🇷")
    fixed(4, 23, "23 Nisan Ulusal Egemenlik ve Çocuk Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(5, 1, "1 Mayıs Emek ve Dayanışma Günü", "Resmi Tatil", "🇹🇷")
    fixed(5, 19, "19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(7, 15, "15 Temmuz Demokrasi ve Milli Birlik Günü", "Resmi Tatil", "🇹🇷")
    fixed(8, 30, "30 Ağustos Zafer Bayramı", "Resmi Tatil", "🇹🇷")
    fixed(10, 29, "29 Ekim Cumhuriyet Bayramı", "Resmi Tatil", "🇹🇷")

    return map.toSortedMap()
  }

  /** Miladi yılla kesişen hicri yıllar. */
  private fun hijriYearsOverlapping(year: Int): List<Int> {
    val ic = newIslamicCalendar()
    ic.timeInMillis = Calendar.getInstance().apply { clear(); set(year, 0, 1) }.timeInMillis
    val start = ic.get(IslamicCalendar.YEAR)
    return listOf(start, start + 1)
  }

  private fun daysBetween(a: Calendar, b: Calendar): Int {
    fun dayStart(c: Calendar) = Calendar.getInstance().apply { clear(); set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)) }
    return Math.round((dayStart(b).timeInMillis - dayStart(a).timeInMillis) / 86_400_000.0).toInt()
  }

  private fun keyToCal(key: String): Calendar {
    val p = key.split("-").map { it.toInt() }
    return Calendar.getInstance().apply { clear(); set(p[0], p[1] - 1, p[2], 12, 0) }
  }

  /**
   * Hicri tarihi Diyanet'in Ramazan tablosuna sabitleyerek miladiye çevirir: Şevval ve sonrası
   * Diyanet'in Ramazan bitişinden, öncesi Ramazan başlangıcından sayılır. Tabloda yoksa Ümmü'l-Kura.
   */
  private fun anchoredDate(hy: Int, m: Int, d: Int): Calendar? {
    val uq = hijriToGregorian(hy, m, d) ?: return null
    val uqRamadan = hijriToGregorian(hy, RAMAZAN, 1) ?: return uq
    val range = RamadanCalendar.rangeForYear(uqRamadan.get(Calendar.YEAR)) ?: return uq
    val start = keyToCal(range.first)
    val end = keyToCal(range.second)
    if (kotlin.math.abs(daysBetween(uqRamadan, start)) > 3) return uq
    if (m >= SEVVAL) {
      val uqShawwal = hijriToGregorian(hy, SEVVAL, 1) ?: return uq
      return end.apply { add(Calendar.DAY_OF_MONTH, 1 + daysBetween(uqShawwal, uq)) }
    }
    return start.apply { add(Calendar.DAY_OF_MONTH, daysBetween(uqRamadan, uq)) }
  }

  private fun newIslamicCalendar(): IslamicCalendar = IslamicCalendar().apply {
    calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
  }

  private fun hijriToGregorian(hYear: Int, hMonth: Int, hDay: Int): Calendar? = try {
    val ic = newIslamicCalendar()
    ic.clear()
    ic.set(hYear, hMonth, hDay, 12, 0)
    Calendar.getInstance().apply {
      timeInMillis = ic.timeInMillis
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
  } catch (e: Exception) {
    null
  }

  @Suppress("unused")
  private fun key(y: Int, m: Int, d: Int) = String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, d)
}
