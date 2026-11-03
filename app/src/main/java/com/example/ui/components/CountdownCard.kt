package com.example.ui.components

import com.example.i18n.t
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CountdownMode
import com.example.data.CountdownResult
import com.example.data.PrayerTimeUtils

@Composable
fun CountdownCard(
  countdown: CountdownResult,
  city: String,
  district: String,
  onFireCannon: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isIftarCountdown = countdown.title.contains("İftar", ignoreCase = true)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("smart_countdown_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header tag
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer)
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.WbTwilight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "$city / $district",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title: "İftara Kalan Vakit" or "İmsaka Kalan Vakit"
      AnimatedContent(
        targetState = countdown.title,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "countdown_title_anim"
      ) { title ->
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Digital Clock Display (HH : MM : SS)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        TimeUnitBox(
          value = String.format("%02d", countdown.hours),
          label = "Saat"
        )
        Text(
          text = ":",
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        TimeUnitBox(
          value = String.format("%02d", countdown.minutes),
          label = "Dakika"
        )
        Text(
          text = ":",
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        TimeUnitBox(
          value = String.format("%02d", countdown.seconds),
          label = "Saniye"
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Target Prayer info chip
      Text(
        text = "${countdown.targetTimeName} Vakti: ${countdown.targetTimeString}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { countdown.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Cannon trigger button
      OutlinedButton(
        onClick = onFireCannon,
        modifier = Modifier.testTag("fire_cannon_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Text(t("💥 Ramazan Topunu Test Et"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun TimeUnitBox(
  value: String,
  label: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Text(
      text = value,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

/**
 * Ana sayfanın üst satırında, cami kartının yanında yarım genişlikte duran kompakt geri sayım.
 */
@Composable
fun CompactCountdownCard(
  countdown: CountdownResult,
  onFireCannon: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.testTag("smart_countdown_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      AnimatedContent(
        targetState = countdown.title,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "compact_countdown_title_anim"
      ) { title ->
        Text(
          text = t(title),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          maxLines = 2
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        MiniTimeUnitBox(String.format("%02d", countdown.hours), "Saat", Modifier.weight(1f))
        CompactColon()
        MiniTimeUnitBox(String.format("%02d", countdown.minutes), "Dakika", Modifier.weight(1f))
        CompactColon()
        MiniTimeUnitBox(String.format("%02d", countdown.seconds), "Saniye", Modifier.weight(1f))
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = t(countdown.targetTimeName),
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
        Text(
          text = countdown.targetTimeString,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      LinearProgressIndicator(
        progress = { countdown.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      if (countdown.mode == CountdownMode.IFTAR) {
        OutlinedButton(
          onClick = onFireCannon,
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .testTag("fire_cannon_button"),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
          Text(t("💥 Topu Test Et"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
      } else {
        // Sahur / Teravih / Namaz modunda: hedeften sonraki vakit
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = t("Sonra:") + " " + t(countdown.nextLabel),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
          )
        }
      }
    }
  }
}

@Composable
private fun CompactColon() {
  Text(
    text = ":",
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 2.dp)
  )
}

@Composable
private fun MiniTimeUnitBox(value: String, label: String, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(vertical = 6.dp)
  ) {
    Text(
      text = value,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = label,
      fontSize = 8.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1
    )
  }
}
