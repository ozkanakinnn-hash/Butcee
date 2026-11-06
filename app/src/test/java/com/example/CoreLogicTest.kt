package com.example

import com.example.data.EnglishContent
import com.example.data.EsmaulHusna
import com.example.data.FastingKnowledge
import com.example.data.GreetingOccasion
import com.example.data.KandilDualari
import com.example.data.LocationsData
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import com.example.data.CountdownMode
import com.example.data.QuranData
import com.example.data.RamadanCalendar
import com.example.data.RamadanMenus
import com.example.data.SahurMenus
import com.example.data.SpiritualQuotes
import com.example.data.TurkeyProvinces
import com.example.data.quote
import com.example.i18n.AppLanguage
import com.example.i18n.t
import com.example.util.AutoLocation
import com.example.util.PrayerCalc
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import kotlin.math.abs

/** Saf (Android'e bağlı olmayan) çekirdek mantık testleri. */
class CoreLogicTest {

  @After fun resetLanguage() { AppLanguage.current = "tr" }

  private fun cal(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0) =
    Calendar.getInstance().apply { clear(); set(y, m - 1, d, h, min, 0) }

  private val manisa = PrayerTimes("06:01", "07:34", "12:59", "15:48", "18:13", "19:31", "05.11.2026", "Manisa", "Saruhanlı")

  @Test fun cleanTimeString_handlesApiFormats() {
    assertEquals("18:30", PrayerTimeUtils.cleanTimeString("18:30 (+03)"))
    assertEquals("05:07", PrayerTimeUtils.cleanTimeString("5:07"))
    assertEquals("00:00", PrayerTimeUtils.cleanTimeString(null))
    assertEquals("12:00", PrayerTimeUtils.cleanTimeString("abc", "12:00"))
  }

  @Test fun countdown_normalDay_nextPrayer() {
    val r = PrayerTimeUtils.calculateCountdown(manisa, emptySet(), cal(2026, 11, 5, 14, 0))
    assertEquals(CountdownMode.PRAYER, r.mode)
    assertEquals("15:48", r.targetTimeString)
    assertEquals(1L, r.hours); assertEquals(48L, r.minutes)
  }

  @Test fun countdown_afterIsha_wrapsToTomorrowImsak() {
    val r = PrayerTimeUtils.calculateCountdown(manisa, emptySet(), cal(2026, 11, 5, 22, 0))
    assertEquals("06:01", r.targetTimeString)
    assertEquals(8L, r.hours); assertEquals(1L, r.minutes)
  }

  @Test fun countdown_plannedFast_showsIftarAndSahur() {
    val fast = setOf("2026-11-05")
    val day = PrayerTimeUtils.calculateCountdown(manisa, fast, cal(2026, 11, 5, 14, 0))
    assertEquals(CountdownMode.IFTAR, day.mode)
    assertEquals("18:13", day.targetTimeString)
    val dawn = PrayerTimeUtils.calculateCountdown(manisa, fast, cal(2026, 11, 5, 4, 0))
    assertEquals(CountdownMode.SAHUR, dawn.mode)
    // Ertesi gün oruç planlıysa yatsıdan sonra imsaka (sahura) sayar
    val night = PrayerTimeUtils.calculateCountdown(manisa, setOf("2026-11-06"), cal(2026, 11, 5, 21, 0))
    assertEquals(CountdownMode.SAHUR, night.mode)
  }

  @Test fun countdown_ramadan_teravih() {
    val r = PrayerTimeUtils.calculateCountdown(manisa, emptySet(), cal(2027, 2, 19, 18, 30))
    assertEquals(CountdownMode.TERAVIH, r.mode)
  }

  @Test fun ramadanCalendar_days() {
    assertEquals(12, RamadanCalendar.dayOfRamadan(cal(2027, 2, 19)))
    assertEquals(1, RamadanCalendar.dayOfRamadan(cal(2026, 2, 19)))
    assertNull(RamadanCalendar.dayOfRamadan(cal(2026, 11, 5)))
    assertTrue(RamadanCalendar.isRamadanDay("2027-03-08"))
    assertFalse(RamadanCalendar.isRamadanDay("2027-03-09"))
    assertEquals("2027-02-08", RamadanCalendar.nextStartKey("2026-11-05"))
  }

  @Test fun offlineCalc_closeToDiyanet() {
    val c = PrayerCalc.provinceCoords.getValue("Manisa")
    val got = PrayerCalc.times(c.first, c.second, cal(2026, 11, 5), 3.0)
    val expected = listOf("06:10", "07:34", "12:59", "15:48", "18:13", "19:31")
    got.zip(expected).forEach { (g, e) ->
      val diff = abs(PrayerTimeUtils.parseTimeToMinutes(g) - PrayerTimeUtils.parseTimeToMinutes(e))
      assertTrue("hesap $g / beklenen $e", diff <= 3)
    }
    assertEquals(81, PrayerCalc.provinceCoords.size)
    TurkeyProvinces.list.forEach { assertNotNull("koordinat yok: ${it.name}", PrayerCalc.provinceCoords[it.name]) }
  }

  @Test fun autoLocation_nameMatching() {
    assertEquals("sanliurfa", AutoLocation.norm("Şanlıurfa"))
    assertEquals(AutoLocation.norm("İstanbul"), AutoLocation.norm("Istanbul"))
    assertEquals(AutoLocation.norm("Manisa"), AutoLocation.norm("Manisa İli"))
  }

  @Test fun i18n_translatesAndFallsBack() {
    AppLanguage.current = "en"
    assertEquals("Settings", t("Ayarlar"))
    assertEquals("Time until Dhuhr", t("Öğle Vaktine Kalan"))
    assertEquals("Bilinmeyen metin", t("Bilinmeyen metin"))
    AppLanguage.current = "tr"
    assertEquals("Ayarlar", t("Ayarlar"))
  }

  @Test fun greetingCards_everyOccasionHasQuoteAndMessage() {
    // Önceki sürümde başlatma sırası hatası CUMA kartında çökmeye yol açmıştı
    GreetingOccasion.entries.forEach { o ->
      assertTrue(o.quote.text.tr.isNotBlank())
      assertTrue(o.quote.source.isNotBlank())
      assertTrue(o.messages.isNotEmpty())
    }
  }

  @Test fun content_indicesWrapAndAreComplete() {
    for (d in listOf(-5, 0, 1, 15, 30, 31, 400)) {
      assertNotNull(SpiritualQuotes.getForDay(d))
      assertNotNull(RamadanMenus.getMenuForDay(d))
      assertNotNull(SahurMenus.forDay(d))
      assertTrue(FastingKnowledge.tipForDay(d).tr.isNotBlank())
    }
    assertEquals(99, EsmaulHusna.names.size)
    assertEquals(114, QuranData.surahs.size)
    assertEquals(114, EnglishContent.surahMeanings.size)
    assertEquals(81, TurkeyProvinces.list.size)
    assertTrue(LocationsData.countries.all { it.cities.isNotEmpty() })
    assertEquals(5, KandilDualari.kandiller.size)
    KandilDualari.kandiller.forEach { assertTrue(it.duas.isNotEmpty()) }
  }

  @Test fun englishSpiritual_coversRamadanDays() {
    AppLanguage.current = "en"
    for (d in 1..30) {
      val item = SpiritualQuotes.getForDay(d)
      assertTrue("gün $d", item.verseTurkish.isNotBlank() && item.hadithTurkish.isNotBlank())
    }
  }
}
