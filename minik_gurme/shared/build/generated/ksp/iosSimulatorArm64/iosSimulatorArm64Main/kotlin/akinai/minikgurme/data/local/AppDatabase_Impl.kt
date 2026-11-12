package akinai.minikgurme.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _mealDao: Lazy<MealDao> = lazy {
    MealDao_Impl(this)
  }

  private val _feedingDiaryDao: Lazy<FeedingDiaryDao> = lazy {
    FeedingDiaryDao_Impl(this)
  }

  private val _growthRecordDao: Lazy<GrowthRecordDao> = lazy {
    GrowthRecordDao_Impl(this)
  }

  private val _allergenTestDao: Lazy<AllergenTestDao> = lazy {
    AllergenTestDao_Impl(this)
  }

  private val _babyProfileDao: Lazy<BabyProfileDao> = lazy {
    BabyProfileDao_Impl(this)
  }

  private val _triedFoodDao: Lazy<TriedFoodDao> = lazy {
    TriedFoodDao_Impl(this)
  }

  private val _favoriteRecipeDao: Lazy<FavoriteRecipeDao> = lazy {
    FavoriteRecipeDao_Impl(this)
  }

  private val _shoppingItemDao: Lazy<ShoppingItemDao> = lazy {
    ShoppingItemDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(7, "a4fc9bb9bcefd69987b52dddf393cfd5", "045e7855aea2e387cf25b26f0a42570b") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `meals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `mealType` TEXT NOT NULL, `foodItems` TEXT NOT NULL, `texture` TEXT NOT NULL, `reaction` TEXT NOT NULL, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `feeding_diary` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `time` TEXT NOT NULL, `mealType` TEXT NOT NULL, `foodItems` TEXT NOT NULL, `portionAmount` TEXT NOT NULL, `texture` TEXT NOT NULL, `appetite` TEXT NOT NULL, `reaction` TEXT NOT NULL, `reactionNotes` TEXT NOT NULL, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `growth_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `weightKg` REAL NOT NULL, `heightCm` REAL NOT NULL, `headCircumferenceCm` REAL, `ageMonths` INTEGER NOT NULL, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `allergen_tests` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `food` TEXT NOT NULL, `startDate` TEXT NOT NULL, `day` INTEGER NOT NULL, `status` TEXT NOT NULL, `notes` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `baby_profile` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `ageMonths` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `notes` TEXT NOT NULL, `sex` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tried_foods` (`foodId` TEXT NOT NULL, `foodName` TEXT NOT NULL, `isTried` INTEGER NOT NULL, `reaction` TEXT NOT NULL, `symptoms` TEXT NOT NULL, `triedDate` TEXT NOT NULL, `reactionNotes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`foodId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `favorite_recipes` (`recipeId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`recipeId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `shopping_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` TEXT NOT NULL, `factor` REAL NOT NULL, `emoji` TEXT NOT NULL, `source` TEXT NOT NULL, `checked` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a4fc9bb9bcefd69987b52dddf393cfd5')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `meals`")
        connection.execSQL("DROP TABLE IF EXISTS `feeding_diary`")
        connection.execSQL("DROP TABLE IF EXISTS `growth_records`")
        connection.execSQL("DROP TABLE IF EXISTS `allergen_tests`")
        connection.execSQL("DROP TABLE IF EXISTS `baby_profile`")
        connection.execSQL("DROP TABLE IF EXISTS `tried_foods`")
        connection.execSQL("DROP TABLE IF EXISTS `favorite_recipes`")
        connection.execSQL("DROP TABLE IF EXISTS `shopping_items`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsMeals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMeals.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("mealType", TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("foodItems", TableInfo.Column("foodItems", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("texture", TableInfo.Column("texture", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("reaction", TableInfo.Column("reaction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMeals.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMeals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMeals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMeals: TableInfo = TableInfo("meals", _columnsMeals, _foreignKeysMeals, _indicesMeals)
        val _existingMeals: TableInfo = read(connection, "meals")
        if (!_infoMeals.equals(_existingMeals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |meals(akinai.minikgurme.data.model.Meal).
              | Expected:
              |""".trimMargin() + _infoMeals + """
              |
              | Found:
              |""".trimMargin() + _existingMeals)
        }
        val _columnsFeedingDiary: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFeedingDiary.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("time", TableInfo.Column("time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("mealType", TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("foodItems", TableInfo.Column("foodItems", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("portionAmount", TableInfo.Column("portionAmount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("texture", TableInfo.Column("texture", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("appetite", TableInfo.Column("appetite", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("reaction", TableInfo.Column("reaction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("reactionNotes", TableInfo.Column("reactionNotes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFeedingDiary.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFeedingDiary: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFeedingDiary: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFeedingDiary: TableInfo = TableInfo("feeding_diary", _columnsFeedingDiary, _foreignKeysFeedingDiary, _indicesFeedingDiary)
        val _existingFeedingDiary: TableInfo = read(connection, "feeding_diary")
        if (!_infoFeedingDiary.equals(_existingFeedingDiary)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |feeding_diary(akinai.minikgurme.data.model.FeedingDiaryEntry).
              | Expected:
              |""".trimMargin() + _infoFeedingDiary + """
              |
              | Found:
              |""".trimMargin() + _existingFeedingDiary)
        }
        val _columnsGrowthRecords: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGrowthRecords.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("weightKg", TableInfo.Column("weightKg", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("heightCm", TableInfo.Column("heightCm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("headCircumferenceCm", TableInfo.Column("headCircumferenceCm", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("ageMonths", TableInfo.Column("ageMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGrowthRecords.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGrowthRecords: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGrowthRecords: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGrowthRecords: TableInfo = TableInfo("growth_records", _columnsGrowthRecords, _foreignKeysGrowthRecords, _indicesGrowthRecords)
        val _existingGrowthRecords: TableInfo = read(connection, "growth_records")
        if (!_infoGrowthRecords.equals(_existingGrowthRecords)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |growth_records(akinai.minikgurme.data.model.GrowthRecord).
              | Expected:
              |""".trimMargin() + _infoGrowthRecords + """
              |
              | Found:
              |""".trimMargin() + _existingGrowthRecords)
        }
        val _columnsAllergenTests: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAllergenTests.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAllergenTests.put("food", TableInfo.Column("food", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAllergenTests.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAllergenTests.put("day", TableInfo.Column("day", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAllergenTests.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAllergenTests.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAllergenTests: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAllergenTests: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAllergenTests: TableInfo = TableInfo("allergen_tests", _columnsAllergenTests, _foreignKeysAllergenTests, _indicesAllergenTests)
        val _existingAllergenTests: TableInfo = read(connection, "allergen_tests")
        if (!_infoAllergenTests.equals(_existingAllergenTests)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |allergen_tests(akinai.minikgurme.data.model.AllergenTest).
              | Expected:
              |""".trimMargin() + _infoAllergenTests + """
              |
              | Found:
              |""".trimMargin() + _existingAllergenTests)
        }
        val _columnsBabyProfile: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBabyProfile.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBabyProfile.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBabyProfile.put("ageMonths", TableInfo.Column("ageMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBabyProfile.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBabyProfile.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBabyProfile.put("sex", TableInfo.Column("sex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBabyProfile: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBabyProfile: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBabyProfile: TableInfo = TableInfo("baby_profile", _columnsBabyProfile, _foreignKeysBabyProfile, _indicesBabyProfile)
        val _existingBabyProfile: TableInfo = read(connection, "baby_profile")
        if (!_infoBabyProfile.equals(_existingBabyProfile)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |baby_profile(akinai.minikgurme.data.model.BabyProfile).
              | Expected:
              |""".trimMargin() + _infoBabyProfile + """
              |
              | Found:
              |""".trimMargin() + _existingBabyProfile)
        }
        val _columnsTriedFoods: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTriedFoods.put("foodId", TableInfo.Column("foodId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("foodName", TableInfo.Column("foodName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("isTried", TableInfo.Column("isTried", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("reaction", TableInfo.Column("reaction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("symptoms", TableInfo.Column("symptoms", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("triedDate", TableInfo.Column("triedDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("reactionNotes", TableInfo.Column("reactionNotes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTriedFoods.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTriedFoods: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTriedFoods: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTriedFoods: TableInfo = TableInfo("tried_foods", _columnsTriedFoods, _foreignKeysTriedFoods, _indicesTriedFoods)
        val _existingTriedFoods: TableInfo = read(connection, "tried_foods")
        if (!_infoTriedFoods.equals(_existingTriedFoods)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tried_foods(akinai.minikgurme.data.model.TriedFood).
              | Expected:
              |""".trimMargin() + _infoTriedFoods + """
              |
              | Found:
              |""".trimMargin() + _existingTriedFoods)
        }
        val _columnsFavoriteRecipes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFavoriteRecipes.put("recipeId", TableInfo.Column("recipeId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFavoriteRecipes.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFavoriteRecipes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFavoriteRecipes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFavoriteRecipes: TableInfo = TableInfo("favorite_recipes", _columnsFavoriteRecipes, _foreignKeysFavoriteRecipes, _indicesFavoriteRecipes)
        val _existingFavoriteRecipes: TableInfo = read(connection, "favorite_recipes")
        if (!_infoFavoriteRecipes.equals(_existingFavoriteRecipes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |favorite_recipes(akinai.minikgurme.data.model.FavoriteRecipe).
              | Expected:
              |""".trimMargin() + _infoFavoriteRecipes + """
              |
              | Found:
              |""".trimMargin() + _existingFavoriteRecipes)
        }
        val _columnsShoppingItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsShoppingItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("amount", TableInfo.Column("amount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("factor", TableInfo.Column("factor", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("emoji", TableInfo.Column("emoji", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("checked", TableInfo.Column("checked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysShoppingItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesShoppingItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoShoppingItems: TableInfo = TableInfo("shopping_items", _columnsShoppingItems, _foreignKeysShoppingItems, _indicesShoppingItems)
        val _existingShoppingItems: TableInfo = read(connection, "shopping_items")
        if (!_infoShoppingItems.equals(_existingShoppingItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |shopping_items(akinai.minikgurme.data.model.ShoppingItem).
              | Expected:
              |""".trimMargin() + _infoShoppingItems + """
              |
              | Found:
              |""".trimMargin() + _existingShoppingItems)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "meals", "feeding_diary", "growth_records", "allergen_tests", "baby_profile", "tried_foods", "favorite_recipes", "shopping_items")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(MealDao::class, MealDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FeedingDiaryDao::class, FeedingDiaryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GrowthRecordDao::class, GrowthRecordDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AllergenTestDao::class, AllergenTestDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BabyProfileDao::class, BabyProfileDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TriedFoodDao::class, TriedFoodDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FavoriteRecipeDao::class, FavoriteRecipeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ShoppingItemDao::class, ShoppingItemDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun mealDao(): MealDao = _mealDao.value

  public override fun feedingDiaryDao(): FeedingDiaryDao = _feedingDiaryDao.value

  public override fun growthRecordDao(): GrowthRecordDao = _growthRecordDao.value

  public override fun allergenTestDao(): AllergenTestDao = _allergenTestDao.value

  public override fun babyProfileDao(): BabyProfileDao = _babyProfileDao.value

  public override fun triedFoodDao(): TriedFoodDao = _triedFoodDao.value

  public override fun favoriteRecipeDao(): FavoriteRecipeDao = _favoriteRecipeDao.value

  public override fun shoppingItemDao(): ShoppingItemDao = _shoppingItemDao.value
}
