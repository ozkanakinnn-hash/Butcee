package com.example

import com.example.qibla.HeadingFilter
import com.example.qibla.HeadingMath
import com.example.qibla.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Random
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QiblaTest {
  private val trTz = TimeZone.getTimeZone("Europe/Istanbul")
  private val now = GregorianCalendar(trTz).apply { set(2026, 10, 7, 12, 0, 0) }

  // Diyanet sitesinde yayımlanan "Kıble Açısı" (manyetik, tam derece)
  private data class City(val name: String, val lat: Double, val lon: Double, val diyanet: Int?, val trueRef: Double)
  private val cities = listOf(
    City("İstanbul", 41.0082, 28.9784, 147, 151.6),
    City("Ankara", 39.9334, 32.8597, 155, 160.2),
    City("Manisa", 38.6191, 27.4289, null, 144.7),
    City("Saruhanlı", 38.7333, 27.5667, 141, 145.2),
    City("Van", 38.4946, 43.3832, null, 191.2),
  )

  @Test fun kaaba_coordinates_are_diyanet() {
    assertEquals(21.4225, QiblaCalculator.KAABA_LAT, 1e-4)
    assertEquals(39.8261, QiblaCalculator.KAABA_LON, 1e-4)
  }

  @Test fun true_bearings_great_circle() {
    for (c in cities) assertEquals(c.name, c.trueRef, QiblaCalculator.trueBearing(c.lat, c.lon), 0.15)
    assertEquals(0.0, QiblaCalculator.trueBearing(10.0, QiblaCalculator.KAABA_LON), 1e-6)      // tam güneyden → kuzey
    assertEquals(180.0, QiblaCalculator.trueBearing(30.0, QiblaCalculator.KAABA_LON), 1e-6)    // tam kuzeyden → güney
  }

  @Test fun distance_istanbul_mecca() {
    assertEquals(2405.0, QiblaCalculator.distanceKm(41.0082, 28.9784), 25.0)
  }

  @Test fun magnetic_angles_vs_diyanet_published() {
    val ms = now.timeInMillis
    for (c in cities) {
      val d = QiblaCalculator.declination(c.lat, c.lon, ms)
      val tb = QiblaCalculator.trueBearing(c.lat, c.lon)
      val ours = QiblaCalculator.diyanetAngle(tb, d)
      println("${c.name}: gerçek=${"%.1f".format(tb)}° sapma=${"%+.2f".format(d)}° pusula=${ours}° Diyanet=${c.diyanet ?: "-"}°")
      assertTrue("${c.name} sapma $d", d in 4.0..8.5)
      // Diyanet'in yayımladığı açı birebir (tablo 2010 sapmasıyla)
      c.diyanet?.let { assertEquals(c.name, it, QiblaCalculator.diyanetPublishedAngle(c.lat, c.lon)) }
    }
  }

  @Test fun qibla_time_sun_points_to_kaaba() {
    for (c in cities.filter { it.name != "Van" }) {
      val ms = QiblaCalculator.qiblaTimeMillis(c.lat, c.lon, now)
      assertNotNull(c.name, ms)
      // Diyanet (Saruhanlı 07.11.2026): Kıble Saati 10:46
      if (c.name == "Saruhanlı") assertEquals("10:46", QiblaCalculator.qiblaTimeText(c.lat, c.lon, now, trTz))
      val sun = QiblaCalculator.sun(c.lat, c.lon, ms!!)
      assertEquals(c.name, 0.0, QiblaCalculator.turnAngle(QiblaCalculator.trueBearing(c.lat, c.lon), sun.azimuth), 0.1)
      assertTrue(sun.elevation > 0)
      println("${c.name} Kıble Saati: ${QiblaCalculator.qiblaTimeText(c.lat, c.lon, now, trTz)}")
    }
  }

  // ── Sensör yön formülü ──
  private fun matrix(h: Double, pitch: Double, sideTilt: Double = 0.0): FloatArray {
    fun mul(a: Array<DoubleArray>, b: Array<DoubleArray>) = Array(3) { i -> DoubleArray(3) { j -> (0..2).sumOf { a[i][it] * b[it][j] } } }
    val hr = Math.toRadians(h); val pr = Math.toRadians(pitch); val tr = Math.toRadians(sideTilt)
    val rz = arrayOf(doubleArrayOf(cos(hr), sin(hr), 0.0), doubleArrayOf(-sin(hr), cos(hr), 0.0), doubleArrayOf(0.0, 0.0, 1.0))
    val rx = arrayOf(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, cos(pr), -sin(pr)), doubleArrayOf(0.0, sin(pr), cos(pr)))
    val ry = arrayOf(doubleArrayOf(cos(tr), 0.0, sin(tr)), doubleArrayOf(0.0, 1.0, 0.0), doubleArrayOf(-sin(tr), 0.0, cos(tr)))
    val m = mul(mul(rz, rx), ry)
    return FloatArray(9) { m[it / 3][it % 3].toFloat() }
  }

  @Test fun heading_stable_for_any_grip() {
    for (h in listOf(0.0, 45.0, 139.3, 200.0, 359.0)) {
      for (p in listOf(0.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0)) {
        assertEquals("h=$h p=$p", 0.0, QiblaCalculator.turnAngle(h, HeadingMath.heading(matrix(h, p))!!), 0.3)
      }
      for (t in listOf(-25.0, 10.0, 25.0)) assertEquals("flat tilt $t", 0.0, QiblaCalculator.turnAngle(h, HeadingMath.heading(matrix(h, 0.0, t))!!), 0.3)
    }
    // Yatay ekran (ROTATION_90): ekranın üstü cihazın −X ekseni
    assertEquals(0.0, QiblaCalculator.turnAngle(10.0, HeadingMath.heading(matrix(100.0, 0.0), 1)!!), 0.3)
    assertEquals(0.0, QiblaCalculator.turnAngle(190.0, HeadingMath.heading(matrix(100.0, 0.0), 3)!!), 0.3)
  }

  @Test fun full_pipeline_magnetic_sensor_to_true_qibla() {
    val c = cities[3]
    val d = QiblaCalculator.declination(c.lat, c.lon, now.timeInMillis)
    val qibla = QiblaCalculator.trueBearing(c.lat, c.lon)
    val sensor = QiblaCalculator.magneticBearing(qibla, d)       // kıbleye dönük telefonun sensör değeri
    val heading = QiblaCalculator.magneticToTrue(HeadingMath.heading(matrix(sensor, 20.0))!!, d)
    assertEquals(0.0, QiblaCalculator.turnAngle(qibla, heading), 0.3)
  }

  // ── Kararlılık (titreme süzgeci) ──
  private fun std(xs: List<Double>): Double { val m = xs.average(); return sqrt(xs.sumOf { (it - m) * (it - m) } / xs.size) }

  @Test fun filter_removes_jitter_when_still() {
    val f = HeadingFilter(); val rnd = Random(1)
    val out = mutableListOf<Double>()
    for (i in 0 until 500) {           // 50 Hz, 10 sn, ±4° gürültü
      val v = f.update(145.0 + rnd.nextGaussian() * 4.0, i * 20_000_000L)
      if (i > 150) out += v
    }
    println("Sabit tutuşta ham gürültü 4.0° → ekranda ${"%.2f".format(std(out))}°")
    assertTrue("std ${std(out)}", std(out) < 0.6)
    assertEquals(145.0, QiblaCalculator.norm(out.average()), 1.0)
  }

  @Test fun filter_no_jump_across_north() {
    val f = HeadingFilter(); val rnd = Random(2)
    var prev = f.update(0.0, 0)
    for (i in 1 until 500) {
      val v = f.update(QiblaCalculator.norm(rnd.nextGaussian() * 3.0), i * 20_000_000L)
      assertTrue("sıçrama ${v - prev}", abs(v - prev) < 3.0)
      prev = v
    }
  }

  @Test fun filter_follows_real_turn_quickly() {
    val f = HeadingFilter()
    var t = 0L
    repeat(100) { f.update(100.0, t); t += 20_000_000L }
    var reachedAt = -1
    for (i in 0 until 100) {
      val v = f.update(190.0, t); t += 20_000_000L
      if (reachedAt < 0 && abs(QiblaCalculator.turnAngle(190.0, QiblaCalculator.norm(v))) < 2.0) reachedAt = i
    }
    println("90° dönüş ekranda ${reachedAt * 20} ms'de yakalandı")
    assertTrue("reached $reachedAt", reachedAt in 0..25)   // ≤ 0,5 sn
  }
}
