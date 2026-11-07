package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EsmaulHusna
import com.example.i18n.AppLanguage

/** Esmâü'l-Hüsnâ: Allah'ın 99 ismi (Arapça, okunuş, anlam). */
@Composable
fun EsmaulHusnaScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val en = AppLanguage.isEnglish
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    item {
      Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (en) "Back" else "Geri")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(if (en) "The 99 Names of Allah" else "Esmâü'l-Hüsnâ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Text(
            if (en) "Al-Asma ul-Husna • the Beautiful Names" else "Allah'ın 99 güzel ismi",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
    items(EsmaulHusna.names, key = { it.number }) { n ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text("${n.number}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(n.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(if (en) n.meaningEn else n.meaningTr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(n.arabic, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
        }
      }
    }
  }
}
