package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HatimStore
import com.example.data.QuranData
import com.example.data.QuranText
import com.example.i18n.AppLanguage
import kotlin.math.cos
import kotlin.math.sin

/** Hatim takibi durumu (ekranda tutulur, HatimStore'a yazılır). */
class HatimState(val store: HatimStore) {
  var juz by mutableStateOf(store.readJuz)
  var surahs by mutableStateOf(store.readSurahs)
  var completed by mutableIntStateOf(store.completedCount)
  var tab by mutableIntStateOf(0)
  var confirm by mutableStateOf<String?>(null) // "complete" / "reset"

  fun toggleJuz(j: Int) { store.toggleJuz(j); juz = store.readJuz }
  fun toggleSurah(n: Int) { store.toggleSurah(n); surahs = store.readSurahs }
  fun complete() { store.completeHatim(); juz = emptySet(); surahs = emptySet(); completed = store.completedCount }
  fun reset() { store.resetProgress(); juz = emptySet(); surahs = emptySet() }
  val nextJuz: Int get() = (1..30).firstOrNull { it !in juz } ?: 30
}

@Composable
fun rememberHatimState(): HatimState {
  val context = LocalContext.current
  return remember { HatimState(HatimStore(context)) }
}

private fun tr(trText: String, en: String) = if (AppLanguage.isEnglish) en else trText

/** Kur'an ekranındaki "Hatim Takibi" sekmesi içeriği. */
fun LazyListScope.hatimItems(state: HatimState, onOpenPage: (Int) -> Unit) {
  item { HatimHero(state) }
  item { HatimStats(state) }
  item {
    Segmented(
      listOf(tr("Cüz Takibi", "Juz Tracking"), tr("Sure Takibi", "Surah Tracking")),
      state.tab
    ) { state.tab = it }
  }
  if (state.tab == 0) {
    item { JuzGrid(state) }
  } else {
    items(QuranData.surahs) { s ->
      val read = s.number in state.surahs
      val cs = MaterialTheme.colorScheme
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 2.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(cs.surface)
          .clickable { state.toggleSurah(s.number) }
          .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        StarTile(s.number, if (read) TileState.DONE else TileState.TODO, modifier = Modifier.size(34.dp), textSize = 11)
        Spacer(modifier = Modifier.width(10.dp))
        Text(s.nameTurkish, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface, modifier = Modifier.weight(1f))
        Text(
          if (read) tr("✓ Okundu", "✓ Read") else tr("Okunmadı", "Not read"),
          fontSize = 12.sp, fontWeight = FontWeight.Bold,
          color = if (read) cs.primary else cs.onSurfaceVariant
        )
      }
    }
  }
  item { Legend() }
  item { HatimActions(state, onOpenPage) }
  item { Spacer(modifier = Modifier.height(16.dp)) }
}

