package com.rhetorica.app.data.repository

import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.data.local.IdiomEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Deterministic Idiom of the Day.
 *
 * The pool is the idioms whose complexity matches the profile filter, sorted by id.
 * The calendar day picks a stable index in that pool, the same day-offset the Word
 * of the Day uses inside a working set. The same date and the same filter always
 * return the same idiom. Basic also includes the seed value beginner.
 */
object IdiomOfDaySelector {
    fun select(
        idioms: List<IdiomEntity>,
        complexity: WordComplexity = WordComplexity.All,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
    ): IdiomEntity? {
        val pool = idioms
            .filter { complexity.matches(it.complexity) }
            .sortedBy { it.id }
        if (pool.isEmpty()) return null
        return pool[WordOfDaySelector.dayOffset(pool.size, dayOfYear)]
    }

    /** A hidden card stays hidden even when the filtered pool is not empty. */
    fun visible(
        show: Boolean,
        idioms: List<IdiomEntity>,
        complexity: WordComplexity = WordComplexity.All,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
    ): IdiomEntity? {
        if (!show) return null
        return select(idioms, complexity, dayOfYear)
    }
}
