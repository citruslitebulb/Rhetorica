package com.rhetorica.app.core.model

/**
 * Shared rules for which orators belong in the daily library after catalog
 * flags, starred favorites, and theme filters are applied.
 */
object LearningPool {

    /**
     * When rotation is on and the user starred a subset, that subset is the
     * library. Otherwise use every currently visible orator.
     */
    fun rotationOratorIds(
        visibleOratorIds: Collection<Long>,
        rotateThroughAll: Boolean,
        favoriteOratorIds: Collection<Long>,
    ): List<Long> {
        val visible = visibleOratorIds.toList()
        val visibleSet = visible.toSet()
        val favorites = favoriteOratorIds.filter { it in visibleSet }
        return if (rotateThroughAll && favorites.isNotEmpty()) favorites else visible
    }

    fun feedScope(
        visibleOrators: List<OratorProfile>,
        selectedOratorId: Long?,
        rotateThroughAll: Boolean,
        favoriteOratorIds: Collection<Long>,
        selectedThemes: Collection<String>,
    ): FeedScope {
        val visibleIds = visibleOrators.map { it.id }
        val visibleSet = visibleIds.toSet()
        val activeThemeSet = selectedThemes.toSet()
        val themeMatchingOratorIds: Set<Long> = if (activeThemeSet.isEmpty()) {
            emptySet()
        } else {
            visibleOrators.filter { orator ->
                orator.themeCategories.any { it in activeThemeSet }
            }.map { it.id }.toSet()
        }

        val effectiveSelected = selectedOratorId?.takeIf { it in visibleSet }
        val feedOratorId = if (rotateThroughAll) {
            null
        } else {
            effectiveSelected?.takeIf { id ->
                themeMatchingOratorIds.isEmpty() || id in themeMatchingOratorIds
            }
        }

        val rotationIds = rotationOratorIds(
            visibleOratorIds = visibleIds,
            rotateThroughAll = rotateThroughAll,
            favoriteOratorIds = favoriteOratorIds,
        )

        val scopedOratorIds: Collection<Long>? = when {
            feedOratorId != null -> null
            themeMatchingOratorIds.isEmpty() -> rotationIds
            else -> rotationIds.filter { it in themeMatchingOratorIds }
        }

        return FeedScope(
            feedOratorId = feedOratorId,
            scopedOratorIds = scopedOratorIds,
            activeThemeSet = activeThemeSet,
            hasActiveFilters = activeThemeSet.isNotEmpty() ||
                (!rotateThroughAll && effectiveSelected != null),
        )
    }

    data class FeedScope(
        val feedOratorId: Long?,
        val scopedOratorIds: Collection<Long>?,
        val activeThemeSet: Set<String>,
        val hasActiveFilters: Boolean,
    )
}
