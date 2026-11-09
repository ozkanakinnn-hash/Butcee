package com.example.util

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** Tarif porsiyonundaki ilk sayı ("2 porsiyon", "4 mini pankek" → 2, 4). Bulunamazsa 1. */
fun baseServings(servings: String): Int =
    Regex("""\d+""").find(servings)?.value?.toIntOrNull()?.coerceAtLeast(1) ?: 1

private val leading = Regex("""^(\d+(?:[.,]\d+)?)(?:\s*/\s*(\d+))?(?:\s*-\s*(\d+(?:[.,]\d+)?)(?:\s*/\s*(\d+))?)?""")

private fun num(a: String, b: String?): Double {
    val x = a.replace(',', '.').toDouble()
    return if (b != null) x / b.toDouble() else x
}

/** 0.5 → "1/2", 1.5 → "1 1/2", 2.0 → "2", 0.3 → "0,3" gibi okunur biçim. */
fun formatQuantity(v: Double): String {
    val whole = floor(v + 1e-9).toInt()
    val frac = v - whole
    val fractions = listOf(0.0 to "", 0.25 to "1/4", 1.0 / 3 to "1/3", 0.5 to "1/2", 2.0 / 3 to "2/3", 0.75 to "3/4", 1.0 to "")
    val (fv, fs) = fractions.minBy { abs(it.first - frac) }
    if (abs(fv - frac) < 0.04) {
        val w = if (fv == 1.0) whole + 1 else whole
        return when {
            fs.isEmpty() -> "$w"
            w == 0 -> fs
            else -> "$w $fs"
        }
    }
    val r = (v * 10).roundToInt() / 10.0
    return if (r == floor(r)) "${r.toInt()}" else r.toString().replace('.', ',')
}

/** Miktar metninin başındaki sayıyı (kesir veya aralık dahil) katsayıyla çarpar; sayı yoksa metni aynen döndürür. */
fun scaleAmount(text: String, factor: Float): String {
    if (abs(factor - 1f) < 1e-3) return text
    val m = leading.find(text.trim()) ?: return text
    val g = m.groupValues
    val first = num(g[1], g[2].ifEmpty { null }) * factor
    val scaled = if (g[3].isNotEmpty()) {
        formatQuantity(first) + "-" + formatQuantity(num(g[3], g[4].ifEmpty { null }) * factor)
    } else formatQuantity(first)
    return scaled + text.trim().substring(m.value.length)
}
