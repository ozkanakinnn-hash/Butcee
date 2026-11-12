package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.TriedFood
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlin.Boolean
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

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TriedFoodDao_Impl(
  __db: RoomDatabase,
) : TriedFoodDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTriedFood: EntityInsertAdapter<TriedFood>
  init {
    this.__db = __db
    this.__insertAdapterOfTriedFood = object : EntityInsertAdapter<TriedFood>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `tried_foods` (`foodId`,`foodName`,`isTried`,`reaction`,`symptoms`,`triedDate`,`reactionNotes`,`timestamp`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TriedFood) {
        statement.bindText(1, entity.foodId)
        statement.bindText(2, entity.foodName)
        val _tmp: Int = if (entity.isTried) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindText(4, entity.reaction)
        statement.bindText(5, entity.symptoms)
        statement.bindText(6, entity.triedDate)
        statement.bindText(7, entity.reactionNotes)
        statement.bindLong(8, entity.timestamp)
      }
    }
  }

  public override suspend fun insertOrUpdateTriedFood(triedFood: TriedFood): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTriedFood.insert(_connection, triedFood)
  }

  public override fun getAllTriedFoods(): Flow<List<TriedFood>> {
    val _sql: String = "SELECT * FROM tried_foods ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("tried_foods")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfFoodId: Int = getColumnIndexOrThrow(_stmt, "foodId")
        val _columnIndexOfFoodName: Int = getColumnIndexOrThrow(_stmt, "foodName")
        val _columnIndexOfIsTried: Int = getColumnIndexOrThrow(_stmt, "isTried")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfSymptoms: Int = getColumnIndexOrThrow(_stmt, "symptoms")
        val _columnIndexOfTriedDate: Int = getColumnIndexOrThrow(_stmt, "triedDate")
        val _columnIndexOfReactionNotes: Int = getColumnIndexOrThrow(_stmt, "reactionNotes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<TriedFood> = mutableListOf()
        while (_stmt.step()) {
          val _item: TriedFood
          val _tmpFoodId: String
          _tmpFoodId = _stmt.getText(_columnIndexOfFoodId)
          val _tmpFoodName: String
          _tmpFoodName = _stmt.getText(_columnIndexOfFoodName)
          val _tmpIsTried: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsTried).toInt()
          _tmpIsTried = _tmp != 0
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpSymptoms: String
          _tmpSymptoms = _stmt.getText(_columnIndexOfSymptoms)
          val _tmpTriedDate: String
          _tmpTriedDate = _stmt.getText(_columnIndexOfTriedDate)
          val _tmpReactionNotes: String
          _tmpReactionNotes = _stmt.getText(_columnIndexOfReactionNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = TriedFood(_tmpFoodId,_tmpFoodName,_tmpIsTried,_tmpReaction,_tmpSymptoms,_tmpTriedDate,_tmpReactionNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTriedCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM tried_foods WHERE isTried = 1"
    return createFlow(__db, false, arrayOf("tried_foods")) { _connection ->
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

  public override suspend fun getTriedFoodById(foodId: String): TriedFood? {
    val _sql: String = "SELECT * FROM tried_foods WHERE foodId = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, foodId)
        val _columnIndexOfFoodId: Int = getColumnIndexOrThrow(_stmt, "foodId")
        val _columnIndexOfFoodName: Int = getColumnIndexOrThrow(_stmt, "foodName")
        val _columnIndexOfIsTried: Int = getColumnIndexOrThrow(_stmt, "isTried")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfSymptoms: Int = getColumnIndexOrThrow(_stmt, "symptoms")
        val _columnIndexOfTriedDate: Int = getColumnIndexOrThrow(_stmt, "triedDate")
        val _columnIndexOfReactionNotes: Int = getColumnIndexOrThrow(_stmt, "reactionNotes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: TriedFood?
        if (_stmt.step()) {
          val _tmpFoodId: String
          _tmpFoodId = _stmt.getText(_columnIndexOfFoodId)
          val _tmpFoodName: String
          _tmpFoodName = _stmt.getText(_columnIndexOfFoodName)
          val _tmpIsTried: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsTried).toInt()
          _tmpIsTried = _tmp != 0
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpSymptoms: String
          _tmpSymptoms = _stmt.getText(_columnIndexOfSymptoms)
          val _tmpTriedDate: String
          _tmpTriedDate = _stmt.getText(_columnIndexOfTriedDate)
          val _tmpReactionNotes: String
          _tmpReactionNotes = _stmt.getText(_columnIndexOfReactionNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result = TriedFood(_tmpFoodId,_tmpFoodName,_tmpIsTried,_tmpReaction,_tmpSymptoms,_tmpTriedDate,_tmpReactionNotes,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTriedFood(foodId: String) {
    val _sql: String = "DELETE FROM tried_foods WHERE foodId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, foodId)
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
