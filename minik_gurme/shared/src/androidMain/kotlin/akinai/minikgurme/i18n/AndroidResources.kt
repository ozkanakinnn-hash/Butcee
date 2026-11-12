package akinai.minikgurme.i18n

import android.content.Context

/**
 * Compose kaynak dosyalarını (files/...) Android asset'lerinden doğrudan okur.
 * Uygulama açılırken (Compose başlamadan) ve arka plan alıcılarında kullanılır.
 */
fun composeFileReader(context: Context): suspend (String) -> ByteArray = { path ->
    context.assets.open("composeResources/akinai.minikgurme.resources/$path").use { it.readBytes() }
}
