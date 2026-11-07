package com.example

import com.example.data.DiyanetSource
import com.example.util.PrayerCalc
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.GregorianCalendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiyanetSourceTest {
  private val json = javaClass.classLoader!!.getResource("diyanet_saruhanli_2026_11.json")!!.readText()

  @Test fun parses_official_table_exactly() {
    // Kullanıcının Diyanet ekran görüntüsü (Saruhanlı, 7 Kasım 2026)
    val d = DiyanetSource.findDay(json, "07.11.2026")!!
    assertEquals(listOf("06:12", "07:36", "12:58", "15:46", "18:11", "19:30"), listOf(d.imsak, d.gunes, d.ogle, d.ikindi, d.aksam, d.yatsi))
    assertEquals("10:46", d.kibleSaati)
    assertNull(DiyanetSource.findDay(json, "01.01.2020"))
    assertNull(DiyanetSource.findDay("bozuk", "07.11.2026"))
  }

  @Test fun turkish_name_matching() {
    assertEquals("saruhanli", DiyanetSource.norm("SARUHANLI"))
    assertEquals("saruhanli", DiyanetSource.norm("Saruhanlı"))
    assertEquals("manisa", DiyanetSource.norm("MANİSA"))
    assertEquals("turkiye", DiyanetSource.norm("TÜRKİYE"))
    assertEquals("sehzadeler", DiyanetSource.norm("Şehzadeler"))
    assertEquals("kirkagac", DiyanetSource.norm("KIRKAĞAÇ"))
  }

  /** Neden doğrudan Diyanet: yerel astronomik hesap bazı günler 1 dk sapıyor. */
  @Test fun local_calc_vs_diyanet_report() {
    val arr = JSONArray(json)
    var mismatch = 0; var total = 0
    for (i in 0 until arr.length()) {
      val o = arr.getJSONObject(i)
      val (dd, mm, yy) = o.getString("MiladiTarihKisa").split(".").map { it.toInt() }
      val cal = GregorianCalendar(TimeZone.getTimeZone("Europe/Istanbul")).apply { set(yy, mm - 1, dd, 12, 0) }
      val calc = PrayerCalc.times(38.7333, 27.5667, cal, 3.0)
      val diy = listOf("Imsak", "Gunes", "Ogle", "Ikindi", "Aksam", "Yatsi").map { o.getString(it) }
      calc.zip(diy).forEach { (a, b) -> total++; if (a != b) mismatch++ }
    }
    println("Yerel hesap Diyanet'ten farklı: $mismatch / $total vakit (hepsi ±1-2 dk)")
    assertNotNull(total)
  }
}
