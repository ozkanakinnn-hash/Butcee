package akinai.minikgurme

import akinai.minikgurme.util.DateKit
import akinai.minikgurme.util.fmt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Ortak DateKit, eski java.text biçimleriyle birebir aynı çıktıyı vermeli (Android'de görünüm değişmesin). */
class DateKitTest {
    private val patterns = listOf("d MMMM, EEE", "d MMMM EEEE", "MMMM yyyy", "d MMMM yyyy, EEEE", "d MMM yyyy", "d MMM")

    @Test
    fun formatsLikeJavaForTurkishAndEnglish() {
        val cal = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 1) }
        repeat(800) {
            val y = cal.get(Calendar.YEAR); val m = cal.get(Calendar.MONTH) + 1; val d = cal.get(Calendar.DAY_OF_MONTH)
            val ld = DateKit.of(y, m, d)
            for (lang in listOf("tr", "en")) for (p in patterns) {
                val java = SimpleDateFormat(p, Locale(lang)).format(cal.time)
                assertEquals("$p $lang $ld", java, DateKit.format(ld, p, lang))
            }
            assertEquals(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time), ld.toString())
            assertEquals(cal.getActualMaximum(Calendar.DAY_OF_MONTH), DateKit.daysInMonth(y, m))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        assertEquals("2026-03-01", DateKit.isoPlusDays("2026-02-28", 1))
        assertEquals("2025-12-31", DateKit.isoPlusDays("2026-01-01", -1))
    }

    @Test
    fun numberFormatMatchesStringFormat() {
        for (v in listOf(0.0, 7.8, 7.805, 7.815, 12.345, -0.04, 68.0, 9.95, 0.125, 1234.5678, 3.3f.toDouble()))
            for (d in 0..2) assertEquals("$v/$d", String.format(Locale.US, "%.${d}f", v), v.fmt(d))
        assertEquals(String.format(Locale.US, "%.2f", 7.8f), 7.8f.fmt(2))
    }
}
