package com.rhetorica.app.data.repository

import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.DictionaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepository @Inject constructor(
    private val dictionaryDao: DictionaryDao,
) {
    fun observeOratorProfiles(): Flow<List<OratorProfile>> {
        return dictionaryDao.observeDictionaries().map { entities ->
            entities.map { it.toOratorProfile() }
        }
    }

    fun observeActiveOratorProfiles(): Flow<List<OratorProfile>> {
        return dictionaryDao.observeActiveDictionaries().map { entities ->
            entities.map { it.toOratorProfile() }
        }
    }

    suspend fun getOratorProfileById(id: Long): OratorProfile? {
        val entity = dictionaryDao.getDictionaryById(id) ?: return null
        return entity.toOratorProfile()
    }

    suspend fun updateOratorActiveStatus(id: Long, isActive: Boolean) {
        dictionaryDao.updateDictionaryActiveStatus(id, isActive)
    }

    private fun DictionaryEntity.toOratorProfile(): OratorProfile {
        return OratorProfile(
            id = id,
            name = name,
            category = category,
            era = era,
            bio = bio,
            portraitUrl = portraitUrl,
            primaryStyle = primaryStyle,
            voiceStyle = voiceStyle,
            colorAccent = colorAccent,
            sampleSpeech = sampleSpeech,
            tags = tags,
            isActive = isActive,
        )
    }
}
