package akinai.minikgurme.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import akinai.minikgurme.data.model.FeedingDiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDiaryDao {
    @Query("SELECT * FROM feeding_diary ORDER BY date DESC, time DESC, timestamp DESC")
    fun getAllDiaryEntries(): Flow<List<FeedingDiaryEntry>>

    @Query("SELECT * FROM feeding_diary WHERE date = :date ORDER BY time ASC, timestamp ASC")
    fun getEntriesForDate(date: String): Flow<List<FeedingDiaryEntry>>

    @Query("SELECT * FROM feeding_diary WHERE foodItems LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY date DESC, timestamp DESC")
    fun searchEntries(query: String): Flow<List<FeedingDiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FeedingDiaryEntry): Long

    @Update
    suspend fun updateEntry(entry: FeedingDiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: FeedingDiaryEntry)

    @Query("DELETE FROM feeding_diary WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT COUNT(*) FROM feeding_diary")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM feeding_diary ORDER BY date DESC")
    fun getDistinctDates(): Flow<List<String>>
}
