package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.lerp

/** Bir caminin silüet özellikleri (gerçek yapılara göre sadeleştirilmiş oranlar). */
data class MosqueSpec(
  val domeR: Float,            // ana kubbe yarıçapı (genişliğin oranı)
  val domeHeight: Float,       // 1 = yarım küre, <1 basık (Ayasofya)
  val semiDomes: Int,          // 0, 2 veya 4 yarım kubbe
  val smallDomes: Int,         // gövde üstündeki küçük kubbe sayısı
  val minarets: List<Pair<Float, Float>>, // (x oranı, yükseklik çarpanı)
  val serefe: Int,             // minare başına şerefe sayısı
  val wall: Color,             // taş rengi
  val lead: Color,             // kurşun kubbe rengi
  val buttresses: Boolean = false,
  val hill: Boolean = false,
  val bridge: Boolean = false,
  val baroque: Boolean = false
)

object IstanbulMosques {
  val sultanahmet = MosqueSpec(0.17f, 0.95f, 4, 4,
    listOf(0.06f to 0.92f, 0.16f to 0.85f, 0.30f to 0.80f, 0.70f to 0.80f, 0.84f to 0.85f, 0.94f to 0.92f), 3,
    Color(0xFFB9AB98), Color(0xFF7D8EA0))
  val ayasofya = MosqueSpec(0.21f, 0.62f, 2, 0,
    listOf(0.08f to 0.88f, 0.20f to 0.78f, 0.80f to 0.78f, 0.92f to 0.88f), 2,
    Color(0xFFD08A5E), Color(0xFF7F93A5), buttresses = true)
  val suleymaniye = MosqueSpec(0.18f, 0.9f, 2, 6,
    listOf(0.07f to 0.92f, 0.19f to 0.78f, 0.81f to 0.78f, 0.93f to 0.92f), 3,
    Color(0xFFC2B6A4), Color(0xFF7B8C9C))
  val camlica = MosqueSpec(0.19f, 0.95f, 4, 4,
    listOf(0.05f to 0.88f, 0.14f to 0.80f, 0.24f to 0.74f, 0.76f to 0.74f, 0.86f to 0.80f, 0.95f to 0.88f), 3,
    Color(0xFFD8D2C8), Color(0xFF8A9CAB), hill = true)
  val eyup = MosqueSpec(0.15f, 0.95f, 0, 3,
    listOf(0.20f to 0.82f, 0.80f to 0.82f), 2,
    Color(0xFFD6CDBD), Color(0xFF7F90A0))
  val ortakoy = MosqueSpec(0.15f, 1.05f, 0, 0,
    listOf(0.20f to 0.76f, 0.80f to 0.76f), 1,
    Color(0xFFE3DCCD), Color(0xFF8B9DAC), bridge = true, baroque = true)
  val fatih = MosqueSpec(0.18f, 0.95f, 4, 2,
    listOf(0.12f to 0.84f, 0.88f to 0.84f), 2,
    Color(0xFFC4B8A6), Color(0xFF7B8C9C))
}

private fun Color.shade(f: Float): Color =
  if (f >= 1f) lerp(this, Color.White, (f - 1f).coerceIn(0f, 1f)) else lerp(Color.Black, this, f.coerceIn(0f, 1f))

private val warmLight = Color(0xFFFFD282)
private val gold = Color(0xFFE8C070)

/**
 * Gece, kızıl gökyüzü altında projektörle aydınlatılmış İstanbul camisi; önünde ağaçlar,
 * fenerli meydan ve (varsa) minareler arasında mahya yazısı.
 */
