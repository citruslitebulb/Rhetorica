import type { SavedRecord, SavedSort, Word } from '../types';

export const SAVED_WORDS_KEY = 'rhetorica.saved-words.v1';

export function parseSaved(raw: string | null): SavedRecord[] {
  if (!raw) return [];
  try {
    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    const records: SavedRecord[] = [];
    for (const item of parsed) {
      if (typeof item !== 'object' || item === null) continue;
      const wordId = 'wordId' in item ? item.wordId : undefined;
      const savedAt = 'savedAt' in item ? item.savedAt : undefined;
      if (typeof wordId !== 'number' || !Number.isInteger(wordId)) continue;
      if (typeof savedAt !== 'number' || !Number.isFinite(savedAt)) continue;
      records.push({ wordId, savedAt });
    }
    return records;
  } catch {
    return [];
  }
}

export function serializeSaved(records: readonly SavedRecord[]): string {
  return JSON.stringify(records);
}

export function loadSaved(storage: Pick<Storage, 'getItem'>): SavedRecord[] {
  return parseSaved(storage.getItem(SAVED_WORDS_KEY));
}

export function storeSaved(records: readonly SavedRecord[], storage: Pick<Storage, 'setItem'>): void {
  storage.setItem(SAVED_WORDS_KEY, serializeSaved(records));
}

export function toggleSaved(records: readonly SavedRecord[], wordId: number, now = Date.now()): SavedRecord[] {
  if (records.some((record) => record.wordId === wordId)) {
    return records.filter((record) => record.wordId !== wordId);
  }
  return [{ wordId, savedAt: now }, ...records];
}

export function orderSaved(
  records: readonly SavedRecord[],
  sort: SavedSort,
  lookup: ReadonlyMap<number, Word>,
): Word[] {
  const rows = records.flatMap((record) => {
    const word = lookup.get(record.wordId);
    return word ? [{ word, savedAt: record.savedAt }] : [];
  });
  if (sort === 'alphabetical') {
    rows.sort((a, b) => a.word.word.localeCompare(b.word.word) || a.word.id - b.word.id);
  } else if (sort === 'partOfSpeech') {
    rows.sort(
      (a, b) => a.word.partOfSpeech.localeCompare(b.word.partOfSpeech) || a.word.word.localeCompare(b.word.word),
    );
  } else {
    rows.sort((a, b) => b.savedAt - a.savedAt || b.word.id - a.word.id);
  }
  return rows.map((row) => row.word);
}
