package akinai.minikgurme.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.ui.theme.CardSurface
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.util.ServeShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Tabakta servis şekli çizimi (vektör, tema rengine uyar) + altında kısa etiket. */
@Composable
fun ServeShapeIcon(shape: ServeShape, food: Color, plateBorder: Color, size: Dp = 56.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.width(size + 12.dp).testTag("serve_${shape.name}")) {
        val plate = CardSurface
        Canvas(Modifier.size(size)) { drawPlate(plate, plateBorder); drawShape(shape, food) }
        Text(shape.label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, lineHeight = 11.sp, color = TextSecondary, textAlign = TextAlign.Center, maxLines = 2)
    }
}

private fun DrawScope.drawPlate(plate: Color, border: Color) {
    val r = size.minDimension / 2
    drawCircle(plate, r)
    drawCircle(border, r - 1.5f, style = Stroke(2.5f))
    drawCircle(border.copy(alpha = 0.35f), r * 0.78f, style = Stroke(1.5f))
}

private fun DrawScope.drawShape(shape: ServeShape, c: Color) {
    val s = size.minDimension
    val cx = size.width / 2; val cy = size.height / 2
    val light = c.copy(alpha = 0.55f)
    when (shape) {
        ServeShape.MASH -> {
            val p = Path()
            val n = 14
            for (i in 0..n) {
                val a = 2 * PI * i / n
                val rr = s * (0.22 + 0.025 * sin(a * 3))
                val x = cx + (rr * cos(a)).toFloat(); val y = cy + (rr * 0.78 * sin(a)).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close(); drawPath(p, light)
            // çatal izleri
            for (k in -1..1) drawLine(c, Offset(cx - s * 0.13f, cy + k * s * 0.06f), Offset(cx + s * 0.13f, cy + k * s * 0.06f - s * 0.03f), strokeWidth = s * 0.025f)
        }
        ServeShape.STICK -> {
            // üç paralel parmak şerit, grup hâlinde hafif eğik
            val w = s * 0.1f; val h = s * 0.46f
            rotate(-25f, Offset(cx, cy)) {
                listOf(-1, 0, 1).forEach { k ->
                    drawRoundRect(if (k == 0) light else c, Offset(cx - w / 2 + k * s * 0.14f, cy - h / 2), Size(w, h), CornerRadius(w / 2))
                }
            }
        }
        ServeShape.BITES -> {
            val b = s * 0.1f; val g = s * 0.05f
            for (i in -1..1) for (j in -1..1) if ((i + j) % 2 == 0 || i == 0)
                drawRoundRect(c, Offset(cx + i * (b + g) - b / 2, cy + j * (b + g) - b / 2), Size(b, b), CornerRadius(b * 0.3f))
        }
        ServeShape.SHRED -> {
            for (k in 0 until 6) {
                val y0 = cy - s * 0.18f + k * s * 0.07f
                val p = Path(); p.moveTo(cx - s * 0.2f, y0)
                p.cubicTo(cx - s * 0.08f, y0 - s * 0.05f, cx + s * 0.05f, y0 + s * 0.05f, cx + s * 0.2f, y0 - s * 0.01f * k)
                drawPath(p, if (k % 2 == 0) c else light, style = Stroke(s * 0.03f))
            }
        }
        ServeShape.QUARTER -> {
            val r = s * 0.24f; val gap = s * 0.035f
            listOf(0f, 90f, 180f, 270f).forEachIndexed { i, start ->
                val mid = (start + 45f) * PI.toFloat() / 180f
                val off = Offset(cos(mid) * gap, sin(mid) * gap)
                drawArc(if (i % 2 == 0) c else light, start, 90f, true, Offset(cx - r + off.x, cy - r + off.y), Size(2 * r, 2 * r))
            }
        }
        ServeShape.POWDER -> {
            val p = Path(); p.moveTo(cx - s * 0.24f, cy + s * 0.1f)
            p.quadraticTo(cx, cy - s * 0.22f, cx + s * 0.24f, cy + s * 0.1f); p.close()
            drawPath(p, light)
            for (k in 0 until 18) {
                val a = k * 2.399f; val rr = s * 0.03f * kotlin.math.sqrt(k.toFloat())
                drawCircle(c, s * 0.012f, Offset(cx + cos(a) * rr * 1.6f, cy - s * 0.02f + sin(a) * rr * 0.6f))
            }
        }
        ServeShape.SPREAD -> {
            val w = s * 0.46f; val h = s * 0.4f
            drawRoundRect(Color(0xFFE2B77A), Offset(cx - w / 2, cy - h / 2), Size(w, h), CornerRadius(s * 0.08f))
            drawRoundRect(c, Offset(cx - w / 2 + s * 0.04f, cy - h / 2 + s * 0.04f), Size(w - s * 0.08f, h - s * 0.08f), CornerRadius(s * 0.05f))
        }
        ServeShape.MINCED -> {
            for (k in 0 until 22) {
                val a = k * 2.399f; val rr = s * 0.04f * kotlin.math.sqrt(k.toFloat())
                val o = Offset(cx + cos(a) * rr, cy + sin(a) * rr * 0.8f)
                drawRect(if (k % 3 == 0) light else c, Offset(o.x - s * 0.018f, o.y - s * 0.018f), Size(s * 0.036f, s * 0.036f))
            }
        }
        ServeShape.DISH -> {
            // kase + içinde parçalar
            drawArc(light, 0f, 180f, true, Offset(cx - s * 0.26f, cy - s * 0.2f), Size(s * 0.52f, s * 0.44f))
            drawLine(c, Offset(cx - s * 0.27f, cy + s * 0.02f), Offset(cx + s * 0.27f, cy + s * 0.02f), strokeWidth = s * 0.03f)
            listOf(-0.12f to -0.03f, 0.0f to -0.06f, 0.12f to -0.03f).forEach { (dx, dy) -> drawCircle(c, s * 0.045f, Offset(cx + dx * s, cy + dy * s)) }
        }
        ServeShape.SPOON -> {
            rotate(-35f, Offset(cx, cy)) {
                drawOval(c, Offset(cx - s * 0.1f, cy - s * 0.28f), Size(s * 0.2f, s * 0.26f))
                drawRoundRect(c, Offset(cx - s * 0.025f, cy - s * 0.04f), Size(s * 0.05f, s * 0.32f), CornerRadius(s * 0.025f))
                drawOval(light, Offset(cx - s * 0.065f, cy - s * 0.24f), Size(s * 0.13f, s * 0.17f))
            }
        }
        ServeShape.PATTY -> {
            listOf(-1, 1).forEach { k ->
                val o = Offset(cx + k * s * 0.12f - s * 0.13f, cy + k * s * 0.05f - s * 0.09f)
                drawOval(c, o, Size(s * 0.26f, s * 0.18f))
                drawCircle(light, s * 0.015f, Offset(o.x + s * 0.08f, o.y + s * 0.07f))
                drawCircle(light, s * 0.015f, Offset(o.x + s * 0.16f, o.y + s * 0.1f))
            }
        }
    }
}
