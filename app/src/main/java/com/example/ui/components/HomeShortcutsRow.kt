package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ZikirStore
import com.example.i18n.t
import com.example.ui.theme.isAppInDarkTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ana sayfa kısayol satırı (Günün Manevi İklimi'nin altında):
 * [Yakındaki Camiler]  (Kıble yuvarlak düğmesi)  [Zikirmatik]
 * Tüm renkler seçili temadan gelir (Saray / İznik Çinisi / Zümrüt, aydınlık ve koyu).
 */
@Composable
fun HomeShortcutsRow(
  onOpenMosques: () -> Unit,
  onOpenQibla: () -> Unit,
  onOpenZikirmatik: () -> Unit,
  refreshKey: Any? = null,
  modifier: Modifier = Modifier
) {
  val cs = MaterialTheme.colorScheme
  val dark = isAppInDarkTheme()
  val context = LocalContext.current
  val zikirTotal = remember(refreshKey) { ZikirStore(context).totalAllTime }
  val iconBg = if (dark) cs.surfaceVariant else cs.secondaryContainer

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(124.dp)
      .padding(horizontal = 16.dp)
      .testTag("home_shortcuts_row"),
    contentAlignment = Alignment.Center
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      // 1) Yakındaki Camiler
      ShortcutBox(
        shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp, topEnd = 48.dp, bottomEnd = 48.dp),
        alignEnd = false,
        onClick = onOpenMosques,
        tag = "shortcut_mosques",
        modifier = Modifier.weight(1f)
      ) {
        IconChip(iconBg) {
          Icon(painterResource(R.drawable.ic_stat_mosque), contentDescription = null, tint = cs.secondary, modifier = Modifier.size(20.dp))
        }
        Text(t("Yakındaki\nCamiler"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, lineHeight = 16.sp)
        Text(t("Harita & yol tarifi"), fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1)
      }
      Spacer(modifier = Modifier.width(8.dp))
      // 3) Zikirmatik
      ShortcutBox(
        shape = RoundedCornerShape(topStart = 48.dp, bottomStart = 48.dp, topEnd = 22.dp, bottomEnd = 22.dp),
        alignEnd = true,
        onClick = onOpenZikirmatik,
        tag = "shortcut_zikirmatik",
        modifier = Modifier.weight(1f)
      ) {
        IconChip(iconBg) {
          Icon(painterResource(R.drawable.ic_tasbih), contentDescription = null, tint = cs.secondary, modifier = Modifier.size(20.dp))
        }
        Text(t("Zikirmatik"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        Text(
          buildAnnotatedString {
            append(t("Toplam") + " ")
            withStyle(SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) { append("$zikirTotal") }
          },
          fontSize = 11.sp,
          color = cs.onSurfaceVariant,
          textAlign = TextAlign.End
        )
      }
    }

    // 2) Ortada Kıble düğmesi
    Box(
      modifier = Modifier
        .size(124.dp)
        .clip(CircleShape)
        .background(cs.background),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(112.dp)
          .clip(CircleShape)
          .background(cs.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        val primary = cs.primary
        val secondary = cs.secondary
        Canvas(modifier = Modifier.fillMaxSize()) {
          val c = center
          val r = size.minDimension / 2f - 1.5.dp.toPx()
          drawCircle(primary, radius = r, center = c, style = Stroke(1.6.dp.toPx()))
          drawCircle(
            secondary.copy(alpha = 0.8f), radius = r - 5.dp.toPx(), center = c,
            style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx())))
          )
          for (k in 0 until 4) {
            val a = Math.PI / 2 * k - Math.PI / 2
            drawPath(starPath(Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()), 4.5.dp.toPx()), primary)
          }
        }
        Box(
          modifier = Modifier
            .size(86.dp)
            .shadow(if (dark) 10.dp else 8.dp, CircleShape, ambientColor = primary, spotColor = if (dark) Color.Black else primary)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(lerp(primary, Color.White, 0.38f), primary, lerp(primary, Color.Black, 0.18f)),
                center = Offset.Unspecified
              )
            )
            .clickable { onOpenQibla() }
            .testTag("shortcut_qibla"),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val onP = cs.onPrimary
            Canvas(modifier = Modifier.size(38.dp)) { drawQiblaCompass(onP, primary) }
            Text(
              text = t("KIBLE"),
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.4.sp,
              color = onP
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ShortcutBox(
  shape: RoundedCornerShape,
  alignEnd: Boolean,
  onClick: () -> Unit,
  tag: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val cs = MaterialTheme.colorScheme
  Box(
    modifier = modifier
      .height(96.dp)
      .clip(shape)
      .background(cs.surface)
      .border(1.dp, cs.outline, shape)
      .clickable { onClick() }
      .testTag(tag)
  ) {
    // Köşede çok soluk sekiz köşeli yıldız süsü
    val orn = cs.primary.copy(alpha = 0.10f)
    Canvas(
      modifier = Modifier
        .size(80.dp)
        .align(if (alignEnd) Alignment.BottomEnd else Alignment.BottomStart)
        .offset(x = if (alignEnd) 14.dp else (-14).dp, y = 16.dp)
    ) {
      drawPath(starPath(center, size.minDimension * 0.47f), orn, style = Stroke(3.dp.toPx() / 2))
      drawPath(starPath(center, size.minDimension * 0.27f), orn)
    }
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(start = if (alignEnd) 58.dp else 14.dp, end = if (alignEnd) 14.dp else 58.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) { content() }
  }
}

@Composable
private fun IconChip(bg: Color, content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .padding(bottom = 6.dp)
      .size(34.dp)
      .clip(RoundedCornerShape(11.dp))
      .background(bg),
    contentAlignment = Alignment.Center
  ) { content() }
}

