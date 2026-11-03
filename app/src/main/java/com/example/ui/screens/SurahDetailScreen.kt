package com.example.ui.screens

import com.example.i18n.t
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranData
import com.example.data.SurahInfo
import com.example.ui.components.AdBannerComponent

@Composable
fun SurahDetailScreen(
  surah: SurahInfo,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  var arabicFontSize by remember { mutableFloatStateOf(22f) }

  val surahDetail = remember(surah.number) {
    QuranData.getSurahDetailContent(surah.number, surah.nameTurkish)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("surah_detail_screen")
  ) {
    // Top Bar Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("surah_back_button")
          ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"))
          }
          Spacer(modifier = Modifier.width(4.dp))
          Column {
            Text(
              text = if (com.example.i18n.AppLanguage.isEnglish) "${surah.number}. Surah ${surah.nameTurkish}" else "${surah.number}. ${surah.nameTurkish} Sûresi",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "${surah.juzRangeStr} • ${surah.revelationType} • ${surah.totalVerses} Ayet",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Font Size Adjustments & Share
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { if (arabicFontSize > 16f) arabicFontSize -= 2f }
          ) {
            Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
          }
          IconButton(
            onClick = { if (arabicFontSize < 32f) arabicFontSize += 2f }
          ) {
            Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
          }
          IconButton(
            onClick = {
              val shareText = """
                📖 ${surah.number}. ${surah.nameTurkish} Sûresi (${surah.nameArabic})
                ${surah.juzRangeStr} • ${surah.totalVerses} Ayet
                
                [ARAPÇA METİN]
                ${surahDetail.fullArabic}
                
                [TÜRKÇE OKUNUŞU]
                ${surahDetail.fullPronunciation.withoutVerseNumbers()}
                
                [DİYANET TÜRKÇE MEALİ]
                ${surahDetail.fullTranslation.withoutVerseNumbers()}
                
                🏷️ Ramazan Rehberi Uygulaması
              """.trimIndent()
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, "${surah.nameTurkish} Suresini Paylaş"))
            }
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = t("Paylaş"), tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // Surah Banner Info Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = surah.nameArabic,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = (if (com.example.i18n.AppLanguage.isEnglish) "Meaning: " else "Anlamı: ") + com.example.data.QuranData.meaningOf(surah),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Kur'an-ı Kerim ${surah.juzRangeStr} • Sıra No: ${surah.number}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
      }
    }

    // 1. BÖLÜM: EN ÜSTTE ARAPÇA SURENİN TAMAMI (BÜTÜN OLARAK)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("surah_arabic_full_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Section Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = t("ARAPÇA METİN"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            IconButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(surahDetail.fullArabic))
                Toast.makeText(context, "Arapça metin kopyalandı ✓", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = t("Kopyala"), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Full Continuous Arabic Text
          val isDark = com.example.ui.theme.isAppInDarkTheme()
          val arabicTextColor = if (isDark) Color(0xFFF0E5D3) else Color(0xFF1B3D2F)
          Text(
            text = surahDetail.fullArabic,
            fontSize = arabicFontSize.sp,
            lineHeight = (arabicFontSize * 1.85f).sp,
            color = arabicTextColor,
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(16.dp)
          )
        }
      }
    }

    // 2. BÖLÜM: HEMEN ALTINDA TÜRKÇE OKUNUŞU (LATİN HARFLERİYLE)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("surah_pronunciation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Section Badge
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
                text = t("TÜRKÇE OKUNUŞU"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            IconButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(surahDetail.fullPronunciation.withoutVerseNumbers()))
                Toast.makeText(context, "Türkçe okunuşu kopyalandı ✓", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = t("Kopyala"), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = surahDetail.fullPronunciation.withoutVerseNumbers(),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(14.dp)
          )
        }
      }
    }

    // 3. BÖLÜM: EN ALTTA DİYANET KAYNAKLI/UYUMLU TÜRKÇE MEALİ
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("surah_translation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Section Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = t("DİYANET TÜRKÇE MEALİ"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
            }

            IconButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(surahDetail.fullTranslation.withoutVerseNumbers()))
                Toast.makeText(context, "Türkçe meal kopyalandı ✓", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = t("Kopyala"), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = surahDetail.fullTranslation.withoutVerseNumbers(),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(14.dp)
          )
        }
      }
    }

    // AdMob Banner & Bottom Spacing
    item {
      AdBannerComponent()
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

/** Okunuş ve meal metinlerindeki satır başı ayet numaralarını ("1. ", "2. " …) kaldırır. */
private fun String.withoutVerseNumbers(): String =
  lines().joinToString("\n") { it.replaceFirst(Regex("^(\\s*)\\d+\\.\\s+"), "$1") }