@Composable
fun IstanbulMosqueScene(spec: MosqueSpec, glow: Float, mahya: List<String>? = null, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val ground = h * 0.62f

    // Kızıl gece gökyüzü
    drawRect(
      Brush.verticalGradient(
        0f to Color(0xFF0E0A1C), 0.35f to Color(0xFF2A0F24), 0.7f to Color(0xFF7A1C24), 1f to Color(0xFFC2402C),
        startY = 0f, endY = ground
      ),
      size = Size(w, ground)
    )
    drawRect(
      Brush.radialGradient(
        0f to Color(0xFFF06E3C).copy(alpha = 0.55f * glow), 1f to Color.Transparent,
        center = Offset(w * 0.15f, ground), radius = w * 0.9f
      ),
      size = Size(w, ground)
    )
    var seed = 11
    fun rnd(): Float { seed = (seed * 9301 + 49297) % 233280; return seed / 233280f }
    repeat(70) {
      val y = rnd() * ground * 0.55f
      val a = 0.3f + rnd() * 0.6f
      drawCircle(Color.White.copy(alpha = a), radius = rnd() * 1.4f + 0.4f, center = Offset(rnd() * w, y))
    }
    listOf(Triple(0.25f, 0.32f, 0.35f), Triple(0.7f, 0.40f, 0.3f), Triple(0.5f, 0.47f, 0.4f)).forEach { (x, y, r) ->
      drawOval(Color(0xFFFF785A).copy(alpha = 0.12f), topLeft = Offset(w * x - w * r, h * y - h * 0.02f), size = Size(w * r * 2, h * 0.04f))
    }
    // Uzaktaki cami silüetleri
    val far = Color(0xFF3C1928).copy(alpha = 0.7f)
    listOf(0.04f, 0.12f, 0.84f, 0.92f).forEach { fx ->
      val r = w * 0.03f
      drawArc(far, 180f, 180f, true, topLeft = Offset(w * fx - r, ground - h * 0.03f - r), size = Size(r * 2, r * 2))
      drawRect(far, topLeft = Offset(w * fx + r * 1.3f, ground - h * 0.12f), size = Size(w * 0.004f, h * 0.1f))
    }

    val minaretTops = drawMosque(spec, w, h, ground)

    // Mahya: en dıştaki iki minare arasına gerili ışıklı yazı
    if (!mahya.isNullOrEmpty() && minaretTops.size >= 2) {
      val left = minaretTops.minByOrNull { it.x }!!
      val right = minaretTops.maxByOrNull { it.x }!!
      val y1 = maxOf(left.y, right.y) + h * 0.04f
      val lineGap = h * 0.055f
      mahya.forEachIndexed { k, _ ->
        val y = y1 + k * lineGap
        drawPath(Path().apply {
          moveTo(left.x, y - h * 0.01f); quadraticBezierTo(w / 2f, y + h * 0.012f, right.x, y - h * 0.01f)
        }, Color(0xFFFFDCA0).copy(alpha = 0.25f), style = Stroke(width = 1f))
      }
      drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
          isAntiAlias = true
          textAlign = android.graphics.Paint.Align.CENTER
          typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
          color = android.graphics.Color.rgb(255, 215, 138)
          setShadowLayer(w * 0.02f, 0f, 0f, android.graphics.Color.argb(230, 255, 170, 60))
        }
        mahya.forEachIndexed { k, text ->
          val span = right.x - left.x
          paint.textSize = minOf(w * 0.055f, span * 0.9f / (text.length * 0.62f))
          canvas.nativeCanvas.drawText(text, w / 2f, y1 + k * lineGap + paint.textSize * 0.35f, paint)
        }
      }
    }

    // Meydan zemini ve caminin önüne vuran ışık
    drawRect(
      Brush.verticalGradient(0f to Color(0xFF3A2A2A), 1f to Color(0xFF120D10), startY = ground, endY = h),
      topLeft = Offset(0f, ground), size = Size(w, h - ground)
    )
    drawRect(
      Brush.radialGradient(0f to Color(0xFFFFB464).copy(alpha = 0.35f), 1f to Color.Transparent, center = Offset(w / 2f, ground), radius = w * 0.6f),
      topLeft = Offset(0f, ground), size = Size(w, h - ground)
    )
    // Ağaçlar
    fun tree(x: Float, y: Float, r: Float) {
      val leaf = Color(0xFF0B120D)
      drawRect(leaf, topLeft = Offset(x - r * 0.06f, y - r * 0.2f), size = Size(r * 0.12f, r * 0.4f))
      repeat(6) {
        drawCircle(leaf, radius = r * (0.45f + rnd() * 0.35f), center = Offset(x + (rnd() - 0.5f) * r, y - rnd() * r * 0.9f))
      }
    }
    tree(w * 0.05f, ground + h * 0.03f, w * 0.09f)
    tree(w * 0.17f, ground + h * 0.04f, w * 0.065f)
    tree(w * 0.83f, ground + h * 0.03f, w * 0.08f)
    tree(w * 0.96f, ground + h * 0.04f, w * 0.09f)
    // Sokak fenerleri
    listOf(0.32f to 0.07f, 0.68f to 0.07f, 0.12f to 0.14f, 0.88f to 0.14f).forEach { (fx, dy) ->
      val x = w * fx
      val b = ground + h * dy
      val top = b - h * 0.09f
      drawRect(Color(0xFF1A1414), topLeft = Offset(x - 1.5f, top), size = Size(3f, b - top))
      drawCircle(
        Brush.radialGradient(
          0f to Color(0xFFFFDC96), 0.25f to Color(0xFFFFBE6E).copy(alpha = 0.6f), 1f to Color.Transparent,
          center = Offset(x, top), radius = w * 0.05f
        ),
        radius = w * 0.05f, center = Offset(x, top)
      )
    }
  }
}

