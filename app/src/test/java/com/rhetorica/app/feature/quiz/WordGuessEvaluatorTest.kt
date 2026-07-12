package com.rhetorica.app.feature.quiz

import org.junit.Assert.assertEquals
import org.junit.Test

class WordGuessEvaluatorTest {

    @Test
    fun `normalize strips non-letters and lowercases`() {
        assertEquals("eloquence", WordGuessEvaluator.normalize("Eloquence!"))
        assertEquals("wellsaid", WordGuessEvaluator.normalize("well-said"))
    }

    @Test
    fun `all correct`() {
        val marks = WordGuessEvaluator.evaluate("apple", "apple")
        assertEquals(List(5) { LetterMark.CORRECT }, marks)
    }

    @Test
    fun `all absent`() {
        val marks = WordGuessEvaluator.evaluate("zzzzz", "apple")
        assertEquals(List(5) { LetterMark.ABSENT }, marks)
    }

    @Test
    fun `present and correct with duplicates`() {
        // target: a p p l e
        // guess:  p a p e r
        // p0 PRESENT, a1 PRESENT, p2 CORRECT, e3 PRESENT, r4 ABSENT
        val marks = WordGuessEvaluator.evaluate("paper", "apple")
        assertEquals(
            listOf(
                LetterMark.PRESENT,
                LetterMark.PRESENT,
                LetterMark.CORRECT,
                LetterMark.PRESENT,
                LetterMark.ABSENT,
            ),
            marks,
        )
    }

    @Test
    fun `duplicate guess letter only marks as many as target has`() {
        // target has one L; guess has two — only one PRESENT
        val marks = WordGuessEvaluator.evaluate("llxxx", "apple")
        val presentCount = marks.count { it == LetterMark.PRESENT }
        val correctCount = marks.count { it == LetterMark.CORRECT }
        // positions: L P, L A/P?, ... none correct for llxxx vs apple
        assertEquals(0, correctCount)
        assertEquals(1, presentCount)
    }

    @Test
    fun `unequal length still marks correct and present letters`() {
        // target: apple (5); guess: apples (6) — first 5 can match, trailing s absent
        val marks = WordGuessEvaluator.evaluateAllowingLengthMismatch("apples", "apple")
        assertEquals(
            listOf(
                LetterMark.CORRECT,
                LetterMark.CORRECT,
                LetterMark.CORRECT,
                LetterMark.CORRECT,
                LetterMark.CORRECT,
                LetterMark.ABSENT,
            ),
            marks,
        )
    }

    @Test
    fun `short guess against longer target marks positions only`() {
        // target: c o u r a g e — guess: c o r e
        // Pass1: c0 CORRECT, o1 CORRECT; remaining u,r,a,g,e
        // Pass2: r PRESENT, e PRESENT
        assertEquals(
            listOf(
                LetterMark.CORRECT,
                LetterMark.CORRECT,
                LetterMark.PRESENT,
                LetterMark.PRESENT,
            ),
            WordGuessEvaluator.evaluateAllowingLengthMismatch("core", "courage"),
        )
    }

    @Test
    fun `keyboard merge prefers correct over present over absent`() {
        var kb = emptyMap<Char, LetterMark>()
        kb = WordGuessEvaluator.mergeKeyboardState(
            kb,
            "abcde",
            listOf(LetterMark.ABSENT, LetterMark.PRESENT, LetterMark.ABSENT, LetterMark.ABSENT, LetterMark.ABSENT),
        )
        assertEquals(LetterMark.PRESENT, kb['b'])
        kb = WordGuessEvaluator.mergeKeyboardState(
            kb,
            "bxzzz",
            listOf(LetterMark.CORRECT, LetterMark.ABSENT, LetterMark.ABSENT, LetterMark.ABSENT, LetterMark.ABSENT),
        )
        assertEquals(LetterMark.CORRECT, kb['b'])
    }
}
