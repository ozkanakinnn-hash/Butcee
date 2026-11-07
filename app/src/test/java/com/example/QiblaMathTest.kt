package com.example

import android.view.Surface
import com.example.util.QiblaMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QiblaMathTest {
  private val nov2026 = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply { set(2026, 10, 7, 12, 0, 0) }.timeInMillis

  @Test fun bearings_true_north() {
    assertEquals(145.2, QiblaMath.bearing(38.7333, 27.5667), 0.2)  // Saruhanlı
    assertEquals(151.6, QiblaMath.bearing(41.0082, 28.9784), 0.2)  // İstanbul
    assertEquals(160.2, QiblaMath.bearing(39.9334, 32.8597), 0.2)  // Ankara
  }

  @Test fun declination_turkey_is_east_and_plausible() {
    val cities = mapOf("Saruhanlı" to (38.7333 to 27.5667), "İstanbul" to (41.0082 to 28.9784), "Ankara" to (39.9334 to 32.8597), "Van" to (38.4946 to 43.3832))
    for ((name, c) in cities) {
      val d = QiblaMath.declination(c.first, c.second, 0f, nov2026)
      println("Sapma $name: ${"%.2f".format(d)}°  kıble gerçek ${"%.1f".format(QiblaMath.bearing(c.first, c.second))}° → manyetik ${"%.1f".format(QiblaMath.trueToMagnetic(QiblaMath.bearing(c.first, c.second), d.toDouble()))}°")
      assertTrue("$name sapma $d", d in 4f..8.5f)  // Türkiye'de doğu sapma (WMM)
    }
  }

  @Test fun magnetic_true_roundtrip_and_wrap() {
    assertEquals(5.0, QiblaMath.magneticToTrue(359.0, 6.0), 1e-9)
    assertEquals(355.0, QiblaMath.trueToMagnetic(1.0, 6.0), 1e-9)
    assertEquals(-10.0, QiblaMath.turnAngle(140.0, 150.0), 1e-9)
    assertEquals(20.0, QiblaMath.turnAngle(10.0, 350.0), 1e-9)
  }

  /** Dünya←cihaz döndürme matrisi: manyetik yön h, öne eğim p (üst kenar yukarı), satır sıralı. */
  private fun matrix(h: Double, pitch: Double, tiltY: Double = 0.0): FloatArray {
    fun mul(a: Array<DoubleArray>, b: Array<DoubleArray>) = Array(3) { i -> DoubleArray(3) { j -> (0..2).sumOf { a[i][it] * b[it][j] } } }
    val hr = Math.toRadians(h); val pr = Math.toRadians(pitch); val tr = Math.toRadians(tiltY)
    val rz = arrayOf(doubleArrayOf(cos(hr), sin(hr), 0.0), doubleArrayOf(-sin(hr), cos(hr), 0.0), doubleArrayOf(0.0, 0.0, 1.0))
    val rx = arrayOf(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, cos(pr), -sin(pr)), doubleArrayOf(0.0, sin(pr), cos(pr)))
    val ry = arrayOf(doubleArrayOf(cos(tr), 0.0, sin(tr)), doubleArrayOf(0.0, 1.0, 0.0), doubleArrayOf(-sin(tr), 0.0, cos(tr)))
    val m = mul(mul(rz, rx), ry)
    return FloatArray(9) { m[it / 3][it % 3].toFloat() }
  }

  @Test fun heading_independent_of_pitch_and_side_tilt_when_flat() {
    for (h in listOf(0.0, 90.0, 139.6, 270.0)) {
      for (p in listOf(0.0, 30.0, 60.0, 85.0, 90.0)) {
        val got = QiblaMath.headingFromRotationMatrix(matrix(h, p))!!
        assertEquals("h=$h p=$p", 0.0, QiblaMath.turnAngle(h, got), 0.5)
      }
      // Yatay telefon yana eğilse de yön değişmez
      assertEquals(0.0, QiblaMath.turnAngle(h, QiblaMath.headingFromRotationMatrix(matrix(h, 0.0, 20.0))!!), 0.5)
    }
  }

  @Test fun full_pipeline_points_to_kaaba() {
    // Saruhanlı: kullanıcı tam kıbleye dönük; sensör manyetik yön verir
    val lat = 38.7333; val lon = 27.5667
    val decl = QiblaMath.declination(lat, lon, 0f, nov2026).toDouble()
    val qibla = QiblaMath.bearing(lat, lon)
    val sensorMagnetic = QiblaMath.trueToMagnetic(qibla, decl)
    val heading = QiblaMath.magneticToTrue(QiblaMath.headingFromRotationMatrix(matrix(sensorMagnetic, 10.0))!!, decl)
    assertEquals(0.0, QiblaMath.turnAngle(qibla, heading), 0.5)
    // Sapma eklenmeseydi hata ≈ sapma kadar olurdu
    assertTrue(kotlin.math.abs(QiblaMath.turnAngle(qibla, sensorMagnetic)) > 4.0)
  }

  @Test fun landscape_rotation_uses_screen_top() {
    // Telefon yatay ve sola çevrilmiş (ROTATION_90): ekranın üstü cihazın −X ekseni
    val r = matrix(100.0, 0.0) // cihaz Y'si 100°'ye bakıyor → −X ekseni 10°'ye bakar
    assertEquals(0.0, QiblaMath.turnAngle(10.0, QiblaMath.headingFromRotationMatrix(r, Surface.ROTATION_90)!!), 0.5)
  }
}