@Composable
private fun HatimHero(state: HatimState) {
  val cs = MaterialTheme.colorScheme
  val progress = state.juz.size / 30f
  val next = state.nextJuz
  val nextSurah = QuranText.page(QuranText.firstPageOfJuz(next)).firstOrNull()?.let { r -> QuranData.surahs.firstOrNull { it.number == r.surah }?.nameTurkish }.orEmpty()
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(cs.surface)
      .border(1.dp, cs.outline, RoundedCornerShape(22.dp))
      .testTag("hatim_hero")
  ) {
    val orn = cs.primary.copy(alpha = 0.09f)
    Canvas(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 26.dp, y = (-26).dp)) {
      drawPath(star(center, size.minDimension * 0.48f, 0.6f), orn, style = Stroke(1.5.dp.toPx()))
      drawPath(star(center, size.minDimension * 0.28f, 0.6f), orn)
    }
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        val track = cs.surfaceVariant; val prim = cs.primary; val pc = cs.primaryContainer
        Canvas(modifier = Modifier.fillMaxSize()) {
          val sw = 8.dp.toPx()
          drawArc(track, 0f, 360f, false, topLeft = Offset(sw / 2, sw / 2), size = androidx.compose.ui.geometry.Size(size.width - sw, size.height - sw), style = Stroke(sw))
          drawArc(prim, -90f, 360f * progress, false, topLeft = Offset(sw / 2, sw / 2), size = androidx.compose.ui.geometry.Size(size.width - sw, size.height - sw), style = Stroke(sw, cap = StrokeCap.Round))
          drawPath(star(center, size.minDimension * 0.24f, 0.72f), pc)
          drawPath(star(center, size.minDimension * 0.24f, 0.72f), prim, style = Stroke(0.8.dp.toPx()))
        }
        Text("%${(progress * 100).toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(
          if (AppLanguage.isEnglish) "Khatm #${state.completed + 1}" else "${state.completed + 1}. Hatim",
          fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onSurface
        )
        Text(
          if (AppLanguage.isEnglish) "Next: Juz $next • $nextSurah" else "Sıradaki: $next. Cüz • $nextSurah Sûresi",
          fontSize = 12.sp, color = cs.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          tr("🏆 Tamamlanan hatim: ", "🏆 Completed: ") + state.completed,
          fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.primary,
          modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(cs.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun HatimStats(state: HatimState) {
  val cs = MaterialTheme.colorScheme
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(cs.surface)
      .border(1.dp, cs.outline, RoundedCornerShape(18.dp))
  ) {
    listOf(
      Triple(tr("Cüz İlerlemesi", "Juz progress"), state.juz.size, 30),
      Triple(tr("Sure İlerlemesi", "Surah progress"), state.surahs.size, 114)
    ).forEachIndexed { i, (label, v, total) ->
      if (i == 1) Box(modifier = Modifier.width(1.dp).height(72.dp).background(cs.outline).align(Alignment.CenterVertically))
      Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(label, fontSize = 11.sp, color = cs.onSurfaceVariant)
        Text("$v/$total", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
          progress = { v / total.toFloat() },
          modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
          color = cs.primary, trackColor = cs.surfaceVariant
        )
      }
    }
  }
}

@Composable
private fun Segmented(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
  val cs = MaterialTheme.colorScheme
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(cs.surface)
      .border(1.dp, cs.outline, RoundedCornerShape(14.dp))
      .padding(3.dp)
  ) {
    labels.forEachIndexed { i, l ->
      val sel = i == selected
      Text(
        l, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
        color = if (sel) cs.onPrimary else cs.onSurfaceVariant, textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(11.dp)).background(if (sel) cs.primary else Color.Transparent)
          .clickable { onSelect(i) }.padding(vertical = 8.dp)
      )
    }
  }
}

/** Sure listesi / ana Kur'an ekranı üstündeki "Sureler | Hatim Takibi" seçici. */
@Composable
fun QuranTabSelector(selected: Int, onSelect: (Int) -> Unit) =
  Segmented(listOf(tr("📖 Sureler", "📖 Surahs"), tr("🏆 Hatim Takibi", "🏆 Khatm Tracker")), selected, onSelect)

private enum class TileState { DONE, NOW, TODO }

@Composable
private fun JuzGrid(state: HatimState) {
  val next = state.nextJuz
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    for (row in 0 until 6) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (col in 0 until 5) {
          val j = row * 5 + col + 1
          val st = when { j in state.juz -> TileState.DONE; j == next -> TileState.NOW; else -> TileState.TODO }
          StarTile(
            j, st,
            modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(50)).clickable { state.toggleJuz(j) }.testTag("juz_$j"),
            textSize = 15
          )
        }
      }
    }
  }
}

