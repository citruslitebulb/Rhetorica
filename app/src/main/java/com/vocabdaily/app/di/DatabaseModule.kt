package com.vocabdaily.app.di

import android.content.Context
import androidx.room.Room
import com.vocabdaily.app.data.local.ProgressDao
import com.vocabdaily.app.data.local.SavedWordDao
import com.vocabdaily.app.data.local.VocabDailyDatabase
import com.vocabdaily.app.data.local.WordDao
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
    fun provideDatabase(@ApplicationContext context: Context): VocabDailyDatabase {
        return Room.databaseBuilder(
            context,
            VocabDailyDatabase::class.java,
            "vocabdaily.db",
        ).build()
    }

    @Provides
    fun provideWordDao(database: VocabDailyDatabase): WordDao = database.wordDao()

    @Provides
    fun provideSavedWordDao(database: VocabDailyDatabase): SavedWordDao = database.savedWordDao()

    @Provides
    fun provideProgressDao(database: VocabDailyDatabase): ProgressDao = database.progressDao()
}
