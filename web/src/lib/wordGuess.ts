export type LetterMark = 'unused' | 'absent' | 'present' | 'correct';

const RANK: Record<LetterMark, number> = {
  unused: 0,
  absent: 1,
  present: 2,
  correct: 3,
};

export function normalizeGuess(raw: string): string {
  return [...raw].filter((character) => /\p{L}/u.test(character)).join('').toLowerCase();
}

export function evaluateGuess(guess: string, target: string): LetterMark[] {
  const normalizedGuess = normalizeGuess(guess);
  const normalizedTarget = normalizeGuess(target);
  if (normalizedGuess.length !== normalizedTarget.length) {
    throw new Error(`Guess length ${normalizedGuess.length} must match target length ${normalizedTarget.length}`);
  }
  return evaluateInternal(normalizedGuess, normalizedTarget);
}

export function evaluateGuessAllowingLengthMismatch(guess: string, target: string): LetterMark[] {
  const normalizedGuess = normalizeGuess(guess);
  const normalizedTarget = normalizeGuess(target);
  if (normalizedGuess.length === 0) return [];
  return evaluateInternal(normalizedGuess, normalizedTarget);
}

function evaluateInternal(guess: string, target: string): LetterMark[] {
  const result: LetterMark[] = Array.from({ length: guess.length }, () => 'absent');
  const remaining = new Map<string, number>();
  for (const character of target) {
    remaining.set(character, (remaining.get(character) ?? 0) + 1);
  }
  const guessChars = [...guess];
  const targetChars = [...target];
  for (let index = 0; index < guessChars.length; index += 1) {
    const character = guessChars[index];
    if (character != null && index < targetChars.length && character === targetChars[index]) {
      result[index] = 'correct';
      remaining.set(character, (remaining.get(character) ?? 0) - 1);
    }
  }
  for (let index = 0; index < guessChars.length; index += 1) {
    if (result[index] === 'correct') continue;
    const character = guessChars[index];
    if (character == null) continue;
    const count = remaining.get(character) ?? 0;
    if (count > 0) {
      result[index] = 'present';
      remaining.set(character, count - 1);
    }
  }
  return result;
}

export function mergeKeyboardState(
  current: ReadonlyMap<string, LetterMark>,
  guess: string,
  marks: readonly LetterMark[],
): Map<string, LetterMark> {
  const next = new Map(current);
  const characters = [...normalizeGuess(guess)];
  characters.forEach((character, index) => {
    const mark = marks[index];
    if (!mark) return;
    const existing = next.get(character);
    if (existing == null || RANK[mark] > RANK[existing]) next.set(character, mark);
  });
  return next;
}
