/** Leitner boxes, matching LeitnerScheduler. Box 4 and above count as mastered. */

const DAY_MS = 86_400_000;

const INTERVALS_MS = [0, DAY_MS, 3 * DAY_MS, 7 * DAY_MS, 14 * DAY_MS, 30 * DAY_MS] as const;

export const MASTERED_BOX = 4;

export const MAX_BOX = INTERVALS_MS.length - 1;

export type Review = {
  box: number;
  nextDueAtEpochMillis: number;
};

export function intervalMillisFor(box: number): number {
  const index = Math.min(MAX_BOX, Math.max(0, box));
  return INTERVALS_MS[index] ?? 0;
}

export function nextBox(currentBox: number, correct: boolean): number {
  if (!correct) return 0;
  return Math.min(MAX_BOX, currentBox + 1);
}

export function nextDueAt(box: number, nowMillis: number): number {
  return nowMillis + intervalMillisFor(box);
}

export function isMastered(box: number): boolean {
  return box >= MASTERED_BOX;
}

export function review(currentBox: number, correct: boolean, nowMillis: number): Review {
  const box = nextBox(currentBox, correct);
  return { box, nextDueAtEpochMillis: nextDueAt(box, nowMillis) };
}
