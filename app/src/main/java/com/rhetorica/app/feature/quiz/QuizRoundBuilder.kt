package com.rhetorica.app.feature.quiz

import com.rhetorica.app.data.local.WordEntity

/**
 * Assembles multiple-choice options so the round is a fair test of meaning.
 *
 * - Distractors never repeat the correct headword (several orators share words such
 *   as *conviction*; two identical labels would make one of them a trick answer).
 * - Distractors share the correct word's part of speech when enough are available,
 *   so a definition that reads as a noun cannot be solved by eliminating the verbs.
 */
object QuizRoundBuilder {
    fun buildOptions(
        correct: WordEntity,
        candidates: List<WordEntity>,
        optionCount: Int,
    ): List<WordEntity> {
        val correctHeadword = correct.word.trim().lowercase()
        val usedHeadwords = mutableSetOf(correctHeadword)
        val eligible = candidates
            .filter { it.id != correct.id }
            .shuffled()
            .filter { usedHeadwords.add(it.word.trim().lowercase()) }

        val samePos = eligible.filter { it.partOfSpeech.equals(correct.partOfSpeech, ignoreCase = true) }
        val otherPos = eligible.filter { it !in samePos }
        val distractors = (samePos + otherPos).take(optionCount - 1)
        if (distractors.size < optionCount - 1) return emptyList()
        return (distractors + correct).shuffled()
    }
}
