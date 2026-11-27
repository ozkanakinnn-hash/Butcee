package akinai.app1

import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Uygulamanın kendi kurduğu bildirimlere (haber, canlı skor) dokununca gelen veri Flutter'a "akinai/launch"
 * kanalıyla iletilir: soğuk açılışta takePayload ile, uygulama açıkken "payload" çağrısıyla.
 *
 * Uçtan uca ekran (Play uyarısı, SDK 35+): içerik durum ve gezinme çubuklarının arkasına uzanır; boşlukları Flutter
 * (AppBar, NavigationBar, SafeArea) ayarlar. Kullanımdan kaldırılmış çubuk rengi API'leri kullanılmaz.
 */
class MainActivity : FlutterActivity() {
    private var channel: MethodChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Tüm Android sürümlerinde uçtan uca: Android 15+ zaten zorunlu kılıyor, eski sürümler de aynı görünsün.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
    }

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
