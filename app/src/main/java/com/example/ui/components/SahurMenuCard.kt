package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SahurMenu
import com.example.i18n.AppLanguage
import com.example.i18n.t
import com.example.ui.theme.isAppInDarkTheme
import kotlin.math.cos
import kotlin.math.sin

/** Sivri (mihrap) kemerli kart biçimi. */
private val MihrabShape = GenericShape { size, _ ->
  val w = size.width
  val apex = 0f
  val side = w * 0.42f
  moveTo(0f, size.height - 24f)
  lineTo(0f, side)
  cubicTo(0f, side * 0.45f, w * 0.34f, side * 0.18f, w / 2f, apex)
  cubicTo(w * 0.66f, side * 0.18f, w, side * 0.45f, w, side)
  lineTo(w, size.height - 24f)
  quadraticBezierTo(w, size.height, w - 24f, size.height)
  lineTo(24f, size.height)
  quadraticBezierTo(0f, size.height, 0f, size.height - 24f)
  close()
}

/**
 * Günün sahur menü önerisi: seher vaktini anlatan gece tonlu mihrap kartı (hilal ve yıldızlar),
 * dört bölüm: sıcak başlangıç, protein, tam tahıl & sebze, içecek & meyve.
 */
@Composable
fun SahurMenuCard(menu: SahurMenu, imsakTime: String?, ramadanDay: Int?, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  val dark = isAppInDarkTheme()
  // Gece göğü: temanın ana rengini koyulaştırarak (her temada uyumlu)
  val skyTop = lerp(cs.primary, Color.Black, if (dark) 0.55f else 0.62f)
  val skyBottom = lerp(cs.primary, Color.Black, if (dark) 0.78f else 0.82f)
  val gold = if (dark) cs.primary else lerp(cs.primary, Color.White, 0.45f)
  val ink = Color(0xFFF3EEE2)
  val soft = Color(0xFFF3EEE2).copy(alpha = 0.72f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 32.dp, vertical = 10.dp)
      .testTag("sahur_menu_card"),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(6.dp, MihrabShape)
        .clip(MihrabShape)
        .background(Brush.verticalGradient(listOf(skyTop, skyBottom)))
        .border(1.5.dp, gold.copy(alpha = 0.7f), MihrabShape)
    ) {
      // Yıldızlar ve hilal
      Canvas(modifier = Modifier.matchParentSize()) {
        val rnd = java.util.Random(27)
        repeat(38) {
          val x = rnd.nextFloat() * size.width
          val y = rnd.nextFloat() * size.height * 0.55f
          drawCircle(ink.copy(alpha = 0.25f + rnd.nextFloat() * 0.5f), radius = 0.8f + rnd.nextFloat() * 1.8f, center = Offset(x, y))
        }
        val c = Offset(size.width * 0.84f, size.width * 0.36f)
        val r = size.width * 0.055f
        drawCircle(gold.copy(alpha = 0.18f), radius = r * 2.2f, center = c)
        drawCircle(gold, radius = r, center = c)
        drawCircle(skyTop, radius = r * 0.86f, center = Offset(c.x + r * 0.42f, c.y - r * 0.18f))
        // Küçük sekiz köşeli yıldız
        val s = Offset(size.width * 0.9f, size.width * 0.26f)
        val star = Path().apply {
          for (k in 0 until 16) {
            val a = Math.PI / 8 * k - Math.PI / 2
            val rr = if (k % 2 == 0) r * 0.45f else r * 0.2f
            val x = s.x + rr * cos(a).toFloat(); val y = s.y + rr * sin(a).toFloat()
            if (k == 0) moveTo(x, y) else lineTo(x, y)
          }
          close()
        }
        drawPath(star, gold)
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 34.dp, bottom = 20.dp, start = 22.dp, end = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Madalyon
        Box(
          modifier = Modifier
            .size(124.dp)
            .clip(CircleShape)
            .background(skyBottom.copy(alpha = 0.6f))
            .border(1.dp, gold.copy(alpha = 0.8f), CircleShape)
            .padding(6.dp)
            .border(0.5.dp, gold.copy(alpha = 0.45f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t("Günün"), fontSize = 11.sp, letterSpacing = 2.sp, color = gold, fontWeight = FontWeight.SemiBold)
            Text(t("Sahur"), fontFamily = FontFamily.Cursive, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ink)
            Text(
              t("Menü Önerisi") + (ramadanDay?.let { if (AppLanguage.isEnglish) " • Day $it" else " • $it. Gün" } ?: ""),
              fontSize = 10.sp, color = soft, textAlign = TextAlign.Center
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Course(t("SICAK BAŞLANGIÇ"), menu.warm.text, gold, ink)
        Course(t("PROTEİN"), menu.protein.text, gold, ink)
        Course(t("TAM TAHIL & SEBZE"), menu.grain.text, gold, ink)
        Course(t("İÇECEK & MEYVE"), menu.drink.text, gold, ink)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          (imsakTime?.let { if (AppLanguage.isEnglish) "Finish before imsak ($it)" else "İmsaktan ($it) önce bitirin" } ?: t("İmsaktan önce bitirin")) +
            " • " + t("Bol su için, tuzludan kaçının"),
          fontSize = 11.sp, color = soft, textAlign = TextAlign.Center,
          modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun Course(label: String, dish: String, accent: Color, ink: Color) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = accent)
    Text(dish, fontSize = 16.sp, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, color = ink, textAlign = TextAlign.Center)
  }
}

@Suppress("unused")
private val unusedRect = Rect.Zero
