import { describe, expect, it } from 'vitest';
import { dictionaries, findSpeech, oratorById, quotes, speeches, wordById, words } from './catalog';

describe('seed catalog', () => {
  it('loads the bundled dictionaries, words, quotes, and speeches', () => {
    expect(dictionaries.length).toBeGreaterThan(20);
    expect(words.length).toBeGreaterThan(500);
    expect(quotes.length).toBeGreaterThan(200);
    expect(speeches.length).toBeGreaterThan(10);
  });

  it('keeps a known seed word instead of inventing copy', () => {
    const tyranny = wordById.get(40001);
    expect(tyranny?.word).toBe('tyranny');
    expect(tyranny?.definition).toBe('Cruel and oppressive government or rule');
    expect(oratorById.get(tyranny?.oratorId ?? -1)?.name).toBe('Demosthenes');
  });

  it('joins every word and quote to a real orator', () => {
    const ids = new Set(words.map((word) => word.id));
    expect(ids.size).toBe(words.length);
    for (const word of words) {
      expect(oratorById.has(word.oratorId)).toBe(true);
    }
    for (const quote of quotes) {
      expect(oratorById.has(quote.oratorId)).toBe(true);
      expect(quote.text.length).toBeGreaterThan(0);
    }
  });

  it('links a seed speech title back to the full text', () => {
    const speech = findSpeech(5, 'Gettysburg Address');
    expect(speech?.title).toBe('Gettysburg Address');
    expect(speech?.fullText.startsWith('Four score and seven years ago')).toBe(true);
  });
});
