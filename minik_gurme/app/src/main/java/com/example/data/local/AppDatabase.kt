package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AllergenTest
import com.example.data.model.BabyProfile
import com.example.data.model.FavoriteRecipe
import com.example.data.model.FeedingDiaryEntry
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
import com.example.data.model.TriedFood
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
        FavoriteRecipe::class
    ],
    version = 4,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minik_gurme_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Seed Baby Profile
            database.babyProfileDao().insertOrUpdateProfile(
                BabyProfile(
                    id = 1,
                    name = "Alp",
                    ageMonths = 7,
                    startDate = "2024-01-15",
                    notes = "Ek gıdaya heyecanla başladık!"
                )
            )

            // Seed Initial Meals
            database.mealDao().insertMeal(
                Meal(
                    date = todayStr,
                    mealType = "Sabah",
                    foodItems = "Yumurta sarısı (çeyrek), Avokado ezmesi",
                    texture = "Çatalla Ezilmiş",
                    reaction = "Sevdi",
                    notes = "Keyifle yedi, kızarıklık gözlenmedi.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 180
                )
            )

            database.mealDao().insertMeal(
                Meal(
                    date = todayStr,
                    mealType = "Öğle",
                    foodItems = "Kıymalı kabak & havuç ezmesi",
                    texture = "Pürüzsüz Püre",
                    reaction = "Sevdi",
                    notes = "2 tatlı kaşığı yedi, porsiyon iyiydi.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                )
            )

            // Seed Initial Feeding Diary Entries (Beslenme Günlüğü)
            val cal = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = df.format(cal.time)
            
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val twoDaysAgoStr = df.format(cal.time)

            // Today's Diary Entries
            database.feedingDiaryDao().insertEntry(
                FeedingDiaryEntry(
                    date = todayStr,
                    time = "09:30",
                    mealType = "Kahvaltı / Sabah",
                    foodItems = "Yumurta sarısı (çeyrek), Avokado ezmesi, Zeytinyağı",
                    portionAmount = "3 tatlı kaşığı",
                    texture = "Çatalla Ezilmiş",
                    appetite = "İştahla Bitirdi 😋",
                    reaction = "Sorunsuz / Keyifli ✨",
                    reactionNotes = "Dudak çevresinde kızarıklık yok.",
                    notes = "Avokadoyu çok sevdi, 1 çay kaşığı soğuk sıkım zeytinyağı ilave edildi.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 200
                )
            )

            database.feedingDiaryDao().insertEntry(
                FeedingDiaryEntry(
                    date = todayStr,
                    time = "13:15",
                    mealType = "Öğle Yemeği",
                    foodItems = "Kıymalı kabak & havuç ezmesi, İlikli kemik suyu",
                    portionAmount = "Yarım küçük kase",
                    texture = "Pürüzsüz Püre",
                    appetite = "Severek Yedi 😊",
                    reaction = "Sorunsuz / Keyifli ✨",
                    reactionNotes = "Sindirim rahat, gaz yapmadı.",
                    notes = "Buharda 15 dk pişti, çatalla ezilip kıymayla karıştırıldı.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                )
            )

            // Yesterday's Diary Entries
            database.feedingDiaryDao().insertEntry(
                FeedingDiaryEntry(
                    date = yesterdayStr,
                    time = "10:00",
                    mealType = "Kahvaltı / Sabah",
                    foodItems = "Bebek yulaf lapası, Buharda pişmiş elma püresi",
                    portionAmount = "4 tatlı kaşığı",
                    texture = "Pütürlü Ezme",
                    appetite = "İştahla Bitirdi 😋",
                    reaction = "Sorunsuz / Keyifli ✨",
                    notes = "Elmanın tatlı aroması yulafla çok uyumlu oldu.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 26
                )
            )

            database.feedingDiaryDao().insertEntry(
                FeedingDiaryEntry(
                    date = yesterdayStr,
                    time = "16:00",
                    mealType = "İkindi / Ara Öğün",
                    foodItems = "Tam yağlı ev yoğurdu, Çeyrek muz ezmesi",
                    portionAmount = "3 tatlı kaşığı",
                    texture = "Pürüzsüz Püre",
                    appetite = "İştahla Bitirdi 😋",
                    reaction = "Sorunsuz / Keyifli ✨",
                    notes = "Yoğurdu tek başına da çok seviyor.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 20
                )
            )

            // Two days ago Diary Entry
            database.feedingDiaryDao().insertEntry(
                FeedingDiaryEntry(
                    date = twoDaysAgoStr,
                    time = "12:30",
                    mealType = "Öğle Yemeği",
                    foodItems = "Kırmızı mercimek & tatlı patates çorbası",
                    portionAmount = "Yarım çay bardağı",
                    texture = "Pürüzsüz Püre",
                    appetite = "Bir Kısmını Yedi 🙂",
                    reaction = "Hafif Gaz / Huysuzluk 💨",
                    reactionNotes = "Öğleden sonra hafif gaz sancısı oldu, masaj yapıldı.",
                    notes = "Mercimek miktarı bir sonraki sefere daha az tutulacak.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 50
                )
            )

            // Seed Initial Allergen Tests
            database.allergenTestDao().insertTest(
                AllergenTest(
                    food = "Yumurta Sarısı",
                    startDate = todayStr,
                    day = 3,
                    status = "Güvenli",
                    notes = "3 gün denendi. Döküntü veya gaz olmadı."
                )
            )

            database.allergenTestDao().insertTest(
                AllergenTest(
                    food = "Sade Yoğurt (Tam Yağlı)",
                    startDate = todayStr,
                    day = 2,
                    status = "Devam Ediyor",
                    notes = "İkinci gün denemesi başarılı. Hafif kızarıklık yok."
                )
            )

            database.allergenTestDao().insertTest(
                AllergenTest(
                    food = "Yer Fıstığı Ezmesi (Şekersiz)",
                    startDate = todayStr,
                    day = 1,
                    status = "Devam Ediyor",
                    notes = "Püre ile inceltilerek az miktarda tattırılacak."
                )
            )

            // Seed Initial Tried Foods for the 100 Foods Challenge
            val initialFoods = listOf(
                TriedFood("avokado", "Avokado", true, "loved", "", todayStr, "Yumuşacık ezildi, çok sevdi."),
                TriedFood("kabak", "Kabak", true, "loved", "", todayStr, "Kıymayla birlikte keyifle tüketti."),
                TriedFood("havuc", "Havuç", true, "neutral", "", todayStr, "Buharda pişirildi."),
                TriedFood("tatli_patates", "Tatlı Patates", true, "loved", "", todayStr, "Tatlı aromayı beğendi."),
                TriedFood("elma", "Elma", true, "neutral", "", todayStr, "Cam rendede püre yapıldı."),
                TriedFood("yumurta_sarisi", "Yumurta Sarısı", true, "loved", "", todayStr, "3 gün testi tamamlandı."),
                TriedFood("yogurt", "Doğal Ev Yoğurdu", true, "loved", "", todayStr, "Tam yağlı ve mayalı."),
                TriedFood("dana_kiyma", "Dana Kıyma", true, "loved", "", todayStr, "İki kez çekilmiş az yağlı."),
                TriedFood("kirmizi_mercimek", "Kırmızı Mercimek", true, "neutral", "", todayStr, "Çorba içinde."),
                TriedFood("yulaf", "Bebek Yulafı", true, "loved", "", todayStr, "Su ve elma püresi ile pişirildi.")
            )
            for (food in initialFoods) {
                database.triedFoodDao().insertOrUpdateTriedFood(food)
            }

            // Seed Favorite Recipes
            database.favoriteRecipeDao().addFavorite(FavoriteRecipe("kabak_sandal"))
            database.favoriteRecipeDao().addFavorite(FavoriteRecipe("avokado_tost"))

            // Seed Initial Growth Records (Gelişim Takibi Verileri: Doğumdan 7. Aya)
            val growthCalendar = Calendar.getInstance()
            // 7. Ay (Güncel - Bugün)
            val date7m = df.format(growthCalendar.time)

            growthCalendar.add(Calendar.MONTH, -1) // 6. Ay
            val date6m = df.format(growthCalendar.time)

            growthCalendar.add(Calendar.MONTH, -2) // 4. Ay
            val date4m = df.format(growthCalendar.time)

            growthCalendar.add(Calendar.MONTH, -2) // 2. Ay
            val date2m = df.format(growthCalendar.time)

            growthCalendar.add(Calendar.MONTH, -1) // 1. Ay
            val date1m = df.format(growthCalendar.time)

            growthCalendar.add(Calendar.MONTH, -1) // Doğum (0. Ay)
            val dateBirth = df.format(growthCalendar.time)

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = dateBirth,
                    weightKg = 3.35f,
                    heightCm = 50.0f,
                    headCircumferenceCm = 35.0f,
                    ageMonths = 0,
                    notes = "Doğum kilosu ve boyu, hastane çıkış kontrolü."
                )
            )

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = date1m,
                    weightKg = 4.30f,
                    heightCm = 54.0f,
                    headCircumferenceCm = 37.0f,
                    ageMonths = 1,
                    notes = "1. ay doktor kontrolü, kilo alımı çok iyi."
                )
            )

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = date2m,
                    weightKg = 5.45f,
                    heightCm = 58.0f,
                    headCircumferenceCm = 39.0f,
                    ageMonths = 2,
                    notes = "2. ay aşıları yapıldı, gelişim persentilinde dengeli ilerliyor."
                )
            )

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = date4m,
                    weightKg = 6.85f,
                    heightCm = 63.5f,
                    headCircumferenceCm = 41.5f,
                    ageMonths = 4,
                    notes = "4. ay kontrolü, baş kontrolü tam, hareketli."
                )
            )

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = date6m,
                    weightKg = 7.90f,
                    heightCm = 67.5f,
                    headCircumferenceCm = 43.0f,
                    ageMonths = 6,
                    notes = "Ek gıdaya başlama öncesi ölçüm. Doktor onayladı."
                )
            )

            database.growthRecordDao().insertRecord(
                GrowthRecord(
                    date = date7m,
                    weightKg = 8.45f,
                    heightCm = 69.5f,
                    headCircumferenceCm = 44.0f,
                    ageMonths = 7,
                    notes = "7. ay kontrolü. Ek gıda ile büyüme eğrisi harika devam ediyor."
                )
            )
        }
    }
}
