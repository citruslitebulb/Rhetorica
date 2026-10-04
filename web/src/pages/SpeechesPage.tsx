import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { oratorById, speeches } from '../data/catalog';
import { formatYear } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';

export function SpeechesPage() {
  useDocumentTitle('Speeches');
  const [query, setQuery] = useState('');

  const rows = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return speeches
      .filter((speech) => {
        if (!needle) return true;
        const orator = oratorById.get(speech.oratorId);
        const haystack = [speech.title, speech.description ?? '', orator?.name ?? '', orator?.oratorName ?? '']
          .join(' ')
          .toLowerCase();
        return haystack.includes(needle);
      })
      .slice()
      .sort((a, b) => a.oratorId - b.oratorId || a.id - b.id);
  }, [query]);

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">Full texts</p>
        <h1>Speeches</h1>
        <p className="lede">The speeches behind the words, kept on this device.</p>
      </header>
      <label className="field" htmlFor="speech-search">
        <span>Search speeches</span>
        <input
          id="speech-search"
          type="search"
          value={query}
          placeholder="Title or orator"
          onChange={(event) => setQuery(event.target.value)}
        />
      </label>
      {rows.length === 0 ? (
        <div className="empty">
          <p className="empty-title">No speeches match that search.</p>
          <p>Try another title, or clear the search to see the library.</p>
        </div>
      ) : (
        <ul className="speech-list">
          {rows.map((speech) => {
            const orator = oratorById.get(speech.oratorId);
            const year = formatYear(speech.year);
            return (
              <li key={speech.id}>
                <Link to={`/speeches/${speech.id}`} className="speech-card">
                  <h2>{speech.title}</h2>
                  {orator ? <p className="byline">{orator.oratorName}</p> : null}
                  <p className="meta">
                    {year ?? 'Undated'}
                    {speech.description ? ` · ${speech.description}` : ''}
                  </p>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
