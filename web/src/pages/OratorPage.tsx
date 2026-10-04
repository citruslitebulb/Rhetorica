import { useEffect, type CSSProperties } from 'react';
import { Link, useParams } from 'react-router-dom';
import { oratorById, quotesByOrator, wordCountFor } from '../data/catalog';
import { accentColor, themeLabel } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';
import { Monogram } from '../components/Monogram';

function parseId(value: string | undefined): number | null {
  if (!value) return null;
  const id = Number(value);
  return Number.isInteger(id) ? id : null;
}

export function OratorPage() {
  const params = useParams();
  const orator = oratorById.get(parseId(params.oratorId) ?? -1);
  const { selectOrator } = useLibrary();
  useDocumentTitle(orator?.name ?? 'Orator');

  useEffect(() => {
    if (orator) selectOrator(orator.id);
  }, [orator, selectOrator]);

  if (!orator) {
    return (
      <div className="page">
        <h1>Orator not found</h1>
        <p>That dictionary is not in the library.</p>
        <Link to="/orators" className="button">
          All orators
        </Link>
      </div>
    );
  }

  const quoteCount = quotesByOrator.get(orator.id)?.length ?? 0;

  return (
    <div className="page">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/orators">Orators</Link>
        <span aria-hidden="true"> / </span>
        <span>{orator.category}</span>
      </nav>
      <header className="orator-hero" style={{ '--accent': accentColor(orator.colorAccent) } as CSSProperties}>
        <Monogram name={orator.oratorName} />
        <div>
          <p className="kicker">{orator.category}</p>
          <h1>{orator.name}</h1>
          <p className="meta">{orator.era}</p>
        </div>
      </header>
      <p className="lede">{orator.description}</p>
      {orator.bio ? <p className="prose">{orator.bio}</p> : null}
      <dl className="fact-list">
        {orator.primaryStyle ? (
          <>
            <dt>Style</dt>
            <dd>{orator.primaryStyle}</dd>
          </>
        ) : null}
        {orator.voiceStyle ? (
          <>
            <dt>Voice</dt>
            <dd>{orator.voiceStyle}</dd>
          </>
        ) : null}
      </dl>
      {orator.sampleSpeech ? (
        <blockquote className="pull-quote">
          <p>{orator.sampleSpeech}</p>
          <footer>{orator.oratorName}</footer>
        </blockquote>
      ) : null}
      {orator.tags.length > 0 ? (
        <ul className="tag-list">
          {orator.tags.map((tag) => (
            <li key={tag}>{tag}</li>
          ))}
        </ul>
      ) : null}
      {orator.themeCategories.length > 0 ? (
        <ul className="tag-list">
          {orator.themeCategories.map((theme) => (
            <li key={theme}>{themeLabel(theme)}</li>
          ))}
        </ul>
      ) : null}
      <div className="action-row">
        <Link to="/words" className="button primary">
          {wordCountFor(orator.id)} words
        </Link>
        <Link to="/quotes" className="button">
          {quoteCount} quotes
        </Link>
      </div>
    </div>
  );
}
