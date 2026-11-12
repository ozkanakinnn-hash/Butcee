package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.FeedingDiaryEntry
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
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
public class FeedingDiaryDao_Impl(
  __db: RoomDatabase,
) : FeedingDiaryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFeedingDiaryEntry: EntityInsertAdapter<FeedingDiaryEntry>

  private val __deleteAdapterOfFeedingDiaryEntry: EntityDeleteOrUpdateAdapter<FeedingDiaryEntry>

  private val __updateAdapterOfFeedingDiaryEntry: EntityDeleteOrUpdateAdapter<FeedingDiaryEntry>
  init {
    this.__db = __db
    this.__insertAdapterOfFeedingDiaryEntry = object : EntityInsertAdapter<FeedingDiaryEntry>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `feeding_diary` (`id`,`date`,`time`,`mealType`,`foodItems`,`portionAmount`,`texture`,`appetite`,`reaction`,`reactionNotes`,`notes`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FeedingDiaryEntry) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.time)
        statement.bindText(4, entity.mealType)
        statement.bindText(5, entity.foodItems)
        statement.bindText(6, entity.portionAmount)
        statement.bindText(7, entity.texture)
        statement.bindText(8, entity.appetite)
        statement.bindText(9, entity.reaction)
        statement.bindText(10, entity.reactionNotes)
        statement.bindText(11, entity.notes)
        statement.bindLong(12, entity.timestamp)
      }
    }
    this.__deleteAdapterOfFeedingDiaryEntry = object : EntityDeleteOrUpdateAdapter<FeedingDiaryEntry>() {
      protected override fun createQuery(): String = "DELETE FROM `feeding_diary` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FeedingDiaryEntry) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfFeedingDiaryEntry = object : EntityDeleteOrUpdateAdapter<FeedingDiaryEntry>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `feeding_diary` SET `id` = ?,`date` = ?,`time` = ?,`mealType` = ?,`foodItems` = ?,`portionAmount` = ?,`texture` = ?,`appetite` = ?,`reaction` = ?,`reactionNotes` = ?,`notes` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FeedingDiaryEntry) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.time)
        statement.bindText(4, entity.mealType)
        statement.bindText(5, entity.foodItems)
        statement.bindText(6, entity.portionAmount)
        statement.bindText(7, entity.texture)
        statement.bindText(8, entity.appetite)
        statement.bindText(9, entity.reaction)
        statement.bindText(10, entity.reactionNotes)
        statement.bindText(11, entity.notes)
        statement.bindLong(12, entity.timestamp)
        statement.bindLong(13, entity.id)
      }
    }
  }

  public override suspend fun insertEntry(entry: FeedingDiaryEntry): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfFeedingDiaryEntry.insertAndReturnId(_connection, entry)
    _result
  }

  public override suspend fun deleteEntry(entry: FeedingDiaryEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfFeedingDiaryEntry.handle(_connection, entry)
  }

  public override suspend fun updateEntry(entry: FeedingDiaryEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfFeedingDiaryEntry.handle(_connection, entry)
  }

  public override fun getAllDiaryEntries(): Flow<List<FeedingDiaryEntry>> {
    val _sql: String = "SELECT * FROM feeding_diary ORDER BY date DESC, time DESC, timestamp DESC"
    return createFlow(__db, false, arrayOf("feeding_diary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTime: Int = getColumnIndexOrThrow(_stmt, "time")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfFoodItems: Int = getColumnIndexOrThrow(_stmt, "foodItems")
        val _columnIndexOfPortionAmount: Int = getColumnIndexOrThrow(_stmt, "portionAmount")
        val _columnIndexOfTexture: Int = getColumnIndexOrThrow(_stmt, "texture")
        val _columnIndexOfAppetite: Int = getColumnIndexOrThrow(_stmt, "appetite")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfReactionNotes: Int = getColumnIndexOrThrow(_stmt, "reactionNotes")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FeedingDiaryEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: FeedingDiaryEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTime: String
          _tmpTime = _stmt.getText(_columnIndexOfTime)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpFoodItems: String
          _tmpFoodItems = _stmt.getText(_columnIndexOfFoodItems)
          val _tmpPortionAmount: String
          _tmpPortionAmount = _stmt.getText(_columnIndexOfPortionAmount)
          val _tmpTexture: String
          _tmpTexture = _stmt.getText(_columnIndexOfTexture)
          val _tmpAppetite: String
          _tmpAppetite = _stmt.getText(_columnIndexOfAppetite)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpReactionNotes: String
          _tmpReactionNotes = _stmt.getText(_columnIndexOfReactionNotes)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = FeedingDiaryEntry(_tmpId,_tmpDate,_tmpTime,_tmpMealType,_tmpFoodItems,_tmpPortionAmount,_tmpTexture,_tmpAppetite,_tmpReaction,_tmpReactionNotes,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEntriesForDate(date: String): Flow<List<FeedingDiaryEntry>> {
    val _sql: String = "SELECT * FROM feeding_diary WHERE date = ? ORDER BY time ASC, timestamp ASC"
    return createFlow(__db, false, arrayOf("feeding_diary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTime: Int = getColumnIndexOrThrow(_stmt, "time")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfFoodItems: Int = getColumnIndexOrThrow(_stmt, "foodItems")
        val _columnIndexOfPortionAmount: Int = getColumnIndexOrThrow(_stmt, "portionAmount")
        val _columnIndexOfTexture: Int = getColumnIndexOrThrow(_stmt, "texture")
        val _columnIndexOfAppetite: Int = getColumnIndexOrThrow(_stmt, "appetite")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfReactionNotes: Int = getColumnIndexOrThrow(_stmt, "reactionNotes")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FeedingDiaryEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: FeedingDiaryEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTime: String
          _tmpTime = _stmt.getText(_columnIndexOfTime)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpFoodItems: String
          _tmpFoodItems = _stmt.getText(_columnIndexOfFoodItems)
          val _tmpPortionAmount: String
          _tmpPortionAmount = _stmt.getText(_columnIndexOfPortionAmount)
          val _tmpTexture: String
          _tmpTexture = _stmt.getText(_columnIndexOfTexture)
          val _tmpAppetite: String
          _tmpAppetite = _stmt.getText(_columnIndexOfAppetite)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpReactionNotes: String
          _tmpReactionNotes = _stmt.getText(_columnIndexOfReactionNotes)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = FeedingDiaryEntry(_tmpId,_tmpDate,_tmpTime,_tmpMealType,_tmpFoodItems,_tmpPortionAmount,_tmpTexture,_tmpAppetite,_tmpReaction,_tmpReactionNotes,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun searchEntries(query: String): Flow<List<FeedingDiaryEntry>> {
    val _sql: String = "SELECT * FROM feeding_diary WHERE foodItems LIKE '%' || ? || '%' OR notes LIKE '%' || ? || '%' ORDER BY date DESC, timestamp DESC"
    return createFlow(__db, false, arrayOf("feeding_diary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTime: Int = getColumnIndexOrThrow(_stmt, "time")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfFoodItems: Int = getColumnIndexOrThrow(_stmt, "foodItems")
        val _columnIndexOfPortionAmount: Int = getColumnIndexOrThrow(_stmt, "portionAmount")
        val _columnIndexOfTexture: Int = getColumnIndexOrThrow(_stmt, "texture")
        val _columnIndexOfAppetite: Int = getColumnIndexOrThrow(_stmt, "appetite")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfReactionNotes: Int = getColumnIndexOrThrow(_stmt, "reactionNotes")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FeedingDiaryEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: FeedingDiaryEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTime: String
          _tmpTime = _stmt.getText(_columnIndexOfTime)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpFoodItems: String
          _tmpFoodItems = _stmt.getText(_columnIndexOfFoodItems)
          val _tmpPortionAmount: String
          _tmpPortionAmount = _stmt.getText(_columnIndexOfPortionAmount)
          val _tmpTexture: String
          _tmpTexture = _stmt.getText(_columnIndexOfTexture)
          val _tmpAppetite: String
          _tmpAppetite = _stmt.getText(_columnIndexOfAppetite)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpReactionNotes: String
          _tmpReactionNotes = _stmt.getText(_columnIndexOfReactionNotes)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = FeedingDiaryEntry(_tmpId,_tmpDate,_tmpTime,_tmpMealType,_tmpFoodItems,_tmpPortionAmount,_tmpTexture,_tmpAppetite,_tmpReaction,_tmpReactionNotes,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM feeding_diary"
    return createFlow(__db, false, arrayOf("feeding_diary")) { _connection ->
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

  public override fun getDistinctDates(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT date FROM feeding_diary ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("feeding_diary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteEntryById(id: Long) {
    val _sql: String = "DELETE FROM feeding_diary WHERE id = ?"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
