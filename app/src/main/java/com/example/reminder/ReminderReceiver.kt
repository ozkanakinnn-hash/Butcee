package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppPreferences
import com.example.data.RamadanCalendar
import com.example.util.NotificationHelper
import java.util.Calendar

/** Alarm çaldığında: hedef gün oruç günüyse motive edici bildirim gönderir, ertesi günü kurar. */
class ReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val prefs = AppPreferences(context)
    val type = runCatching { ReminderScheduler.Type.valueOf(intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: "") }.getOrNull()
    val targetMillis = intent.getLongExtra(ReminderScheduler.EXTRA_TARGET_MILLIS, 0L)

    if (type != null && targetMillis > 0L) {
      val targetDay = Calendar.getInstance().apply { timeInMillis = targetMillis }
      val dayKey = RamadanCalendar.dateKey(targetDay)
      val isFastingDay = RamadanCalendar.isRamadanDay(dayKey) || prefs.getFastingDates().contains(dayKey)

      if (isFastingDay) {
        val (title, message) = when (type) {
          ReminderScheduler.Type.SAHUR ->
            "🌙 Sahura ${prefs.remindBeforeImsakMinutes} dakika kaldı" to MotivationMessages.randomSahur()
          ReminderScheduler.Type.IFTAR ->
            "🍽️ İftara ${prefs.remindBeforeIftarMinutes} dakika kaldı" to MotivationMessages.randomIftar()
        }
        NotificationHelper.sendNotification(context, title, message, notificationId = type.requestCode)
      }
    }

    // Bir sonraki günün hatırlatıcısını kur
    ReminderScheduler.rescheduleAll(context)
  }
}

/** Cihaz yeniden başladığında veya uygulama güncellendiğinde alarmları yeniden kurar. */
class ReminderBootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
        ReminderScheduler.rescheduleAll(context)
    }
  }
}
