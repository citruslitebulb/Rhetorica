import { useDeferredValue, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { WordCard } from '../components/WordCard';
import { oratorById, words } from '../data/catalog';
import { COMPLEXITY_TIERS, matchesComplexity, toggleComplexityTier, type ComplexityTier } from '../lib/complexity';
import { themeLabel } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';

const PAGE_SIZE = 36;

export function WordsPage() {
  useDocumentTitle('Words');
  const { selectedOratorId, visibleOratorIds, wordOfDay } = useLibrary();
  const selected = selectedOratorId == null ? undefined : oratorById.get(selectedOratorId);
  const [scope, setScope] = useState<'selected' | 'all'>(selected ? 'selected' : 'all');
  const [complexity, setComplexity] = useState<ReadonlySet<ComplexityTier>>(() => new Set());
  const [query, setQuery] = useState('');
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE);
  const deferredQuery = useDeferredValue(query);
  const oratorId = scope === 'selected' && selected ? selected.id : null;
  const visibleIds = useMemo(() => new Set(visibleOratorIds), [visibleOratorIds]);

  const filtered = useMemo(() => {
    const needle = deferredQuery.trim().toLowerCase();
    return words.filter((word) => {
      if (oratorId != null) {
        if (word.oratorId !== oratorId) return false;
      } else if (!visibleIds.has(word.oratorId)) {
        return false;
      }
      if (!matchesComplexity(word.complexity, complexity)) return false;
      if (!needle) return true;
      const oratorName = oratorById.get(word.oratorId)?.oratorName ?? '';
      return (
        word.word.toLowerCase().includes(needle) ||
        word.definition.toLowerCase().includes(needle) ||
        word.example.toLowerCase().includes(needle) ||
        oratorName.toLowerCase().includes(needle)
      );
    });
  }, [complexity, deferredQuery, oratorId, visibleIds]);

  const visible = filtered.slice(0, visibleCount);
  const wotdOrator = wordOfDay ? oratorById.get(wordOfDay.oratorId) : undefined;

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">Vocabulary</p>
        <h1>{selected && scope === 'selected' ? selected.name : 'Words'}</h1>
        <p className="lede">
          {selected && scope === 'selected'
            ? `Words gathered from ${selected.oratorName}.`
            : 'The full library, drawn from every orator dictionary.'}
        </p>
      </header>

      {wordOfDay ? (
        <article className="wotd">
          <p className="kicker">Word of the Day</p>
          <h2>
            <Link to={`/words/${wordOfDay.id}`}>{wordOfDay.word}</Link>
          </h2>
          <p>{wordOfDay.definition}</p>
          {wotdOrator ? <p className="byline">{wotdOrator.oratorName}</p> : null}
        </article>
      ) : null}

      <div className="toolbar">
        <label className="field grow" htmlFor="word-search">
          <span>Search words</span>
          <input
            id="word-search"
            type="search"
            value={query}
            placeholder="Headword, definition, or orator"
            onChange={(event) => {
              setQuery(event.target.value);
              setVisibleCount(PAGE_SIZE);
            }}
          />
        </label>
      </div>

      <div className="chip-row" role="group" aria-label="Library scope">
        <button
          type="button"
          className={scope === 'selected' && selected ? 'chip is-on' : 'chip'}
          aria-pressed={scope === 'selected' && Boolean(selected)}
          disabled={!selected}
          onClick={() => {
            setScope('selected');
            setVisibleCount(PAGE_SIZE);
          }}
        >
          {selected ? selected.name : 'Choose an orator'}
        </button>
        <button
          type="button"
          className={scope === 'all' || !selected ? 'chip is-on' : 'chip'}
          aria-pressed={scope === 'all' || !selected}
          onClick={() => {
            setScope('all');
            setVisibleCount(PAGE_SIZE);
          }}
        >
          All orators
        </button>
        {!selected ? (
          <Link to="/orators" className="chip">
            Browse dictionaries
          </Link>
        ) : null}
      </div>

      <div className="chip-row" role="group" aria-label="Complexity">
        <button
          type="button"
          className={complexity.size === 0 ? 'chip is-on' : 'chip'}
          aria-pressed={complexity.size === 0}
          onClick={() => {
            setComplexity(new Set());
            setVisibleCount(PAGE_SIZE);
          }}
        >
          Any level
        </button>
        {COMPLEXITY_TIERS.map((item) => (
          <button
            key={item}
            type="button"
            className={complexity.has(item) ? 'chip is-on' : 'chip'}
            aria-pressed={complexity.has(item)}
            onClick={() => {
              setComplexity((current) => toggleComplexityTier(current, item));
              setVisibleCount(PAGE_SIZE);
            }}
          >
            {themeLabel(item)}
          </button>
        ))}
      </div>

      <p className="result-count" aria-live="polite">
        {filtered.length === 1 ? '1 word' : `${filtered.length} words`}
      </p>

      {visible.length === 0 ? (
        <p className="empty-title">No words match that search.</p>
      ) : (
        <ul className="word-list">
          {visible.map((word) => (
            <li key={word.id}>
              <WordCard word={word} />
            </li>
          ))}
        </ul>
      )}

      {visibleCount < filtered.length ? (
        <button type="button" className="button" onClick={() => setVisibleCount((count) => count + PAGE_SIZE)}>
          Show more
        </button>
      ) : null}
    </div>
  );
}
