package akinai.minikgurme.data.local

import android.content.Context
import akinai.minikgurme.util.Babies
import kotlinx.coroutines.CoroutineScope

/** Etkin bebeğin veritabanı (bkz. util.Babies). */
@Suppress("UNUSED_PARAMETER")
fun AppDatabase.Companion.getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase {
    PlatformContext.init(context)
    return getDatabase(Babies.dbName(Babies.activeId()))
}

/** Adı verilen bebek veritabanı. */
fun AppDatabase.Companion.getDatabase(context: Context, name: String): AppDatabase {
    PlatformContext.init(context)
    return getDatabase(name)
}
