package com.rhetorica.app.feature.quiz

/**
 * Wordle-style letter evaluation.
 * Correct (exact position) beats Present (in word, wrong slot) beats Absent.
 * Duplicate letters are handled with the standard two-pass algorithm.
 */
object WordGuessEvaluator {

    fun normalize(raw: String): String =
        raw.filter { it.isLetter() }.lowercase()

    /**
     * Standard Wordle evaluation (guess and target must be the same length).
     */
    fun evaluate(guess: String, target: String): List<LetterMark> {
        val g = normalize(guess)
        val t = normalize(target)
        require(g.length == t.length) {
            "Guess length ${g.length} must match target length ${t.length}"
        }
        return evaluateInternal(g, t)
    }

    /**
     * Evaluation that allows unequal lengths (hardcore / hidden-length mode).
     * Positions beyond the target cannot be CORRECT; presence still counts against
     * unmatched target letters. A win is only possible when lengths match.
     */
    fun evaluateAllowingLengthMismatch(guess: String, target: String): List<LetterMark> {
        val g = normalize(guess)
        val t = normalize(target)
        if (g.isEmpty()) return emptyList()
        return evaluateInternal(g, t)
    }

    private fun evaluateInternal(g: String, t: String): List<LetterMark> {
        val result = Array(g.length) { LetterMark.ABSENT }
        val remaining = IntArray(26)

        // Count all target letters first.
        for (ch in t) {
            remaining[ch - 'a']++
        }

        // Pass 1: exact matches only where both sides have that index.
        for (i in g.indices) {
            if (i < t.length && g[i] == t[i]) {
                result[i] = LetterMark.CORRECT
                remaining[g[i] - 'a']--
            }
        }

        // Pass 2: present letters from remaining unmatched target slots.
        for (i in g.indices) {
            if (result[i] == LetterMark.CORRECT) continue
            val idx = g[i] - 'a'
            if (idx in remaining.indices && remaining[idx] > 0) {
                result[i] = LetterMark.PRESENT
                remaining[idx]--
            }
        }
        return result.toList()
    }

    /**
     * Merge keyboard status: CORRECT > PRESENT > ABSENT > UNUSED.
     */
    fun mergeKeyboardState(
        current: Map<Char, LetterMark>,
        guess: String,
        marks: List<LetterMark>,
    ): Map<Char, LetterMark> {
        val next = current.toMutableMap()
        val g = normalize(guess)
        g.forEachIndexed { i, ch ->
            val mark = marks.getOrNull(i) ?: return@forEachIndexed
            val existing = next[ch]
            next[ch] = when {
                existing == null -> mark
                rank(mark) > rank(existing) -> mark
                else -> existing
            }
        }
        return next
    }

    private fun rank(mark: LetterMark): Int = when (mark) {
        LetterMark.UNUSED -> 0
        LetterMark.ABSENT -> 1
        LetterMark.PRESENT -> 2
        LetterMark.CORRECT -> 3
    }
}

enum class LetterMark {
    UNUSED,
    ABSENT,
    PRESENT,
    CORRECT,
}
