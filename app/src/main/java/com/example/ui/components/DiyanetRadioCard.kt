package com.example.ui.components

import com.example.i18n.t
import android.content.ComponentName
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.R
import com.example.radio.RadioPlaybackService

private data class RadioChannel(val id: String, val title: String, val urlRes: Int)

private val allChannels = listOf(
  RadioChannel("diyanet", "Diyanet Radyo", R.string.radio_diyanet_url),
  RadioChannel("kuran", "Kur'an Radyo", R.string.radio_diyanet_kuran_url),
  RadioChannel("risalet", "Risalet Radyo", R.string.radio_risalet_url)
)

/** İngilizce seçiliyse yalnızca Kur'an Radyo gösterilir. */
private val channels: List<RadioChannel>
  get() = if (com.example.i18n.AppLanguage.isEnglish) allChannels.filter { it.id == "kuran" } else allChannels

/** Ana sayfadaki canlı radyo kartı: oynat / durdur, arka planda çalmaya devam eder. */
@Composable
fun DiyanetRadioCard(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var controller by remember { mutableStateOf<MediaController?>(null) }
  var isPlaying by remember { mutableStateOf(false) }
  var isBuffering by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var selected by remember { mutableStateOf(channels.first()) }
  // Dil değiştiyse ve seçili kanal artık listede yoksa ilk kanala geç
  if (selected !in channels) selected = channels.first()

  DisposableEffect(Unit) {
    val token = SessionToken(context, ComponentName(context, RadioPlaybackService::class.java))
    val future = MediaController.Builder(context, token).buildAsync()
    val listener = object : Player.Listener {
      // Her olay grubundan sonra durumu doğrudan oynatıcıdan oku (tek tek geri çağrılara
      // güvenmek, durdurma sonrası arayüzün "çalıyor" olarak takılı kalmasına yol açıyordu).
      override fun onEvents(player: Player, events: Player.Events) {
        isPlaying = player.isPlaying
        isBuffering = player.playWhenReady && player.playbackState == Player.STATE_BUFFERING
        if (player.playbackState == Player.STATE_READY) error = null
      }
      override fun onPlayerError(e: PlaybackException) {
        error = if (com.example.i18n.AppLanguage.isEnglish) "Could not connect (${e.errorCodeName}). Check your internet connection and try again."
          else "Yayına bağlanılamadı (${e.errorCodeName}). İnternet bağlantınızı kontrol edip tekrar deneyin."
        isPlaying = false
        isBuffering = false
      }
    }
    future.addListener({
      runCatching { future.get() }.getOrNull()?.let { c ->
        controller = c
        c.addListener(listener)
        // Uygulama yeniden açıldığında arka planda çalan yayını göster
        isPlaying = c.isPlaying
        isBuffering = c.playWhenReady && c.playbackState == Player.STATE_BUFFERING
        c.currentMediaItem?.mediaId?.let { id -> channels.firstOrNull { it.id == id }?.let { selected = it } }
      }
    }, ContextCompat.getMainExecutor(context))
    onDispose {
      controller?.removeListener(listener)
      MediaController.releaseFuture(future)
      controller = null
    }
  }

  fun play(channel: RadioChannel) {
    val c = controller ?: return
    error = null
    val item = MediaItem.Builder()
      .setMediaId(channel.id)
      .setUri(context.getString(channel.urlRes))
      .setMimeType(MimeTypes.APPLICATION_M3U8)
      .setMediaMetadata(
        MediaMetadata.Builder()
          .setTitle(t(channel.title))
          .setArtist(t("Canlı Yayın"))
          .build()
      )
      .build()
    c.setMediaItem(item)
    c.prepare()
    c.play()
    isBuffering = true
  }

  // DİKKAT: `controller?.run { stop() }` yazılmamalı — run bloğu içinde `stop()` bu yerel
  // fonksiyona çözülür ve kendini sonsuza dek çağırır (StackOverflowError ile çökme).
  fun stopPlayback() {
    val c = controller ?: return
    c.stop()
    c.clearMediaItems()
    isPlaying = false
    isBuffering = false
  }

  val active = isPlaying || isBuffering

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("diyanet_radio_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Oynat / Durdur düğmesi
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = controller != null) { if (active) stopPlayback() else play(selected) }
            .testTag("radio_play_button"),
          contentAlignment = Alignment.Center
        ) {
          when {
            isBuffering -> CircularProgressIndicator(
              modifier = Modifier.size(26.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.5.dp
            )
            isPlaying -> Icon(Icons.Default.Stop, contentDescription = t("Durdur"), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
            else -> Icon(Icons.Default.PlayArrow, contentDescription = t("Dinle"), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
          }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Radio, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(t(selected.title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          }
          Spacer(modifier = Modifier.height(2.dp))
          when {
            error != null -> Text(error!!, fontSize = 11.sp, color = Color(0xFFD9534F))
            isBuffering -> Text(t("Bağlanıyor…"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            isPlaying -> LiveBadge()
            else -> Text(t("Canlı yayını dinlemek için dokunun"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        channels.forEach { ch ->
          val isSel = ch == selected
          Text(
            text = t(ch.title),
            fontSize = 11.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable {
                if (ch != selected) {
                  selected = ch
                  if (active) play(ch) // çalarken kanal değişirse yeni kanala geç
                }
              }
              .padding(vertical = 8.dp, horizontal = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        t("Uygulamadan çıksanız da yayın bildirim alanından kontrol edilebilir."),
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun LiveBadge() {
  val transition = rememberInfiniteTransition(label = "live_pulse")
  val pulse by transition.animateFloat(
    initialValue = 0.3f, targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
  )
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .alpha(pulse)
        .clip(CircleShape)
        .background(Color(0xFFE53935))
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(t("CANLI • Şu an çalıyor"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
  }
}
