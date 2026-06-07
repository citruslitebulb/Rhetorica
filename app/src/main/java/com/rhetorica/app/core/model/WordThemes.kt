package com.rhetorica.app.core.model

/**
 * Canonical definition of word themes used throughout the app.
 * This is the single source of truth for valid theme values and their display names.
 *
 * Used for:
 * - Filtering in Home and Saved screens
 * - Theme selection in Profile
 * - Displaying category pills on cards and detail
 * - Data validation in seed tests
 */
object WordThemes {

    /** The complete ordered list of supported themes. */
    val all: List<String> = listOf(
        "inspirational",
        "tech",
        "humanities",
        "arts",
        "leadership",
        "democracy",
        "courage",
        "legacy"
    )

    /**
     * Returns a user-friendly display name for a theme slug.
     * Falls back to title-casing the raw value for unknown themes.
     */
    fun displayName(theme: String): String = when (theme) {
        "inspirational" -> "Inspirational"
        "tech" -> "Tech"
        "humanities" -> "Humanities"
        "arts" -> "Arts"
        "leadership" -> "Leadership"
        "democracy" -> "Democracy"
        "courage" -> "Courage"
        "legacy" -> "Legacy"
        else -> theme.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /** Returns true if the given string is one of the supported theme values. */
    fun isValid(theme: String): Boolean = theme in all

    /** Returns the list of themes that should be offered in filter UIs. */
    fun canonicalList(): List<String> = all
}
