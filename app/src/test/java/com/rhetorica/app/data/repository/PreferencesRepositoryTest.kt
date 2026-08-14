package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PreferencesRepositoryTest {

    @Test
    fun `concurrent updates apply on top of each other`() = runBlocking {
        val dao = FakeUserPreferencesDao(upsertDelayMs = 25)
        val repository = PreferencesRepository(dao)

        val first = async {
            repository.update { it.copy(selectedOratorId = 5L, rotateThroughAll = false) }
        }
        val second = async {
            delay(5)
            repository.update { it.copy(notificationsEnabled = false) }
        }
        first.await()
        second.await()

        val stored = repository.get()
        assertEquals(5L, stored.selectedOratorId)
        assertFalse(stored.notificationsEnabled)
        assertFalse(stored.rotateThroughAll)
    }

    private class FakeUserPreferencesDao(
        private val upsertDelayMs: Long,
    ) : UserPreferencesDao {
        private val flow = MutableStateFlow<UserPreferencesEntity?>(null)

        override fun observeUserPreferences(): Flow<UserPreferencesEntity?> = flow

        override suspend fun getUserPreferences(): UserPreferencesEntity? = flow.value

        override suspend fun upsertUserPreferences(preferences: UserPreferencesEntity) {
            if (upsertDelayMs > 0) delay(upsertDelayMs)
            flow.value = preferences
        }
    }
}
