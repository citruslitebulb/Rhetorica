package com.vocabdaily.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, SavedWordEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VocabDailyDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun savedWordDao(): SavedWordDao
    abstract fun progressDao(): ProgressDao
}
