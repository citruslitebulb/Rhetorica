import { describe, expect, it } from 'vitest';
import { catalogKind, dayOfYear, formatYear, monogram, themeLabel } from './format';

describe('format helpers', () => {
  it('classifies orator categories the way the Android catalog does', () => {
    expect(catalogKind('Ancient Classics')).toBe('historical');
    expect(catalogKind('Literary Classics')).toBe('literary');
    expect(catalogKind('Fictional / Mythic')).toBe('fictional');
    expect(catalogKind('Fictional / Modern Inspirational')).toBe('fictional');
  });

  it('builds monograms from orator names', () => {
    expect(monogram('Martin Luther King Jr.')).toBe('MLK');
    expect(monogram('Abraham Lincoln')).toBe('AL');
    expect(monogram('Demosthenes')).toBe('DE');
    expect(monogram('Mr. Miyagi')).toBe('MI');
    expect(monogram(null)).toBe('?');
  });

  it('labels known themes and years', () => {
    expect(themeLabel('courage')).toBe('Courage');
    expect(themeLabel('custom theme')).toBe('Custom Theme');
    expect(formatYear(-351)).toBe('351 BC');
    expect(formatYear(1963)).toBe('1963');
    expect(formatYear(null)).toBeNull();
  });

  it('counts January 1 as day 1', () => {
    expect(dayOfYear(new Date(2026, 0, 1))).toBe(1);
    expect(dayOfYear(new Date(2026, 0, 2))).toBe(2);
  });
});
