package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Diyanet İşleri Başkanlığı'nın ilçe bazlı resmi namaz vakitleri (aylık tablo).
 *
 * Kaynak: ezanvakti.emushaf.net — Diyanet'in yayımladığı vakitleri birebir aktaran açık servis
 * (namazvakitleri.diyanet.gov.tr ile aynı değerler; ör. Saruhanlı 07.11.2026:
 * 06:12 07:36 12:58 15:46 18:11 19:30, Kıble Saati 10:46).
 *
 * Neden: Astronomik hesap (Aladhan / yerel) Diyanet'le her gün dakikası dakikasına tutmaz;
 * Diyanet'in kendi modeli saniye düzeyinde farklıdır (Saruhanlı'da −40…+66 sn) ve bazı günler
 * vakit 1 dk erken çıkar. Bu yüzden Türkiye (ve Diyanet'in listelediği yurt dışı şehirler)
 * için vakitler doğrudan Diyanet tablosundan alınır. Aylık tablo saklanır: ~30 gün çevrimdışı çalışır.
 */
object DiyanetSource {
  private const val BASE = "https://ezanvakti.emushaf.net"
  private const val PREFS = "diyanet_source_v1"

  data class Day(
    val date: String, // dd.MM.yyyy
    val imsak: String, val gunes: String, val ogle: String,
    val ikindi: String, val aksam: String, val yatsi: String,
    val kibleSaati: String?,
  )

  /** "Şehzadeler" → "sehzadeler": Türkçe harf ve büyük/küçük farkını yok sayar. */
  fun norm(s: String): String = s.trim().lowercase(Locale.forLanguageTag("tr"))
    .replace('ç', 'c').replace('ğ', 'g').replace('ı', 'i').replace('ö', 'o')
    .replace('ş', 's').replace('ü', 'u').replace('â', 'a').replace('î', 'i').replace('û', 'u')
    .filter { it.isLetterOrDigit() }

  /** Belirtilen yerin bugünkü Diyanet vakitleri; önbellekte yoksa aylık tablo indirilir. Bulunamazsa null. */
  fun today(context: Context, country: String, province: String, district: String, dateStr: String): Day? {
    val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val placeKey = "id|${norm(country)}|${norm(province)}|${norm(district)}"
    var ilceId = sp.getString(placeKey, null)
    if (ilceId == null) {
      ilceId = resolveIlceId(country, province, district) ?: return null
      sp.edit().putString(placeKey, ilceId).apply()
    }
    findDay(sp.getString("month|$ilceId", null), dateStr)?.let { return it }
    val body = get("$BASE/vakitler/$ilceId") ?: return null
    val day = findDay(body, dateStr) ?: return null
    sp.edit().putString("month|$ilceId", body).apply()
    return day
  }

  /** Tablonun istenen günü (yalnızca önbellekten; ağ yok). Alarm kurarken yarının vakti için. */
  fun cachedDay(context: Context, country: String, province: String, district: String, dateStr: String): Day? {
    val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val id = sp.getString("id|${norm(country)}|${norm(province)}|${norm(district)}", null) ?: return null
    return findDay(sp.getString("month|$id", null), dateStr)
  }

  internal fun findDay(json: String?, dateStr: String): Day? {
    if (json.isNullOrBlank()) return null
    return try {
      val arr = JSONArray(json)
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        if (o.optString("MiladiTarihKisa") == dateStr) return parse(o)
      }
      null
    } catch (_: Exception) { null }
  }

  private fun parse(o: JSONObject): Day? {
    val keys = listOf("Imsak", "Gunes", "Ogle", "Ikindi", "Aksam", "Yatsi")
    val v = keys.map { o.optString(it) }
    if (v.any { !Regex("^\\d{2}:\\d{2}$").matches(it) }) return null
    return Day(o.optString("MiladiTarihKisa"), v[0], v[1], v[2], v[3], v[4], v[5],
      o.optString("KibleSaati").takeIf { Regex("^\\d{2}:\\d{2}$").matches(it) })
  }

  private fun resolveIlceId(country: String, province: String, district: String): String? {
    val countries = getArray("$BASE/ulkeler") ?: return null
    val c = norm(country).let { if (it == "turkey") "turkiye" else it }
    val ulke = pick(countries, "UlkeAdi", "UlkeAdiEn", c) ?: return null
    val cities = getArray("$BASE/sehirler/${ulke.getString("UlkeID")}") ?: return null
    val sehir = pick(cities, "SehirAdi", "SehirAdiEn", norm(province)) ?: return null
    val districts = getArray("$BASE/ilceler/${sehir.getString("SehirID")}") ?: return null
    // Merkez ilçeler (ör. Yunusemre, Şehzadeler) Diyanet'te il adıyla geçer
    val ilce = pick(districts, "IlceAdi", "IlceAdiEn", norm(district))
      ?: pick(districts, "IlceAdi", "IlceAdiEn", norm(province))
      ?: if (districts.length() == 1) districts.getJSONObject(0) else null
    return ilce?.getString("IlceID")
  }

  private fun pick(arr: JSONArray, k1: String, k2: String, target: String): JSONObject? {
    for (i in 0 until arr.length()) {
      val o = arr.getJSONObject(i)
      if (norm(o.optString(k1)) == target || norm(o.optString(k2)) == target) return o
    }
    return null
  }

  private fun getArray(url: String): JSONArray? = get(url)?.let { runCatching { JSONArray(it) }.getOrNull() }

  /** Servis anlık reddedebilir: bir kez daha dene. */
  private fun get(url: String): String? = getOnce(url) ?: run { Thread.sleep(700); getOnce(url) }

  private fun getOnce(url: String): String? = try {
    val c = (URL(url).openConnection() as HttpURLConnection).apply {
      connectTimeout = 6000; readTimeout = 8000; requestMethod = "GET"
      setRequestProperty("User-Agent", "RamazanRehberiApp/1.0")
    }
    if (c.responseCode == 200) c.inputStream.bufferedReader().use { it.readText() } else null
  } catch (e: Exception) {
    Log.w("DiyanetSource", "istek başarısız: ${e.message}"); null
  }
}
