package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.sin

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
    Color(0xFFB9AB98), Color(0xFF7D858C))
  val ayasofya = MosqueSpec(0.21f, 0.62f, 2, 0,
    listOf(0.08f to 0.88f, 0.20f to 0.78f, 0.80f to 0.78f, 0.92f to 0.88f), 2,
    Color(0xFFC98D6A), Color(0xFF868C90), buttresses = true)
  val suleymaniye = MosqueSpec(0.18f, 0.9f, 2, 6,
    listOf(0.07f to 0.92f, 0.19f to 0.78f, 0.81f to 0.78f, 0.93f to 0.92f), 3,
    Color(0xFFC2B6A4), Color(0xFF7B838A))
  val camlica = MosqueSpec(0.19f, 0.95f, 4, 4,
    listOf(0.05f to 0.88f, 0.14f to 0.80f, 0.24f to 0.74f, 0.76f to 0.74f, 0.86f to 0.80f, 0.95f to 0.88f), 3,
    Color(0xFFD8D2C8), Color(0xFF8A939A), hill = true)
  val eyup = MosqueSpec(0.15f, 0.95f, 0, 3,
    listOf(0.20f to 0.82f, 0.80f to 0.82f), 2,
    Color(0xFFD6CDBD), Color(0xFF7F878E))
  val ortakoy = MosqueSpec(0.15f, 1.05f, 0, 0,
    listOf(0.20f to 0.76f, 0.80f to 0.76f), 1,
    Color(0xFFE3DCCD), Color(0xFF8B939A), bridge = true, baroque = true)
  val fatih = MosqueSpec(0.18f, 0.95f, 4, 2,
    listOf(0.12f to 0.84f, 0.88f to 0.84f), 2,
    Color(0xFFC4B8A6), Color(0xFF7B838A))
}

private fun Color.shade(f: Float): Color =
  if (f >= 1f) lerp(this, Color.White, (f - 1f).coerceIn(0f, 1f)) else lerp(Color.Black, this, f.coerceIn(0f, 1f))

private val warmLight = Color(0xFFFFD282)
private val gold = Color(0xFFE8C070)

