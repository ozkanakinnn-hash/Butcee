package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.data.IftarSoundChoice

/**
 * Şimdilik tüm sesler cihazın varsayılan bildirim sesidir.
 * Özel ses dosyaları eklendiğinde [getSoundUri] içinde seçeneğe göre res/raw kaynağı döndürülecek.
 */
object SoundManager {

  private var currentPlayer: MediaPlayer? = null

  /** Bildirim kanalı ve önizleme için ses adresi: sistem varsayılan bildirim sesi. */
  @Suppress("UNUSED_PARAMETER")
  fun getSoundUri(context: Context, soundChoice: IftarSoundChoice): Uri =
    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

  fun playSound(context: Context, soundChoice: IftarSoundChoice, onComplete: (() -> Unit)? = null) {
    try {
      stopSound()
      currentPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        setDataSource(context, getSoundUri(context, soundChoice))
        setOnCompletionListener {
          stopSound()
          onComplete?.invoke()
        }
        prepare()
        start()
      }
    } catch (e: Exception) {
      Log.e("SoundManager", "Error playing sound: ${e.message}")
      onComplete?.invoke()
    }
  }

  fun stopSound() {
    try {
      currentPlayer?.let { player ->
        if (player.isPlaying) player.stop()
        player.release()
      }
    } catch (e: Exception) {
      Log.e("SoundManager", "Error stopping sound: ${e.message}")
    } finally {
      currentPlayer = null
    }
  }

  fun isPlaying(): Boolean = try {
    currentPlayer?.isPlaying == true
  } catch (e: Exception) {
    false
  }
}
