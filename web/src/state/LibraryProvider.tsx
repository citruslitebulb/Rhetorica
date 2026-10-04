import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { dictionaries, oratorById, wordById, words } from '../data/catalog';
import { ensureWordOfDay } from '../data/wordOfDay';
import { dayOfYear } from '../lib/format';
import { localDateString } from '../lib/dates';
import { rotationOratorIds, visibleOratorIds } from '../lib/learningPool';
import { resolveOnboarding } from '../lib/onboarding';
import { reminderIsDue, showWordNotification } from '../lib/reminder';
import type { VoiceFamily } from '../lib/voiceFamily';
import { loadSaved, storeSaved, toggleSaved } from '../storage/savedWords';
import {
  loadStudyPreferences,
  storeStudyPreferences,
  toggleFavorite,
  type StudyPreferences,
  type ThemeMode,
} from '../storage/preferences';
import {
  dueQuizWords,
  loadProgress,
  progressSnapshot,
  recordQuizResult,
  recordWordViewed,
  storeProgress,
  type ProgressSnapshot,
  type ProgressState,
} from '../storage/progress';
import type { Dictionary, SavedRecord, Word } from '../types';

type LibraryContextValue = {
  preferences: StudyPreferences;
  selectedOratorId: number | null;
  selectOrator: (id: number | null) => void;
  chooseDailyOrator: (id: number | null) => void;
  setRotateThroughAll: (enabled: boolean) => void;
  toggleFavoriteOrator: (id: number) => void;
  setIncludeLiterary: (include: boolean) => void;
  setIncludeFictional: (include: boolean) => void;
  setThemeMode: (mode: ThemeMode) => void;
  previewTheme: (mode: ThemeMode | null) => void;
  setReminder: (enabled: boolean, hour: number, minute: number) => void;
  visibleOrators: readonly Dictionary[];
  visibleOratorIds: readonly number[];
  libraryOratorIds: readonly number[];
  wordOfDay: Word | null;
  saved: readonly SavedRecord[];
  savedWords: readonly Word[];
  isSaved: (wordId: number) => boolean;
  toggleSaved: (wordId: number) => void;
  progress: ProgressState;
  snapshot: ProgressSnapshot;
  dueWords: (oratorIds: ReadonlySet<number>, savedOnly: boolean, limit?: number) => Word[];
  recordWordViewed: (wordId: number) => void;
  recordQuizAnswer: (wordId: number, correct: boolean) => void;
  skipSetup: () => void;
  finishSetup: (choice: {
    families: ReadonlySet<VoiceFamily>;
    themeMode: ThemeMode;
    notificationsEnabled: boolean;
    notificationHour: number;
    notificationMinute: number;
  }) => void;
};

const LibraryContext = createContext<LibraryContextValue | null>(null);

function ensure(preferences: StudyPreferences, date = new Date()) {
  const visible = visibleOratorIds(
    dictionaries,
    preferences.includeLiterary,
    preferences.includeFictional,
    preferences.selectedOratorId,
  );
  return ensureWordOfDay(words, preferences, visible, localDateString(date), dayOfYear(date));
}

