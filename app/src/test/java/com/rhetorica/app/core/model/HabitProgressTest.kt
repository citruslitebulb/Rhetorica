package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitProgressTest {

    @Test
    fun `daily streak starts at one with no history`() {
        val next = HabitProgress.nextDailyStreak(currentStreak = 0, lastActiveDate = "", today = "2026-09-11")
        assertEquals(1, next.streak)
        assertEquals("2026-09-11", next.lastActiveDate)
    }

    @Test
    fun `daily streak continues from yesterday and holds on the same day`() {
        val continued = HabitProgress.nextDailyStreak(currentStreak = 3, lastActiveDate = "2026-09-10", today = "2026-09-11")
        assertEquals(4, continued.streak)

        val sameDay = HabitProgress.nextDailyStreak(currentStreak = 4, lastActiveDate = "2026-09-11", today = "2026-09-11")
        assertEquals(4, sameDay.streak)
    }

    @Test
    fun `daily streak restarts after a missed day`() {
        val next = HabitProgress.nextDailyStreak(currentStreak = 9, lastActiveDate = "2026-09-09", today = "2026-09-11")
        assertEquals(1, next.streak)
    }

    @Test
    fun `daily streak continues across a month boundary`() {
        val next = HabitProgress.nextDailyStreak(currentStreak = 2, lastActiveDate = "2026-08-31", today = "2026-09-01")
        assertEquals(3, next.streak)
    }

    @Test
    fun `displayed streak collapses to zero once a day has been skipped`() {
        assertEquals(5, HabitProgress.displayedDailyStreak(storedStreak = 5, lastActiveDate = "2026-09-11", today = "2026-09-11"))
        assertEquals(5, HabitProgress.displayedDailyStreak(storedStreak = 5, lastActiveDate = "2026-09-10", today = "2026-09-11"))
        assertEquals(0, HabitProgress.displayedDailyStreak(storedStreak = 5, lastActiveDate = "2026-09-09", today = "2026-09-11"))
        assertEquals(0, HabitProgress.displayedDailyStreak(storedStreak = 0, lastActiveDate = "", today = "2026-09-11"))
    }

    @Test
    fun `requires a persisted word for today that has been opened`() {
        assertFalse(
            HabitProgress.isTodaysWordOpened(
                todaysWotdId = 12L,
                todaysWotdDate = "2026-08-13",
                isOpened = true,
                today = "2026-08-12",
            ),
        )
        assertFalse(
            HabitProgress.isTodaysWordOpened(
                todaysWotdId = 12L,
                todaysWotdDate = "2026-08-13",
                isOpened = false,
                today = "2026-08-13",
            ),
        )
        assertFalse(
            HabitProgress.isTodaysWordOpened(
                todaysWotdId = null,
                todaysWotdDate = "2026-08-13",
                isOpened = true,
                today = "2026-08-13",
            ),
        )
        assertTrue(
            HabitProgress.isTodaysWordOpened(
                todaysWotdId = 12L,
                todaysWotdDate = "2026-08-13",
                isOpened = true,
                today = "2026-08-13",
            ),
        )
    }
}
