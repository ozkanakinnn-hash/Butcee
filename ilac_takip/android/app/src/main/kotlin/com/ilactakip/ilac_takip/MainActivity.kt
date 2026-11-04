package com.ilactakip.ilac_takip

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * "Alarm güvenilirliği" ekranı için yerel köprü (kanal: app/reliability).
 * Pil kısıtlaması, tam zamanlı alarm, tam ekran bildirim izni ve üretici
 * otomatik başlatma ayarlarını okur/açar.
 */
class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "app/reliability")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "status" -> result.success(status())
                    "openBatterySettings" -> result.success(openBatterySettings())
                    "openOemAutostart" -> result.success(oemIntents().any { tryStart(it) })
                    else -> result.notImplemented()
                }
            }
    }

    private fun status(): Map<String, Any> {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nm = getSystemService(NotificationManager::class.java)
        return mapOf(
            "ignoringBatteryOptimizations" to pm.isIgnoringBatteryOptimizations(packageName),
            "canScheduleExactAlarms" to (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()),
            "canUseFullScreenIntent" to (Build.VERSION.SDK_INT < 34 || nm.canUseFullScreenIntent()),
            "manufacturer" to Build.MANUFACTURER.lowercase(),
            "sdk" to Build.VERSION.SDK_INT,
        )
    }

    private fun openBatterySettings(): Boolean {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
        val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        return tryStart(direct) || tryStart(list)
    }

    /** Bileşen adları sürüme göre değişir; sırayla denenir, en sonda uygulama ayrıntıları. */
    private fun oemIntents(): List<Intent> = listOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oplus.safecenter",
            "com.oplus.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.lool",
            "com.samsung.android.sm.battery.ui.BatteryActivity")),
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
    )

    private fun tryStart(intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
