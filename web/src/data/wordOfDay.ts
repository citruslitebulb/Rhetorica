import { dayOfYear } from '../lib/format';
import type { StudyPreferences } from '../storage/preferences';
import type { Word } from '../types';

const FOUNDATIONAL = new Set(['basic', 'beginner']);

export type WotdPick = {
  word: Word | null;
  shownIds: number[];
  cycleReset: boolean;
};

export function normalizeHeadword(word: string): string {
  return word.trim().toLowerCase();
}

/** `null` means the daily word rotates through the catalog (optionally favorites only). */
export function resolveOratorId(
  selectedOratorId: number | null,
  rotateThroughAll: boolean,
  visibleOratorIds: ReadonlySet<number> | null = null,
): number | null {
  if (rotateThroughAll) return null;
  if (selectedOratorId == null) return null;
  if (visibleOratorIds != null && !visibleOratorIds.has(selectedOratorId)) return null;
  return selectedOratorId;
}

export function poolKey(
  oratorId: number | null,
  favoriteOratorIds: readonly number[] = [],
  includeLiterary = false,
  includeFictional = false,
): string {
  const catalog = `lit:${includeLiterary ? 1 : 0}|fic:${includeFictional ? 1 : 0}`;
  const base =
    oratorId != null
      ? `orator:${oratorId}`
      : favoriteOratorIds.length > 0
        ? `favorites:${[...favoriteOratorIds].sort((a, b) => a - b).join(',')}`
        : 'all';
  return `${base}|${catalog}`;
}

export function dayOffset(poolSize: number, day: number): number {
  if (poolSize <= 0) return 0;
  return (day - 1) % poolSize;
}

export function wordPool(
  allWords: readonly Word[],
  oratorId: number | null,
  favoriteOratorIds: readonly number[] = [],
  visibleOratorIds: ReadonlySet<number> | null = null,
): Word[] {
  const catalogScoped =
    visibleOratorIds == null ? allWords.slice() : allWords.filter((word) => visibleOratorIds.has(word.oratorId));
  if (oratorId != null) return catalogScoped.filter((word) => word.oratorId === oratorId);
  if (favoriteOratorIds.length === 0) return catalogScoped;
  const allowed = new Set(favoriteOratorIds);
  return catalogScoped.filter((word) => allowed.has(word.oratorId));
}

/**
 * Prefer words not yet shown in this cycle. A repeated headword from another
 * orator counts as shown. Intermediate and advanced words lead; basic words
 * wait at the tail. The index inside the working set follows the calendar day.
 */
export function selectUnseen(
  allWords: readonly Word[],
  oratorId: number | null,
  shownIds: ReadonlySet<number>,
  day: number,
  favoriteOratorIds: readonly number[] = [],
  visibleOratorIds: ReadonlySet<number> | null = null,
): WotdPick {
  const candidates = wordPool(allWords, oratorId, favoriteOratorIds, visibleOratorIds);
  if (candidates.length === 0) return { word: null, shownIds: [], cycleReset: false };

  const shownHeadwords = new Set(
    candidates.filter((word) => shownIds.has(word.id)).map((word) => normalizeHeadword(word.word)),
  );
  const unseen = candidates.filter(
    (word) => !shownIds.has(word.id) && !shownHeadwords.has(normalizeHeadword(word.word)),
  );
  const cycleReset = unseen.length === 0;
  const preferred = unseen.filter((word) => !FOUNDATIONAL.has(word.complexity));
  const working = cycleReset ? candidates : preferred.length > 0 ? preferred : unseen;
  const sorted = working.slice().sort((a, b) => a.id - b.id);
  const word = sorted[dayOffset(sorted.length, day)] ?? null;
  if (!word) return { word: null, shownIds: [], cycleReset };
  const nextShown = cycleReset ? [word.id] : [...shownIds, word.id];
  return { word, shownIds: nextShown, cycleReset };
}

export type EnsuredWordOfDay = {
  word: Word | null;
  preferences: StudyPreferences;
  changed: boolean;
};

/**
 * One persisted pick per calendar day and pool. A later call the same day
 * returns the stored word instead of stepping the unseen cycle again.
 */
export function ensureWordOfDay(
  allWords: readonly Word[],
  preferences: StudyPreferences,
  visibleIds: readonly number[],
  today: string,
  day: number,
): EnsuredWordOfDay {
  const visible = new Set(visibleIds);
  const oratorId = resolveOratorId(preferences.selectedOratorId, preferences.rotateThroughAll, visible);
  const favorites =
    oratorId == null ? preferences.favoriteOratorIds.filter((id) => visible.has(id)) : [];
  const key = poolKey(oratorId, favorites, preferences.includeLiterary, preferences.includeFictional);
  const byId = new Map(allWords.map((word) => [word.id, word]));

  if (preferences.todaysWotdDate === today && preferences.todaysWotdId != null && preferences.shownWotdPoolKey === key) {
    const existing = byId.get(preferences.todaysWotdId);
    if (existing) return { word: existing, preferences, changed: false };
  }

  const shown = preferences.shownWotdPoolKey === key ? new Set(preferences.shownWotdIds) : new Set<number>();
  const pick = selectUnseen(allWords, oratorId, shown, day, favorites, visible);
  if (!pick.word) return { word: null, preferences, changed: false };
  return {
    word: pick.word,
    changed: true,
    preferences: {
      ...preferences,
      shownWotdIds: pick.shownIds,
      shownWotdPoolKey: key,
      todaysWotdId: pick.word.id,
      todaysWotdDate: today,
    },
  };
}

export function dayIndex(date: Date): number {
  return dayOfYear(date);
}
