package com.rhetorica.app.core.model

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeitnerSchedulerTest {

    private val now = 1_000_000L

    @Test
    fun `correct answers climb one box and space the next review further out`() {
        val first = LeitnerScheduler.review(currentBox = 0, correct = true, nowMillis = now)
        assertEquals(1, first.box)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), first.nextDueAtEpochMillis)

        val second = LeitnerScheduler.review(currentBox = first.box, correct = true, nowMillis = now)
        assertEquals(2, second.box)
        assertEquals(now + TimeUnit.DAYS.toMillis(3), second.nextDueAtEpochMillis)
    }

    @Test
    fun `an incorrect answer drops the word back to box zero and makes it due now`() {
        val review = LeitnerScheduler.review(currentBox = 4, correct = false, nowMillis = now)
        assertEquals(0, review.box)
        assertEquals(now, review.nextDueAtEpochMillis)
    }

    @Test
    fun `box is capped at the top interval`() {
        val review = LeitnerScheduler.review(currentBox = LeitnerScheduler.maxBox, correct = true, nowMillis = now)
        assertEquals(LeitnerScheduler.maxBox, review.box)
        assertEquals(now + TimeUnit.DAYS.toMillis(30), review.nextDueAtEpochMillis)
    }

    @Test
    fun `mastery begins at the configured box`() {
        assertFalse(LeitnerScheduler.isMastered(LeitnerScheduler.MASTERED_BOX - 1))
        assertTrue(LeitnerScheduler.isMastered(LeitnerScheduler.MASTERED_BOX))
        assertTrue(LeitnerScheduler.isMastered(LeitnerScheduler.maxBox))
    }
}
