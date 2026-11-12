package akinai.minikgurme.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE baby_profile ADD COLUMN sex TEXT NOT NULL DEFAULT ''")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentsDir(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
    return requireNotNull(url?.path)
}

actual fun databaseBuilder(name: String): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(name = "${documentsDir()}/$name")
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)

// iOS'ta veritabanları yalnızca ana iş parçacığından açılır; basit kilit yeterli.
@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> synchronizedCompat(lock: Any, block: () -> T): T = block()
