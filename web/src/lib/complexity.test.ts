import { describe, expect, it } from 'vitest';
import {
  complexityFromStorage,
  complexityToStorage,
  matchesComplexity,
  toggleComplexityTier,
  type ComplexityTier,
} from './complexity';

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

  it('stores all or a comma-separated set in chip order', () => {
    expect(complexityToStorage(new Set())).toBe('all');
    expect(complexityFromStorage('all').size).toBe(0);
    expect(complexityFromStorage(null).size).toBe(0);
    const stored = complexityToStorage(new Set<ComplexityTier>(['advanced', 'basic']));
    expect(stored).toBe('basic,advanced');
    const restored = complexityFromStorage('advanced, basic');
    expect(matchesComplexity('beginner', restored)).toBe(true);
    expect(matchesComplexity('intermediate', restored)).toBe(false);
    expect(matchesComplexity('advanced', restored)).toBe(true);
    expect(complexityFromStorage('nope').size).toBe(0);
  });
});
