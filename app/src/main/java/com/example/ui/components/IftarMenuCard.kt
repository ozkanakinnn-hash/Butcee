package com.example.ui.components

import com.example.i18n.t
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RamadanMenu

/** Üstü kemerli (yarım daire) menü kartı biçimi — restoran menü kartlarından esinlenildi. */
private val ArchShape = GenericShape { size, _ ->
  val r = size.width / 2f
  moveTo(0f, r)
  arcTo(Rect(0f, 0f, size.width, size.width), 180f, 180f, false)
  lineTo(size.width, size.height - 24f)
  quadraticBezierTo(size.width, size.height, size.width - 24f, size.height)
  lineTo(24f, size.height)
  quadraticBezierTo(0f, size.height, 0f, size.height - 24f)
  close()
}

@Composable
fun IftarMenuCard(
  menu: RamadanMenu,
  onViewAllMenus: () -> Unit,
  ramadanDay: Int? = null, // Ramazan'da kaçıncı gün; Ramazan dışında null
  modifier: Modifier = Modifier
) {
  val accent = MaterialTheme.colorScheme.primary
  val paper = MaterialTheme.colorScheme.surface
  val ink = MaterialTheme.colorScheme.onSurface
  val leaf = MaterialTheme.colorScheme.secondary

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 40.dp, vertical = 10.dp)
      .testTag("daily_menu_card"),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, ArchShape)
        .clip(ArchShape)
        .background(paper)
        .border(1.5.dp, accent.copy(alpha = 0.55f), ArchShape)
    ) {
      // Köşe ve kenar süslemeleri (zeytin dalı yaprakları)
      Canvas(modifier = Modifier.matchParentSize()) {
        drawBranch(Offset(size.width * 0.06f, size.width * 0.62f), -35f, leaf)
        drawBranch(Offset(size.width * 0.94f, size.width * 0.62f), 215f, leaf)
        drawBranch(Offset(size.width * 0.10f, size.height - 30f), -70f, leaf.copy(alpha = 0.6f))
        drawBranch(Offset(size.width * 0.90f, size.height - 30f), 250f, leaf.copy(alpha = 0.6f))
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 26.dp, bottom = 18.dp, start = 22.dp, end = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // "Menü" madalyonu
        Box(
          modifier = Modifier
            .size(118.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, accent.copy(alpha = 0.7f), CircleShape)
            .padding(6.dp)
            .border(0.5.dp, accent.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = t(if (ramadanDay != null) "İftar" else "Günün"),
              fontSize = 11.sp,
              letterSpacing = 2.sp,
              color = accent,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = t("Menü"),
              fontFamily = FontFamily.Cursive,
              fontSize = 34.sp,
              fontWeight = FontWeight.Bold,
              color = ink
            )
            Text(
              text = if (ramadanDay != null) (if (com.example.i18n.AppLanguage.isEnglish) "Day $ramadanDay" else "$ramadanDay. Gün") else t("Bugün"),
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MenuCourse(t("ÇORBA"), menu.soup, accent, ink)
        MenuCourse(t("ANA YEMEK"), menu.mainDish, accent, ink)
        MenuCourse(t("YAN YEMEK & SALATA"), menu.sideDish, accent, ink)
        MenuCourse(t("TATLI"), menu.dessert, accent, ink)

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = t(if (ramadanDay != null) "Ramazan Rehberi & tüm menüler ›" else "Tüm menüleri gör ›"),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = accent,
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onViewAllMenus() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("view_all_menus")
        )
      }
    }
  }
}

@Composable
private fun MenuCourse(label: String, dish: String, accent: Color, ink: Color) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      letterSpacing = 1.5.sp,
      fontWeight = FontWeight.Bold,
      fontStyle = FontStyle.Italic,
      color = accent
    )
    Text(
      text = dish,
      fontSize = 16.sp,
      fontFamily = FontFamily.Serif,
      fontStyle = FontStyle.Italic,
      color = ink,
      textAlign = TextAlign.Center
    )
  }
}

/** Yapraklı küçük dal (zeytin dalı) süsü. */
private fun DrawScope.drawBranch(origin: Offset, angle: Float, color: Color) {
  val len = size.width * 0.16f
  rotate(degrees = angle, pivot = origin) {
    drawLine(color, origin, Offset(origin.x + len, origin.y), strokeWidth = 2f)
    for (i in 1..4) {
      val x = origin.x + len * i / 5f
      val leafLen = len * 0.22f
      listOf(-1f, 1f).forEach { side ->
        val path = Path().apply {
          moveTo(x, origin.y)
          quadraticBezierTo(x + leafLen * 0.5f, origin.y + side * leafLen * 0.55f, x + leafLen, origin.y + side * leafLen * 0.15f)
          quadraticBezierTo(x + leafLen * 0.4f, origin.y + side * leafLen * 0.05f, x, origin.y)
          close()
        }
        drawPath(path, color.copy(alpha = color.alpha * 0.85f))
      }
    }
  }
}
