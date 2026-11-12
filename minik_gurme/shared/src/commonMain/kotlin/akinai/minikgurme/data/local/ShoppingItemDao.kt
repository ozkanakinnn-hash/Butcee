package akinai.minikgurme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import akinai.minikgurme.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY checked ASC, timestamp ASC")
    fun getAll(): Flow<List<ShoppingItem>>

    @Insert
    suspend fun insertAll(items: List<ShoppingItem>)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteChecked()
}
