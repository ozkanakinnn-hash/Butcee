package akinai.minikgurme.`data`.local

import akinai.minikgurme.`data`.model.FavoriteRecipe
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FavoriteRecipeDao_Impl(
  __db: RoomDatabase,
) : FavoriteRecipeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFavoriteRecipe: EntityInsertAdapter<FavoriteRecipe>
  init {
    this.__db = __db
    this.__insertAdapterOfFavoriteRecipe = object : EntityInsertAdapter<FavoriteRecipe>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `favorite_recipes` (`recipeId`,`timestamp`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteRecipe) {
        statement.bindText(1, entity.recipeId)
        statement.bindLong(2, entity.timestamp)
      }
    }
  }

  public override suspend fun addFavorite(favorite: FavoriteRecipe): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFavoriteRecipe.insert(_connection, favorite)
  }

  public override fun getAllFavoriteIds(): Flow<List<String>> {
    val _sql: String = "SELECT recipeId FROM favorite_recipes"
    return createFlow(__db, false, arrayOf("favorite_recipes")) { _connection ->
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

  public override suspend fun isFavorite(recipeId: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE recipeId = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, recipeId)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeFavorite(recipeId: String) {
    val _sql: String = "DELETE FROM favorite_recipes WHERE recipeId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, recipeId)
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
