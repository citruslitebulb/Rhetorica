import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { WordCard } from '../components/WordCard';
import { oratorById, wordById } from '../data/catalog';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { orderSaved } from '../storage/savedWords';
import { useLibrary } from '../state/LibraryProvider';
import type { SavedSort } from '../types';

const SORTS: { id: SavedSort; label: string }[] = [
  { id: 'newest', label: 'Newest' },
  { id: 'alphabetical', label: 'A–Z' },
  { id: 'partOfSpeech', label: 'Part of speech' },
];

export function SavedPage() {
  useDocumentTitle('Saved');
  const { saved } = useLibrary();
  const [sort, setSort] = useState<SavedSort>('newest');
  const [oratorFilter, setOratorFilter] = useState<number | 'all'>('all');

  const ordered = useMemo(() => orderSaved(saved, sort, wordById), [saved, sort]);
  const oratorOptions = useMemo(() => {
    const ids = new Set(ordered.map((word) => word.oratorId));
    return [...ids]
      .map((id) => oratorById.get(id))
      .filter((orator) => orator != null)
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [ordered]);
  const visible = oratorFilter === 'all' ? ordered : ordered.filter((word) => word.oratorId === oratorFilter);

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">Library</p>
        <h1>Saved</h1>
        <p className="lede">Your saved vocabulary, ready for a quick revisit. It stays in this browser.</p>
      </header>
      <p className="result-count">
        {oratorFilter === 'all'
          ? ordered.length === 1
            ? '1 saved'
            : `${ordered.length} saved`
          : `${visible.length} of ${ordered.length} saved`}
      </p>
      {ordered.length > 0 ? (
        <>
          <div className="chip-row" role="group" aria-label="Sort">
            {SORTS.map((item) => (
              <button
                key={item.id}
                type="button"
                className={sort === item.id ? 'chip is-on' : 'chip'}
                aria-pressed={sort === item.id}
                onClick={() => setSort(item.id)}
              >
                {item.label}
              </button>
            ))}
          </div>
          {oratorOptions.length > 1 ? (
            <label className="field" htmlFor="saved-orator">
              <span>Filter</span>
              <select
                id="saved-orator"
                value={oratorFilter === 'all' ? '' : String(oratorFilter)}
                onChange={(event) => {
                  const next = event.target.value;
                  setOratorFilter(next === '' ? 'all' : Number(next));
                }}
              >
                <option value="">All orators</option>
                {oratorOptions.map((orator) => (
                  <option key={orator.id} value={orator.id}>
                    {orator.name}
                  </option>
                ))}
              </select>
            </label>
          ) : null}
        </>
      ) : null}
      {visible.length === 0 ? (
        <div className="empty">
          <p className="empty-title">{ordered.length === 0 ? 'No saved words yet' : 'No saved words match this filter'}</p>
          <p>
            {ordered.length === 0
              ? 'Save words from the feed or a word page to collect them here.'
              : 'Try another orator, or clear the filter.'}
          </p>
          {ordered.length === 0 ? (
            <Link to="/words" className="button primary">
              Browse words
            </Link>
          ) : null}
        </div>
      ) : (
        <ul className="word-list">
          {visible.map((word) => (
            <li key={word.id}>
              <WordCard word={word} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
