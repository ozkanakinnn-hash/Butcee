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
  val statusColor by animateColorAsState(if (aligned) green else MaterialTheme.colorScheme.surfaceVariant, label = "status")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("qibla_finder_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Üst çubuk
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L("Geri", "Back")) }
      Column(Modifier.padding(start = 4.dp)) {
        Text(L("Kıble Bulucu", "Qibla Finder"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(place?.label ?: L("Konum bulunamadı", "Location unavailable"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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

    Spacer(Modifier.height(12.dp))

    // Durum
    Card(
      Modifier.fillMaxWidth().testTag("qibla_status"), shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = statusColor),
    ) {
      val onStatus = if (aligned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
      Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          when {
            !hasCompass -> L("Cihazınızda pusula sensörü yok", "Your device has no compass sensor")
            !reading.ready -> L("Pusula hazırlanıyor…", "Starting compass…")
            aligned -> L("🕋 KIBLEYE DÖNDÜNÜZ", "🕋 YOU ARE FACING THE QIBLA")
            else -> L("KIBLE YÖNÜNÜ BULUN", "FIND THE QIBLA")
          },
          fontWeight = FontWeight.Bold, fontSize = 17.sp, color = onStatus,
        )
        if (hasCompass && reading.ready && turn != null && !aligned) {
          val deg = abs(turn).roundToInt()
          Text(
            if (turn > 0) L("Sağa doğru $deg° dönün  ⟳", "Turn right $deg°  ⟳") else L("⟲  Sola doğru $deg° dönün", "⟲  Turn left $deg°"),
            fontSize = 14.sp, color = onStatus,
          )
        }
        if (aligned) Text(L("Hizalama tamam. Huşû ile namaza durabilirsiniz.", "Aligned. You may begin your prayer."), fontSize = 13.sp, color = onStatus)
      }
    }

    // Uyarılar (kararlılığı bozan durumlar)
    val warnings = buildList {
      if (reading.interference) add(L("🧲 Yakında manyetik parazit var (kılıf mıknatısı, metal masa, hoparlör, araç). Bu konumdan uzaklaşın.",
        "🧲 Magnetic interference nearby (magnetic case, metal desk, speaker, car). Move away from it."))
      if (reading.accuracy in 0..1) add(L("🔄 Pusula kalibrasyonu gerekiyor: telefonu havada birkaç kez 8 çizerek çevirin.",
        "🔄 Compass needs calibration: move the phone in a figure-8 a few times."))
      if (reading.ready && reading.tiltDeg in 25.0..65.0) add(L("📱 Telefonu yere paralel (yatay) tutun.", "📱 Hold the phone flat."))
      if (reading.ready && reading.jitterDeg > 8) add(L("〰️ Ölçüm dalgalanıyor; telefonu sabit tutun.", "〰️ Reading is unsteady; hold the phone still."))
    }
    warnings.forEach {
      Spacer(Modifier.height(8.dp))
      Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)).padding(10.dp))
    }

    Spacer(Modifier.height(18.dp))

    // ── Pusula kadranı ──
    val dark = MaterialTheme.colorScheme.background.let { (it.red + it.green + it.blue) / 3 < 0.5f }
    val dialBg = if (dark) Color(0xFF15140F) else Color(0xFFFBF7EC)
    val tickMinor = if (dark) Color(0xFF5B5446) else Color(0xFFD5C8AE)
    val labelColor = if (dark) Color(0xFFE8DFC8) else Color(0xFF4A3B22)
    val ring = if (aligned) green else gold
    Box(Modifier.size(300.dp).testTag("qibla_dial"), contentAlignment = Alignment.Center) {
      Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height / 2)
        val rad = size.minDimension / 2 - 6.dp.toPx()
        drawCircle(dialBg, rad, c)
        drawCircle(ring, rad, c, style = Stroke(4.dp.toPx()))
        rotate(dialAngle, c) {
          for (d in 0 until 360 step 5) {
            val major = d % 30 == 0
            val len = if (major) 14.dp.toPx() else if (d % 10 == 0) 9.dp.toPx() else 5.dp.toPx()
            val a = Math.toRadians(d - 90.0)
            drawLine(
              if (major) gold else tickMinor,
              Offset(c.x + (rad - 4.dp.toPx() - len) * cos(a).toFloat(), c.y + (rad - 4.dp.toPx() - len) * sin(a).toFloat()),
              Offset(c.x + (rad - 4.dp.toPx()) * cos(a).toFloat(), c.y + (rad - 4.dp.toPx()) * sin(a).toFloat()),
              strokeWidth = if (major) 2.5f.dp.toPx() / 1.5f else 1.2f,
            )
          }
          // Kıble ibresi (kadranla birlikte döner → her zaman Kâbe'yi gösterir)
          rotate(trueBearing.toFloat(), c) {
            val tip = c.y - rad + 30.dp.toPx()
            val arrow = Path().apply {
              moveTo(c.x, tip)
              lineTo(c.x + 11.dp.toPx(), c.y)
              lineTo(c.x, c.y + 18.dp.toPx())
              lineTo(c.x - 11.dp.toPx(), c.y)
              close()
            }
            drawPath(arrow, if (aligned) green else gold)
            val edge = Path().apply { moveTo(c.x, tip); lineTo(c.x + 11.dp.toPx(), c.y); lineTo(c.x, c.y); close() }
            drawPath(edge, Color.White.copy(alpha = 0.25f))
          }
        }
        drawCircle(ring, 9.dp.toPx(), c)
        drawCircle(dialBg, 4.dp.toPx(), c)
        // Telefonun baktığı yön işareti (sabit, üstte)
        val top = c.y - rad - 2.dp.toPx()
        drawLine(if (aligned) green else Color(0xFFD32F2F), Offset(c.x, top), Offset(c.x, top + 18.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
      }
      // Yön harfleri ve Kâbe/Güneş rozetleri (kadranla döner)
      val letters = listOf(L("K", "N") to 0.0, L("D", "E") to 90.0, L("G", "S") to 180.0, L("B", "W") to 270.0)
      letters.forEach { (s, a) ->
        DialBadge(angle = a + dialAngle, radiusDp = 108f) {
          Text(s, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = if (a == 0.0) Color(0xFFD32F2F) else labelColor)
        }
      }
      DialBadge(angle = trueBearing + dialAngle, radiusDp = 134f) {
        Box(Modifier.size(34.dp).background(if (aligned) green else gold, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
          Text("🕋", fontSize = 18.sp)
        }
      }
      sunAzimuth?.let { DialBadge(angle = it + dialAngle, radiusDp = 80f) { Text("☀️", fontSize = 16.sp) } }
    }

    Spacer(Modifier.height(8.dp))
    Text(
      L("Telefon yönü: ${reading.trueHeading.roundToInt() % 360}° ${directionName(reading.trueHeading)}", "Heading: ${reading.trueHeading.roundToInt() % 360}° ${directionName(reading.trueHeading)}"),
      fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.height(16.dp))

    // ── Diyanet verileri ──
    Card(Modifier.fillMaxWidth().testTag("qibla_diyanet_card"), shape = RoundedCornerShape(18.dp)) {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(L("Kıble Bilgileri", "Qibla Data"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        InfoRow(L("Kıble açısı (resmî takvim)", "Qibla angle (official calendar)"), "${diyanetAngle}°", bold = true)
        InfoRow(L("Kıble açısı (gerçek kuzey)", "Qibla angle (true north)"), String.format(Locale.ROOT, "%.1f°", trueBearing))
        InfoRow(L("Bugünkü el pusulası açısı", "Today's hand-compass angle"), "${QiblaCalculator.diyanetAngle(trueBearing, declination)}°")
        InfoRow(L("Manyetik sapma (bugün)", "Magnetic declination (today)"), String.format(Locale.ROOT, "%+.1f°", declination))
        distanceKm?.let { InfoRow(L("Kâbe'ye uzaklık", "Distance to the Kaaba"), String.format(Locale.ROOT, "%,.0f km", it)) }
        InfoRow(L("Bugünkü Kıble Saati", "Today's qibla time"), qiblaTime ?: "—", bold = true)
        Text(
          L("Kıble Saati'nde güneşe dönen kişi kıbleye dönmüş olur; güneş mıknatıs ve metalden etkilenmez. Resmî takvimdeki açı, tablonun hazırlandığı yılın manyetik sapmasına göredir; bugün manyetik kuzey biraz kaydığı için el pusulasında 1–2° farklı okunur. Uygulamanın ibresi gerçek kuzeye göre hesaplandığından bu farktan etkilenmez.",
            "At the qibla time, facing the sun means facing the qibla. The official calendar angle uses the magnetic declination of the year its table was prepared, so a hand compass today reads 1–2° differently. The app's needle uses true north and is not affected."),
          fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (!(place.fromGps)) {
      Spacer(Modifier.height(10.dp))
      OutlinedButton(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) {
        Text(L("📍 Daha hassas sonuç için konumumu kullan", "📍 Use my location for better accuracy"), fontSize = 12.sp)
      }
    }

    Spacer(Modifier.height(12.dp))
    Text(
      L("İpuçları: Telefonu avucunuzda yatay tutun • Mıknatıslı kılıfı çıkarın • Metal eşya, hoparlör ve araç içinden uzak durun • Uyarı çıkarsa 8 çizerek kalibre edin.",
        "Tips: hold the phone flat • remove magnetic cases • stay away from metal, speakers and cars • calibrate with a figure-8 if warned."),
      fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
    )
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
