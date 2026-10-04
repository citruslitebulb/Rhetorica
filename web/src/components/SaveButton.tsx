import { BookmarkIcon } from './Icons';
import { useLibrary } from '../state/LibraryProvider';

export function SaveButton({ wordId, compact = false }: { wordId: number; compact?: boolean }) {
  const { isSaved, toggleSaved } = useLibrary();
  const saved = isSaved(wordId);
  return (
    <button
      type="button"
      className={saved ? 'save-button is-saved' : 'save-button'}
      aria-pressed={saved}
      aria-label={saved ? 'Remove saved word' : 'Save word'}
      onClick={() => toggleSaved(wordId)}
    >
      <BookmarkIcon filled={saved} />
      {compact ? null : <span>{saved ? 'Saved' : 'Save'}</span>}
    </button>
  );
}
