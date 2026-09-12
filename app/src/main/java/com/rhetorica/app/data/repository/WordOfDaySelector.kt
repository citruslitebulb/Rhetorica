package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.WordEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Deterministic Word of the Day selection.
 *
 * Rules:
 * - If [rotateThroughAll] is true (or no orator is selected), pick from the full library
 *   (optionally limited to favorite orators).
 * - If a specific orator is selected, pick **only** from that orator's words.
 * - Theme filters never change which orator owns the Word of the Day.
 * - Within a pool, unseen words are preferred until the pool is exhausted, then the
 *   cycle restarts. The pick for a given calendar day is stable once persisted.
 */
object WordOfDaySelector {

    /**
     * Resolve which orator (if any) owns today's word.
     * `null` means "all orators" (global rotation, optionally favorite-limited).
     */
    fun resolveOratorId(
        selectedOratorId: Long?,
        rotateThroughAll: Boolean,
        visibleOratorIds: Collection<Long>? = null,
    ): Long? {
        if (rotateThroughAll) return null
        val selected = selectedOratorId ?: return null
        if (visibleOratorIds != null && selected !in visibleOratorIds) return null
        return selected
    }

    fun poolKey(
        oratorId: Long?,
        favoriteOratorIds: List<Long> = emptyList(),
        includeLiterary: Boolean = false,
        includeFictional: Boolean = false,
    ): String {
        val catalog = "lit:${if (includeLiterary) 1 else 0}|fic:${if (includeFictional) 1 else 0}"
        val base = when {
            oratorId != null -> "orator:$oratorId"
            favoriteOratorIds.isNotEmpty() -> "favorites:${favoriteOratorIds.sorted().joinToString(",")}"
            else -> "all"
        }
        return "$base|$catalog"
    }

    fun dayOffset(
        poolSize: Int,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
    ): Int {
        if (poolSize <= 0) return 0
        return (dayOfYear - 1) % poolSize
    }

    /** True when there is no persisted pick for [today] (missing id or a different calendar day). */
    fun needsNewPick(
        todaysWotdId: Long?,
        todaysWotdDate: String,
        today: String,
    ): Boolean {
        return todaysWotdId == null || todaysWotdDate != today
    }

    /**
     * Pick Word of the Day from [allWords] for the given orator preference.
     * When [oratorId] is non-null, only that orator's vocabulary is used.
     */
    fun select(
        allWords: List<WordEntity>,
        oratorId: Long?,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
        favoriteOratorIds: List<Long> = emptyList(),
        visibleOratorIds: Collection<Long>? = null,
    ): WordEntity? {
        return selectUnseen(
            allWords = allWords,
            oratorId = oratorId,
            shownIds = emptySet(),
            dayOfYear = dayOfYear,
            favoriteOratorIds = favoriteOratorIds,
            visibleOratorIds = visibleOratorIds,
        ).word
    }

    fun pool(
        allWords: List<WordEntity>,
        oratorId: Long?,
        favoriteOratorIds: List<Long> = emptyList(),
        visibleOratorIds: Collection<Long>? = null,
    ): List<WordEntity> {
        val catalogScoped = if (visibleOratorIds == null) {
            allWords
        } else {
            val allowed = visibleOratorIds.toSet()
            allWords.filter { word -> word.oratorId != null && word.oratorId in allowed }
        }
        return if (oratorId == null) {
            if (favoriteOratorIds.isEmpty()) {
                catalogScoped
            } else {
                val allowed = favoriteOratorIds.toSet()
                catalogScoped.filter { it.oratorId in allowed }
            }
        } else {
            catalogScoped.filter { it.oratorId == oratorId }
        }
    }

    /** Seed complexity tiers that are everyday words rather than vocabulary to learn. */
    private val foundationalComplexities = setOf("basic", "beginner")

    fun normalizeHeadword(word: String): String = word.trim().lowercase()

    /**
     * Prefer words that have not yet been shown in the current cycle.
     * When every word in the pool has been shown, restart the cycle.
     *
     * "Shown" is judged by headword as well as row id: several orators share
     * headwords like *conviction*, and a user should not meet the same word twice
     * in one cycle just because two speeches used it. Within the unseen set,
     * intermediate/advanced words are served before basic ones so the everyday
     * entries land at the tail of a cycle instead of interleaving with it.
     */
    fun selectUnseen(
        allWords: List<WordEntity>,
        oratorId: Long?,
        shownIds: Set<Long>,
        dayOfYear: Int = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
        favoriteOratorIds: List<Long> = emptyList(),
        visibleOratorIds: Collection<Long>? = null,
    ): WotdPick {
        val candidates = pool(allWords, oratorId, favoriteOratorIds, visibleOratorIds)
        if (candidates.isEmpty()) return WotdPick(word = null, shownIds = emptyList(), cycleReset = false)

        val shownHeadwords = candidates
            .filter { it.id in shownIds }
            .map { normalizeHeadword(it.word) }
            .toSet()
        val unseen = candidates.filter {
            it.id !in shownIds && normalizeHeadword(it.word) !in shownHeadwords
        }
        val cycleReset = unseen.isEmpty()
        val working = if (cycleReset) {
            candidates
        } else {
            unseen.filter { it.complexity !in foundationalComplexities }.ifEmpty { unseen }
        }
        val sorted = working.sortedBy { it.id }
        val word = sorted[dayOffset(sorted.size, dayOfYear)]
        val nextShown = if (cycleReset) {
            listOf(word.id)
        } else {
            (shownIds + word.id).toList()
        }
        return WotdPick(word = word, shownIds = nextShown, cycleReset = cycleReset)
    }
}

data class WotdPick(
    val word: WordEntity?,
    val shownIds: List<Long>,
    val cycleReset: Boolean,
)
