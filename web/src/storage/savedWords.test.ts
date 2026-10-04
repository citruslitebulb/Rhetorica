import { describe, expect, it } from 'vitest';
import type { Word } from '../types';
import { memoryStorage } from './memoryStorage';
import { readSelectedOratorId, writeSelectedOratorId } from './preferences';
import { loadSaved, orderSaved, storeSaved, toggleSaved } from './savedWords';

function word(id: number, headword: string, partOfSpeech: string): Word {
  return {
    id,
    word: headword,
    definition: 'Defined in the fixture.',
    example: '',
    partOfSpeech,
    oratorId: 1,
    complexity: 'intermediate',
    source: null,
    speech: null,
    categories: [],
    pronunciation: null,
  };
}

describe('saved words', () => {
  it('round-trips through storage and survives a fresh read', () => {
    const storage = memoryStorage();
    const first = toggleSaved([], 40001, 100);
    const second = toggleSaved(first, 40002, 200);
    storeSaved(second, storage);

    const restored = loadSaved(storage);
    expect(restored).toEqual([
      { wordId: 40002, savedAt: 200 },
      { wordId: 40001, savedAt: 100 },
    ]);

    const removed = toggleSaved(restored, 40002, 300);
    storeSaved(removed, storage);
    expect(loadSaved(storage)).toEqual([{ wordId: 40001, savedAt: 100 }]);
  });

  it('ignores corrupt storage', () => {
    const storage = memoryStorage({ 'rhetorica.saved-words.v1': '{not json' });
    expect(loadSaved(storage)).toEqual([]);
  });

  it('sorts saved words and drops ids missing from the library', () => {
    const lookup = new Map<number, Word>([
      [2, word(2, 'bravery', 'noun')],
      [1, word(1, 'action', 'verb')],
    ]);
    const records = [
      { wordId: 2, savedAt: 10 },
      { wordId: 99, savedAt: 50 },
      { wordId: 1, savedAt: 20 },
    ];
    expect(orderSaved(records, 'newest', lookup).map((entry) => entry.word)).toEqual(['action', 'bravery']);
    expect(orderSaved(records, 'alphabetical', lookup).map((entry) => entry.word)).toEqual(['action', 'bravery']);
    expect(orderSaved(records, 'partOfSpeech', lookup).map((entry) => entry.partOfSpeech)).toEqual(['noun', 'verb']);
  });
});

describe('selected orator preference', () => {
  it('persists the chosen orator across reads', () => {
    const storage = memoryStorage();
    expect(readSelectedOratorId(storage)).toBeNull();
    writeSelectedOratorId(storage, 8);
    expect(readSelectedOratorId(storage)).toBe(8);
    writeSelectedOratorId(storage, null);
    expect(readSelectedOratorId(storage)).toBeNull();
  });
});
