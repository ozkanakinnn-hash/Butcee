package akinai.app1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Locale

/**
 * Canlı skor paneli: maç boyunca bildirim alanında SABİT duran, yerinde güncellenen bildirim.
 *  - Aynı maç için tek bildirim (kimlik = fixture_id); skor değişince yeni bildirim açılmaz, güncellenir.
 *  - Dakika telefonda kendi sayar (Chronometer) → sunucu her dakika veri göndermek zorunda değil.
 *  - Sessiz kanal (`live_score`); skor artınca panel bir kez "goal_alert" kanalından SESLİ uyarır (ayrı bildirim yok).
 *  - Maç bitince "MS" gösterir, kaydırılıp kapatılabilir ve 2 saat sonra kendiliğinden kalkar.
 *  - Ayarlar'daki "Canlı skor paneli" kapalıysa gösterilmez (Flutter SharedPreferences: flutter.liveScorePanel).
 */
object LiveScoreNotifier {
    const val TYPE = "match_live"
    // v2: kilit ekranında herkese açık (eski "live_score" kanalının görünürlüğü sonradan değiştirilemez)
    private const val CHANNEL = "live_score_v2"
    private const val GOAL_CHANNEL = "goal_alert" // Flutter oluşturur ("Goller", telefonun bildirim sesi)
    private const val MATCH_CHANNEL = "match" // Flutter oluşturur ("Maç durumu": kırmızı kart, devre arası, maç sonu)
    private const val KICKOFF_CHANNEL = "kickoff_whistle" // "Maç başladı" — düdük sesi (res/raw/whistle_kickoff)
    private val LIVE = setOf("1H", "2H", "ET", "LIVE")

