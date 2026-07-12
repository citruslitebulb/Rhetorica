const fs = require('fs');
const path = require('path');

const file = path.join('app', 'src', 'main', 'assets', 'data', 'seed', 'words_churchill.json');
const words = JSON.parse(fs.readFileSync(file, 'utf8'));

const rep = {
  invasion: {
    word: 'defiance',
    definition: 'Open resistance; bold disobedience in the face of threat.',
    example: 'Defiance rang through the promise to fight on the beaches and never surrender.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['courage', 'leadership', 'legacy'],
  },
  curtain: {
    word: 'vigilance',
    definition: 'The action or state of keeping careful watch for possible danger.',
    example:
      'From Stettin in the Baltic to Trieste in the Adriatic an iron curtain fell—and vigilance became the price of free nations.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['courage', 'democracy', 'leadership'],
  },
  toil: {
    word: 'toil',
    definition: 'Exhausting labor undertaken for a hard cause.',
    example: 'I have nothing to offer but blood, toil, tears and sweat—the honest ledger of a nation at war.',
    partOfSpeech: 'noun',
    complexity: 'intermediate',
    categories: ['courage', 'leadership', 'legacy'],
  },
  surrender: {
    word: 'surrender',
    definition: "To cease resistance and submit; to yield a free people's future.",
    example: 'We shall fight on the beaches, we shall fight on the landing grounds; we shall never surrender.',
    partOfSpeech: 'verb',
    complexity: 'intermediate',
    categories: ['courage', 'leadership', 'legacy'],
  },
  magnanimity: {
    word: 'magnanimity',
    definition: 'Generosity of spirit, especially toward a defeated opponent.',
    example: 'In war: resolution. In defeat: defiance. In victory: magnanimity. In peace: goodwill.',
    partOfSpeech: 'noun',
    complexity: 'advanced',
    categories: ['leadership', 'humanities', 'legacy'],
  },
  resolve: {
    word: 'resolve',
    definition: 'Firm determination to act despite fear or fatigue.',
    example:
      'In war: resolution. In defeat: defiance. In victory: magnanimity—resolve in every tense of the struggle.',
    partOfSpeech: 'noun',
    complexity: 'intermediate',
    categories: ['courage', 'leadership'],
  },
};

const out = [];
const seen = new Set();
for (const w of words) {
  const r = rep[w.word.toLowerCase()];
  const next = r
    ? { ...w, ...r, id: w.id, oratorId: w.oratorId, source: w.source, speech: w.speech }
    : { ...w };
  if (
    (!next.categories || (next.categories.length === 1 && next.categories[0] === 'inspirational')) &&
    !r
  ) {
    next.categories = ['courage', 'leadership', 'legacy'];
  }
  const k = next.word.toLowerCase();
  if (seen.has(k)) continue;
  seen.add(k);
  out.push(next);
}

fs.writeFileSync(file, JSON.stringify(out, null, 2) + '\n');
console.log(out.map((w) => w.word).join(', '));
