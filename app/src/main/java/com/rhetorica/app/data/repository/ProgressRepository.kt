package com.rhetorica.app.data.repository

import androidx.room.withTransaction
import com.rhetorica.app.data.local.OpenedWordDao
import com.rhetorica.app.data.local.OpenedWordEntity
import com.rhetorica.app.data.local.ProgressDao
import com.rhetorica.app.data.local.ProgressEntity
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.SavedWordDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao,
    private val savedWordDao: SavedWordDao,
    private val openedWordDao: OpenedWordDao,
    private val database: RhetoricaDatabase,
) {
    fun observeProgress(): Flow<ProgressEntity?> = progressDao.observeProgress()

    fun observeUniqueWordsOpened(): Flow<Int> = openedWordDao.observeOpenedCount()

    fun observeIsWordOpened(wordId: Long): Flow<Boolean> = openedWordDao.observeIsOpened(wordId)

    fun observeProgressSnapshot(): Flow<ProgressSnapshot> {
        return combine(
            progressDao.observeProgress(),
            openedWordDao.observeOpenedCount(),
        ) { progress, uniqueOpened ->
            ProgressSnapshot(
                uniqueWordsOpened = uniqueOpened,
                savedCount = progress?.savedCount ?: 0,
                quizCorrectCount = progress?.quizCorrectCount ?: 0,
                quizAttemptCount = progress?.quizAttemptCount ?: 0,
                quizStreak = progress?.quizStreak ?: 0,
                bestQuizStreak = progress?.bestQuizStreak ?: 0,
            )
        }
    }

    /** Records a unique open. Revisits of the same word do not change the count. */
    suspend fun recordWordViewed(wordId: Long) {
        database.withTransaction {
            openedWordDao.insertIfNew(
                OpenedWordEntity(
                    wordId = wordId,
                    firstOpenedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            val current = getOrCreate()
            val unique = openedWordDao.openedCount()
            if (current.viewedCount != unique) {
                progressDao.upsertProgress(
                    current.copy(
                        viewedCount = unique,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    /** @deprecated Use [recordWordViewed] with a word id so opens stay unique. */
    suspend fun recordWordViewed() {
        // Kept for binary compatibility with older call sites; no-op without an id.
    }

    suspend fun recordQuizResult(correct: Boolean) {
        database.withTransaction {
            val current = getOrCreate()
            val nextStreak = if (correct) current.quizStreak + 1 else 0
            progressDao.upsertProgress(
                current.copy(
                    quizCorrectCount = current.quizCorrectCount + if (correct) 1 else 0,
                    quizAttemptCount = current.quizAttemptCount + 1,
                    quizStreak = nextStreak,
                    bestQuizStreak = maxOf(current.bestQuizStreak, nextStreak),
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun recordQuizCorrect() {
        recordQuizResult(correct = true)
    }

    suspend fun syncSavedCount() {
        database.withTransaction {
            val current = getOrCreate()
            val savedCount = savedWordDao.savedCount()
            val unique = openedWordDao.openedCount()
            if (current.savedCount != savedCount || current.viewedCount != unique) {
                progressDao.upsertProgress(
                    current.copy(
                        savedCount = savedCount,
                        viewedCount = unique,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    private suspend fun getOrCreate(): ProgressEntity {
        return progressDao.getProgress() ?: ProgressEntity(
            id = 1,
            viewedCount = 0,
            savedCount = 0,
            quizCorrectCount = 0,
            quizAttemptCount = 0,
            quizStreak = 0,
            bestQuizStreak = 0,
            updatedAtEpochMillis = System.currentTimeMillis(),
        ).also { progressDao.upsertProgress(it) }
    }
}

data class ProgressSnapshot(
    val uniqueWordsOpened: Int,
    val savedCount: Int,
    val quizCorrectCount: Int,
    val quizAttemptCount: Int,
    val quizStreak: Int,
    val bestQuizStreak: Int,
) {
    val quizAccuracyPercent: Int
        get() = if (quizAttemptCount <= 0) {
            0
        } else {
            ((quizCorrectCount * 100f) / quizAttemptCount).toInt().coerceIn(0, 100)
        }
}
