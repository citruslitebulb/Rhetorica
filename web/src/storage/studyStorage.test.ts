import { describe, expect, it } from 'vitest';
import { memoryStorage } from './memoryStorage';
import { SELECTED_ORATOR_KEY, loadStudyPreferences, storeStudyPreferences } from './preferences';
import { dueQuizWords, emptyProgress, progressSnapshot, recordQuizResult, recordWordViewed } from './progress';
import type { Word } from '../types';
import { isCatalogVisible, visibleOratorIds } from '../lib/learningPool';
import { reminderIsDue } from '../lib/reminder';
import { millisUntilNext } from '../lib/dates';

describe('study preferences', () => {
  it('keeps a previously selected literary orator while literary voices stay hidden', () => {
    const storage = memoryStorage({ [SELECTED_ORATOR_KEY]: '42' });
    const loaded = loadStudyPreferences(storage);
    expect(loaded.selectedOratorId).toBe(42);
    expect(loaded.includeLiterary).toBe(false);
    expect(loaded.includeFictional).toBe(false);
    expect(loaded.notificationsEnabled).toBe(false);
    expect(loaded.themeMode).toBe('dark');
    const orators = [
      { id: 1, category: 'Ancient Classics' },
      { id: 42, category: 'Literary Classics' },
      { id: 9, category: 'Fictional / Mythic' },
    ];
    expect(isCatalogVisible('Literary Classics', false, false)).toBe(false);
    expect(visibleOratorIds(orators, false, false, 42)).toEqual([1, 42]);
    expect(visibleOratorIds(orators, false, false, null)).toEqual([1]);
  });

  it('round-trips the preferences blob without dropping the legacy orator key', () => {
    const storage = memoryStorage();
    const loaded = loadStudyPreferences(storage);
    storeStudyPreferences({ ...loaded, selectedOratorId: 7, rotateThroughAll: true, themeMode: 'light' }, storage);
    const again = loadStudyPreferences(storage);
    expect(again.selectedOratorId).toBe(7);
    expect(again.rotateThroughAll).toBe(true);
    expect(again.themeMode).toBe('light');
    expect(storage.getItem(SELECTED_ORATOR_KEY)).toBe('7');
  });
});

describe('progress and reminders', () => {
  it('counts an open, a quiz, mastered words, and words that are due', () => {
    let progress = recordWordViewed(emptyProgress(), 8, '2026-04-02');
    progress = recordQuizResult(progress, 8, true, 1_000, '2026-04-02');
    progress = recordQuizResult(progress, 8, true, 2_000, '2026-04-02');
    progress = recordQuizResult(progress, 8, true, 3_000, '2026-04-02');
    progress = recordQuizResult(progress, 8, true, 4_000, '2026-04-02');
    const snapshot = progressSnapshot(progress, 2, '2026-04-02', 4_000);
    expect(snapshot.uniqueWordsOpened).toBe(1);
    expect(snapshot.savedCount).toBe(2);
    expect(snapshot.quizAttemptCount).toBe(4);
    expect(snapshot.quizCorrectCount).toBe(4);
    expect(snapshot.dailyStreak).toBe(1);
    expect(snapshot.masteredCount).toBe(1);
    expect(snapshot.dueCount).toBe(0);
    const word = {
      id: 8,
      word: 'valor',
      definition: 'Courage.',
      example: '',
      partOfSpeech: 'noun',
      oratorId: 3,
      complexity: 'intermediate',
      source: null,
      speech: null,
      categories: [],
      pronunciation: null,
    } satisfies Word;
    const missed = recordQuizResult(progress, 8, false, 5_000, '2026-04-03');
    expect(dueQuizWords({
      progress: missed,
      words: [word],
      oratorIds: new Set([3]),
      savedOnly: false,
      savedIds: new Set(),
      nowMillis: 5_000,
      limit: 8,
    }).map((item) => item.id)).toEqual([8]);
  });

  it('is due after the chosen local time and not twice the same day', () => {
    const morning = new Date(2026, 3, 2, 7, 59, 0);
    const evening = new Date(2026, 3, 2, 8, 5, 0);
    expect(reminderIsDue({ enabled: false, hour: 8, minute: 0, now: evening, lastNotifiedDate: '' })).toBe(false);
    expect(reminderIsDue({ enabled: true, hour: 8, minute: 0, now: morning, lastNotifiedDate: '' })).toBe(false);
    expect(reminderIsDue({ enabled: true, hour: 8, minute: 0, now: evening, lastNotifiedDate: '' })).toBe(true);
    expect(reminderIsDue({ enabled: true, hour: 8, minute: 0, now: evening, lastNotifiedDate: '2026-04-02' })).toBe(false);
    const exactly = new Date(2026, 3, 2, 8, 0, 0);
    expect(millisUntilNext(8, 0, exactly)).toBeGreaterThan(23 * 60 * 60 * 1000);
    expect(millisUntilNext(8, 30, morning)).toBe(31 * 60 * 1000);
  });
});
