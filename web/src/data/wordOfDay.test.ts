import { describe, expect, it } from 'vitest';
import { defaultPreferences, type StudyPreferences } from '../storage/preferences';
import type { Word } from '../types';
import { ensureWordOfDay, resolveOratorId, selectUnseen } from './wordOfDay';

function word(partial: Partial<Word> & Pick<Word, 'id' | 'word' | 'oratorId'> & { complexity?: string }): Word {
  return {
    definition: `def ${partial.word}`,
    example: 'An example.',
    partOfSpeech: 'noun',
    complexity: partial.complexity ?? 'intermediate',
    source: null,
    speech: null,
    categories: [],
    pronunciation: null,
    ...partial,
  };
}

const library = [
  word({ id: 1, word: 'alpha', oratorId: 10 }),
  word({ id: 2, word: 'bravo', oratorId: 10 }),
  word({ id: 3, word: 'charlie', oratorId: 20 }),
  word({ id: 4, word: 'delta', oratorId: 20 }),
  word({ id: 5, word: 'echo', oratorId: 30 }),
];

function prefs(partial: Partial<StudyPreferences> = {}): StudyPreferences {
  return { ...defaultPreferences(), ...partial };
}

describe('word of the day', () => {
  it('uses the selected orator unless rotation is on', () => {
    expect(resolveOratorId(20, false)).toBe(20);
    expect(resolveOratorId(20, true)).toBeNull();
    expect(resolveOratorId(null, false)).toBeNull();
    expect(resolveOratorId(30, false, new Set([10, 20]))).toBeNull();
  });

  it('skips words already shown until the pool is exhausted', () => {
    const first = selectUnseen(library, 10, new Set(), 1);
    expect(first.word?.word).toBe('alpha');
    const second = selectUnseen(library, 10, new Set(first.shownIds), 2);
    expect(second.word?.word).toBe('bravo');
    expect(second.cycleReset).toBe(false);
    const reset = selectUnseen(library, 10, new Set(second.shownIds), 3);
    expect(reset.cycleReset).toBe(true);
    expect(reset.shownIds).toHaveLength(1);
  });

  it('treats the same headword from another orator as already shown', () => {
    const shared = [
      word({ id: 1, word: 'conviction', oratorId: 10 }),
      word({ id: 2, word: 'Conviction', oratorId: 20 }),
      word({ id: 3, word: 'zeal', oratorId: 30 }),
    ];
    const first = selectUnseen(shared, null, new Set(), 1);
    expect(first.word?.id).toBe(1);
    const second = selectUnseen(shared, null, new Set(first.shownIds), 2);
    expect(second.word?.word).toBe('zeal');
    expect(second.cycleReset).toBe(false);
    const third = selectUnseen(shared, null, new Set(second.shownIds), 3);
    expect(third.cycleReset).toBe(true);
  });

  it('serves intermediate and advanced words before basic ones', () => {
    const mixed = [
      word({ id: 1, word: 'king', oratorId: 10, complexity: 'basic' }),
      word({ id: 2, word: 'lamp', oratorId: 10, complexity: 'beginner' }),
      word({ id: 3, word: 'perfidy', oratorId: 10, complexity: 'advanced' }),
      word({ id: 4, word: 'resolve', oratorId: 10, complexity: 'intermediate' }),
    ];
    let shown = new Set<number>();
    const order: string[] = [];
    for (let day = 1; day <= 4; day += 1) {
      const pick = selectUnseen(mixed, 10, shown, day);
      order.push(pick.word?.word ?? '');
      shown = new Set(pick.shownIds);
    }
    expect(new Set(order.slice(0, 2))).toEqual(new Set(['perfidy', 'resolve']));
    expect(new Set(order.slice(2))).toEqual(new Set(['king', 'lamp']));
  });

  it('limits a rotating pool to favorites and to the visible catalog', () => {
    const favorites = selectUnseen(library, null, new Set(), 1, [30]);
    expect(favorites.word?.word).toBe('echo');
    const visible = selectUnseen(library, null, new Set(), 1, [], new Set([10, 20]));
    expect(visible.word?.word).toBe('alpha');
    const hidden = selectUnseen(library, 30, new Set(), 1, [], new Set([10, 20]));
    expect(hidden.word).toBeNull();
  });

  it('keeps the stored pick for the same day instead of drawing another word', () => {
    const visible = [10, 20, 30];
    const first = ensureWordOfDay(library, prefs({ rotateThroughAll: true }), visible, '2026-01-01', 1);
    expect(first.changed).toBe(true);
    expect(first.word).not.toBeNull();
    const second = ensureWordOfDay(library, first.preferences, visible, '2026-01-01', 1);
    expect(second.changed).toBe(false);
    expect(second.word?.id).toBe(first.word?.id);
    expect(second.preferences.shownWotdIds).toEqual(first.preferences.shownWotdIds);
    expect(second.preferences).toBe(first.preferences);
  });

  it('draws a new word on the next calendar day and skips the one already shown', () => {
    const visible = [10, 20, 30];
    const first = ensureWordOfDay(library, prefs({ rotateThroughAll: true }), visible, '2026-01-01', 1);
    const next = ensureWordOfDay(library, first.preferences, visible, '2026-01-02', 2);
    expect(next.word?.id).not.toBe(first.word?.id);
    expect(next.preferences.shownWotdIds).toContain(first.word?.id);
  });
});