/** Gün batımında, Boğaz kıyısında bir İstanbul camisi; suya yansımasıyla birlikte. */
@Composable
fun IstanbulMosqueScene(spec: MosqueSpec, glow: Float, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val base = h * 0.58f
    val sunX = w * 0.32f
    val sunY = base - h * 0.02f

    // Gökyüzü
    drawRect(
      Brush.verticalGradient(
        0f to Color(0xFF1D2747), 0.45f to Color(0xFF5A4466), 0.78f to Color(0xFFC9784A), 1f to Color(0xFFF0B56A),
        startY = 0f, endY = base
      ),
      size = Size(w, base)
    )
    // Güneş ışıltısı ve güneş
    drawRect(
      Brush.radialGradient(
        0f to Color(0xFFFFD68C).copy(alpha = 0.85f * glow), 0.12f to Color(0xFFFFBE6E).copy(alpha = 0.45f * glow),
        1f to Color.Transparent, center = Offset(sunX, sunY), radius = w * 0.7f
      ),
      size = Size(w, base)
    )
    drawCircle(Color(0xFFFFECBE).copy(alpha = 0.95f), radius = w * 0.045f, center = Offset(sunX, sunY))
    // İnce bulutlar
    listOf(Triple(0.2f, 0.18f, 0.22f), Triple(0.7f, 0.12f, 0.28f), Triple(0.55f, 0.30f, 0.2f)).forEach { (x, y, r) ->
      drawOval(Color(0xFFFFC8AA).copy(alpha = 0.18f), topLeft = Offset(w * x - w * r, h * y - h * 0.018f), size = Size(w * r * 2, h * 0.036f))
    }
    // Yıldızlar ve hilal
    listOf(0.1f to 0.05f, 0.45f to 0.04f, 0.62f to 0.09f, 0.9f to 0.16f, 0.3f to 0.12f).forEach { (x, y) ->
      drawCircle(Color.White.copy(alpha = 0.8f), radius = w * 0.004f, center = Offset(w * x, h * y))
    }
    drawCircle(Color(0xFFFFE7AE), radius = w * 0.05f, center = Offset(w * 0.82f, h * 0.10f))
    drawCircle(Color(0xFF232C4C), radius = w * 0.044f, center = Offset(w * 0.84f, h * 0.09f))

    // Uzak şehir silüeti (pus)
    val haze = Color(0xFF463246).copy(alpha = 0.45f)
    var seed = 7
    fun rnd(): Float { seed = (seed * 9301 + 49297) % 233280; return seed / 233280f }
    val skyline = Path().apply {
      moveTo(0f, base)
      var x = 0f
      while (x <= w) { lineTo(x, base - h * (0.01f + rnd() * 0.025f)); x += w / 40f }
      lineTo(w, base); close()
    }
    drawPath(skyline, haze)
    repeat(5) {
      val x = w * rnd()
      val r = w * (0.02f + rnd() * 0.02f)
      drawArc(haze, 180f, 180f, true, topLeft = Offset(x - r, base - h * 0.02f - r), size = Size(r * 2, r * 2))
      drawRect(haze, topLeft = Offset(x + r * 1.6f, base - h * 0.09f), size = Size(w * 0.005f, h * 0.07f))
    }
    if (spec.hill) {
      drawPath(Path().apply {
        moveTo(0f, base); quadraticBezierTo(w * 0.5f, base - h * 0.1f, w, base); close()
      }, Color(0xFF2A2230))
    }
    if (spec.bridge) drawBridge(w, h, base)

    val ground = if (spec.hill) base - h * 0.075f else base
    drawMosque(spec, w, h, ground)

    // Su (Boğaz)
    drawRect(
      Brush.verticalGradient(0f to Color(0xFF7A5446), 0.4f to Color(0xFF3B3448), 1f to Color(0xFF141826), startY = base, endY = h),
      topLeft = Offset(0f, base), size = Size(w, h - base)
    )
    // Yansıma
    clipRect(0f, base, w, h) {
      drawIntoCanvas { canvas ->
        canvas.saveLayer(Rect(0f, base, w, h), Paint().apply { alpha = 0.28f })
        scale(1f, -1f, pivot = Offset(0f, base)) {
          drawMosque(spec, w, h, ground)
        }
        canvas.restore()
      }
    }
    // Güneşin sudaki yolu
    for (i in 0 until 14) {
      val y = base + (h - base) * i / 14f + 3f
      val ww = w * (0.18f - 0.008f * i)
      drawRect(
        Color(0xFFFFC878).copy(alpha = (0.35f - 0.022f * i).coerceAtLeast(0f)),
        topLeft = Offset(sunX - ww / 2 + if (i % 2 == 0) -4f else 4f, y), size = Size(ww, h * 0.0026f)
      )
    }
  }
}

private fun DrawScope.drawBridge(w: Float, h: Float, base: Float) {
  val c = Color(0xFF281E32).copy(alpha = 0.85f)
  drawPath(Path().apply {
    moveTo(0f, base - h * 0.13f); quadraticBezierTo(w * 0.5f, base - h * 0.02f, w, base - h * 0.13f)
  }, c, style = Stroke(width = w * 0.006f))
  drawRect(c, topLeft = Offset(w * 0.02f, base - h * 0.26f), size = Size(w * 0.03f, h * 0.26f))
  drawRect(c, topLeft = Offset(w * 0.95f, base - h * 0.26f), size = Size(w * 0.03f, h * 0.26f))
  for (i in 1 until 10) {
    val x = w * i / 10f
    drawLine(c, Offset(x, base - h * (0.13f - 0.11f * sin(PI * i / 10).toFloat())), Offset(x, base - h * 0.1f), strokeWidth = 1.5f)
  }
}

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

