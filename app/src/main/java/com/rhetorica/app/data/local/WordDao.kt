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

    @Query("SELECT * FROM words ORDER BY id ASC")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM words WHERE oratorId = :oratorId ORDER BY id ASC")
    suspend fun getWordsByOrator(oratorId: Long): List<WordEntity>

    @Query("SELECT * FROM words WHERE oratorId IN (:oratorIds) ORDER BY id ASC")
    suspend fun getWordsByOratorIds(oratorIds: List<Long>): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :wordId LIMIT 1")
    fun observeWordById(wordId: Long): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE oratorId = :oratorId ORDER BY id ASC")
    fun observeWordsByOrator(oratorId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE oratorId IN (:oratorIds) ORDER BY id ASC")
    fun observeWordsByOratorIds(oratorIds: List<Long>): Flow<List<WordEntity>>

    @Query(
        """
        SELECT * FROM words
        WHERE word LIKE '%' || :query || '%'
           OR definition LIKE '%' || :query || '%'
           OR example LIKE '%' || :query || '%'
        ORDER BY word COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun searchWords(query: String, limit: Int = 40): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE oratorId IN (:oratorIds)
          AND (
            word LIKE '%' || :query || '%'
            OR definition LIKE '%' || :query || '%'
            OR example LIKE '%' || :query || '%'
          )
        ORDER BY word COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun searchWordsInOrators(
        query: String,
        oratorIds: List<Long>,
        limit: Int = 40,
    ): List<WordEntity>

    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN saved_words ON words.id = saved_words.wordId
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomSavedWords(limit: Int): List<WordEntity>

    @Query("SELECT COUNT(*) FROM saved_words")
    suspend fun savedWordCount(): Int

    @Query("SELECT * FROM words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Long): WordEntity?

    @Query("SELECT * FROM words ORDER BY id ASC LIMIT 1 OFFSET :offset")
    suspend fun getWordOfTheDay(offset: Int): WordEntity?

    @Query("SELECT * FROM words WHERE oratorId = :oratorId ORDER BY id ASC LIMIT 1 OFFSET :offset")
    suspend fun getWordOfTheDayByOrator(oratorId: Long, offset: Int): WordEntity?

    @Query("SELECT * FROM words WHERE oratorId IS NULL")
    suspend fun getWordsWithNullOratorId(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWords(words: List<WordEntity>)

    @Query("SELECT id FROM words")
    suspend fun getAllWordIds(): List<Long>

    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteWordsByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun wordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE oratorId = :oratorId")
    suspend fun wordCountByOrator(oratorId: Long): Int

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE oratorId = :oratorId
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomWordsByOrator(oratorId: Long, limit: Int): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE oratorId IN (:oratorIds)
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomWordsByOratorIds(oratorIds: List<Long>, limit: Int): List<WordEntity>

    /** Larger pool for letter-guess filtering (length / alphabetic-only). */
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWordsPool(limit: Int): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE oratorId = :oratorId
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomWordsPoolByOrator(oratorId: Long, limit: Int): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE oratorId IN (:oratorIds)
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomWordsPoolByOratorIds(oratorIds: List<Long>, limit: Int): List<WordEntity>
}
