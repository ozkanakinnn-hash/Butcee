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
  val preview = today ?: GreetingOccasion.CUMA
  val accent = MaterialTheme.colorScheme.primary
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
      .background(
        Brush.horizontalGradient(
          listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
        )
      )
      .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("share_loved_ones_card")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Bugünün kartından küçük önizleme
      androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(preview.bgRes),
        contentDescription = null,
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        modifier = Modifier
          .size(width = 40.dp, height = 64.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
      )
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = t("Sevdiklerinizle Paylaşın"),
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 12.sp, color = accent)
      }
      Text(text = "›", fontSize = 28.sp, color = accent, fontWeight = FontWeight.Bold)
    }
  }
}
