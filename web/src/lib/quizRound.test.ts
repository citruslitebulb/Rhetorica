import { describe, expect, it } from 'vitest';
import type { Word } from '../types';
import { assembleLetterGuess, assembleMultipleChoice, buildOptions } from './quizRound';

function word(partial: Partial<Word> & Pick<Word, 'id' | 'word'> & { partOfSpeech?: string; oratorId?: number }): Word {
  return {
    definition: `def ${partial.word}`,
    example: 'Example.',
    partOfSpeech: partial.partOfSpeech ?? 'noun',
    oratorId: partial.oratorId ?? 1,
    complexity: 'intermediate',
    source: null,
    speech: null,
    categories: [],
    pronunciation: null,
    ...partial,
  };
}

const rng = () => 0.999;

describe('quiz rounds', () => {
  it('never repeats the correct headword from another orator', () => {
    const correct = word({ id: 1, word: 'conviction' });
    const options = buildOptions(
      correct,
      [correct, word({ id: 2, word: 'Conviction' }), word({ id: 3, word: 'zeal' }), word({ id: 4, word: 'candor' }), word({ id: 5, word: 'valor' })],
      4,
      rng,
    );
    expect(options).toHaveLength(4);
    expect(options.filter((option) => option.word.toLowerCase() === 'conviction')).toHaveLength(1);
    expect(options.some((option) => option.id === correct.id)).toBe(true);
  });

  it('prefers distractors with the same part of speech and falls back when it must', () => {
    const correct = word({ id: 1, word: 'exhort', partOfSpeech: 'verb' });
    const verbs = [
      correct,
      word({ id: 2, word: 'rally', partOfSpeech: 'verb' }),
      word({ id: 3, word: 'kindle', partOfSpeech: 'verb' }),
      word({ id: 4, word: 'galvanize', partOfSpeech: 'verb' }),
      word({ id: 5, word: 'valor', partOfSpeech: 'noun' }),
    ];
    expect(buildOptions(correct, verbs, 4, Math.random).every((option) => option.partOfSpeech === 'verb')).toBe(true);
    const mixed = buildOptions(
      correct,
      [correct, word({ id: 2, word: 'rally', partOfSpeech: 'verb' }), word({ id: 5, word: 'valor' }), word({ id: 6, word: 'candor' })],
      4,
      rng,
    );
    expect(mixed).toHaveLength(4);
    expect(mixed.some((option) => option.id === 2)).toBe(true);
    expect(buildOptions(correct, [correct, word({ id: 2, word: 'rally', partOfSpeech: 'verb' })], 4, rng)).toEqual([]);
  });

  it('prefers a due word and refuses a pool that is too small', () => {
    const words = [1, 2, 3, 4, 5].map((id) => word({ id, word: `word${id}`, oratorId: 7 }));
    const due = words[3];
    if (!due) throw new Error('fixture');
    const round = assembleMultipleChoice({
      words,
      savedIds: new Set(),
      dueWords: [due],
      pool: 'library',
      oratorId: 7,
      libraryOratorIds: new Set([7]),
      previousWordId: null,
      rng,
    });
    expect(round?.correct.id).toBe(due.id);
    expect(round?.review).toBe(true);
    expect(round?.options).toHaveLength(4);
    expect(
      assembleMultipleChoice({
        words: words.slice(0, 2),
        savedIds: new Set([1, 2]),
        dueWords: [],
        pool: 'saved',
        oratorId: null,
        libraryOratorIds: new Set([7]),
        previousWordId: null,
        rng,
      }),
    ).toBeNull();
  });

  it('picks a letter-guess word inside the difficulty length and skips the previous answer', () => {
    const words = [
      word({ id: 1, word: 'hope' }),
      word({ id: 2, word: 'valor' }),
      word({ id: 3, word: 'zeal' }),
    ];
    const pick = assembleLetterGuess({
      words,
      savedIds: new Set(),
      pool: 'library',
      oratorId: 1,
      libraryOratorIds: new Set([1]),
      minLetters: 4,
      maxLetters: 5,
      excludeWordIds: new Set([1]),
      excludeDefinitions: new Set(),
      rng,
    });
    expect(pick?.id).not.toBe(1);
    expect(pick && [...pick.word].length).toBeGreaterThanOrEqual(4);
    expect(pick && [...pick.word].length).toBeLessThanOrEqual(5);
  });

  it('letter guess skips inflected headwords and multiple choice does not', () => {
    const advertising = word({ id: 1, word: 'advertising', partOfSpeech: 'noun' });
    const advertise = word({ id: 2, word: 'advertise', partOfSpeech: 'verb' });
    const advertised = word({ id: 3, word: 'advertised', partOfSpeech: 'verb' });
    const bring = word({ id: 4, word: 'bring', partOfSpeech: 'verb' });
    const picks = new Set<string>();
    for (let index = 0; index < 30; index += 1) {
      const pick = assembleLetterGuess({
        words: [advertising, advertise, advertised, bring],
        savedIds: new Set(),
        pool: 'library',
        oratorId: 1,
        libraryOratorIds: new Set([1]),
        minLetters: 4,
        maxLetters: 14,
        excludeWordIds: new Set(),
        excludeDefinitions: new Set(),
        rng: Math.random,
      });
      if (pick) picks.add(pick.word);
    }
    expect(picks.has('advertising')).toBe(false);
    expect(picks.has('advertised')).toBe(false);
    expect(picks.has('advertise')).toBe(true);
    expect(picks.has('bring')).toBe(true);
    expect(
      assembleLetterGuess({
        words: [advertising, advertised],
        savedIds: new Set(),
        pool: 'library',
        oratorId: 1,
        libraryOratorIds: new Set([1]),
        minLetters: 4,
        maxLetters: 14,
        excludeWordIds: new Set(),
        excludeDefinitions: new Set(),
        rng,
      }),
    ).toBeNull();

    const distractors = [2, 3, 4, 5].map((id) => word({ id, word: `noun${id}`, partOfSpeech: 'noun' }));
    const round = assembleMultipleChoice({
      words: [advertising, ...distractors],
      savedIds: new Set(),
      dueWords: [advertising],
      pool: 'library',
      oratorId: 1,
      libraryOratorIds: new Set([1]),
      previousWordId: null,
      rng,
    });
    expect(round?.correct.word).toBe('advertising');
  });
});
