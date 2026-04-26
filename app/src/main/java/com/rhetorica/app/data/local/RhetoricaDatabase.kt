package com.rhetorica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordEntity::class, SavedWordEntity::class, ProgressEntity::class, DictionaryEntity::class, UserPreferencesEntity::class],
    version = 4,
    exportSchema = false,
    typeConverters = [Converters::class],
)
abstract class RhetoricaDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun savedWordDao(): SavedWordDao
    abstract fun progressDao(): ProgressDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: RhetoricaDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dictionaries (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        oratorName TEXT NOT NULL,
                        wordCount INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        era TEXT NOT NULL,
                        bio TEXT NOT NULL,
                        portraitUrl TEXT NOT NULL,
                        primaryStyle TEXT NOT NULL,
                        voiceStyle TEXT NOT NULL,
                        colorAccent INTEGER NOT NULL,
                        sampleSpeech TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_preferences (
                        id INTEGER PRIMARY KEY NOT NULL,
                        favoriteOratorIds TEXT NOT NULL,
                        rotateThroughAll INTEGER NOT NULL DEFAULT 0,
                        selectedOratorId INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN oratorId INTEGER")
            }
        }

        fun getDatabase(context: Context): RhetoricaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhetoricaDatabase::class.java,
                    "rhetorica.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
