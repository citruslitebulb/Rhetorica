import { previousDateString } from './dates';

export type DailyStreak = {
  streak: number;
  lastActiveDate: string;
};

/** Advance a consecutive-days streak. Same day stays put; yesterday continues; any gap restarts at 1. */
export function nextDailyStreak(currentStreak: number, lastActiveDate: string, today: string): DailyStreak {
  if (lastActiveDate === today) {
    return { streak: Math.max(1, currentStreak), lastActiveDate: today };
  }
  const yesterday = previousDateString(today);
  const continued = yesterday != null && lastActiveDate === yesterday && currentStreak > 0;
  return {
    streak: continued ? currentStreak + 1 : 1,
    lastActiveDate: today,
  };
}

/** A streak whose last activity was before yesterday is already broken. */
export function displayedDailyStreak(storedStreak: number, lastActiveDate: string, today: string): number {
  if (storedStreak <= 0 || lastActiveDate.trim().length === 0) return 0;
  const yesterday = previousDateString(today);
  if (lastActiveDate === today || lastActiveDate === yesterday) return storedStreak;
  return 0;
}
