package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.ShoppingItem
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ShoppingItemDao_Impl(
  __db: RoomDatabase,
) : ShoppingItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfShoppingItem: EntityInsertAdapter<ShoppingItem>
  init {
    this.__db = __db
    this.__insertAdapterOfShoppingItem = object : EntityInsertAdapter<ShoppingItem>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `shopping_items` (`id`,`name`,`amount`,`factor`,`emoji`,`source`,`checked`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ShoppingItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.amount)
        statement.bindDouble(4, entity.factor.toDouble())
        statement.bindText(5, entity.emoji)
        statement.bindText(6, entity.source)
        val _tmp: Int = if (entity.checked) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.timestamp)
      }
    }
  }

  public override suspend fun insertAll(items: List<ShoppingItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfShoppingItem.insert(_connection, items)
  }

  public override fun getAll(): Flow<List<ShoppingItem>> {
    val _sql: String = "SELECT * FROM shopping_items ORDER BY checked ASC, timestamp ASC"
    return createFlow(__db, false, arrayOf("shopping_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfFactor: Int = getColumnIndexOrThrow(_stmt, "factor")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfChecked: Int = getColumnIndexOrThrow(_stmt, "checked")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<ShoppingItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShoppingItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: String
          _tmpAmount = _stmt.getText(_columnIndexOfAmount)
          val _tmpFactor: Float
          _tmpFactor = _stmt.getDouble(_columnIndexOfFactor).toFloat()
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpChecked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfChecked).toInt()
          _tmpChecked = _tmp != 0
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = ShoppingItem(_tmpId,_tmpName,_tmpAmount,_tmpFactor,_tmpEmoji,_tmpSource,_tmpChecked,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setChecked(id: Long, checked: Boolean) {
    val _sql: String = "UPDATE shopping_items SET checked = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (checked) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM shopping_items WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM shopping_items"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteChecked() {
    val _sql: String = "DELETE FROM shopping_items WHERE checked = 1"
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
