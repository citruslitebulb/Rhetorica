/**
 * Improve Rhetorica seed vocabulary + speeches for all orators.
 *
 * - Maps invalid theme tags → canonical WordThemes
 * - Rewrites generic / broken examples so the headword appears
 * - Expands thin word lists to a solid rhetorical set
 * - Ensures every orator has at least one speech entry
 * - Dedupes speeches, lowercases headwords, updates dictionary wordCount
 * - Bumps SeedDataLoader.SEED_VERSION via separate note (do manually if needed)
 *
 * Run from repo root: node tools/improve_seed_content.js
 */
const fs = require('fs');
const path = require('path');

const seedDir = path.join('app', 'src', 'main', 'assets', 'data', 'seed');
const CANONICAL = new Set([
  'inspirational', 'tech', 'humanities', 'arts', 'leadership', 'democracy', 'courage', 'legacy',
]);

const THEME_MAP = {
  stoicism: 'humanities', wisdom: 'humanities', virtue: 'humanities', ethics: 'humanities',
  acceptance: 'humanities', judgment: 'humanities', philosophy: 'humanities', balance: 'humanities',
  patience: 'humanities', respect: 'humanities', forgiveness: 'humanities', mentorship: 'leadership',
  entrepreneurship: 'tech', innovation: 'tech', technology: 'tech', moonshots: 'tech',
  'customer-focus': 'tech', 'long-termism': 'tech', space: 'tech', sustainability: 'tech',
  enterprise: 'tech', competition: 'tech', vision: 'inspirational', hope: 'inspirational',
  belief: 'inspirational', destiny: 'inspirational', dreams: 'inspirational', purpose: 'inspirational',
  potential: 'inspirational', opportunity: 'inspirational', ambition: 'leadership',
  perseverance: 'courage', grit: 'courage', resilience: 'courage', adversity: 'courage',
  determination: 'courage', resolve: 'courage', endurance: 'courage', fear: 'courage',
  failure: 'courage', pain: 'courage', comeback: 'courage', victory: 'courage',
  underdog: 'courage', heroism: 'courage', champion: 'courage', heart: 'courage',
  freedom: 'democracy', community: 'democracy', responsibility: 'leadership',
  leadership: 'leadership', discipline: 'leadership', focus: 'leadership', unity: 'leadership',
  teamwork: 'leadership', fundamentals: 'leadership', tradition: 'legacy', history: 'legacy',
  legacy: 'legacy', family: 'legacy', pride: 'legacy', dignity: 'legacy', redemption: 'legacy',
  philanthropy: 'legacy', 'global-impact': 'legacy', power: 'leadership', attitude: 'inspirational',
  winner: 'courage', perfection: 'leadership', conditioning: 'courage', moment: 'inspirational',
  life: 'humanities', arts: 'arts',
};

function mapThemes(cats) {
  const out = [];
  for (const c of cats || []) {
    const key = String(c).toLowerCase().trim();
    const mapped = CANONICAL.has(key) ? key : THEME_MAP[key];
    if (mapped && CANONICAL.has(mapped) && !out.includes(mapped)) out.push(mapped);
  }
  if (out.length === 0) out.push('inspirational');
  return out.slice(0, 3);
}

function containsWord(word, example) {
  const w = (word || '').toLowerCase();
  const ex = (example || '').toLowerCase();
  if (!w || !ex) return false;
  return (
    ex.includes(w) ||
    ex.includes(w + 's') ||
    ex.includes(w + 'd') ||
    ex.includes(w + 'ing') ||
    ex.includes(w + 'ed') ||
    ex.includes(w.replace(/-/g, ' '))
  );
}

function isGenericExample(example) {
  return /orator urged|In his address, the orator|show \w+ when facing the greatest/i.test(example || '');
}

function rewriteExample(entry) {
  const word = entry.word;
  const wl = word.toLowerCase();
  const pos = (entry.partOfSpeech || 'noun').toLowerCase();
  const speech = entry.speech || entry.source || 'the speech';

  if (pos.includes('verb')) {
    return `The appeal was not to wait, but to ${wl}—${speech} made delay look like surrender.`;
  }
  if (pos.includes('adj') || pos.includes('adjective')) {
    return `A ${wl} argument runs through ${speech}: conviction without cruelty, force without panic.`;
  }
  // noun / default
  return `${word.charAt(0).toUpperCase()}${word.slice(1)} is the hinge of ${speech}: without it the rest is decoration.`;
}

function normalizeEntry(entry) {
  const word = String(entry.word || '').trim();
  // Prefer sentence-case headwords for UX (first letter upper only if multi-word titles rare)
  const normalizedWord = word.includes(' ') || word.includes('-')
    ? word
    : word.toLowerCase();

  let example = entry.example || '';
  if (!example.trim() || isGenericExample(example) || !containsWord(normalizedWord, example)) {
    example = rewriteExample({ ...entry, word: normalizedWord });
    // If rewrite still fails (odd morphology), force include
    if (!containsWord(normalizedWord, example)) {
      example = `${normalizedWord.charAt(0).toUpperCase()}${normalizedWord.slice(1)}: ${example}`;
    }
  }

  return {
    ...entry,
    word: normalizedWord,
    definition: String(entry.definition || '').trim(),
    example,
    partOfSpeech: String(entry.partOfSpeech || 'noun').toLowerCase(),
    complexity: entry.complexity || 'intermediate',
    categories: mapThemes(entry.categories),
  };
}

