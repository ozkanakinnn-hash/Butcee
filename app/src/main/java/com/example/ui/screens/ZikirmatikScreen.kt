package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ZikirStore
import com.example.i18n.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class TesbihatStep(val arabic: String, val tr: String, val en: String)

/** Namaz sonrası tesbihat: 33 Sübhânallah, 33 Elhamdülillâh, 33 Allâhu Ekber. */
private val tesbihat = listOf(
  TesbihatStep("سُبْحَانَ اللّٰهِ", "Sübhânallah", "SubhanAllah"),
  TesbihatStep("اَلْحَمْدُ لِلّٰهِ", "Elhamdülillâh", "Alhamdulillah"),
  TesbihatStep("اَللّٰهُ اَكْبَرُ", "Allâhu Ekber", "Allahu Akbar")
)

@Composable
fun ZikirmatikScreen(onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val store = remember { ZikirStore(context) }
  val en = AppLanguage.isEnglish

  var mode by remember { mutableStateOf(store.mode) }      // "free", "33", "99", "tesbihat"
  var count by remember { mutableIntStateOf(store.count) }
  var step by remember { mutableIntStateOf(store.tesbihatStep) }
  var total by remember { mutableIntStateOf(store.totalAllTime) }
  var pulse by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(if (pulse) 1.06f else 1f, label = "count_pulse")

  val target: Int? = when (mode) { "33", "tesbihat" -> 33; "99" -> 99; else -> null }
  val tesbihatDone = mode == "tesbihat" && step >= tesbihat.size

  fun persist() { store.count = count; store.mode = mode; store.tesbihatStep = step; store.totalAllTime = total }

  fun reset() { count = 0; step = 0; persist() }

  fun tap() {
    if (tesbihatDone) return
    count++; total++; pulse = !pulse
    if (target != null && count >= target) {
      vibrate(context, 350)
      if (mode == "tesbihat") { step++; count = 0 } else count = 0.also { saveEntry(context, store, mode, target, en) }
    } else vibrate(context, 18)
    persist()
  }

  val bg = MaterialTheme.colorScheme.background
  val panel = MaterialTheme.colorScheme.surface
  val accent = MaterialTheme.colorScheme.primary

  Column(modifier = Modifier.fillMaxSize().background(bg).testTag("zikirmatik_screen")) {
    // Üst çubuk
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (en) "Back" else "Geri") }
      Text(if (en) "Tasbih Counter" else "Zikirmatik", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
      IconButton(onClick = {
        saveEntry(context, store, mode, count, en)
      }) { Icon(Icons.Default.BookmarkBorder, contentDescription = if (en) "Save" else "Kaydet") }
      IconButton(onClick = { reset() }) { Icon(Icons.Default.Refresh, contentDescription = if (en) "Reset" else "Sıfırla") }
    }

    // Mod seçici
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
      listOf("free" to (if (en) "Free" else "Serbest"), "33" to "33", "99" to "99", "tesbihat" to (if (en) "Tasbihat" else "Tesbihat")).forEach { (key, label) ->
        val sel = mode == key
        Text(
          text = label,
          fontSize = 13.sp,
          fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
          color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .weight(1f)
            .background(if (sel) accent else Color.Transparent)
            .clickable { if (!sel) { mode = key; reset() } }
            .padding(vertical = 10.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Büyük dokunma alanı: her dokunuş bir zikir
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(28.dp))
        .background(panel)
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { tap() }
        .testTag("zikir_tap_area"),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (mode == "tesbihat") {
          if (tesbihatDone) {
            Text("✓", fontSize = 64.sp, color = accent)
            Text(if (en) "Tasbihat completed" else "Tesbihat tamamlandı", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(if (en) "May Allah accept it 🤲" else "Allah kabul etsin 🤲", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          } else {
            val s = tesbihat[step]
            Text(s.arabic, fontSize = 30.sp, color = accent)
            Text(if (en) s.en else s.tr, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("${step + 1} / ${tesbihat.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
          }
        }
        if (!tesbihatDone) {
          Text(
            text = "$count",
            fontSize = 120.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.scale(scale)
          )
          Text(
            text = if (target != null) "/ $target" else (if (en) "Tap anywhere to count" else "Saymak için dokunun"),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Text(
      text = (if (en) "Total dhikr: " else "Toplam zikir: ") + total,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 4.dp)
    )
  }
}

private fun saveEntry(context: Context, store: ZikirStore, mode: String, value: Int, en: Boolean) {
  if (value <= 0) return
  val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(Date())
  store.save("$date|$mode|$value")
  Toast.makeText(context, if (en) "Saved: $value" else "Kaydedildi: $value", Toast.LENGTH_SHORT).show()
}

private fun vibrate(context: Context, ms: Long) {
  try {
    val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
      (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    else @Suppress("DEPRECATION") v?.vibrate(ms)
  } catch (_: Exception) {
  }
}
