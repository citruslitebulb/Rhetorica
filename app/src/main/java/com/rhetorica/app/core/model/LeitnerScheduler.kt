package com.rhetorica.app.core.model

import java.util.concurrent.TimeUnit

/**
 * Leitner-box spaced repetition for individual words.
 *
 * Every quiz answer moves a word one box up (correct) or back to box 0 (incorrect).
 * Each box carries a review interval; a word is "due" once that interval has elapsed
 * and "mastered" once it reaches [MASTERED_BOX]. Words that have never been quizzed
 * have no row and are treated as due immediately.
 */
object LeitnerScheduler {
    /** Box index at which a word counts as mastered. */
    const val MASTERED_BOX = 4

    private val intervalsMillis: LongArray = longArrayOf(
        0L, // box 0: just missed, review in the next session
        TimeUnit.DAYS.toMillis(1),
        TimeUnit.DAYS.toMillis(3),
        TimeUnit.DAYS.toMillis(7),
        TimeUnit.DAYS.toMillis(14),
        TimeUnit.DAYS.toMillis(30),
    )

    val maxBox: Int get() = intervalsMillis.lastIndex

    fun intervalMillisFor(box: Int): Long = intervalsMillis[box.coerceIn(0, maxBox)]

    fun nextBox(currentBox: Int, correct: Boolean): Int {
        return if (correct) (currentBox + 1).coerceAtMost(maxBox) else 0
    }

    fun nextDueAt(box: Int, nowMillis: Long): Long = nowMillis + intervalMillisFor(box)

    fun isMastered(box: Int): Boolean = box >= MASTERED_BOX

    data class Review(
        val box: Int,
        val nextDueAtEpochMillis: Long,
    )

    /** Compute the state a word should move to after an answer at [nowMillis]. */
    fun review(currentBox: Int, correct: Boolean, nowMillis: Long): Review {
        val box = nextBox(currentBox, correct)
        return Review(box = box, nextDueAtEpochMillis = nextDueAt(box, nowMillis))
    }
}
