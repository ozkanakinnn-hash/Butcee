package com.example.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.sqrt

/** Pusulanın anlık durumu (ekrana verilecek). */
data class CompassReading(
  /** Gerçek kuzeye göre kesintisiz (sarılmamış) yön — animasyon için. */
  val continuousTrueHeading: Double = 0.0,
  /** Ölçüm başladı mı */
  val ready: Boolean = false,
  /** Manyetometre doğruluğu: 3 yüksek, 2 orta, 1 düşük, 0 güvenilmez, -1 bilinmiyor */
  val accuracy: Int = -1,
  /** Ölçülen alan beklenenden çok farklı → yakında mıknatıs / metal var */
  val interference: Boolean = false,
  /** Ölçülen / beklenen manyetik alan (µT) */
  val fieldMicroTesla: Double = 0.0,
  val expectedMicroTesla: Double = 0.0,
  /** Telefonun yataydan sapması (derece) */
  val tiltDeg: Double = 0.0,
  /** Son 1 sn'deki oynama (derece) */
  val jitterDeg: Double = 0.0,
) {
  val trueHeading: Double get() = QiblaCalculator.norm(continuousTrueHeading)
}

/**
 * Sensör füzyonu: TYPE_ROTATION_VECTOR (ivmeölçer + jiroskop + manyetometre; en kararlı kaynak).
 * Cihazda yoksa ivmeölçer + manyetometre ile döndürme matrisi kurulur.
 * Sensörler MANYETİK kuzeyi verir; [declinationProvider] ile gerçek kuzeye çevrilir.
 */
class CompassSensor(
  private val context: Context,
  private val declinationProvider: () -> Double,
  private val expectedFieldProvider: () -> Double,
  private val onReading: (CompassReading) -> Unit,
) : SensorEventListener {
  private val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val rotationSensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val accelSensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val magnetSensor = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

  val hasCompass: Boolean = magnetSensor != null && (rotationSensor != null || accelSensor != null)

  private val r = FloatArray(9)
  private val gravity = FloatArray(3)
  private val geomagnetic = FloatArray(3)
  private var hasGravity = false
  private var hasGeomagnetic = false
  private val filter = HeadingFilter()
  private var accuracy = -1
  private var field = 0.0
  private var reading = CompassReading()
  private var lastEmitNs = 0L
  private var gotHeading = false

  fun start() {
    val s = sm ?: return
    filter.reset(); gotHeading = false
    if (rotationSensor != null) s.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    else accelSensor?.let { s.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    // Manyetometre her durumda: doğruluk + parazit tespiti (+ yedek yöntemde yön)
    magnetSensor?.let { s.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
  }

  fun stop() { sm?.unregisterListener(this) }

  private fun screenRotation(): Int = try {
    val rot = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.display?.rotation
    else @Suppress("DEPRECATION") (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    when (rot) { Surface.ROTATION_90 -> 1; Surface.ROTATION_180 -> 2; Surface.ROTATION_270 -> 3; else -> 0 }
  } catch (_: Exception) { 0 }

  override fun onSensorChanged(event: SensorEvent) {
    var haveMatrix = false
    when (event.sensor.type) {
      Sensor.TYPE_ROTATION_VECTOR -> {
        SensorManager.getRotationMatrixFromVector(r, event.values)
        haveMatrix = true
      }
      Sensor.TYPE_ACCELEROMETER -> {
        lowPass(event.values, gravity, if (hasGravity) 0.15f else 1f); hasGravity = true
        if (rotationSensor == null && hasGeomagnetic) haveMatrix = SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)
      }
      Sensor.TYPE_MAGNETIC_FIELD -> {
        lowPass(event.values, geomagnetic, if (hasGeomagnetic) 0.15f else 1f); hasGeomagnetic = true
        val v = event.values
        val magnitude = sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble())
        field = if (field == 0.0) magnitude else field + 0.1 * (magnitude - field)
        if (rotationSensor == null && hasGravity) haveMatrix = SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)
        if (!haveMatrix) emit(event.timestamp, force = true)
      }
    }
    if (!haveMatrix) return
    val magnetic = HeadingMath.heading(r, screenRotation()) ?: return
    val trueHeading = QiblaCalculator.magneticToTrue(magnetic, declinationProvider())
    filter.update(trueHeading, event.timestamp)
    gotHeading = true
    reading = reading.copy(tiltDeg = HeadingMath.tiltDegrees(r))
    emit(event.timestamp, force = false)
  }

  private fun emit(ts: Long, force: Boolean) {
    // Ekranı en çok ~30 kez/sn güncelle
    if (!force && ts - lastEmitNs < 33_000_000L) return
    lastEmitNs = ts
    val expected = expectedFieldProvider()
    val interference = expected > 0 && field > 0 && abs(field - expected) / expected > 0.30
    reading = reading.copy(
      continuousTrueHeading = filter.output,
      ready = gotHeading,
      accuracy = accuracy,
      interference = interference,
      fieldMicroTesla = field,
      expectedMicroTesla = expected,
      jitterDeg = filter.jitterDeg,
    )
    onReading(reading)
  }

  override fun onAccuracyChanged(sensor: Sensor, acc: Int) {
    if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) accuracy = acc
  }

  private fun lowPass(input: FloatArray, out: FloatArray, a: Float) {
    for (i in 0..2) out[i] += a * (input[i] - out[i])
  }
}
