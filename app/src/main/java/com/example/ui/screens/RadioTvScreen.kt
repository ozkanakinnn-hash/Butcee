package com.example.ui.screens

import com.example.i18n.t
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.DiyanetRadioCard
import com.example.ui.components.KabeLiveCard

/** Dahası > Radyo & TV: canlı radyolar ve Kâbe canlı yayını. */
@Composable
fun RadioTvScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(t("Radyo & TV"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
    }
    item { DiyanetRadioCard() }
    item { KabeLiveCard() }
  }
}