@Composable
private fun StarTile(n: Int, st: TileState, modifier: Modifier = Modifier, textSize: Int) {
  val cs = MaterialTheme.colorScheme
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    val prim = cs.primary; val sec = cs.secondary; val pc = cs.primaryContainer; val surf = cs.surface; val line = cs.outline
    Canvas(modifier = Modifier.fillMaxSize()) {
      val p = star(center, size.minDimension * 0.47f, 0.86f)
      when (st) {
        TileState.DONE -> {
          drawPath(p, Brush.linearGradient(listOf(lerp(prim, Color.White, 0.25f), prim), Offset.Zero, Offset(size.width, size.height)))
          drawPath(p, prim, style = Stroke(1.dp.toPx()))
        }
        TileState.NOW -> { drawPath(p, pc); drawPath(p, sec, style = Stroke(2.dp.toPx())) }
        TileState.TODO -> { drawPath(p, surf); drawPath(p, line, style = Stroke(1.dp.toPx())) }
      }
    }
    Text(
      "$n", fontSize = textSize.sp, fontWeight = FontWeight.ExtraBold,
      color = when (st) { TileState.DONE -> cs.onPrimary; TileState.NOW -> cs.primary; TileState.TODO -> cs.onSurface }
    )
  }
}

@Composable
private fun Legend() {
  val cs = MaterialTheme.colorScheme
  Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), horizontalArrangement = Arrangement.Center) {
    listOf(
      Triple(cs.primary, cs.primary, tr("Okundu", "Read")),
      Triple(cs.primaryContainer, cs.secondary, tr("Sırada", "Next")),
      Triple(cs.surface, cs.outline, tr("Okunmadı", "Not read"))
    ).forEach { (fill, border, label) ->
      Box(modifier = Modifier.padding(start = 12.dp, end = 4.dp).size(10.dp).clip(RoundedCornerShape(3.dp)).background(fill).border(1.dp, border, RoundedCornerShape(3.dp)).align(Alignment.CenterVertically))
      Text(label, fontSize = 11.sp, color = cs.onSurfaceVariant)
    }
  }
}

@Composable
private fun HatimActions(state: HatimState, onOpenPage: (Int) -> Unit) {
  val cs = MaterialTheme.colorScheme
  val next = state.nextJuz
  Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      if (AppLanguage.isEnglish) "📖 Read Juz $next" else "📖 $next. Cüzü Oku",
      fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onPrimary, textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(cs.primary)
        .clickable { onOpenPage(QuranText.firstPageOfJuz(next)) }.padding(vertical = 12.dp).testTag("hatim_read_next")
    )
    Text(
      tr("Hatmi Tamamla", "Complete Khatm"),
      fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.primary, textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(cs.surface).border(1.dp, cs.outline, RoundedCornerShape(14.dp))
        .clickable { state.confirm = "complete" }.padding(vertical = 12.dp)
    )
  }
  Text(
    tr("↻ Bu hatmin işaretlerini sıfırla", "↻ Reset this khatm's marks"),
    fontSize = 12.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().clickable { state.confirm = "reset" }.padding(8.dp)
  )

  state.confirm?.let { kind ->
    AlertDialog(
      onDismissRequest = { state.confirm = null },
      title = { Text(if (kind == "complete") tr("Hatmi tamamla", "Complete khatm") else tr("İşaretleri sıfırla", "Reset marks")) },
      text = {
        Text(
          if (kind == "complete") tr("Allah kabul etsin! Tamamlanan hatim sayısı artırılacak ve yeni hatim için işaretler temizlenecek.", "May God accept it! The completed count will increase and marks will be cleared for a new khatm.")
          else tr("Bu hatimdeki cüz ve sure işaretleri silinecek.", "Juz and surah marks of this khatm will be cleared.")
        )
      },
      confirmButton = {
        TextButton(onClick = { if (kind == "complete") state.complete() else state.reset(); state.confirm = null }) {
          Text(tr("Onayla", "Confirm"), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = { TextButton(onClick = { state.confirm = null }) { Text(tr("Vazgeç", "Cancel")) } }
    )
  }
}

private fun star(c: Offset, r: Float, inner: Float): Path = Path().apply {
  for (k in 0 until 16) {
    val a = Math.PI / 8 * k - Math.PI / 2
    val rr = if (k % 2 == 0) r else r * inner
    val x = c.x + rr * cos(a).toFloat(); val y = c.y + rr * sin(a).toFloat()
    if (k == 0) moveTo(x, y) else lineTo(x, y)
  }
  close()
}
