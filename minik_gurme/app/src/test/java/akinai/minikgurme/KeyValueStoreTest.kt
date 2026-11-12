package akinai.minikgurme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.data.local.PlatformContext
import akinai.minikgurme.shared.KeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Ortak ayar deposu Android'de eski SharedPreferences dosyasını okur/yazar (ayarlar güncellemede korunur). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyValueStoreTest {
    @Test
    fun readsAndWritesSameSharedPreferences() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        PlatformContext.init(ctx)
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().putString("language", "en").commit()
        val store = KeyValueStore("app_prefs")
        assertEquals("en", store.getString("language"))
        store.putBoolean("x", true); store.putLong("n", 42L); store.putStringSet("s", setOf("a", "b"))
        val raw = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        assertTrue(raw.getBoolean("x", false)); assertEquals(42L, raw.getLong("n", 0)); assertEquals(setOf("a", "b"), raw.getStringSet("s", null))
        store.remove("x"); assertFalse(store.contains("x"))
        assertEquals("fallback", store.getString("missing", "fallback"))
    }
}
