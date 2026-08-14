package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.local.orDefault
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class PreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
) {
    private val updateMutex = Mutex()

    fun observe(): Flow<UserPreferencesEntity> =
        userPreferencesDao.observeUserPreferences().map { it.orDefault() }

    suspend fun get(): UserPreferencesEntity = userPreferencesDao.getUserPreferences().orDefault()

    suspend fun update(transform: (UserPreferencesEntity) -> UserPreferencesEntity) {
        updateMutex.withLock {
            val current = userPreferencesDao.getUserPreferences().orDefault()
            userPreferencesDao.upsertUserPreferences(transform(current))
        }
    }
}
