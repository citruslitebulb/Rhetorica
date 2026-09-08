package com.rhetorica.app.core.model

import com.rhetorica.app.core.model.OratorVoiceFamily.Companion.filterByFamilies

/**
 * Turns first-run questionnaire answers into the preference fields the rest
 * of the app already honors (favorites, rotation, catalog flags, themes).
 */
data class OnboardingPreferencePatch(
    val favoriteOratorIds: List<Long>,
    val selectedOratorId: Long?,
    val rotateThroughAll: Boolean,
    val selectedThemeCategories: List<String>,
    val includeLiteraryOrators: Boolean,
    val includeFictionalOrators: Boolean,
)

object OnboardingAnswers {

    fun resolve(
        orators: List<OratorProfile>,
        families: Set<OratorVoiceFamily>,
        themes: Set<String>,
        selectedOratorIds: Set<Long>,
    ): OnboardingPreferencePatch {
        val activeFamilies = families.ifEmpty { OratorVoiceFamily.defaultSelected }
        val inFamilies = orators.filterByFamilies(activeFamilies)
        val allowedIds = inFamilies.map { it.id }.toSet()
        var chosenIds = selectedOratorIds.filter { it in allowedIds }
        if (chosenIds.isEmpty()) {
            chosenIds = inFamilies.map { it.id }
        }
        if (chosenIds.isEmpty()) {
            chosenIds = orators
                .filter { OratorVoiceFamily.from(it) == OratorVoiceFamily.Classical }
                .map { it.id }
        }
        if (chosenIds.isEmpty()) {
            chosenIds = orators
                .filter {
                    OratorCatalogKind.fromCategory(it.category) == OratorCatalogKind.Historical
                }
                .map { it.id }
        }

        val chosenSet = chosenIds.toSet()
        val chosenOrators = orators.filter { it.id in chosenSet }
        val sortedIds = chosenIds.distinct().sorted()
        val singleId = sortedIds.singleOrNull()
        return OnboardingPreferencePatch(
            favoriteOratorIds = sortedIds,
            selectedOratorId = singleId,
            rotateThroughAll = singleId == null,
            selectedThemeCategories = themes.filter { WordThemes.isValid(it) },
            includeLiteraryOrators = chosenOrators.any {
                OratorCatalogKind.fromCategory(it.category) == OratorCatalogKind.Literary
            },
            includeFictionalOrators = chosenOrators.any {
                OratorCatalogKind.fromCategory(it.category) == OratorCatalogKind.Fictional
            },
        )
    }
}
