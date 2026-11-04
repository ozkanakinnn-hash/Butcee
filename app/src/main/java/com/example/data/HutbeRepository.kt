package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HutbeItem(val date: String, val title: String, val summary: List<String>, val source: String)

/**
 * Cuma hutbesi özetleri: uygulama sahibinin güncellediği JSON dosyasından (GitHub) okunur,
 * son başarılı sonuç cihazda saklanır. Özet yoksa Diyanet'in resmî hutbe sayfası açılır.
 * Biçim: {"items":[{"date":"2026-11-06","title":"...","summary":["...","..."],"source":"https://..."}]}
 */
object HutbeRepository {
  const val FEED_URL = "https://raw.githubusercontent.com/ozkanakinnn-hash/Butcee/claude/taslak-uygulamasi-vn0y2s/content/hutbe.json"
  /** Diyanet İşleri Başkanlığı Cuma ve Bayram hutbeleri (e-Devlet, resmî). */
  const val OFFICIAL_URL = "https://www.turkiye.gov.tr/diyanet-isleri-cuma-ve-bayram-hutbeleri"

  private const val PREFS = "hutbe_cache"

  fun cached(context: Context): List<HutbeItem> =
    parse(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("json", null))

  suspend fun refresh(context: Context): List<HutbeItem>? = withContext(Dispatchers.IO) {
    try {
      val conn = (URL(FEED_URL).openConnection() as HttpURLConnection).apply {
        connectTimeout = 8000; readTimeout = 8000; useCaches = false
      }
      conn.inputStream.bufferedReader().use { r ->
        val text = r.readText()
        val items = parse(text)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("json", text).apply()
        items
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun parse(json: String?): List<HutbeItem> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
      val arr = JSONObject(json).optJSONArray("items") ?: return emptyList()
      (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        val s = o.optJSONArray("summary")
        val source = o.optString("source")
        HutbeItem(
          date = o.optString("date"),
          title = o.optString("title"),
          summary = if (s == null) emptyList() else (0 until s.length()).map { s.optString(it) },
          // Yalnızca https bağlantılara izin ver
          source = if (source.startsWith("https://")) source else OFFICIAL_URL
        )
      }.sortedByDescending { it.date }
    } catch (_: Exception) {
      emptyList()
    }
  }
}
