import type { Rng } from './quizRound';
import { shuffle } from './quizRound';
import type { Idiom } from '../types';

export const IDIOM_MIN_OPTIONS = 4;
const ASK_BELOW = 0.35;

/** Saved-word rounds and letter guess never ask an idiom question. */
export function shouldAskIdiom(libraryPool: boolean, idiomCount: number, roll: number): boolean {
  return libraryPool && idiomCount >= IDIOM_MIN_OPTIONS && roll < ASK_BELOW;
}

export function idiomQuestion(idiom: string): string {
  return `What does '${idiom}' mean?`;
}

function normalize(meaning: string): string {
  return meaning.trim().toLowerCase();
}

export function buildIdiomOptions(
  correct: Idiom,
  candidates: readonly Idiom[],
  optionCount: number,
  rng: Rng,
): Idiom[] {
  if (optionCount < 2) return [];
  const used = new Set<string>([normalize(correct.meaning)]);
  const distractors = shuffle(
    candidates.filter((idiom) => idiom.id !== correct.id),
    rng,
  ).filter((idiom) => {
    const meaning = normalize(idiom.meaning);
    if (used.has(meaning)) return false;
    used.add(meaning);
    return true;
  });
  const chosen = distractors.slice(0, optionCount - 1);
  if (chosen.length < optionCount - 1) return [];
  return shuffle([...chosen, correct], rng);
}

export type IdiomChoice = {
  correct: Idiom;
  options: Idiom[];
  prompt: string;
};

export function assembleIdiomChoice(
  idioms: readonly Idiom[],
  optionCount: number,
  rng: Rng,
): IdiomChoice | null {
  if (idioms.length < optionCount) return null;
  const index = Math.min(idioms.length - 1, Math.floor(rng() * idioms.length));
  const correct = idioms[index];
  if (!correct) return null;
  const options = buildIdiomOptions(correct, idioms, optionCount, rng);
  if (options.length < optionCount) return null;
  return { correct, options, prompt: idiomQuestion(correct.idiom) };
}
