package com.nobet.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nobet.MainActivity
import com.nobet.R
import com.nobet.data.Store
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDur
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Vardiyaya duyarlı hatırlatıcılar. Her seferinde yalnızca EN YAKIN hatırlatıcı AlarmManager'a kurulur;
 * çalınca bildirim gösterilir ve bir sonraki kurulur (zincir). Veri değişince [reschedule] çağrılır.
 */
object Reminders {
  const val CHANNEL = "nobet_reminders"

  data class Event(val at: Long, val title: String, val body: String, val critical: Boolean)

  fun ensureChannel(ctx: Context) {
    val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
    val ch = NotificationChannel(CHANNEL, t("Vardiya hatırlatıcıları", "Shift reminders"), NotificationManager.IMPORTANCE_DEFAULT)
    ch.description = t("Vardiya, uyku ve enerji hatırlatmaları", "Shift, sleep and energy reminders")
    nm.createNotificationChannel(ch)
  }

  /** Önümüzdeki günlerdeki tüm aday hatırlatıcılar (zamana göre sıralı). */
  fun upcoming(now: LocalDateTime = LocalDateTime.now(Store.zone)): List<Event> {
    val out = mutableListOf<Event>()
    val today = now.toLocalDate()
    val smart = Store.premium && Store.remSmart
    val sleeping = Store.sleepStart > 0L

    for (i in -1..3) {
      val d = today.plusDays(i.toLong())
      val w = Store.workOn(d) ?: continue
      val name = typeName(w.type)
      val startTxt = fmtClock(w.type.startMin)
      if (Store.remShiftStart) {
        val lead = Store.remShiftLeadMin + if (w.type.isNight) 30 else 0
        out += Event(
          Store.toMs(w.start.minusMinutes(lead.toLong())),
          t("$name vardiyası yaklaşıyor", "$name shift coming up"),
          t("$name vardiyanız ${fmtDur(lead)} sonra ($startTxt) başlıyor.", "Your $name shift starts in ${fmtDur(lead)} ($startTxt)."),
          critical = true,
        )
      }
      if (!smart) continue
      if (w.type.isNight) {
        var before = w.start.minusHours(24)
        if (before.hour >= 22) before = before.toLocalDate().atTime(20, 0)
        else if (before.hour < 8) before = before.toLocalDate().minusDays(1).atTime(20, 0)
        out += Event(
          Store.toMs(before),
          t("Yarın gece vardiyası 🌙", "Night shift tomorrow 🌙"),
          t("Yarın $startTxt'de gece vardiyanız var — sabah biraz geç kalkmaya ve bu akşamı sakin geçirmeye çalışın.",
            "You have a night shift tomorrow at $startTxt — try to sleep in and keep tonight relaxed."),
          critical = false,
        )
        out += Event(
          Store.toMs(w.start.minusHours(4)),
          t("Vardiya öncesi kısa uyku", "Pre-shift nap"),
          t("$startTxt'de gece vardiyanız var. Şimdi 60–90 dakikalık bir kestirme dikkatinizi korumanıza yardımcı olur.",
            "Night shift at $startTxt. A 60–90 minute nap now can help you stay sharp."),
          critical = false,
        )
        out += Event(
          Store.toMs(w.end.plusMinutes(15)),
          t("Vardiya bitti 👏", "Shift done 👏"),
          t("Daha iyi uyumak için eve dönerken güneş gözlüğü takın, odanızı karanlık ve serin tutun.",
            "Keep your sunglasses on the way home and your room dark and cool for better sleep."),
          critical = false,
        )
      } else {
        // Ertesi sabah vardiyası: yatış = başlangıç − yol (1 sa) − hazırlık (30 dk) − uyku hedefi
        val bed = w.start.minusMinutes(90L + Store.sleepGoalMin)
        val bedTxt = fmtClock(bed.hour * 60 + bed.minute)
        out += Event(
          Store.toMs(bed.minusMinutes(45)),
          t("Yatma vakti yaklaşıyor", "Wind-down time"),
          t("Yarınki $name vardiyası için $bedTxt civarı yatmayı hedefleyin. Ekranları kısın, ışıkları azaltın.",
            "Aim for bed around $bedTxt for tomorrow's $name shift. Dim the screens and lights."),
          critical = false,
        )
        out += Event(
          Store.toMs(bed.minusHours(6)),
          t("Kahve için son çağrı ☕", "Last call for coffee ☕"),
          t("Şimdi alınan kafein $bedTxt'deki uykunuzu etkileyebilir.", "Caffeine now may affect your sleep at $bedTxt."),
          critical = false,
        )
      }
    }

    if (Store.remCheckIn) {
      for (i in 0..1) {
        val at = today.plusDays(i.toLong()).atStartOfDay().plusMinutes(Store.remCheckInMin.toLong())
        out += Event(Store.toMs(at), t("Bugün enerjiniz nasıl?", "How's your energy today?"), t("Sadece 5 saniye sürer.", "It takes 5 seconds."), false)
      }
    }
    if (Store.remForgot && sleeping) {
      out += Event(
        Store.sleepStart + 14 * 3600_000L,
        t("Uyku kaydı hâlâ açık", "Sleep timer still running"),
        t("Uyandığınızı kaydetmeyi unuttunuz mu?", "Did you forget to log that you woke up?"),
        true,
      )
    }
    val nowMs = Store.toMs(now)
    return out
      .filter { it.at > nowMs && it.at > Store.lastFired }
      .filter { it.critical || !sleeping }
      .sortedBy { it.at }
  }

