package com.example.data

import android.content.Context

/** Kaza namazı borcu: vakit başına kalan sayı ve kılınan toplam. */
class KazaStore(context: Context) {
  private val p = context.getSharedPreferences("kaza_namazi", Context.MODE_PRIVATE)

  enum class Vakit(val key: String, val tr: String, val en: String, val arabic: String) {
    SABAH("sabah", "Sabah", "Fajr", "الصبح"),
    OGLE("ogle", "Öğle", "Dhuhr", "الظهر"),
    IKINDI("ikindi", "İkindi", "Asr", "العصر"),
    AKSAM("aksam", "Akşam", "Maghrib", "المغرب"),
    YATSI("yatsi", "Yatsı", "Isha", "العشاء"),
    VITIR("vitir", "Vitir", "Witr", "الوتر")
  }

  fun remaining(v: Vakit): Int = p.getInt("kalan_${v.key}", 0)
  fun prayed(v: Vakit): Int = p.getInt("kilinan_${v.key}", 0)
  val totalRemaining: Int get() = Vakit.entries.sumOf { remaining(it) }
  val totalPrayed: Int get() = Vakit.entries.sumOf { prayed(it) }

  /** Bir kaza namazı kılındı: borç bir azalır, kılınan bir artar. */
  fun markPrayed(v: Vakit) {
    val r = remaining(v)
    if (r <= 0) return
    p.edit().putInt("kalan_${v.key}", r - 1).putInt("kilinan_${v.key}", prayed(v) + 1).apply()
  }

  fun add(v: Vakit, n: Int) {
    p.edit().putInt("kalan_${v.key}", (remaining(v) + n).coerceAtLeast(0)).apply()
  }

  /** Gün sayısı kadar her vakte borç ekler (vitir dahil isteğe bağlı). */
  fun addDays(days: Int, includeVitir: Boolean) {
    val e = p.edit()
    Vakit.entries.forEach { v ->
      if (v != Vakit.VITIR || includeVitir) e.putInt("kalan_${v.key}", remaining(v) + days)
    }
    e.apply()
  }

  fun resetAll() { p.edit().clear().apply() }
}
