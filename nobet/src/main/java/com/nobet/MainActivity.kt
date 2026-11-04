package com.nobet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.key
import com.nobet.data.Store
import com.nobet.notify.Reminders
import com.nobet.ui.AppRoot
import com.nobet.ui.NobetTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Store.init(applicationContext)
    Reminders.ensureChannel(this)
    Reminders.reschedule(this)
    enableEdgeToEdge()
    setContent {
      NobetTheme {
        // Dil değişince tüm ağaç yeniden kurulur (TR/EN anında geçiş).
        key(Store.lang) { AppRoot() }
      }
    }
  }
}
