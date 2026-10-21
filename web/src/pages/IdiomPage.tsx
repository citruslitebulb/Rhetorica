import { Link, useParams } from 'react-router-dom';
import { idiomById, oratorById, quoteById } from '../data/catalog';
import { useDocumentTitle } from '../lib/useDocumentTitle';

export function IdiomPage() {
  const { idiomId } = useParams();
  const id = Number(idiomId);
  const idiom = Number.isInteger(id) ? idiomById.get(id) : undefined;
  useDocumentTitle(idiom ? idiom.idiom : 'Idiom');

  if (!idiom) {
    return (
      <div className="page">
        <h1>Idiom not found</h1>
        <p>That idiom is not in the library on this device.</p>
        <Link className="button" to="/words">
          Back to words
        </Link>
      </div>
    );
  }

  const quote = idiom.quoteId == null ? undefined : quoteById.get(idiom.quoteId);
  const linked = quote && idiom.oratorId != null && quote.oratorId === idiom.oratorId ? quote : undefined;
  const orator = idiom.oratorId == null ? undefined : oratorById.get(idiom.oratorId);

  return (
    <div className="page">
      <p className="kicker">Today&apos;s idiom</p>
      <h1>{idiom.idiom}</h1>
      <section className="panel">
        <h2>Meaning</h2>
        <p className="prose">{idiom.meaning}</p>
      </section>
      {idiom.origin ? (
        <section className="panel">
          <h2>Origin</h2>
          <p className="prose">{idiom.origin}</p>
        </section>
      ) : null}
      <section className="panel">
        <h2>Example</h2>
        <p className="prose">{idiom.example}</p>
      </section>
      {linked ? (
        <section className="panel">
          <h2>In their words</h2>
          <blockquote className="prose">{linked.text}</blockquote>
          <p className="byline">
            {orator ? <Link to={`/orators/${orator.id}`}>{orator.oratorName}</Link> : null}
            {linked.source ? <span>{orator ? ' · ' : ''}{linked.source}</span> : null}
          </p>
        </section>
      ) : null}
    </div>
  );
}
