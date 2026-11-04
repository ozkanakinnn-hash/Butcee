package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ZikirStore
import com.example.i18n.AppLanguage
import com.example.i18n.t

/**
 * Ana sayfadaki kompakt zikirmatik: büyük yuvarlak düğmeye her dokunuş bir zikir.
 * Tam ekran Zikirmatik ile aynı sayacı (ZikirStore) paylaşır.
 */
@Composable
fun HomeZikirCard(onOpenFull: () -> Unit, refreshKey: Any? = null, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val store = remember { ZikirStore(context) }
  var count by remember(refreshKey) { mutableIntStateOf(store.count) }
  var total by remember(refreshKey) { mutableIntStateOf(store.totalAllTime) }
  val mode = remember(refreshKey) { store.mode }
  var step by remember(refreshKey) { mutableIntStateOf(store.tesbihatStep) }
  val target: Int? = when (mode) { "33", "tesbihat" -> 33; "99" -> 99; else -> null }
  val en = AppLanguage.isEnglish
  val tesbihat = listOf("Sübhânallah" to "SubhanAllah", "Elhamdülillâh" to "Alhamdulillah", "Allâhu Ekber" to "Allahu Akbar")
  val label = when {
    mode == "tesbihat" && step < 3 -> if (en) tesbihat[step].second else tesbihat[step].first
    mode == "tesbihat" -> if (en) "Tasbihat completed" else "Tesbihat tamamlandı"
    mode == "free" -> t("Serbest zikir")
    else -> "$mode'lük zikir".let { if (en) "Count of $mode" else it }
  }

  fun tap() {
    if (mode == "tesbihat" && step >= 3) return
    count++; total++
    if (target != null && count >= target) {
      vibrate(context, 300)
      count = 0
      if (mode == "tesbihat") step++
    } else vibrate(context, 15)
    store.count = count; store.totalAllTime = total; store.tesbihatStep = step
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("home_zikir_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            painter = painterResource(R.drawable.ic_tasbih),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = t("Zikirmatik"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Text(
          text = if (target != null) "$count / $target" else "$count",
          fontSize = 34.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = (if (en) "Total: " else "Toplam: ") + total,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
          IconButton(onClick = {
            count = 0; step = 0; store.count = 0; store.tesbihatStep = 0
          }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = t("Sıfırla"), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
          }
          Text(
            text = t("Tam ekran aç ›"),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { onOpenFull() }
              .padding(horizontal = 6.dp, vertical = 6.dp)
          )
        }
      }
      // Büyük dokunma düğmesi
      Box(
        modifier = Modifier
          .size(118.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer)
          .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
          .clickable { tap() }
          .testTag("home_zikir_tap"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center
        ) {
          Text("+1", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
      }
    }
  }
}

private fun vibrate(context: Context, ms: Long) {
  try {
    val v = if (Build.VERSION.SDK_INT >= 31) {
      (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
      @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    else @Suppress("DEPRECATION") v.vibrate(ms)
  } catch (_: Exception) { }
}
