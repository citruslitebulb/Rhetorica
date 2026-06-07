package com.rhetorica.app.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tests the deterministic "Word of the Day" selection logic.
 * The actual repository uses the current date, so we test the offset formula in isolation.
 */
class WordOfTheDayOffsetTest {

    private fun calculateOffset(dayOfYear: Int, totalCount: Int): Int {
        return (dayOfYear - 1) % totalCount
    }

    @Test
    fun `offset is zero-based and wraps correctly`() {
        assertEquals(0, calculateOffset(dayOfYear = 1, totalCount = 100))
        assertEquals(1, calculateOffset(dayOfYear = 2, totalCount = 100))
        assertEquals(99, calculateOffset(dayOfYear = 100, totalCount = 100))
        assertEquals(0, calculateOffset(dayOfYear = 101, totalCount = 100)) // wraps
    }

    @Test
    fun `offset works for small collections`() {
        assertEquals(0, calculateOffset(1, 5))
        assertEquals(4, calculateOffset(5, 5))
        assertEquals(0, calculateOffset(6, 5))
    }

    @Test
    fun `offset is stable for a given day of year and count`() {
        val count = 18 // typical number of orators
        val day1 = calculateOffset(42, count)
        val day2 = calculateOffset(42, count)
        assertEquals(day1, day2)
    }
}
