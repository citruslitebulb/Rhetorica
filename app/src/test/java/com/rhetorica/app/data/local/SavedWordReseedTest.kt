package com.rhetorica.app.data.local

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Seed writes go through Room `@Upsert` (plain INSERT, then UPDATE on a unique
 * conflict). `@Insert(REPLACE)` is `INSERT OR REPLACE`, which deletes the word
 * row first. [SavedWordEntity] uses `ON DELETE CASCADE`, so that delete drops
 * the saved row.
 */
class SavedWordReseedTest {

    @Test
    fun `reseed keeps saved words`() {
        assertSeedWriteIsUpsert("src/main/java/com/rhetorica/app/data/local/WordDao.kt", "upsertWords")
        assertSeedWriteIsUpsert(
            "src/main/java/com/rhetorica/app/data/local/DictionaryDao.kt",
            "upsertDictionaries",
        )
        assertSeedWriteIsUpsert("src/main/java/com/rhetorica/app/data/local/QuoteDao.kt", "upsertQuotes")
        assertSeedWriteIsUpsert("src/main/java/com/rhetorica/app/data/local/SpeechDao.kt", "upsertSpeeches")
        assertSeedWriteIsUpsert("src/main/java/com/rhetorica/app/data/local/IdiomDao.kt", "upsertIdioms")

        Class.forName("org.sqlite.JDBC")
        openWords().use { connection ->
            insertWord(connection, id = 7, headword = "ethos", definition = "old")
            insertSaved(connection, wordId = 7, savedAt = 1_700_000_000_000)
            upsertWord(connection, id = 7, headword = "ethos", definition = "moral character")

            assertEquals("moral character", definitionOf(connection, 7))
            assertEquals(1, savedCount(connection))
            assertEquals(1_700_000_000_000, savedAt(connection, 7))
        }

        openWords().use { connection ->
            insertWord(connection, id = 7, headword = "ethos", definition = "old")
            insertSaved(connection, wordId = 7, savedAt = 1_700_000_000_000)
            connection.createStatement().use { statement ->
                statement.execute(
                    "INSERT OR REPLACE INTO words (id, word, definition) VALUES (7, 'ethos', 'replaced')",
                )
            }
            assertEquals(0, savedCount(connection))
        }
    }

    private fun assertSeedWriteIsUpsert(relativePath: String, methodName: String) {
        val source = sourceFile(relativePath).readText()
        val methodAt = source.indexOf("fun $methodName")
        assertTrue("$methodName missing from $relativePath", methodAt >= 0)
        val annotation = source.substring(0, methodAt).substringAfterLast('@')
        assertTrue("$methodName must be @Upsert", annotation.startsWith("Upsert"))
        assertFalse(annotation.contains("REPLACE"))
    }

    private fun sourceFile(relativePath: String): File {
        val dir = File(System.getProperty("user.dir") ?: error("user.dir is unset"))
        val candidates = listOf(File(dir, relativePath), File(dir, "app/$relativePath"))
        return candidates.firstOrNull { it.isFile }
            ?: error("Cannot find $relativePath from $dir")
    }

    private fun openWords(): Connection {
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
            statement.execute(
                """
                CREATE TABLE words (
                    id INTEGER NOT NULL,
                    word TEXT NOT NULL,
                    definition TEXT NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                CREATE TABLE saved_words (
                    wordId INTEGER NOT NULL,
                    savedAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(wordId),
                    FOREIGN KEY(wordId) REFERENCES words(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
        }
        return connection
    }

    private fun insertWord(connection: Connection, id: Long, headword: String, definition: String) {
        connection.prepareStatement("INSERT INTO words (id, word, definition) VALUES (?, ?, ?)").use { statement ->
            statement.setLong(1, id)
            statement.setString(2, headword)
            statement.setString(3, definition)
            statement.executeUpdate()
        }
    }

    /**
     * Same steps as Room's `@Upsert`: insert, and on a unique conflict update
     * the existing row. An update does not fire `ON DELETE CASCADE`.
     */
    private fun upsertWord(connection: Connection, id: Long, headword: String, definition: String) {
        try {
            insertWord(connection, id, headword, definition)
        } catch (ex: SQLException) {
            val message = ex.message.orEmpty()
            if (!message.contains("unique", ignoreCase = true)) throw ex
            connection.prepareStatement(
                "UPDATE words SET word = ?, definition = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, headword)
                statement.setString(2, definition)
                statement.setLong(3, id)
                assertEquals(1, statement.executeUpdate())
            }
        }
    }

    private fun insertSaved(connection: Connection, wordId: Long, savedAt: Long) {
        connection.prepareStatement(
            "INSERT INTO saved_words (wordId, savedAtEpochMillis) VALUES (?, ?)",
        ).use { statement ->
            statement.setLong(1, wordId)
            statement.setLong(2, savedAt)
            statement.executeUpdate()
        }
    }

    private fun definitionOf(connection: Connection, id: Long): String {
        connection.prepareStatement("SELECT definition FROM words WHERE id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { rows ->
                assertTrue(rows.next())
                return rows.getString(1)
            }
        }
    }

    private fun savedCount(connection: Connection): Int {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM saved_words").use { rows ->
                assertTrue(rows.next())
                return rows.getInt(1)
            }
        }
    }

    private fun savedAt(connection: Connection, wordId: Long): Long {
        connection.prepareStatement(
            "SELECT savedAtEpochMillis FROM saved_words WHERE wordId = ?",
        ).use { statement ->
            statement.setLong(1, wordId)
            statement.executeQuery().use { rows ->
                assertTrue(rows.next())
                return rows.getLong(1)
            }
        }
    }
}
