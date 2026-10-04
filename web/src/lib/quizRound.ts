import type { Word } from '../types';
import { normalizeGuess } from './wordGuess';

export type Rng = () => number;

export const MIN_OPTIONS = 4;
export const CANDIDATE_POOL = 24;

export const WORD_GUESS_DIFFICULTIES = {
  easy: { minLetters: 4, maxLetters: 5, revealsLength: true, maxAttempts: 6 },
  medium: { minLetters: 5, maxLetters: 6, revealsLength: true, maxAttempts: 5 },
  hard: { minLetters: 7, maxLetters: 10, revealsLength: true, maxAttempts: 4 },
  hardcore: { minLetters: 4, maxLetters: 14, revealsLength: false, maxAttempts: 3 },
} as const;

export type WordGuessDifficulty = keyof typeof WORD_GUESS_DIFFICULTIES;

export const MAX_UNKNOWN_INPUT_LENGTH = 14;
export const MIN_UNKNOWN_GUESS_LENGTH = 3;

export function shuffle<T>(items: readonly T[], rng: Rng): T[] {
  const copy = items.slice();
  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swap = Math.floor(rng() * (index + 1));
    const current = copy[index];
    const other = copy[swap];
    if (current === undefined || other === undefined) continue;
    copy[index] = other;
    copy[swap] = current;
  }
  return copy;
}

function randomItem<T>(items: readonly T[], rng: Rng): T | null {
  if (items.length === 0) return null;
  const index = Math.min(items.length - 1, Math.floor(rng() * items.length));
  return items[index] ?? null;
}

/**
 * Multiple-choice options. Distractors never repeat the correct headword, and
 * they share its part of speech when enough candidates exist.
 */
export function buildOptions(correct: Word, candidates: readonly Word[], optionCount: number, rng: Rng): Word[] {
  const correctHeadword = correct.word.trim().toLowerCase();
  const usedHeadwords = new Set<string>([correctHeadword]);
  const eligible = shuffle(
    candidates.filter((word) => word.id !== correct.id),
    rng,
  ).filter((word) => {
    const headword = word.word.trim().toLowerCase();
    if (usedHeadwords.has(headword)) return false;
    usedHeadwords.add(headword);
    return true;
  });

  const samePos = eligible.filter((word) => word.partOfSpeech.toLowerCase() === correct.partOfSpeech.toLowerCase());
  const sameIds = new Set(samePos.map((word) => word.id));
  const otherPos = eligible.filter((word) => !sameIds.has(word.id));
  const distractors = [...samePos, ...otherPos].slice(0, optionCount - 1);
  if (distractors.length < optionCount - 1) return [];
  return shuffle([...distractors, correct], rng);
}

export type ChoiceRound = {
  correct: Word;
  options: Word[];
  review: boolean;
};

export function assembleMultipleChoice(args: {
  words: readonly Word[];
  savedIds: ReadonlySet<number>;
  dueWords: readonly Word[];
  pool: 'library' | 'saved';
  oratorId: number | null;
  libraryOratorIds: ReadonlySet<number>;
  previousWordId: number | null;
  rng: Rng;
}): ChoiceRound | null {
  const savedOnly = args.pool === 'saved';
  const inLibrary = (word: Word) => args.libraryOratorIds.has(word.oratorId);
  let scoped = args.words.filter((word) => {
    if (savedOnly) return args.savedIds.has(word.id);
    if (args.oratorId != null) return word.oratorId === args.oratorId;
    return inLibrary(word);
  });
  if (!savedOnly && args.oratorId != null && scoped.length < MIN_OPTIONS) {
    scoped = args.words.filter(inLibrary);
  }
  if (scoped.length < MIN_OPTIONS) return null;

  const candidates = shuffle(scoped, args.rng).slice(0, CANDIDATE_POOL);
  const due = args.dueWords.filter((word) => word.id !== args.previousWordId);
  const fresh = candidates.filter((word) => word.id !== args.previousWordId);
  const correct = randomItem(due, args.rng) ?? randomItem(fresh.length > 0 ? fresh : candidates, args.rng);
  if (!correct) return null;
  const options = buildOptions(correct, candidates, MIN_OPTIONS, args.rng);
  if (options.length < MIN_OPTIONS) return null;
  return {
    correct,
    options,
    review: args.dueWords.some((word) => word.id === correct.id),
  };
}

export function letterCount(word: string): number {
  return [...word].filter((character) => /\p{L}/u.test(character)).length;
}

export function assembleLetterGuess(args: {
  words: readonly Word[];
  savedIds: ReadonlySet<number>;
  pool: 'library' | 'saved';
  oratorId: number | null;
  libraryOratorIds: ReadonlySet<number>;
  minLetters: number;
  maxLetters: number;
  excludeWordIds: ReadonlySet<number>;
  excludeDefinitions: ReadonlySet<string>;
  rng: Rng;
}): Word | null {
  const lengthOk = (word: Word) => {
    const count = letterCount(word.word);
    return count > 0 && count >= args.minLetters && count <= args.maxLetters;
  };
  const savedOnly = args.pool === 'saved';

  const poolFor = (oratorId: number | null): Word[] => {
    return args.words.filter((word) => {
      if (!lengthOk(word)) return false;
      if (savedOnly) return args.savedIds.has(word.id);
      if (oratorId != null) return word.oratorId === oratorId;
      return args.libraryOratorIds.has(word.oratorId);
    });
  };

  const pick = (candidates: readonly Word[]): Word | null => {
    if (candidates.length === 0) return null;
    const notSameWord = candidates.filter((word) => !args.excludeWordIds.has(word.id));
    const notSameDefinition = notSameWord.filter(
      (word) => !args.excludeDefinitions.has(word.definition.trim().toLowerCase()),
    );
    const chosen =
      notSameDefinition.length > 0 ? notSameDefinition : notSameWord.length > 0 ? notSameWord : candidates;
    return randomItem(shuffle(chosen, args.rng), args.rng);
  };

  const primary = pick(poolFor(savedOnly ? null : args.oratorId));
  if (primary) return primary;
  if (!savedOnly && args.oratorId != null) {
    const widened = pick(poolFor(null));
    if (widened) return widened;
  }
  return null;
}

export function guessTarget(word: Word): string {
  return normalizeGuess(word.word);
}