    fun show(context: Context, d: Map<String, String>) {
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val fixtureId = d["fixture_id"] ?: return
        val lang = prefs.getString("flutter.language", null)
        val en = (if (lang == null || lang == "system") Locale.getDefault().language else lang).startsWith("en")
        ensureChannel(context, en)
        ensureKickoffChannel(context, en)
        val alert = d["alert"].orEmpty()
        val alertChannel = alertChannel(alert, d["alert_quiet"] == "1")
        // Panel kapalıysa: maç olayı (gol, kırmızı kart, devre, sonuç, başlama) normal bildirim olarak gösterilir.
        if (!prefs.getBoolean("flutter.liveScorePanel", true)) {
            if (alertChannel != null) showPlainAlert(context, fixtureId, alert, alertChannel, d)
            return
        }

        val status = d["status"] ?: "NS"
        val finished = d["finished"] == "1"
        val homeShort = d["home_short"].orEmpty()
        val awayShort = d["away_short"].orEmpty()
        val gh = d["gh"] ?: "0"
        val ga = d["ga"] ?: "0"

        val rv = RemoteViews(context.packageName, R.layout.notification_live_score)
        val big = RemoteViews(context.packageName, R.layout.notification_live_score_big)
        // Küçük ve geniş görünümün ortak alanları
        for (v in listOf(rv, big)) {
            v.setTextViewText(R.id.live_league, d["league"] ?: "Süper Lig")
            v.setTextViewText(R.id.live_home_short, homeShort)
            v.setTextViewText(R.id.live_away_short, awayShort)
            // Yeni sunucu: takım adı ayrı alanda (home_team); eski sunucu: home_name
            v.setTextViewText(R.id.live_home_name, d["home_team"] ?: d["home_name"].orEmpty())
            v.setTextViewText(R.id.live_away_name, d["away_team"] ?: d["away_name"].orEmpty())
            v.setInt(R.id.live_home_dot, "setColorFilter", parseColor(d["home_color"]))
            v.setInt(R.id.live_away_dot, "setColorFilter", parseColor(d["away_color"]))
            // Milli maç: renk noktası yerine bayrak (sunucu bayrak emojisi gönderir)
            for ((flag, dot, value) in listOf(
                Triple(R.id.live_home_flag, R.id.live_home_dot, d["home_flag"]),
                Triple(R.id.live_away_flag, R.id.live_away_dot, d["away_flag"]),
            )) {
                val hasFlag = !value.isNullOrEmpty()
                v.setTextViewText(flag, value.orEmpty())
                v.setViewVisibility(flag, if (hasFlag) View.VISIBLE else View.GONE)
                v.setViewVisibility(dot, if (hasFlag) View.GONE else View.VISIBLE)
            }
            v.setTextViewText(R.id.live_score, if (status == "NS") d["kickoff_text"] ?: "– : –" else "$gh – $ga")

            // Dakika: canlıysa Chronometer, değilse durum yazısı (İY, MS, …)
            if (status in LIVE) {
                val elapsedMin = (d["elapsed"] ?: "0").toLongOrNull() ?: 0L
                val sentMs = (d["sent_ms"] ?: "0").toLongOrNull() ?: System.currentTimeMillis()
                val lag = (System.currentTimeMillis() - sentMs).coerceIn(0L, 10 * 60_000L)
                val base = SystemClock.elapsedRealtime() - (elapsedMin * 60_000L + lag)
                v.setViewVisibility(R.id.live_minute, View.VISIBLE)
                v.setViewVisibility(R.id.live_status, View.GONE)
                v.setChronometer(R.id.live_minute, base, null, true)
            } else {
                v.setViewVisibility(R.id.live_minute, View.GONE)
                v.setViewVisibility(R.id.live_status, View.VISIBLE)
                v.setTextViewText(R.id.live_status, statusLabel(status, en))
            }
        }
        // Geniş görünüm: zaman çizelgesi (sunucu "taraf|dakika|metin" satırları gönderir; H solda, A sağda, S ayırıcı)
        val lines = d["timeline"].orEmpty().split('\n').filter { it.isNotBlank() }
        big.removeAllViews(R.id.live_events)
        for (line in lines) {
            val parts = line.split('|', limit = 3)
            if (parts.size < 3) continue
            val (side, minute, text) = parts
            if (side == "S") {
                val sep = RemoteViews(context.packageName, R.layout.notification_event_sep)
                sep.setTextViewText(R.id.ev_sep, "— $text —")
                big.addView(R.id.live_events, sep)
            } else {
                val row = RemoteViews(context.packageName, R.layout.notification_event_row)
                row.setTextViewText(R.id.ev_left, if (side == "H") text else "")
                row.setTextViewText(R.id.ev_min, minute)
                row.setTextViewText(R.id.ev_right, if (side == "A") text else "")
                big.addView(R.id.live_events, row)
            }
        }
        val hasEvents = lines.isNotEmpty()
        big.setViewVisibility(R.id.live_events, if (hasEvents) View.VISIBLE else View.GONE)
        big.setViewVisibility(R.id.live_events_divider, if (hasEvents) View.VISIBLE else View.GONE)

        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NewsNotifier.EXTRA_PAYLOAD, org.json.JSONObject(mapOf("type" to "match", "fixture_id" to fixtureId)).toString())
        }
        val pi = open?.let {
            PendingIntent.getActivity(
                context, fixtureId.hashCode(), it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        // Olay anı (gol, kırmızı kart, devre arası, maç sonu, başlama düdüğü): panel bir kez SESLİ uyarır — ayrı
        // bildirim kartı yok. Sarı kart ve diğer güncellemeler sessiz. Aynı uyarı tekrar gelirse çalmaz.
        val state = context.getSharedPreferences("live_score_state", Context.MODE_PRIVATE)
        val alertKey = "$alert|${d["alert_title"].orEmpty()}"
        val alertNow = alertChannel != null && state.getString("alert_$fixtureId", "") != alertKey
        if (alertNow) state.edit().putString("alert_$fixtureId", alertKey).apply()

        val builder = NotificationCompat.Builder(context, if (alertNow) alertChannel!! else CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_news)
            .setColor(Color.parseColor("#0B1F3A"))
            .setCustomContentView(rv)
            .setCustomBigContentView(big)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("$homeShort $gh – $ga $awayShort")
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setOnlyAlertOnce(!alertNow)
            // Kilit ekranında içeriğiyle görünsün (setSilent kullanılmaz: Samsung "sessiz" bildirimleri kilit
            // ekranında gizleyebilir; kanalın sesi zaten kapalı)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(!finished)
            .setAutoCancel(finished)
            .setPriority(if (alertNow) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
        if (pi != null) builder.setContentIntent(pi)
        if (finished) builder.setTimeoutAfter(2 * 60 * 60 * 1000L)
        // Uyarı anında açılır pencerede (heads-up) ve erişilebilirlik için olayın metni
        if (alertNow) builder.setTicker(d["alert_title"].orEmpty())

        try {
            NotificationManagerCompat.from(context).notify("live_$fixtureId", 7100, builder.build())
        } catch (_: SecurityException) {
            // Bildirim izni yok → sessizce geç
        }
    }

    /** Olay → ses kanalı. null = sessiz (sarı kart, 2. yarı, güncelleme). Gece (quiet) tüm olaylar sessiz. */
    private fun alertChannel(alert: String, quiet: Boolean): String? = if (quiet) null else when (alert) {
        "goal", "goal_cancelled" -> GOAL_CHANNEL
        "red_card", "halftime", "fulltime" -> MATCH_CHANNEL
        "kickoff" -> KICKOFF_CHANNEL
        else -> null
    }

    /** Panel kapalı kullanıcı için maç olayı: normal bildirim (aynı kanal ve ses). */
    private fun showPlainAlert(context: Context, fixtureId: String, alert: String, channel: String, d: Map<String, String>) {
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NewsNotifier.EXTRA_PAYLOAD, org.json.JSONObject(mapOf("type" to "match", "fixture_id" to fixtureId)).toString())
        }
        val b = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_news)
            .setColor(Color.parseColor("#0B1F3A"))
            .setContentTitle(d["alert_title"].orEmpty())
            .setContentText(d["alert_body"].orEmpty())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        open?.let {
            b.setContentIntent(PendingIntent.getActivity(context, fixtureId.hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
        try {
            NotificationManagerCompat.from(context).notify("match_${fixtureId}_$alert", 7101, b.build())
        } catch (_: SecurityException) {
        }
    }

    private fun statusLabel(status: String, en: Boolean): String = when (status) {
        "NS" -> if (en) "Kick-off" else "Başlamadı"
        "HT" -> if (en) "HT" else "İY"
        "BT" -> if (en) "Break" else "Ara"
        "P" -> if (en) "Penalties" else "Penaltılar"
        "FT" -> if (en) "FT" else "MS"
        "AET" -> if (en) "AET" else "UZ. MS"
        "PEN" -> if (en) "Pens" else "PEN. MS"
        "PST" -> if (en) "Postponed" else "Ertelendi"
        "CANC" -> if (en) "Cancelled" else "İptal"
        "SUSP", "INT" -> if (en) "Suspended" else "Durduruldu"
        else -> status
    }

    private fun parseColor(hex: String?): Int = try {
        Color.parseColor(if (hex.isNullOrBlank()) "#8E8E93" else hex)
    } catch (_: IllegalArgumentException) {
        Color.parseColor("#8E8E93")
    }

    private fun ensureChannel(context: Context, en: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.deleteNotificationChannel("live_score")
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val ch = NotificationChannel(
            CHANNEL,
            if (en) "Live score" else "Canlı skor",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = if (en) "Live score panel for your teams' matches" else "Takip ettiğin takımların maçlarında canlı skor paneli"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)
    }

    /** Maç başladı kanalı: uygulamanın düdük sesi (kanal sesi sonradan değiştirilemez; yeni ses = yeni kanal kimliği). */
    private fun ensureKickoffChannel(context: Context, en: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(KICKOFF_CHANNEL) != null) return
        val sound = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.whistle_kickoff}")
        val attrs = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val ch = NotificationChannel(KICKOFF_CHANNEL, if (en) "Kick-off whistle" else "Maç başladı (düdük)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = if (en) "Whistle when a match kicks off" else "Maç başlayınca hakem düdüğü"
            setSound(sound, attrs)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)
    }
}
