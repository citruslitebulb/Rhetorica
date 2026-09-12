package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordProgressDao {
    @Query("SELECT * FROM word_progress WHERE wordId = :wordId LIMIT 1")
    suspend fun get(wordId: Long): WordProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WordProgressEntity)

    @Query("SELECT COUNT(*) FROM word_progress WHERE box >= :minBox")
    fun observeMasteredCount(minBox: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM word_progress WHERE nextDueAtEpochMillis <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    /** Words whose review is due, scoped to the given orators, soonest first. */
    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN word_progress p ON words.id = p.wordId
        WHERE p.nextDueAtEpochMillis <= :now
          AND words.oratorId IN (:oratorIds)
        ORDER BY p.nextDueAtEpochMillis ASC, RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getDueWordsInOrators(now: Long, oratorIds: List<Long>, limit: Int): List<WordEntity>

    /** Due words restricted to the saved list. */
    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN word_progress p ON words.id = p.wordId
        INNER JOIN saved_words s ON words.id = s.wordId
        WHERE p.nextDueAtEpochMillis <= :now
        ORDER BY p.nextDueAtEpochMillis ASC, RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getDueSavedWords(now: Long, limit: Int): List<WordEntity>

    @Query("DELETE FROM word_progress WHERE wordId NOT IN (SELECT id FROM words)")
    suspend fun deleteOrphaned()
}
