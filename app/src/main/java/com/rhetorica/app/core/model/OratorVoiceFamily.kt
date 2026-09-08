package com.rhetorica.app.core.model

/**
 * User-facing voice families for first-run onboarding.
 *
 * These are coarser than [OratorProfile.category] so a new user can choose
 * "classical philosophers" or "fictional mentors" without scanning fifty names.
 * Each orator maps to exactly one family.
 *
 * Default selection is [Classical] only — the catalog is polarizing, so the
 * app does not start with every orator visible.
 */
enum class OratorVoiceFamily {
    Classical,
    Statesmen,
    Justice,
    Literary,
    Technology,
    Fictional,
    ;

    companion object {
        val defaultSelected: Set<OratorVoiceFamily> = setOf(Classical)

        fun from(orator: OratorProfile): OratorVoiceFamily {
            return from(
                category = orator.category,
                tags = orator.tags,
                themeCategories = orator.themeCategories,
            )
        }

        fun from(
            category: String,
            tags: List<String> = emptyList(),
            themeCategories: List<String> = emptyList(),
        ): OratorVoiceFamily {
            val normalizedCategory = category.lowercase()
            val normalizedTags = tags.map { it.lowercase() }.toSet()
            val normalizedThemes = themeCategories.map { it.lowercase() }.toSet()
            return when {
                "fictional" in normalizedCategory || "mythic" in normalizedCategory -> Fictional
                "literary" in normalizedCategory -> Literary
                "ancient" in normalizedCategory || "philosophical" in normalizedCategory -> Classical
                "tech" in normalizedThemes -> Technology
                normalizedTags.any { it in literaryTags } -> Literary
                normalizedTags.any { it in justiceTags } -> Justice
                "equality" in normalizedTags && "law" in normalizedTags -> Justice
                else -> Statesmen
            }
        }

        fun List<OratorProfile>.filterByFamilies(
            families: Set<OratorVoiceFamily>,
        ): List<OratorProfile> {
            if (families.isEmpty()) return emptyList()
            return filter { from(it) in families }
        }

        fun List<OratorProfile>.groupedByFamily(): Map<OratorVoiceFamily, List<OratorProfile>> {
            return groupBy { from(it) }
        }

        private val literaryTags = setOf("poetry", "drama")

        private val justiceTags = setOf(
            "justice",
            "human rights",
            "abolition",
            "nonviolence",
            "civil rights",
            "women's rights",
            "suffrage",
            "reconciliation",
            "dissent",
        )
    }
}
