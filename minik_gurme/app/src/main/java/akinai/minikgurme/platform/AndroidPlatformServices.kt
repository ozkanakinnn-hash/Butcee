package akinai.minikgurme.platform

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import akinai.minikgurme.BuildConfig
import akinai.minikgurme.i18n.t
import akinai.minikgurme.util.Backup
import akinai.minikgurme.util.DoctorReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Android gerçeklemesi: Toast, pano, izin penceresi, SAF dosya seçici, PDF paylaşımı. */
class AndroidPlatformServices(private val app: Context) : PlatformServices {
    override val appVersion: String =
        runCatching { app.packageManager.getPackageInfo(app.packageName, 0).versionName }.getOrNull() ?: ""
    override val isDebugBuild: Boolean = BuildConfig.DEBUG

    override fun toast(message: String) = Toast.makeText(app, t(message), Toast.LENGTH_LONG).show()

    override fun copyText(text: String) {
        app.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    @Composable
    override fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onResult(it) }
        return {
            val needs = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            if (needs) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else onResult(true)
        }
    }

    @Composable
    override fun rememberBackupExport(onResult: (Boolean) -> Unit): () -> Unit {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) scope.launch {
                onResult(runCatching { withContext(Dispatchers.IO) { Backup.export(context, uri) } }.isSuccess)
            }
        }
        return { launcher.launch("MinikGurme-yedek.json") }
    }

    @Composable
    override fun rememberBackupPicker(onPicked: (Any) -> Unit): () -> Unit {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) onPicked(uri) }
        return { launcher.launch(arrayOf("application/json", "text/plain", "*/*")) }
    }

    override suspend fun importBackup(file: Any): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { Backup.import(app, file as Uri) } }

    override suspend fun shareDoctorReport(): Result<Unit> = runCatching {
        val f = withContext(Dispatchers.IO) { DoctorReport.build(app) }
        DoctorReport.share(app, f)
    }
}
