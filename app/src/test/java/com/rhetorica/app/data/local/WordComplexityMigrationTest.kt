package com.rhetorica.app.data.local

import com.rhetorica.app.core.model.WordComplexity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v18 → v19 keeps the `wordComplexity` column and rewrites a row only when
 * [WordComplexity.migrationUpdates] says the stored text is not canonical.
 * Single-select installs already store `all`, `basic`, `intermediate`, or `advanced`.
 */
class WordComplexityMigrationTest {
    @Test
    fun `legacy single select values are not rewritten`() {
        val rows = listOf(
            1 to "all",
            2 to "basic",
            3 to "intermediate",
            4 to "advanced",
        )
        assertTrue(WordComplexity.migrationUpdates(rows).isEmpty())
        rows.forEach { (_, raw) ->
            assertEquals(raw, WordComplexity.migratedStorage(raw))
        }
    }

    @Test
    fun `non canonical rows keep the tiers they name`() {
        assertEquals(
            listOf(
                1 to "basic,advanced",
                2 to "all",
                3 to "all",
                4 to "intermediate",
                5 to "all",
                6 to "basic",
                7 to "all",
            ),
            WordComplexity.migrationUpdates(
                listOf(
                    1 to "advanced,basic",
                    2 to "nope",
                    3 to null,
                    4 to " intermediate ",
                    5 to "",
                    6 to "basic,nope",
                    7 to "advanced,all",
                ),
            ),
        )
    }

    @Test
    fun `an upgraded single tier still selects only that tier`() {
        listOf(
            "basic" to WordComplexity.Basic,
            "intermediate" to WordComplexity.Intermediate,
            "advanced" to WordComplexity.Advanced,
            "all" to WordComplexity.All,
        ).forEach { (stored, expected) ->
            val upgraded = WordComplexity.fromStorage(WordComplexity.migratedStorage(stored))
            assertEquals(expected, upgraded)
        }
    }
}
