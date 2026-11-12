package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.GrowthRecord
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlin.Float
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
public class GrowthRecordDao_Impl(
  __db: RoomDatabase,
) : GrowthRecordDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGrowthRecord: EntityInsertAdapter<GrowthRecord>

  private val __deleteAdapterOfGrowthRecord: EntityDeleteOrUpdateAdapter<GrowthRecord>

  private val __updateAdapterOfGrowthRecord: EntityDeleteOrUpdateAdapter<GrowthRecord>
  init {
    this.__db = __db
    this.__insertAdapterOfGrowthRecord = object : EntityInsertAdapter<GrowthRecord>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `growth_records` (`id`,`date`,`weightKg`,`heightCm`,`headCircumferenceCm`,`ageMonths`,`notes`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GrowthRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.weightKg.toDouble())
        statement.bindDouble(4, entity.heightCm.toDouble())
        val _tmpHeadCircumferenceCm: Float? = entity.headCircumferenceCm
        if (_tmpHeadCircumferenceCm == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpHeadCircumferenceCm.toDouble())
        }
        statement.bindLong(6, entity.ageMonths.toLong())
        statement.bindText(7, entity.notes)
        statement.bindLong(8, entity.timestamp)
      }
    }
    this.__deleteAdapterOfGrowthRecord = object : EntityDeleteOrUpdateAdapter<GrowthRecord>() {
      protected override fun createQuery(): String = "DELETE FROM `growth_records` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GrowthRecord) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfGrowthRecord = object : EntityDeleteOrUpdateAdapter<GrowthRecord>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `growth_records` SET `id` = ?,`date` = ?,`weightKg` = ?,`heightCm` = ?,`headCircumferenceCm` = ?,`ageMonths` = ?,`notes` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GrowthRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.weightKg.toDouble())
        statement.bindDouble(4, entity.heightCm.toDouble())
        val _tmpHeadCircumferenceCm: Float? = entity.headCircumferenceCm
        if (_tmpHeadCircumferenceCm == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpHeadCircumferenceCm.toDouble())
        }
        statement.bindLong(6, entity.ageMonths.toLong())
        statement.bindText(7, entity.notes)
        statement.bindLong(8, entity.timestamp)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insertRecord(record: GrowthRecord): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfGrowthRecord.insertAndReturnId(_connection, record)
    _result
  }

  public override suspend fun deleteRecord(record: GrowthRecord): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfGrowthRecord.handle(_connection, record)
  }

  public override suspend fun updateRecord(record: GrowthRecord): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfGrowthRecord.handle(_connection, record)
  }

  public override fun getAllGrowthRecords(): Flow<List<GrowthRecord>> {
    val _sql: String = "SELECT * FROM growth_records ORDER BY date ASC, timestamp ASC"
    return createFlow(__db, false, arrayOf("growth_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfHeadCircumferenceCm: Int = getColumnIndexOrThrow(_stmt, "headCircumferenceCm")
        val _columnIndexOfAgeMonths: Int = getColumnIndexOrThrow(_stmt, "ageMonths")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<GrowthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: GrowthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpWeightKg: Float
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg).toFloat()
          val _tmpHeightCm: Float
          _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm).toFloat()
          val _tmpHeadCircumferenceCm: Float?
          if (_stmt.isNull(_columnIndexOfHeadCircumferenceCm)) {
            _tmpHeadCircumferenceCm = null
          } else {
            _tmpHeadCircumferenceCm = _stmt.getDouble(_columnIndexOfHeadCircumferenceCm).toFloat()
          }
          val _tmpAgeMonths: Int
          _tmpAgeMonths = _stmt.getLong(_columnIndexOfAgeMonths).toInt()
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = GrowthRecord(_tmpId,_tmpDate,_tmpWeightKg,_tmpHeightCm,_tmpHeadCircumferenceCm,_tmpAgeMonths,_tmpNotes,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getLatestGrowthRecord(): Flow<GrowthRecord?> {
    val _sql: String = "SELECT * FROM growth_records ORDER BY date DESC, timestamp DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("growth_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfHeadCircumferenceCm: Int = getColumnIndexOrThrow(_stmt, "headCircumferenceCm")
        val _columnIndexOfAgeMonths: Int = getColumnIndexOrThrow(_stmt, "ageMonths")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: GrowthRecord?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpWeightKg: Float
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg).toFloat()
          val _tmpHeightCm: Float
          _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm).toFloat()
          val _tmpHeadCircumferenceCm: Float?
          if (_stmt.isNull(_columnIndexOfHeadCircumferenceCm)) {
            _tmpHeadCircumferenceCm = null
          } else {
            _tmpHeadCircumferenceCm = _stmt.getDouble(_columnIndexOfHeadCircumferenceCm).toFloat()
          }
          val _tmpAgeMonths: Int
          _tmpAgeMonths = _stmt.getLong(_columnIndexOfAgeMonths).toInt()
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result = GrowthRecord(_tmpId,_tmpDate,_tmpWeightKg,_tmpHeightCm,_tmpHeadCircumferenceCm,_tmpAgeMonths,_tmpNotes,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRecordCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM growth_records"
    return createFlow(__db, false, arrayOf("growth_records")) { _connection ->
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

  public override suspend fun deleteRecordById(id: Long) {
    val _sql: String = "DELETE FROM growth_records WHERE id = ?"
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
