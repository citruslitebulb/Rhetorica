package com.rhetorica.app.feature.quiz

import com.rhetorica.app.data.local.IdiomEntity
import kotlin.random.Random

/**
 * Multiple-choice idiom questions: "What does 'X' mean?"
 * Distractors are other idiom meanings. Letter guess does not use this type.
 */
object IdiomQuizBuilder {
    const val MIN_OPTIONS = 4
    private const val ASK_BELOW = 0.35

    /**
     * Library multiple choice may ask an idiom. Saved-word rounds and letter guess
     * never do: idioms are not saved words and are not spellable headwords.
     */
    fun shouldAsk(libraryPool: Boolean, idiomCount: Int, roll: Double): Boolean {
        return libraryPool && idiomCount >= MIN_OPTIONS && roll < ASK_BELOW
    }

    fun question(idiom: String): String = "What does '$idiom' mean?"

    fun buildOptions(
        correct: IdiomEntity,
        candidates: List<IdiomEntity>,
        optionCount: Int,
        random: Random,
    ): List<IdiomEntity> {
        if (optionCount < 2) return emptyList()
        val usedMeanings = mutableSetOf(normalize(correct.meaning))
        val distractors = candidates
            .filter { it.id != correct.id }
            .shuffled(random)
            .filter { usedMeanings.add(normalize(it.meaning)) }
            .take(optionCount - 1)
        if (distractors.size < optionCount - 1) return emptyList()
        return (distractors + correct).shuffled(random)
    }

    private fun normalize(meaning: String): String = meaning.trim().lowercase()
}
