import { catalogKind } from './format';
import { filterByFamilies, voiceFamilyOf, type VoiceFamily, type VoiceOrator } from './voiceFamily';

const THEMES = new Set([
  'inspirational',
  'tech',
  'humanities',
  'arts',
  'leadership',
  'democracy',
  'courage',
  'legacy',
]);

export type OnboardingPatch = {
  favoriteOratorIds: number[];
  selectedOratorId: number | null;
  rotateThroughAll: boolean;
  selectedThemeCategories: string[];
  includeLiterary: boolean;
  includeFictional: boolean;
};

export function resolveOnboarding(
  orators: readonly VoiceOrator[],
  families: ReadonlySet<VoiceFamily>,
  themes: ReadonlySet<string>,
  selectedOratorIds: ReadonlySet<number>,
): OnboardingPatch {
  const activeFamilies = families.size > 0 ? families : new Set<VoiceFamily>(['classical']);
  const inFamilies = filterByFamilies(orators, activeFamilies);
  const allowed = new Set(inFamilies.map((orator) => orator.id));
  let chosenIds = [...selectedOratorIds].filter((id) => allowed.has(id));
  if (chosenIds.length === 0) chosenIds = inFamilies.map((orator) => orator.id);
  if (chosenIds.length === 0) {
    chosenIds = orators.filter((orator) => voiceFamilyOf(orator) === 'classical').map((orator) => orator.id);
  }
  if (chosenIds.length === 0) {
    chosenIds = orators.filter((orator) => catalogKind(orator.category) === 'historical').map((orator) => orator.id);
  }

  const chosenSet = new Set(chosenIds);
  const chosenOrators = orators.filter((orator) => chosenSet.has(orator.id));
  const sortedIds = [...new Set(chosenIds)].sort((a, b) => a - b);
  const singleId = sortedIds.length === 1 ? sortedIds[0] ?? null : null;
  return {
    favoriteOratorIds: sortedIds,
    selectedOratorId: singleId,
    rotateThroughAll: singleId == null,
    selectedThemeCategories: [...themes].filter((theme) => THEMES.has(theme)),
    includeLiterary: chosenOrators.some((orator) => catalogKind(orator.category) === 'literary'),
    includeFictional: chosenOrators.some((orator) => catalogKind(orator.category) === 'fictional'),
  };
}
