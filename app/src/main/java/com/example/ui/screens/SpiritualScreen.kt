package com.example.ui.screens

import com.example.i18n.t
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppPreferences
import com.example.data.HolidayCountdownCalculator
import com.example.data.SpiritualItem
import com.example.data.SpiritualQuotes
import com.example.ui.components.AdBannerComponent
import com.example.ui.components.PrivacyFooter

@Composable
fun SpiritualScreen(
  prefs: AppPreferences,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  val holidayInfo = remember { HolidayCountdownCalculator.calculate() }
  val isRamadan = holidayInfo.title.contains("Bayramı", ignoreCase = true)

  // Today's item based on day of year / Ramadan day
  val todayItem = remember { SpiritualQuotes.forToday() }

  val badgeText = if (isRamadan) {
    "Ramazan'ın ${todayItem.day}. Günü Hikmeti"
  } else {
    "Günün Hikmeti"
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("spiritual_screen")
  ) {
    // Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            painter = painterResource(R.drawable.ic_quran_mushaf),
            contentDescription = t("Ayet ve Dua"),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = t("Günün Ayet & Hadisi"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = t("Gününüzü aydınlatacak ayet-i kerime, hadis-i şerif ve dua."),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Hero Quran Rahle Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Image(
            painter = painterResource(R.drawable.ic_quran_rahle),
            contentDescription = t("Kur'an-ı Kerim ve Rahle"),
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = t("Mübarek Kur'an İklimi"),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = t("“Kur'an okuyunuz; zira o, kıyamet gününde okuyanlarına şefaatçi olarak gelecektir.”"),
              fontSize = 11.sp,
              fontStyle = FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )
          }
        }
      }
    }

    // Single Daily Card (Only today's content!)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = {
                  com.example.util.StoryImageGenerator.shareSpiritualStory(context, todayItem, 0)
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = t("Hikaye Olarak Paylaş"),
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }

              IconButton(
                onClick = {
                  val copyText = """
                    📖 ${todayItem.verseSource}:
                    "${todayItem.verseTurkish}"
                    
                    💬 ${todayItem.hadithSource}:
                    "${todayItem.hadithTurkish}"
                    
                    🤲 Dua:
                    "${todayItem.prayerTurkish}"
                  """.trimIndent()
                  clipboardManager.setText(AnnotatedString(copyText))
                  Toast.makeText(context, "Günün hikmeti panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = t("Kopyala"),
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 1. Ayet
          Text(
            text = t("📖 GÜNÜN AYETİ"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          if (todayItem.verseArabic.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = todayItem.verseArabic,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "“${todayItem.verseTurkish}”",
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 19.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "— ${todayItem.verseSource}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 12.dp)
          )

          // 2. Hadis
          Text(
            text = t("💬 GÜNÜN HADİSİ"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "“${todayItem.hadithTurkish}”",
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 19.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "— ${todayItem.hadithSource}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 12.dp)
          )

          // 3. Dua
          Text(
            text = t("🤲 GÜNÜN DUASI"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "“${todayItem.prayerTurkish}”",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 19.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "— ${todayItem.prayerSource}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Google AdMob Banner
    item {
      AdBannerComponent()
    }

    item {
      PrivacyFooter()
    }
  }
}
