package com.rhetorica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {
    @Query("SELECT wordId FROM saved_words")
    fun observeSavedWordIds(): Flow<List<Long>>

    @Query("SELECT * FROM saved_words ORDER BY savedAtEpochMillis DESC")
    fun observeSavedWords(): Flow<List<SavedWordEntity>>

    @Query(
        """
        SELECT
            words.id AS id,
            words.word AS word,
            words.definition AS definition,
            words.example AS example,
            words.partOfSpeech AS partOfSpeech,
            words.oratorId AS oratorId,
            words.source AS source,
            words.speech AS speech,
            words.categories AS categories,
            saved_words.savedAtEpochMillis AS savedAtEpochMillis
        FROM saved_words
        INNER JOIN words ON words.id = saved_words.wordId
        ORDER BY saved_words.savedAtEpochMillis DESC
        """,
    )
    fun observeSavedWordSummaries(): Flow<List<SavedWordSummary>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_words WHERE wordId = :wordId)")
    fun observeIsWordSaved(wordId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_words WHERE wordId = :wordId)")
    suspend fun isWordSaved(wordId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWord(entity: SavedWordEntity)

    @Query("DELETE FROM saved_words WHERE wordId = :wordId")
    suspend fun unsaveWord(wordId: Long)

    /** Drop favorites that no longer have a matching word row (e.g. after seed prune). */
    @Query(
        """
        DELETE FROM saved_words
        WHERE wordId NOT IN (SELECT id FROM words)
        """,
    )
    suspend fun deleteOrphanedSavedWords()

    @Query("SELECT COUNT(*) FROM saved_words")
    suspend fun savedCount(): Int
}
