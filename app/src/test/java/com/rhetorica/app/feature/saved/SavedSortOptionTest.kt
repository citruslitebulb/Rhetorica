package com.rhetorica.app.feature.saved

import com.rhetorica.app.data.local.SavedWordSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedSortOptionTest {

    private fun makeWord(
        id: Long = 1,
        word: String = "Test",
        savedAt: Long = 1000L,
        partOfSpeech: String = "noun"
    ) = SavedWordSummary(
        id = id,
        word = word,
        definition = "A test definition",
        example = "An example sentence.",
        partOfSpeech = partOfSpeech,
        oratorId = 1L,
        savedAtEpochMillis = savedAt
    )

    @Test
    fun `Newest sorts by savedAt descending`() {
        val words = listOf(
            makeWord(id = 1, savedAt = 1000L),
            makeWord(id = 2, savedAt = 3000L),
            makeWord(id = 3, savedAt = 2000L)
        )

        val sorted = SavedSortOption.Newest.sort(words)

        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun `Alphabetical sorts case-insensitively by word`() {
        val words = listOf(
            makeWord(id = 1, word = "zebra"),
            makeWord(id = 2, word = "Apple"),
            makeWord(id = 3, word = "banana")
        )

        val sorted = SavedSortOption.Alphabetical.sort(words)

        assertEquals(listOf("Apple", "banana", "zebra"), sorted.map { it.word })
    }

    @Test
    fun `PartOfSpeech sorts by partOfSpeech then word`() {
        val words = listOf(
            makeWord(id = 1, word = "Zebra", partOfSpeech = "noun"),
            makeWord(id = 2, word = "Apple", partOfSpeech = "verb"),
            makeWord(id = 3, word = "Banana", partOfSpeech = "noun"),
            makeWord(id = 4, word = "Cherry", partOfSpeech = "adjective")
        )

        val sorted = SavedSortOption.PartOfSpeech.sort(words)

        assertEquals(
            listOf("Cherry", "Apple", "Banana", "Zebra"),
            sorted.map { it.word }
        )
        assertEquals(
            listOf("adjective", "verb", "noun", "noun"),
            sorted.map { it.partOfSpeech }
        )
    }

    @Test
    fun `empty list returns empty for all options`() {
        val empty = emptyList<SavedWordSummary>()

        assertEquals(empty, SavedSortOption.Newest.sort(empty))
        assertEquals(empty, SavedSortOption.Alphabetical.sort(empty))
        assertEquals(empty, SavedSortOption.PartOfSpeech.sort(empty))
    }
}
