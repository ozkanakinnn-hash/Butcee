package akinai.minikgurme.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import akinai.minikgurme.data.model.AllergenTest
import akinai.minikgurme.data.model.BabyProfile
import akinai.minikgurme.data.model.FavoriteRecipe
import akinai.minikgurme.data.model.FeedingDiaryEntry
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.data.model.ShoppingItem
import akinai.minikgurme.data.model.TriedFood

/**
 * Bebek başına bir veritabanı (Android ve iOS ortak). Dosya adı ve şema Android'deki eski sürümle aynıdır;
 * güncellemede mevcut kayıtlar korunur.
 */
@Database(
    entities = [
        Meal::class,
        FeedingDiaryEntry::class,
        GrowthRecord::class,
        AllergenTest::class,
        BabyProfile::class,
        TriedFood::class,
        FavoriteRecipe::class,
        ShoppingItem::class
    ],
    version = 7,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun feedingDiaryDao(): FeedingDiaryDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun allergenTestDao(): AllergenTestDao
    abstract fun babyProfileDao(): BabyProfileDao
    abstract fun triedFoodDao(): TriedFoodDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object

}

/** v7: bebek profiline cinsiyet (WHO karşılaştırması için). Mevcut veriler korunur. */
expect val MIGRATION_6_7: Migration

/** Platformun veritabanı oluşturucusu (Android: uygulama bağlamı, iOS: belgeler klasörü + SQLite). */
expect fun databaseBuilder(name: String): RoomDatabase.Builder<AppDatabase>

expect inline fun <T> synchronizedCompat(lock: Any, block: () -> T): T

// Room derleyicisi her platform için gerçeklemeyi üretir.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

private object DatabaseCache {
    val instances = mutableMapOf<String, AppDatabase>()
    val lock = Any()
}

/** Verilen adla (bkz. Babies.dbName) açık veritabanı; yoksa açar. */
fun AppDatabase.Companion.getDatabase(name: String): AppDatabase = synchronizedCompat(DatabaseCache.lock) {
    DatabaseCache.instances.getOrPut(name) {
        databaseBuilder(name)
            .addMigrations(MIGRATION_6_7)
            .fallbackToDestructiveMigration(dropAllTables = true)
            // Örnek veri eklenmez: uygulama boş açılır, ilk açılışta bebek bilgileri sorulur.
            .build()
    }
}

/** Testler ve bebek silme için açık bağlantıları kapatır. */
fun AppDatabase.Companion.closeAll() = synchronizedCompat(DatabaseCache.lock) {
    DatabaseCache.instances.values.forEach { it.close() }; DatabaseCache.instances.clear()
}

/** Bir bebeğin veritabanı dosyasını siler (bebek profili silinince). */
expect fun deleteDatabaseFile(name: String)
