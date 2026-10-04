import { dictionaries as rawDictionaries, quotes as rawQuotes, speeches as rawSpeeches, words as rawWords } from 'virtual:rhetorica-seed';
import { catalogKind, normalizeTitle } from '../lib/format';
import type { CatalogKind, Dictionary, Quote, Speech, Word } from '../types';

const CATEGORY_ORDER = [
  'Ancient Classics',
  'Historical / Philosophical',
  'Founding Era',
  '19th Century Powerhouses',
  'Literary Classics',
  '20th Century Legends',
  'Modern / Contemporary',
  'Modern / Philosophical',
  'Fictional / Mythic',
  'Fictional / Modern Inspirational',
];

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function asString(value: unknown, fallback = ''): string {
  return typeof value === 'string' ? value : fallback;
}

function asStringList(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  return value.filter((item): item is string => typeof item === 'string');
}

function asNullableString(value: unknown): string | null {
  return typeof value === 'string' && value.length > 0 ? value : null;
}

function asNullableNumber(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}

function parseDictionary(value: unknown): Dictionary | null {
  if (!isRecord(value) || typeof value.id !== 'number' || typeof value.name !== 'string') return null;
  if (value.isActive === false) return null;
  return {
    id: value.id,
    name: value.name,
    description: asString(value.description),
    oratorName: asString(value.oratorName, value.name),
    wordCount: typeof value.wordCount === 'number' ? value.wordCount : 0,
    category: asString(value.category, 'Orators'),
    era: asString(value.era),
    bio: asString(value.bio),
    portraitUrl: asString(value.portraitUrl),
    primaryStyle: asString(value.primaryStyle),
    voiceStyle: asString(value.voiceStyle),
    colorAccent: typeof value.colorAccent === 'number' ? value.colorAccent : 0xd4af37,
    sampleSpeech: asString(value.sampleSpeech),
    tags: asStringList(value.tags),
    themeCategories: asStringList(value.themeCategories),
    isActive: true,
  };
}

function parseWord(value: unknown): Word | null {
  if (!isRecord(value)) return null;
  if (typeof value.id !== 'number' || typeof value.word !== 'string' || typeof value.definition !== 'string') {
    return null;
  }
  if (typeof value.oratorId !== 'number') return null;
  return {
    id: value.id,
    word: value.word,
    definition: value.definition,
    example: asString(value.example),
    partOfSpeech: asString(value.partOfSpeech),
    oratorId: value.oratorId,
    complexity: asString(value.complexity, 'intermediate'),
    source: asNullableString(value.source),
    speech: asNullableString(value.speech),
    categories: asStringList(value.categories),
    pronunciation: asNullableString(value.pronunciation),
  };
}

function parseQuote(value: unknown): Quote | null {
  if (!isRecord(value)) return null;
  if (typeof value.id !== 'number' || typeof value.oratorId !== 'number' || typeof value.text !== 'string') {
    return null;
  }
  return {
    id: value.id,
    oratorId: value.oratorId,
    text: value.text,
    source: asNullableString(value.source),
    speech: asNullableString(value.speech),
    year: asNullableNumber(value.year),
    context: asNullableString(value.context),
  };
}

function parseSpeech(value: unknown): Speech | null {
  if (!isRecord(value)) return null;
  if (typeof value.id !== 'number' || typeof value.oratorId !== 'number' || typeof value.title !== 'string') {
    return null;
  }
  if (typeof value.fullText !== 'string') return null;
  return {
    id: value.id,
    oratorId: value.oratorId,
    title: value.title,
    fullText: value.fullText,
    year: asNullableNumber(value.year),
    description: asNullableString(value.description),
  };
}

const categoryRank = new Map(CATEGORY_ORDER.map((category, index) => [category, index]));

function byCategoryThenName(a: Dictionary, b: Dictionary): number {
  const rankA = categoryRank.get(a.category) ?? 999;
  const rankB = categoryRank.get(b.category) ?? 999;
  if (rankA !== rankB) return rankA - rankB;
  return a.name.localeCompare(b.name);
}

const parsedDictionaries = (Array.isArray(rawDictionaries) ? rawDictionaries : [])
  .map(parseDictionary)
  .filter((row): row is Dictionary => row !== null)
  .sort(byCategoryThenName);

const dictionaryIds = new Set(parsedDictionaries.map((orator) => orator.id));

export const dictionaries: readonly Dictionary[] = parsedDictionaries;

export const words: readonly Word[] = (Array.isArray(rawWords) ? rawWords : [])
  .map(parseWord)
  .filter((row): row is Word => row !== null && dictionaryIds.has(row.oratorId));

export const quotes: readonly Quote[] = (Array.isArray(rawQuotes) ? rawQuotes : [])
  .map(parseQuote)
  .filter((row): row is Quote => row !== null && dictionaryIds.has(row.oratorId));

export const speeches: readonly Speech[] = (Array.isArray(rawSpeeches) ? rawSpeeches : [])
  .map(parseSpeech)
  .filter((row): row is Speech => row !== null && dictionaryIds.has(row.oratorId));

export const oratorById = new Map(dictionaries.map((orator) => [orator.id, orator]));
export const wordById = new Map(words.map((word) => [word.id, word]));
export const speechById = new Map(speeches.map((speech) => [speech.id, speech]));

function groupByOrator<T extends { oratorId: number }>(rows: readonly T[]): Map<number, T[]> {
  const grouped = new Map<number, T[]>();
  for (const row of rows) {
    const list = grouped.get(row.oratorId);
    if (list) list.push(row);
    else grouped.set(row.oratorId, [row]);
  }
  return grouped;
}

export const wordsByOrator = groupByOrator(words);
export const quotesByOrator = groupByOrator(quotes);
export const speechesByOrator = groupByOrator(speeches);

const speechByTitle = new Map<string, Speech>();
for (const speech of speeches) {
  speechByTitle.set(`${speech.oratorId}:${normalizeTitle(speech.title)}`, speech);
}

export function findSpeech(oratorId: number, title: string | null | undefined): Speech | undefined {
  if (!title) return undefined;
  return speechByTitle.get(`${oratorId}:${normalizeTitle(title)}`);
}

export function wordCountFor(oratorId: number): number {
  return wordsByOrator.get(oratorId)?.length ?? 0;
}

export const oratorsByCategory: readonly (readonly [string, readonly Dictionary[]])[] = (() => {
  const grouped = new Map<string, Dictionary[]>();
  for (const orator of dictionaries) {
    const list = grouped.get(orator.category);
    if (list) list.push(orator);
    else grouped.set(orator.category, [orator]);
  }
  return [...grouped.entries()];
})();

export function catalogCounts(): Record<CatalogKind | 'all', number> {
  const counts = { all: dictionaries.length, historical: 0, literary: 0, fictional: 0 };
  for (const orator of dictionaries) {
    counts[catalogKind(orator.category)] += 1;
  }
  return counts;
}
