package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.AllergenTest
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
public class AllergenTestDao_Impl(
  __db: RoomDatabase,
) : AllergenTestDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAllergenTest: EntityInsertAdapter<AllergenTest>

  private val __deleteAdapterOfAllergenTest: EntityDeleteOrUpdateAdapter<AllergenTest>

  private val __updateAdapterOfAllergenTest: EntityDeleteOrUpdateAdapter<AllergenTest>
  init {
    this.__db = __db
    this.__insertAdapterOfAllergenTest = object : EntityInsertAdapter<AllergenTest>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `allergen_tests` (`id`,`food`,`startDate`,`day`,`status`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AllergenTest) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.food)
        statement.bindText(3, entity.startDate)
        statement.bindLong(4, entity.day.toLong())
        statement.bindText(5, entity.status)
        statement.bindText(6, entity.notes)
      }
    }
    this.__deleteAdapterOfAllergenTest = object : EntityDeleteOrUpdateAdapter<AllergenTest>() {
      protected override fun createQuery(): String = "DELETE FROM `allergen_tests` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AllergenTest) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfAllergenTest = object : EntityDeleteOrUpdateAdapter<AllergenTest>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `allergen_tests` SET `id` = ?,`food` = ?,`startDate` = ?,`day` = ?,`status` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AllergenTest) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.food)
        statement.bindText(3, entity.startDate)
        statement.bindLong(4, entity.day.toLong())
        statement.bindText(5, entity.status)
        statement.bindText(6, entity.notes)
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insertTest(test: AllergenTest): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfAllergenTest.insertAndReturnId(_connection, test)
    _result
  }

  public override suspend fun deleteTest(test: AllergenTest): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfAllergenTest.handle(_connection, test)
  }

  public override suspend fun updateTest(test: AllergenTest): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfAllergenTest.handle(_connection, test)
  }

  public override fun getAllTests(): Flow<List<AllergenTest>> {
    val _sql: String = "SELECT * FROM allergen_tests ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("allergen_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFood: Int = getColumnIndexOrThrow(_stmt, "food")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfDay: Int = getColumnIndexOrThrow(_stmt, "day")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<AllergenTest> = mutableListOf()
        while (_stmt.step()) {
          val _item: AllergenTest
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFood: String
          _tmpFood = _stmt.getText(_columnIndexOfFood)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpDay: Int
          _tmpDay = _stmt.getLong(_columnIndexOfDay).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = AllergenTest(_tmpId,_tmpFood,_tmpStartDate,_tmpDay,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllTests() {
    val _sql: String = "DELETE FROM allergen_tests"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTestById(id: Long) {
    val _sql: String = "DELETE FROM allergen_tests WHERE id = ?"
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
