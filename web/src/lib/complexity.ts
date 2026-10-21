export const COMPLEXITY_TIERS = ['basic', 'intermediate', 'advanced'] as const;
export type ComplexityTier = (typeof COMPLEXITY_TIERS)[number];

/** Empty means every word, including values outside the three chips. */
export function matchesComplexity(value: string, tiers: ReadonlySet<ComplexityTier>): boolean {
  if (tiers.size === 0) return true;
  if (tiers.has('basic') && (value === 'basic' || value === 'beginner')) return true;
  return (COMPLEXITY_TIERS as readonly string[]).includes(value) && tiers.has(value as ComplexityTier);
}

/** Profile storage: `all`, or a comma-separated subset in chip order. */
export function complexityFromStorage(value: string | null | undefined): Set<ComplexityTier> {
  if (!value || value.trim().length === 0) return new Set();
  const tokens = value
    .split(',')
    .map((token) => token.trim().toLowerCase())
    .filter((token) => token.length > 0);
  if (tokens.includes('all')) return new Set();
  const tiers = new Set<ComplexityTier>();
  for (const token of tokens) {
    if ((COMPLEXITY_TIERS as readonly string[]).includes(token)) tiers.add(token as ComplexityTier);
  }
  return tiers;
}

export function complexityToStorage(tiers: ReadonlySet<ComplexityTier>): string {
  if (tiers.size === 0) return 'all';
  return COMPLEXITY_TIERS.filter((tier) => tiers.has(tier)).join(',');
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
