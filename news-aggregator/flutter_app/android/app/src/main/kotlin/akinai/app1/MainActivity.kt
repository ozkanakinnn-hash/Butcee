package akinai.app1

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Uygulamanın kendi kurduğu bildirimlere (haber, canlı skor) dokununca gelen veri Flutter'a "akinai/launch"
 * kanalıyla iletilir: soğuk açılışta takePayload ile, uygulama açıkken "payload" çağrısıyla.
 */
class MainActivity : FlutterActivity() {
    private var channel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "akinai/launch").apply {
            setMethodCallHandler { call, result ->
                if (call.method == "takePayload") result.success(takePayload(intent)) else result.notImplemented()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        takePayload(intent)?.let { channel?.invokeMethod("payload", it) }
    }

    private fun takePayload(intent: Intent?): String? {
        val p = intent?.getStringExtra(NewsNotifier.EXTRA_PAYLOAD) ?: return null
        intent.removeExtra(NewsNotifier.EXTRA_PAYLOAD)
        return p
    }
}
