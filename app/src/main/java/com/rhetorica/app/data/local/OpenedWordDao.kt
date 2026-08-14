package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OpenedWordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNew(entity: OpenedWordEntity): Long

    @Query("SELECT COUNT(*) FROM opened_words")
    fun observeOpenedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM opened_words")
    suspend fun openedCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM opened_words WHERE wordId = :wordId)")
    fun observeIsOpened(wordId: Long): Flow<Boolean>

    @Query("SELECT wordId FROM opened_words")
    fun observeOpenedWordIds(): Flow<List<Long>>
}
