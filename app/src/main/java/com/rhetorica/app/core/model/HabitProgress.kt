package com.rhetorica.app.core.model

import java.time.LocalDate
import java.time.ZoneId

object HabitProgress {
    data class DailyStreak(
        val streak: Int,
        val lastActiveDate: String,
    )

    /**
     * Advance a consecutive-days streak for activity on [today] (ISO local date).
     * Same day: unchanged. Yesterday: +1. Any longer gap (or no history): restart at 1.
     */
    fun nextDailyStreak(
        currentStreak: Int,
        lastActiveDate: String,
        today: String,
    ): DailyStreak {
        if (lastActiveDate == today) {
            return DailyStreak(streak = currentStreak.coerceAtLeast(1), lastActiveDate = today)
        }
        val yesterday = runCatching { LocalDate.parse(today).minusDays(1).toString() }.getOrNull()
        val continued = yesterday != null && lastActiveDate == yesterday && currentStreak > 0
        return DailyStreak(
            streak = if (continued) currentStreak + 1 else 1,
            lastActiveDate = today,
        )
    }

    /**
     * Streak to display: a streak whose last activity was before yesterday is
     * already broken even though nothing has written the reset yet.
     */
    fun displayedDailyStreak(
        storedStreak: Int,
        lastActiveDate: String,
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): Int {
        if (storedStreak <= 0 || lastActiveDate.isBlank()) return 0
        val yesterday = runCatching { LocalDate.parse(today).minusDays(1).toString() }.getOrNull()
        return if (lastActiveDate == today || lastActiveDate == yesterday) storedStreak else 0
    }

    fun isTodaysWordOpened(
        todaysWotdId: Long?,
        todaysWotdDate: String,
        isOpened: Boolean,
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): Boolean {
        return todaysWotdId != null && todaysWotdDate == today && isOpened
    }
}
