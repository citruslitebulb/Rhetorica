package com.rhetorica.app.data.repository

import androidx.room.withTransaction
import com.rhetorica.app.core.model.HabitProgress
import com.rhetorica.app.core.model.LeitnerScheduler
import com.rhetorica.app.data.local.OpenedWordDao
import com.rhetorica.app.data.local.OpenedWordEntity
import com.rhetorica.app.data.local.ProgressDao
import com.rhetorica.app.data.local.ProgressEntity
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.WordProgressDao
import com.rhetorica.app.data.local.WordProgressEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao,
    private val savedWordDao: SavedWordDao,
    private val openedWordDao: OpenedWordDao,
    private val wordProgressDao: WordProgressDao,
    private val database: RhetoricaDatabase,
) {
    fun observeProgress(): Flow<ProgressEntity?> = progressDao.observeProgress()

    fun observeUniqueWordsOpened(): Flow<Int> = openedWordDao.observeOpenedCount()

    fun observeIsWordOpened(wordId: Long): Flow<Boolean> = openedWordDao.observeIsOpened(wordId)

    fun observeProgressSnapshot(
        nowMillis: Long = System.currentTimeMillis(),
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): Flow<ProgressSnapshot> {
        return combine(
            progressDao.observeProgress(),
            openedWordDao.observeOpenedCount(),
            wordProgressDao.observeMasteredCount(LeitnerScheduler.MASTERED_BOX),
            wordProgressDao.observeDueCount(nowMillis),
        ) { progress, uniqueOpened, mastered, due ->
            ProgressSnapshot(
                uniqueWordsOpened = uniqueOpened,
                savedCount = progress?.savedCount ?: 0,
                quizCorrectCount = progress?.quizCorrectCount ?: 0,
                quizAttemptCount = progress?.quizAttemptCount ?: 0,
                quizStreak = progress?.quizStreak ?: 0,
                bestQuizStreak = progress?.bestQuizStreak ?: 0,
                dailyStreak = HabitProgress.displayedDailyStreak(
                    storedStreak = progress?.dailyStreak ?: 0,
                    lastActiveDate = progress?.lastActiveDate.orEmpty(),
                    today = today,
                ),
                bestDailyStreak = progress?.bestDailyStreak ?: 0,
                masteredCount = mastered,
                dueCount = due,
            )
        }
    }

    /** Records a unique open and counts the day toward the activity streak. */
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
            progressDao.upsertProgress(
                current
                    .copy(viewedCount = unique)
                    .withActivityToday()
                    .copy(updatedAtEpochMillis = System.currentTimeMillis()),
            )
        }
    }

    /**
     * Records a quiz answer for [wordId]: aggregate counters, the correct-answer
     * streak, the activity-day streak, and the word's Leitner box.
     */
    suspend fun recordQuizResult(wordId: Long, correct: Boolean) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val current = getOrCreate()
            val nextStreak = if (correct) current.quizStreak + 1 else 0
            progressDao.upsertProgress(
                current
                    .copy(
                        quizCorrectCount = current.quizCorrectCount + if (correct) 1 else 0,
                        quizAttemptCount = current.quizAttemptCount + 1,
                        quizStreak = nextStreak,
                        bestQuizStreak = maxOf(current.bestQuizStreak, nextStreak),
                    )
                    .withActivityToday()
                    .copy(updatedAtEpochMillis = now),
            )

            if (wordId > 0L) {
                val existing = wordProgressDao.get(wordId)
                val review = LeitnerScheduler.review(
                    currentBox = existing?.box ?: 0,
                    correct = correct,
                    nowMillis = now,
                )
                wordProgressDao.upsert(
                    WordProgressEntity(
                        wordId = wordId,
                        box = review.box,
                        correctCount = (existing?.correctCount ?: 0) + if (correct) 1 else 0,
                        incorrectCount = (existing?.incorrectCount ?: 0) + if (correct) 0 else 1,
                        lastReviewedAtEpochMillis = now,
                        nextDueAtEpochMillis = review.nextDueAtEpochMillis,
                    ),
                )
            }
        }
    }

    /** Words due for review, scoped to [oratorIds] (or the saved list when [savedOnly]). */
    suspend fun getDueQuizWords(
        oratorIds: Collection<Long>,
        savedOnly: Boolean,
        limit: Int,
        nowMillis: Long = System.currentTimeMillis(),
    ): List<WordEntity> {
        return when {
            savedOnly -> wordProgressDao.getDueSavedWords(nowMillis, limit)
            oratorIds.isEmpty() -> emptyList()
            else -> wordProgressDao.getDueWordsInOrators(nowMillis, oratorIds.toList(), limit)
        }
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

    private fun ProgressEntity.withActivityToday(
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): ProgressEntity {
        val next = HabitProgress.nextDailyStreak(
            currentStreak = dailyStreak,
            lastActiveDate = lastActiveDate,
            today = today,
        )
        return copy(
            dailyStreak = next.streak,
            bestDailyStreak = maxOf(bestDailyStreak, next.streak),
            lastActiveDate = next.lastActiveDate,
        )
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
    val uniqueWordsOpened: Int = 0,
    val savedCount: Int = 0,
    val quizCorrectCount: Int = 0,
    val quizAttemptCount: Int = 0,
    val quizStreak: Int = 0,
    val bestQuizStreak: Int = 0,
    val dailyStreak: Int = 0,
    val bestDailyStreak: Int = 0,
    val masteredCount: Int = 0,
    val dueCount: Int = 0,
)
