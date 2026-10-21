import { describe, expect, it } from 'vitest';
import type { Idiom } from '../types';
import { assembleIdiomChoice, buildIdiomOptions, idiomQuestion, shouldAskIdiom } from './idiomQuiz';

function idiom(id: number, meaning: string): Idiom {
  return {
    id,
    idiom: `idiom ${id}`,
    meaning,
    example: 'Example.',
    complexity: 'intermediate',
    origin: null,
    oratorId: null,
    quoteId: null,
  };
}

const rng = () => 0.1;

describe('idiom multiple choice', () => {
  it('asks what the idiom means and uses other meanings as distractors', () => {
    const idioms = [1, 2, 3, 4, 5, 6].map((id) => idiom(id, `meaning ${id}`));
    const correct = idioms[0];
    if (!correct) throw new Error('fixture');
    const options = buildIdiomOptions(correct, idioms, 4, rng);
    expect(options).toHaveLength(4);
    expect(options.filter((option) => option.id === correct.id)).toHaveLength(1);
    expect(new Set(options.map((option) => option.meaning.toLowerCase())).size).toBe(4);
    expect(options.every((option) => option.meaning !== option.idiom)).toBe(true);
    expect(idiomQuestion(correct.idiom)).toBe("What does 'idiom 1' mean?");
  });

  it('repeats a round when the random sequence repeats', () => {
    const idioms = [1, 2, 3, 4, 5].map((id) => idiom(id, `meaning ${id}`));
    const first = assembleIdiomChoice(idioms, 4, rng);
    const second = assembleIdiomChoice(idioms, 4, rng);
    expect(first?.prompt).toBe(second?.prompt);
    expect(first?.options.map((option) => option.id)).toEqual(second?.options.map((option) => option.id));
    expect(first?.options.some((option) => option.id === first.correct.id)).toBe(true);
  });

  it('refuses duplicate meanings and a pool that is too small', () => {
    const correct = idiom(1, 'to start');
    expect(
      buildIdiomOptions(
        correct,
        [correct, idiom(2, 'to start'), idiom(3, 'to wait'), idiom(4, 'to leave'), idiom(5, 'To Wait')],
        4,
        rng,
      ),
    ).toEqual([]);
    expect(assembleIdiomChoice([1, 2, 3].map((id) => idiom(id, `meaning ${id}`)), 4, rng)).toBeNull();
  });

  it('does not offer idioms outside library multiple choice', () => {
    expect(shouldAskIdiom(false, 20, 0)).toBe(false);
    expect(shouldAskIdiom(true, 3, 0)).toBe(false);
    expect(shouldAskIdiom(true, 20, 0.35)).toBe(false);
    expect(shouldAskIdiom(true, 20, 0)).toBe(true);
  });
});
