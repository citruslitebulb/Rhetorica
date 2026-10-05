export const COMPLEXITY_TIERS = ['basic', 'intermediate', 'advanced'] as const;
export type ComplexityTier = (typeof COMPLEXITY_TIERS)[number];

/** Empty means every word, including values outside the three chips. */
export function matchesComplexity(value: string, tiers: ReadonlySet<ComplexityTier>): boolean {
  if (tiers.size === 0) return true;
  if (tiers.has('basic') && (value === 'basic' || value === 'beginner')) return true;
  return (COMPLEXITY_TIERS as readonly string[]).includes(value) && tiers.has(value as ComplexityTier);
}

/** Turning the last tier off returns an empty set, which is Any level. */
export function toggleComplexityTier(
  tiers: ReadonlySet<ComplexityTier>,
  tier: ComplexityTier,
): Set<ComplexityTier> {
  const next = new Set(tiers);
  if (next.has(tier)) next.delete(tier);
  else next.add(tier);
  return next;
}
