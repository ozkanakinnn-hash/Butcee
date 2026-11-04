package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HatimStore
import com.example.data.QuranData
import com.example.data.QuranText
import com.example.i18n.AppLanguage
import com.example.i18n.t
import com.example.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val Amiri = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal), Font(R.font.amiri_bold, FontWeight.Bold))

private fun arabicDigits(n: Int): String = n.toString().map { c -> '٠' + (c - '0') }.joinToString("")

/**
 * Mushaf sayfası boyutunda, yana kaydırmalı Kur'an okuyucu (Medine mushafı 604 sayfa).
 * Sayfalar Arapça kitap düzeninde sağdan sola ilerler. Arapça / Okunuş / Meal görünümleri.
 */
@Composable
fun QuranReaderScreen(startPage: Int, onNavigateBack: () -> Unit) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  val store = remember { HatimStore(context) }
  val en = AppLanguage.isEnglish
  val modes = if (en) listOf("Arabic", "Transliteration") else listOf("Arapça", "Okunuş", "Meal")
  var mode by remember { mutableIntStateOf(0) }
  var fontStep by remember { mutableIntStateOf(store.fontStep.coerceIn(0, 2)) }
  var bookmark by remember { mutableIntStateOf(store.bookmarkPage) }
  val pager = rememberPagerState(initialPage = (startPage - 1).coerceIn(0, QuranText.PAGE_COUNT - 1), pageCount = { QuranText.PAGE_COUNT })
  val scope = rememberCoroutineScope()
  val currentPage = pager.currentPage + 1
  val rows = remember(currentPage) { QuranText.page(currentPage) }
  val first = rows.firstOrNull()
  val surahInfo = first?.let { r -> QuranData.surahs.firstOrNull { it.number == r.surah } }

  LaunchedEffect(currentPage) { store.lastPage = currentPage }

  val cs = MaterialTheme.colorScheme
  Column(modifier = Modifier.fillMaxSize().background(cs.background).testTag("quran_reader")) {
    // Üst çubuk
    Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"), tint = cs.onBackground) }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = surahInfo?.let { if (en) "Surah ${it.nameTurkish}" else "${it.nameTurkish} Sûresi" } ?: "",
          fontSize = 17.sp, fontWeight = FontWeight.Bold, color = cs.onBackground
        )
        Text(
          text = if (en) "Juz ${first?.juz ?: 1} • Page $currentPage / ${QuranText.PAGE_COUNT}" else "${first?.juz ?: 1}. Cüz • Sayfa $currentPage / ${QuranText.PAGE_COUNT}",
          fontSize = 11.sp, color = cs.onSurfaceVariant
        )
      }
      IconButton(onClick = {
        bookmark = if (bookmark == currentPage) 0 else currentPage
        store.bookmarkPage = bookmark
        android.widget.Toast.makeText(
          context,
          if (bookmark != 0) (if (en) "Page $currentPage bookmarked" else "Sayfa $currentPage işaretlendi") else (if (en) "Bookmark removed" else "Yer imi kaldırıldı"),
          android.widget.Toast.LENGTH_SHORT
        ).show()
      }) {
        Icon(if (bookmark == currentPage) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = t("Yer imi"), tint = cs.primary)
      }
      IconButton(onClick = { fontStep = (fontStep + 1) % 3; store.fontStep = fontStep }) {
        Icon(Icons.Default.FormatSize, contentDescription = t("Yazı boyutu"), tint = cs.primary)
      }
    }

    // Arapça / Okunuş / Meal
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(cs.surface)
        .border(1.dp, cs.outline, RoundedCornerShape(14.dp))
        .padding(3.dp)
    ) {
      modes.forEachIndexed { i, m ->
        val sel = i == mode
        Text(
          text = m,
          fontSize = 12.sp,
          fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
          color = if (sel) cs.onPrimary else cs.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(11.dp))
            .background(if (sel) cs.primary else Color.Transparent)
            .clickable { mode = i }
            .padding(vertical = 8.dp)
        )
      }
    }

    // Sayfalar (sağdan sola)
    HorizontalPager(
      state = pager,
      reverseLayout = true,
      pageSpacing = 10.dp,
      modifier = Modifier.weight(1f).fillMaxWidth()
    ) { index ->
      QuranPage(
        page = index + 1,
        mode = mode,
        fontStep = fontStep,
        onPrev = { scope.launch { pager.animateScrollToPage((index - 1).coerceAtLeast(0)) } },
        onNext = { scope.launch { pager.animateScrollToPage((index + 1).coerceAtMost(QuranText.PAGE_COUNT - 1)) } }
      )
    }

    LinearProgressIndicator(
      progress = { currentPage / QuranText.PAGE_COUNT.toFloat() },
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
      color = cs.primary,
      trackColor = cs.surfaceVariant
    )
  }
}

