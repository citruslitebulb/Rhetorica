import { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { SaveButton } from '../components/SaveButton';
import { findSpeech, oratorById, wordById } from '../data/catalog';
import { formatYear, themeLabel } from '../lib/format';
import { speakWordAndDefinition } from '../lib/reminder';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';

function parseId(value: string | undefined): number | null {
  if (!value) return null;
  const id = Number(value);
  return Number.isInteger(id) ? id : null;
}

export function WordPage() {
  const params = useParams();
  const word = wordById.get(parseId(params.wordId) ?? -1);
  const { recordWordViewed } = useLibrary();
  useDocumentTitle(word?.word ?? 'Word');

  useEffect(() => {
    if (word) recordWordViewed(word.id);
  }, [recordWordViewed, word]);

  if (!word) {
    return (
      <div className="page">
        <h1>Word not found</h1>
        <p>We could not find that word.</p>
        <Link to="/words" className="button">
          Back to words
        </Link>
      </div>
    );
  }

  const orator = oratorById.get(word.oratorId);
  const speech = findSpeech(word.oratorId, word.speech) ?? findSpeech(word.oratorId, word.source);
  const year = speech ? formatYear(speech.year) : null;

  return (
    <div className="page">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/words">Words</Link>
        {orator ? (
          <>
            <span aria-hidden="true"> / </span>
            <Link to={`/orators/${orator.id}`}>{orator.name}</Link>
          </>
        ) : null}
      </nav>
      <header className="word-hero">
        <p className="kicker">{orator?.oratorName ?? 'Rhetorica'}</p>
        <h1>{word.word}</h1>
        <p className="meta">
          {themeLabel(word.partOfSpeech)}
          <span aria-hidden="true"> · </span>
          {themeLabel(word.complexity)}
          {word.pronunciation ? (
            <>
              <span aria-hidden="true"> · </span>
              <span className="pronunciation">{word.pronunciation}</span>
            </>
          ) : null}
        </p>
        <div className="action-row">
          <SaveButton wordId={word.id} />
          <button type="button" className="button" onClick={() => speakWordAndDefinition(word.word, word.definition)}>
            Pronounce word
          </button>
        </div>
      </header>
      <section>
        <h2>Definition</h2>
        <p className="prose">{word.definition}</p>
      </section>
      {word.example ? (
        <section>
          <h2>Example</h2>
          <blockquote className="pull-quote">
            <p>{word.example}</p>
          </blockquote>
        </section>
      ) : null}
      {word.source || word.speech ? (
        <section>
          <h2>Source</h2>
          <p className="prose">
            {word.speech ? <span>{word.speech}</span> : null}
            {word.speech && word.source ? <span aria-hidden="true"> · </span> : null}
            {word.source ? <span>{word.source}</span> : null}
          </p>
          {speech ? (
            <p>
              <Link to={`/speeches/${speech.id}`} className="text-link">
                Read {speech.title}
                {year ? ` (${year})` : ''}
              </Link>
            </p>
          ) : null}
        </section>
      ) : null}
      {word.categories.length > 0 ? (
        <ul className="tag-list">
          {word.categories.map((category) => (
            <li key={category}>{themeLabel(category)}</li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
