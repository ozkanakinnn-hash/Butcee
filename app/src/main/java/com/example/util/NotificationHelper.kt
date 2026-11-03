package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
  /** Sistem varsayılan sesli tek kanal. (Eski özel sesli kanallar silinir.) */
  private const val CHANNEL_ID = "ramazan_rehberi_system"
  private const val CHANNEL_NAME = "Ramazan Rehberi & Namaz Vakitleri"

  @Suppress("UNUSED_PARAMETER")
  fun createNotificationChannel(context: Context, soundChoice: IftarSoundChoice = IftarSoundChoice.RAMAZAN_TOPU) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      // Önceki sürümlerdeki özel sesli kanalları kaldır
      IftarSoundChoice.entries.forEach { manager.deleteNotificationChannel("${BASE_CHANNEL_ID}_${it.name}") }

      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Namaz vakitleri, iftar ve sahur hatırlatıcıları"
        enableVibration(true)
        // setSound çağrılmıyor: sistem varsayılan bildirim sesi kullanılır
      }
      manager.createNotificationChannel(channel)
    }
  }

  /** Akşam ezanı sesli kanal (res/raw/ezan_aksam.mp3). Kanal sesi sonradan değişmez; ses değişirse ID değiştirilmeli. */
  private const val EZAN_CHANNEL_ID = "ramazan_rehberi_ezan_aksam_v1"

  fun ezanSoundUri(context: Context): android.net.Uri =
    android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.ezan_aksam}")

  private fun createEzanChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(EZAN_CHANNEL_ID, "Ezan (Akşam / İftar)", NotificationManager.IMPORTANCE_HIGH).apply {
        description = "Akşam ezanı vaktinde ezan sesiyle bildirim"
        enableVibration(true)
        setSound(
          ezanSoundUri(context),
          android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
      }
      manager.createNotificationChannel(channel)
    }
  }

  /** Ezan sesli bildirim (akşam / iftar vakti). */
  fun sendEzanNotification(context: Context, title: String, message: String, notificationId: Int) {
    createEzanChannel(context)
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val contentIntent = launchIntent?.let {
      PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    val builder = NotificationCompat.Builder(context, EZAN_CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_mosque)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_REMINDER)
      .setAutoCancel(true)
      .setSound(ezanSoundUri(context)) // Android 7 ve altı
      .setContentIntent(contentIntent)
    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (e: SecurityException) {
      // Android 13+ bildirim izni yok
    }
  }

  @Suppress("UNUSED_PARAMETER")
  fun sendNotification(
    context: Context,
    title: String,
    message: String,
    soundChoice: IftarSoundChoice = IftarSoundChoice.RAMAZAN_TOPU,
    notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
  ) {
    createNotificationChannel(context)

    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val contentIntent = launchIntent?.let {
      PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_mosque)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL) // sistem sesi + titreşimi
      .setContentIntent(contentIntent)

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
