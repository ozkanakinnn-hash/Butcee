package akinai.minikgurme.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.FavoriteRecipe
import akinai.minikgurme.data.model.FeedingDiaryEntry
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.data.model.TriedFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Meal::class,
        FeedingDiaryEntry::class,
        GrowthRecord::class,
        AllergenTest::class,
        BabyProfile::class,
        TriedFood::class,
        FavoriteRecipe::class,
        akinai.minikgurme.data.model.ShoppingItem::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun feedingDiaryDao(): FeedingDiaryDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun allergenTestDao(): AllergenTestDao
    abstract fun babyProfileDao(): BabyProfileDao
    abstract fun triedFoodDao(): TriedFoodDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        /** v7: bebek profiline cinsiyet (WHO karşılaştırması için). Mevcut veriler korunur. */
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE baby_profile ADD COLUMN sex TEXT NOT NULL DEFAULT ''")
            }
        }

        private val instances = mutableMapOf<String, AppDatabase>()

        /** Etkin bebeğin veritabanı (bkz. util.Babies). */
        fun getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase =
            getDatabase(context, akinai.minikgurme.util.Babies.dbName(akinai.minikgurme.util.Babies.activeId(context)))

        fun getDatabase(context: Context, name: String): AppDatabase = synchronized(this) {
            instances.getOrPut(name) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    // Örnek veri eklenmez: uygulama boş açılır, ilk açılışta bebek bilgileri sorulur.
                    .build()
            }
        }

        /** Testler ve bebek silme için önbellekteki bağlantıyı kapatır. */
        fun closeAll() = synchronized(this) { instances.values.forEach { it.close() }; instances.clear() }
    }
}
