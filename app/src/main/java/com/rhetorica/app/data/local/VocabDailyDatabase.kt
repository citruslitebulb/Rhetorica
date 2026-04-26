package com.rhetorica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordEntity::class, SavedWordEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RhetoricaDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun savedWordDao(): SavedWordDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: RhetoricaDatabase? = null

        fun getDatabase(context: Context): RhetoricaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhetoricaDatabase::class.java,
                    "rhetorica.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
