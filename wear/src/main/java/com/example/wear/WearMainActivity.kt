package com.example.wear

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import java.util.Calendar

private const val PATH = "/ramazan/prayer_times"
private val Gold = Color(0xFFE5B869)

/** Telefondan gelen son veriler (saatte kalıcı saklanır). */
private class WearStore(context: Context) {
  private val p = context.getSharedPreferences("wear_data", Context.MODE_PRIVATE)
  val keys = listOf("imsak", "gunes", "ogle", "ikindi", "aksam", "yatsi")
  fun times(): List<String> = keys.map { p.getString(it, "--:--") ?: "--:--" }
  fun place(): String = p.getString("place", "") ?: ""
  fun english(): Boolean = p.getString("lang", "tr") == "en"
  fun hasData(): Boolean = p.contains("imsak")
  fun save(values: Map<String, String>) { p.edit().apply { values.forEach { (k, v) -> putString(k, v) } }.apply() }
  var zikir: Int
    get() = p.getInt("zikir", 0)
    set(v) = p.edit().putInt("zikir", v).apply()
}

class WearMainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MaterialTheme { WearApp() } }
  }
}

@Composable
private fun WearApp() {
  val context = LocalContext.current
  val store = remember { WearStore(context) }
  var version by remember { mutableIntStateOf(0) } // veri gelince yeniden çiz
  var screen by remember { mutableStateOf("home") }

  // Telefondan gelen verileri oku ve değişiklikleri dinle
  DisposableEffect(Unit) {
    val client = Wearable.getDataClient(context)
    fun ingest(item: com.google.android.gms.wearable.DataItem) {
      if (item.uri.path != PATH) return
      val map = DataMapItem.fromDataItem(item).dataMap
      store.save((store.keys + listOf("place", "lang")).associateWith { map.getString(it) ?: "" })
      version++
    }
    client.dataItems.addOnSuccessListener { buf -> buf.forEach { ingest(it) }; buf.release() }
    val listener = DataClient.OnDataChangedListener { events ->
      events.forEach { if (it.type == DataEvent.TYPE_CHANGED) ingest(it.dataItem) }
    }
    client.addListener(listener)
    onDispose { client.removeListener(listener) }
  }

  val en = remember(version) { store.english() }
  Scaffold(timeText = { TimeText() }) {
    if (screen == "zikir") ZikirScreen(store, en) { screen = "home" }
    else HomeScreen(store, en, version) { screen = "zikir" }
  }
}

@Composable
private fun HomeScreen(store: WearStore, en: Boolean, version: Int, onZikir: () -> Unit) {
  val times = remember(version) { store.times() }
  val names = if (en) listOf("Imsak", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
              else listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
  var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }

  // Sıradaki vakit ve kalan süre
  val cal = Calendar.getInstance().apply { timeInMillis = now }
  val nowSec = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
  val secs = times.map { t -> t.split(":").let { if (it.size == 2) (it[0].toIntOrNull() ?: 0) * 3600 + (it[1].toIntOrNull() ?: 0) * 60 else -1 } }
  val idx = secs.indexOfFirst { it > nowSec }.let { if (it == -1) 0 else it }
  val remaining = ((if (secs[idx] > nowSec) secs[idx] else secs[idx] + 86400) - nowSec).coerceAtLeast(0)
  val countdown = String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", remaining / 3600, (remaining % 3600) / 60, remaining % 60)

  val listState = rememberScalingLazyListState()
  ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    item {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (en) "Next: ${names[idx]}" else "Sıradaki: ${names[idx]}", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(if (store.hasData()) countdown else "--:--:--", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(store.place().ifBlank { if (en) "Open the phone app to sync" else "Eşitlemek için telefondaki uygulamayı açın" },
          fontSize = 10.sp, textAlign = TextAlign.Center, color = Color.LightGray)
      }
    }
    times.forEachIndexed { i, t ->
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp)
            .background(if (i == idx) Gold.copy(alpha = 0.25f) else Color.Transparent),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(names[i], fontSize = 14.sp, fontWeight = if (i == idx) FontWeight.Bold else FontWeight.Normal)
          Text(t, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (i == idx) Gold else Color.White)
        }
      }
    }
    item {
      Chip(
        onClick = onZikir,
        label = { Text(if (en) "📿 Tasbih counter" else "📿 Zikirmatik") },
        colors = ChipDefaults.primaryChipColors(backgroundColor = Gold, contentColor = Color.Black),
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
private fun ZikirScreen(store: WearStore, en: Boolean, onBack: () -> Unit) {
  val context = LocalContext.current
  var count by remember { mutableIntStateOf(store.zikir) }
  Box(
    modifier = Modifier.fillMaxSize().clickable {
      count++
      store.zikir = count
      vibrate(context, if (count % 33 == 0) 300 else 20)
    },
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text("$count", fontSize = 54.sp, fontWeight = FontWeight.Bold)
      Text("/ 33 • ${count / 33} ${if (en) "rounds" else "tur"}", fontSize = 12.sp, color = Gold)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
        Chip(onClick = { count = 0; store.zikir = 0 }, label = { Text(if (en) "Reset" else "Sıfırla", fontSize = 11.sp) },
          colors = ChipDefaults.secondaryChipColors())
        Chip(onClick = onBack, label = { Text(if (en) "Back" else "Geri", fontSize = 11.sp) },
          colors = ChipDefaults.secondaryChipColors())
      }
    }
  }
}

private fun vibrate(context: Context, ms: Long) {
  try {
    val v: Vibrator? = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    v?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
  } catch (_: Exception) {
  }
}
