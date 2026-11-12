package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Beklenmeyen bir çökme olursa hata ayrıntısını cihaza kaydeder; uygulama bir sonraki açılışta
 * kullanıcıya gösterip kopyalamasını sağlar (geliştiriciye iletmek için).
 */
object CrashReporter {
  private const val FILE = "last_crash.txt"

  fun install(context: Context) {
    val appContext = context.applicationContext
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      try {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val info = "Cihaz: ${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
          "İş parçacığı: ${thread.name}\n\n$sw"
        File(appContext.filesDir, FILE).writeText(info.take(20_000))
      } catch (_: Exception) {
      }
      previous?.uncaughtException(thread, throwable)
    }
  }

  /** Kaydedilmiş son çökme raporu (varsa) — okunduktan sonra silinir. */
  fun consumeLastCrash(context: Context): String? {
    val f = File(context.filesDir, FILE)
    if (!f.exists()) return null
    return runCatching { f.readText() }.getOrNull().also { f.delete() }
  }

  fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Ezan & Vakit hata raporu", text))
  }
}
