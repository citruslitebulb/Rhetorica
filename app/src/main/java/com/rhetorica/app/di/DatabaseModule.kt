package com.rhetorica.app.di

import android.content.Context
import com.rhetorica.app.data.local.ProgressDao
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RhetoricaDatabase {
        return RhetoricaDatabase.getDatabase(context)
    }

    @Provides
    fun provideWordDao(database: RhetoricaDatabase): WordDao = database.wordDao()

    @Provides
    fun provideSavedWordDao(database: RhetoricaDatabase): SavedWordDao = database.savedWordDao()

    @Provides
    fun provideProgressDao(database: RhetoricaDatabase): ProgressDao = database.progressDao()

    @Provides
    fun provideDictionaryDao(database: RhetoricaDatabase): DictionaryDao = database.dictionaryDao()

    @Provides
    fun provideUserPreferencesDao(database: RhetoricaDatabase): UserPreferencesDao = database.userPreferencesDao()
}
