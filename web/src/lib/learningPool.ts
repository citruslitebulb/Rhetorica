import { catalogKind } from './format';

export function isCatalogVisible(category: string, includeLiterary: boolean, includeFictional: boolean): boolean {
  const kind = catalogKind(category);
  if (kind === 'literary') return includeLiterary;
  if (kind === 'fictional') return includeFictional;
  return true;
}

/**
 * Orators the study features may draw from.
 * The selected orator stays in the set even when their catalog is hidden, so an
 * existing literary or fictional choice is not dropped when those catalogs default off.
 */
export function visibleOratorIds(
  orators: readonly { id: number; category: string }[],
  includeLiterary: boolean,
  includeFictional: boolean,
  selectedOratorId: number | null,
): number[] {
  const ids = orators
    .filter((orator) => isCatalogVisible(orator.category, includeLiterary, includeFictional))
    .map((orator) => orator.id);
  if (
    selectedOratorId != null &&
    !ids.includes(selectedOratorId) &&
    orators.some((orator) => orator.id === selectedOratorId)
  ) {
    ids.push(selectedOratorId);
  }
  return ids;
}

/** When rotation is on and the user starred a subset, that subset is the library. */
export function rotationOratorIds(
  visibleIds: readonly number[],
  rotateThroughAll: boolean,
  favoriteOratorIds: readonly number[],
): number[] {
  const visible = visibleIds.slice();
  const visibleSet = new Set(visible);
  const favorites = favoriteOratorIds.filter((id) => visibleSet.has(id));
  if (rotateThroughAll && favorites.length > 0) return favorites;
  return visible;
}
