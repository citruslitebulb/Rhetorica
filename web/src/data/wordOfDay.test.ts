import { describe, expect, it } from 'vitest';
import type { Word } from '../types';
import { selectWordOfDay } from './wordOfDay';

function word(partial: Partial<Word> & Pick<Word, 'id' | 'oratorId' | 'complexity'>): Word {
  return {
    word: `word-${partial.id}`,
    definition: 'A definition from the fixture.',
    example: 'An example.',
    partOfSpeech: 'noun',
    source: null,
    speech: null,
    categories: [],
    pronunciation: null,
    ...partial,
  };
}

const library = [
  word({ id: 1, oratorId: 5, complexity: 'basic' }),
  word({ id: 2, oratorId: 5, complexity: 'intermediate' }),
  word({ id: 3, oratorId: 8, complexity: 'advanced' }),
  word({ id: 4, oratorId: 5, complexity: 'advanced' }),
];

describe('selectWordOfDay', () => {
  it('is stable for a calendar day and prefers learned vocabulary', () => {
    const date = new Date(2026, 0, 1);
    const first = selectWordOfDay(library, null, date);
    const again = selectWordOfDay(library, null, date);
    expect(first?.id).toBe(again?.id);
    expect(first?.complexity === 'basic').toBe(false);
  });

  it('limits the pool to the selected orator', () => {
    const date = new Date(2026, 5, 15);
    const pick = selectWordOfDay(library, 8, date);
    expect(pick?.oratorId).toBe(8);
    expect(selectWordOfDay(library, 99, date)).toBeNull();
  });
});
