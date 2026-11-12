package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.BabyProfile
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BabyProfileDao_Impl(
  __db: RoomDatabase,
) : BabyProfileDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBabyProfile: EntityInsertAdapter<BabyProfile>
  init {
    this.__db = __db
    this.__insertAdapterOfBabyProfile = object : EntityInsertAdapter<BabyProfile>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `baby_profile` (`id`,`name`,`ageMonths`,`startDate`,`notes`,`sex`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BabyProfile) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.ageMonths.toLong())
        statement.bindText(4, entity.startDate)
        statement.bindText(5, entity.notes)
        statement.bindText(6, entity.sex)
      }
    }
  }

  public override suspend fun insertOrUpdateProfile(profile: BabyProfile): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBabyProfile.insert(_connection, profile)
  }

  public override fun getProfile(): Flow<BabyProfile?> {
    val _sql: String = "SELECT * FROM baby_profile WHERE id = 1 LIMIT 1"
    return createFlow(__db, false, arrayOf("baby_profile")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAgeMonths: Int = getColumnIndexOrThrow(_stmt, "ageMonths")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfSex: Int = getColumnIndexOrThrow(_stmt, "sex")
        val _result: BabyProfile?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAgeMonths: Int
          _tmpAgeMonths = _stmt.getLong(_columnIndexOfAgeMonths).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpSex: String
          _tmpSex = _stmt.getText(_columnIndexOfSex)
          _result = BabyProfile(_tmpId,_tmpName,_tmpAgeMonths,_tmpStartDate,_tmpNotes,_tmpSex)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
