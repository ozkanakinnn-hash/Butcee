package com.example.qibla

import android.hardware.GeomagneticField
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Kıble hesapları — Diyanet İşleri Başkanlığı'nın kullandığı yöntem:
 *
 *  1. Kâbe koordinatı: 21°25'21" K, 39°49'34" D.
 *  2. Kıble açısı: bulunulan yer ile Kâbe arasındaki büyük çemberin (en kısa yol) başlangıç
 *     açısıdır; COĞRAFİ (gerçek) kuzeyden saat yönünde ölçülür.
 *  3. Diyanet'in "Kıble Açısı" olarak yayımladığı değer, el pusulasıyla kullanılmak üzere
 *     MANYETİK kuzeye göre, tam dereceye yuvarlanmış değerdir:  manyetik = gerçek − sapma.
 *     (Sapma = manyetik sapma / declination; Türkiye'de doğu yönlü, ≈ +5°…+7°.)
 *  4. Kıble Saati: Güneş'in tam kıble istikametinde bulunduğu andır; o anda Güneş'e dönen
 *     kişi kıbleye dönmüş olur. Pusuladan, metalden ve kalibrasyondan etkilenmez.
 */
object QiblaCalculator {
  /** Diyanet: 21°25'21" K, 39°49'34" D */
  const val KAABA_LAT = 21.0 + 25.0 / 60 + 21.0 / 3600   // 21.422500
  const val KAABA_LON = 39.0 + 49.0 / 60 + 34.0 / 3600   // 39.826111
  private const val EARTH_RADIUS_KM = 6371.0088

  /** Gerçek (coğrafi) kuzeye göre kıble açısı, 0–360°. */
  fun trueBearing(lat: Double, lon: Double): Double {
    val p1 = Math.toRadians(lat)
    val p2 = Math.toRadians(KAABA_LAT)
    val dl = Math.toRadians(KAABA_LON - lon)
    val y = sin(dl) * cos(p2)
    val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
    return norm(Math.toDegrees(atan2(y, x)))
  }

  /** Kâbe'ye büyük çember uzaklığı (km). */
  fun distanceKm(lat: Double, lon: Double): Double {
    val p1 = Math.toRadians(lat)
    val p2 = Math.toRadians(KAABA_LAT)
    val dp = p2 - p1
    val dl = Math.toRadians(KAABA_LON - lon)
    val a = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
    return 2 * EARTH_RADIUS_KM * atan2(sqrt(a), sqrt(1 - a))
  }

  /** Manyetik sapma (derece, doğu +) — Dünya Manyetik Modeli (WMM). */
  fun declination(lat: Double, lon: Double, timeMillis: Long = System.currentTimeMillis()): Double =
    GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, timeMillis).declination.toDouble()

  /** Bu konumda beklenen toplam manyetik alan şiddeti (µT); parazit tespiti için. */
  fun expectedFieldMicroTesla(lat: Double, lon: Double, timeMillis: Long = System.currentTimeMillis()): Double =
    GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, timeMillis).fieldStrength / 1000.0

  /** Manyetik kuzeye göre kıble açısı (el pusulası). */
  fun magneticBearing(trueBearing: Double, declination: Double): Double = norm(trueBearing - declination)

  /** Manyetik kuzeye göre, tam dereceye yuvarlanmış açı. */
  fun diyanetAngle(trueBearing: Double, declination: Double): Int =
    magneticBearing(trueBearing, declination).roundToInt() % 360

  /**
   * Diyanet'in yayımladığı tablolar 2007–2014 arası manyetik sapmayla hesaplanmıştır (2010 ortası esas
   * alınır). Bu değerle Saruhanlı 141°, İstanbul 147°, Ankara 155° — Diyanet ile birebir.
   * Yalnızca bilgi amaçlıdır: pusula ibresi gerçek kuzeye göre hesaplanır.
   */
  val DIYANET_TABLE_EPOCH_MILLIS: Long = java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC")).apply {
    clear(); set(2010, 6, 1)
  }.timeInMillis

  fun diyanetPublishedAngle(lat: Double, lon: Double): Int =
    diyanetAngle(trueBearing(lat, lon), declination(lat, lon, DIYANET_TABLE_EPOCH_MILLIS))

  fun magneticToTrue(magnetic: Double, declination: Double): Double = norm(magnetic + declination)

  /** Kıbleye dönmek için gereken dönüş: + sağa (saat yönü), − sola; −180…+180. */
  fun turnAngle(target: Double, heading: Double): Double {
    val d = norm(target - heading)
    return if (d > 180) d - 360 else d
  }

  fun norm(a: Double): Double = ((a % 360.0) + 360.0) % 360.0

  // ───────────── Güneş konumu (NOAA) ve Kıble Saati ─────────────

  data class Sun(val azimuth: Double, val elevation: Double)

  fun sun(lat: Double, lon: Double, utcMillis: Long): Sun {
    fun r(d: Double) = Math.toRadians(d)
    val jd = utcMillis / 86_400_000.0 + 2440587.5
    val t = (jd - 2451545.0) / 36525.0
    val l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0
    val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
    val c = sin(r(m)) * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin(r(2 * m)) * (0.019993 - 0.000101 * t) + sin(r(3 * m)) * 0.000289
    val omega = 125.04 - 1934.136 * t
    val lambda = l0 + c - 0.00569 - 0.00478 * sin(r(omega))
    val eps0 = 23.0 + (26.0 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
    val eps = eps0 + 0.00256 * cos(r(omega))
    val decl = asin(sin(r(eps)) * sin(r(lambda)))
    val y = tan(r(eps) / 2).let { it * it }
    val eot = 4 * Math.toDegrees(
      y * sin(2 * r(l0)) - 2 * e * sin(r(m)) + 4 * e * y * sin(r(m)) * cos(2 * r(l0)) -
        0.5 * y * y * sin(4 * r(l0)) - 1.25 * e * e * sin(2 * r(m))
    )
    val utcMinutes = ((utcMillis % 86_400_000L + 86_400_000L) % 86_400_000L) / 60_000.0
    val trueSolarTime = ((utcMinutes + eot + 4 * lon) % 1440 + 1440) % 1440
    val hourAngle = trueSolarTime / 4 - 180
    val zenith = acos((sin(r(lat)) * sin(decl) + cos(r(lat)) * cos(decl) * cos(r(hourAngle))).coerceIn(-1.0, 1.0))
    val azRaw = Math.toDegrees(acos(((sin(r(lat)) * cos(zenith) - sin(decl)) / (cos(r(lat)) * sin(zenith))).coerceIn(-1.0, 1.0)))
    val azimuth = if (hourAngle > 0) (azRaw + 180) % 360 else (540 - azRaw) % 360
    return Sun(azimuth, 90 - Math.toDegrees(zenith))
  }

  /**
   * Kıble Saati: o gün Güneş'in azimutunun kıble açısına eşit olduğu an (Güneş ufkun üzerindeyken).
   * Dakika hassasiyetinde bulunur, sonra saniye düzeyinde iyileştirilir. Yoksa null.
   */
  fun qiblaTimeMillis(lat: Double, lon: Double, day: Calendar = Calendar.getInstance()): Long? {
    val bearing = trueBearing(lat, lon)
    val start = (day.clone() as Calendar).apply {
      set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    var prev = sun(lat, lon, start)
    for (minute in 1..1440) {
      val time = start + minute * 60_000L
      val cur = sun(lat, lon, time)
      val before = turnAngle(bearing, prev.azimuth)   // kıble − güneş
      val after = turnAngle(bearing, cur.azimuth)
      if (cur.elevation > 0 && before > 0 && after <= 0 && before - after < 30) {
        // doğrusal ara değer
        val frac = before / (before - after)
        return time - 60_000L + (frac * 60_000L).toLong()
      }
      prev = cur
    }
    return null
  }

  /** "HH:mm" biçiminde Kıble Saati (cihaz saat diliminde) ya da null. */
  fun qiblaTimeText(lat: Double, lon: Double, day: Calendar = Calendar.getInstance(), tz: TimeZone = TimeZone.getDefault()): String? {
    val ms = qiblaTimeMillis(lat, lon, day) ?: return null
    val c = Calendar.getInstance(tz).apply { timeInMillis = ms + 30_000 } // en yakın dakikaya yuvarla
    return String.format(java.util.Locale.ROOT, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
  }
}
