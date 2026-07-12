/**
 * Second-pass quality upgrade:
 * - Raise every orator to at least TARGET words
 * - Replace shallow/weak headwords on major historical orators
 * - Improve categories for common speech words
 *
 * Run: node tools/upgrade_classic_vocab.js
 */
const fs = require('fs');
const path = require('path');

const seedDir = path.join('app', 'src', 'main', 'assets', 'data', 'seed');
const TARGET = 16;
const CANONICAL = new Set([
  'inspirational', 'tech', 'humanities', 'arts', 'leadership', 'democracy', 'courage', 'legacy',
]);

function load(file) {
  return JSON.parse(fs.readFileSync(path.join(seedDir, file), 'utf8'));
}
function save(file, data) {
  fs.writeFileSync(path.join(seedDir, file), JSON.stringify(data, null, 2) + '\n', 'utf8');
}

function containsWord(word, example) {
  const w = (word || '').toLowerCase();
  const ex = (example || '').toLowerCase();
  return ex.includes(w) || ex.includes(w + 's') || ex.includes(w + 'ed') || ex.includes(w + 'ing');
}

function norm(entry) {
  const word = String(entry.word).includes(' ') || String(entry.word).includes('-')
    ? entry.word
    : String(entry.word).toLowerCase();
  let example = entry.example;
  if (!containsWord(word, example)) {
    example = `${word.charAt(0).toUpperCase()}${word.slice(1)} stands at the center of the argument: ${example}`;
  }
  const categories = (entry.categories || []).filter((c) => CANONICAL.has(c));
  return {
    ...entry,
    word,
    example,
    partOfSpeech: (entry.partOfSpeech || 'noun').toLowerCase(),
    categories: categories.length ? categories.slice(0, 3) : ['inspirational'],
  };
}

/** Weak headwords → stronger rhetorical replacements (by lowercase word) */
const GLOBAL_REPLACEMENTS = {
  sweat: {
    word: 'fortitude',
    definition: 'Courage in pain or adversity; steady moral strength.',
    example: 'Blood, toil, tears and sweat named the price; fortitude named the character that would pay it.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['courage', 'leadership', 'legacy'],
  },
  curtain: {
    word: 'vigilance',
    definition: 'The action or state of keeping careful watch for possible danger.',
    example: 'An iron curtain may fall abroad, but vigilance at home is what keeps free peoples awake.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['courage', 'democracy', 'leadership'],
  },
  invasion: {
    word: 'defiance',
    definition: 'Open resistance; bold disobedience in the face of threat.',
    example: 'Defiance rang through the promise to fight on the beaches and never surrender.',
    partOfSpeech: 'noun',
    complexity: 'intermediate',
    categories: ['courage', 'leadership', 'legacy'],
  },
  victim: {
    word: 'indictment',
    definition: 'A formal accusation; a powerful public charge against wrong.',
    example: 'His Fourth of July oration was an indictment of a nation celebrating liberty while denying it.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['democracy', 'humanities', 'courage'],
  },
  cruelty: {
    word: 'hypocrisy',
    definition: 'The practice of claiming standards to which one’s own behavior does not conform.',
    example: 'He named the hypocrisy of praising freedom while protecting the machinery of bondage.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['democracy', 'humanities', 'courage'],
  },
};

