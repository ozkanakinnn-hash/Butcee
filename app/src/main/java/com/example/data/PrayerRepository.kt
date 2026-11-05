package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class PrayerRepository(private val context: Context) {

  private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
  private val apiDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)

  /**
   * [lat]/[lon] verilirse (GPS ile yurt dışı / eşleşmeyen konum) vakitler koordinata göre alınır;
   * aksi halde il/ilçe adresine göre (Türkiye'de Diyanet ilçe vakitleri).
   */
  suspend fun getPrayerTimes(city: String, district: String, country: String = "Turkey", lat: Double? = null, lon: Double? = null): PrayerTimes = withContext(Dispatchers.IO) {
    val todayStr = dateFormat.format(Date())

    // 1. Check local cache
    val cached = getCachedPrayerTimes(city, district, todayStr)
    if (cached != null) {
      return@withContext cached
    }

    // 2. Aladhan (Diyanet yöntemi, method=13): koordinat veya ilçe adresi
    try {
      val day = apiDateFormat.format(Date())
      fun enc(v: String) = java.net.URLEncoder.encode(v, "UTF-8")
      val isTurkey = country.equals("Türkiye", true) || country.equals("Turkey", true)
      val urlStr = if (lat != null && lon != null) {
        "https://api.aladhan.com/v1/timings/$day?latitude=$lat&longitude=$lon&method=13"
      } else {
        val address = if (isTurkey) "$district, $city, Turkey" else "$city, $country"
        "https://api.aladhan.com/v1/timingsByAddress/$day?address=${enc(address)}&method=13"
      }
      val url = URL(urlStr)
      val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 5000
        readTimeout = 5000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "RamazanRehberiApp/1.0")
      }

      if (connection.responseCode == 200) {
        val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)
        if (root.optInt("code") == 200) {
          val timings = root.getJSONObject("data").getJSONObject("timings")
          val imsak = PrayerTimeUtils.cleanTimeString(timings.optString("Imsak"), "05:45")
          val sunrise = PrayerTimeUtils.cleanTimeString(timings.optString("Sunrise"), "07:10")
          val dhuhr = PrayerTimeUtils.cleanTimeString(timings.optString("Dhuhr"), "13:05")
          val asr = PrayerTimeUtils.cleanTimeString(timings.optString("Asr"), "16:20")
          val maghrib = PrayerTimeUtils.cleanTimeString(timings.optString("Maghrib"), "18:45")
          val isha = PrayerTimeUtils.cleanTimeString(timings.optString("Isha"), "20:05")

          val times = PrayerTimes(
            imsak = imsak,
            gunes = sunrise,
            ogle = dhuhr,
            ikindi = asr,
            aksam = maghrib,
            yatsi = isha,
            dateStr = todayStr,
            city = city,
            district = district
          )
          saveCache(times)
          return@withContext times
        }
      }
    } catch (e: Exception) {
      Log.w("PrayerRepository", "Aladhan API error, falling back to local Diyanet calculation: ${e.message}")
    }

    // 3. Fallback: Accurate local Diyanet calculation for Turkey
    val fallbackTimes = calculateLocalTimes(city, district, todayStr, lat, lon)
    saveCache(fallbackTimes)
    fallbackTimes
  }

  /** Çevrimdışı: koordinat (GPS) veya il merkezine göre astronomik hesap. */
  private fun calculateLocalTimes(city: String, district: String, dateStr: String, lat: Double?, lon: Double?): PrayerTimes {
    val known = com.example.util.PrayerCalc.provinceCoords[city]
    val la = lat ?: known?.first
    val lo = lon ?: known?.second
    if (la != null && lo != null) {
      // Türkiye illeri her zaman TSİ (UTC+3)
      val t = com.example.util.PrayerCalc.times(la, lo, Calendar.getInstance(), if (lat == null && known != null) 3.0 else null)
      return PrayerTimes(t[0], t[1], t[2], t[3], t[4], t[5], dateStr, city, district)
    }
    return calculateLocalDiyanetTimes(city, district, dateStr)
  }

  private fun calculateLocalDiyanetTimes(city: String, district: String, dateStr: String): PrayerTimes {
    val cal = Calendar.getInstance()
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

    // Longitude shift based on Turkey provinces (from Izmir ~27E to Van ~43E)
    val provinceIndex = TurkeyProvinces.list.indexOfFirst { it.name.equals(city, ignoreCase = true) }
    val longitudeDiffMinutes = if (provinceIndex >= 0) {
      // 81 provinces roughly west to east mapping
      ((provinceIndex % 15) - 7) * 4
    } else {
      0
    }

    // Seasonal daylight equation approximation for Turkey (~39°N)
    val seasonalMinutes = (sin((dayOfYear - 80) * 2 * Math.PI / 365.25) * 45).toInt()

    val imsakMin = 330 - seasonalMinutes - longitudeDiffMinutes
    val sunriseMin = 420 - seasonalMinutes - longitudeDiffMinutes
    val dhuhrMin = 775 - longitudeDiffMinutes
    val asrMin = 980 + (seasonalMinutes / 2) - longitudeDiffMinutes
    val maghribMin = 1130 + seasonalMinutes - longitudeDiffMinutes
    val ishaMin = 1210 + seasonalMinutes - longitudeDiffMinutes

    fun formatMinutes(minutes: Int): String {
      val m = ((minutes % 1440) + 1440) % 1440
      val h = m / 60
      val min = m % 60
      return String.format(Locale.ROOT, "%02d:%02d", h, min)
    }

    return PrayerTimes(
      imsak = formatMinutes(imsakMin),
      gunes = formatMinutes(sunriseMin),
      ogle = formatMinutes(dhuhrMin),
      ikindi = formatMinutes(asrMin),
      aksam = formatMinutes(maghribMin),
      yatsi = formatMinutes(ishaMin),
      dateStr = dateStr,
      city = city,
      district = district
    )
  }

  private fun saveCache(times: PrayerTimes) {
    val prefs = context.getSharedPreferences("prayer_cache", Context.MODE_PRIVATE)
    val key = "${times.city}_${times.district}_${times.dateStr}"
    val json = JSONObject().apply {
      put("imsak", times.imsak)
      put("gunes", times.gunes)
      put("ogle", times.ogle)
      put("ikindi", times.ikindi)
      put("aksam", times.aksam)
      put("yatsi", times.yatsi)
      put("dateStr", times.dateStr)
      put("city", times.city)
      put("district", times.district)
    }.toString()
    prefs.edit().putString(key, json).apply()
  }

  private fun getCachedPrayerTimes(city: String, district: String, dateStr: String): PrayerTimes? {
    val prefs = context.getSharedPreferences("prayer_cache", Context.MODE_PRIVATE)
    val key = "${city}_${district}_${dateStr}"
    val jsonStr = prefs.getString(key, null) ?: return null
    return try {
      val obj = JSONObject(jsonStr)
      PrayerTimes(
        imsak = PrayerTimeUtils.cleanTimeString(obj.getString("imsak")),
        gunes = PrayerTimeUtils.cleanTimeString(obj.getString("gunes")),
        ogle = PrayerTimeUtils.cleanTimeString(obj.getString("ogle")),
        ikindi = PrayerTimeUtils.cleanTimeString(obj.getString("ikindi")),
        aksam = PrayerTimeUtils.cleanTimeString(obj.getString("aksam")),
        yatsi = PrayerTimeUtils.cleanTimeString(obj.getString("yatsi")),
        dateStr = obj.getString("dateStr"),
        city = obj.getString("city"),
        district = obj.getString("district")
      )
    } catch (e: Exception) {
      null
    }
  }
}
