package com.example.util

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kullanıcı verilerini (zikir, kaza, hatim, oruç günleri, manevi notlar, ayarlar) tek bir JSON
 * dosyasına yedekler ve geri yükler. Dosya kullanıcının seçtiği yere (Drive, Dosyalar…) kaydedilir;
 * uygulama hiçbir sunucuya veri göndermez.
 */
object BackupManager {
  /** Yedeklenen SharedPreferences dosyaları (vakit önbellekleri hariç: yeniden indirilir). */
  val FILES = listOf("ramazan_rehberi_prefs", "zikirmatik", "kaza_namazi", "hatim_takibi")
  private const val FORMAT = "ramazan-rehberi-backup"
  private const val VERSION = 1

  fun export(context: Context): String {
    val root = JSONObject().put("format", FORMAT).put("version", VERSION).put("createdAt", System.currentTimeMillis())
    val files = JSONObject()
    for (name in FILES) {
      val entries = JSONObject()
      context.getSharedPreferences(name, Context.MODE_PRIVATE).all.forEach { (k, v) ->
        val e = when (v) {
          is Boolean -> JSONObject().put("t", "b").put("v", v)
          is Int -> JSONObject().put("t", "i").put("v", v)
          is Long -> JSONObject().put("t", "l").put("v", v)
          is Float -> JSONObject().put("t", "f").put("v", v.toDouble())
          is String -> JSONObject().put("t", "s").put("v", v)
          is Set<*> -> JSONObject().put("t", "ss").put("v", JSONArray(v.filterIsInstance<String>()))
          else -> null
        }
        if (e != null) entries.put(k, e)
      }
      files.put(name, entries)
    }
    return root.put("files", files).toString(2)
  }

  /** @return geri yüklenen kayıt sayısı; dosya geçersizse -1. */
  fun import(context: Context, json: String): Int {
    val root = runCatching { JSONObject(json) }.getOrNull() ?: return -1
    if (root.optString("format") != FORMAT) return -1
    val files = root.optJSONObject("files") ?: return -1
    var count = 0
    for (name in FILES) {
      val entries = files.optJSONObject(name) ?: continue
      val ed = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
      for (k in entries.keys()) {
        val e = entries.optJSONObject(k) ?: continue
        when (e.optString("t")) {
          "b" -> ed.putBoolean(k, e.optBoolean("v"))
          "i" -> ed.putInt(k, e.optInt("v"))
          "l" -> ed.putLong(k, e.optLong("v"))
          "f" -> ed.putFloat(k, e.optDouble("v").toFloat())
          "s" -> ed.putString(k, e.optString("v"))
          "ss" -> ed.putStringSet(k, e.optJSONArray("v")?.let { a -> (0 until a.length()).map { a.optString(it) }.toSet() } ?: emptySet())
          else -> continue
        }
        count++
      }
      ed.commit()
    }
    return count
  }

  fun writeTo(context: Context, uri: Uri): Boolean = runCatching {
    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(export(context).toByteArray()) } != null
  }.getOrDefault(false)

  fun readFrom(context: Context, uri: Uri): Int = runCatching {
    val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } ?: return -1
    if (text.length > 5_000_000) return -1 // aşırı büyük/yanlış dosya
    import(context, text)
  }.getOrDefault(-1)
}
