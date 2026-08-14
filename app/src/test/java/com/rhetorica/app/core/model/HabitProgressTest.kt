package com.rhetorica.app.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitProgressTest {

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
