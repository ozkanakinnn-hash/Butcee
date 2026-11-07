package com.example.util

import java.util.Calendar
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Güneşin konumu (azimut / yükseklik) — NOAA güneş hesap yöntemi (~0,1° hassasiyet).
 * Kıble Saati: güneşin tam kıble yönünde bulunduğu an (Diyanet takvimlerindeki yöntem).
 * Doğrulama: 28 Mayıs'ta (Dünya Kıble Günü) Türkiye saatiyle ~12:18'de güneş Kâbe'nin
 * tam üzerindedir; bu hesap o anda Mekke için 89,9° yükseklik verir.
 */
object SolarCalc {

  data class SunPosition(val azimuth: Double, val elevation: Double)

  fun position(lat: Double, lon: Double, utcMillis: Long): SunPosition {
    val jd = utcMillis / 86_400_000.0 + 2440587.5
    val t = (jd - 2451545.0) / 36525.0
    val l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0
    val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
    fun r(d: Double) = Math.toRadians(d)
    val c = sin(r(m)) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
      sin(r(2 * m)) * (0.019993 - 0.000101 * t) + sin(r(3 * m)) * 0.000289
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
    val elevation = 90 - Math.toDegrees(zenith)
    val azRaw = Math.toDegrees(
      acos(((sin(r(lat)) * cos(zenith) - sin(decl)) / (cos(r(lat)) * sin(zenith))).coerceIn(-1.0, 1.0))
    )
    val azimuth = if (hourAngle > 0) (azRaw + 180) % 360 else (540 - azRaw) % 360
    return SunPosition(azimuth, elevation)
  }

  /**
   * Bugün güneşin kıble yönünde olduğu yerel saat ("HH:mm"); güneş o yöne gün içinde
   * ufkun üzerinde gelmiyorsa null.
   */
  fun qiblaTime(lat: Double, lon: Double, qiblaBearing: Double, day: Calendar = Calendar.getInstance()): String? {
    val start = (day.clone() as Calendar).apply {
      set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    var prevAz: Double? = null
    for (minute in 0 until 1440) {
      val p = position(lat, lon, start + minute * 60_000L)
      val pa = prevAz
      if (pa != null && p.elevation > 2.0 && pa < qiblaBearing && p.azimuth >= qiblaBearing && p.azimuth - pa < 30) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minute / 60, minute % 60)
      }
      prevAz = p.azimuth
    }
    return null
  }
}
