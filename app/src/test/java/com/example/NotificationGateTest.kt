package com.example

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.reminder.ReminderReceiver
import com.example.reminder.ReminderScheduler
import com.example.reminder.ReminderScheduler.Type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** "Tüm Vakit Bildirimleri" kapalıyken hiçbir vakit bildirimi gösterilmemeli (test kullanıcısı bildirimi). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationGateTest {
  private val ctx: Application = ApplicationProvider.getApplicationContext()
  private val prefs = AppPreferences(ctx)

  private fun fire(type: Type) {
    ReminderReceiver().onReceive(ctx, Intent().apply {
      putExtra(ReminderScheduler.EXTRA_TYPE, type.name)
      putExtra(ReminderScheduler.EXTRA_TARGET_MILLIS, System.currentTimeMillis())
    })
  }

  private fun posted() = shadowOf(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).allNotifications.size

  @Test fun masterOff_disablesEverythingExceptWizardTest() {
    prefs.notificationsEnabled = false
    prefs.remindBeforeIftar = true; prefs.notifyOgle = true; prefs.notifyCuma = true
    Type.values().filter { it != Type.TEST }.forEach { assertFalse(it.name, ReminderScheduler.isEnabled(prefs, it)) }
    assertTrue(ReminderScheduler.isEnabled(prefs, Type.TEST))
  }

  @Test fun masterOn_followsPerPrayerSwitch() {
    prefs.notificationsEnabled = true
    prefs.notifyOgle = true; prefs.notifyYatsi = false
    assertTrue(ReminderScheduler.isEnabled(prefs, Type.PRAYER_OGLE))
    assertFalse(ReminderScheduler.isEnabled(prefs, Type.PRAYER_YATSI))
  }

  @Test fun staleAlarm_withMasterOff_postsNothing() {
    prefs.notificationsEnabled = false
    fire(Type.PRAYER_OGLE)
    fire(Type.PRAYER_YATSI)
    assertEquals(0, posted())
  }

  @Test fun alarm_withMasterOn_postsEzan() {
    prefs.notificationsEnabled = true
    prefs.notifyOgle = true
    fire(Type.PRAYER_OGLE)
    assertEquals(1, posted())
  }
}