/** Sekiz köşeli yıldız (iki karenin birleşimi). */
private fun starPath(c: Offset, r: Float, inner: Float = 0.6f): Path = Path().apply {
  for (k in 0 until 16) {
    val a = Math.PI / 8 * k - Math.PI / 2
    val rr = if (k % 2 == 0) r else r * inner
    val x = c.x + rr * cos(a).toFloat()
    val y = c.y + rr * sin(a).toFloat()
    if (k == 0) moveTo(x, y) else lineTo(x, y)
  }
  close()
}

/** Pusula halkası, kuzey iğnesi ve ortada Kâbe. */
private fun DrawScope.drawQiblaCompass(line: Color, kaabaFill: Color) {
  val c = center
  val r = size.minDimension / 2f - 1.dp.toPx()
  drawCircle(line, radius = r, center = c, style = Stroke(1.5.dp.toPx()))
  for (k in 0 until 8) {
    val a = Math.PI / 4 * k
    drawLine(
      line,
      Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()),
      Offset(c.x + (r - 3.dp.toPx()) * cos(a).toFloat(), c.y + (r - 3.dp.toPx()) * sin(a).toFloat()),
      strokeWidth = 1.3.dp.toPx()
    )
  }
  val n = r * 0.86f
  val w = r * 0.2f
  val needle = Path().apply {
    moveTo(c.x, c.y - n); lineTo(c.x + w, c.y); lineTo(c.x, c.y + n); lineTo(c.x - w, c.y); close()
  }
  drawPath(needle, line.copy(alpha = 0.3f))
  drawPath(Path().apply { moveTo(c.x, c.y - n); lineTo(c.x + w, c.y); lineTo(c.x - w, c.y); close() }, line)
  val k = r * 0.26f
  drawRect(kaabaFill, topLeft = Offset(c.x - k, c.y - k), size = androidx.compose.ui.geometry.Size(2 * k, 2 * k))
  drawRect(line, topLeft = Offset(c.x - k, c.y - k), size = androidx.compose.ui.geometry.Size(2 * k, 2 * k), style = Stroke(1.dp.toPx()))
  drawRect(line, topLeft = Offset(c.x - k, c.y - k * 0.55f), size = androidx.compose.ui.geometry.Size(2 * k, k * 0.32f))
}
