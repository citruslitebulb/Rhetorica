import { Link, useParams } from 'react-router-dom';
import { oratorById, speechById } from '../data/catalog';
import { formatYear } from '../lib/format';
import { useDocumentTitle } from '../lib/useDocumentTitle';

function parseId(value: string | undefined): number | null {
  if (!value) return null;
  const id = Number(value);
  return Number.isInteger(id) ? id : null;
}

export function SpeechPage() {
  const params = useParams();
  const speech = speechById.get(parseId(params.speechId) ?? -1);
  const orator = speech ? oratorById.get(speech.oratorId) : undefined;
  useDocumentTitle(speech?.title ?? 'Speech');

  if (!speech) {
    return (
      <div className="page">
        <h1>Speech not found</h1>
        <p>That speech is not in the library.</p>
        <Link to="/speeches" className="button">
          Speeches
        </Link>
      </div>
    );
  }

  const paragraphs = speech.fullText.split(/\n\n+/).filter((paragraph) => paragraph.trim().length > 0);
  const year = formatYear(speech.year);

  return (
    <article className="page">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/speeches">Speeches</Link>
        {orator ? (
          <>
            <span aria-hidden="true"> / </span>
            <Link to={`/orators/${orator.id}`}>{orator.name}</Link>
          </>
        ) : null}
      </nav>
      <header className="page-header">
        <p className="kicker">{orator?.oratorName ?? 'Speech'}</p>
        <h1>{speech.title}</h1>
        <p className="meta">
          {year ?? 'Undated'}
          {speech.description ? ` · ${speech.description}` : ''}
        </p>
      </header>
      <div className="speech-body">
        {paragraphs.map((paragraph, index) => (
          <p key={index}>{paragraph}</p>
        ))}
      </div>
    </article>
  );
}
