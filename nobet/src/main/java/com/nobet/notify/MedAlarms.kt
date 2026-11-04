package com.nobet.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nobet.MainActivity
import com.nobet.R
import com.nobet.data.Meds
import com.nobet.data.Store
import com.nobet.i18n.fmtClock
import com.nobet.i18n.t
import java.time.LocalDateTime

/**
 * İlaç hatırlatma zinciri. Her doz için: saatinde bildirim (k=0); onaylanmazsa 5 dk sonra tekrar (k=1..2);
 * 3. ertelemede (k=3, saat + 15 dk) zorunlu ilaçlarda KRİTİK uyarı. Her seferinde yalnızca en yakın olay kurulur.
 */
object MedAlarms {
  const val CHANNEL = "nobet_meds"
  const val CHANNEL_CRITICAL = "nobet_meds_critical"

  data class Fire(val at: Long, val key: String, val k: Int)

  fun ensureChannels(ctx: Context) {
    val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
    nm.createNotificationChannel(
      NotificationChannel(CHANNEL, t("İlaç hatırlatmaları", "Medicine reminders"), NotificationManager.IMPORTANCE_HIGH).apply {
        description = t("İlaç saatlerinde bildirim", "Notifications at medicine times")
      },
    )
    nm.createNotificationChannel(
      NotificationChannel(CHANNEL_CRITICAL, t("Kritik ilaç uyarıları", "Critical medicine alerts"), NotificationManager.IMPORTANCE_HIGH).apply {
        description = t("Zorunlu ilaç alınmadığında", "When a required medicine was not taken")
        enableVibration(true)
      },
    )
  }

  fun next(now: LocalDateTime = LocalDateTime.now()): Fire? {
    val nowMs = Store.toMs(now)
    var best: Fire? = null
    for (i in -1..1) {
      Meds.dosesOn(now.toLocalDate().plusDays(i.toLong())).forEach { d ->
        if (Meds.isTaken(d)) return@forEach
        for (k in 0..Meds.MAX_SNOOZES) {
          val at = Store.toMs(d.at.plusMinutes((k * Meds.SNOOZE_MIN).toLong()))
          if (at > nowMs && at > Meds.lastFired && (best == null || at < best!!.at)) best = Fire(at, d.key, k)
        }
      }
    }
    return best
  }

  fun reschedule(ctx: Context) {
    val am = ctx.getSystemService(AlarmManager::class.java) ?: return
    val f = next()
    val intent = Intent(ctx, MedAlarmReceiver::class.java)
    if (f != null) intent.putExtra("at", f.at).putExtra("key", f.key).putExtra("k", f.k)
    val pi = PendingIntent.getBroadcast(ctx, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    am.cancel(pi)
    if (f == null) return
    try {
      if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, f.at, pi)
      } else {
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, f.at, pi)
      }
    } catch (_: SecurityException) {
      am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, f.at, pi)
    }
  }

  fun canExact(ctx: Context): Boolean {
    val am = ctx.getSystemService(AlarmManager::class.java) ?: return false
    return Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
  }

  private fun notifId(key: String) = 50_000 + (key.hashCode() and 0xFFFF)

  fun cancel(ctx: Context, key: String) {
    NotificationManagerCompat.from(ctx).cancel(notifId(key))
  }

  /** Doz bildirimi: "Aldım" ve "5 dk ertele" eylemleriyle. */
  fun show(ctx: Context, key: String, k: Int) {
    ensureChannels(ctx)
    if (!Reminders.canNotify(ctx)) return
    val parts = key.split("|")
    val med = Meds.meds.firstOrNull { it.id == parts.getOrNull(1) } ?: return
    val minute = parts.getOrNull(2)?.toIntOrNull() ?: 0
    val who = if (Meds.forWhom != "self" && Meds.forWhom.isNotEmpty() && Meds.personName.isNotBlank()) " (${Meds.personName.trim()})" else ""
    val open = PendingIntent.getActivity(
      ctx, notifId(key), Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val critical = k >= Meds.MAX_SNOOZES
    val title: String
    val body: String
    if (critical) {
      title = t("⚠ KRİTİK UYARI — hayati risk", "⚠ CRITICAL ALERT — vital risk")
      body = t("${med.name}$who ${fmtClock(minute)} dozu alınmadı. Lütfen hemen ilacınızı alın.",
        "${med.name}$who ${fmtClock(minute)} dose was not taken. Please take your medicine now.")
    } else {
      title = if (k == 0) t("İlaç zamanı 💊", "Medicine time 💊") else t("Hatırlatma ($k/${Meds.MAX_SNOOZES}) 💊", "Reminder ($k/${Meds.MAX_SNOOZES}) 💊")
      body = "${med.name}$who" + (if (med.dose.isNotBlank()) " · ${med.dose}" else "") + " · ${fmtClock(minute)}" +
        if (med.critical) t(" · ZORUNLU", " · REQUIRED") else ""
    }
    val b = NotificationCompat.Builder(ctx, if (critical) CHANNEL_CRITICAL else CHANNEL)
      .setSmallIcon(R.drawable.ic_stat_moon)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setContentIntent(open)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(if (critical) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
      .setAutoCancel(true)
      .addAction(0, t("Aldım", "Taken"), action(ctx, key, "taken"))
    if (!critical) b.addAction(0, t("5 dk ertele", "Snooze 5 min"), action(ctx, key, "snooze"))
    if (med.critical) b.setColor(0xFFC8605A.toInt())
    try {
      NotificationManagerCompat.from(ctx).notify(notifId(key), b.build())
    } catch (_: SecurityException) {
    }
  }

  private fun action(ctx: Context, key: String, act: String): PendingIntent {
    val i = Intent(ctx, MedActionReceiver::class.java).putExtra("key", key).putExtra("act", act)
    return PendingIntent.getBroadcast(ctx, notifId(key) * 2 + if (act == "taken") 1 else 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  }
}

/** Zincirdeki bir olay çaldığında: alınmadıysa bildirim (ya da kritik uyarı) göster, sonrakini kur. */
class MedAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Store.init(context)
    val key = intent.getStringExtra("key")
    val k = intent.getIntExtra("k", 0)
    val at = intent.getLongExtra("at", 0L)
    if (key != null && !Meds.taken.containsKey(key)) {
      val med = Meds.meds.firstOrNull { it.id == key.split("|").getOrNull(1) }
      if (med != null) {
        if (k < Meds.MAX_SNOOZES || med.critical) {
          MedAlarms.show(context, key, k)
        } else {
          MedAlarms.cancel(context, key)
        }
      }
    }
    if (at > 0) Meds.lastFired = at
    Meds.save()
    MedAlarms.reschedule(context)
  }
}

/** Bildirim eylemleri: "Aldım" dozu işaretler; "5 dk ertele" bildirimi kapatır (zincir 5 dk sonra tekrar hatırlatır). */
class MedActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Store.init(context)
    val key = intent.getStringExtra("key") ?: return
    if (intent.getStringExtra("act") == "taken") Meds.markTaken(key)
    MedAlarms.cancel(context, key)
    MedAlarms.reschedule(context)
  }
}
