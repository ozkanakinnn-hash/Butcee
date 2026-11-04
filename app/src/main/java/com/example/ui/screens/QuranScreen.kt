package com.example.ui.screens

import com.example.i18n.t
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.hatimItems
import com.example.data.QuranData
import com.example.data.SurahInfo
import com.example.ui.components.AdBannerComponent

@Composable
fun QuranScreen(
  onSelectSurah: (SurahInfo) -> Unit,
  onNavigateBack: () -> Unit,
  onOpenPage: (Int) -> Unit = {},
  initialTab: Int = 0,
  modifier: Modifier = Modifier
) {
  var tab by remember { androidx.compose.runtime.mutableIntStateOf(initialTab) }
  val hatim = com.example.ui.components.rememberHatimState()
  val context = androidx.compose.ui.platform.LocalContext.current
  val bookmark = remember(tab) { com.example.data.HatimStore(context).bookmarkPage }
  BackHandler { onNavigateBack() }
  var searchQuery by remember { mutableStateOf("") }

  val filteredSurahs = remember(searchQuery) {
    if (searchQuery.isBlank()) QuranData.surahs
    else QuranData.surahs.filter {
      it.nameTurkish.contains(searchQuery, ignoreCase = true) ||
        it.meaningTurkish.contains(searchQuery, ignoreCase = true) ||
        com.example.data.QuranData.meaningOf(it).contains(searchQuery, ignoreCase = true) ||
        it.juzRangeStr.contains(searchQuery, ignoreCase = true) ||
        it.number.toString() == searchQuery.trim()
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("quran_screen")
  ) {
    // Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            painter = painterResource(R.drawable.ic_quran_mushaf),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = t("Kur'an-ı Kerîm"),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = t("114 Sure • 30 Cüz • Orijinal Sıralama"),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Sureler | Hatim Takibi
    item { com.example.ui.components.QuranTabSelector(tab) { tab = it } }
    if (tab == 1) {
      hatimItems(hatim, onOpenPage)
      return@LazyColumn
    }

    // Yer imi
    if (bookmark > 0) item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val en = com.example.i18n.AppLanguage.isEnglish
        if (bookmark > 0) ResumeChip(if (en) "🔖 Bookmark • p. $bookmark" else "🔖 Yer imi • s. $bookmark", Modifier.weight(1f)) { onOpenPage(bookmark) }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(t("Sure adı, cüz no veya anlam ara (Örn: Yasin, 30. Cüz)...")) },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = t("Temizle"))
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("search_surah_input"),
        shape = RoundedCornerShape(14.dp)
      )
    }

    // Surahs count subtitle
    item {
      Text(
        text = if (com.example.i18n.AppLanguage.isEnglish) "Surahs in Quran order (${filteredSurahs.size})" else "Kur'an Sırasına Göre Sureler (${filteredSurahs.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
      )
    }

    // Surah Items
    items(filteredSurahs) { surah ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clickable { onSelectSurah(surah) }
          .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left: Sequence Number & Name
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${surah.number}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = surah.nameTurkish,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Cüz indicator badge requested by user
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = com.example.data.QuranData.juzLabel(surah),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }

              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (com.example.i18n.AppLanguage.isEnglish) "${com.example.data.QuranData.meaningOf(surah)} • ${surah.totalVerses} verses • ${if (surah.revelationType.startsWith("Mek")) "Meccan" else "Medinan"}"
                  else "${surah.meaningTurkish} • ${surah.totalVerses} Ayet • ${surah.revelationType}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Right: Arabic Calligraphy
          Text(
            text = surah.nameArabic,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    item {
      AdBannerComponent()
    }
  }
}

@Composable
private fun ResumeChip(label: String, modifier: Modifier, onClick: () -> Unit) {
  Text(
    text = label,
    fontSize = 12.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    maxLines = 1,
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.primaryContainer)
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 10.dp)
  )
}
