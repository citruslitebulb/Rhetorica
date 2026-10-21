import { describe, expect, it } from 'vitest';
import type { Idiom } from '../types';
import { selectIdiomOfDay, visibleIdiomOfDay } from './idiomOfDay';

function idiom(partial: Pick<Idiom, 'id' | 'complexity'> & Partial<Idiom>): Idiom {
  return {
    idiom: `idiom ${partial.id}`,
    meaning: `meaning ${partial.id}`,
    example: 'Example.',
    origin: null,
    oratorId: null,
    quoteId: null,
    ...partial,
  };
}

const sample = [
  idiom({ id: 1, complexity: 'basic' }),
  idiom({ id: 2, complexity: 'beginner' }),
  idiom({ id: 3, complexity: 'intermediate' }),
  idiom({ id: 4, complexity: 'advanced' }),
];

describe('idiom of the day', () => {
  it('picks the same idiom for the same day and wraps with the pool', () => {
    const anyLevel = new Set<never>();
    const first = selectIdiomOfDay(sample, anyLevel, 40);
    expect(selectIdiomOfDay(sample, anyLevel, 40)?.id).toBe(first?.id);
    expect(selectIdiomOfDay(sample, anyLevel, 40 + sample.length)?.id).toBe(first?.id);
    expect(selectIdiomOfDay(sample, anyLevel, 1)?.id).not.toBe(selectIdiomOfDay(sample, anyLevel, 2)?.id);
  });

  it('walks the pool in id order', () => {
    const anyLevel = new Set<never>();
    const shuffled = [sample[3], sample[0], sample[2], sample[1]].filter((item): item is Idiom => item != null);
    expect([1, 2, 3, 4].map((day) => selectIdiomOfDay(shuffled, anyLevel, day)?.id)).toEqual([1, 2, 3, 4]);
  });

  it('honors complexity and treats beginner as basic', () => {
    const basic = new Set<'basic'>(['basic']);
    expect([1, 2, 3].map((day) => selectIdiomOfDay(sample, basic, day)?.id)).toEqual([1, 2, 1]);
    expect(selectIdiomOfDay(sample, new Set(['advanced']), 1)?.complexity).toBe('advanced');
    expect(selectIdiomOfDay(sample, new Set(['basic', 'advanced']), 3)?.id).toBe(4);
    expect(selectIdiomOfDay([idiom({ id: 3, complexity: 'intermediate' })], new Set(['advanced']), 1)).toBeNull();
    expect(selectIdiomOfDay([], new Set(), 4)).toBeNull();
  });

  it('hides the card when the setting is off', () => {
    expect(visibleIdiomOfDay(false, sample, new Set(), 1)).toBeNull();
    expect(visibleIdiomOfDay(true, sample, new Set(), 1)?.id).toBe(1);
  });
});