@Composable
private fun QuranPage(page: Int, mode: Int, fontStep: Int, onPrev: () -> Unit, onNext: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  val dark = isAppInDarkTheme()
  val rows = remember(page) { QuranText.page(page) }
  val en = AppLanguage.isEnglish
  val pageBg = if (dark) lerp(cs.surface, cs.primary, 0.03f) else lerp(cs.background, Color(0xFFFFFBF0), 0.6f)
  val groups = remember(page) { rows.groupBy { it.surah }.toList() }
  val arSize = listOf(21, 24, 28)[fontStep]
  val trSize = listOf(14, 16, 18)[fontStep]

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(pageBg)
      .border(1.dp, cs.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
      .padding(5.dp)
      .border(1.5.dp, cs.primary.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
      .padding(3.dp)
      .border(0.6.dp, cs.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
  ) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
      // Sayfa başlığı: cüz • sure
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("الجزء " + arabicDigits(rows.firstOrNull()?.juz ?: 1), fontFamily = Amiri, fontSize = 12.sp, color = cs.onSurfaceVariant)
        val name = rows.firstOrNull()?.let { r -> QuranData.surahs.firstOrNull { it.number == r.surah }?.nameArabic }.orEmpty()
        Text("سورة $name", fontFamily = Amiri, fontSize = 12.sp, color = cs.onSurfaceVariant)
      }
      Column(
        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 6.dp)
      ) {
        groups.forEach { (surahNo, list) ->
          val info = QuranData.surahs.firstOrNull { it.number == surahNo }
          if (list.first().ayah == 1 && info != null) {
            SurahBand(info.nameArabic)
            if (surahNo != 1 && surahNo != 9) {
              Text(
                text = if (mode == 0) "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" else if (en) "Bismi Allahi alrrahmani alrraheemi" else "Bismillâhirrahmânirrahîm",
                fontFamily = if (mode == 0) Amiri else FontFamily.Default,
                fontStyle = if (mode == 0) FontStyle.Normal else FontStyle.Italic,
                fontSize = if (mode == 0) arSize.sp else trSize.sp,
                color = cs.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
              )
            }
          }
          when (mode) {
            0 -> {
              val inline = mutableMapOf<String, InlineTextContent>()
              val text = buildAnnotatedString {
                list.forEach { r ->
                  append(r.arabic)
                  append(" ")
                  val key = "a${r.surah}_${r.ayah}"
                  appendInlineContent(key, "﴿${r.ayah}﴾")
                  inline[key] = InlineTextContent(Placeholder(1.45.em, 1.45.em, PlaceholderVerticalAlign.TextCenter)) {
                    AyahMedallion(r.ayah, arSize)
                  }
                  append(" ")
                }
              }
              Text(
                text = text,
                inlineContent = inline,
                style = TextStyle(
                  fontFamily = Amiri,
                  fontSize = arSize.sp,
                  lineHeight = (arSize * 2.0).sp,
                  color = cs.onSurface,
                  textAlign = TextAlign.Justify,
                  textDirection = TextDirection.Rtl
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
              )
            }
            else -> list.forEach { r ->
              val body = if (mode == 1) (if (en) r.english else r.pronunciation) else r.meal
              if (body.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                  Box(
                    modifier = Modifier.padding(top = 2.dp).size(22.dp).clip(CircleShape).background(cs.primaryContainer),
                    contentAlignment = Alignment.Center
                  ) { Text("${r.ayah}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = cs.primary) }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = body,
                    fontSize = trSize.sp,
                    lineHeight = (trSize * 1.5).sp,
                    fontStyle = if (mode == 1) FontStyle.Italic else FontStyle.Normal,
                    color = if (mode == 1) cs.onSurfaceVariant else cs.onSurface
                  )
                }
              }
            }
          }
        }
      }
      // Alt: ‹ sayfa ›  (sağdan sola: ‹ sonraki, › önceki)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text("‹", fontSize = 22.sp, color = cs.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clip(CircleShape).clickable { onNext() }.padding(horizontal = 14.dp))
        Text(arabicDigits(page), fontFamily = Amiri, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        Text("›", fontSize = 22.sp, color = cs.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clip(CircleShape).clickable { onPrev() }.padding(horizontal = 14.dp))
      }
    }
  }
}

