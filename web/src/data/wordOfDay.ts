import { dayOfYear } from '../lib/format';
import type { Word } from '../types';

const FOUNDATIONAL = new Set(['basic', 'beginner']);

/**
 * Stable daily pick. When an orator is selected, the pool is only their words.
 * Intermediate and advanced entries lead, matching the Android selector's preference.
 */
export function selectWordOfDay(allWords: readonly Word[], oratorId: number | null, date: Date): Word | null {
  const scoped = oratorId == null ? allWords : allWords.filter((word) => word.oratorId === oratorId);
  if (scoped.length === 0) return null;
  const preferred = scoped.filter((word) => !FOUNDATIONAL.has(word.complexity));
  const pool = (preferred.length > 0 ? preferred : scoped).slice().sort((a, b) => a.id - b.id);
  const index = (dayOfYear(date) - 1) % pool.length;
  return pool[index] ?? null;
}
