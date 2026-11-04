package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GreetingOccasion
import com.example.i18n.t
import com.example.util.GreetingCardRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

/** "Sevdiklerinize kart gönderin": Cuma, kandil, Ramazan ve bayram kartları. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun GreetingCardsScreen(
  onNavigateBack: () -> Unit,
  initial: GreetingOccasion? = null
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  var occasion by remember { mutableStateOf(initial ?: GreetingOccasion.forToday() ?: GreetingOccasion.CUMA) }
  val pagerState = rememberPagerState(pageCount = { occasion.messages.size })
  androidx.compose.runtime.LaunchedEffect(occasion) { pagerState.scrollToPage(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .testTag("greeting_cards_screen")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"), tint = MaterialTheme.colorScheme.primary)
      }
      Text(
        text = t("Özel Gün Kartları"),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    // Başlık
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = t("Sevdiklerinize"),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = t("kart gönderin"),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = t("Kandil, bayram ve cuma kartları"),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Kart önizlemeleri (yana kaydırılır; her sayfa farklı mesaj)
    HorizontalPager(
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 64.dp),
      pageSpacing = 12.dp,
      modifier = Modifier.fillMaxWidth()
    ) { page ->
      val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
      val message = occasion.messages[page]
      val bitmap by produceState<ImageBitmap?>(null, occasion, page) {
        value = withContext(Dispatchers.Default) {
          GreetingCardRenderer.render(context, occasion, message, 0.45f).asImageBitmap()
        }
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(9f / 16f)
          .graphicsLayer {
            val s = 1f - 0.1f * offset
            scaleX = s; scaleY = s
            rotationZ = -6f * (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
            alpha = 1f - 0.35f * offset
          }
          .clip(RoundedCornerShape(18.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        val bmp = bitmap
        if (bmp != null) {
          Image(bitmap = bmp, contentDescription = message.headline.text, modifier = Modifier.fillMaxSize())
        } else {
          CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        }
      }
    }

    if (occasion.messages.size > 1) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        repeat(occasion.messages.size) { i ->
          Box(
            modifier = Modifier
              .padding(horizontal = 3.dp)
              .size(if (pagerState.currentPage == i) 9.dp else 7.dp)
              .clip(CircleShape)
              .background(
                if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline
              )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Özel gün seçici
    FlowRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(22.dp))
        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
        .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      GreetingOccasion.entries.forEach { o ->
        val selected = o == occasion
        Text(
          text = o.label.text,
          fontSize = 13.sp,
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
              if (selected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            )
            .clickable { occasion = o }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("occasion_${o.name}")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val currentMessage = occasion.messages[pagerState.currentPage.coerceIn(0, occasion.messages.size - 1)]
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { GreetingCardRenderer.share(context, occasion, currentMessage) },
        modifier = Modifier.weight(1f).testTag("greeting_share"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(t("Paylaş"), fontWeight = FontWeight.Bold)
      }
      OutlinedButton(
        onClick = { GreetingCardRenderer.shareToInstagram(context, occasion, currentMessage) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(t("📸 Instagram Hikayesi"), fontSize = 13.sp, maxLines = 1)
      }
    }
    Spacer(modifier = Modifier.height(24.dp))
  }
}