/** Ayet sonu: temanın renginde sekiz köşeli yıldız içinde Arapça ayet numarası. */
@Composable
private fun AyahMedallion(n: Int, arSize: Int) {
  val cs = MaterialTheme.colorScheme
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    val fill = cs.primaryContainer
    val line = cs.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
      val r = size.minDimension / 2f
      drawPath(star(center, r * 0.95f, 0.8f), fill)
      drawPath(star(center, r * 0.95f, 0.8f), line, style = Stroke(1.dp.toPx()))
      drawCircle(line, radius = r * 0.58f, center = center, style = Stroke(0.6.dp.toPx()))
    }
    Text(arabicDigits(n), fontFamily = Amiri, fontWeight = FontWeight.Bold, fontSize = (arSize * 0.52f).sp, color = cs.primary, lineHeight = (arSize * 0.6f).sp)
  }
}

/** Sure başı: çini/tezhip desenli şerit ve ortasında sure adı. */
@Composable
private fun SurahBand(nameArabic: String) {
  val cs = MaterialTheme.colorScheme
  val primary = cs.primary
  val onP = cs.onPrimary
  val sec = cs.secondary
  val tert = cs.tertiary
  val panel = if (isAppInDarkTheme()) cs.surface else lerp(cs.background, Color(0xFFFFFBF0), 0.6f)
  Box(modifier = Modifier.fillMaxWidth().height(46.dp).padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width; val h = size.height
      drawRoundRect(primary, cornerRadius = CornerRadius(6.dp.toPx()))
      drawRoundRect(onP.copy(alpha = 0.55f), topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(0.6.dp.toPx()))
      val fr = h * 0.2f
      listOf(0.08f, 0.18f, 0.82f, 0.92f).forEach { fx ->
        val c = Offset(w * fx, h / 2f)
        for (i in 0 until 6) {
          rotate(i * 60f, c) {
            drawOval(if (i % 2 == 0) onP else sec, topLeft = Offset(c.x - fr * 0.3f, c.y - fr * 1.45f), size = Size(fr * 0.6f, fr * 1.05f))
          }
        }
        drawCircle(tert, radius = fr * 0.5f, center = c)
        drawCircle(onP, radius = fr * 0.25f, center = c)
      }
      // Ortadaki sivri uçlu panel
      val l = w * 0.27f; val r = w * 0.73f; val t = h * 0.18f; val b = h * 0.82f
      val path = Path().apply {
        moveTo(l, t); lineTo(r, t); quadraticBezierTo(r + h * 0.35f, h / 2f, r, b); lineTo(l, b); quadraticBezierTo(l - h * 0.35f, h / 2f, l, t); close()
      }
      drawPath(path, panel)
      drawPath(path, onP, style = Stroke(0.8.dp.toPx()))
    }
    Text("سُورَةُ $nameArabic", fontFamily = Amiri, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = primary)
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
