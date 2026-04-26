package com.vocabdaily.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY savedAtEpochMillis DESC")
    fun observeSavedWords(): Flow<List<SavedWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWord(entity: SavedWordEntity)

    @Query("DELETE FROM saved_words WHERE wordId = :wordId")
    suspend fun unsaveWord(wordId: Long)
}
