package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun observeWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE oratorId = :oratorId ORDER BY id ASC")
    fun observeWordsByOrator(oratorId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Long): WordEntity?

    @Query("SELECT * FROM words ORDER BY id ASC LIMIT 1 OFFSET :offset")
    suspend fun getWordOfTheDay(offset: Int): WordEntity?

    @Query("SELECT * FROM words WHERE oratorId = :oratorId ORDER BY id ASC LIMIT 1 OFFSET :offset")
    suspend fun getWordOfTheDayByOrator(oratorId: Long, offset: Int): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWords(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun wordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE oratorId = :oratorId")
    suspend fun wordCountByOrator(oratorId: Long): Int
}
