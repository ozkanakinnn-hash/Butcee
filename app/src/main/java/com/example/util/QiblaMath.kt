package com.example.util

import android.hardware.GeomagneticField
import android.view.Surface
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kıble pusulası hesapları (ekrandan bağımsız, birim testli).
 *
 * Kuzey referansları:
 *  - Kıble açısı (büyük çember) GERÇEK (coğrafi) kuzeye göredir.
 *  - Telefonun manyetometresi / TYPE_ROTATION_VECTOR MANYETİK kuzeye göre yön verir.
 *  - Gerçek yön = manyetik yön + manyetik sapma (declination; doğu pozitif).
 *    Türkiye'de sapma doğuya ~+5° … +7°'dir; eklenmezse ibre o kadar kayar.
 */
object QiblaMath {
  const val KAABA_LAT = 21.422487
  const val KAABA_LON = 39.826206

  /** Gerçek kuzeye göre Kâbe açısı (0–360°). */
  fun bearing(lat: Double, lon: Double): Double {
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(KAABA_LAT)
    val dl = Math.toRadians(KAABA_LON - lon)
    val y = sin(dl) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dl)
    return norm(Math.toDegrees(atan2(y, x)))
  }

  /** Konum ve tarih için manyetik sapma (derece, doğu +). Dünya Manyetik Modeli (WMM). */
  fun declination(lat: Double, lon: Double, altitudeM: Float = 0f, timeMillis: Long = System.currentTimeMillis()): Float =
    GeomagneticField(lat.toFloat(), lon.toFloat(), altitudeM, timeMillis).declination

  /** Manyetik yön → gerçek yön. */
  fun magneticToTrue(magnetic: Double, declination: Double): Double = norm(magnetic + declination)

  /** Gerçek yön → manyetik yön (el pusulası / Diyanet "kıble açısı" ile karşılaştırma için). */
  fun trueToMagnetic(trueDeg: Double, declination: Double): Double = norm(trueDeg - declination)

  /**
   * Döndürme matrisinden (cihaz→dünya: Doğu, Kuzey, Yukarı) eğimden bağımsız MANYETİK yön (derece).
   * Telefon yataysa ekranın üst kenarı, dik tutuluyorsa ekranın arkası (kullanıcının baktığı yön)
   * esas alınır; arada ağırlıklı karışım. Yön belirsizse (ör. ekran tam aşağı bakıyor) null.
   */
  fun headingFromRotationMatrix(r: FloatArray, displayRotation: Int = Surface.ROTATION_0): Double? {
    val up = when (displayRotation) {
      Surface.ROTATION_90 -> floatArrayOf(-r[0], -r[3], -r[6])
      Surface.ROTATION_180 -> floatArrayOf(-r[1], -r[4], -r[7])
      Surface.ROTATION_270 -> floatArrayOf(r[0], r[3], r[6])
      else -> floatArrayOf(r[1], r[4], r[7])
    }
    val flatW = abs(r[8])
    val uprightW = up[2].coerceAtLeast(0f)
    val east = flatW * up[0] - uprightW * r[2]
    val north = flatW * up[1] - uprightW * r[5]
    if (east * east + north * north < 1e-4f) return null
    return norm(Math.toDegrees(atan2(east.toDouble(), north.toDouble())))
  }

  /** Kıbleye göre dönülmesi gereken açı: + sağa (saat yönü), − sola; −180…180. */
  fun turnAngle(qiblaTrue: Double, headingTrue: Double): Double {
    val d = norm(qiblaTrue - headingTrue)
    return if (d > 180) d - 360 else d
  }

  fun norm(a: Double): Double = ((a % 360.0) + 360.0) % 360.0
}
