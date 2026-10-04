export const SELECTED_ORATOR_KEY = 'rhetorica.selected-orator.v1';
export const PREFERENCES_KEY = 'rhetorica.preferences.v1';

export type ThemeMode = 'system' | 'dark' | 'light';
export type OnboardingStatus = 'pending' | 'completed' | 'skipped';

export type StudyPreferences = {
  selectedOratorId: number | null;
  rotateThroughAll: boolean;
  favoriteOratorIds: number[];
  includeLiterary: boolean;
  includeFictional: boolean;
  themeMode: ThemeMode;
  notificationsEnabled: boolean;
  notificationHour: number;
  notificationMinute: number;
  onboardingStatus: OnboardingStatus;
  shownWotdIds: number[];
  shownWotdPoolKey: string;
  todaysWotdId: number | null;
  todaysWotdDate: string;
  lastNotifiedDate: string;
};

export function defaultPreferences(): StudyPreferences {
  return {
    selectedOratorId: null,
    rotateThroughAll: false,
    favoriteOratorIds: [],
    includeLiterary: false,
    includeFictional: false,
    themeMode: 'dark',
    notificationsEnabled: false,
    notificationHour: 8,
    notificationMinute: 0,
    onboardingStatus: 'pending',
    shownWotdIds: [],
    shownWotdPoolKey: '',
    todaysWotdId: null,
    todaysWotdDate: '',
    lastNotifiedDate: '',
  };
}

export function readSelectedOratorId(storage: Pick<Storage, 'getItem'>): number | null {
  const raw = storage.getItem(SELECTED_ORATOR_KEY);
  if (!raw) return null;
  const id = Number(raw);
  if (!Number.isInteger(id) || id <= 0) return null;
  return id;
}

export function writeSelectedOratorId(storage: Pick<Storage, 'setItem' | 'removeItem'>, id: number | null): void {
  if (id == null) storage.removeItem(SELECTED_ORATOR_KEY);
  else storage.setItem(SELECTED_ORATOR_KEY, String(id));
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function integerList(value: unknown): number[] {
  if (!Array.isArray(value)) return [];
  return value.filter((item): item is number => typeof item === 'number' && Number.isInteger(item) && item > 0);
}

function optionalId(value: unknown): number | null {
  if (typeof value !== 'number' || !Number.isInteger(value) || value <= 0) return null;
  return value;
}

function themeMode(value: unknown): ThemeMode {
  if (value === 'system' || value === 'light' || value === 'dark') return value;
  return 'dark';
}

function onboardingStatus(value: unknown): OnboardingStatus {
  if (value === 'completed' || value === 'skipped' || value === 'pending') return value;
  return 'pending';
}

function clock(value: unknown, fallback: number, max: number): number {
  if (typeof value !== 'number' || !Number.isInteger(value)) return fallback;
  return Math.min(max, Math.max(0, value));
}

export function parsePreferences(raw: string | null, legacyOratorId: number | null): StudyPreferences {
  const base = defaultPreferences();
  if (!raw) {
    return { ...base, selectedOratorId: legacyOratorId };
  }
  try {
    const parsed: unknown = JSON.parse(raw);
    if (!isRecord(parsed)) return { ...base, selectedOratorId: legacyOratorId };
    const selected = optionalId(parsed.selectedOratorId);
    return {
      selectedOratorId: selected ?? (parsed.selectedOratorId === null ? null : legacyOratorId),
      rotateThroughAll: parsed.rotateThroughAll === true,
      favoriteOratorIds: integerList(parsed.favoriteOratorIds),
      includeLiterary: parsed.includeLiterary === true,
      includeFictional: parsed.includeFictional === true,
      themeMode: themeMode(parsed.themeMode),
      notificationsEnabled: parsed.notificationsEnabled === true,
      notificationHour: clock(parsed.notificationHour, 8, 23),
      notificationMinute: clock(parsed.notificationMinute, 0, 59),
      onboardingStatus: onboardingStatus(parsed.onboardingStatus),
      shownWotdIds: integerList(parsed.shownWotdIds),
      shownWotdPoolKey: typeof parsed.shownWotdPoolKey === 'string' ? parsed.shownWotdPoolKey : '',
      todaysWotdId: optionalId(parsed.todaysWotdId),
      todaysWotdDate: typeof parsed.todaysWotdDate === 'string' ? parsed.todaysWotdDate : '',
      lastNotifiedDate: typeof parsed.lastNotifiedDate === 'string' ? parsed.lastNotifiedDate : '',
    };
  } catch {
    return { ...base, selectedOratorId: legacyOratorId };
  }
}

export function loadStudyPreferences(storage: Pick<Storage, 'getItem'>): StudyPreferences {
  return parsePreferences(storage.getItem(PREFERENCES_KEY), readSelectedOratorId(storage));
}

export function storeStudyPreferences(preferences: StudyPreferences, storage: Pick<Storage, 'setItem' | 'removeItem'>): void {
  storage.setItem(PREFERENCES_KEY, JSON.stringify(preferences));
  writeSelectedOratorId(storage, preferences.selectedOratorId);
}

export function toggleFavorite(preferences: StudyPreferences, oratorId: number): StudyPreferences {
  const favoriteOratorIds = preferences.favoriteOratorIds.includes(oratorId)
    ? preferences.favoriteOratorIds.filter((id) => id !== oratorId)
    : [...preferences.favoriteOratorIds, oratorId];
  return { ...preferences, favoriteOratorIds };
}
