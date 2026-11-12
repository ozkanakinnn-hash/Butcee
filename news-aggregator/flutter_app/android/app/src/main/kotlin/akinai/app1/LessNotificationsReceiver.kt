package akinai.app1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

/**
 * Bildirimdeki "Daha az bildirim" düğmesi: seviyeyi "Sadece önemli"ye indirir.
 * Ayar Flutter'ın SharedPreferences dosyasına yazılır (uygulama öne gelince okur) ve konu abonelikleri hemen
 * değiştirilir; uygulama açılmasa da bir sonraki bildirimden itibaren geçerlidir.
 */
class LessNotificationsReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION = "akinai.app1.LESS_NOTIFICATIONS"
        const val EXTRA_TAG = "tag"
        const val EXTRA_ID = "id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        prefs.edit().putString("flutter.notif_level", "important").apply()
        val fm = FirebaseMessaging.getInstance()
        for (t in listOf("lvl_balanced", "lvl_all")) fm.unsubscribeFromTopic(t)
        for (t in listOf("lvl_important", "calm")) fm.subscribeToTopic(t)
        NotificationManagerCompat.from(context).cancel(intent.getStringExtra(EXTRA_TAG), intent.getIntExtra(EXTRA_ID, 0))
        val lang = prefs.getString("flutter.language", null)
        val en = (if (lang == null || lang == "system") Locale.getDefault().language else lang).startsWith("en")
        Toast.makeText(
            context,
            if (en) "You'll now get only important news. Change it in Settings." else "Artık yalnızca önemli haberler gelecek. Ayarlar'dan değiştirebilirsiniz.",
            Toast.LENGTH_LONG,
        ).show()
    }
}
