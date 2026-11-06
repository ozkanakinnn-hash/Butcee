package com.example.ui.screens

import com.example.i18n.t
import android.content.Context
import com.example.util.SolarCalc
import androidx.compose.foundation.clickable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import android.view.WindowManager
import android.view.Surface
import android.location.LocationManager
import android.hardware.GeomagneticField
import android.content.pm.PackageManager
import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaFinderScreen(
  prefs: AppPreferences,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val isDark = com.example.ui.theme.isAppInDarkTheme()

  // Konum: izin varsa cihazın son bilinen GPS/ağ konumu, yoksa seçili ilçe/il merkezi
  var locationRefresh by remember { mutableStateOf(0) }
  val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    locationRefresh++
  }
  val resolvedLocation = remember(prefs.country, prefs.province, prefs.district, locationRefresh) {
    resolveQiblaLocation(context, prefs.country, prefs.province, prefs.district)
  }
  // Son bilinen konum yoksa / eskiyse, izin varsa cihazdan tek seferlik taze konum iste
  var freshLocation by remember { mutableStateOf<QiblaLocation?>(null) }
  LaunchedEffect(locationRefresh) {
    if (!resolvedLocation.fromDevice) {
      requestFreshLocation(context) { lat, lon -> freshLocation = QiblaLocation(lat, lon, fromDevice = true) }
    }
  }
  val location = freshLocation ?: resolvedLocation
  val qiblaBearing = remember(location) { qiblaBearingFrom(location.lat, location.lon) }

  // Manyetik sapma: pusula manyetik kuzeyi gösterir, Kıble açısı coğrafi (gerçek) kuzeye göredir.
  // Türkiye'de bu fark ~5-7° doğudur; düzeltilmezse ibre bu kadar kayar.
  val declination = remember(location) {
    GeomagneticField(location.lat.toFloat(), location.lon.toFloat(), 0f, System.currentTimeMillis()).declination
  }

  val qiblaTime = remember(location) {
    SolarCalc.qiblaTime(location.lat, location.lon, qiblaBearing.toDouble())
  }
  var sun by remember { mutableStateOf(SolarCalc.position(location.lat, location.lon, System.currentTimeMillis())) }
  LaunchedEffect(location) {
    while (true) {
      sun = SolarCalc.position(location.lat, location.lon, System.currentTimeMillis())
      kotlinx.coroutines.delay(60_000)
    }
  }

  var currentAzimuth by remember { mutableFloatStateOf(0f) }
  var sensorAccuracy by remember { mutableStateOf("Ölçülüyor") }
  var needsCalibration by remember { mutableStateOf(false) }
  var isUpright by remember { mutableStateOf(false) }
  var hasCompass by remember { mutableStateOf(true) }
  var showCalibrationDialog by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    hasCompass = magnetSensor != null

    val listener = object : SensorEventListener {
      val rotationMatrix = FloatArray(9)
      val remapped = FloatArray(9)
      val orientationValues = FloatArray(3)
      val gravity = FloatArray(3)
      val geomagnetic = FloatArray(3)
      var hasAccel = false
      var hasMagnet = false
      val lowPass = 0.25f
      var smoothSin = 0.0
      var smoothCos = 1.0
      var initialized = false

      override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        var gotMatrix = false
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            gotMatrix = true
          }
          Sensor.TYPE_ACCELEROMETER -> {
            for (i in 0..2) gravity[i] += lowPass * (event.values[i] - gravity[i]); hasAccel = true
          }
          Sensor.TYPE_MAGNETIC_FIELD -> {
            for (i in 0..2) geomagnetic[i] += lowPass * (event.values[i] - geomagnetic[i]); hasMagnet = true
          }
        }
        if (!gotMatrix && rotationSensor == null && hasAccel && hasMagnet) {
          gotMatrix = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        }
        if (!gotMatrix) return

        // Telefon dik tutuluyorsa (60°'den fazla eğik) eksenleri yeniden eşle; aksi halde
        // azimut kararsızlaşır ve yanlış yön gösterir. Ekran yatay dönmüşse de düzelt.
        val upright = abs(rotationMatrix[8]) < 0.5f
        isUpright = upright
        @Suppress("DEPRECATION")
        val displayRotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        val (axisX, axisY) = if (upright) {
          SensorManager.AXIS_X to SensorManager.AXIS_Z
        } else when (displayRotation) {
          Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
          Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
          Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
          else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remapped)
        SensorManager.getOrientation(remapped, orientationValues)

        // Manyetik → gerçek kuzey
        val trueHeading = Math.toDegrees(orientationValues[0].toDouble()) + declination
        val rad = Math.toRadians(trueHeading)
        if (!initialized) {
          smoothSin = sin(rad); smoothCos = cos(rad); initialized = true
        } else {
          // Açısal düzleştirme (0°/360° sıçraması olmadan)
          smoothSin += 0.2 * (sin(rad) - smoothSin)
          smoothCos += 0.2 * (cos(rad) - smoothCos)
        }
        currentAzimuth = ((Math.toDegrees(atan2(smoothSin, smoothCos)) + 360.0) % 360.0).toFloat()
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Doğruluk bilgisini manyetometreden okuyoruz: asıl hata kaynağı odur
        if (sensor?.type != Sensor.TYPE_MAGNETIC_FIELD) return
        needsCalibration = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
        sensorAccuracy = when (accuracy) {
          SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Yüksek"
          SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Orta"
          SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Düşük"
          else -> "Güvenilmez"
        }
      }
    }

    if (rotationSensor != null) {
      sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
      // Yalnızca doğruluk takibi için manyetometreyi de dinle
      magnetSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
    } else {
      accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
      magnetSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    onDispose { sensorManager?.unregisterListener(listener) }
  }

  // Angular difference between phone heading and target Qibla bearing
  val rawDiff = (qiblaBearing - currentAzimuth + 360f) % 360f
  val angleDifference = if (rawDiff > 180f) rawDiff - 360f else rawDiff
  val isFacingQibla = abs(angleDifference) <= 4.0f

  // Relative needle angle: points directly toward Mecca relative to top of phone
  val needleAngle = (qiblaBearing - currentAzimuth + 360f) % 360f

  // Haptic feedback when user locks onto Mecca
  var hasVibratedForCurrentLock by remember { mutableStateOf(false) }
  LaunchedEffect(isFacingQibla) {
    if (isFacingQibla && !hasVibratedForCurrentLock) {
      hasVibratedForCurrentLock = true
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          @Suppress("DEPRECATION")
          vibrator?.vibrate(120)
        }
      } catch (e: Exception) {
        // Ignored
      }
    } else if (!isFacingQibla) {
      hasVibratedForCurrentLock = false
    }
  }

  val statusBgColor by animateColorAsState(
    targetValue = if (isFacingQibla) Color(0xFF2D5A43) else MaterialTheme.colorScheme.primaryContainer,
    label = "status_bg_color"
  )
  val statusTextColor by animateColorAsState(
    targetValue = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
    label = "status_text_color"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("qibla_finder_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"))
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = t("Kıble Bulucu (Hassas Pusula)"),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = (if (location.fromDevice) "📍 Cihaz konumu" else "${prefs.province} / ${prefs.district}") +
            " • Kıble: ${qiblaBearing.toInt()}° (gerçek kuzey) • " +
            // Diyanet sitesindeki "Kıble Açısı" manyetik kuzeye göredir (pusulanın gösterdiği kuzey)
            "${(((qiblaBearing - declination) % 360f + 360f) % 360f).toInt()}° (manyetik / Diyanet)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Real-Time Direction Status Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = statusBgColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isFacingQibla) Color(0xFF388E68) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          if (isFacingQibla) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
          } else {
            Text("🕋", fontSize = 22.sp)
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = if (isFacingQibla) "KÂBE İSTİKAMETİNDESİNİZ" else "KIBLE YÖNÜNÜ BULUN",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = statusTextColor
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isFacingQibla) "Hizalama tamamlandı! Huşû ile namaza durabilirsiniz."
            else if (angleDifference > 0) "Telefonu sağa doğru ${(angleDifference).toInt()}° çevirin"
            else "Telefonu sola doğru ${(-angleDifference).toInt()}° çevirin",
            fontSize = 12.sp,
            color = statusTextColor.copy(alpha = 0.95f)
          )
        }
      }
    }

    // Ölçüm doğruluğunu etkileyen durumlar için uyarılar
    val warning = when {
      !hasCompass -> "Cihazınızda pusula (manyetometre) sensörü bulunmuyor; yön ölçülemez."
      needsCalibration -> "Pusula doğruluğu düşük: telefonu havada 8 çizerek kalibre edin ve metal eşyalardan uzaklaşın."
      isUpright -> "Telefonu yere paralel (yatay) tutun; dik tutulduğunda ölçüm daha az hassastır."
      else -> null
    }
    if (warning != null) {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC77700).copy(alpha = 0.18f))
      ) {
        Text(
          text = "⚠️ $warning",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = needsCalibration) { showCalibrationDialog = true }
            .padding(12.dp)
        )
      }
    }
    if (!location.fromDevice) {
      Spacer(modifier = Modifier.height(8.dp))
      TextButton(onClick = {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
      }) {
        Text(t("📍 Daha hassas ölçüm için konumumu kullan"), fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // PRECISION COMPASS DIAL (Krem / Bej / Altın lüks pusula tablası)
    val compassBgColor = if (isDark) Color(0xFF111110) else Color(0xFFFBF8F0)
    val dialBorderColor = if (isFacingQibla) Color(0xFF2D5A43) else Color(0xFFC7A762)

    Box(
      modifier = Modifier
        .size(300.dp)
        .clip(CircleShape)
        .background(compassBgColor)
        .border(4.dp, dialBorderColor, CircleShape)
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      // 1. ROTATING COMPASS DIAL (Kuzey-Güney pusula kadranı)
      // Rotates by -currentAzimuth so 'K' always points to physical North
      Box(
        modifier = Modifier
          .fillMaxSize()
          .rotate(-currentAzimuth)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2, size.height / 2)
          val radius = size.width / 2 - 12.dp.toPx()

          // Draw Degree Ticks
          for (deg in 0 until 360 step 5) {
            val isMajor = deg % 30 == 0
            val isMedium = deg % 10 == 0
            val lineLen = if (isMajor) 14.dp.toPx() else (if (isMedium) 9.dp.toPx() else 5.dp.toPx())
            val angleRad = Math.toRadians((deg - 90).toDouble())

            val startX = (center.x + (radius - lineLen) * cos(angleRad)).toFloat()
            val startY = (center.y + (radius - lineLen) * sin(angleRad)).toFloat()
            val endX = (center.x + radius * cos(angleRad)).toFloat()
            val endY = (center.y + radius * sin(angleRad)).toFloat()

            drawLine(
              color = if (isMajor) Color(0xFF9E6B20) else (if (isDark) Color(0xFF5A5243) else Color(0xFFD6C8AF)),
              start = Offset(startX, startY),
              end = Offset(endX, endY),
              strokeWidth = if (isMajor) 2.6f else 1.2f
            )
          }
        }

        // Cardinal Direction Labels on Rotating Rose
        Text(
          text = t("K"),
          fontWeight = FontWeight.Bold,
          color = Color(0xFFD32F2F), // Red North
          fontSize = 17.sp,
          modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        )
        Text(
          text = t("G"),
          fontWeight = FontWeight.Bold,
          color = if (isDark) Color(0xFFB0A48E) else Color(0xFF6B5838),
          fontSize = 16.sp,
          modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
        Text(
          text = t("D"),
          fontWeight = FontWeight.Bold,
          color = if (isDark) Color(0xFFB0A48E) else Color(0xFF6B5838),
          fontSize = 16.sp,
          modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        )
        Text(
          text = t("B"),
          fontWeight = FontWeight.Bold,
          color = if (isDark) Color(0xFFB0A48E) else Color(0xFF6B5838),
          fontSize = 16.sp,
          modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
        )
      }

      // 1b. GÜNEŞ İŞARETİ: pusula doğruysa ☀ gökyüzündeki gerçek güneşi gösterir (kontrol için)
      if (sun.elevation > 0) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .rotate(((sun.azimuth.toFloat() - currentAzimuth) + 360f) % 360f),
          contentAlignment = Alignment.TopCenter
        ) {
          Text("☀️", fontSize = 18.sp, modifier = Modifier.padding(top = 22.dp))
        }
      }

      // 2. DEDICATED ROTATING QIBLA NEEDLE WITH KAABA ICON
      // Needle dynamically rotates directly toward Mecca at needleAngle
      Box(
        modifier = Modifier
          .fillMaxSize()
          .rotate(needleAngle),
        contentAlignment = Alignment.Center
      ) {
        // Needle Graphic
        Canvas(modifier = Modifier.fillMaxSize()) {
          val cx = size.width / 2
          val cy = size.height / 2
          val needleLength = size.width * 0.38f

          // Emerald / Gold Mecca Pointer (Top half pointing to Kaaba)
          val meccaArrow = Path().apply {
            moveTo(cx, cy - needleLength)
            lineTo(cx + 14f, cy - 10f)
            lineTo(cx, cy)
            lineTo(cx - 14f, cy - 10f)
            close()
          }
          drawPath(
            path = meccaArrow,
            color = if (isFacingQibla) Color(0xFF2D5A43) else Color(0xFF9E6B20)
          )

          // Light accent edge on pointer
          val meccaEdge = Path().apply {
            moveTo(cx, cy - needleLength)
            lineTo(cx, cy)
            lineTo(cx + 14f, cy - 10f)
            close()
          }
          drawPath(
            path = meccaEdge,
            color = if (isFacingQibla) Color(0xFF388E68) else Color(0xFFC7A762)
          )

          // Counterweight Pointer (Bottom half pointing opposite)
          val counterArrow = Path().apply {
            moveTo(cx, cy + needleLength * 0.65f)
            lineTo(cx + 10f, cy + 8f)
            lineTo(cx, cy)
            lineTo(cx - 10f, cy + 8f)
            close()
          }
          drawPath(
            path = counterArrow,
            color = if (isDark) Color(0xFF4A443A) else Color(0xFFB5A68E)
          )
        }

        // Kaaba Badge at the exact tip of the needle
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 18.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isFacingQibla) Color(0xFF2D5A43) else Color(0xFF9E6B20))
              .border(2.dp, Color(0xFFFFD56B), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text("🕋", fontSize = 20.sp)
          }
        }
      }

      // 3. Central Gold Jewel Pivot Point
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(CircleShape)
          .background(Color(0xFF9E6B20))
          .border(2.5.dp, Color.White, CircleShape)
      )

      // Forward Phone Pointer (Small triangle at top of dial for user heading alignment)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 2.dp),
        contentAlignment = Alignment.TopCenter
      ) {
        Canvas(modifier = Modifier.size(12.dp)) {
          val marker = Path().apply {
            moveTo(size.width / 2, size.height)
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
          }
          drawPath(marker, color = if (isFacingQibla) Color(0xFF2D5A43) else Color(0xFFB3261E))
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Real-Time Degree & Bearing Details
    // KIBLE SAATİ — pusuladan bağımsız, en güvenilir yöntem (Diyanet takvimlerindeki "Kıble Saati")
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = t("☀️ Bugünün Kıble Saati: ") + (qiblaTime ?: "—"),
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (qiblaTime != null)
            "Saat $qiblaTime'de güneşe döndüğünüzde kıbleye dönmüş olursunuz. Güneş mıknatıstan, metalden veya kalibrasyondan etkilenmez; pusulayı bu yöntemle kontrol edebilirsiniz. Kadrandaki ☀️ işareti gerçek güneşi göstermiyorsa pusulayı kalibre edin."
          else
            "Bugün güneş bu konumda kıble yönünden geçmiyor; pusulayı kullanın.",
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(t("Mevcut Yön"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${currentAzimuth.toInt()}°", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          Text(getHeadingName(currentAzimuth), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(t("Hedef Kıble"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${qiblaBearing.toInt()}°", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E6B20))
          Text(getHeadingName(qiblaBearing), fontSize = 10.sp, color = Color(0xFF2D5A43), fontWeight = FontWeight.SemiBold)
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(t("Sensör Durumu"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(sensorAccuracy, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          Text(t("Manyetometre"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Calibration & Guidance Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = t("Hassas Ölçüm İpuçları"),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "• Telefonunuzu yere paralel (avucunuzun içinde düz) tutunuz.\n• Mıknatıslı kılıf, metal masa, araç içi, priz ve hoparlörlerden uzak durunuz.\n• Sensör durumu \"Düşük\" ise havada 8 çizerek kalibre edin.\n• Manyetik sapma düzeltmesi: ${"%+.1f".format(declination)}° (otomatik)\n• Konum: " + (if (location.fromDevice) "cihazın konumu kullanılıyor" else "il/ilçe merkezi kullanılıyor (konum izni verirseniz daha hassas)") + ".",
          fontSize = 11.sp,
          lineHeight = 16.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Calibration Button
    OutlinedButton(
      onClick = { showCalibrationDialog = true },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(imageVector = Icons.Default.CompassCalibration, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(t("Pusulayı Kalibre Et (8 Çiz)"))
    }

    Spacer(modifier = Modifier.height(20.dp))
  }

  // Calibration Info Dialog
  if (showCalibrationDialog) {
    AlertDialog(
      onDismissRequest = { showCalibrationDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(t("Pusula Kalibrasyonu 🧭"), fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(t("Cihazın manyetik sensörlerinin en yüksek doğrulukla çalışması için:"))
          Spacer(modifier = Modifier.height(8.dp))
          Text(t("1. Telefonunuzu elinize alarak havada yatay bir '8' (sonsuzluk) işareti çizecek şekilde 3-4 kez çevirin."))
          Spacer(modifier = Modifier.height(6.dp))
          Text(t("2. Mıknatıslı telefon kılıflarından, prizlerden ve büyük metal yüzeylerden uzaklaşın."))
          Spacer(modifier = Modifier.height(6.dp))
          Text(t("3. Telefonunuzu yere paralel (avucunuzun içinde düz) tutarak Kâbe yönüne çevirin."))
        }
      },
      confirmButton = {
        TextButton(onClick = { showCalibrationDialog = false }) {
          Text(t("Anladım"))
        }
      }
    )
  }
}

private fun getHeadingName(deg: Float): String = t(headingNameTr(deg))

private fun headingNameTr(deg: Float): String {
  return when (((deg + 22.5f) % 360f) / 45f) {
    in 0f..1f -> "Kuzey"
    in 1f..2f -> "Kuzeydoğu"
    in 2f..3f -> "Doğu"
    in 3f..4f -> "Güneydoğu"
    in 4f..5f -> "Güney"
    in 5f..6f -> "Güneybatı"
    in 6f..7f -> "Batı"
    else -> "Kuzeybatı"
  }
}

// Great-Circle Spherical Trigonometry calculation to Kaaba (21.422487 N, 39.826206 E)
// For Saruhanlı/Manisa this yields ~145.2° GD
private data class QiblaLocation(val lat: Double, val lon: Double, val fromDevice: Boolean)

/** İzin verilmişse cihazın son bilinen konumunu (en fazla 1 gün önceki), yoksa il/ilçe merkezini kullanır. */
private fun resolveQiblaLocation(context: Context, country: String, province: String, district: String): QiblaLocation {
  val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
  if (hasPermission) {
    try {
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
      val best = lm.getProviders(true)
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .filter { System.currentTimeMillis() - it.time < 24 * 3600_000L }
        .minByOrNull { it.accuracy }
      if (best != null) return QiblaLocation(best.latitude, best.longitude, fromDevice = true)
    } catch (_: SecurityException) {
    }
  }
  val c = getCoordinatesForLocation(country, province, district)
  return QiblaLocation(c.first, c.second, fromDevice = false)
}

/** İzin varsa cihazdan tek seferlik güncel konum ister (Android 11+ getCurrentLocation; eskilerde tek güncelleme). */
private fun requestFreshLocation(context: Context, onResult: (Double, Double) -> Unit) {
  val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
  if (!granted) return
  try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val provider = when {
      lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
      lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
      else -> return
    }
    androidx.core.location.LocationManagerCompat.getCurrentLocation(
      lm, provider, null as android.os.CancellationSignal?, ContextCompat.getMainExecutor(context)
    ) { loc -> if (loc != null) onResult(loc.latitude, loc.longitude) }
  } catch (_: SecurityException) {
  } catch (_: Exception) {
  }
}

/** Büyük çember (ortodromi) formülüyle gerçek kuzeye göre Kâbe açısı. */
private fun qiblaBearingFrom(lat: Double, lon: Double): Float {
  val kaabaLat = 21.422487
  val kaabaLon = 39.826206
  val phi1 = Math.toRadians(lat)
  val phi2 = Math.toRadians(kaabaLat)
  val deltaLambda = Math.toRadians(kaabaLon - lon)
  val y = sin(deltaLambda) * cos(phi2)
  val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
  return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

private fun getCoordinatesForLocation(country: String, province: String, district: String): Pair<Double, Double> {
  val pNorm = province.trim().lowercase(Locale.ROOT).replace("ı", "i")
  val dNorm = district.trim().lowercase(Locale.ROOT).replace("ı", "i")

  // Specific district overrides (Saruhanlı, Akhisar, Turgutlu, Salihli)
  if (dNorm.contains("saruhanli") || pNorm.contains("saruhanli")) return Pair(38.7333, 27.5667)
  if (dNorm.contains("akhisar")) return Pair(38.9244, 27.8394)
  if (dNorm.contains("turgutlu")) return Pair(38.4967, 27.7025)
  if (dNorm.contains("salihli")) return Pair(38.4811, 28.1367)

  return when (pNorm) {
    "manisa" -> Pair(38.6191, 27.4289)
    "istanbul" -> Pair(41.0082, 28.9784)
    "ankara" -> Pair(39.9334, 32.8597)
    "izmir" -> Pair(38.4237, 27.1428)
    "bursa" -> Pair(40.1885, 29.0610)
    "adana" -> Pair(37.0000, 35.3213)
    "adiyaman" -> Pair(37.7648, 38.2786)
    "afyonkarahisar", "afyon" -> Pair(38.7507, 30.5567)
    "agri" -> Pair(39.7191, 43.0503)
    "amasya" -> Pair(40.6533, 35.8331)
    "antalya" -> Pair(36.8969, 30.7133)
    "artvin" -> Pair(41.1828, 41.8183)
    "aydin" -> Pair(37.8560, 27.8416)
    "balikesir" -> Pair(39.6484, 27.8826)
    "bilecik" -> Pair(40.1426, 29.9793)
    "bingol" -> Pair(38.8847, 40.4939)
    "bitlis" -> Pair(38.4006, 42.1095)
    "bolu" -> Pair(40.7358, 31.6061)
    "burdur" -> Pair(37.7203, 30.2908)
    "canakkale" -> Pair(40.1553, 26.4142)
    "cankiri" -> Pair(40.6013, 33.6134)
    "corum" -> Pair(40.5506, 34.9556)
    "denizli" -> Pair(37.7765, 29.0864)
    "diyarbakir" -> Pair(37.9144, 40.2306)
    "edirne" -> Pair(41.6771, 26.5557)
    "elazig" -> Pair(38.6810, 39.2264)
    "erzincan" -> Pair(39.7500, 39.5000)
    "erzurum" -> Pair(39.9000, 41.2700)
    "eskisehir" -> Pair(39.7767, 30.5206)
    "gaziantep" -> Pair(37.0662, 37.3833)
    "giresun" -> Pair(40.9128, 38.3895)
    "gumushane" -> Pair(40.4600, 39.4700)
    "hakkari" -> Pair(37.5800, 43.7400)
    "hatay" -> Pair(36.2000, 36.1600)
    "isparta" -> Pair(37.7648, 30.5566)
    "mersin" -> Pair(36.8000, 34.6333)
    "kars" -> Pair(40.6167, 43.1000)
    "kastamonu" -> Pair(41.3887, 33.7827)
    "kayseri" -> Pair(38.7312, 35.4787)
    "kirklareli" -> Pair(41.7333, 27.2167)
    "kirsehir" -> Pair(39.1425, 34.1709)
    "kocaeli" -> Pair(40.8533, 29.8815)
    "konya" -> Pair(37.8746, 32.4932)
    "kutahya" -> Pair(39.4167, 29.9833)
    "malatya" -> Pair(38.3552, 38.3095)
    "kahramanmaras", "maras" -> Pair(37.5858, 36.9371)
    "mardin" -> Pair(37.3212, 40.7245)
    "mugla" -> Pair(37.2153, 28.3636)
    "mus" -> Pair(38.7432, 41.5064)
    "nevsehir" -> Pair(38.6244, 34.7144)
    "nigde" -> Pair(37.9667, 34.6833)
    "ordu" -> Pair(40.9839, 37.8764)
    "rize" -> Pair(41.0201, 40.5234)
    "sakarya" -> Pair(40.7569, 30.3783)
    "samsun" -> Pair(41.2928, 36.3313)
    "siirt" -> Pair(37.9333, 41.9500)
    "sinop" -> Pair(42.0231, 35.1531)
    "sivas" -> Pair(39.7477, 37.0179)
    "tekirdag" -> Pair(40.9833, 27.5167)
    "tokat" -> Pair(40.3167, 36.5500)
    "trabzon" -> Pair(41.0027, 39.7168)
    "tunceli" -> Pair(39.1079, 39.5401)
    "sanliurfa", "urfa" -> Pair(37.1674, 38.7955)
    "usak" -> Pair(38.6823, 29.4082)
    "van" -> Pair(38.4891, 43.4089)
    "yozgat" -> Pair(39.8181, 34.8147)
    "zonguldak" -> Pair(41.4564, 31.7987)
    "aksaray" -> Pair(38.3687, 34.0370)
    "bayburt" -> Pair(40.2552, 40.2249)
    "karaman" -> Pair(37.1759, 33.2287)
    "kirikkale" -> Pair(39.8468, 33.5153)
    "batman" -> Pair(37.8812, 41.1294)
    "sirnak" -> Pair(37.5164, 42.4918)
    "bartin" -> Pair(41.6344, 32.3375)
    "ardahan" -> Pair(41.1105, 42.7022)
    "igdir" -> Pair(39.9196, 44.0450)
    "yalova" -> Pair(40.6500, 29.2667)
    "karabuk" -> Pair(41.2061, 32.6204)
    "kilis" -> Pair(36.7184, 37.1212)
    "osmaniye" -> Pair(37.0742, 36.2472)
    "duzce" -> Pair(40.8438, 31.1565)
    // World major cities
    "berlin" -> Pair(52.5200, 13.4050)
    "koln" -> Pair(50.9375, 6.9603)
    "amsterdam" -> Pair(52.3676, 4.9041)
    "paris" -> Pair(48.8566, 2.3522)
    "london", "londra" -> Pair(51.5074, -0.1278)
    "vienna", "viyana" -> Pair(48.2082, 16.3738)
    "brussels", "bruksel" -> Pair(50.8503, 4.3517)
    "baku", "baku" -> Pair(40.4093, 49.8671)
    else -> Pair(38.6191, 27.4289) // Default Manisa
  }
}
