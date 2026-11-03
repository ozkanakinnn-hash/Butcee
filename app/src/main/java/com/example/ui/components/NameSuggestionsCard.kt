package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NameSuggestion
import com.example.data.NameSuggestions

/** "Bugün doğanlara isim önerileri" — kız ve erkek çocuklar için ayrı sütunlar. */
@Composable
fun NameSuggestionsCard(modifier: Modifier = Modifier) {
  val (girls, boys) = remember { NameSuggestions.forToday() }
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("name_suggestions_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("👶 Bugün Doğanlara İsim Önerileri", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(
        "Anlamı güzel, Kur'an ve İslam tarihinde geçen isimler",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NameColumn("👧 Kız", girls, Color(0xFFD9779B), Modifier.weight(1f))
        NameColumn("👦 Erkek", boys, Color(0xFF5B8FD1), Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun NameColumn(title: String, names: List<NameSuggestion>, tint: Color, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(tint.copy(alpha = 0.10f))
      .padding(10.dp)
  ) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tint)
    names.forEach { n ->
      Spacer(modifier = Modifier.height(8.dp))
      Text(n.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(n.meaning, fontSize = 10.sp, lineHeight = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(modifier = Modifier.width(1.dp))
  }
}
