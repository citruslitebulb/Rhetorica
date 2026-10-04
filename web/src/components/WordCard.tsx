import { Link } from 'react-router-dom';
import { oratorById } from '../data/catalog';
import { themeLabel } from '../lib/format';
import type { Word } from '../types';
import { SaveButton } from './SaveButton';

export function WordCard({ word }: { word: Word }) {
  const orator = oratorById.get(word.oratorId);
  return (
    <article className="word-card">
      <Link to={`/words/${word.id}`} className="word-card-link">
        <h2>{word.word}</h2>
        <p className="meta">
          {themeLabel(word.partOfSpeech)}
          <span aria-hidden="true"> · </span>
          {themeLabel(word.complexity)}
        </p>
        <p className="definition">{word.definition}</p>
        {orator ? <p className="byline">{orator.oratorName}</p> : null}
      </Link>
      <SaveButton wordId={word.id} compact />
    </article>
  );
}
