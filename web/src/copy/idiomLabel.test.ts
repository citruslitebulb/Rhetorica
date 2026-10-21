import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';

const uiFiles = [
  new URL('../pages/WordsPage.tsx', import.meta.url),
  new URL('../pages/IdiomPage.tsx', import.meta.url),
  new URL('../pages/ProfilePage.tsx', import.meta.url),
];

describe("Today's idiom label", () => {
  it("matches Today's word capitalization in the web UI", () => {
    for (const file of uiFiles) {
      const source = readFileSync(file, 'utf8');
      expect(source).toContain("Today&apos;s idiom");
      expect(source).not.toContain('Idiom of the Day');
    }
  });
});
