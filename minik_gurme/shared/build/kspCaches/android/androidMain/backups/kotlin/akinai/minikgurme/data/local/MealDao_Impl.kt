package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.Meal
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MealDao_Impl(
  __db: RoomDatabase,
) : MealDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMeal: EntityInsertAdapter<Meal>

  private val __deleteAdapterOfMeal: EntityDeleteOrUpdateAdapter<Meal>

  private val __updateAdapterOfMeal: EntityDeleteOrUpdateAdapter<Meal>
  init {
    this.__db = __db
    this.__insertAdapterOfMeal = object : EntityInsertAdapter<Meal>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `meals` (`id`,`date`,`mealType`,`foodItems`,`texture`,`reaction`,`notes`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Meal) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.mealType)
        statement.bindText(4, entity.foodItems)
        statement.bindText(5, entity.texture)
        statement.bindText(6, entity.reaction)
        statement.bindText(7, entity.notes)
        statement.bindLong(8, entity.timestamp)
      }
    }
    this.__deleteAdapterOfMeal = object : EntityDeleteOrUpdateAdapter<Meal>() {
      protected override fun createQuery(): String = "DELETE FROM `meals` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Meal) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfMeal = object : EntityDeleteOrUpdateAdapter<Meal>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `meals` SET `id` = ?,`date` = ?,`mealType` = ?,`foodItems` = ?,`texture` = ?,`reaction` = ?,`notes` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Meal) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.mealType)
        statement.bindText(4, entity.foodItems)
        statement.bindText(5, entity.texture)
        statement.bindText(6, entity.reaction)
        statement.bindText(7, entity.notes)
        statement.bindLong(8, entity.timestamp)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insertMeal(meal: Meal): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMeal.insertAndReturnId(_connection, meal)
    _result
  }

  public override suspend fun deleteMeal(meal: Meal): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMeal.handle(_connection, meal)
  }

  public override suspend fun updateMeal(meal: Meal): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfMeal.handle(_connection, meal)
  }

  public override fun getAllMeals(): Flow<List<Meal>> {
    val _sql: String = "SELECT * FROM meals ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("meals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfFoodItems: Int = getColumnIndexOrThrow(_stmt, "foodItems")
        val _columnIndexOfTexture: Int = getColumnIndexOrThrow(_stmt, "texture")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<Meal> = mutableListOf()
        while (_stmt.step()) {
          val _item: Meal
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpFoodItems: String
          _tmpFoodItems = _stmt.getText(_columnIndexOfFoodItems)
          val _tmpTexture: String
          _tmpTexture = _stmt.getText(_columnIndexOfTexture)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = Meal(_tmpId,_tmpDate,_tmpMealType,_tmpFoodItems,_tmpTexture,_tmpReaction,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMealsForDate(date: String): Flow<List<Meal>> {
    val _sql: String = "SELECT * FROM meals WHERE date = ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("meals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfFoodItems: Int = getColumnIndexOrThrow(_stmt, "foodItems")
        val _columnIndexOfTexture: Int = getColumnIndexOrThrow(_stmt, "texture")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<Meal> = mutableListOf()
        while (_stmt.step()) {
          val _item: Meal
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpFoodItems: String
          _tmpFoodItems = _stmt.getText(_columnIndexOfFoodItems)
          val _tmpTexture: String
          _tmpTexture = _stmt.getText(_columnIndexOfTexture)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = Meal(_tmpId,_tmpDate,_tmpMealType,_tmpFoodItems,_tmpTexture,_tmpReaction,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMealCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM meals"
    return createFlow(__db, false, arrayOf("meals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMealById(id: Long) {
    val _sql: String = "DELETE FROM meals WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllMeals() {
    val _sql: String = "DELETE FROM meals"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
