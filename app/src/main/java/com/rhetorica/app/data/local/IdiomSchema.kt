package com.rhetorica.app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

internal const val RHETORICA_SCHEMA_VERSION = 20

/**
 * Schema added in database version 20.
 * Fresh installs get the same table from [IdiomEntity]; upgrades run [migrate].
 */
internal object IdiomSchema {
    const val CREATE_IDIOMS_TABLE = """
        CREATE TABLE IF NOT EXISTS `idioms` (
            `id` INTEGER NOT NULL,
            `idiom` TEXT NOT NULL,
            `meaning` TEXT NOT NULL,
            `example` TEXT NOT NULL,
            `complexity` TEXT NOT NULL,
            `origin` TEXT,
            `oratorId` INTEGER,
            `quoteId` INTEGER,
            PRIMARY KEY(`id`)
        )
    """

    const val ADD_SHOW_IDIOM_OF_DAY =
        "ALTER TABLE user_preferences ADD COLUMN showIdiomOfDay INTEGER NOT NULL DEFAULT 1"

    fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(CREATE_IDIOMS_TABLE)
        database.execSQL(ADD_SHOW_IDIOM_OF_DAY)
    }
}
