package com.example.qibla

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private fun L(tr: String, en: String) = if (AppLanguage.isEnglish) en else tr

/** Sonsuzluk (8) eğrisi: Bernoulli lemniskatı, t ∈ [0, 2π). Birim genişliğe göre (-1..1, -0.35..0.35). */
private fun lemniscate(t: Double): Pair<Double, Double> {
  val d = 1 + sin(t) * sin(t)
  return cos(t) / d to sin(t) * cos(t) / d
}

/**
 * Kıble ekranı açılışında gösterilen kalibrasyon anlatımı: telefon simgesi havada 8 çizer.
 * [accuracy] sensörün bildirdiği doğruluk (0–3); 3 olunca "Pusula hazır" görünür.
 */
@Composable
fun CalibrationOverlay(
  accuracy: Int,
  animate: Boolean,
  testProgress: Float? = null,
  onDone: () -> Unit,
  onNeverShow: () -> Unit,
) {
  BackHandler { onDone() }
  val progress = if (testProgress != null || !animate) (testProgress ?: 0.15f) else {
    val inf = rememberInfiniteTransition(label = "calib")
    val p by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart), label = "p")
    p
  }
  val gold = Color(0xFFC59B3C)
  val green = Color(0xFF2E7D57)
  val ready = accuracy >= 3

  Box(
    Modifier
      .fillMaxSize()
      .background(Color(0xFF12110E))
      // Arkadaki ekrana dokunuşları engelle
      .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
      .testTag("qibla_calibration"),
    contentAlignment = Alignment.Center,
  ) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(L("Pusulayı kalibre edin", "Calibrate your compass"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
      Spacer(Modifier.height(6.dp))
      Text(
        L("Doğru yön için telefonu havada yavaşça 8 çizerek 2–3 kez çevirin.",
          "For an accurate direction, slowly move the phone in a figure-8 two or three times."),
        color = Color(0xFFE8DFC8), fontSize = 15.sp, textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(20.dp))

      Canvas(Modifier.size(300.dp, 220.dp)) { drawFigureEight(progress, gold) }

      Spacer(Modifier.height(16.dp))
      Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Step("1", L("Telefonu avucunuza alın.", "Hold the phone in your hand."))
        Step("2", L("Bileğinizi çevirerek havada büyük bir 8 çizin.", "Draw a large 8 in the air, turning your wrist."))
        Step("3", L("Mıknatıslı kılıf, metal ve hoparlörden uzak durun.", "Keep away from magnetic cases, metal and speakers."))
      }
      Spacer(Modifier.height(16.dp))

      // Sensör doğruluğu
      val (label, color) = when {
        accuracy >= 3 -> L("✓ Pusula hazır", "✓ Compass ready") to green
        accuracy == 2 -> L("Hassasiyet: orta, biraz daha çizin", "Accuracy: medium, keep going") to gold
        accuracy >= 0 -> L("Hassasiyet: düşük", "Accuracy: low") to Color(0xFFE57373)
        else -> L("Pusula bekleniyor…", "Waiting for compass…") to Color(0xFFBDBDBD)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.testTag("qibla_calibration_accuracy"))
      }
      Spacer(Modifier.height(20.dp))
      Button(
        onClick = onDone,
        colors = ButtonDefaults.buttonColors(containerColor = if (ready) green else gold, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("qibla_calibration_done"),
        shape = RoundedCornerShape(14.dp),
      ) { Text(if (ready) L("Kıbleyi bul", "Find the qibla") else L("Tamam", "OK"), fontSize = 17.sp, fontWeight = FontWeight.Bold) }
      TextButton(onClick = onNeverShow) {
        Text(L("Açılışta bir daha gösterme", "Don't show on start again"), color = Color(0xFFBDBDBD), fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun Step(n: String, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(24.dp).background(Color(0x33C59B3C), RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
      Text(n, color = Color(0xFFC59B3C), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.width(10.dp))
    Text(text, color = Color.White, fontSize = 14.sp)
  }
}

/** 8 yolunu kesikli çizer, telefon simgesini yol üzerinde [progress] noktasına yerleştirir. */
private fun DrawScope.drawFigureEight(progress: Float, gold: Color) {
  val w = size.width * 0.42f
  val c = Offset(size.width / 2, size.height / 2)
  fun pt(t: Double): Offset { val (x, y) = lemniscate(t); return Offset(c.x + (x * w).toFloat(), c.y + (y * w * 1.9).toFloat()) }

  // Yol (kesikli)
  val path = Path()
  for (i in 0..120) { val o = pt(2 * PI * i / 120); if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
  drawPath(path, gold.copy(alpha = 0.35f), style = Stroke(3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f)), cap = StrokeCap.Round))

  // Telefonun arkasından gelen iz
  val t = 2 * PI * progress
  val trail = Path()
  for (i in 0..30) { val o = pt(t - 1.4 * (30 - i) / 30); if (i == 0) trail.moveTo(o.x, o.y) else trail.lineTo(o.x, o.y) }
  drawPath(trail, gold, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))

  // Telefon simgesi: yolun yönüne göre hafifçe yatar (bilek dönüşü hissi)
  val p = pt(t); val q = pt(t + 0.02)
  val tilt = Math.toDegrees(atan2((q.y - p.y).toDouble(), (q.x - p.x).toDouble())).toFloat() * 0.35f
  val pw = 42.dp.toPx(); val ph = 74.dp.toPx()
  rotate(tilt, p) {
    drawRoundRect(Color(0xFF2A2620), Offset(p.x - pw / 2, p.y - ph / 2), Size(pw, ph), CornerRadius(9.dp.toPx()))
    drawRoundRect(gold, Offset(p.x - pw / 2, p.y - ph / 2), Size(pw, ph), CornerRadius(9.dp.toPx()), style = Stroke(2.5f.dp.toPx()))
    // Ekrandaki küçük pusula ibresi
    val n = Path().apply {
      moveTo(p.x, p.y - 16.dp.toPx()); lineTo(p.x + 6.dp.toPx(), p.y + 4.dp.toPx()); lineTo(p.x - 6.dp.toPx(), p.y + 4.dp.toPx()); close()
    }
    drawPath(n, Color(0xFFD32F2F))
    drawCircle(gold, 3.dp.toPx(), Offset(p.x, p.y + 12.dp.toPx()))
  }
}
