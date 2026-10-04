import { Link } from 'react-router-dom';
import { OratorSelect } from '../components/OratorSelect';
import { findSpeech, oratorById, quotesByOrator } from '../data/catalog';
import { formatYear } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';
import type { Quote } from '../types';

function groupQuotes(rows: readonly Quote[]): { label: string; quotes: Quote[] }[] {
  const groups: { label: string; quotes: Quote[] }[] = [];
  for (const quote of rows) {
    const label = quote.speech || quote.source || 'Lines';
    const last = groups[groups.length - 1];
    if (last && last.label === label) last.quotes.push(quote);
    else groups.push({ label, quotes: [quote] });
  }
  return groups;
}

export function QuotesPage() {
  const { selectedOratorId, selectOrator, visibleOrators } = useLibrary();
  const orator = selectedOratorId == null ? undefined : oratorById.get(selectedOratorId);
  const selectable = orator && !visibleOrators.some((item) => item.id === orator.id) ? [...visibleOrators, orator] : visibleOrators;
  useDocumentTitle(orator ? `${orator.name} quotes` : 'Quotes');
  const rows = orator ? (quotesByOrator.get(orator.id) ?? []) : [];
  const groups = groupQuotes(rows);

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">Lines</p>
        <h1>{orator ? orator.name : 'Quotes'}</h1>
        <p className="lede">
          {orator ? `Passages kept for ${orator.oratorName}.` : 'Choose an orator to read their lines.'}
        </p>
      </header>
      <OratorSelect
        id="quote-orator"
        label="Orator"
        value={selectedOratorId}
        orators={selectable}
        onChange={selectOrator}
        emptyLabel="Choose an orator"
      />
      {!orator ? (
        <p className="empty-title">
          Open a dictionary from <Link to="/orators">Orators</Link>, or pick a name above.
        </p>
      ) : null}
      {orator && rows.length === 0 ? <p className="empty-title">No quotes are filed for this orator.</p> : null}
      {groups.map((group, index) => {
        const speech = orator ? findSpeech(orator.id, group.label) : undefined;
        return (
          <section key={`${group.label}-${index}`} className="quote-group">
            <h2>
              {speech ? <Link to={`/speeches/${speech.id}`}>{group.label}</Link> : group.label}
            </h2>
            <ul className="quote-list">
              {group.quotes.map((quote) => {
                const year = formatYear(quote.year);
                return (
                  <li key={quote.id}>
                    <blockquote className="quote-card">
                      <p>{quote.text}</p>
                      <footer>
                        {quote.source ?? orator?.oratorName}
                        {year ? ` · ${year}` : ''}
                      </footer>
                      {quote.context ? <p className="context">{quote.context}</p> : null}
                    </blockquote>
                  </li>
                );
              })}
            </ul>
          </section>
        );
      })}
    </div>
  );
}
