package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import java.util.Calendar

private data class PrayerItem(
  val name: String,
  val time: String,
  val icon: ImageVector,
  val isIftar: Boolean = false
)

@Composable
fun PrayerTimesCard(
  prayerTimes: PrayerTimes,
  modifier: Modifier = Modifier
) {
  val now = Calendar.getInstance()
  val currentMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

  val imsakMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.imsak)
  val gunesMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.gunes)
  val ogleMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.ogle)
  val ikindiMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.ikindi)
  val aksamMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.aksam)
  val yatsiMin = PrayerTimeUtils.parseTimeToMinutes(prayerTimes.yatsi)

  val activeIndex = when {
    currentMin in imsakMin until gunesMin -> 0 // Imsak
    currentMin in gunesMin until ogleMin -> 1  // Güneş
    currentMin in ogleMin until ikindiMin -> 2 // Öğle
    currentMin in ikindiMin until aksamMin -> 3 // İkindi
    currentMin in aksamMin until yatsiMin -> 4  // Akşam (İftar)
    else -> 5                                   // Yatsı
  }

  val items = listOf(
    PrayerItem("İmsak", prayerTimes.imsak, Icons.Default.NightsStay),
    PrayerItem("Güneş", prayerTimes.gunes, Icons.Default.WbSunny),
    PrayerItem("Öğle", prayerTimes.ogle, Icons.Default.LightMode),
    PrayerItem("İkindi", prayerTimes.ikindi, Icons.Default.WbSunny),
    PrayerItem("Akşam", prayerTimes.aksam, Icons.Default.WbTwilight, isIftar = true),
    PrayerItem("Yatsı", prayerTimes.yatsi, Icons.Default.DarkMode)
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("prayer_times_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.padding(3.dp))
          Text(
            text = "Günün Namaz Vakitleri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = prayerTimes.dateStr,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items.forEachIndexed { index, item ->
          val isActive = index == activeIndex
          PrayerColumnItem(
            item = item,
            isActive = isActive,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun PrayerColumnItem(
  item: PrayerItem,
  isActive: Boolean,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else if (item.isIftar) {
    MaterialTheme.colorScheme.primaryContainer
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  val textColor = if (isActive) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurface
  }

  val iconTint = if (isActive) {
    MaterialTheme.colorScheme.onPrimary
  } else if (item.isIftar) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .padding(vertical = 10.dp, horizontal = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = item.icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = item.name,
      fontSize = 11.sp,
      fontWeight = if (isActive || item.isIftar) FontWeight.Bold else FontWeight.Medium,
      color = textColor
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = item.time,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
    if (item.isIftar && !isActive) {
      Text(
        text = "İFTAR",
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}
