package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.IftarSoundChoice

object NotificationHelper {

  private const val BASE_CHANNEL_ID = "ramazan_rehberi_channel"
  private const val CHANNEL_NAME = "Ramazan Rehberi & Namaz Vakitleri"

  fun createNotificationChannel(context: Context, soundChoice: IftarSoundChoice = IftarSoundChoice.RAMAZAN_TOPU) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val soundUri = SoundManager.getSoundUri(context, soundChoice)
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()

      val channelId = "${BASE_CHANNEL_ID}_${soundChoice.name}"
      val channel = NotificationChannel(
        channelId,
        "$CHANNEL_NAME (${soundChoice.title})",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Namaz vakitleri, iftar topu ve sahur bildirimleri"
        enableLights(true)
        enableVibration(true)
        setSound(soundUri, audioAttributes)
      }

      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun sendNotification(
    context: Context,
    title: String,
    message: String,
    soundChoice: IftarSoundChoice = IftarSoundChoice.RAMAZAN_TOPU,
    notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
  ) {
    createNotificationChannel(context, soundChoice)

    // Trigger haptic vibration pattern for prayer / cannon
    triggerCannonHaptics(context)

    // Also play audio in foreground if active
    SoundManager.playSound(context, soundChoice)

    val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      "${BASE_CHANNEL_ID}_${soundChoice.name}"
    } else {
      BASE_CHANNEL_ID
    }

    val soundUri = SoundManager.getSoundUri(context, soundChoice)

    val builder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setSound(soundUri)
      .setVibrate(longArrayOf(0, 300, 150, 600, 200, 400))

    try {
      with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
      }
    } catch (e: SecurityException) {
      // Permission not granted on Android 13+
    }
  }

  fun triggerCannonHaptics(context: Context) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        val effect = VibrationEffect.createWaveform(
          longArrayOf(0, 180, 80, 400, 120, 250),
          intArrayOf(0, 255, 0, 220, 0, 180),
          -1
        )
        vibrator?.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 500), -1)
          )
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(longArrayOf(0, 200, 100, 500), -1)
        }
      }
    } catch (e: Exception) {
      // Ignore haptic errors on unsupported hardware
    }
  }
}
