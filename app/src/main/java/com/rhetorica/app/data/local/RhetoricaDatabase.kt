package com.rhetorica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WordEntity::class,
        SavedWordEntity::class,
        ProgressEntity::class,
        DictionaryEntity::class,
        UserPreferencesEntity::class,
        QuoteEntity::class,
        SpeechEntity::class,
        OpenedWordEntity::class,
        WordProgressEntity::class,
    ],
    version = 17,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RhetoricaDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun savedWordDao(): SavedWordDao
    abstract fun progressDao(): ProgressDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun quoteDao(): QuoteDao
    abstract fun speechDao(): SpeechDao
    abstract fun openedWordDao(): OpenedWordDao
    abstract fun wordProgressDao(): WordProgressDao

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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Convert tags from String to JSON array format
                // Old format: "[\"Ancient\", \"Anti-tyranny\"]" (already JSON)
                // New format: Same, but Room will now use TypeConverter
                // No change needed for data, just column type change handled by Room
                
                // Convert favoriteOratorIds from String to JSON array format
                // Old format: "[1, 2, 3]" (already JSON)
                // New format: Same, but Room will now use TypeConverter
                // No change needed for data, just column type change handled by Room
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes - version bump for new seed data
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN widgetBackgroundColor INTEGER NOT NULL DEFAULT -13877680",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN widgetBackgroundOpacityPercent INTEGER NOT NULL DEFAULT 80",
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE words ADD COLUMN complexity TEXT NOT NULL DEFAULT 'intermediate'",
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New quotes table for famous speech excerpts per orator
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quotes` (
                        `id` INTEGER NOT NULL,
                        `oratorId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `speech` TEXT,
                        `year` INTEGER,
                        `context` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_quotes_oratorId` ON `quotes` (`oratorId`)"
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add source and speech columns to words for authentic quote context
                database.execSQL("ALTER TABLE words ADD COLUMN source TEXT")
                database.execSQL("ALTER TABLE words ADD COLUMN speech TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Full speeches table, linked from word examples for richer context
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `speeches` (
                        `id` INTEGER NOT NULL,
                        `oratorId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `fullText` TEXT NOT NULL,
                        `year` INTEGER,
                        `description` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_speeches_oratorId` ON `speeches` (`oratorId`)"
                )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add categories (thematic tags) to words for filtering by inspirational, tech, humanities, arts, leadership, democracy, courage, legacy, etc.
                database.execSQL("ALTER TABLE words ADD COLUMN categories TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add selected theme categories to user preferences (global filter for Home feed, controlled from Profile)
                database.execSQL("ALTER TABLE user_preferences ADD COLUMN selectedThemeCategories TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add themeCategories (thematic tags like leadership, democracy, etc.) to dictionaries/orators
                // so that the Profile themes filter can also apply to which orators are available/shown in the feed.
                database.execSQL("ALTER TABLE dictionaries ADD COLUMN themeCategories TEXT NOT NULL DEFAULT '[]'")
            }
        }

        /**
         * Make quotes.source nullable so seed rows without a source field load cleanly.
         * SQLite cannot ALTER nullability in-place — rebuild the table and preserve data.
         */
        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quotes_new` (
                        `id` INTEGER NOT NULL,
                        `oratorId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `source` TEXT,
                        `speech` TEXT,
                        `year` INTEGER,
                        `context` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT INTO `quotes_new` (`id`, `oratorId`, `text`, `source`, `speech`, `year`, `context`)
                    SELECT `id`, `oratorId`, `text`, `source`, `speech`, `year`, `context` FROM `quotes`
                    """.trimIndent(),
                )
                database.execSQL("DROP TABLE `quotes`")
                database.execSQL("ALTER TABLE `quotes_new` RENAME TO `quotes`")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_quotes_oratorId` ON `quotes` (`oratorId`)",
                )
            }
        }

        /**
         * Habit-loop prefs, unique word opens, quiz accuracy/streak, optional pronunciation,
         * and widget image background keys. Existing installs skip onboarding
         * (`onboardingCompleted` defaults to 1).
         */
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN widgetBackgroundImageKey TEXT NOT NULL DEFAULT 'none'",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN widgetGalleryUri TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN onboardingCompleted INTEGER NOT NULL DEFAULT 1",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN notificationsEnabled INTEGER NOT NULL DEFAULT 1",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN notificationHour INTEGER NOT NULL DEFAULT 8",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN notificationMinute INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'system'",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN includeFictionalOrators INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN includeLiteraryOrators INTEGER NOT NULL DEFAULT 1",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN shownWotdIds TEXT NOT NULL DEFAULT '[]'",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN shownWotdPoolKey TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN todaysWotdId INTEGER",
                )
                database.execSQL(
                    "ALTER TABLE user_preferences ADD COLUMN todaysWotdDate TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL("ALTER TABLE words ADD COLUMN pronunciation TEXT")
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN quizAttemptCount INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN quizStreak INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN bestQuizStreak INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS opened_words (
                        wordId INTEGER NOT NULL,
                        firstOpenedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(wordId)
                    )
                    """.trimIndent(),
                )
            }
        }

        /**
         * Per-word Leitner state for the quiz plus a consecutive-days activity streak.
         */
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN dailyStreak INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN bestDailyStreak INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN lastActiveDate TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `word_progress` (
                        `wordId` INTEGER NOT NULL,
                        `box` INTEGER NOT NULL,
                        `correctCount` INTEGER NOT NULL,
                        `incorrectCount` INTEGER NOT NULL,
                        `lastReviewedAtEpochMillis` INTEGER NOT NULL,
                        `nextDueAtEpochMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`wordId`)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_word_progress_nextDueAtEpochMillis` " +
                        "ON `word_progress` (`nextDueAtEpochMillis`)",
                )
            }
        }

        fun getDatabase(context: Context): RhetoricaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhetoricaDatabase::class.java,
                    "rhetorica.db",
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                    )
                    // No destructive fallback: a missing migration must fail loudly in
                    // development rather than silently wiping saved words and progress.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
