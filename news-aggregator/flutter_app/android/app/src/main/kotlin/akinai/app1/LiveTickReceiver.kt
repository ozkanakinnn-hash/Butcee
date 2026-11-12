package akinai.app1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Canlı skor panelinin dakikasını her dakika ilerletir ("61'" → "62'"); sunucudan istek gerekmez. */
class LiveTickReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_FIXTURE = "fixture_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_FIXTURE) ?: return
        LiveScoreNotifier.tick(context, id)
    }
}