private fun DrawScope.dome(spec: MosqueSpec, h: Float, x: Float, y: Float, r: Float, hr: Float, ribs: Boolean, main: Boolean) {
  val rect = Rect(x - r, y - r * hr, x + r, y + r * hr)
  drawArc(
    Brush.radialGradient(
      0f to spec.lead.shade(1.55f), 0.35f to spec.lead, 1f to spec.lead.shade(0.38f),
      center = Offset(x - r * 0.45f, y - r * hr * 0.7f), radius = r * 1.3f
    ),
    180f, 180f, true, topLeft = rect.topLeft, size = rect.size
  )
  // Batan güneşin sol yandan vuran ılık ışığı
  drawArc(
    Brush.horizontalGradient(0f to Color(0xFFFFAA5A).copy(alpha = 0.35f), 0.5f to Color.Transparent, startX = x - r, endX = x + r),
    180f, 180f, true, topLeft = rect.topLeft, size = rect.size
  )
  if (ribs) {
    for (k in -3..3) {
      drawPath(Path().apply {
        moveTo(x + k * r / 4f, y)
        quadraticBezierTo(x + k * r / 7f, y - r * hr * 0.9f, x, y - r * hr)
      }, Color.Black.copy(alpha = 0.18f), style = Stroke(width = 1f))
    }
  }
  val top = y - r * hr
  drawLine(gold, Offset(x, top), Offset(x, top - if (main) h * 0.035f else h * 0.015f), strokeWidth = if (main) 2.5f else 1.5f)
  if (main) drawCircle(gold, radius = h * 0.007f, center = Offset(x, top - h * 0.04f))
}

private fun DrawScope.minaret(spec: MosqueSpec, w: Float, h: Float, ground: Float, x: Float, heightF: Float) {
  val top = ground - h * 0.40f * heightF
  val mw = w * 0.022f
  drawRect(
    Brush.horizontalGradient(0f to spec.wall.shade(1.15f), 0.4f to spec.wall.shade(0.95f), 1f to spec.wall.shade(0.45f), startX = x - mw / 2, endX = x + mw / 2),
    topLeft = Offset(x - mw / 2, top), size = Size(mw, ground - top)
  )
  drawRect(spec.wall.shade(0.8f), topLeft = Offset(x - mw * 0.75f, ground - h * 0.05f), size = Size(mw * 1.5f, h * 0.05f))
  for (k in 1..spec.serefe) {
    val by = top + (ground - top) * (0.12f + 0.2f * (k - 1) + if (spec.serefe == 1) 0.15f else 0f)
    drawRect(spec.wall.shade(0.9f), topLeft = Offset(x - mw * 0.95f, by), size = Size(mw * 1.9f, h * 0.008f))
    drawRect(Color.Black.copy(alpha = 0.35f), topLeft = Offset(x - mw * 0.8f, by + h * 0.008f), size = Size(mw * 1.6f, h * 0.006f))
  }
  drawPath(Path().apply {
    moveTo(x - mw * 0.6f, top); lineTo(x, top - h * 0.085f); lineTo(x + mw * 0.6f, top); close()
  }, Brush.horizontalGradient(0f to spec.lead.shade(1.4f), 1f to spec.lead.shade(0.4f), startX = x - mw / 2, endX = x + mw / 2))
  drawLine(gold, Offset(x, top - h * 0.085f), Offset(x, top - h * 0.1f), strokeWidth = 1.5f)
}

