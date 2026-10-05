import { describe, expect, it } from 'vitest';
import { matchesComplexity, toggleComplexityTier, type ComplexityTier } from './complexity';

describe('complexity chips', () => {
  it('treats an empty selection as every word', () => {
    const anyLevel = new Set<ComplexityTier>();
    for (const value of ['basic', 'beginner', 'intermediate', 'advanced', 'unexpected']) {
      expect(matchesComplexity(value, anyLevel)).toBe(true);
    }
  });

  it('toggles tiers independently and keeps beginner with basic', () => {
    const intermediate = toggleComplexityTier(new Set(), 'intermediate');
    const both = toggleComplexityTier(intermediate, 'advanced');
    expect(matchesComplexity('intermediate', both)).toBe(true);
    expect(matchesComplexity('advanced', both)).toBe(true);
    expect(matchesComplexity('basic', both)).toBe(false);
    expect(matchesComplexity('beginner', both)).toBe(false);

    const withBasic = toggleComplexityTier(both, 'basic');
    expect(matchesComplexity('beginner', withBasic)).toBe(true);
    expect(matchesComplexity('unexpected', withBasic)).toBe(false);
    expect(toggleComplexityTier(toggleComplexityTier(toggleComplexityTier(withBasic, 'basic'), 'intermediate'), 'advanced').size).toBe(0);
  });
});
