package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.WordEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Deterministic Word of the Day selection.
 *
 * Rules:
 * - If [rotateThroughAll] is true (or no orator is selected), pick from the full library.
 * - If a specific orator is selected, pick **only** from that orator's words.
 * - Theme filters never change which orator owns the Word of the Day.
 */
object WordOfDaySelector {

    /**
     * Resolve which orator (if any) owns today's word.
     * `null` means "all orators" (global rotation).
     */
    fun resolveOratorId(
        selectedOratorId: Long?,
        rotateThroughAll: Boolean,
    ): Long? {
        return if (rotateThroughAll) null else selectedOratorId
    }

    fun dayOffset(
        poolSize: Int,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
    ): Int {
        if (poolSize <= 0) return 0
        return (dayOfYear - 1) % poolSize
    }

    /**
     * Pick Word of the Day from [allWords] for the given orator preference.
     * When [oratorId] is non-null, only that orator's vocabulary is used.
     */
    fun select(
        allWords: List<WordEntity>,
        oratorId: Long?,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
    ): WordEntity? {
        val pool = if (oratorId == null) {
            allWords
        } else {
            allWords.filter { it.oratorId == oratorId }
        }
        if (pool.isEmpty()) return null
        val sorted = pool.sortedBy { it.id }
        return sorted[dayOffset(sorted.size, dayOfYear)]
    }
}