private fun DrawScope.drawMosque(spec: MosqueSpec, w: Float, h: Float, ground: Float) {
  val cx = w * 0.5f
  val r0 = w * spec.domeR
  val bodyW = w * 0.62f
  val bodyH = h * 0.085f
  val bodyTop = ground - bodyH
  val wallBrush = Brush.verticalGradient(0f to spec.wall, 1f to spec.wall.shade(0.55f), startY = bodyTop, endY = ground)

  // Gövde + gün batımı tonu
  drawRect(wallBrush, topLeft = Offset(cx - bodyW / 2, bodyTop), size = Size(bodyW, bodyH))
  drawRect(
    Brush.horizontalGradient(0f to Color(0xFFFFAA5A).copy(alpha = 0.35f), 1f to Color(0xFF1E1428).copy(alpha = 0.35f), startX = cx - bodyW / 2, endX = cx + bodyW / 2),
    topLeft = Offset(cx - bodyW / 2, bodyTop), size = Size(bodyW, bodyH)
  )
  // Pencere sıraları (içeriden yanan ışık)
  val nW = if (spec.baroque) 7 else 11
  for (i in 0 until nW) {
    val x = cx - bodyW / 2 + bodyW * (i + 0.5f) / nW
    val ww = bodyW / nW * if (spec.baroque) 0.35f else 0.28f
    val wh = bodyH * if (spec.baroque) 0.55f else 0.32f
    archWindow(x, ground - bodyH * 0.25f, ww, wh, warmLight.copy(alpha = 0.9f))
  }
  if (spec.buttresses) {
    listOf(-0.52f, -0.36f, 0.36f, 0.52f).forEach { f ->
      val bx = cx + bodyW * f
      drawPath(Path().apply {
        moveTo(bx - w * 0.025f, ground); lineTo(bx - w * 0.012f, bodyTop - h * 0.05f)
        lineTo(bx + w * 0.012f, bodyTop - h * 0.05f); lineTo(bx + w * 0.025f, ground); close()
      }, spec.wall.shade(0.7f))
    }
  }
  for (i in 0 until spec.smallDomes) {
    dome(spec, h, cx - bodyW / 2 + bodyW * (i + 0.5f) / spec.smallDomes, bodyTop, w * 0.035f, 1f, ribs = false, main = false)
  }
  // Yarım kubbeler (kademeli kubbe dizilimi)
  val tierTop = bodyTop - h * 0.04f
  if (spec.semiDomes >= 2) {
    drawRect(wallBrush, topLeft = Offset(cx - r0 * 1.9f, tierTop), size = Size(r0 * 3.8f, bodyTop - tierTop + 1f))
    dome(spec, h, cx - r0 * 1.15f, tierTop, r0 * 0.62f, 0.85f, ribs = true, main = false)
    dome(spec, h, cx + r0 * 1.15f, tierTop, r0 * 0.62f, 0.85f, ribs = true, main = false)
  }
  if (spec.semiDomes >= 4) {
    dome(spec, h, cx - r0 * 1.75f, bodyTop, r0 * 0.38f, 0.85f, ribs = false, main = false)
    dome(spec, h, cx + r0 * 1.75f, bodyTop, r0 * 0.38f, 0.85f, ribs = false, main = false)
  }
  // Kasnak (pencereli) ve ana kubbe
  val drumH = h * 0.035f
  val drumTop = (if (spec.semiDomes > 0) tierTop else bodyTop) - drumH
  drawRect(
    Brush.horizontalGradient(0f to spec.wall.shade(1.1f), 1f to spec.wall.shade(0.55f), startX = cx - r0, endX = cx + r0),
    topLeft = Offset(cx - r0 * 0.95f, drumTop), size = Size(r0 * 1.9f, drumH + 1f)
  )
  for (i in 0 until 12) {
    archWindow(cx - r0 * 0.85f + r0 * 1.7f * (i + 0.5f) / 12f, drumTop + drumH * 0.8f, r0 * 0.07f, drumH * 0.5f, warmLight.copy(alpha = 0.85f))
  }
  dome(spec, h, cx, drumTop, r0, spec.domeHeight, ribs = true, main = true)
  // Minareler
  spec.minarets.forEach { (fx, hf) -> minaret(spec, w, h, ground, w * fx, hf) }
}
