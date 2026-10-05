package com.rhetorica.app.core.model

/**
 * Vocabulary tier the user wants to see.
 *
 * Mirrors the Words page chips (`all`, `basic`, `intermediate`, `advanced`).
 * `basic` also includes the seed value `beginner`, the same grouping that page uses.
 * [All] is the default and matches every stored complexity, including values
 * outside those chips, so an untouched setting keeps today's full library.
 */
enum class WordComplexity(val storageValue: String) {
    All("all"),
    Basic("basic"),
    Intermediate("intermediate"),
    Advanced("advanced"),
    ;

    fun matches(complexity: String): Boolean {
        val allowed = allowedSeedValues()
        return allowed == null || complexity in allowed
    }

    /**
     * Seed values included by this tier.
     * Null means no restriction.
     */
    fun allowedSeedValues(): Set<String>? = when (this) {
        All -> null
        Basic -> BASIC_SEED_VALUES
        Intermediate -> setOf(storageValue)
        Advanced -> setOf(storageValue)
    }

    companion object {
        /** Nearby seed tier grouped under [Basic], matching the website Words page. */
        val BASIC_SEED_VALUES: Set<String> = setOf("basic", "beginner")

        fun fromStorage(value: String?): WordComplexity {
            return entries.firstOrNull { it.storageValue == value } ?: All
        }

        /** 1 when the SQL complexity predicate should be skipped. */
        fun includeAllFlag(complexity: WordComplexity): Int = if (complexity == All) 1 else 0

        /**
         * Non-empty bind args for `complexity IN (:complexities)`.
         * The placeholder is unused when [includeAllFlag] is 1.
         */
        fun sqlValues(complexity: WordComplexity): List<String> {
            return complexity.allowedSeedValues()?.toList() ?: listOf("")
        }
    }
}
