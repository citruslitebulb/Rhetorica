/**
 * Letter-guess answers should be citation forms: "advertise", never "advertising"
 * or "advertised".
 *
 * Same rules as `LetterGuessLemma.kt`. A form is rejected when a suffix rule can
 * map it to a base, or when a superlative's base is already in [lexicon].
 * The exception list keeps roots that only look inflected ("bring", "need",
 * "bless", "kindly").
 */

const VOWELS = new Set('aeiou');
const IRREGULAR_IES = new Set(['series', 'species']);

/** Citation forms a suffix chop would mistake for an inflection. */
const BASE_FORMS = new Set([
  'bring',
  'king',
  'morning',
  'evening',
  'something',
  'nothing',
  'anything',
  'everything',
  'during',
  'ceiling',
  'offspring',
  'shortcoming',
  'need',
  'feed',
  'seed',
  'weed',
  'bleed',
  'speed',
  'greed',
  'creed',
  'breed',
  'exceed',
  'proceed',
  'succeed',
  'bless',
  'pass',
  'class',
  'glass',
  'canvas',
  'means',
  'series',
  'species',
  'kindly',
  'only',
  'early',
  'holy',
  'ugly',
  'jolly',
  'silly',
  'worldly',
  'sacred',
  'naked',
  'wicked',
  'rugged',
  'hundred',
]);

type PosKind = 'adverb' | 'adjective' | 'verb' | 'noun' | 'other';

export function headwordKey(word: string): string {
  return word.toLowerCase().replace(/[^a-z]/g, '');
}

/**
 * [lexicon] holds [headwordKey] values. It only affects superlatives such as
 * "chiefest" when "chief" is in the library.
 */
export function isLetterGuessBaseForm(
  word: string,
  partOfSpeech: string,
  lexicon: ReadonlySet<string> = new Set(),
): boolean {
  const normalized = headwordKey(word);
  if (normalized.length === 0) return false;
  if (BASE_FORMS.has(normalized)) return true;
  const kind = posKind(partOfSpeech);
  if (isDerivedAdverb(normalized, kind)) return false;
  if (isIngForm(normalized, kind)) return false;
  if (isEdForm(normalized, kind)) return false;
  if (isPluralOrThirdPerson(normalized, kind)) return false;
  if (isSuperlative(normalized, kind, lexicon)) return false;
  return true;
}

function posKind(partOfSpeech: string): PosKind {
  const pos = partOfSpeech.toLowerCase();
  if (pos.includes('adverb')) return 'adverb';
  if (pos.includes('adjective')) return 'adjective';
  if (pos.includes('verb')) return 'verb';
  if (pos.includes('noun')) return 'noun';
  return 'other';
}

function hasVowel(value: string): boolean {
  return [...value].some((character) => VOWELS.has(character));
}

function isDoubledConsonant(stem: string): boolean {
  if (stem.length < 3) return false;
  const last = stem.at(-1);
  return last != null && last === stem.at(-2) && !VOWELS.has(last);
}

function endsWithSingleConsonant(stem: string): boolean {
  if (stem.length < 2 || stem.endsWith('ss')) return false;
  const previous = stem.at(-2);
  const last = stem.at(-1);
  return previous != null && last != null && VOWELS.has(previous) && !VOWELS.has(last);
}

function isDerivedAdverb(word: string, kind: PosKind): boolean {
  return kind === 'adverb' && word.endsWith('ly') && word.length >= 5;
}

function isIngForm(word: string, kind: PosKind): boolean {
  if (!word.endsWith('ing') || word.length < 5) return false;
  const stem = word.slice(0, -3);
  if (!hasVowel(stem)) return false;
  if (kind === 'verb') return true;
  if (kind === 'adjective') return stem.length >= 2;
  if (kind === 'noun' || kind === 'other') return isGerundStem(stem);
  return false;
}

function isGerundStem(stem: string): boolean {
  if (isDoubledConsonant(stem)) return true;
  return stem.length >= 3 && endsWithSingleConsonant(stem);
}

function isEdForm(word: string, kind: PosKind): boolean {
  if (!word.endsWith('ed')) return false;
  const stem = word.slice(0, -2);
  if (stem.length < 2 || !hasVowel(stem)) return false;
  const plusE = `${stem}e`;
  if (word.endsWith('ied') || isDoubledConsonant(stem) || isVerbishBase(plusE)) return true;
  if (isMonosyllabicEDrop(plusE)) return true;
  if (kind === 'verb' && stem.length >= 3) return true;
  if (kind === 'adjective' && stem.length >= 4) return true;
  return false;
}

function isVerbishBase(base: string): boolean {
  return (
    base.endsWith('ise') ||
    base.endsWith('ize') ||
    base.endsWith('yze') ||
    base.endsWith('yse') ||
    base.endsWith('ate') ||
    base.endsWith('ify') ||
    base.endsWith('ite') ||
    base.endsWith('ute')
  );
}

function isMonosyllabicEDrop(plusE: string): boolean {
  if (plusE.length < 3 || !plusE.endsWith('e')) return false;
  const stem = plusE.slice(0, -1);
  const vowels = [...stem].filter((character) => VOWELS.has(character));
  if (vowels.length !== 1) return false;
  const vowel = plusE.at(-3);
  const consonant = plusE.at(-2);
  return vowel != null && consonant != null && VOWELS.has(vowel) && !VOWELS.has(consonant);
}

function isPluralOrThirdPerson(word: string, kind: PosKind): boolean {
  if (kind !== 'noun' && kind !== 'verb') return false;
  if (word.endsWith('ies') && !IRREGULAR_IES.has(word) && word.length >= 5) return true;
  if (word.length >= 5 && (word.endsWith('xes') || word.endsWith('zes') || word.endsWith('ches') || word.endsWith('shes'))) {
    return true;
  }
  if (word.length >= 5 && word.endsWith('ses') && !word.endsWith('sses')) return true;
  if (!word.endsWith('s') || isUninflectedSEnding(word)) return false;
  const singular = word.slice(0, -1);
  const minSingular = kind === 'verb' ? 3 : 4;
  if (singular.length < minSingular || !hasVowel(singular)) return false;
  const last = singular.at(-1);
  const singularLooksReal = (last != null && !VOWELS.has(last)) || singular.endsWith('e');
  return singularLooksReal;
}

function isUninflectedSEnding(word: string): boolean {
  return (
    word.endsWith('ss') ||
    word.endsWith('ous') ||
    word.endsWith('us') ||
    word.endsWith('is') ||
    word.endsWith('os') ||
    word.endsWith('as') ||
    word.endsWith('ics') ||
    word.endsWith('ness')
  );
}

function isSuperlative(word: string, kind: PosKind, lexicon: ReadonlySet<string>): boolean {
  if (kind !== 'adjective' || lexicon.size === 0) return false;
  if (!word.endsWith('est') || word.length < 6) return false;
  const stem = word.slice(0, -3);
  const candidates = [stem, `${stem}e`];
  if (isDoubledConsonant(stem)) candidates.push(stem.slice(0, -1));
  if (word.endsWith('iest') && word.length > 4) candidates.push(`${word.slice(0, -4)}y`);
  return candidates.some((candidate) => candidate.length >= 4 && candidate !== word && lexicon.has(candidate));
}
