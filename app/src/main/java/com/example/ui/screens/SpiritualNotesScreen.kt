package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpiritualNotesScreen(
  prefs: AppPreferences,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current

  var notes by remember { mutableStateOf(prefs.getNotes()) }
  var newNoteText by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .testTag("spiritual_notes_screen")
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "Manevi Notlar & Günlük",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Kişisel dualarınız ve Ramazan notlarınız cihazınızda güvenle saklanır",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Add note card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Yeni Dua veya Not Ekle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newNoteText,
            onValueChange = { newNoteText = it },
            placeholder = { Text("Bugün için niyetiniz, ettiğiniz bir dua veya şükür notunuz...") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = {
              if (newNoteText.isNotBlank()) {
                val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                val fullNote = "$timestamp — ${newNoteText.trim()}"
                prefs.addNote(fullNote)
                notes = prefs.getNotes()
                newNoteText = ""
                Toast.makeText(context, "Notunuz kaydedildi", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kaydet")
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Notes list
    if (notes.isEmpty()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Henüz bir not eklemediniz.\nYukarıdaki kutudan ilk duanızı yazabilirsiniz.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      itemsIndexed(notes) { index, note ->
        Card(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = note,
              fontSize = 13.sp,
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = {
                prefs.deleteNote(index)
                notes = prefs.getNotes()
                Toast.makeText(context, "Not silindi", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.outline)
            }
          }
        }
      }
    }
  }
}