private fun floodBrush(wall: Color, top: Float, bottom: Float) =
  Brush.verticalGradient(0f to wall.shade(0.62f), 1f to wall.shade(1.12f), startY = top, endY = bottom)

private fun DrawScope.archWindow(x: Float, bottom: Float, ww: Float, wh: Float, color: Color) {
  val r = ww / 2
  drawPath(Path().apply {
    moveTo(x - r, bottom)
    lineTo(x - r, bottom - wh + r)
    arcTo(Rect(x - r, bottom - wh, x + r, bottom - wh + ww), 180f, 180f, false)
    lineTo(x + r, bottom)
    close()
  }, color)
}

/** Projektörle alttan aydınlatılmış kurşun kubbe. */
private fun DrawScope.dome(spec: MosqueSpec, h: Float, x: Float, y: Float, r: Float, hr: Float, ribs: Boolean, main: Boolean) {
  val rect = Rect(x - r, y - r * hr, x + r, y + r * hr)
  drawArc(
    Brush.verticalGradient(0f to spec.lead.shade(0.55f), 0.4f to spec.lead, 1f to spec.lead.shade(1.45f), startY = y - r * hr, endY = y),
    180f, 180f, true, topLeft = rect.topLeft, size = rect.size
  )
  drawArc(
    Brush.horizontalGradient(
      0f to Color.Black.copy(alpha = 0.25f), 0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.3f),
      startX = x - r, endX = x + r
    ),
    180f, 180f, true, topLeft = rect.topLeft, size = rect.size
  )
  if (ribs) {
    for (k in -3..3) {
      drawPath(Path().apply {
        moveTo(x + k * r / 4f, y)
        quadraticBezierTo(x + k * r / 7f, y - r * hr * 0.9f, x, y - r * hr)
      }, Color.Black.copy(alpha = 0.2f), style = Stroke(width = 1f))
    }
  }
  val top = y - r * hr
  drawLine(gold, Offset(x, top), Offset(x, top - if (main) h * 0.035f else h * 0.015f), strokeWidth = if (main) 2.5f else 1.5f)
  if (main) drawCircle(gold, radius = h * 0.007f, center = Offset(x, top - h * 0.04f))
}

/** Minare çizer ve tepe noktasını (mahya için) döndürür. */
private fun DrawScope.minaret(spec: MosqueSpec, w: Float, h: Float, ground: Float, x: Float, heightF: Float): Offset {
  val top = ground - h * 0.40f * heightF
  val mw = w * 0.022f
  drawRect(
    Brush.horizontalGradient(0f to spec.wall.shade(0.75f), 0.45f to spec.wall.shade(1.12f), 1f to spec.wall.shade(0.6f), startX = x - mw / 2, endX = x + mw / 2),
    topLeft = Offset(x - mw / 2, top), size = Size(mw, ground - top)
  )
  for (k in 1..spec.serefe) {
    val by = top + (ground - top) * (0.12f + 0.2f * (k - 1) + if (spec.serefe == 1) 0.15f else 0f)
    drawRect(spec.wall.shade(0.85f), topLeft = Offset(x - mw * 0.95f, by), size = Size(mw * 1.9f, h * 0.008f))
    // Şerefe ışıkları
    drawRect(warmLight.copy(alpha = 0.95f), topLeft = Offset(x - mw * 0.8f, by + h * 0.008f), size = Size(mw * 1.6f, h * 0.005f))
    drawCircle(
      Brush.radialGradient(0f to warmLight.copy(alpha = 0.45f), 1f to Color.Transparent, center = Offset(x, by), radius = mw * 2.2f),
      radius = mw * 2.2f, center = Offset(x, by)
    )
  }
  drawPath(Path().apply {
    moveTo(x - mw * 0.6f, top); lineTo(x, top - h * 0.085f); lineTo(x + mw * 0.6f, top); close()
  }, spec.lead.shade(0.6f))
  drawLine(gold, Offset(x, top - h * 0.085f), Offset(x, top - h * 0.1f), strokeWidth = 1.5f)
  return Offset(x, top)
}

