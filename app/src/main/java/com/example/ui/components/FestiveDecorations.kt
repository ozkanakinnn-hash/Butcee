package com.example.ui.components

import com.example.i18n.t
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Festival
import kotlin.math.sin

private fun palette(f: Festival) = when (f) {
  Festival.RAMAZAN_BAYRAMI -> listOf(Color(0xFFD4A537), Color(0xFFB3282D), Color(0xFF1F6B4A), Color(0xFFE8C468))
  Festival.KURBAN_BAYRAMI -> listOf(Color(0xFFD4A537), Color(0xFF1A6E7A), Color(0xFF8E3B5A), Color(0xFFE8C468))
}

/** Ekranın üstünde sallanan bayram süsü: renkli flama dizisi ve asılı fenerler. */
@Composable
fun FestiveGarland(festival: Festival, modifier: Modifier = Modifier) {
  val colors = palette(festival)
  val t = rememberInfiniteTransition(label = "garland")
  val sway by t.animateFloat(
    initialValue = -1f, targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway"
  )
  val twinkle by t.animateFloat(
    initialValue = 0f, targetValue = 6.283f,
    animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "twinkle"
  )
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(70.dp)
      .testTag("festive_garland")
  ) {
    val w = size.width
    val sag = size.height * 0.28f
    fun ropeY(x: Float) = 4f + sag * 4f * (x / w) * (1f - x / w)

    // İp
    drawPath(Path().apply {
      moveTo(0f, ropeY(0f))
      var x = 0f
      while (x <= w) { lineTo(x, ropeY(x)); x += w / 40f }
    }, Color(0xFFB08A4A), style = Stroke(width = 2f))

    // Üçgen flamalar
    val flagCount = 14
    val flagW = w / flagCount
    for (i in 0 until flagCount) {
      val x0 = i * flagW + flagW * 0.08f
      val x1 = (i + 1) * flagW - flagW * 0.08f
      val mid = (x0 + x1) / 2f
      val drop = flagW * 0.75f
      val swing = sway * 2.5f * if (i % 2 == 0) 1f else -1f
      drawPath(Path().apply {
        moveTo(x0, ropeY(x0)); lineTo(x1, ropeY(x1)); lineTo(mid + swing, ropeY(mid) + drop); close()
      }, colors[i % 3])
      // Flama üzerinde küçük altın nokta
      drawCircle(colors[3], radius = flagW * 0.07f, center = Offset(mid + swing * 0.5f, ropeY(mid) + drop * 0.35f))
    }

    // Asılı fenerler
    listOf(0.18f, 0.5f, 0.82f).forEachIndexed { k, fx ->
      val x = w * fx
      val top = ropeY(x)
      val len = size.height * (0.38f + 0.08f * k % 2)
      rotate(degrees = sway * 6f * (if (k == 1) -1f else 1f), pivot = Offset(x, top)) {
        drawLine(Color(0xFFB08A4A), Offset(x, top), Offset(x, top + len * 0.45f), strokeWidth = 1.5f)
        val cy = top + len * 0.45f
        val lw = size.height * 0.16f
        val lh = size.height * 0.30f
        // Fener gövdesi (ışıldayan)
        drawCircle(
          Brush.radialGradient(0f to Color(0xFFFFE6A0).copy(alpha = 0.55f + 0.25f * sin(twinkle + k)), 1f to Color.Transparent,
            center = Offset(x, cy + lh / 2), radius = lh),
          radius = lh, center = Offset(x, cy + lh / 2)
        )
        drawPath(Path().apply {
          moveTo(x - lw * 0.35f, cy); lineTo(x + lw * 0.35f, cy)
          lineTo(x + lw * 0.5f, cy + lh * 0.3f); lineTo(x + lw * 0.4f, cy + lh * 0.85f)
          lineTo(x - lw * 0.4f, cy + lh * 0.85f); lineTo(x - lw * 0.5f, cy + lh * 0.3f); close()
        }, Color(0xFFFFD27A))
        drawRect(colors[1], topLeft = Offset(x - lw * 0.4f, cy - lh * 0.1f), size = Size(lw * 0.8f, lh * 0.12f))
        drawRect(colors[1], topLeft = Offset(x - lw * 0.32f, cy + lh * 0.85f), size = Size(lw * 0.64f, lh * 0.1f))
        drawLine(colors[0], Offset(x, cy + lh * 0.95f), Offset(x, cy + lh * 1.15f), strokeWidth = 2f)
      }
    }
  }
}

/** Bayram tebrik kartı: parıltılı arka plan üzerinde bayram mesajı. */
@Composable
fun FestiveGreetingCard(festival: Festival, name: String, modifier: Modifier = Modifier) {
  val colors = palette(festival)
  val t = rememberInfiniteTransition(label = "sparkle")
  val phase by t.animateFloat(
    initialValue = 0f, targetValue = 6.283f,
    animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "phase"
  )
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("festive_greeting_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(listOf(colors[1], colors[2]))),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.matchParentSize()) {
        // Parıltılar
        for (i in 0 until 18) {
          val x = size.width * ((i * 0.137f + 0.05f) % 1f)
          val y = size.height * ((i * 0.293f + 0.1f) % 1f)
          val a = 0.25f + 0.6f * ((sin(phase + i) + 1f) / 2f)
          drawCircle(colors[3].copy(alpha = a), radius = 1.5f + (i % 3), center = Offset(x, y))
        }
      }
      Column(
        modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(festival.emoji + "  " + festival.emoji + "  " + festival.emoji, fontSize = 20.sp)
        Text(
          text = t(festival.greeting),
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFF4D6),
          textAlign = TextAlign.Center
        )
        Text(
          text = if (com.example.i18n.AppLanguage.isEnglish) "Dear $name, may your Eid be blessed." else "Sevgili $name, bayramınız hayırlara vesile olsun.",
          fontSize = 12.sp,
          color = Color(0xFFFFF4D6).copy(alpha = 0.85f),
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
