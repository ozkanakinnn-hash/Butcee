package com.example.data

import android.content.Context

/** Zikirmatik sayacının kalıcı durumu ve kaydedilen zikirler. */
class ZikirStore(context: Context) {
  private val p = context.getSharedPreferences("zikirmatik", Context.MODE_PRIVATE)

  var count: Int
    get() = p.getInt("count", 0)
    set(v) = p.edit().putInt("count", v).apply()

  var mode: String
    get() = p.getString("mode", "33") ?: "33"
    set(v) = p.edit().putString("mode", v).apply()

  var tesbihatStep: Int
    get() = p.getInt("tesbihat_step", 0)
    set(v) = p.edit().putInt("tesbihat_step", v).apply()

  var totalAllTime: Int
    get() = p.getInt("total", 0)
    set(v) = p.edit().putInt("total", v).apply()

  /** Kaydedilen zikirler: "tarih|etiket|sayı" satırları (en yeni başta, en fazla 20). */
  fun saved(): List<String> = (p.getString("saved", "") ?: "").split("\n").filter { it.isNotBlank() }

  fun save(entry: String) {
    val list = (listOf(entry) + saved()).take(20)
    p.edit().putString("saved", list.joinToString("\n")).apply()
  }
}
