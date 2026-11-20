package com.example.qibla

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.i18n.AppLanguage
import com.example.util.AutoLocation
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun L(tr: String, en: String) = if (AppLanguage.isEnglish) en else tr

/** Kıble hesabında kullanılan konum. */
data class QiblaPlace(val lat: Double, val lon: Double, val label: String, val fromGps: Boolean)

/** Kıbleye "dönük" sayılma toleransı (derece). */
const val QIBLA_ALIGN_TOLERANCE = 3.0

@Composable
fun QiblaFinderScreen(
  prefs: AppPreferences,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  /** Yalnızca testler için: sensör yerine sabit okuma ve konum. */
  testReading: CompassReading? = null,
  testPlace: QiblaPlace? = null,
  /** Yalnızca testler için: kalibrasyon animasyonunu zorla göster/gizle ve karesini sabitle. */
  showCalibrationIntro: Boolean? = null,
  calibrationTestProgress: Float? = null,
  showDetailsInitially: Boolean = false,
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  // ── Konum: önce cihaz GPS'i, yoksa seçili il/ilçe merkezi ──
  val tablePlace = remember(prefs.province, prefs.district) {
    QiblaPlaces.coordinates(prefs.province, prefs.district)?.let {
      QiblaPlace(it.first, it.second, "${prefs.province} / ${prefs.district}", fromGps = false)
    }
  }
  var gpsPlace by remember { mutableStateOf<QiblaPlace?>(null) }
  var hasPermission by remember { mutableStateOf(AutoLocation.hasPermission(context)) }
  var permissionAsked by remember { mutableIntStateOf(0) }
  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    hasPermission = AutoLocation.hasPermission(context); permissionAsked++
  }
  LaunchedEffect(hasPermission, permissionAsked) {
    if (hasPermission) {
      AutoLocation.currentLocation(context)?.let {
        gpsPlace = QiblaPlace(it.latitude, it.longitude, L("📍 Cihaz konumu (GPS)", "📍 Device location (GPS)"), fromGps = true)
      }
    }
  }
  val place = testPlace ?: gpsPlace ?: tablePlace

  // ── Diyanet yöntemiyle kıble değerleri ──
  val trueBearing = remember(place) { place?.let { QiblaCalculator.trueBearing(it.lat, it.lon) } }
  val declination = remember(place) { place?.let { QiblaCalculator.declination(it.lat, it.lon) } ?: 0.0 }
  val expectedField = remember(place) { place?.let { QiblaCalculator.expectedFieldMicroTesla(it.lat, it.lon) } ?: 0.0 }
  val distanceKm = remember(place) { place?.let { QiblaCalculator.distanceKm(it.lat, it.lon) } }
  // Kıble Saati: GPS kullanılmıyorsa Diyanet'in ilçe tablosundaki değer, yoksa hesap (aynı yöntem)
  val qiblaTime = remember(place) {
    place?.let { p ->
      val today = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(java.util.Date())
      (if (!p.fromGps) com.example.data.DiyanetSource.cachedDay(context, prefs.country, prefs.province, prefs.district, today)?.kibleSaati else null)
        ?: QiblaCalculator.qiblaTimeText(p.lat, p.lon)
    }
  }
  val diyanetAngle = remember(place) { place?.let { QiblaCalculator.diyanetPublishedAngle(it.lat, it.lon) } }
  var sunAzimuth by remember { mutableStateOf<Double?>(null) }
  LaunchedEffect(place) {
    while (place != null) {
      val s = QiblaCalculator.sun(place.lat, place.lon, System.currentTimeMillis())
      sunAzimuth = if (s.elevation > 0) s.azimuth else null
      kotlinx.coroutines.delay(60_000)
    }
  }

  // ── Pusula ──
  val currentDeclination by rememberUpdatedState(declination)
  val currentExpected by rememberUpdatedState(expectedField)
  var reading by remember { mutableStateOf(CompassReading()) }
  var hasCompass by remember { mutableStateOf(true) }
  if (testReading != null) { reading = testReading }
  DisposableEffect(Unit) {
    if (testReading != null) return@DisposableEffect onDispose { }
    val sensor = CompassSensor(context, { currentDeclination }, { currentExpected }) { reading = it }
    hasCompass = sensor.hasCompass
    sensor.start()
    onDispose { sensor.stop() }
  }

  // Ekrandaki kadran açısı: süzgeçten gelen kesintisiz açı → kısa yoldan, yaylı animasyon
  val dialAngle by animateFloatAsState(
    targetValue = -reading.continuousTrueHeading.toFloat(),
    animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f),
    label = "dial",
  )

  val turn = trueBearing?.let { QiblaCalculator.turnAngle(it, reading.trueHeading) }
  val aligned = reading.ready && turn != null && abs(turn) <= QIBLA_ALIGN_TOLERANCE
  LaunchedEffect(aligned) { if (aligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

  val green = Color(0xFF2E7D57)
  val gold = Color(0xFFC59B3C)
  val statusColor by animateColorAsState(if (aligned) green else MaterialTheme.colorScheme.onBackground, label = "status")

  // Açılışta kalibrasyon animasyonu (testlerde yalnızca istenirse)
  var showCalibration by remember {
    mutableStateOf(showCalibrationIntro ?: (testReading == null && prefs.qiblaCalibrationIntro))
  }
  var showDetails by remember { mutableStateOf(showDetailsInitially) }

  Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("qibla_finder_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Üst çubuk
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L("Geri", "Back")) }
      Column(Modifier.padding(start = 4.dp).weight(1f)) {
        Text(L("Kıble Bulucu", "Qibla Finder"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(place?.label ?: L("Konum bulunamadı", "Location unavailable"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
      }
      if (place != null) {
        TextButton(onClick = { showCalibration = true }, modifier = Modifier.testTag("qibla_calibrate_button")) {
          Text(L("🔄 Kalibre et", "🔄 Calibrate"), fontSize = 13.sp)
        }
      }
    }

    if (place == null || trueBearing == null) {
      Spacer(Modifier.height(24.dp))
      Text(
        L("Kıble yönünü hesaplamak için konum gerekli. Konum iznini verin veya Ayarlar'dan il/ilçe seçin.",
          "Your location is needed to calculate the qibla. Allow location access or choose a city in Settings."),
        textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(Modifier.height(12.dp))
      OutlinedButton(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) {
        Text(L("📍 Konumumu kullan", "📍 Use my location"))
      }
      return@Column
    }

    Spacer(Modifier.height(20.dp))

    // ── Tek satır büyük durum ──
    Column(Modifier.fillMaxWidth().height(72.dp).testTag("qibla_status"), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
      Text(
        when {
          !hasCompass -> L("Cihazınızda pusula sensörü yok", "Your device has no compass sensor")
          !reading.ready -> L("Pusula hazırlanıyor…", "Starting compass…")
          aligned -> L("🕋 KIBLEYE DÖNDÜNÜZ", "🕋 YOU ARE FACING THE QIBLA")
          turn == null -> ""
          turn > 0 -> L("Sağa doğru ${abs(turn).roundToInt()}° dönün  ⟳", "Turn right ${abs(turn).roundToInt()}°  ⟳")
          else -> L("⟲  Sola doğru ${abs(turn).roundToInt()}° dönün", "⟲  Turn left ${abs(turn).roundToInt()}°")
        },
        fontWeight = FontWeight.Bold, fontSize = if (aligned) 22.sp else 24.sp, color = statusColor, textAlign = TextAlign.Center,
      )
      if (aligned) Text(L("Huşû ile namaza durabilirsiniz.", "You may begin your prayer."), fontSize = 14.sp, color = green)
    }

    Spacer(Modifier.height(12.dp))

    // ── Sade pusula kadranı ──
    val dark = MaterialTheme.colorScheme.background.let { (it.red + it.green + it.blue) / 3 < 0.5f }
    val dialBg = if (dark) Color(0xFF15140F) else Color(0xFFFBF7EC)
    val tick = if (dark) Color(0xFF5B5446) else Color(0xFFD5C8AE)
    val labelColor = if (dark) Color(0xFFE8DFC8) else Color(0xFF4A3B22)
    val accent = if (aligned) green else gold
    Box(Modifier.size(310.dp).testTag("qibla_dial"), contentAlignment = Alignment.Center) {
      Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height / 2)
        val rad = size.minDimension / 2 - 8.dp.toPx()
        drawCircle(dialBg, rad, c)
        drawCircle(accent, rad, c, style = Stroke(if (aligned) 6.dp.toPx() else 3.dp.toPx()))
        rotate(dialAngle, c) {
          // Yalnızca 30°'lik ana çizgiler
          for (d in 0 until 360 step 30) {
            val a = Math.toRadians(d - 90.0)
            val r1 = rad - 6.dp.toPx(); val r2 = rad - 16.dp.toPx()
            drawLine(tick, Offset(c.x + r2 * cos(a).toFloat(), c.y + r2 * sin(a).toFloat()), Offset(c.x + r1 * cos(a).toFloat(), c.y + r1 * sin(a).toFloat()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
          }
          // Kıble ibresi: kadranla birlikte döner → her zaman Kâbe'yi gösterir
          rotate(trueBearing.toFloat(), c) {
            val tip = c.y - rad + 46.dp.toPx()
            val arrow = Path().apply {
              moveTo(c.x, tip)
              lineTo(c.x + 16.dp.toPx(), c.y + 6.dp.toPx())
              lineTo(c.x, c.y - 8.dp.toPx())
              lineTo(c.x - 16.dp.toPx(), c.y + 6.dp.toPx())
              close()
            }
            drawPath(arrow, accent)
          }
        }
        drawCircle(accent, 7.dp.toPx(), c)
        // Telefonun baktığı yön (sabit, üstte)
        val top = c.y - rad - 4.dp.toPx()
        drawLine(if (aligned) green else Color(0xFFD32F2F), Offset(c.x, top), Offset(c.x, top + 22.dp.toPx()), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
      }
      DialBadge(angle = dialAngle.toDouble(), radiusDp = 112f) {
        Text(L("K", "N"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD32F2F))
      }
      listOf(90.0, 180.0, 270.0).forEachIndexed { i, a ->
        DialBadge(angle = a + dialAngle, radiusDp = 112f) {
          Text(listOf(L("D", "E"), L("G", "S"), L("B", "W"))[i], fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = labelColor.copy(alpha = 0.7f))
        }
      }
      DialBadge(angle = trueBearing + dialAngle, radiusDp = 140f) {
        Box(Modifier.size(38.dp).background(accent, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
          Text("🕋", fontSize = 20.sp)
        }
      }
    }

    // ── En önemli tek uyarı ──
    val warning = when {
      !reading.ready || !hasCompass -> null
      reading.interference -> L("🧲 Yakında mıknatıs veya metal var; biraz uzaklaşın.", "🧲 Magnet or metal nearby; move away a little.")
      reading.accuracy in 0..1 -> L("🔄 Pusula kalibrasyon istiyor: \"Kalibre et\"e dokunun.", "🔄 Compass needs calibration: tap \"Calibrate\".")
      reading.tiltDeg in 25.0..65.0 -> L("📱 Telefonu yere paralel tutun.", "📱 Hold the phone flat.")
      reading.jitterDeg > 8 -> L("〰️ Telefonu sabit tutun.", "〰️ Hold the phone still.")
      else -> null
    }
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
      warning?.let {
        Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center,
          modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp).testTag("qibla_warning"))
      }
    }

    // ── Kısa özet + ayrıntılar ──
    Text(
      L("Kıble açısı $diyanetAngle° • Kıble saati ${qiblaTime ?: "—"}", "Qibla angle $diyanetAngle° • Qibla time ${qiblaTime ?: "—"}"),
      fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("qibla_summary"),
    )
    TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.testTag("qibla_details_toggle")) {
      Text(if (showDetails) L("Ayrıntıları gizle ▴", "Hide details ▴") else L("Ayrıntılar ▾", "Details ▾"), fontSize = 13.sp)
    }
    if (showDetails) {
      Card(Modifier.fillMaxWidth().testTag("qibla_diyanet_card"), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          InfoRow(L("Kıble açısı (resmî takvim)", "Qibla angle (official calendar)"), "${diyanetAngle}°", bold = true)
          InfoRow(L("Kıble açısı (gerçek kuzey)", "Qibla angle (true north)"), String.format(Locale.ROOT, "%.1f°", trueBearing))
          InfoRow(L("Bugünkü el pusulası açısı", "Today's hand-compass angle"), "${QiblaCalculator.diyanetAngle(trueBearing, declination)}°")
          InfoRow(L("Manyetik sapma (bugün)", "Magnetic declination (today)"), String.format(Locale.ROOT, "%+.1f°", declination))
          distanceKm?.let { InfoRow(L("Kâbe'ye uzaklık", "Distance to the Kaaba"), String.format(Locale.ROOT, "%,.0f km", it)) }
          InfoRow(L("Bugünkü Kıble Saati", "Today's qibla time"), qiblaTime ?: "—", bold = true)
          sunAzimuth?.let { InfoRow(L("Güneşin yönü (şu an)", "Sun direction (now)"), String.format(Locale.ROOT, "%.0f°", it)) }
          Text(
            L("Kıble Saati'nde güneşe dönen kişi kıbleye dönmüş olur; güneş mıknatıs ve metalden etkilenmez. Resmî takvimdeki açı, tablonun hazırlandığı yılın manyetik sapmasına göredir; el pusulasında bugün 1–2° farklı okunabilir. Uygulamanın ibresi gerçek kuzeye göre hesaplanır.",
              "At the qibla time, facing the sun means facing the qibla. The official calendar angle uses the magnetic declination of the year its table was prepared, so a hand compass may read 1–2° differently today. The app's needle uses true north."),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      if (!place.fromGps) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) {
          Text(L("📍 Daha hassas sonuç için konumumu kullan", "📍 Use my location for better accuracy"), fontSize = 12.sp)
        }
      }
    }
  }

  if (showCalibration && place != null) {
    CalibrationOverlay(
      accuracy = reading.accuracy,
      animate = testReading == null,
      testProgress = calibrationTestProgress,
      onDone = { showCalibration = false },
      onNeverShow = { prefs.qiblaCalibrationIntro = false; showCalibration = false },
    )
  }
  }
}

@Composable
private fun InfoRow(label: String, value: String, bold: Boolean = false) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    Text(value, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
  }
}

/** İçeriği kadran merkezinden [angle] yönünde [radiusDp] uzağa yerleştirir (içerik dik kalır). */
@Composable
private fun DialBadge(angle: Double, radiusDp: Float, content: @Composable () -> Unit) {
  val a = Math.toRadians(angle - 90)
  Box(Modifier.offset((radiusDp * cos(a)).toFloat().dp, (radiusDp * sin(a)).toFloat().dp)) { content() }
}

private fun directionName(deg: Double): String {
  val tr = listOf("Kuzey", "Kuzeydoğu", "Doğu", "Güneydoğu", "Güney", "Güneybatı", "Batı", "Kuzeybatı")
  val en = listOf("North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest")
  val i = (((QiblaCalculator.norm(deg) + 22.5) / 45).toInt()) % 8
  return if (AppLanguage.isEnglish) en[i] else tr[i]
}
