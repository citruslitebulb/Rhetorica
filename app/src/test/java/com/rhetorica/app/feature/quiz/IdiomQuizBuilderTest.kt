package com.rhetorica.app.feature.quiz

import com.rhetorica.app.data.local.IdiomEntity
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdiomQuizBuilderTest {
    @Test
    fun `a question asks for the meaning and distractors are other meanings`() {
        val idioms = (1..6).map { idiom(it.toLong(), "meaning $it") }
        val correct = idioms.first()
        val options = IdiomQuizBuilder.buildOptions(correct, idioms, optionCount = 4, random = Random(7))
        assertEquals(4, options.size)
        assertEquals(1, options.count { it.id == correct.id })
        assertEquals(4, options.map { it.meaning.lowercase() }.distinct().size)
        assertTrue(options.all { it.idiom != it.meaning })
        assertEquals("What does '${correct.idiom}' mean?", IdiomQuizBuilder.question(correct.idiom))
    }

    @Test
    fun `the same seed builds the same options`() {
        val idioms = (1..8).map { idiom(it.toLong(), "meaning $it") }
        val first = IdiomQuizBuilder.buildOptions(idioms[2], idioms, 4, Random(3)).map { it.id }
        val second = IdiomQuizBuilder.buildOptions(idioms[2], idioms, 4, Random(3)).map { it.id }
        assertEquals(first, second)
    }

    @Test
    fun `duplicate meanings are not both offered`() {
        val correct = idiom(1, "to start")
        val options = IdiomQuizBuilder.buildOptions(
            correct = correct,
            candidates = listOf(
                correct,
                idiom(2, "to start"),
                idiom(3, "to wait"),
                idiom(4, "to leave"),
                idiom(5, "To Wait"),
            ),
            optionCount = 4,
            random = Random(1),
        )
        assertTrue(options.isEmpty())
    }

    @Test
    fun `too few distinct meanings yields no round`() {
        val idioms = (1..3).map { idiom(it.toLong(), "meaning $it") }
        assertTrue(IdiomQuizBuilder.buildOptions(idioms[0], idioms, 4, Random(1)).isEmpty())
    }

    @Test
    fun `idiom questions stay out of the saved pool and out of letter guess`() {
        assertFalse(IdiomQuizBuilder.shouldAsk(libraryPool = false, idiomCount = 20, roll = 0.0))
        assertFalse(IdiomQuizBuilder.shouldAsk(libraryPool = true, idiomCount = 3, roll = 0.0))
        assertFalse(IdiomQuizBuilder.shouldAsk(libraryPool = true, idiomCount = 20, roll = 0.35))
        assertTrue(IdiomQuizBuilder.shouldAsk(libraryPool = true, idiomCount = 20, roll = 0.0))
    }

    private fun idiom(id: Long, meaning: String) = IdiomEntity(
        id = id,
        idiom = "idiom $id",
        meaning = meaning,
        example = "Example $id.",
        complexity = "intermediate",
    )
}
