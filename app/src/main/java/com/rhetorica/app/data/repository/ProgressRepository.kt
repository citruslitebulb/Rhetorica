package com.rhetorica.app.data.repository

import androidx.room.withTransaction
import com.rhetorica.app.data.local.ProgressDao
import com.rhetorica.app.data.local.ProgressEntity
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.SavedWordDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao,
    private val savedWordDao: SavedWordDao,
    private val database: RhetoricaDatabase,
) {
    fun observeProgress(): Flow<ProgressEntity?> = progressDao.observeProgress()

    suspend fun recordWordViewed() {
        database.withTransaction {
            val current = getOrCreate()
            progressDao.upsertProgress(
                current.copy(
                    viewedCount = current.viewedCount + 1,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun recordQuizCorrect() {
        database.withTransaction {
            val current = getOrCreate()
            progressDao.upsertProgress(
                current.copy(
                    quizCorrectCount = current.quizCorrectCount + 1,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun syncSavedCount() {
        database.withTransaction {
            val current = getOrCreate()
            val savedCount = savedWordDao.savedCount()
            if (current.savedCount != savedCount) {
                progressDao.upsertProgress(
                    current.copy(
                        savedCount = savedCount,
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
            updatedAtEpochMillis = System.currentTimeMillis(),
        ).also { progressDao.upsertProgress(it) }
    }
}
