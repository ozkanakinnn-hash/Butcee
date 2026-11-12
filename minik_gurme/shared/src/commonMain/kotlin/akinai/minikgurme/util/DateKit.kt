package akinai.minikgurme.util

import akinai.minikgurme.i18n.AppLanguage
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Android (java.text) ve iOS'ta aynı çalışan tarih yardımcıları.
 * Tarihler veritabanında "yyyy-MM-dd" (ISO) metni olarak tutulur.
 */
@OptIn(ExperimentalTime::class)
object DateKit {
    private val monthsTr = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    private val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val monthsShortTr = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
    private val daysTr = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    private val daysEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val daysShortTr = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    private val zone get() = TimeZone.currentSystemDefault()

    fun today(): LocalDate = Clock.System.todayIn(zone)
    fun todayIso(): String = today().toString()
    fun parseIso(iso: String): LocalDate? = runCatching { LocalDate.parse(iso.trim()) }.getOrNull()
    fun isoPlusDays(iso: String, days: Int): String = ((parseIso(iso) ?: today()).plus(DatePeriod(days = days))).toString()
    fun of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

    /** Şimdiki saat "HH:mm". */
    fun nowHm(): String {
        val t = Clock.System.now().toLocalDateTime(zone)
        return "${t.hour.pad(2)}:${t.minute.pad(2)}"
    }

    fun fromMillis(millis: Long): LocalDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date

    fun daysInMonth(year: Int, month: Int): Int {
        val first = LocalDate(year, month, 1)
        val next = first.plus(DatePeriod(months = 1))
        return (next.toEpochDays() - first.toEpochDays()).toInt()
    }

    /** Pazartesi = 0 … Pazar = 6. */
    fun mondayIndex(date: LocalDate): Int = date.dayOfWeek.ordinalMondayFirst()

    private fun DayOfWeek.ordinalMondayFirst() = when (this) {
        DayOfWeek.MONDAY -> 0; DayOfWeek.TUESDAY -> 1; DayOfWeek.WEDNESDAY -> 2; DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4; DayOfWeek.SATURDAY -> 5; else -> 6
    }

    fun monthName(month: Int, lang: String = AppLanguage.current) = (if (lang == "en") monthsEn else monthsTr)[month - 1]

    /**
     * Basit desen biçimlendirici: d, dd, MMM, MMMM, M, MM, yyyy, EEE, EEEE; diğer karakterler aynen yazılır.
     * Örn. "d MMMM, EEE" → "12 Kasım, Per" / "12 November, Thu".
     */
    fun format(date: LocalDate, pattern: String, lang: String = AppLanguage.current): String {
        val en = lang == "en"
        val m = date.month.ordinal
        val dow = date.dayOfWeek.ordinalMondayFirst()
        val sb = StringBuilder()
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            var n = 1
            while (i + n < pattern.length && pattern[i + n] == c) n++
            when (c) {
                'd' -> sb.append(if (n >= 2) date.day.pad(2) else date.day.toString())
                'M' -> sb.append(
                    when {
                        n >= 4 -> (if (en) monthsEn else monthsTr)[m]
                        n == 3 -> if (en) monthsEn[m].take(3) else monthsShortTr[m]
                        n == 2 -> (m + 1).pad(2)
                        else -> (m + 1).toString()
                    }
                )
                'y' -> sb.append(if (n == 2) (date.year % 100).pad(2) else date.year.toString())
                'E' -> sb.append(if (n >= 4) (if (en) daysEn else daysTr)[dow] else if (en) daysEn[dow].take(3) else daysShortTr[dow])
                else -> repeat(n) { sb.append(c) }
            }
            i += n
        }
        return sb.toString()
    }

    fun format(iso: String, pattern: String, lang: String = AppLanguage.current): String? =
        parseIso(iso)?.let { format(it, pattern, lang) }

    fun formatToday(pattern: String, lang: String = AppLanguage.current) = format(today(), pattern, lang)
}

/** "2026-11-09" → "9 Kas 2026" / "9 Nov 2026". Ayrıştırılamazsa metni aynen döndürür. */
fun prettyDate(iso: String): String = DateKit.format(iso, "d MMM yyyy") ?: iso

/** Sıfırla doldurulmuş tam sayı: 7.pad(2) → "07". */
fun Int.pad(width: Int): String = toString().padStart(width, '0')

/** Ondalık biçim (nokta ayırıcı, yarım yukarı yuvarlama): 7.8.fmt(2) → "7.80". Java'daki String.format(Locale.US, "%.2f") ile aynı. */
fun Double.fmt(decimals: Int): String {
    if (isNaN() || isInfinite()) return toString()
    val factor = 10.0.pow(decimals)
    val scaled = floor(abs(this) * factor + 0.5).toLong()
    val intPart = scaled / factor.toLong()
    val frac = scaled % factor.toLong()
    val sign = if (this < 0) "-" else ""
    return if (decimals == 0) "$sign$intPart" else "$sign$intPart.${frac.toString().padStart(decimals, '0')}"
}

fun Float.fmt(decimals: Int): String = toDouble().fmt(decimals)

/** Türkçe büyük/küçük harf (İ/ı) doğru dönüşüm. */
fun String.lowercaseTr(): String = replace('I', 'ı').replace('İ', 'i').lowercase()
fun String.uppercaseTr(): String = replace('i', 'İ').replace('ı', 'I').uppercase()
