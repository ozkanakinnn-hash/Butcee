package com.nobet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.key
import com.nobet.data.Store
import com.nobet.i18n.t
import com.nobet.notify.Reminders
import com.nobet.ui.AppRoot
import com.nobet.ui.Nav
import com.nobet.ui.NobetTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Store.init(applicationContext)
    Reminders.ensureChannel(this)
    Reminders.reschedule(this)
    enableEdgeToEdge()
    if (savedInstanceState == null) handleShare(intent)
    setContent {
      NobetTheme {
        // Dil değişince tüm ağaç yeniden kurulur (TR/EN anında geçiş).
        key(Store.lang) { AppRoot() }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleShare(intent)
  }

  /** nobet://share?c=... bağlantısı veya paylaşılan metindeki kodu içe aktarır. */
  private fun handleShare(intent: Intent?) {
    val text = when (intent?.action) {
      Intent.ACTION_VIEW -> intent.dataString
      Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
      else -> null
    } ?: return
    if (!text.contains("NB1")) return
    val who = Store.importShareCode(text)
    if (who != null) {
      Toast.makeText(this, t("$who takvimi eklendi", "$who's schedule added"), Toast.LENGTH_LONG).show()
      if (Store.onboarded) Nav.open("share")
    } else {
      Toast.makeText(this, t("Paylaşım kodu okunamadı", "Couldn't read the share code"), Toast.LENGTH_LONG).show()
    }
  }
}
