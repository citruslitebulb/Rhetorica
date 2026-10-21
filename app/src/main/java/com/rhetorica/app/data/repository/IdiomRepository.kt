package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.IdiomDao
import com.rhetorica.app.data.local.IdiomEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class IdiomRepository @Inject constructor(
    private val idiomDao: IdiomDao,
) {
    fun observeIdioms(): Flow<List<IdiomEntity>> = idiomDao.observeIdioms()

    fun observeById(idiomId: Long): Flow<IdiomEntity?> = idiomDao.observeById(idiomId)

    suspend fun getAll(): List<IdiomEntity> = idiomDao.getAll()
}
