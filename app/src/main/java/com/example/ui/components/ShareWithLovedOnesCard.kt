package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GreetingOccasion
import com.example.i18n.AppLanguage
import com.example.i18n.t

/** Ana sayfada namaz vakitlerinin üstünde: "Sevdiklerinizle Paylaşın" özel gün kartları girişi. */
@Composable
fun ShareWithLovedOnesCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
  val today = remember { GreetingOccasion.forToday() }
  val gold = Color(0xFFE5B869)
  val subtitle = when {
    today != null && AppLanguage.isEnglish -> "Today: ${today.label.en} • Your card is ready"
    today != null -> "Bugün ${today.label.tr} • Kartınız hazır"
    else -> t("Kandil, bayram ve cuma kartları")
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(Brush.verticalGradient(listOf(Color(0xFF0C1424), Color(0xFF04070E))))
      .border(1.dp, gold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("share_loved_ones_card")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Küçük altın kemer simgesi
      Canvas(modifier = Modifier.size(width = 34.dp, height = 44.dp)) {
        val w = size.width; val h = size.height
        val arch = Path().apply {
          moveTo(0f, h); lineTo(0f, h * 0.42f)
          cubicTo(0f, h * 0.18f, w * 0.34f, h * 0.08f, w / 2f, 0f)
          cubicTo(w * 0.66f, h * 0.08f, w, h * 0.18f, w, h * 0.42f)
          lineTo(w, h); close()
        }
        drawPath(arch, gold, style = Stroke(width = 2.5f))
        drawLine(gold, Offset(w / 2f, h * 0.12f), Offset(w / 2f, h * 0.38f), strokeWidth = 2f)
        drawCircle(Color(0xFFFFD27A), radius = w * 0.13f, center = Offset(w / 2f, h * 0.5f))
        drawCircle(Color(0x55FFD27A), radius = w * 0.28f, center = Offset(w / 2f, h * 0.5f))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = t("Sevdiklerinizle Paylaşın"),
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = Color(0xFFF3EEE2)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 12.sp, color = gold)
      }
      Text(text = "›", fontSize = 28.sp, color = gold, fontWeight = FontWeight.Bold)
    }
  }
}
