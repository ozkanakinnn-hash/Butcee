package com.example.radio

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
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
    mediaSession = MediaSession.Builder(this, player)
      .setCallback(SecureSessionCallback())
      .build()
  }

  /**
   * Güvenlik: servis Media3 gereği dışa açık (exported). Yalnızca bu uygulamanın, sistem arayüzünün
   * (bildirim/kilit ekranı medya kontrolleri), Bluetooth ve Android Auto'nun bağlanmasına izin verilir;
   * başka uygulamalar oynatıcıyı kontrol edemez. Oynatıcıya yalnızca izinli yayın sunucularındaki
   * adresler eklenebilir.
   */
  private inner class SecureSessionCallback : MediaSession.Callback {
    private val trustedPackages = setOf(
      "com.android.systemui", "com.android.bluetooth", "com.google.android.projection.gearhead"
    )

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
      val allowed = controller.packageName == packageName ||
        controller.packageName in trustedPackages ||
        session.isMediaNotificationController(controller)
      return if (allowed) super.onConnect(session, controller) else MediaSession.ConnectionResult.reject()
    }

    override fun onAddMediaItems(
      mediaSession: MediaSession,
      controller: MediaSession.ControllerInfo,
      mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
      val safe = mediaItems.filter { item ->
        val host = item.localConfiguration?.uri?.host ?: return@filter false
        item.localConfiguration?.uri?.scheme == "https" && ALLOWED_STREAM_HOSTS.any { host == it || host.endsWith(".$it") }
      }.toMutableList()
      return Futures.immediateFuture(safe)
    }
  }

  companion object {
    /** Radyo yayın adreslerinin bulunduğu sunucular (strings.xml'deki radio_*_url ile uyumlu olmalı). */
    val ALLOWED_STREAM_HOSTS = setOf("mediatriple.net")
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
