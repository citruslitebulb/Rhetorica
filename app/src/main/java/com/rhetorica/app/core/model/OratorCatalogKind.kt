package com.rhetorica.app.core.model

/**
 * High-level catalog buckets derived from [OratorProfile.category].
 * Fictional voices stay off by default so v1 reads as historical oratory.
 */
enum class OratorCatalogKind {
    Historical,
    Literary,
    Fictional,
    ;

    companion object {
        fun fromCategory(category: String): OratorCatalogKind {
            val normalized = category.lowercase()
            return when {
                "fictional" in normalized || "mythic" in normalized -> Fictional
                "literary" in normalized -> Literary
                else -> Historical
            }
        }

        fun OratorProfile.catalogKind(): OratorCatalogKind = fromCategory(category)

        fun isVisible(
            category: String,
            includeLiterary: Boolean,
            includeFictional: Boolean,
        ): Boolean {
            return when (fromCategory(category)) {
                Historical -> true
                Literary -> includeLiterary
                Fictional -> includeFictional
            }
        }

        fun List<OratorProfile>.filterByCatalog(
            includeLiterary: Boolean,
            includeFictional: Boolean,
        ): List<OratorProfile> {
            return filter { orator ->
                isVisible(
                    category = orator.category,
                    includeLiterary = includeLiterary,
                    includeFictional = includeFictional,
                )
            }
        }
    }
}
