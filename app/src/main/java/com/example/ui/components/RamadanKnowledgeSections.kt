package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bi
import com.example.data.FastingKnowledge
import com.example.i18n.t

/** Orucun faydaları: Dini / Tıbbi sekmeli kart. */
@Composable
fun FastingBenefitsSection(modifier: Modifier = Modifier) {
  var tab by remember { mutableIntStateOf(0) }
  val tabs = listOf(t("🕌 Dini Faydaları"), t("🩺 Tıbbi Faydaları"))
  val items = if (tab == 0) FastingKnowledge.religiousBenefits else FastingKnowledge.medicalBenefits

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("fasting_benefits_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(
        text = t("🌙 Orucun Faydaları"),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(4.dp)
      ) {
        tabs.forEachIndexed { i, title ->
          val selected = tab == i
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9.dp))
              .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
              .clickable { tab = i }
              .padding(vertical = 9.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              fontSize = 12.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))

      items.forEachIndexed { index, b ->
        if (index > 0) {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 10.dp)
          )
        }
        Text(
          text = b.title.text,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = b.body.text,
          fontSize = 12.sp,
          lineHeight = 17.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (b.source.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "— ${b.source}",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (tab == 1) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "ⓘ " + FastingKnowledge.medicalNote.text,
          fontSize = 10.sp,
          lineHeight = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp)
        )
      }
    }
  }
}

/** Maddeler hâlinde öneri listesi (iftar / sahur). İlk 5 madde görünür, kalanı "Tümünü gör" ile açılır. */
@Composable
fun TipsListSection(
  title: String,
  emoji: String,
  tips: List<Bi>,
  testTag: String,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val visible = if (expanded) tips else tips.take(5)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 17.sp) }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      visible.forEachIndexed { i, tip ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
          horizontalArrangement = Arrangement.Start
        ) {
          Box(
            modifier = Modifier
              .padding(top = 1.dp)
              .size(20.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${i + 1}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = tip.text,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
      if (tips.size > 5) {
        Text(
          text = if (expanded) t("Daha az göster ▲") else t("Tümünü gör ▼"),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }
    }
  }
}
