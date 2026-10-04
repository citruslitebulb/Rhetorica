export type Dictionary = {
  id: number;
  name: string;
  description: string;
  oratorName: string;
  wordCount: number;
  category: string;
  era: string;
  bio: string;
  portraitUrl: string;
  primaryStyle: string;
  voiceStyle: string;
  colorAccent: number;
  sampleSpeech: string;
  tags: string[];
  themeCategories: string[];
  isActive: boolean;
};

export type Word = {
  id: number;
  word: string;
  definition: string;
  example: string;
  partOfSpeech: string;
  oratorId: number;
  complexity: string;
  source: string | null;
  speech: string | null;
  categories: string[];
  pronunciation: string | null;
};

export type Quote = {
  id: number;
  oratorId: number;
  text: string;
  source: string | null;
  speech: string | null;
  year: number | null;
  context: string | null;
};

export type Speech = {
  id: number;
  oratorId: number;
  title: string;
  fullText: string;
  year: number | null;
  description: string | null;
};

export type CatalogKind = 'historical' | 'literary' | 'fictional';

export type SavedRecord = {
  wordId: number;
  savedAt: number;
};

export type SavedSort = 'newest' | 'alphabetical' | 'partOfSpeech';
