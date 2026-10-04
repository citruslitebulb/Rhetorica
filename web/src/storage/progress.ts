import { displayedDailyStreak, nextDailyStreak } from '../lib/habit';
import { isMastered, review } from '../lib/leitner';
import type { Word } from '../types';

export const PROGRESS_KEY = 'rhetorica.progress.v1';

export type WordProgress = {
  box: number;
  correctCount: number;
  incorrectCount: number;
  lastReviewedAtEpochMillis: number;
  nextDueAtEpochMillis: number;
};

export type ProgressState = {
  openedWordIds: number[];
  quizCorrectCount: number;
  quizAttemptCount: number;
  quizStreak: number;
  bestQuizStreak: number;
  dailyStreak: number;
  bestDailyStreak: number;
  lastActiveDate: string;
  words: Record<string, WordProgress>;
};

export type ProgressSnapshot = {
  uniqueWordsOpened: number;
  savedCount: number;
  quizCorrectCount: number;
  quizAttemptCount: number;
  quizStreak: number;
  bestQuizStreak: number;
  dailyStreak: number;
  bestDailyStreak: number;
  masteredCount: number;
  dueCount: number;
};

export function emptyProgress(): ProgressState {
  return {
    openedWordIds: [],
    quizCorrectCount: 0,
    quizAttemptCount: 0,
    quizStreak: 0,
    bestQuizStreak: 0,
    dailyStreak: 0,
    bestDailyStreak: 0,
    lastActiveDate: '',
    words: {},
  };
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function parseWordProgress(value: unknown): WordProgress | null {
  if (!isRecord(value)) return null;
  const box = value.box;
  const correctCount = value.correctCount;
  const incorrectCount = value.incorrectCount;
  const lastReviewedAtEpochMillis = value.lastReviewedAtEpochMillis;
  const nextDueAtEpochMillis = value.nextDueAtEpochMillis;
  if (typeof box !== 'number' || typeof correctCount !== 'number' || typeof incorrectCount !== 'number') return null;
  if (typeof lastReviewedAtEpochMillis !== 'number' || typeof nextDueAtEpochMillis !== 'number') return null;
  return { box, correctCount, incorrectCount, lastReviewedAtEpochMillis, nextDueAtEpochMillis };
}

export function parseProgress(raw: string | null): ProgressState {
  const base = emptyProgress();
  if (!raw) return base;
  try {
    const parsed: unknown = JSON.parse(raw);
    if (!isRecord(parsed)) return base;
    const openedWordIds = Array.isArray(parsed.openedWordIds)
      ? parsed.openedWordIds.filter((id): id is number => typeof id === 'number' && Number.isInteger(id) && id > 0)
      : [];
    const words: Record<string, WordProgress> = {};
    if (isRecord(parsed.words)) {
      for (const [key, value] of Object.entries(parsed.words)) {
        const row = parseWordProgress(value);
        if (row && /^\d+$/.test(key)) words[key] = row;
      }
    }
    const numberOr = (value: unknown, fallback: number) => (typeof value === 'number' && Number.isFinite(value) ? value : fallback);
    return {
      openedWordIds,
      quizCorrectCount: numberOr(parsed.quizCorrectCount, 0),
      quizAttemptCount: numberOr(parsed.quizAttemptCount, 0),
      quizStreak: numberOr(parsed.quizStreak, 0),
      bestQuizStreak: numberOr(parsed.bestQuizStreak, 0),
      dailyStreak: numberOr(parsed.dailyStreak, 0),
      bestDailyStreak: numberOr(parsed.bestDailyStreak, 0),
      lastActiveDate: typeof parsed.lastActiveDate === 'string' ? parsed.lastActiveDate : '',
      words,
    };
  } catch {
    return base;
  }
}

export function loadProgress(storage: Pick<Storage, 'getItem'>): ProgressState {
  return parseProgress(storage.getItem(PROGRESS_KEY));
}

export function storeProgress(progress: ProgressState, storage: Pick<Storage, 'setItem'>): void {
  storage.setItem(PROGRESS_KEY, JSON.stringify(progress));
}

function withActivity(progress: ProgressState, today: string): ProgressState {
  const next = nextDailyStreak(progress.dailyStreak, progress.lastActiveDate, today);
  return {
    ...progress,
    dailyStreak: next.streak,
    bestDailyStreak: Math.max(progress.bestDailyStreak, next.streak),
    lastActiveDate: next.lastActiveDate,
  };
}

export function recordWordViewed(progress: ProgressState, wordId: number, today: string): ProgressState {
  if (!Number.isInteger(wordId) || wordId <= 0) return progress;
  const opened = progress.openedWordIds.includes(wordId)
    ? progress.openedWordIds
    : [...progress.openedWordIds, wordId];
  return withActivity({ ...progress, openedWordIds: opened }, today);
}

export function recordQuizResult(
  progress: ProgressState,
  wordId: number,
  correct: boolean,
  nowMillis: number,
  today: string,
): ProgressState {
  const nextStreak = correct ? progress.quizStreak + 1 : 0;
  let next = withActivity(
    {
      ...progress,
      quizCorrectCount: progress.quizCorrectCount + (correct ? 1 : 0),
      quizAttemptCount: progress.quizAttemptCount + 1,
      quizStreak: nextStreak,
      bestQuizStreak: Math.max(progress.bestQuizStreak, nextStreak),
    },
    today,
  );
  if (wordId > 0) {
    const existing = progress.words[String(wordId)];
    const moved = review(existing?.box ?? 0, correct, nowMillis);
    next = {
      ...next,
      words: {
        ...next.words,
        [String(wordId)]: {
          box: moved.box,
          correctCount: (existing?.correctCount ?? 0) + (correct ? 1 : 0),
          incorrectCount: (existing?.incorrectCount ?? 0) + (correct ? 0 : 1),
          lastReviewedAtEpochMillis: nowMillis,
          nextDueAtEpochMillis: moved.nextDueAtEpochMillis,
        },
      },
    };
  }
  return next;
}

export function progressSnapshot(progress: ProgressState, savedCount: number, today: string, nowMillis: number): ProgressSnapshot {
  const rows = Object.values(progress.words);
  return {
    uniqueWordsOpened: progress.openedWordIds.length,
    savedCount,
    quizCorrectCount: progress.quizCorrectCount,
    quizAttemptCount: progress.quizAttemptCount,
    quizStreak: progress.quizStreak,
    bestQuizStreak: progress.bestQuizStreak,
    dailyStreak: displayedDailyStreak(progress.dailyStreak, progress.lastActiveDate, today),
    bestDailyStreak: progress.bestDailyStreak,
    masteredCount: rows.filter((row) => isMastered(row.box)).length,
    dueCount: rows.filter((row) => row.nextDueAtEpochMillis <= nowMillis).length,
  };
}

export function dueQuizWords(args: {
  progress: ProgressState;
  words: readonly Word[];
  oratorIds: ReadonlySet<number>;
  savedOnly: boolean;
  savedIds: ReadonlySet<number>;
  nowMillis: number;
  limit: number;
}): Word[] {
  const byId = new Map(args.words.map((word) => [word.id, word]));
  const due = Object.entries(args.progress.words)
    .filter(([, row]) => row.nextDueAtEpochMillis <= args.nowMillis)
    .sort((a, b) => a[1].nextDueAtEpochMillis - b[1].nextDueAtEpochMillis || Number(a[0]) - Number(b[0]))
    .flatMap(([id]) => {
      const word = byId.get(Number(id));
      if (!word) return [];
      if (args.savedOnly) return args.savedIds.has(word.id) ? [word] : [];
      if (args.oratorIds.size === 0) return [];
      return args.oratorIds.has(word.oratorId) ? [word] : [];
    });
  return due.slice(0, args.limit);
}