/** Curated expansions: oratorId -> additional words (or full replacement if replace:true) */
const EXPANSIONS = {
  // Steve Jobs (thin)
  19: {
    add: [
      { word: 'renaissance', definition: 'A revival of art, skill, or cultural achievement; a renewed flourishing.', example: 'He cast the personal computer as a tool of renaissance—technology in service of human creativity.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Various interviews', speech: 'Think Different era', categories: ['arts', 'tech', 'inspirational'] },
      { word: 'intersection', definition: 'A point where two or more things meet and influence each other.', example: 'Innovation, he argued, lives at the intersection of technology and the liberal arts.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'iPad introduction', speech: 'iPad introduction', categories: ['tech', 'arts', 'humanities'] },
      { word: 'simplicity', definition: 'The quality of being easy to understand or use; freedom from complexity.', example: 'Simplicity is the ultimate sophistication—remove until only the essential remains.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Design philosophy', speech: 'Design talks', categories: ['tech', 'arts', 'leadership'] },
      { word: 'insanely', definition: 'To an extreme or extraordinary degree (colloquial intensifier).', example: 'The promise was not merely better—it was insanely great, or not worth shipping.', partOfSpeech: 'adverb', complexity: 'basic', source: 'Early Apple culture', speech: 'Apple product launches', categories: ['tech', 'inspirational'] },
      { word: 'connect', definition: 'To join or bring together; to form a meaningful link.', example: 'You cannot connect the dots looking forward; you can only connect them looking backward.', partOfSpeech: 'verb', complexity: 'basic', source: 'Stanford Commencement', speech: 'Stanford Commencement Address', categories: ['inspirational', 'legacy', 'humanities'] },
      { word: 'death', definition: 'The end of life; used metaphorically as the ultimate constraint that clarifies priorities.', example: 'Remembering that you will be dead soon is the most important tool for avoiding the trap of thinking you have something to lose.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Stanford Commencement', speech: 'Stanford Commencement Address', categories: ['humanities', 'inspirational', 'legacy'] },
      { word: 'stay', definition: 'To remain in a specified state or place; to continue.', example: 'Stay hungry. Stay foolish—keep the beginner’s appetite even after success.', partOfSpeech: 'verb', complexity: 'basic', source: 'Stanford Commencement', speech: 'Stanford Commencement Address', categories: ['inspirational', 'leadership'] },
      { word: 'design', definition: 'The purposeful shaping of how something looks and works.', example: 'Design is not just how it looks; design is how it works—form obeying function and feeling.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Product philosophy', speech: 'Product keynotes', categories: ['tech', 'arts'] },
      { word: 'humanity', definition: 'Human beings collectively; the quality of being humane.', example: 'Technology alone is not enough—it is technology married with the humanities that yields results that make our hearts sing.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'iPad introduction', speech: 'iPad introduction', categories: ['humanities', 'tech', 'arts'] },
      { word: 'pioneer', definition: 'A person who is among the first to explore or develop a new area.', example: 'The ones who are crazy enough to think they can change the world are the ones who do—pioneers, not caretakers.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Think Different', speech: 'Think Different', categories: ['inspirational', 'leadership', 'legacy'] },
      { word: 'polish', definition: 'Refinement and care applied until a work feels finished and intentional.', example: 'He demanded polish on details most people never notice—because excellence is a habit of attention.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Apple product culture', speech: 'Product reviews / culture', categories: ['leadership', 'tech', 'arts'] },
    ],
  },
  20: { // Gates
    add: [
      { word: 'equity', definition: 'Fairness in outcomes and access, not merely equal treatment on paper.', example: 'Philanthropy without equity still leaves the poorest waiting at the back of the line for breakthroughs.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Gates Foundation talks', speech: 'Global health addresses', categories: ['legacy', 'humanities', 'inspirational'] },
      { word: 'breakthrough', definition: 'A sudden, important advance in knowledge or technique.', example: 'A single medical breakthrough can rewrite the life expectancy of an entire generation.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['tech', 'inspirational', 'legacy'] },
      { word: 'complexity', definition: 'The state of having many interconnected parts; difficulty of analysis.', example: 'Software taught him to tame complexity; global health taught him complexity fights back.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Reflections on work', speech: 'Various addresses', categories: ['tech', 'humanities'] },
      { word: 'privilege', definition: 'A special right or advantage available only to a particular person or group.', example: 'Privilege unused for others is a wasted inheritance of luck and education.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['leadership', 'legacy', 'humanities'] },
      { word: 'measure', definition: 'To assess by a standard; a basis for comparison.', example: 'We measure progress not by patents filed, but by lives lengthened and children educated.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Foundation metrics talks', speech: 'Foundation addresses', categories: ['leadership', 'legacy'] },
      { word: 'optimism', definition: 'Hopefulness and confidence about the future or success of something.', example: 'Realistic optimism still builds schools and vaccines; cynicism builds nothing.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Public writing', speech: 'Public essays and talks', categories: ['inspirational', 'leadership'] },
      { word: 'scale', definition: 'The relative size or extent of something; to grow to large size.', example: 'Ideas that cannot scale remain anecdotes; ideas that scale become history.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Tech and philanthropy', speech: 'Various', categories: ['tech', 'leadership'] },
      { word: 'inequity', definition: 'Lack of fairness or justice; an instance of unfairness.', example: 'The scandal of our age is not only poverty—it is preventable inequity next door to abundance.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['humanities', 'democracy', 'legacy'] },
    ],
  },
  21: { // Bezos
    add: [
      { word: 'customer', definition: 'A person who buys goods or services; the focus of a business.', example: 'Start with the customer and work backwards—every process is a servant of that end.', partOfSpeech: 'noun', complexity: 'basic', source: 'Amazon leadership principles', speech: 'Day 1 Philosophy', categories: ['tech', 'leadership'] },
      { word: 'day-one', definition: 'A mindset of permanent beginning: urgency, invention, and refusal of stagnation.', example: 'Day-one culture resists day-two stagnation—bureaucracy, proxies, and resting on momentum.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Shareholder letters', speech: 'Day 1 Philosophy', categories: ['tech', 'leadership', 'inspirational'] },
      { word: 'proxy', definition: 'A stand-in measure used instead of the real goal (often dangerously).', example: 'When a metric becomes a proxy for the mission, people optimize the number and forget the customer.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Leadership principles', speech: 'Leadership talks', categories: ['leadership', 'tech'] },
      { word: 'invent', definition: 'To create or design something that has not existed before.', example: 'If you invent only what is safe, you will never hear the future arrive.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Shareholder letters', speech: 'Long-Term Thinking', categories: ['tech', 'inspirational'] },
      { word: 'patience', definition: 'The capacity to accept delay without anger; long-horizon endurance.', example: 'Patience is a strategy when the flywheel needs years, not quarters, to turn.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Long-term investing talks', speech: 'Long-Term Thinking', categories: ['leadership', 'tech'] },
      { word: 'high-standards', definition: 'Demanding criteria for quality that raise the floor for everyone.', example: 'High-standards are contagious: people either rise to them or leave the room.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Leadership principles', speech: 'Leadership principles', categories: ['leadership'] },
    ],
  },
  22: { // Musk
    add: [
      { word: 'multiplanetary', definition: 'Existing or capable of existing on more than one planet.', example: 'A multiplanetary civilization is an insurance policy for consciousness itself.', partOfSpeech: 'adjective', complexity: 'advanced', source: 'Making Life Multiplanetary', speech: 'Making Life Multiplanetary', categories: ['tech', 'inspirational', 'legacy'] },
      { word: 'reusability', definition: 'The ability of a system (e.g. rockets) to be used again after recovery.', example: 'Without reusability, spaceflight remains a fireworks show priced for nations alone.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Starship updates', speech: 'Starship Update', categories: ['tech', 'leadership'] },
      { word: 'physics', definition: 'The science of matter, energy, and their interactions; fundamental constraints on engineering.', example: 'Reason from physics first—marketing cannot repeal thermodynamics.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Engineering talks', speech: 'Engineering discussions', categories: ['tech', 'humanities'] },
      { word: 'acceleration', definition: 'Increase in rate or speed; rapid progress.', example: 'The goal is not gradualism for its own sake, but intelligent acceleration of useful tech.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Product launches', speech: 'Product and energy talks', categories: ['tech', 'inspirational'] },
      { word: 'scarce', definition: 'Insufficient for the demand; rare.', example: 'Attention is scarce; waste it on theater and the mission starves.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Work culture remarks', speech: 'Internal and public remarks', categories: ['leadership', 'tech'] },
    ],
  },
  23: { // Zuckerberg
    add: [
      { word: 'connection', definition: 'A relationship in which people or things are linked.', example: 'The bet was simple and vast: connection at global scale can still start with one campus network.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['tech', 'democracy', 'inspirational'] },
      { word: 'purpose', definition: 'The reason for which something is done or created.', example: 'Purpose is the story that makes hard years feel like building, not drifting.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['inspirational', 'leadership'] },
      { word: 'community', definition: 'A group of people living or working together with shared interests.', example: 'A community is not a graph of accounts; it is trust that survives disagreement.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Public talks', speech: 'Community and product talks', categories: ['democracy', 'tech', 'inspirational'] },
      { word: 'misunderstood', definition: 'Incorrectly interpreted or judged by others.', example: 'Be prepared to be misunderstood if your horizon is longer than a news cycle.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['inspirational', 'leadership', 'courage'] },
      { word: 'progress', definition: 'Forward movement toward a destination or improved condition.', example: 'Progress is uneven—defend the direction even when the week looks ugly.', partOfSpeech: 'noun', complexity: 'basic', source: 'Public essays', speech: 'Public addresses', categories: ['inspirational', 'tech'] },
      { word: 'responsibility', definition: 'The state of being accountable for something within one’s power.', example: 'Scale without responsibility is just a larger blast radius.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Public testimony and talks', speech: 'Public addresses', categories: ['leadership', 'democracy', 'tech'] },
      { word: 'mission', definition: 'An important assignment or purpose undertaken by a group.', example: 'A mission statement is worthless until it forces trade-offs in the product roadmap.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Company culture', speech: 'Internal and public talks', categories: ['leadership', 'tech'] },
      { word: 'idealistic', definition: 'Characterized by idealism; guided by high principles.', example: 'It is good to be idealistic—then pair it with the humility to learn from people you affect.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Harvard Commencement', speech: 'Harvard Commencement Address', categories: ['inspirational', 'humanities'] },
    ],
  },
  24: { // Page
    add: [
      { word: 'moonshot', definition: 'An extremely ambitious project with potentially transformative impact.', example: 'A moonshot accepts a high chance of failure in exchange for a non-incremental prize.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Google I/O', speech: 'Google I/O Keynote on Moonshots', categories: ['tech', 'inspirational', 'leadership'] },
      { word: '10x', definition: 'An improvement on the order of ten times, not ten percent.', example: 'Aim for 10x, not 10 percent—small thinking makes small organizations.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Moonshot culture', speech: 'Google I/O Keynote on Moonshots', categories: ['tech', 'leadership'] },
      { word: 'imagine', definition: 'To form a mental image of something not present; to conceive creatively.', example: 'The future belongs to those who can imagine it clearly enough to prototype.', partOfSpeech: 'verb', complexity: 'basic', source: 'Vision talks', speech: 'Vision talks', categories: ['inspirational', 'tech'] },
      { word: 'constraint', definition: 'A limitation or restriction that shapes design choices.', example: 'Treat physics as a constraint, not an excuse—great engineering dances with limits.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Engineering culture', speech: 'Engineering talks', categories: ['tech', 'leadership'] },
      { word: 'audacity', definition: 'Willingness to take bold risks; confident daring.', example: 'Audacity without rigor is noise; rigor without audacity is maintenance.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Moonshot culture', speech: 'Moonshot talks', categories: ['courage', 'tech', 'leadership'] },
      { word: 'platform', definition: 'A foundation on which other applications, processes, or technologies are built.', example: 'Build a platform when one product cannot hold the whole opportunity.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Product strategy', speech: 'Product strategy talks', categories: ['tech', 'leadership'] },
      { word: 'relentless', definition: 'Oppressively constant; unyielding in pursuit.', example: 'Relentless curiosity is the only scalable hiring filter that still feels human.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Culture remarks', speech: 'Culture talks', categories: ['leadership', 'inspirational'] },
      { word: 'foundational', definition: 'Serving as a base or groundwork; fundamental.', example: 'Foundational research looks unprofitable—until the industry stands on it.', partOfSpeech: 'adjective', complexity: 'advanced', source: 'Research philosophy', speech: 'Research talks', categories: ['tech', 'legacy'] },
    ],
  },
  25: { // Brin
    add: [
      { word: 'organize', definition: 'To arrange systematically; to put into a structured whole.', example: 'The mission was almost absurd in scale: organize the world’s information and make it useful.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Google mission', speech: 'On Organizing the World\'s Information', categories: ['tech', 'inspirational'] },
      { word: 'useful', definition: 'Able to be used for a practical purpose; beneficial.', example: 'Information is not enough—it must become useful in the moment of need.', partOfSpeech: 'adjective', complexity: 'basic', source: 'Google mission', speech: 'On Organizing the World\'s Information', categories: ['tech', 'humanities'] },
      { word: 'experiment', definition: 'A procedure carried out to test a hypothesis or demonstrate a fact.', example: 'Culture that forbids experiment eventually forbids discovery.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Google culture', speech: 'Culture talks', categories: ['tech', 'inspirational'] },
      { word: 'curiosity', definition: 'A strong desire to know or learn something.', example: 'Hire for curiosity and the product roadmap writes half of itself.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Hiring philosophy', speech: 'Talks on culture', categories: ['humanities', 'tech', 'leadership'] },
      { word: 'access', definition: 'The means or opportunity to approach or use something.', example: 'Access to knowledge is a quiet form of power redistribution.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Mission talks', speech: 'Mission talks', categories: ['democracy', 'tech', 'humanities'] },
      { word: 'serendipity', definition: 'The occurrence of valuable discoveries by chance.', example: 'Great search preserves serendipity—the result you needed but did not know to name.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Search philosophy', speech: 'Search talks', categories: ['tech', 'arts', 'humanities'] },
      { word: 'openness', definition: 'Lack of secrecy or concealment; willingness to share.', example: 'Openness in research multiplies progress faster than secrecy multiplies prestige.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Research culture', speech: 'Research remarks', categories: ['humanities', 'tech'] },
      { word: 'impact', definition: 'A marked effect or influence.', example: 'Measure impact by problems reduced, not by press releases issued.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Philanthropy and tech', speech: 'Impact talks', categories: ['legacy', 'leadership'] },
    ],
  },
  26: { // Ellison
    add: [
      { word: 'database', definition: 'A structured set of data held in a computer, accessible in various ways.', example: 'He treated the database as industrial infrastructure—the silent heart of modern enterprise.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Oracle keynotes', speech: 'Oracle OpenWorld Keynote Vision', categories: ['tech', 'leadership'] },
      { word: 'compete', definition: 'To strive to gain or win something by defeating others.', example: 'To compete at the top is to learn faster than the market can copy you.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Competitive strategy', speech: 'Industry talks', categories: ['tech', 'leadership', 'courage'] },
      { word: 'cloud', definition: 'On-demand computing resources delivered over the internet.', example: 'The cloud is not a place—it is an operating model that punishes hesitation.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cloud strategy talks', speech: 'Cloud keynotes', categories: ['tech'] },
      { word: 'automation', definition: 'The use of technology to perform tasks with minimal human intervention.', example: 'Automation without judgment creates faster mistakes; pair it with accountability.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Enterprise talks', speech: 'Enterprise talks', categories: ['tech', 'leadership'] },
      { word: 'ruthless', definition: 'Without pity or compassion; relentlessly severe (in pursuit of goals).', example: 'A ruthless focus on the mission can still leave room for loyalty to people who ship.', partOfSpeech: 'adjective', complexity: 'advanced', source: 'Leadership style commentary', speech: 'Leadership remarks', categories: ['leadership', 'courage'] },
      { word: 'architecture', definition: 'The complex structure of a system; the art of designing structures.', example: 'Software architecture is destiny: choose poorly and every feature becomes a tax.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Engineering strategy', speech: 'Engineering keynotes', categories: ['tech', 'leadership'] },
      { word: 'enterprise', definition: 'A business organization; large-scale commercial activity.', example: 'Enterprise software is unglamorous until civilization depends on it staying up.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Oracle positioning', speech: 'Oracle OpenWorld Keynote Vision', categories: ['tech', 'leadership'] },
      { word: 'performance', definition: 'The action of carrying out a task; measurable efficiency of a system.', example: 'Performance is a feature: users feel latency as disrespect.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Product philosophy', speech: 'Product talks', categories: ['tech', 'leadership'] },
    ],
  },
};

// Shared expansion pack generator for sports/film mentors with thin lists
function mentorPack(oratorId, sourceLabel, speechTitle, words) {
  return words.map((w, i) => ({
    word: w.word,
    definition: w.definition,
    example: w.example,
    partOfSpeech: w.partOfSpeech || 'noun',
    complexity: w.complexity || 'intermediate',
    source: sourceLabel,
    speech: speechTitle,
    categories: w.categories || ['inspirational', 'courage'],
    oratorId,
  }));
}

const MENTOR_EXPANSIONS = {
  28: { // Aurelius
    add: [
      { word: 'logos', definition: 'In Stoic thought, the rational principle ordering the cosmos and human reason.', example: 'Live according to logos: let reason, not impulse, write the next line of your day.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Meditations', speech: 'Meditations', categories: ['humanities', 'leadership'] },
      { word: 'discipline', definition: 'Training that develops self-control, character, or efficiency.', example: 'Discipline of perception is the first freedom—see clearly before you act.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Meditations', speech: 'Meditations', categories: ['leadership', 'humanities', 'courage'] },
      { word: 'impermanence', definition: 'The state of not lasting forever; transience.', example: 'Impermanence is not a threat to meaning; it is the reason this hour matters.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Meditations', speech: 'Meditations', categories: ['humanities', 'legacy'] },
      { word: 'equanimity', definition: 'Mental calmness and composure, especially in difficulty.', example: 'Equanimity is not numbness—it is strength that refuses to be jerked by every rumor.', partOfSpeech: 'noun', complexity: 'advanced', source: 'Meditations', speech: 'Meditations', categories: ['humanities', 'courage', 'leadership'] },
    ],
  },
  29: {
    add: [
      { word: 'fellowship', definition: 'Friendly association, especially with people who share interests; a company of companions.', example: 'Fellowship is the answer when the road is too dark for one pair of feet.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Lord of the Rings', speech: 'Council counsel', categories: ['inspirational', 'leadership', 'courage'] },
      { word: 'shadow', definition: 'A dark area; metaphorically, evil or despair that follows light.', example: 'Even the smallest person can change the course of the future when the shadow seems complete.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Lord of the Rings', speech: 'Counsel to Frodo', categories: ['courage', 'inspirational', 'humanities'] },
      { word: 'pity', definition: 'The feeling of sorrow and compassion caused by the suffering of others.', example: 'It was pity that stayed Bilbo’s hand—and pity that left a crack for light to enter later.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Fellowship of the Ring', speech: 'On Bilbo and the Ring', categories: ['humanities', 'legacy', 'courage'] },
      { word: 'peril', definition: 'Serious and immediate danger.', example: 'The world is full of peril, yet still full of things worth defending.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Lord of the Rings', speech: 'Counsel in dark times', categories: ['courage', 'inspirational'] },
      { word: 'courage', definition: 'The ability to do something that frightens one; strength in the face of pain or grief.', example: 'Courage is found in unlikely places, often in those who never sought the title of hero.', partOfSpeech: 'noun', complexity: 'basic', source: 'The Lord of the Rings', speech: 'On hobbits and heroes', categories: ['courage', 'inspirational', 'leadership'] },
      { word: 'wanderer', definition: 'A person who travels aimlessly; one who roams.', example: 'Not all those who wander are lost—a wanderer may still walk a true road.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Fellowship of the Ring', speech: 'Verse of the Rings / Strider', categories: ['inspirational', 'arts', 'legacy'] },
      { word: 'kindle', definition: 'To light or set on fire; to arouse or inspire an emotion.', example: 'A single act of kindness can kindle hope when strategy has gone cold.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'The Lord of the Rings', speech: 'Counsel of hope', categories: ['inspirational', 'humanities'] },
      { word: 'endurance', definition: 'The ability to withstand hardship over a long period.', example: 'Endurance, not glory, carries the Ring when strength has already been spent.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Lord of the Rings', speech: 'On the long road', categories: ['courage', 'legacy'] },
    ],
  },
  30: {
    add: [
      { word: 'luminous', definition: 'Full of or shedding light; brilliant or enlightening.', example: 'In a dark place we find ourselves, and a little more knowledge lights our way—luminous, if we allow it.', partOfSpeech: 'adjective', complexity: 'advanced', source: 'Star Wars', speech: 'Counsel in darkness', categories: ['humanities', 'inspirational'] },
      { word: 'control', definition: 'The power to influence or direct behavior or the course of events.', example: 'Control is an illusion the dark side sells; mastery begins with the self.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Star Wars', speech: 'Lessons on the Force', categories: ['humanities', 'leadership', 'courage'] },
      { word: 'pass', definition: 'To go by; to let something move beyond without holding it.', example: 'Train yourself to let go—what you fear to lose will pass through open hands more safely.', partOfSpeech: 'verb', complexity: 'basic', source: 'Revenge of the Sith', speech: 'Yoda\'s Advice to Anakin', categories: ['humanities', 'courage'] },
      { word: 'judge', definition: 'To form an opinion about; to conclude after assessment.', example: 'Size matters not—judge me by my size, do you?', partOfSpeech: 'verb', complexity: 'basic', source: 'The Empire Strikes Back', speech: 'Training Luke', categories: ['inspirational', 'courage', 'humanities'] },
      { word: 'try', definition: 'To make an attempt or effort to do something.', example: 'Do or do not. There is no try—commitment leaves no half-measure.', partOfSpeech: 'verb', complexity: 'basic', source: 'The Empire Strikes Back', speech: 'Training Luke', categories: ['courage', 'leadership', 'inspirational'] },
      { word: 'anger', definition: 'A strong feeling of annoyance, displeasure, or hostility.', example: 'Fear leads to anger; anger leads to hate—the path is a slope, not a single step.', partOfSpeech: 'noun', complexity: 'basic', source: 'The Phantom Menace', speech: 'On fear', categories: ['humanities', 'courage'] },
      { word: 'serenity', definition: 'The state of being calm, peaceful, and untroubled.', example: 'Serenity is not the absence of the storm—it is balance within it.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Star Wars lore / Jedi code themes', speech: 'Jedi counsel', categories: ['humanities', 'inspirational'] },
      { word: 'teacher', definition: 'A person who instructs others.', example: 'The greatest teacher, failure is—if the student will listen.', partOfSpeech: 'noun', complexity: 'basic', source: 'The Last Jedi', speech: 'Yoda\'s Lesson to Luke', categories: ['humanities', 'leadership', 'legacy'] },
    ],
  },
  31: {
    add: [
      { word: 'distance', definition: 'The full length of a race or struggle; endurance to the finish.', example: 'I can’t beat him, but I can go the distance—and that will mean I am not just another bum from the neighborhood.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Rocky', speech: 'Going the Distance', categories: ['courage', 'inspirational', 'legacy'] },
      { word: 'forward', definition: 'Toward a point ahead; onward in progress.', example: 'It is not how hard you hit—it is how hard you can get hit and keep moving forward.', partOfSpeech: 'adverb', complexity: 'basic', source: 'Rocky Balboa', speech: 'Keep Moving Forward', categories: ['courage', 'inspirational'] },
      { word: 'champion', definition: 'A person who has defeated all rivals; a fighter for a cause.', example: 'A champion is someone who gets up when they can’t—title belt optional.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Rocky series', speech: 'Training and fights', categories: ['courage', 'leadership', 'inspirational'] },
      { word: 'belief', definition: 'Trust, faith, or confidence in someone or something.', example: 'Belief is a muscle: unused, it atrophies under other people’s opinions.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Rocky series', speech: 'Locker-room counsel', categories: ['inspirational', 'courage'] },
      { word: 'round', definition: 'A unit of play in a contest; another attempt after fatigue.', example: 'Going in one more round when you don’t think you can—that is perseverance with a mouthguard.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rocky series', speech: 'On perseverance', categories: ['courage', 'inspirational'] },
      { word: 'tough', definition: 'Strong enough to withstand adverse conditions; resilient.', example: 'Life is tough; tough is not the same as cruel—keep your heart while you take the punches.', partOfSpeech: 'adjective', complexity: 'basic', source: 'Rocky Balboa', speech: 'Keep Moving Forward', categories: ['courage', 'humanities'] },
      { word: 'neighborhood', definition: 'A district or community within a town or city.', example: 'He fought so the neighborhood would see that somebody from these streets could stand tall.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rocky', speech: 'Going the Distance', categories: ['legacy', 'inspirational', 'democracy'] },
      { word: 'will', definition: 'The faculty by which a person decides on and initiates action.', example: 'Talent opens the door; will keeps you in the gym after talent gets tired.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Rocky series', speech: 'Training montages / counsel', categories: ['courage', 'leadership'] },
    ],
  },
  32: {
    add: [
      { word: 'balance', definition: 'An even distribution of weight enabling steadiness; emotional equilibrium.', example: 'Better learn balance—balance is key, Daniel-san.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Karate Kid', speech: 'Miyagi training', categories: ['humanities', 'leadership', 'courage'] },
      { word: 'wax', definition: 'To apply wax; in training, a repetitive motion that builds skill indirectly.', example: 'Wax on, wax off—the chore hides the lesson until the body understands.', partOfSpeech: 'verb', complexity: 'basic', source: 'The Karate Kid', speech: 'Miyagi training', categories: ['leadership', 'inspirational'] },
      { word: 'honor', definition: 'High respect; adherence to what is right.', example: 'Karate is for defense; honor decides when the hands stay down.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Karate Kid', speech: 'Miyagi philosophy', categories: ['humanities', 'courage', 'legacy'] },
      { word: 'inner', definition: 'Situated inside; of the mind or spirit rather than outward show.', example: 'If come from inside you, always right one—the inner voice before the crowd’s.', partOfSpeech: 'adjective', complexity: 'basic', source: 'The Karate Kid', speech: 'Miyagi counsel', categories: ['humanities', 'inspirational'] },
      { word: 'defense', definition: 'The action of protecting from attack.', example: 'First learn stand, then learn fly—defense before spectacle.', partOfSpeech: 'noun', complexity: 'basic', source: 'The Karate Kid', speech: 'Miyagi training', categories: ['courage', 'leadership'] },
      { word: 'trust', definition: 'Firm belief in the reliability or truth of someone or something.', example: 'Trust the process when the lesson looks like chores and not championships.', partOfSpeech: 'verb', complexity: 'basic', source: 'The Karate Kid', speech: 'Miyagi training', categories: ['leadership', 'inspirational'] },
      { word: 'gentle', definition: 'Mild in temperament or behavior; not harsh.', example: 'A gentle teacher can still forge a fighter who never starts the fight.', partOfSpeech: 'adjective', complexity: 'basic', source: 'The Karate Kid', speech: 'Miyagi philosophy', categories: ['humanities', 'leadership'] },
      { word: 'foundation', definition: 'An underlying basis or principle.', example: 'Without foundation, fancy kicks are only decoration waiting to collapse.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'The Karate Kid', speech: 'Miyagi training', categories: ['leadership', 'legacy'] },
    ],
  },
  33: {
    add: [
      { word: 'dream', definition: 'A cherished aspiration or ideal.', example: 'The dream was simple and impossible: play for the Irish, then refuse to quit it.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rudy', speech: 'Rudy\'s pursuit', categories: ['inspirational', 'courage', 'legacy'] },
      { word: 'quit', definition: 'To stop or discontinue an activity.', example: 'I came here to play football for the Irish—and I’m not going to quit what I started.', partOfSpeech: 'verb', complexity: 'basic', source: 'Rudy', speech: 'Rudy\'s pursuit', categories: ['courage', 'inspirational'] },
      { word: 'practice', definition: 'Repeated exercise to acquire proficiency; the carrying out of an idea.', example: 'Practice is where the walk-on becomes real—before the stadium ever knows a name.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rudy', speech: 'Practice field', categories: ['leadership', 'courage'] },
      { word: 'scrimmage', definition: 'A practice game or session in sports.', example: 'One scrimmage can justify years of invisible work if you meet it ready.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Rudy', speech: 'Team practice', categories: ['courage', 'inspirational'] },
      { word: 'loyal', definition: 'Giving or showing firm and constant support.', example: 'Loyal teammates can hand you a moment the coaches still doubt you deserve.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Rudy', speech: 'Team loyalty', categories: ['leadership', 'legacy', 'inspirational'] },
      { word: 'impossible', definition: 'Not able to occur or be done.', example: 'Impossible is a scouting report, not a prophecy—until you believe it.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Rudy', speech: 'Underdog story', categories: ['courage', 'inspirational'] },
      { word: 'name', definition: 'A word by which a person is known; reputation.', example: 'They finally chanted the name—not because it was famous, but because it refused to leave.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rudy', speech: 'Stadium moment', categories: ['legacy', 'inspirational'] },
      { word: 'effort', definition: 'A vigorous or determined attempt.', example: 'Effort is the only highlight reel a walk-on can always control.', partOfSpeech: 'noun', complexity: 'basic', source: 'Rudy', speech: 'Rudy\'s pursuit', categories: ['courage', 'leadership'] },
    ],
  },
  34: {
    add: [
      { word: 'tonight', definition: 'On the present or approaching evening; this decisive moment.', example: 'If we played them ten times, they might win nine. But not this game. Not tonight.', partOfSpeech: 'adverb', complexity: 'basic', source: 'Miracle', speech: 'Locker room speech', categories: ['courage', 'inspirational', 'leadership'] },
      { word: 'name-on-the-front', definition: 'Allegiance to the team identity over individual glory (the jersey’s front).', example: 'The name on the front of the jersey is more important than the one on the back.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Miracle', speech: 'Team identity', categories: ['leadership', 'courage'] },
      { word: 'skate', definition: 'To move on skates; metaphorically, to match pace with a superior opponent.', example: 'Tonight we skate with them—we stay with them—and we shut them down because we can.', partOfSpeech: 'verb', complexity: 'basic', source: 'Miracle', speech: 'Locker room speech', categories: ['courage', 'leadership'] },
      { word: 'condition', definition: 'The state of physical fitness; to train for endurance.', example: 'You don’t have enough talent to win on talent alone—condition until character shows.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Miracle', speech: 'Training philosophy', categories: ['leadership', 'courage'] },
      { word: 'american', definition: 'Of or relating to the United States; used here as a call to shared identity.', example: 'I’m sick and tired of hearing about what a great hockey team the Soviets have—this is your time.', partOfSpeech: 'adjective', complexity: 'basic', source: 'Miracle', speech: 'Locker room speech', categories: ['inspirational', 'courage', 'legacy'] },
      { word: 'together', definition: 'With each other; into companionship or unity.', example: 'Great moments are born from great opportunity—and from players who finally play together.', partOfSpeech: 'adverb', complexity: 'basic', source: 'Miracle', speech: 'Team building', categories: ['leadership', 'inspirational'] },
      { word: 'opportunity', definition: 'A set of circumstances that makes it possible to do something.', example: 'Great moments are born from great opportunity—if you are ready when it arrives.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Miracle', speech: 'Locker room speech', categories: ['inspirational', 'leadership', 'courage'] },
      { word: 'miracle', definition: 'An extraordinary event that brings welcome consequences; a seeming impossibility achieved.', example: 'They called it a miracle after the fact; in the room it was only work meeting belief.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Miracle', speech: '1980 Olympic hockey', categories: ['inspirational', 'legacy', 'courage'] },
    ],
  },
  35: {
    add: [
      { word: 'hallowed', definition: 'Made holy; greatly revered and honored.', example: 'If we don’t come together on this hallowed ground, we too will be destroyed—just like they were.', partOfSpeech: 'adjective', complexity: 'advanced', source: 'Remember the Titans', speech: 'Gettysburg run', categories: ['legacy', 'courage', 'democracy'] },
      { word: 'together', definition: 'With each other; in unity.', example: 'We will not be perfect until we play as one—together is the only formation that wins.', partOfSpeech: 'adverb', complexity: 'basic', source: 'Remember the Titans', speech: 'Team speeches', categories: ['leadership', 'inspirational'] },
      { word: 'perfect', definition: 'Having all required elements; free from flaw in effort and standard.', example: 'We will be perfect in every aspect of the game—you drop a pass, you run a mile.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Remember the Titans', speech: 'Training standards', categories: ['leadership', 'courage'] },
      { word: 'attitude', definition: 'A settled way of thinking or feeling, typically reflected in behavior.', example: 'Attitude reflects leadership—what you permit, you teach.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Remember the Titans', speech: 'Leadership on the field', categories: ['leadership', 'inspirational'] },
      { word: 'left-side', definition: 'A call of unity and strength from one half of a divided team.', example: 'Left side! Strong side!—the chant that turned two sides into one body.', partOfSpeech: 'noun', complexity: 'basic', source: 'Remember the Titans', speech: 'Team unity', categories: ['leadership', 'courage', 'democracy'] },
      { word: 'listen', definition: 'To give attention with the ear; to heed.', example: 'You will listen to each other, or you will listen to the sound of losing.', partOfSpeech: 'verb', complexity: 'basic', source: 'Remember the Titans', speech: 'Team meetings', categories: ['leadership', 'humanities'] },
      { word: 'respect', definition: 'Due regard for the feelings, rights, or traditions of others.', example: 'Respect is not a trophy—it is the daily decision to see the man beside you.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Remember the Titans', speech: 'Team culture', categories: ['humanities', 'leadership', 'democracy'] },
      { word: 'football', definition: 'A team sport; here, a vehicle for character and integration.', example: 'Football is the classroom; character is the exam you cannot cheat.', partOfSpeech: 'noun', complexity: 'basic', source: 'Remember the Titans', speech: 'Season arc', categories: ['leadership', 'legacy'] },
    ],
  },
  36: {
    add: [
      { word: 'fundamentals', definition: 'Basic principles or skills forming the foundation of a discipline.', example: 'Forget the crowds and fancy uniforms—remember the fundamentals that got you here.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Hoosiers', speech: 'Coach Dale talks', categories: ['leadership', 'courage', 'inspirational'] },
      { word: 'team', definition: 'A group organized to work together.', example: 'Five players on the floor functioning as one single unit: team, team, team.', partOfSpeech: 'noun', complexity: 'basic', source: 'Hoosiers', speech: 'Coach Dale talks', categories: ['leadership', 'inspirational'] },
      { word: 'measure', definition: 'To assess size or extent; the basket’s height as a metaphor for constancy.', example: 'The basket is still ten feet—measure your fear against what has not changed.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Hoosiers', speech: 'State finals calm', categories: ['courage', 'inspirational', 'leadership'] },
      { word: 'focus', definition: 'The center of interest or activity; concentrated attention.', example: 'My practices aren’t designed for your enjoyment—they are designed for focus under noise.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Hoosiers', speech: 'Practice philosophy', categories: ['leadership', 'courage'] },
      { word: 'small-town', definition: 'Characteristic of a small community; often underestimated.', example: 'A small-town team can still walk into a palace if their habits are bigger than the building.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Hoosiers', speech: 'Underdog season', categories: ['inspirational', 'legacy', 'courage'] },
      { word: 'pass', definition: 'To throw the ball to a teammate; to prioritize the group.', example: 'The best shot is sometimes the pass you almost refused to make.', partOfSpeech: 'noun', complexity: 'basic', source: 'Hoosiers', speech: 'Team basketball', categories: ['leadership', 'inspirational'] },
      { word: 'pressure', definition: 'Continuous physical or mental force; stress of competition.', example: 'Pressure is a privilege when you trained for the moment it arrives.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Hoosiers', speech: 'Game preparation', categories: ['courage', 'leadership'] },
      { word: 'belief', definition: 'Trust or confidence in a person, team, or idea.', example: 'Belief starts on the practice floor, long before the gym ever fills.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Hoosiers', speech: 'Season arc', categories: ['inspirational', 'courage'] },
    ],
  },
  37: {
    add: [
      { word: 'believe', definition: 'To accept as true; to have confidence in.', example: 'I have to believe that when things are bad I can change them.', partOfSpeech: 'verb', complexity: 'basic', source: 'Cinderella Man', speech: 'Braddock resolve', categories: ['inspirational', 'courage'] },
      { word: 'fight', definition: 'To take part in a violent struggle; to struggle determinedly.', example: 'I know what I’m fighting for—and that knowledge is heavier than any opponent.', partOfSpeech: 'verb', complexity: 'basic', source: 'Cinderella Man', speech: 'Braddock resolve', categories: ['courage', 'legacy', 'inspirational'] },
      { word: 'relief', definition: 'Financial or material assistance; alleviation of distress.', example: 'Dignity asked for work, not pity—yet relief kept a family standing until the comeback.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cinderella Man', speech: 'Depression-era struggle', categories: ['humanities', 'democracy', 'legacy'] },
      { word: 'champion', definition: 'The winner of a competition; one who defends a person or cause.', example: 'You are the champion of my heart—long before the belt returned.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cinderella Man', speech: 'Family and fight', categories: ['legacy', 'inspirational', 'courage'] },
      { word: 'heavyweight', definition: 'A boxer of the heaviest class; metaphorically, a major challenge.', example: 'A heavyweight title is loud; a heavyweight character is quiet and rarer.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cinderella Man', speech: 'Title fight arc', categories: ['courage', 'legacy'] },
      { word: 'hunger', definition: 'A feeling of needing food; strong desire or craving.', example: 'Hunger can mean dinner—or the sharper hunger to reclaim your name.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cinderella Man', speech: 'Depression-era struggle', categories: ['courage', 'humanities'] },
      { word: 'promise', definition: 'An assurance that one will do something; grounds for expectation.', example: 'He fought on a promise to his children that the lights would stay on.', partOfSpeech: 'noun', complexity: 'basic', source: 'Cinderella Man', speech: 'Family and fight', categories: ['legacy', 'inspirational'] },
      { word: 'comeback', definition: 'A return to success after a period of difficulty.', example: 'A true comeback is not nostalgia—it is proof that character outlasted the fall.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Cinderella Man', speech: 'Braddock resolve', categories: ['courage', 'inspirational', 'legacy'] },
    ],
  },
  38: {
    add: [
      { word: 'brother', definition: 'A male sibling; a fellow member bound by loyalty.', example: 'In the cage or out of it, brother is a word that can save you or break you.', partOfSpeech: 'noun', complexity: 'basic', source: 'Warrior', speech: 'Family conflict', categories: ['legacy', 'humanities', 'courage'] },
      { word: 'tap', definition: 'To signal submission in combat sports.', example: 'I’m not a quitter—I don’t tap—until love makes surrender a different kind of strength.', partOfSpeech: 'verb', complexity: 'basic', source: 'Warrior', speech: 'Tournament arc', categories: ['courage', 'humanities'] },
      { word: 'train', definition: 'To teach a skill through practice; to prepare physically.', example: 'We train. That’s it—not a word about anything but the work in front of us.', partOfSpeech: 'verb', complexity: 'basic', source: 'Warrior', speech: 'Training', categories: ['leadership', 'courage'] },
      { word: 'forgive', definition: 'To stop feeling angry or resentful toward someone for an offense.', example: 'To forgive a father is sometimes harder than any round in the cage.', partOfSpeech: 'verb', complexity: 'intermediate', source: 'Warrior', speech: 'Family conflict', categories: ['humanities', 'legacy', 'courage'] },
      { word: 'underdog', definition: 'A competitor thought to have little chance of winning.', example: 'You never had interest in underdogs—until one of them was your son.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Warrior', speech: 'Family conflict', categories: ['courage', 'inspirational', 'legacy'] },
      { word: 'round', definition: 'A segment of a fight; another interval of suffering and will.', example: 'Survive the round you are in—tomorrow’s strategy cannot help tonight’s blood.', partOfSpeech: 'noun', complexity: 'basic', source: 'Warrior', speech: 'Tournament arc', categories: ['courage', 'inspirational'] },
      { word: 'sober', definition: 'Not affected by alcohol; clear-headed and restrained.', example: 'A sober second chance is heavier than a drunken apology.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Warrior', speech: 'Redemption arc', categories: ['humanities', 'courage', 'legacy'] },
      { word: 'love', definition: 'An intense feeling of deep affection; steadfast care.', example: 'Love can sound like a corner man pleading: tap out—it’s okay; I love you.', partOfSpeech: 'noun', complexity: 'basic', source: 'Warrior', speech: 'Climax', categories: ['humanities', 'legacy', 'inspirational'] },
    ],
  },
  27: {
    add: [
      { word: 'power', definition: 'The ability to do something or act in a particular way; influence.', example: 'With great power comes great responsibility—don’t ever forget that.', partOfSpeech: 'noun', complexity: 'basic', source: 'Spider-Man lore', speech: 'Uncle Ben\'s Final Lesson', categories: ['leadership', 'courage', 'legacy'] },
      { word: 'choice', definition: 'An act of selecting between possibilities.', example: 'Power is given; character is choice under pressure.', partOfSpeech: 'noun', complexity: 'intermediate', source: 'Spider-Man lore', speech: 'Uncle Ben\'s Final Lesson', categories: ['humanities', 'courage', 'leadership'] },
      { word: 'ordinary', definition: 'With no special features; normal—until action reveals more.', example: 'Ordinary people become extraordinary when they refuse to look away.', partOfSpeech: 'adjective', complexity: 'intermediate', source: 'Spider-Man lore', speech: 'On responsibility', categories: ['inspirational', 'courage', 'democracy'] },
      { word: 'stand', definition: 'To take up a position of resistance or support.', example: 'I can’t just stand by—accountability begins when watching becomes participating.', partOfSpeech: 'verb', complexity: 'basic', source: 'Hero reflections', speech: 'Peter Parker\'s Reflection on Responsibility', categories: ['courage', 'leadership', 'democracy'] },
    ],
  },
};

/** Speeches to ensure every orator has at least one (public-domain or educational monologue). */
const SPEECH_ADDITIONS = [
  {
    oratorId: 6,
    title: 'What to the Slave Is the Fourth of July?',
    year: 1852,
    description: 'Frederick Douglass’s oration at Rochester, New York—public domain.',
    fullText:
      'Fellow-citizens, pardon me, allow me to ask, why am I called upon to speak here to-day? What have I, or those I represent, to do with your national independence? Are the great principles of political freedom and of natural justice, embodied in that Declaration of Independence, extended to us?\n\nI am not included within the pale of this glorious anniversary! Your high independence only reveals the immeasurable distance between us. The blessings in which you, this day, rejoice, are not enjoyed in common. The rich inheritance of justice, liberty, prosperity and independence, bequeathed by your fathers, is shared by you, not by me.\n\nWhat, to the American slave, is your 4th of July? I answer; a day that reveals to him, more than all other days in the year, the gross injustice and cruelty to which he is the constant victim. To him, your celebration is a sham; your boasted liberty, an unholy license; your national greatness, swelling vanity.\n\nAt a time like this, scorching irony, not convincing argument, is needed. O! had I the ability, and could I reach the nation’s ear, I would, to-day, pour out a fiery stream of biting ridicule, blasting reproach, withering sarcasm, and stern rebuke.\n\nFor it is not light that is needed, but fire; it is not the gentle shower, but thunder. We need the storm, the whirlwind, and the earthquake. The feeling of the nation must be quickened; the conscience of the nation must be roused; the propriety of the nation must be startled; the hypocrisy of the nation must be exposed.\n\nAllow me to say, in conclusion, notwithstanding the dark picture I have this day presented of the state of the nation, I do not despair of this country. There are forces in operation, which must inevitably work the downfall of slavery.',
  },
  {
    oratorId: 12,
    title: 'Inaugural Address (excerpt)',
    year: 1994,
    description: 'Educational excerpt from Nelson Mandela’s inaugural themes of reconciliation and democracy.',
    fullText:
      'We have triumphed in the effort to implant hope in the breasts of the millions of our people. We enter into a covenant that we shall build the society in which all South Africans, both black and white, will be able to walk tall, without any fear in their hearts, assured of their inalienable right to human dignity—a rainbow nation at peace with itself and the world.\n\nWe understand it still that there is no easy road to freedom. We know it well that none of us acting alone can achieve success. We must therefore act together as a united people, for national reconciliation, for nation building, for the birth of a new world.\n\nLet there be justice for all. Let there be peace for all. Let there be work, bread, water and salt for all. Let each know that for each the body, the mind and the soul have been freed to fulfill themselves.\n\nNever, never and never again shall it be that this beautiful land will again experience the oppression of one by another and suffer the indignity of being the skunk of the world. Let freedom reign. The sun shall never set on so glorious a human achievement!',
  },
  {
    oratorId: 13,
    title: 'Quit India (excerpt themes)',
    year: 1942,
    description: 'Educational reconstruction of themes from Gandhi’s Quit India call—nonviolence and swaraj.',
    fullText:
      'I want freedom immediately, this very night, before dawn, if it can be had. Freedom cannot be real if many are still in bondage. Ours is not a movement of hate; ours is a movement of conversion through suffering.\n\nHere is a mantra, a short one, that I give you. You may imprint it on your hearts and let every breath of yours give expression to it. The mantra is: “Do or Die.” We shall either free India or die in the attempt; we shall not live to see the perpetuation of our slavery.\n\nNon-violence is not a cover for cowardice, but it is the supreme virtue of the brave. Exercise of non-violence requires far greater bravery than that of swordsmanship. Cowardice is wholly inconsistent with non-violence.\n\nA non-violent revolution is not a program of seizure of power. It is a program of transformation of relationships, ending in a peaceful transfer of power. The power that is gained by violence is only a more organized form of the same violence.\n\nLet every Indian consider himself to be a free man. He must be prepared to die free rather than live as a slave. Freedom of the mind is the first freedom; without it, no flag is enough.',
  },
  {
    oratorId: 14,
    title: 'The Lady’s Not for Turning (themes)',
    year: 1980,
    description: 'Educational paraphrase of themes from Margaret Thatcher’s conference address on resolve.',
    fullText:
      'To those waiting with bated breath for that favourite media catchphrase, the U-turn, I have only one thing to say: You turn if you want to. The lady’s not for turning.\n\nI am not a consensus politician. I am a conviction politician. Consensus seems to be the process of abandoning all beliefs, principles, values and policies. So it is something in which no one believes and to which no one objects.\n\nWe shall not be diverted from our course. To those who say we must compromise, I say that the middle way is often the wrong way when the country needs a clear direction. Pennies do not fall from heaven—they have to be earned here on earth.\n\nFreedom will destroy itself if it is not exercised with some restraint and some sense of responsibility. The first duty of government is to uphold the law; if it tries to count every star, it will drop the lantern.\n\nI came to office with one deliberate intent: to change Britain from a dependent to a self-reliant society—from a give-it-to-me to a do-it-yourself nation. That is the only way a free people remain free.',
  },
  {
    oratorId: 16,
    title: 'On Courage and Rising',
    year: 1993,
    description: 'Educational tribute monologue in the spirit of Maya Angelou’s themes of resilience and dignity.',
    fullText:
      'You may write me down in history with your bitter, twisted lies. You may trod me in the very dirt—but still, like dust, I’ll rise.\n\nCourage is the most important of all the virtues, because without courage you cannot practice any other virtue consistently. You can practice any virtue erratically, but nothing consistently without courage.\n\nI love to see a young girl go out and grab the world by the lapels. Life loves to be taken by the lapel and told: “I’m with you kid. Let’s go.”\n\nWe may encounter many defeats, but we must not be defeated. It may even be necessary to encounter the defeat, so that we can know who we are.\n\nMy mission in life is not merely to survive, but to thrive; and to do so with some passion, some compassion, some humor, and some style. Try to be a rainbow in someone else’s cloud.\n\nWhen you learn, teach. When you get, give. That is how a people turns memory into momentum.',
  },
  {
    oratorId: 17,
    title: 'Education for All (UN themes)',
    year: 2013,
    description: 'Educational paraphrase of themes from Malala Yousafzai’s advocacy for girls’ education.',
    fullText:
      'Dear brothers and sisters, do remember one thing: Malala day is not my day. Today is the day of every woman, every boy and every girl who have raised their voice for their rights.\n\nThe terrorists thought that they would change our aims and stop our ambitions, but nothing changed in my life except this: weakness, fear and hopelessness died. Strength, power and courage were born.\n\nI am those 66 million girls who are deprived of education. And today I am not raising my voice, it is the voice of those 66 million girls.\n\nOne child, one teacher, one pen and one book can change the world. Education is the only solution. Education first.\n\nWe call upon all governments to ensure free compulsory education all over the world for every child. We call upon all governments to fight against terrorism and violence, to protect children from brutality and harm.\n\nWe realize the importance of light when we see darkness. We realize the importance of our voice when we are silenced. Let us pick up our books and pens. They are our most powerful weapons.',
  },
  {
    oratorId: 31,
    title: 'Keep Moving Forward',
    year: 2006,
    description: 'Educational monologue based on Rocky Balboa’s themes of resilience.',
    fullText:
      'You ain’t gonna hit as hard as life. But it ain’t about how hard you hit. It’s about how hard you can get hit and keep moving forward—how much you can take and keep moving forward. That’s how winning is done!\n\nNow if you know what you’re worth, then go out and get what you’re worth. But you gotta be willing to take the hits, and not pointing fingers saying you ain’t where you wanna be because of him, or her, or anybody!\n\nCowards do that and that ain’t you! You’re better than that!\n\nI’m always gonna love you no matter what. No matter what happens. You’re my son and you’re my blood. You’re the best thing in my life. But until you start believing in yourself, you ain’t gonna have a life.\n\nDon’t be looking at me for answers. The answers are in you. Now get out of here—and go the distance.',
  },
  {
    oratorId: 32,
    title: 'Wax On, Wax Off',
    year: 1984,
    description: 'Educational training monologue in the spirit of Mr. Miyagi’s mentorship.',
    fullText:
      'First learn stand, then learn fly. Nature rule, Daniel-san—not mine.\n\nWax on, right hand. Wax off, left hand. Breathe in through nose, out the mouth. Wax on, wax off. Don’t forget to breathe—very important.\n\nNo such thing as bad student, only bad teacher. Teacher say, student do.\n\nWalk on road, hm? Walk right side, safe. Walk left side, safe. Walk middle, sooner or later get squish just like grape. Here, karate same thing. Either you karate do “yes” or karate do “no.” You karate do “guess so,” squish just like grape.\n\nBetter learn balance. Balance is key. Balance good, karate good. Everything good. Balance bad, better pack up, go home. Understand?\n\nMan who catch fly with chopsticks accomplish anything. Miyagi show you; you believe later.',
  },
  {
    oratorId: 33,
    title: 'I Came to Play for the Irish',
    year: 1993,
    description: 'Educational monologue based on Rudy’s underdog resolve.',
    fullText:
      'You’re five feet nothing, a hundred and nothing, and you have hardly a speck of athletic ability. And you hung in there with the best college football team in the land for two years.\n\nAnd you’re also going to walk out of here with a degree from the University of Notre Dame. In this life, you don’t have to prove nothing to nobody but yourself.\n\nI came here to play football for the Irish. And I’m gonna finish what I started. They can keep me on the scout team. They can keep me off the traveling squad. But they can’t take the dream unless I hand it to them.\n\nIt’s not the size of the dog in the fight—it’s the size of the fight in the dog. Someday you’re gonna look back on this and be proud. Not because it was easy—because you stayed.',
  },
  {
    oratorId: 34,
    title: 'This Is Your Time',
    year: 1980,
    description: 'Educational monologue based on Herb Brooks’s 1980 locker-room themes.',
    fullText:
      'Great moments are born from great opportunity, and that’s what you have here tonight, boys. That’s what you’ve earned here tonight.\n\nOne game: if we played them ten times, they might win nine. But not this game, not tonight. Tonight, we skate with them. Tonight we stay with them, and we shut them down because we can.\n\nTonight, we are the greatest hockey team in the world. You were born to be hockey players—every one of you. And you were meant to be here tonight.\n\nThis is your time. Their time is done. It’s over. I’m sick and tired of hearing about what a great hockey team the Soviets have. Screw ’em. This is your time!\n\nNow go out there and take it!',
  },
  {
    oratorId: 35,
    title: 'Gettysburg and One Team',
    year: 1971,
    description: 'Educational monologue based on Herman Boone’s unity themes in Remember the Titans.',
    fullText:
      'Anybody know what this place is? This is Gettysburg. This is where they fought the Battle of Gettysburg. Fifty thousand men died right here on this field, fighting the same fight that we’re still fighting amongst ourselves today.\n\nThis green field right here was painted red, bubbling with the blood of young boys, smoke and hot lead pouring right through their bodies. Listen to their souls, men.\n\nI killed my brother with malice in my heart. Hatred destroyed my family. You listen. And you take a lesson from the dead. If we don’t come together right now on this hallowed ground, we too will be destroyed—just like they were.\n\nI don’t care if you like each other or not. But you will respect each other. And maybe—I don’t know—maybe we’ll learn to play this game like men.',
  },
  {
    oratorId: 36,
    title: 'The Basket Is Still Ten Feet',
    year: 1952,
    description: 'Educational monologue based on Coach Norman Dale’s fundamentals in Hoosiers.',
    fullText:
      'Forget about the crowds, the size of the school, their fancy uniforms, and remember what got you here. Focus on the fundamentals that got you to this floor.\n\nFive players on the floor functioning as one single unit: team, team, team—no one more important than the other.\n\nMost people would kill to be a part of what you boys have right now. Don’t be afraid to be excellent when the lights are this bright.\n\nYou know, most people would say that a team from a little school like ours has no business being here. But most people would be wrong about a lot of things.\n\nLet’s measure the basket. Still ten feet. They don’t get taller for state finals. We play our game—and we play it together.',
  },
  {
    oratorId: 37,
    title: 'I Have to Believe',
    year: 1935,
    description: 'Educational monologue based on James J. Braddock’s Depression-era resolve.',
    fullText:
      'I have to believe that when things are bad I can change them. That is not optimism as decoration—that is optimism as a work order.\n\nI know what I’m fighting for. Not the noise. Not the headlines. The rent. The milk. The faces at the table that still look at me like I can fix the dark.\n\nThey say boxing is dangerous. You think you’re telling me something? Being broke with a family counting on you—that is dangerous. Getting in the ring with a plan is the safer kind of fear.\n\nA country is great when it helps a man stand up again without taking his name as payment. I’ll take the work. I’ll take the rounds. I’ll take the comeback one honest punch at a time.\n\nDon’t call it a fairy tale. Call it a man who refused to stay down.',
  },
  {
    oratorId: 38,
    title: 'We Train. That’s It.',
    year: 2011,
    description: 'Educational monologue based on Warrior’s themes of discipline and fractured family.',
    fullText:
      'We train. That’s it. I don’t want to hear a word about anything but training. The past can wait outside the gym door until the work is done.\n\nI’m not a quitter. I don’t tap. That sentence has saved me and ruined me in equal measure.\n\nYou want to talk about underdogs? You never had interest in underdogs—but I was your son. That is a different kind of corner to fight out of.\n\nIn the end, it’s not about who hits harder. It’s about who can take it and keep coming when the arms are gone and the pride is loud.\n\nAnd if love ever sounds like a plea between rounds—tap out, it’s okay, I love you—then maybe winning was never only the hand raised at the end.',
  },
];

function nextId(used, start) {
  let id = start;
  while (used.has(id)) id += 1;
  used.add(id);
  return id;
}

function loadJson(file) {
  return JSON.parse(fs.readFileSync(path.join(seedDir, file), 'utf8'));
}

function saveJson(file, data) {
  fs.writeFileSync(path.join(seedDir, file), JSON.stringify(data, null, 2) + '\n', 'utf8');
}

function main() {
  const dicts = loadJson('dictionaries.json');
  const usedWordIds = new Set();
  let wordsUpdated = 0;
  let wordsAdded = 0;
  let examplesFixed = 0;

  // Pass 1: normalize all word files
  const wordFiles = fs.readdirSync(seedDir).filter((f) => f.startsWith('words_') && f.endsWith('.json'));
  for (const file of wordFiles) {
    const words = loadJson(file);
    const before = JSON.stringify(words);
    const next = words.map((w) => {
      usedWordIds.add(w.id);
      const oldEx = w.example;
      const n = normalizeEntry(w);
      if (oldEx !== n.example) examplesFixed += 1;
      return n;
    });
    if (JSON.stringify(next) !== before) {
      saveJson(file, next);
      wordsUpdated += 1;
    }
  }

  // Pass 2: expansions for thin orators
  const fileByOrator = {};
  for (const file of wordFiles) {
    const words = loadJson(file);
    if (words[0]?.oratorId != null) fileByOrator[words[0].oratorId] = file;
  }

  const allExp = { ...EXPANSIONS };
  for (const [oid, pack] of Object.entries(MENTOR_EXPANSIONS)) {
    allExp[oid] = pack;
  }

  for (const [oidStr, pack] of Object.entries(allExp)) {
    const oid = Number(oidStr);
    const file = fileByOrator[oid];
    if (!file) {
      console.warn('No word file for orator', oid);
      continue;
    }
    const words = loadJson(file);
    const existing = new Set(words.map((w) => w.word.toLowerCase()));
    let maxLocal = words.reduce((m, w) => Math.max(m, w.id), oid * 1000);
    const toAdd = (pack.add || []).filter((w) => !existing.has(w.word.toLowerCase()));
    for (const w of toAdd) {
      maxLocal = nextId(usedWordIds, maxLocal + 1);
      words.push(
        normalizeEntry({
          id: maxLocal,
          oratorId: oid,
          ...w,
        }),
      );
      wordsAdded += 1;
    }
    saveJson(file, words);
    fileByOrator[oid] = file;
  }

  // Pass 3: speeches — dedupe + add missing
  let speeches = loadJson('speeches.json');
  const seenTitles = new Set();
  const deduped = [];
  for (const s of speeches) {
    const key = `${s.oratorId}::${(s.title || '').toLowerCase()}`;
    if (seenTitles.has(key)) continue;
    // also collapse near-duplicate Cross of Gold / Obama concession duplicates by normalized title prefix
    const loose = `${s.oratorId}::${(s.title || '').toLowerCase().slice(0, 24)}`;
    if (seenTitles.has(loose) && (s.fullText || '').length < 100) continue;
    seenTitles.add(key);
    seenTitles.add(loose);
    deduped.push(s);
  }
  speeches = deduped;

  const oratorsWithSpeech = new Set(speeches.map((s) => s.oratorId));
  let maxSpeechId = speeches.reduce((m, s) => Math.max(m, s.id), 0);
  let speechesAdded = 0;
  for (const s of SPEECH_ADDITIONS) {
    if (oratorsWithSpeech.has(s.oratorId)) continue;
    maxSpeechId += 1;
    speeches.push({
      id: maxSpeechId,
      oratorId: s.oratorId,
      title: s.title,
      fullText: s.fullText,
      year: s.year,
      description: s.description,
    });
    oratorsWithSpeech.add(s.oratorId);
    speechesAdded += 1;
  }

  // Ensure still-missing orators get a short profile speech from dictionary bio/sample
  for (const d of dicts) {
    if (oratorsWithSpeech.has(d.id)) continue;
    maxSpeechId += 1;
    const body = [d.sampleSpeech, d.bio].filter(Boolean).join('\n\n');
    speeches.push({
      id: maxSpeechId,
      oratorId: d.id,
      title: `${d.name}: On Character and Voice`,
      fullText:
        body ||
        `${d.name} stands in the long tradition of rhetoric: speech as character under pressure, and language as a tool for courage.`,
      year: null,
      description: `Profile speech for ${d.name}, assembled from curated biographical themes.`,
    });
    oratorsWithSpeech.add(d.id);
    speechesAdded += 1;
  }

  saveJson('speeches.json', speeches);

  // Pass 4: update dictionary word counts
  for (const d of dicts) {
    const file = fileByOrator[d.id];
    if (!file) continue;
    const words = loadJson(file);
    d.wordCount = words.length;
  }
  saveJson('dictionaries.json', dicts);

  // Validation summary
  let remainingBadExamples = 0;
  let remainingBadThemes = 0;
  let minWords = Infinity;
  let maxWords = 0;
  for (const file of fs.readdirSync(seedDir).filter((f) => f.startsWith('words_'))) {
    const words = loadJson(file);
    minWords = Math.min(minWords, words.length);
    maxWords = Math.max(maxWords, words.length);
    for (const w of words) {
      if (!containsWord(w.word, w.example)) remainingBadExamples += 1;
      for (const c of w.categories || []) {
        if (!CANONICAL.has(c)) remainingBadThemes += 1;
      }
    }
  }

  const missingSpeech = dicts.filter((d) => !oratorsWithSpeech.has(d.id)).map((d) => d.name);

  console.log(
    JSON.stringify(
      {
        wordFilesTouched: wordsUpdated,
        wordsAdded,
        examplesFixed,
        speechesAfter: speeches.length,
        speechesAdded,
        minWordsPerOrator: minWords,
        maxWordsPerOrator: maxWords,
        remainingBadExamples,
        remainingBadThemes,
        missingSpeech,
      },
      null,
      2,
    ),
  );
}

main();
