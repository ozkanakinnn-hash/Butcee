package com.example.radio

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Canlı radyoyu arka planda çalan servis. Media3, çalarken bildirim alanında
 * oynat/durdur kontrollü bir medya bildirimi gösterir; uygulama kapansa da yayın sürer.
 */
class RadioPlaybackService : MediaSessionService() {

  private var mediaSession: MediaSession? = null

  override fun onCreate() {
    super.onCreate()
    val player = ExoPlayer.Builder(this)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(C.USAGE_MEDIA)
          .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
          .build(),
        /* handleAudioFocus = */ true
      )
      .setHandleAudioBecomingNoisy(true) // kulaklık çıkarılınca durdur
      .setWakeMode(C.WAKE_MODE_NETWORK)
      .build()
    mediaSession = MediaSession.Builder(this, player).build()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

  override fun onTaskRemoved(rootIntent: Intent?) {
    // Uygulama son kullanılanlardan kaldırıldığında: çalmıyorsa servisi kapat
    val player = mediaSession?.player
    if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
      stopSelf()
    }
  }

  override fun onDestroy() {
    mediaSession?.run {
      player.release()
      release()
    }
    mediaSession = null
    super.onDestroy()
  }
}
