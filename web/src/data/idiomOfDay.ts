import { dayOffset } from './wordOfDay';
import { matchesComplexity, type ComplexityTier } from '../lib/complexity';
import type { Idiom } from '../types';

/**
 * Deterministic Idiom of the Day. Same calendar day and complexity filter
 * always return the same idiom. Basic also includes beginner.
 */
export function selectIdiomOfDay(
  idioms: readonly Idiom[],
  tiers: ReadonlySet<ComplexityTier>,
  day: number,
): Idiom | null {
  const pool = idioms
    .filter((idiom) => matchesComplexity(idiom.complexity, tiers))
    .slice()
    .sort((a, b) => a.id - b.id);
  if (pool.length === 0) return null;
  return pool[dayOffset(pool.length, day)] ?? null;
}

export function visibleIdiomOfDay(
  show: boolean,
  idioms: readonly Idiom[],
  tiers: ReadonlySet<ComplexityTier>,
  day: number,
): Idiom | null {
  if (!show) return null;
  return selectIdiomOfDay(idioms, tiers, day);
}
