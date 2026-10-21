import { describe, expect, it } from 'vitest';
import { idioms, oratorById, quoteById } from './catalog';

const COMPLEXITY = new Set(['beginner', 'basic', 'intermediate', 'advanced']);

function normalize(value: string): string {
  return value
    .toLowerCase()
    .replaceAll('’', "'")
    .replaceAll('‘', "'")
    .replace(/[^a-z0-9' ]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

describe('idiom seed', () => {
  it('parses required fields and keeps ids unique', () => {
    expect(idioms.length).toBeGreaterThanOrEqual(140);
    expect(idioms.length).toBeLessThanOrEqual(180);
    expect(new Set(idioms.map((idiom) => idiom.id)).size).toBe(idioms.length);
    expect(new Set(idioms.map((idiom) => idiom.idiom.trim().toLowerCase())).size).toBe(idioms.length);
    for (const idiom of idioms) {
      expect(idiom.idiom.length).toBeGreaterThan(0);
      expect(idiom.meaning.length).toBeGreaterThan(0);
      expect(idiom.example.length).toBeGreaterThan(0);
      expect(COMPLEXITY.has(idiom.complexity)).toBe(true);
      if (idiom.origin != null) expect(idiom.origin.length).toBeGreaterThan(0);
    }
    for (const tier of COMPLEXITY) {
      expect(idioms.some((idiom) => idiom.complexity === tier)).toBe(true);
    }
  });

  it('points every orator link at a real orator and a quote that uses the idiom', () => {
    const linked = idioms.filter((idiom) => idiom.oratorId != null || idiom.quoteId != null);
    expect(linked.length).toBeGreaterThan(0);
    for (const idiom of linked) {
      expect(idiom.oratorId).not.toBeNull();
      expect(oratorById.has(idiom.oratorId ?? -1)).toBe(true);
      const quote = idiom.quoteId == null ? undefined : quoteById.get(idiom.quoteId);
      expect(quote).toBeDefined();
      expect(quote?.oratorId).toBe(idiom.oratorId);
      expect(normalize(quote?.text ?? '')).toContain(normalize(idiom.idiom));
    }
  });
});