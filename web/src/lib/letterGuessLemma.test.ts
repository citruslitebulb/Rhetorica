import { describe, expect, it } from 'vitest';
import { words } from '../data/catalog';
import { matchesComplexity, type ComplexityTier } from './complexity';
import { headwordKey, isLetterGuessBaseForm } from './letterGuessLemma';
import { letterCount, WORD_GUESS_DIFFICULTIES, type WordGuessDifficulty } from './quizRound';

describe('letter-guess base forms', () => {
  it('keeps advertise and drops inflections, including false-positive traps', () => {
    expect(isLetterGuessBaseForm('advertise', 'verb')).toBe(true);
    expect(isLetterGuessBaseForm('advertising', 'noun')).toBe(false);
    expect(isLetterGuessBaseForm('advertised', 'verb')).toBe(false);
    expect(isLetterGuessBaseForm('advertised', 'adjective')).toBe(false);
    expect(isLetterGuessBaseForm('self-limiting', 'adjective')).toBe(false);
    expect(isLetterGuessBaseForm('used', 'verb')).toBe(false);

    expect(isLetterGuessBaseForm('bring', 'verb')).toBe(true);
    expect(isLetterGuessBaseForm('need', 'verb')).toBe(true);
    expect(isLetterGuessBaseForm('need', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('bless', 'verb')).toBe(true);
    expect(isLetterGuessBaseForm('kindly', 'adverb')).toBe(true);
    expect(isLetterGuessBaseForm('kindly', 'adjective')).toBe(true);
    expect(isLetterGuessBaseForm('king', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('morning', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('sacred', 'adjective')).toBe(true);
    expect(isLetterGuessBaseForm('canvas', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('focus', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('creed', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('means', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('physics', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('economics', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('shortcoming', 'noun')).toBe(true);
    expect(isLetterGuessBaseForm('notwithstanding', 'adverb')).toBe(true);
    expect(isLetterGuessBaseForm('worldly', 'adjective')).toBe(true);
    expect(isLetterGuessBaseForm('pass', 'verb')).toBe(true);
    expect(isLetterGuessBaseForm('oppress', 'verb')).toBe(true);

    expect(isLetterGuessBaseForm('ancestors', 'noun')).toBe(false);
    expect(isLetterGuessBaseForm('impulses', 'noun')).toBe(false);
    expect(isLetterGuessBaseForm('inequities', 'noun')).toBe(false);
    expect(isLetterGuessBaseForm('multitudes', 'noun')).toBe(false);
    expect(isLetterGuessBaseForm('courageously', 'adverb')).toBe(false);
    expect(isLetterGuessBaseForm('outstanding', 'adjective')).toBe(false);
    expect(isLetterGuessBaseForm('chiefest', 'adjective')).toBe(true);
    expect(isLetterGuessBaseForm('chiefest', 'adjective', new Set(['chief']))).toBe(false);
    expect(isLetterGuessBaseForm('protest', 'noun', new Set(['prot']))).toBe(true);
    expect(isLetterGuessBaseForm('honest', 'adjective', new Set(['hon']))).toBe(true);
  });

  it('keeps a playable base-form pool in every difficulty and complexity band', () => {
    const lexicon = new Set(words.map((word) => headwordKey(word.word)));
    const tiers: Array<readonly [string, ReadonlySet<ComplexityTier>]> = [
      ['all', new Set()],
      ['basic', new Set(['basic'])],
      ['intermediate', new Set(['intermediate'])],
      ['advanced', new Set(['advanced'])],
    ];
    const floors: Record<WordGuessDifficulty, Record<string, number>> = {
      easy: { all: 150, basic: 40, intermediate: 50, advanced: 8 },
      medium: { all: 200, basic: 40, intermediate: 80, advanced: 20 },
      hard: { all: 600, basic: 20, intermediate: 300, advanced: 150 },
      hardcore: { all: 1000, basic: 80, intermediate: 500, advanced: 250 },
    };
    const lines: string[] = [];
    for (const difficulty of Object.keys(WORD_GUESS_DIFFICULTIES) as WordGuessDifficulty[]) {
      const band = WORD_GUESS_DIFFICULTIES[difficulty];
      for (const [tierName, tier] of tiers) {
        const inTier = words.filter((word) => matchesComplexity(word.complexity, tier));
        const before = inTier.filter((word) => {
          const count = letterCount(word.word);
          return count >= band.minLetters && count <= band.maxLetters;
        });
        const after = before.filter((word) => isLetterGuessBaseForm(word.word, word.partOfSpeech, lexicon));
        lines.push(`${difficulty} ${tierName} ${before.length} -> ${after.length}`);
        const floor = floors[difficulty][tierName] ?? 0;
        expect(after.length, `${difficulty} ${tierName} before ${before.length}`).toBeGreaterThanOrEqual(floor);
        expect(after.length).toBeLessThanOrEqual(before.length);
      }
    }
    expect(lines).toHaveLength(16);
    expect(words.some((word) => headwordKey(word.word) === 'advertising')).toBe(true);
    expect(
      words
        .filter((word) => headwordKey(word.word) === 'advertising')
        .every((word) => !isLetterGuessBaseForm(word.word, word.partOfSpeech, lexicon)),
    ).toBe(true);
  });
});
