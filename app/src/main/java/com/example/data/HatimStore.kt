package com.example.data

import android.content.Context

/** Hatim takibi: okunan cüzler / sureler, tamamlanan hatim sayısı, kaldığın sayfa ve yer imi. */
class HatimStore(context: Context) {
  private val p = context.getSharedPreferences("hatim_takibi", Context.MODE_PRIVATE)

  var readJuz: Set<Int>
    get() = parse(p.getString("juz", ""))
    set(v) = p.edit().putString("juz", v.sorted().joinToString(",")).apply()

  var readSurahs: Set<Int>
    get() = parse(p.getString("surah", ""))
    set(v) = p.edit().putString("surah", v.sorted().joinToString(",")).apply()

  var completedCount: Int
    get() = p.getInt("completed", 0)
    set(v) = p.edit().putInt("completed", v).apply()

  var lastPage: Int
    get() = p.getInt("last_page", 1)
    set(v) = p.edit().putInt("last_page", v).apply()

  var bookmarkPage: Int
    get() = p.getInt("bookmark_page", 0)
    set(v) = p.edit().putInt("bookmark_page", v).apply()

  var fontStep: Int
    get() = p.getInt("font_step", 1)
    set(v) = p.edit().putInt("font_step", v).apply()

  fun toggleJuz(j: Int) { readJuz = readJuz.toMutableSet().apply { if (!add(j)) remove(j) } }
  fun toggleSurah(n: Int) { readSurahs = readSurahs.toMutableSet().apply { if (!add(n)) remove(n) } }

  /** Hatmi tamamla: sayaç artar, yeni hatim için işaretler temizlenir. */
  fun completeHatim() { completedCount += 1; resetProgress() }
  fun resetProgress() { readJuz = emptySet(); readSurahs = emptySet() }

  private fun parse(s: String?): Set<Int> = s.orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
}
