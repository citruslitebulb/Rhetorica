import { describe, expect, it } from 'vitest';
import { MASTERED_BOX, isMastered, nextBox, review } from './leitner';

describe('Leitner scheduler', () => {
  it('advances a box on a correct answer and resets on a miss', () => {
    expect(nextBox(0, true)).toBe(1);
    expect(nextBox(5, true)).toBe(5);
    expect(nextBox(3, false)).toBe(0);
  });

  it('marks box 4 and above as mastered and schedules the next review', () => {
    expect(isMastered(MASTERED_BOX - 1)).toBe(false);
    expect(isMastered(MASTERED_BOX)).toBe(true);
    const moved = review(0, true, 1_000);
    expect(moved.box).toBe(1);
    expect(moved.nextDueAtEpochMillis).toBe(1_000 + 86_400_000);
    expect(review(2, false, 5_000)).toEqual({ box: 0, nextDueAtEpochMillis: 5_000 });
  });
});