private fun DrawScope.drawMosque(spec: MosqueSpec, w: Float, h: Float, ground: Float): List<Offset> {
  val cx = w * 0.5f
  val r0 = w * spec.domeR
  val bodyW = w * 0.62f
  val bodyH = h * 0.085f
  val bodyTop = ground - bodyH

  drawRect(floodBrush(spec.wall, bodyTop, ground), topLeft = Offset(cx - bodyW / 2, bodyTop), size = Size(bodyW, bodyH))
  val nW = if (spec.baroque) 7 else 11
  for (i in 0 until nW) {
    val x = cx - bodyW / 2 + bodyW * (i + 0.5f) / nW
    val ww = bodyW / nW * if (spec.baroque) 0.35f else 0.28f
    val wh = bodyH * if (spec.baroque) 0.55f else 0.34f
    archWindow(x, ground - bodyH * 0.25f, ww, wh, warmLight.copy(alpha = 0.9f))
  }
  if (spec.buttresses) {
    listOf(-0.52f, -0.36f, 0.36f, 0.52f).forEach { f ->
      val bx = cx + bodyW * f
      drawPath(Path().apply {
        moveTo(bx - w * 0.025f, ground); lineTo(bx - w * 0.012f, bodyTop - h * 0.05f)
        lineTo(bx + w * 0.012f, bodyTop - h * 0.05f); lineTo(bx + w * 0.025f, ground); close()
      }, spec.wall.shade(0.75f))
    }
  }
  for (i in 0 until spec.smallDomes) {
    dome(spec, h, cx - bodyW / 2 + bodyW * (i + 0.5f) / spec.smallDomes, bodyTop, w * 0.035f, 1f, ribs = false, main = false)
  }
  val tierTop = bodyTop - h * 0.04f
  if (spec.semiDomes >= 2) {
    drawRect(floodBrush(spec.wall, tierTop, bodyTop), topLeft = Offset(cx - r0 * 1.9f, tierTop), size = Size(r0 * 3.8f, bodyTop - tierTop + 1f))
    dome(spec, h, cx - r0 * 1.15f, tierTop, r0 * 0.62f, 0.85f, ribs = true, main = false)
    dome(spec, h, cx + r0 * 1.15f, tierTop, r0 * 0.62f, 0.85f, ribs = true, main = false)
  }
  if (spec.semiDomes >= 4) {
    dome(spec, h, cx - r0 * 1.75f, bodyTop, r0 * 0.38f, 0.85f, ribs = false, main = false)
    dome(spec, h, cx + r0 * 1.75f, bodyTop, r0 * 0.38f, 0.85f, ribs = false, main = false)
  }
  val drumH = h * 0.035f
  val drumTop = (if (spec.semiDomes > 0) tierTop else bodyTop) - drumH
  drawRect(floodBrush(spec.wall, drumTop, drumTop + drumH), topLeft = Offset(cx - r0 * 0.95f, drumTop), size = Size(r0 * 1.9f, drumH + 1f))
  for (i in 0 until 12) {
    archWindow(cx - r0 * 0.85f + r0 * 1.7f * (i + 0.5f) / 12f, drumTop + drumH * 0.8f, r0 * 0.07f, drumH * 0.5f, warmLight.copy(alpha = 0.85f))
  }
  dome(spec, h, cx, drumTop, r0, spec.domeHeight, ribs = true, main = true)
  return spec.minarets.map { (fx, hf) -> minaret(spec, w, h, ground, w * fx, hf) }
}
