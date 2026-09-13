package com.rhetorica.app.feature.quiz

import com.rhetorica.app.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizRoundBuilderTest {

    private fun word(id: Long, text: String, pos: String = "noun") = WordEntity(
        id = id,
        word = text,
        definition = "def $text",
        example = "Example with $text.",
        partOfSpeech = pos,
        oratorId = 1L,
    )

    @Test
    fun `options never repeat the correct headword from another orator`() {
        val correct = word(1, "conviction")
        val candidates = listOf(
            correct,
            word(2, "Conviction"),
            word(3, "zeal"),
            word(4, "candor"),
            word(5, "valor"),
        )
        val options = QuizRoundBuilder.buildOptions(correct, candidates, optionCount = 4)
        assertEquals(4, options.size)
        assertEquals(1, options.count { it.word.equals("conviction", ignoreCase = true) })
        assertTrue(options.any { it.id == correct.id })
    }

    @Test
    fun `distractors prefer the correct word's part of speech`() {
        val correct = word(1, "exhort", pos = "verb")
        val candidates = listOf(
            correct,
            word(2, "rally", pos = "verb"),
            word(3, "kindle", pos = "verb"),
            word(4, "galvanize", pos = "verb"),
            word(5, "valor", pos = "noun"),
            word(6, "candor", pos = "noun"),
        )
        repeat(10) {
            val options = QuizRoundBuilder.buildOptions(correct, candidates, optionCount = 4)
            assertTrue(options.all { it.partOfSpeech == "verb" })
        }
    }

    @Test
    fun `falls back to other parts of speech when there are not enough matches`() {
        val correct = word(1, "exhort", pos = "verb")
        val candidates = listOf(
            correct,
            word(2, "rally", pos = "verb"),
            word(5, "valor", pos = "noun"),
            word(6, "candor", pos = "noun"),
        )
        val options = QuizRoundBuilder.buildOptions(correct, candidates, optionCount = 4)
        assertEquals(4, options.size)
        assertTrue(options.any { it.id == 2L })
    }

    @Test
    fun `returns empty when the pool cannot fill the round`() {
        val correct = word(1, "exhort")
        val options = QuizRoundBuilder.buildOptions(correct, listOf(correct, word(2, "rally")), optionCount = 4)
        assertTrue(options.isEmpty())
    }
}
