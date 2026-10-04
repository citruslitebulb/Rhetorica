import { describe, expect, it } from 'vitest';
import { evaluateGuess, evaluateGuessAllowingLengthMismatch, mergeKeyboardState, normalizeGuess } from './wordGuess';

describe('letter guess', () => {
  it('normalizes to letters', () => {
    expect(normalizeGuess('Eloquence!')).toBe('eloquence');
    expect(normalizeGuess('well-said')).toBe('wellsaid');
  });

  it('marks correct, present, and absent letters, including duplicates', () => {
    expect(evaluateGuess('apple', 'apple')).toEqual(['correct', 'correct', 'correct', 'correct', 'correct']);
    expect(evaluateGuess('zzzzz', 'apple')).toEqual(['absent', 'absent', 'absent', 'absent', 'absent']);
    expect(evaluateGuess('paper', 'apple')).toEqual(['present', 'present', 'correct', 'present', 'absent']);
    const marks = evaluateGuess('llxxx', 'apple');
    expect(marks.filter((mark) => mark === 'correct')).toHaveLength(0);
    expect(marks.filter((mark) => mark === 'present')).toHaveLength(1);
  });

  it('marks unequal lengths without calling a short guess a win', () => {
    expect(evaluateGuessAllowingLengthMismatch('apples', 'apple')).toEqual([
      'correct',
      'correct',
      'correct',
      'correct',
      'correct',
      'absent',
    ]);
    expect(evaluateGuessAllowingLengthMismatch('core', 'courage')).toEqual(['correct', 'correct', 'present', 'present']);
  });

  it('keeps the strongest keyboard mark', () => {
    const first = mergeKeyboardState(new Map(), 'pap', ['present', 'absent', 'correct']);
    const second = mergeKeyboardState(first, 'xap', ['absent', 'absent', 'present']);
    expect(second.get('p')).toBe('correct');
    expect(second.get('a')).toBe('absent');
  });
});
