import { describe, expect, it } from 'vitest';
import { displayedDailyStreak, nextDailyStreak } from './habit';

describe('daily streak', () => {
  it('stays on the same day, continues from yesterday, and restarts after a gap', () => {
    expect(nextDailyStreak(3, '2026-04-02', '2026-04-02')).toEqual({ streak: 3, lastActiveDate: '2026-04-02' });
    expect(nextDailyStreak(3, '2026-04-01', '2026-04-02')).toEqual({ streak: 4, lastActiveDate: '2026-04-02' });
    expect(nextDailyStreak(3, '2026-03-01', '2026-04-02')).toEqual({ streak: 1, lastActiveDate: '2026-04-02' });
    expect(nextDailyStreak(0, '', '2026-04-02')).toEqual({ streak: 1, lastActiveDate: '2026-04-02' });
  });

  it('hides a streak that already broke', () => {
    expect(displayedDailyStreak(4, '2026-04-02', '2026-04-02')).toBe(4);
    expect(displayedDailyStreak(4, '2026-04-01', '2026-04-02')).toBe(4);
    expect(displayedDailyStreak(4, '2026-03-30', '2026-04-02')).toBe(0);
    expect(displayedDailyStreak(0, '2026-04-02', '2026-04-02')).toBe(0);
  });
});
