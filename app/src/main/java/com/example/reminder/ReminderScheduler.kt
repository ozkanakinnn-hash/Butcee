package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppPreferences
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import java.util.Calendar

/** İftar ve sahur öncesi hatırlatıcılarını AlarmManager ile kurar. */
object ReminderScheduler {

  enum class Type(val requestCode: Int) { SAHUR(7101), IFTAR(7102) }

  const val EXTRA_TYPE = "reminder_type"
  const val EXTRA_TARGET_MILLIS = "reminder_target_millis"

  /** Namaz vakitleri yüklendiğinde çağrılır: vakitleri saklar ve alarmları yeniler. */
  fun onPrayerTimesLoaded(context: Context, prayerTimes: PrayerTimes) {
    val prefs = AppPreferences(context)
    prefs.lastImsak = PrayerTimeUtils.cleanTimeString(prayerTimes.imsak, prefs.lastImsak)
    prefs.lastAksam = PrayerTimeUtils.cleanTimeString(prayerTimes.aksam, prefs.lastAksam)
    rescheduleAll(context)
  }

  fun rescheduleAll(context: Context) {
    val prefs = AppPreferences(context)
    schedule(context, Type.SAHUR, prefs.remindBeforeImsak, prefs.lastImsak, prefs.remindBeforeImsakMinutes)
    schedule(context, Type.IFTAR, prefs.remindBeforeIftar, prefs.lastAksam, prefs.remindBeforeIftarMinutes)
  }

  private fun schedule(context: Context, type: Type, enabled: Boolean, targetTime: String, minutesBefore: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (!enabled) {
      alarmManager.cancel(pendingIntent(context, type, 0L))
      return
    }

    // Hedef vakit (bugün), hatırlatma zamanı geçmişse yarına kaydır
    val totalMin = PrayerTimeUtils.parseTimeToMinutes(targetTime)
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, totalMin / 60)
      set(Calendar.MINUTE, totalMin % 60)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val now = System.currentTimeMillis()
    var triggerAt = target.timeInMillis - minutesBefore * 60_000L
    if (triggerAt <= now + 30_000L) {
      target.add(Calendar.DAY_OF_MONTH, 1)
      triggerAt = target.timeInMillis - minutesBefore * 60_000L
    }

    val pi = pendingIntent(context, type, target.timeInMillis)
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    try {
      if (canExact) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
      } else {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
      }
    } catch (e: SecurityException) {
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
  }

  private fun pendingIntent(context: Context, type: Type, targetMillis: Long): PendingIntent {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
      action = "com.example.reminder.${type.name}"
      putExtra(EXTRA_TYPE, type.name)
      putExtra(EXTRA_TARGET_MILLIS, targetMillis)
    }
    return PendingIntent.getBroadcast(
      context, type.requestCode, intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }
}