function applyTheme(mode: ThemeMode) {
  const dark =
    mode === 'dark' || (mode === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
  document.documentElement.dataset.theme = dark ? 'dark' : 'light';
  document.documentElement.style.colorScheme = dark ? 'dark' : 'light';
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', dark ? '#14110e' : '#f8f4ed');
}

function initialPreferences(): StudyPreferences {
  const ensured = ensure(loadStudyPreferences(localStorage));
  storeStudyPreferences(ensured.preferences, localStorage);
  return ensured.preferences;
}

export function LibraryProvider({ children }: { children: ReactNode }) {
  const [preferences, setPreferences] = useState<StudyPreferences>(initialPreferences);
  const [themePreview, setThemePreview] = useState<ThemeMode | null>(null);
  const [saved, setSaved] = useState<SavedRecord[]>(() => loadSaved(localStorage));
  const [progress, setProgress] = useState<ProgressState>(() => loadProgress(localStorage));
  const [clock, setClock] = useState(() => Date.now());

  const commit = useCallback((next: StudyPreferences) => {
    const ensured = ensure(next);
    storeStudyPreferences(ensured.preferences, localStorage);
    setPreferences(ensured.preferences);
  }, []);

  useEffect(() => {
    const id = window.setInterval(() => {
      const date = new Date();
      setClock(date.getTime());
      setPreferences((current) => {
        const ensured = ensure(current, date);
        if (!ensured.changed) return current;
        storeStudyPreferences(ensured.preferences, localStorage);
        return ensured.preferences;
      });
    }, 30_000);
    return () => window.clearInterval(id);
  }, []);

  const themeMode = themePreview ?? preferences.themeMode;
  useEffect(() => {
    applyTheme(themeMode);
    if (themeMode !== 'system') return;
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => applyTheme('system');
    media.addEventListener('change', onChange);
    return () => media.removeEventListener('change', onChange);
  }, [themeMode]);

  useEffect(() => {
    if (!preferences.notificationsEnabled) return;
    const tick = () => {
      const stored = loadStudyPreferences(localStorage);
      const date = new Date();
      if (
        !reminderIsDue({
          enabled: stored.notificationsEnabled,
          hour: stored.notificationHour,
          minute: stored.notificationMinute,
          now: date,
          lastNotifiedDate: stored.lastNotifiedDate,
        })
      ) {
        return;
      }
      const word = stored.todaysWotdId == null ? undefined : wordById.get(stored.todaysWotdId);
      if (!word) return;
      const next = { ...stored, lastNotifiedDate: localDateString(date) };
      storeStudyPreferences(next, localStorage);
      setPreferences(next);
      showWordNotification(word.word, word.definition, word.id);
    };
    tick();
    const id = window.setInterval(tick, 15_000);
    return () => window.clearInterval(id);
  }, [
    preferences.notificationsEnabled,
    preferences.notificationHour,
    preferences.notificationMinute,
    preferences.lastNotifiedDate,
    preferences.todaysWotdId,
  ]);

  const visibleIds = useMemo(
    () =>
      visibleOratorIds(
        dictionaries,
        preferences.includeLiterary,
        preferences.includeFictional,
        preferences.selectedOratorId,
      ),
    [preferences.includeFictional, preferences.includeLiterary, preferences.selectedOratorId],
  );
  const visibleIdSet = useMemo(() => new Set(visibleIds), [visibleIds]);
  const visibleOrators = useMemo(
    () => dictionaries.filter((orator) => visibleIdSet.has(orator.id)),
    [visibleIdSet],
  );
  const libraryIds = useMemo(
    () => rotationOratorIds(visibleIds, preferences.rotateThroughAll, preferences.favoriteOratorIds),
    [preferences.favoriteOratorIds, preferences.rotateThroughAll, visibleIds],
  );

  const selectOrator = useCallback((id: number | null) => {
    const nextId = id != null && oratorById.has(id) ? id : null;
    setPreferences((current) => {
      if (current.selectedOratorId === nextId) return current;
      const ensured = ensure({ ...current, selectedOratorId: nextId });
      storeStudyPreferences(ensured.preferences, localStorage);
      return ensured.preferences;
    });
  }, []);

  const chooseDailyOrator = useCallback(
    (id: number | null) => {
      const nextId = id != null && oratorById.has(id) ? id : null;
      commit({
        ...preferences,
        selectedOratorId: nextId,
        rotateThroughAll: nextId != null ? false : preferences.rotateThroughAll,
      });
    },
    [commit, preferences],
  );

  const savedIds = useMemo(() => new Set(saved.map((record) => record.wordId)), [saved]);
  const savedWords = useMemo(
    () => saved.flatMap((record) => {
      const word = wordById.get(record.wordId);
      return word ? [word] : [];
    }),
    [saved],
  );
  const today = localDateString(new Date(clock));
  const snapshot = useMemo(
    () => progressSnapshot(progress, savedWords.length, today, clock),
    [clock, progress, savedWords.length, today],
  );
  const wordOfDay = preferences.todaysWotdDate === today && preferences.todaysWotdId != null
    ? wordById.get(preferences.todaysWotdId) ?? null
    : null;

  const toggle = useCallback((wordId: number) => {
    setSaved((current) => {
      const next = toggleSaved(current, wordId);
      storeSaved(next, localStorage);
      return next;
    });
  }, []);

  const viewWord = useCallback((wordId: number) => {
    setProgress((current) => {
      const next = recordWordViewed(current, wordId, localDateString(new Date()));
      storeProgress(next, localStorage);
      return next;
    });
  }, []);

  const answerQuiz = useCallback((wordId: number, correct: boolean) => {
    const date = new Date();
    setProgress((current) => {
      const next = recordQuizResult(current, wordId, correct, date.getTime(), localDateString(date));
      storeProgress(next, localStorage);
      return next;
    });
  }, []);

  const value = useMemo<LibraryContextValue>(
    () => ({
      preferences,
      selectedOratorId: preferences.selectedOratorId,
      selectOrator,
      chooseDailyOrator,
      setRotateThroughAll: (enabled) => commit({ ...preferences, rotateThroughAll: enabled }),
      toggleFavoriteOrator: (id) => commit(toggleFavorite(preferences, id)),
      setIncludeLiterary: (include) => commit({ ...preferences, includeLiterary: include }),
      setIncludeFictional: (include) => commit({ ...preferences, includeFictional: include }),
      setThemeMode: (mode) => {
        setThemePreview(null);
        commit({ ...preferences, themeMode: mode });
      },
      previewTheme: setThemePreview,
      setReminder: (enabled, hour, minute) =>
        commit({
          ...preferences,
          notificationsEnabled: enabled,
          notificationHour: hour,
          notificationMinute: minute,
        }),
      visibleOrators,
      visibleOratorIds: visibleIds,
      libraryOratorIds: libraryIds,
      wordOfDay,
      saved,
      savedWords,
      isSaved: (wordId) => savedIds.has(wordId),
      toggleSaved: toggle,
      progress,
      snapshot,
      dueWords: (oratorIds, savedOnly, limit = 8) =>
        dueQuizWords({
          progress,
          words,
          oratorIds,
          savedOnly,
          savedIds,
          nowMillis: Date.now(),
          limit,
        }),
      recordWordViewed: viewWord,
      recordQuizAnswer: answerQuiz,
      skipSetup: () => commit({ ...preferences, onboardingStatus: preferences.onboardingStatus === 'pending' ? 'skipped' : preferences.onboardingStatus }),
      finishSetup: (choice) => {
        const patch = resolveOnboarding(dictionaries, choice.families, new Set(), new Set());
        const selectedOratorId =
          preferences.selectedOratorId != null && patch.selectedOratorId == null
            ? preferences.selectedOratorId
            : patch.selectedOratorId;
        setThemePreview(null);
        commit({
          ...preferences,
          favoriteOratorIds: patch.favoriteOratorIds,
          selectedOratorId,
          rotateThroughAll: patch.rotateThroughAll,
          includeLiterary: patch.includeLiterary,
          includeFictional: patch.includeFictional,
          themeMode: choice.themeMode,
          notificationsEnabled: choice.notificationsEnabled,
          notificationHour: choice.notificationHour,
          notificationMinute: choice.notificationMinute,
          onboardingStatus: 'completed',
          todaysWotdId: null,
          todaysWotdDate: '',
        });
      },
    }),
    [
      answerQuiz,
      chooseDailyOrator,
      commit,
      libraryIds,
      preferences,
      progress,
      saved,
      savedIds,
      savedWords,
      selectOrator,
      snapshot,
      toggle,
      viewWord,
      visibleIds,
      visibleOrators,
      wordOfDay,
    ],
  );

  return <LibraryContext.Provider value={value}>{children}</LibraryContext.Provider>;
}

export function useLibrary(): LibraryContextValue {
  const value = useContext(LibraryContext);
  if (!value) throw new Error('useLibrary must be used within LibraryProvider');
  return value;
}
