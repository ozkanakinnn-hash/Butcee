package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.data.HatimStore
import com.example.data.HolidayCountdownCalculator
import com.example.data.IslamicDays
import com.example.data.KazaStore
import com.example.data.ZikirStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

/** Android bağlamı gerektiren testler (Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AndroidLogicTest {

  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private fun cal(y: Int, m: Int, d: Int, h: Int = 12) = Calendar.getInstance().apply { clear(); set(y, m - 1, d, h, 0, 0) }

  @Test fun islamicDays_2026_matchDiyanet() {
    val days = IslamicDays.forYear(2026)
    assertTrue(days["2026-03-20"]!!.title.contains("Ramazan Bayramı 1. Gün"))
    assertTrue(days["2026-05-27"]!!.title.contains("Kurban Bayramı 1. Gün"))
    assertTrue(days.values.any { it.title.contains("Kadir Gecesi") })
    assertTrue(days.values.count { it.category == "Mübarek Gece" } >= 5)
  }

  @Test fun holidayCountdown_usesCalendarNotFixedDates() {
    val before = HolidayCountdownCalculator.calculate(cal(2026, 11, 5, 0))
    assertTrue(before.title.contains("Ramazan"))
    assertEquals(95L, before.days) // 5 Kasım 2026 → 8 Şubat 2027
    val inRamadan = HolidayCountdownCalculator.calculate(cal(2027, 2, 19, 0))
    assertTrue(inRamadan.title.contains("Ramazan Bayramı"))
    val afterEid = HolidayCountdownCalculator.calculate(cal(2027, 3, 20, 0))
    assertTrue(afterEid.title.contains("Kurban"))
    // 2028'de Ramazan 28 Ocak'ta başlar (eski sabit 18 Şubat hatası)
    val late2027 = HolidayCountdownCalculator.calculate(cal(2027, 12, 28, 0))
    assertEquals(31L, late2027.days)
  }

  @Test fun preferences_fastingPlanAndAutoLocation() {
    val p = AppPreferences(ctx)
    assertTrue(p.toggleFastingDate("2026-12-01"))
    assertTrue(p.getFastingDates().contains("2026-12-01"))
    assertFalse(p.toggleFastingDate("2026-12-01"))
    assertTrue(p.autoLocation)
    p.latitude = 38.6; p.longitude = 27.4
    assertEquals(38.6, p.latitude, 1e-9)
  }

  @Test fun kaza_hatim_zikir_stores() {
    val k = KazaStore(ctx)
    k.resetAll()
    k.addDays(10, includeVitir = false)
    assertEquals(50, k.totalRemaining)
    k.markPrayed(KazaStore.Vakit.SABAH)
    assertEquals(49, k.totalRemaining); assertEquals(1, k.totalPrayed)
    k.markPrayed(KazaStore.Vakit.VITIR) // borç yokken azalmaz
    assertEquals(0, k.remaining(KazaStore.Vakit.VITIR))

    val h = HatimStore(ctx)
    h.resetProgress(); h.toggleJuz(3); h.toggleSurah(36)
    assertEquals(setOf(3), h.readJuz); assertEquals(setOf(36), h.readSurahs)
    val before = h.completedCount
    h.completeHatim()
    assertEquals(before + 1, h.completedCount); assertTrue(h.readJuz.isEmpty())

    val z = ZikirStore(ctx)
    repeat(25) { z.save("e$it") }
    assertEquals(20, z.saved().size)
  }
}
