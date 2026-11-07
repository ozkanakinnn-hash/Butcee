package com.example.qibla

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Telefonun yönünü sensör döndürme matrisinden hesaplar ve titremeyi süzer.
 * Android'e bağımlı değildir; birim testleriyle doğrulanır.
 */
object HeadingMath {
  /**
   * Döndürme matrisi R (cihaz → dünya; dünya ekseni Doğu, Kuzey, Yukarı; satır sıralı 3×3).
   * Telefon yataysa yön = ekranın üst kenarı; dik tutuluyorsa yön = kullanıcının baktığı yön
   * (ekranın arkası, −Z). Aradaki tutuşlarda ikisi, telefonun ne kadar yatay / dik olduğuna
   * göre ağırlıklandırılır: eşik ve eksen değiştirme yok, dolayısıyla sıçrama yok.
   *
   * @param screenRotation 0, 1, 2, 3 = Surface.ROTATION_0/90/180/270
   * @return sensörün referansına göre (rotation vector için MANYETİK kuzey) yön, ya da yön
   *         belirsizse (ör. ekran tam aşağı bakıyor) null.
   */
  fun heading(r: FloatArray, screenRotation: Int = 0): Double? {
    // Ekranın üst kenarına karşılık gelen cihaz ekseni (dünyadaki bileşenleri)
    val upE: Float; val upN: Float; val upU: Float
    when (screenRotation) {
      1 -> { upE = -r[0]; upN = -r[3]; upU = -r[6] }   // −X
      2 -> { upE = -r[1]; upN = -r[4]; upU = -r[7] }   // −Y
      3 -> { upE = r[0]; upN = r[3]; upU = r[6] }      // +X
      else -> { upE = r[1]; upN = r[4]; upU = r[7] }   // +Y
    }
    val flatWeight = abs(r[8])                 // ekran yukarı/aşağı bakıyorsa 1
    val uprightWeight = upU.coerceAtLeast(0f)  // üst kenar göğe bakıyorsa 1
    val east = flatWeight * upE - uprightWeight * r[2]
    val north = flatWeight * upN - uprightWeight * r[5]
    if (east * east + north * north < 1e-4f) return null
    return QiblaCalculator.norm(Math.toDegrees(atan2(east.toDouble(), north.toDouble())))
  }

  /** Ekran yere ne kadar paralel: 0° = tam yatay, 90° = dik. */
  fun tiltDegrees(r: FloatArray): Double = Math.toDegrees(kotlin.math.acos(abs(r[8]).toDouble().coerceIn(0.0, 1.0)))
}

/**
 * Pusula titremesini gideren uyarlamalı süzgeç.
 *  - Açılar birim vektör olarak süzülür (359° ↔ 1° geçişinde sıçrama olmaz).
 *  - Telefon dururken uzun zaman sabiti (sakin ibre), hızlı dönüşte kısa (gecikmesiz).
 *  - Çıktı "sarılmamış" (kesintisiz) açıdır: ekrandaki animasyon kısa yoldan döner.
 *  - Küçük oynamalar (< ölü bölge) ekrana yansıtılmaz.
 */
class HeadingFilter(
  private val calmTauSec: Double = 0.45,
  private val fastTauSec: Double = 0.08,
  private val fastEnterDeg: Double = 15.0,
  private val fastExitDeg: Double = 2.0,
  private val deadbandDeg: Double = 0.6,
) {
  private var sx = 0.0
  private var sy = 0.0
  private var lastNs = 0L
  private var initialized = false
  /** Gerçek dönüş algılandı: hedefe varana dek hızlı izle (histerezis) */
  private var fastMode = false

  /** Ekranda gösterilen kesintisiz açı. */
  var output = 0.0
    private set

  /** Son ~1 sn'deki oynama (derece, standart sapma benzeri); düşükse ölçüm sabittir. */
  var jitterDeg = 0.0
    private set

  fun reset() { initialized = false; fastMode = false }

  /** @return ekrana yansıtılacak kesintisiz yön (derece). */
  fun update(headingDeg: Double, timestampNs: Long): Double {
    val rad = Math.toRadians(headingDeg)
    if (!initialized) {
      sx = cos(rad); sy = sin(rad); lastNs = timestampNs; initialized = true
      output = headingDeg; jitterDeg = 0.0
      return output
    }
    val dt = ((timestampNs - lastNs) / 1e9).coerceIn(0.001, 0.5)
    lastNs = timestampNs
    val current = Math.toDegrees(atan2(sy, sx))
    val innovation = abs(QiblaCalculator.turnAngle(headingDeg, current))
    fastMode = innovation > fastEnterDeg || (fastMode && innovation > fastExitDeg)
    val tau = if (fastMode) fastTauSec else calmTauSec
    val alpha = 1 - exp(-dt / tau)
    sx += alpha * (cos(rad) - sx)
    sy += alpha * (sin(rad) - sy)
    val len = sqrt(sx * sx + sy * sy)
    if (len > 1e-6) { sx /= len; sy /= len }
    // oynama ölçüsü (≈1 sn pencere)
    val beta = 1 - exp(-dt / 1.0)
    jitterDeg += beta * (innovation - jitterDeg)

    val smoothed = QiblaCalculator.norm(Math.toDegrees(atan2(sy, sx)))
    val delta = QiblaCalculator.turnAngle(smoothed, QiblaCalculator.norm(output))
    if (abs(delta) >= deadbandDeg) output += delta
    return output
  }
}
