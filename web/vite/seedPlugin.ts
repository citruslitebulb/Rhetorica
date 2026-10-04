import fs from 'node:fs';
import path from 'node:path';
import type { Plugin } from 'vite';

const VIRTUAL_ID = 'virtual:rhetorica-seed';
const RESOLVED_ID = `\0${VIRTUAL_ID}`;

function readJson(filePath: string): unknown {
  return JSON.parse(fs.readFileSync(filePath, 'utf8')) as unknown;
}

/**
 * Inlines the Android seed JSON at dev/build time.
 * The files under app/src/main/assets/data/seed stay the source of truth.
 */
export function rhetoricaSeedPlugin(seedDir: string): Plugin {
  return {
    name: 'rhetorica-seed',
    resolveId(id) {
      if (id === VIRTUAL_ID) return RESOLVED_ID;
      return null;
    },
    load(id) {
      if (id !== RESOLVED_ID) return null;
      if (!fs.existsSync(seedDir)) {
        this.error(`Rhetorica seed directory not found: ${seedDir}`);
      }

      const dictionariesPath = path.join(seedDir, 'dictionaries.json');
      const speechesPath = path.join(seedDir, 'speeches.json');
      this.addWatchFile(dictionariesPath);
      this.addWatchFile(speechesPath);

      const words: unknown[] = [];
      const quotes: unknown[] = [];
      const fileNames = fs.readdirSync(seedDir).sort();
      for (const name of fileNames) {
        const filePath = path.join(seedDir, name);
        if (name.startsWith('words_') && name.endsWith('.json')) {
          this.addWatchFile(filePath);
          const rows = readJson(filePath);
          if (Array.isArray(rows)) words.push(...rows);
        } else if (name.startsWith('quotes_') && name.endsWith('.json')) {
          this.addWatchFile(filePath);
          const rows = readJson(filePath);
          if (Array.isArray(rows)) quotes.push(...rows);
        }
      }

      const payload = {
        dictionaries: readJson(dictionariesPath),
        words,
        quotes,
        speeches: readJson(speechesPath),
      };

      return `export const dictionaries = ${JSON.stringify(payload.dictionaries)};
export const words = ${JSON.stringify(payload.words)};
export const quotes = ${JSON.stringify(payload.quotes)};
export const speeches = ${JSON.stringify(payload.speeches)};
`;
    },
  };
}

/** Ships the existing OFL text next to the built font files. */
export function playfairLicensePlugin(licensePath: string, outDir: string): Plugin {
  return {
    name: 'playfair-license',
    apply: 'build',
    closeBundle() {
      if (!fs.existsSync(licensePath)) return;
      const destDir = path.join(outDir, 'licenses');
      fs.mkdirSync(destDir, { recursive: true });
      fs.copyFileSync(licensePath, path.join(destDir, 'PlayfairDisplay-OFL.txt'));
    },
  };
}
