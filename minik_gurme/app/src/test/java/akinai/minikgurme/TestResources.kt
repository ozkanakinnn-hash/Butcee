package akinai.minikgurme

import android.content.ContentProvider
import org.robolectric.Robolectric

/**
 * Compose kaynakları (görsel/yazı tipi) Android'de bağlamı bir ContentProvider'dan alır.
 * Gerçek cihazda manifest bunu başlatır; Robolectric başlatmadığı için testlerde elle kurulur.
 */
object TestResources {
    @Suppress("UNCHECKED_CAST")
    fun init() {
        val cls = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider") as Class<out ContentProvider>
        Robolectric.setupContentProvider(cls)
    }
}
