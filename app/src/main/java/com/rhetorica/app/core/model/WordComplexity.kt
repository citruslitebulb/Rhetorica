package com.rhetorica.app.core.model

/**
 * Vocabulary tiers the user wants to see.
 *
 * Stored in `user_preferences.wordComplexity` as `all`, or a comma-separated
 * subset of `basic`, `intermediate`, and `advanced` in that order.
 * [Tier.Basic] also includes the seed value `beginner`, the same grouping the
 * website Words page uses.
 *
 * [All] (no tier selected) matches every stored complexity, including values
 * outside these tiers, so an untouched setting keeps the full library and the
 * historical Word of the Day cycle. A combination matches the union of its
 * tiers. Selecting every tier is that union, not [All].
 */
data class WordComplexity(
    val tiers: Set<Tier>,
) {
    enum class Tier(val storageValue: String) {
        Basic("basic"),
        Intermediate("intermediate"),
        Advanced("advanced"),
        ;

        fun seedValues(): Set<String> = when (this) {
            Basic -> BASIC_SEED_VALUES
            Intermediate -> setOf(storageValue)
            Advanced -> setOf(storageValue)
        }
    }

    val isAll: Boolean get() = tiers.isEmpty()

    /** Canonical preference text. Legacy single-tier values are already in this form. */
    val storageValue: String
        get() = if (isAll) {
            ALL_STORAGE
        } else {
            Tier.entries.filter { it in tiers }.joinToString(",") { it.storageValue }
        }

    fun matches(complexity: String): Boolean {
        val allowed = allowedSeedValues()
        return allowed == null || complexity in allowed
    }

    /**
     * Seed values included by this selection.
     * Null means no restriction.
     */
    fun allowedSeedValues(): Set<String>? {
        if (isAll) return null
        return tiers.flatMap { it.seedValues() }.toSet()
    }

    /**
     * Independent toggle. Turning the last tier off returns [All], the same
     * outcome as choosing Any level.
     */
    fun toggle(tier: Tier): WordComplexity {
        val next = if (tier in tiers) tiers - tier else tiers + tier
        return WordComplexity(next)
    }

    companion object {
        const val ALL_STORAGE = "all"

        /** Nearby seed tier grouped under [Tier.Basic], matching the website Words page. */
        val BASIC_SEED_VALUES: Set<String> = setOf("basic", "beginner")

        private val SQL_ORDER = listOf("basic", "beginner", "intermediate", "advanced")

        val All = WordComplexity(emptySet())
        val Basic = WordComplexity(setOf(Tier.Basic))
        val Intermediate = WordComplexity(setOf(Tier.Intermediate))
        val Advanced = WordComplexity(setOf(Tier.Advanced))

        fun of(vararg tiers: Tier): WordComplexity = WordComplexity(tiers.toSet())

        fun fromStorage(value: String?): WordComplexity {
            if (value.isNullOrBlank()) return All
            val tokens = value.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            if (tokens.any { it == ALL_STORAGE }) return All
            val tiers = tokens.mapNotNull { token ->
                Tier.entries.firstOrNull { it.storageValue == token }
            }.toSet()
            return if (tiers.isEmpty()) All else WordComplexity(tiers)
        }

        /** 1 when the SQL complexity predicate should be skipped. */
        fun includeAllFlag(complexity: WordComplexity): Int = if (complexity.isAll) 1 else 0

        /**
         * Non-empty bind args for `complexity IN (:complexities)`.
         * The placeholder is unused when [includeAllFlag] is 1.
         */
        fun sqlValues(complexity: WordComplexity): List<String> {
            val allowed = complexity.allowedSeedValues() ?: return listOf("")
            return SQL_ORDER.filter { it in allowed }
        }

        /**
         * Value written by the v18 → v19 migration.
         * Legacy single-select tokens are already canonical, so they round-trip unchanged.
         */
        fun migratedStorage(value: String?): String = fromStorage(value).storageValue

        /**
         * Rows whose stored text is not already canonical.
         * The v18 → v19 migration applies this and leaves every other row untouched.
         */
        fun migrationUpdates(rows: List<Pair<Int, String?>>): List<Pair<Int, String>> {
            return rows.mapNotNull { (id, raw) ->
                val canonical = migratedStorage(raw)
                if (canonical == raw) null else id to canonical
            }
        }
    }
}
