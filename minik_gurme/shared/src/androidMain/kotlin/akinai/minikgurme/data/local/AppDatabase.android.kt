package akinai.minikgurme.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Android uygulama bağlamı; MainActivity (ve testler) ilk iş olarak [init] çağırır. */
@SuppressLint("StaticFieldLeak")
object PlatformContext {
    lateinit var app: Context
        private set
    fun init(context: Context) { app = context.applicationContext }
}

actual val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE baby_profile ADD COLUMN sex TEXT NOT NULL DEFAULT ''")
    }
}

// Android eski sürümle aynı SQLite (framework) sürücüsünü kullanır; yedekleme openHelper'a erişebilir.
actual fun databaseBuilder(name: String): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(PlatformContext.app, name)

@Suppress("NOTHING_TO_INLINE")
actual inline fun <T> synchronizedCompat(lock: Any, block: () -> T): T = synchronized(lock, block)

actual fun deleteDatabaseFile(name: String) { PlatformContext.app.deleteDatabase(name) }
