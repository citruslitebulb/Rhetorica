import { useMemo, useState, type CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import { wordCountFor } from '../data/catalog';
import { accentColor, catalogKind } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';
import type { CatalogKind } from '../types';
import { Monogram } from '../components/Monogram';

const KINDS: { id: CatalogKind | 'all'; label: string }[] = [
  { id: 'all', label: 'All' },
  { id: 'historical', label: 'Historical' },
  { id: 'literary', label: 'Literary' },
  { id: 'fictional', label: 'Fictional' },
];

export function OratorsPage() {
  useDocumentTitle('Orators');
  const { visibleOrators, preferences } = useLibrary();
  const [query, setQuery] = useState('');
  const [kind, setKind] = useState<CatalogKind | 'all'>('all');
  const counts = useMemo(() => {
    const tally = { all: visibleOrators.length, historical: 0, literary: 0, fictional: 0 };
    for (const orator of visibleOrators) tally[catalogKind(orator.category)] += 1;
    return tally;
  }, [visibleOrators]);

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return visibleOrators.filter((orator) => {
      if (kind !== 'all' && catalogKind(orator.category) !== kind) return false;
      if (!needle) return true;
      const haystack = [orator.name, orator.oratorName, orator.era, orator.description, orator.category, ...orator.tags]
        .join(' ')
        .toLowerCase();
      return haystack.includes(needle);
    });
  }, [kind, query, visibleOrators]);

  const groups = useMemo(() => {
    const grouped = new Map<string, typeof filtered>();
    for (const orator of filtered) {
      const list = grouped.get(orator.category);
      if (list) list.push(orator);
      else grouped.set(orator.category, [orator]);
    }
    return [...grouped.entries()];
  }, [filtered]);

  return (
    <div className="page page-wide">
      <header className="page-header">
        <p className="kicker">Dictionaries</p>
        <h1>Orators</h1>
        <p className="lede">
          Choose a voice. Their words, lines, and speeches stay on this device.{' '}
          {preferences.includeLiterary && preferences.includeFictional ? null : (
            <>
              {preferences.includeLiterary ? '' : 'Literary voices are hidden. '}
              {preferences.includeFictional ? '' : 'Fictional voices are hidden. '}
              <Link to="/profile">Change this in Profile</Link>.
            </>
          )}
        </p>
      </header>

      <div className="toolbar">
        <label className="field grow" htmlFor="orator-search">
          <span>Search orators</span>
          <input
            id="orator-search"
            type="search"
            value={query}
            placeholder="Name, era, or theme"
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
      </div>

      <div className="chip-row" role="group" aria-label="Catalog">
        {KINDS.map((item) => (
          <button
            key={item.id}
            type="button"
            className={kind === item.id ? 'chip is-on' : 'chip'}
            aria-pressed={kind === item.id}
            onClick={() => setKind(item.id)}
          >
            {item.label}
            <span className="chip-count">{counts[item.id]}</span>
          </button>
        ))}
      </div>

      {groups.length === 0 ? (
        <p className="empty-title">No orators match that search.</p>
      ) : (
        groups.map(([category, orators]) => (
          <section key={category} className="catalog-section">
            <h2>{category}</h2>
            <ul className="orator-grid">
              {orators.map((orator) => (
                <li key={orator.id}>
                  <Link
                    to={`/orators/${orator.id}`}
                    className="orator-card"
                    style={{ '--accent': accentColor(orator.colorAccent) } as CSSProperties}
                  >
                    <Monogram name={orator.oratorName} />
                    <div>
                      <h3>{orator.name}</h3>
                      <p className="meta">{orator.era}</p>
                      <p className="description">{orator.description}</p>
                      <p className="byline">{wordCountFor(orator.id)} words</p>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        ))
      )}
    </div>
  );
}
