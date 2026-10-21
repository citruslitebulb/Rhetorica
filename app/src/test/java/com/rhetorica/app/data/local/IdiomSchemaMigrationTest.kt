package com.rhetorica.app.data.local

import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v19 → v20 adds the idioms table and showIdiomOfDay, defaulting the card on.
 * [RHETORICA_SCHEMA_VERSION] is the Room database version. The SQL under test is
 * the same text [IdiomSchema] runs inside the migration.
 */
class IdiomSchemaMigrationTest {
    @Test
    fun `schema version is the idiom migration`() {
        assertEquals(20, RHETORICA_SCHEMA_VERSION)
    }

    @Test
    fun `migration adds the idiom table and leaves the show toggle on`() {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite::memory:").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE user_preferences (
                        id INTEGER PRIMARY KEY NOT NULL,
                        wordComplexity TEXT NOT NULL DEFAULT 'all'
                    )
                    """.trimIndent(),
                )
                statement.execute(
                    "INSERT INTO user_preferences (id, wordComplexity) VALUES (1, 'basic,advanced')",
                )
                statement.execute(IdiomSchema.CREATE_IDIOMS_TABLE)
                statement.execute(IdiomSchema.ADD_SHOW_IDIOM_OF_DAY)
                statement.execute(
                    "INSERT INTO user_preferences (id, wordComplexity) VALUES (2, 'all')",
                )
                statement.execute(
                    """
                    INSERT INTO idioms (id, idiom, meaning, example, complexity, origin, oratorId, quoteId)
                    VALUES (90001, 'break the ice', 'To start', 'She broke the ice.', 'beginner', NULL, NULL, NULL)
                    """.trimIndent(),
                )
            }

            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT id, wordComplexity, showIdiomOfDay FROM user_preferences ORDER BY id",
                ).use { rows ->
                    assertTrue(rows.next())
                    assertEquals(1, rows.getInt("id"))
                    assertEquals("basic,advanced", rows.getString("wordComplexity"))
                    assertEquals(1, rows.getInt("showIdiomOfDay"))
                    assertTrue(rows.next())
                    assertEquals(2, rows.getInt("id"))
                    assertEquals(1, rows.getInt("showIdiomOfDay"))
                }
            }

            val columns = linkedMapOf<String, String>()
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info(idioms)").use { info ->
                    while (info.next()) {
                        columns[info.getString("name")] = info.getString("type")
                    }
                }
            }
            assertEquals(
                listOf("id", "idiom", "meaning", "example", "complexity", "origin", "oratorId", "quoteId"),
                columns.keys.toList(),
            )
            assertEquals("INTEGER", columns.getValue("id"))
            assertEquals("TEXT", columns.getValue("origin"))
            assertEquals("INTEGER", columns.getValue("quoteId"))

            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT idiom, origin, quoteId FROM idioms WHERE id = 90001").use { row ->
                    assertTrue(row.next())
                    assertEquals("break the ice", row.getString("idiom"))
                    assertTrue(row.getString("origin") == null)
                    assertTrue(row.getObject("quoteId") == null)
                }
            }
        }
    }
}
