package com.example.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.IftarSoundChoice

object SoundManager {

  private var currentPlayer: MediaPlayer? = null

  fun getAssetPath(soundChoice: IftarSoundChoice): String {
    return when (soundChoice) {
      IftarSoundChoice.RAMAZAN_TOPU -> "audio/sound_ramadan_cannon.wav"
      IftarSoundChoice.SUFI_NEY -> "audio/sound_sufi_ney.wav"
    }
  }

  fun getRawResId(soundChoice: IftarSoundChoice): Int {
    return when (soundChoice) {
      IftarSoundChoice.RAMAZAN_TOPU -> R.raw.sound_ramadan_cannon
      IftarSoundChoice.SUFI_NEY -> R.raw.sound_sufi_ney
    }
  }

  /**
   * Uri used for system notification channels and vibration/sound bindings.
   */
  fun getSoundUri(context: Context, soundChoice: IftarSoundChoice): Uri {
    val resId = getRawResId(soundChoice)
    return Uri.parse("android.resource://${context.packageName}/$resId")
  }

  /**
   * Plays the royalty-free sound using assets/audio or res/raw.
   */
  fun playSound(context: Context, soundChoice: IftarSoundChoice, onComplete: (() -> Unit)? = null) {
    try {
      stopSound()
      val assetPath = getAssetPath(soundChoice)
      
      var player: MediaPlayer? = null
      try {
        val afd: AssetFileDescriptor = context.assets.openFd(assetPath)
        player = MediaPlayer().apply {
          setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
          afd.close()
          prepare()
        }
      } catch (e: Exception) {
        // Fallback to res/raw
        val resId = getRawResId(soundChoice)
        player = MediaPlayer.create(context, resId)
      }

      currentPlayer = player?.apply {
        setOnCompletionListener {
          stopSound()
          onComplete?.invoke()
        }
        start()
      }
    } catch (e: Exception) {
      Log.e("SoundManager", "Error playing sound: ${e.message}")
    }
  }

  fun stopSound() {
    try {
      currentPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.release()
      }
    } catch (e: Exception) {
      Log.e("SoundManager", "Error stopping sound: ${e.message}")
    } finally {
      currentPlayer = null
    }
  }

  fun isPlaying(): Boolean {
    return try {
      currentPlayer?.isPlaying == true
    } catch (e: Exception) {
      false
    }
  }
}