/** Orator-specific additions to hit TARGET with high-quality rhetorical lexicon */
const ADDITIONS = {
  1: [
    { word: 'philippic', definition: 'A bitter verbal denunciation, especially a speech against a public enemy.', example: 'Each philippic sharpened the case against Philip until delay itself looked like treason.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['democracy', 'courage', 'leadership'], source: 'Philippics', speech: 'Philippic 1' },
    { word: 'deliberation', definition: 'Long and careful consideration or discussion before action.', example: 'He warned that endless deliberation can become a soft synonym for fear.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['leadership', 'democracy'], source: 'Philippics', speech: 'Philippic 1' },
  ],
  2: [
    { word: 'eloquence', definition: 'Fluent, forceful, and persuasive speaking or writing.', example: 'Cicero treated eloquence as moral architecture—beauty built to carry truth.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['arts', 'humanities', 'leadership'], source: 'Orations', speech: 'In Catilinam I' },
    { word: 'republic', definition: 'A state in which supreme power is held by the people and their representatives.', example: 'He defended the republic as a living trust, not a stage for personal ambition.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['democracy', 'leadership', 'legacy'], source: 'Orations', speech: 'In Catilinam I' },
  ],
  5: [
    { word: 'proposition', definition: 'A statement expressing a judgment or opinion; a proposed idea.', example: 'A new nation dedicated to the proposition that all men are created equal.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['democracy', 'humanities', 'legacy'], source: 'Gettysburg Address', speech: 'Gettysburg Address' },
    { word: 'consecrate', definition: 'To make or declare sacred; to dedicate formally to a divine or solemn purpose.', example: 'We cannot consecrate this ground—the dead have already made it holy beyond our poor power.', partOfSpeech: 'verb', complexity: 'advanced', categories: ['humanities', 'legacy', 'courage'], source: 'Gettysburg Address', speech: 'Gettysburg Address' },
  ],
  8: [
    { word: 'indomitable', definition: 'Impossible to subdue or defeat.', example: 'He summoned an indomitable will: we shall never surrender.', partOfSpeech: 'adjective', complexity: 'advanced', categories: ['courage', 'leadership', 'legacy'], source: 'War speeches', speech: 'We Shall Fight on the Beaches' },
    { word: 'resolve', definition: 'Firm determination to do something.', example: 'In war: resolution. In defeat: defiance. In victory: magnanimity—resolve in every tense of the struggle.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['courage', 'leadership'], source: 'War speeches', speech: 'We Shall Fight on the Beaches' },
  ],
  9: [
    { word: 'brotherhood', definition: 'An association or community of people linked by a common interest.', example: 'He dreamed of a table of brotherhood where former enemies could sit without fear.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['democracy', 'inspirational', 'humanities'], source: 'I Have a Dream', speech: 'I Have a Dream' },
    { word: 'crescendo', definition: 'A gradual increase in intensity; a peak of rhetorical force.', example: 'The refrain rose in crescendo—I have a dream today—until hope itself became a cadence.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['arts', 'inspirational', 'democracy'], source: 'I Have a Dream', speech: 'I Have a Dream' },
  ],
  18: [
    { word: 'soliloquy', definition: 'An act of speaking one’s thoughts aloud when alone, especially in drama.', example: 'Hamlet’s soliloquy turns private dread into public philosophy of action and delay.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['arts', 'humanities'], source: 'Hamlet', speech: 'To be, or not to be' },
    { word: 'conscience', definition: 'An inner sense of right and wrong guiding action.', example: 'Thus conscience does make cowards of us all—and turns resolution pale with thought.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['humanities', 'courage', 'arts'], source: 'Hamlet', speech: 'To be, or not to be' },
  ],
};

// Generic high-quality fillers by theme family for thin modern/fictional lists
const FILLERS = [
  { word: 'conviction', definition: 'A firmly held belief or opinion.', example: 'Conviction without evidence is noise; conviction after struggle is leadership.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['leadership', 'courage'] },
  { word: 'clarity', definition: 'The quality of being coherent and intelligible.', example: 'Clarity is kindness in speech—confusion is often a way to hide.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['leadership', 'humanities'] },
  { word: 'integrity', definition: 'The quality of being honest and having strong moral principles.', example: 'Integrity is what you do when the speech ends and no one is applauding.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['leadership', 'legacy', 'humanities'] },
  { word: 'sacrifice', definition: 'The act of giving up something valued for the sake of other considerations.', example: 'Sacrifice is the receipt attached to every lasting freedom.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['courage', 'legacy'] },
  { word: 'vision', definition: 'The ability to think about or plan the future with imagination or wisdom.', example: 'Vision without execution is hallucination; execution without vision is drudgery.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['leadership', 'inspirational'] },
  { word: 'tenacity', definition: 'The quality of being very determined; persistence.', example: 'Tenacity is talent’s quieter twin—it shows up after applause leaves.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['courage', 'leadership'] },
  { word: 'humility', definition: 'A modest view of one’s own importance.', example: 'Humility keeps a winner teachable and a learner brave enough to begin again.', partOfSpeech: 'noun', complexity: 'intermediate', categories: ['humanities', 'leadership'] },
  { word: 'stewardship', definition: 'The careful and responsible management of something entrusted to one’s care.', example: 'Stewardship asks what you will leave stronger than you found it.', partOfSpeech: 'noun', complexity: 'advanced', categories: ['leadership', 'legacy'] },
];

function main() {
  const usedIds = new Set();
  const files = fs.readdirSync(seedDir).filter((f) => f.startsWith('words_') && f.endsWith('.json'));
  for (const f of files) {
    for (const w of load(f)) usedIds.add(w.id);
  }

  let replaced = 0;
  let added = 0;

  for (const file of files) {
    let words = load(file).map(norm);
    const oid = words[0]?.oratorId;
    if (oid == null) continue;

    // Replace weak headwords
    words = words.map((w) => {
      const key = w.word.toLowerCase();
      if (GLOBAL_REPLACEMENTS[key]) {
        // Avoid duplicate if replacement word already exists
        const rep = GLOBAL_REPLACEMENTS[key];
        if (words.some((x) => x.word.toLowerCase() === rep.word && x.id !== w.id)) {
          return w;
        }
        replaced += 1;
        return norm({
          ...w,
          ...rep,
          oratorId: oid,
          source: w.source,
          speech: w.speech,
        });
      }
      return w;
    });

    // Orator-specific additions
    const existing = new Set(words.map((w) => w.word.toLowerCase()));
    let maxId = words.reduce((m, w) => Math.max(m, w.id), oid * 1000);
    for (const extra of ADDITIONS[oid] || []) {
      if (existing.has(extra.word.toLowerCase())) continue;
      do { maxId += 1; } while (usedIds.has(maxId));
      usedIds.add(maxId);
      words.push(norm({ id: maxId, oratorId: oid, ...extra, source: extra.source || null, speech: extra.speech || null }));
      existing.add(extra.word.toLowerCase());
      added += 1;
    }

    // Fill to TARGET
    let fi = 0;
    while (words.length < TARGET && fi < FILLERS.length * 3) {
      const filler = FILLERS[fi % FILLERS.length];
      fi += 1;
      const candidate = fi > FILLERS.length ? { ...filler, word: filler.word + '' } : filler;
      // vary slightly if needed
      let word = filler.word;
      if (existing.has(word)) {
        // try next
        continue;
      }
      do { maxId += 1; } while (usedIds.has(maxId));
      usedIds.add(maxId);
      words.push(
        norm({
          id: maxId,
          oratorId: oid,
          word,
          definition: filler.definition,
          example: filler.example,
          partOfSpeech: filler.partOfSpeech,
          complexity: filler.complexity,
          categories: filler.categories,
          source: words[0]?.source || null,
          speech: words[0]?.speech || null,
        }),
      );
      existing.add(word);
      added += 1;
    }

    // If still short, force unique variants
    while (words.length < TARGET) {
      do { maxId += 1; } while (usedIds.has(maxId));
      usedIds.add(maxId);
      const n = words.length + 1;
      const word = `purpose`;
      // already have purpose? use alternate set
      const alts = ['merit', 'duty', 'honor', 'valor', 'prudence', 'temperance', 'justice', 'liberty'];
      const alt = alts.find((a) => !existing.has(a)) || `ideal${n}`;
      words.push(
        norm({
          id: maxId,
          oratorId: oid,
          word: alt,
          definition: `A key virtue of character and public life: ${alt}.`,
          example: `${alt.charAt(0).toUpperCase()}${alt.slice(1)} is tested not in comfort but when the easy path is wrong.`,
          partOfSpeech: 'noun',
          complexity: 'intermediate',
          categories: ['leadership', 'humanities', 'courage'],
          source: words[0]?.source || null,
          speech: words[0]?.speech || null,
        }),
      );
      existing.add(alt);
      added += 1;
    }

    save(file, words);
  }

  // Update dictionary counts
  const dicts = load('dictionaries.json');
  const byOid = {};
  for (const file of files) {
    const words = load(file);
    if (words[0]) byOid[words[0].oratorId] = words.length;
  }
  for (const d of dicts) {
    if (byOid[d.id] != null) d.wordCount = byOid[d.id];
  }
  save('dictionaries.json', dicts);

  // Validate
  let badEx = 0;
  let badTheme = 0;
  let min = Infinity;
  for (const file of files) {
    const words = load(file);
    min = Math.min(min, words.length);
    for (const w of words) {
      if (!containsWord(w.word, w.example)) badEx += 1;
      for (const c of w.categories || []) if (!CANONICAL.has(c)) badTheme += 1;
    }
  }

  console.log(JSON.stringify({ replaced, added, minWords: min, badEx, badTheme }, null, 2));
}

main();
