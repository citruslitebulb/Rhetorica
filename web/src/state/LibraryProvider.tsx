import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { oratorById } from '../data/catalog';
import { readSelectedOratorId, writeSelectedOratorId } from '../storage/preferences';
import { loadSaved, storeSaved, toggleSaved } from '../storage/savedWords';
import type { SavedRecord } from '../types';

type LibraryContextValue = {
  selectedOratorId: number | null;
  selectOrator: (id: number | null) => void;
  saved: readonly SavedRecord[];
  isSaved: (wordId: number) => boolean;
  toggleSaved: (wordId: number) => void;
};

const LibraryContext = createContext<LibraryContextValue | null>(null);

function initialOrator(): number | null {
  const id = readSelectedOratorId(localStorage);
  return id != null && oratorById.has(id) ? id : null;
}

export function LibraryProvider({ children }: { children: ReactNode }) {
  const [selectedOratorId, setSelectedOratorId] = useState<number | null>(initialOrator);
  const [saved, setSaved] = useState<SavedRecord[]>(() => loadSaved(localStorage));

  const selectOrator = useCallback((id: number | null) => {
    const next = id != null && oratorById.has(id) ? id : null;
    setSelectedOratorId(next);
    writeSelectedOratorId(localStorage, next);
  }, []);

  const toggle = useCallback((wordId: number) => {
    setSaved((current) => {
      const next = toggleSaved(current, wordId);
      storeSaved(next, localStorage);
      return next;
    });
  }, []);

  const savedIds = useMemo(() => new Set(saved.map((record) => record.wordId)), [saved]);
  const isSaved = useCallback((wordId: number) => savedIds.has(wordId), [savedIds]);

  const value = useMemo(
    () => ({
      selectedOratorId,
      selectOrator,
      saved,
      isSaved,
      toggleSaved: toggle,
    }),
    [selectedOratorId, selectOrator, saved, isSaved, toggle],
  );

  return <LibraryContext.Provider value={value}>{children}</LibraryContext.Provider>;
}

export function useLibrary(): LibraryContextValue {
  const value = useContext(LibraryContext);
  if (!value) throw new Error('useLibrary must be used within LibraryProvider');
  return value;
}
