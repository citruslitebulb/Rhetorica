export const VOICE_FAMILIES = ['classical', 'statesmen', 'justice', 'literary', 'technology', 'fictional'] as const;

export type VoiceFamily = (typeof VOICE_FAMILIES)[number];

export const DEFAULT_VOICE_FAMILIES: readonly VoiceFamily[] = ['classical'];

const LITERARY_TAGS = new Set(['poetry', 'drama']);

const JUSTICE_TAGS = new Set([
  'justice',
  'human rights',
  'abolition',
  'nonviolence',
  'civil rights',
  "women's rights",
  'suffrage',
  'reconciliation',
  'dissent',
]);

export type VoiceOrator = {
  id: number;
  name: string;
  category: string;
  tags: readonly string[];
  themeCategories: readonly string[];
};

export const VOICE_FAMILY_COPY: Record<VoiceFamily, { title: string; body: string }> = {
  classical: {
    title: 'Classical philosophers & orators',
    body: 'Ancient persuasion and Stoic reflection — Demosthenes, Cicero, Marcus Aurelius.',
  },
  statesmen: {
    title: 'Historical statesmen',
    body: 'Civic speech that shaped nations — Lincoln, Churchill, the founding generation.',
  },
  justice: {
    title: 'Justice & human rights',
    body: 'Dignity, rights, and moral courage — Douglass, King, Mandela.',
  },
  literary: {
    title: 'Literary voices',
    body: 'Language shaped for the page and the stage — Shakespeare, Angelou.',
  },
  technology: {
    title: 'Business & technology',
    body: 'The rhetoric of invention and product — Jobs, Gates, and Silicon Valley.',
  },
  fictional: {
    title: 'Fictional & film',
    body: 'Invented mentors and movie speeches — Yoda, Gandalf, Rocky. Off unless you want them.',
  },
};

export function voiceFamilyOf(orator: VoiceOrator): VoiceFamily {
  const category = orator.category.toLowerCase();
  const tags = new Set(orator.tags.map((tag) => tag.toLowerCase()));
  const themes = new Set(orator.themeCategories.map((theme) => theme.toLowerCase()));
  if (category.includes('fictional') || category.includes('mythic')) return 'fictional';
  if (category.includes('literary')) return 'literary';
  if (category.includes('ancient') || category.includes('philosophical')) return 'classical';
  if (themes.has('tech')) return 'technology';
  for (const tag of tags) {
    if (LITERARY_TAGS.has(tag)) return 'literary';
  }
  for (const tag of tags) {
    if (JUSTICE_TAGS.has(tag)) return 'justice';
  }
  if (tags.has('equality') && tags.has('law')) return 'justice';
  return 'statesmen';
}

export function filterByFamilies(orators: readonly VoiceOrator[], families: ReadonlySet<VoiceFamily>): VoiceOrator[] {
  if (families.size === 0) return [];
  return orators.filter((orator) => families.has(voiceFamilyOf(orator)));
}