  fun reschedule(ctx: Context) {
    val am = ctx.getSystemService(AlarmManager::class.java) ?: return
    val intent = Intent(ctx, ReminderReceiver::class.java)
    val next = upcoming().firstOrNull()
    if (next != null) {
      intent.putExtra("at", next.at).putExtra("title", next.title).putExtra("body", next.body)
    }
    val pi = PendingIntent.getBroadcast(ctx, 7, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    am.cancel(pi)
    if (next == null) return
    try {
      if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.at, pi)
      } else {
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.at, pi)
      }
    } catch (_: SecurityException) {
      am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.at, pi)
    }
  }

  fun canNotify(ctx: Context): Boolean =
    Build.VERSION.SDK_INT < 33 ||
      ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

  fun show(ctx: Context, title: String, body: String, id: Int = (System.currentTimeMillis() % 100000).toInt()) {
    ensureChannel(ctx)
    if (!canNotify(ctx)) return
    val open = PendingIntent.getActivity(
      ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val n = NotificationCompat.Builder(ctx, CHANNEL)
      .setSmallIcon(R.drawable.ic_stat_moon)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setContentIntent(open)
      .setAutoCancel(true)
      .build()
    try {
      NotificationManagerCompat.from(ctx).notify(id, n)
    } catch (_: SecurityException) {
    }
  }

  fun describeNext(): String? {
    val e = upcoming().firstOrNull() ?: return null
    val at = Store.toLdt(e.at)
    val days = ChronoUnit.DAYS.between(Store.today(), at.toLocalDate())
    val day = when (days) {
      0L -> t("Bugün", "Today")
      1L -> t("Yarın", "Tomorrow")
      else -> at.toLocalDate().toString()
    }
    return "$day ${fmtClock(at.hour * 60 + at.minute)} · ${e.title}"
  }
}

class ReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Store.init(context)
    val at = intent.getLongExtra("at", 0L)
    val title = intent.getStringExtra("title")
    val body = intent.getStringExtra("body")
    if (title != null && body != null) Reminders.show(context, title, body)
    if (at > 0) Store.lastFired = at
    Store.save()
    Reminders.reschedule(context)
  }
}

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Store.init(context)
    Reminders.reschedule(context)
  }
}
