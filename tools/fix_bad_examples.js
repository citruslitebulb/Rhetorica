/**
 * Rewrites seed examples that do not contain their target word.
 * Usage (from repo root): node tools/fix_bad_examples.js
 *
 * Keys are word entry ids. After running, bump SeedDataLoader.SEED_VERSION
 * if you want installed apps to re-import automatically.
 */
const fs = require('fs');
const path = require('path');

const seedDir = path.join('app', 'src', 'main', 'assets', 'data', 'seed');

/** @type {Record<number, string>} */
const fixes = {
  // Marcus Aurelius
  28002:
    'Seek tranquility of mind: you have power over your judgment of events, not over the events themselves.',
  28003:
    'Waste no more time arguing about virtue—embody it. Be the good person you describe.',
  28004:
    'Suspend hasty judgment: everything we hear is an opinion, not a fact; everything we see is a perspective, not the truth.',
  28006:
    'Resilience is choosing not to be harmed—and so not feeling harmed; refuse the injury, and you have not been injured.',
  28009:
    'Practice acceptance of the things to which fate binds you, and love the people with whom fate brings you together.',
  28010:
    'Fear not mortality itself, but never beginning to live while you still draw breath.',
  28012:
    'Act with urgency: do not live as if you had ten thousand years; death hangs over you—while it is in your power, be good.',

  // Jeff Bezos
  21003:
    'Minimize regret: in the end we are our choices, so build yourself a story you will not wish to rewrite.',
  21004:
    'Long-termism asks what we will do with our gifts—and that answer will define us more than any quarter’s results.',
  21005:
    'Invention is hard because gifts are easy; choices demand courage, and great companies choose to invent.',
  21007:
    'Ownership means thinking long term and refusing to sacrifice long-term value for short-term results.',
  21009:
    'Customer obsession treats guests as invited to a party where we are the hosts—our job is their delight every day.',

  // Boone (Remember the Titans)
  35001:
    'We will pursue perfection in every aspect of the game: drop a pass, you run a mile—no excuses.',
  35002:
    'Without unity on this hallowed ground, we too will be destroyed, just as they were.',
  35006:
    'Teamwork shouts from both sides of the field: left side, strong side—one team, one purpose.',

  // James Braddock
  37001:
    'Resilience is believing that when things are bad, you can still change them.',
  37002:
    'A true comeback begins when you know what you are fighting for—and refuse to stay down.',
  37003:
    'Dignity insists we live in a country great enough to help a man when he is in trouble.',
  37004:
    'Perseverance answers danger with resolve: you think boxing is dangerous? So is giving up.',
  37005:
    'Family made him everybody’s hope, the kids’ hero, and the champion of one heart above all.',
  37006:
    'Hope is the belief that when things are bad, you can still change them.',
  37008:
    'Grit is knowing what you are fighting for when the odds say walk away.',

  // Sergey Brin
  25001:
    'Innovation often finds that solving big problems is easier than solving little ones.',
  25003:
    'Our mission is a perfect search engine—one that understands exactly what you mean and gives back exactly what you want.',
  25007:
    'Every experiment multiplies attempts: the only path to success is to allow lots of failures.',
  25008:
    'Impact asks more than “don’t be evil”—we must actively try to be good.',

  // Herb Brooks (Miracle)
  34002:
    'Belief says this is your time; their time is done—it’s over.',
  34003:
    'Destiny put you here: you were born to be hockey players, and you were meant to be here tonight.',
  34004:
    'Play the underdog’s game: if we played them ten times they might win nine—but not this game, not tonight.',
  34005:
    'Teamwork means tonight we skate with them, stay with them, and shut them down because we can.',
  34007:
    'Conditioning closes the gap when you don’t have enough talent to win on talent alone.',

  // Mickey Conlon / Warrior
  38001:
    'Redemption can sound like love in the corner: tap out, Tommy—it’s okay; I love you.',
  38002:
    'Discipline is the rule of the room: we train—that’s it; not a word about anything but training.',
  38003:
    'Perseverance refuses the mat: I’m not a quitter; I don’t tap.',
  38004:
    'Forgiveness can be a plea from the ropes: tap out—it’s okay; I love you, Tommy.',
  38005:
    'Grit says I’m not a quitter; I don’t tap.',
  38006:
    'Family cuts deepest: you never cared for underdogs, but I was your son.',
  38007:
    'Endurance decides the fight: not who hits harder, but who can take it and keep coming.',
  38008:
    'Pain has a dark humor: I think I liked you better when you were a drunk.',

  // Dale (Hoosiers)
  36003:
    'Teamwork is five players on the floor as one unit—team, team, team—no one more important than the other.',
  36004:
    'The underdog forgets the crowds, the school size, the fancy uniforms, and remembers what got them here.',
  36005:
    'Discipline designs practices not for your enjoyment, but for your readiness when the lights come on.',

  // Larry Ellison
  26002:
    'Superiority is the aim: building the most advanced technology not to follow, but to lead.',
  26004:
    'Enterprise lives in the database—the heart of everything—and we will make it the most powerful heart in the industry.',
  26005:
    'Dominance goes to those who learn faster than anyone else.',
  26006:
    'Agility belongs to people who change industries by learning faster than the environment around them.',
  26007:
    'Execution rejects the incremental: we do what’s next; we ship; we deliver.',
  26008:
    'Leadership builds the most advanced technology in the world—not to follow, but to lead.',

  // Gandalf
  29001:
    'Destiny is not only what happens to us; all we have to decide is what to do with the time that is given us.',
  29005:
    'Grief need not be denied: I will not say do not weep, for not all tears are an evil.',
  29007:
    'Hope remains even when the world is full of peril and many dark places—still there is much that is fair.',
  29008:
    'Agency is the choice before us: all we have to decide is what to do with the time that is given us.',

  // Bill Gates
  20002:
    'I left Harvard unaware of the world’s disparity—the appalling gaps of health and wealth I would later work to close.',
  20003:
    'Expectation follows gift: from those to whom much is given, much is expected.',
  20004:
    'Philanthropy asks what those of us with talent, privilege, and opportunity will give back.',
  20008:
    'Ending preventable suffering means finding breakthroughs so the poorest people can live better lives.',

  // Steve Jobs
  19002:
    'Trust intuition: your heart and gut somehow already know what you truly want to become; everything else is secondary.',
  19003:
    'Authenticity refuses a borrowed life—your time is limited, so don’t waste it living someone else’s.',
  19004:
    'Legacy is the wish he carried for himself and now offers graduates beginning anew.',

  // Mr. Miyagi
  32002:
    'Discipline runs both ways: no such thing as bad student, only bad teacher—teacher say, student do.',
  32003:
    'Perseverance looks small until it isn’t: man who catch fly with chopstick can accomplish anything.',
  32004:
    'Mentorship orders the path: first learn stand, then learn fly—nature’s rule, Daniel-san, not mine.',
  32005:
    'Focus can look like catching a fly with chopsticks—and the man who can do that can accomplish anything.',
  32006:
    'Respect the road: walk right side, safe; walk left side, safe; walk middle, sooner or later get squish like grape.',
  32007:
    'Wisdom from inside you is always the right one.',
  32008:
    'Patience learns balance first—balance is key.',

  // Elon Musk
  22004:
    'First principles mean boiling things down to fundamental truths and then reasoning up from there.',
  22008:
    'Optimism is getting up thinking the future will be better—that makes it a bright day; otherwise it is not.',
  22009:
    'Sustainability sits with the internet and multi-planetary life among the forces most likely to shape humanity’s future.',
  22010:
    'Mars colonization needs a rocketry breakthrough: rapid and complete reusability if humanity is to become multi-planetary.',

  // Larry Page
  24002:
    'We reject the merely incremental: most companies improve ten percent; we aim for something far bolder.',
  24003:
    'Additionality means doing something that wouldn’t happen unless you are actually doing it.',
  24004:
    'Ambition admits we are at maybe one percent of what is possible—still moving slowly relative to the opportunity.',
  24007:
    'Transformative products and technologies are both our opportunity and our responsibility.',
  24008:
    'Foundational work imagines and creates the future, not merely optimizes the present.',

  // Rocky
  31002:
    'Perseverance is going one more round when you don’t think you can—that’s what makes all the difference.',
  31003:
    'Resilience isn’t how hard you hit; it’s how hard you can get hit and keep moving forward.',
  31004:
    'Grit faces a mean world: nobody hits as hard as life, but you keep moving forward anyway.',
  31005:
    'Heart isn’t about how hard you hit—it’s about how hard you can get hit and keep moving forward.',
  31006:
    'The underdog may not beat him, but can go the distance—and know for the first time he isn’t just another bum.',
  31007:
    'Adversity fills a mean, nasty world; it doesn’t care how tough you are—so you move forward anyway.',

  // Rudy
  33001:
    'The underdog remembers: it’s not the size of the dog in the fight, it’s the size of the fight in the dog.',
  33002:
    'Perseverance hung in when you were five feet nothing, a hundred and nothing, with hardly a speck of athletic ability.',
  33004:
    'Determination says I came here to play football for the Irish, and I’m going to finish what I started.',
  33005:
    'Heart, not size, wins the fight in the dog.',
  33006:
    'Resolve finishes what it started: I came to play for the Irish, and I will.',
  33007:
    'Pride waits down the road: someday you’ll look back and be proud of yourself.',
  33008:
    'A walk-on can still leave with a degree from the University of Notre Dame—and a story earned the hard way.',

  // Uncle Ben / mentor figures
  27003:
    'Power is a burden and a gift most people will never understand—and with that power comes responsibility.',
  27005:
    'Destiny still leaves you a choice: all we have to decide is what to do with the time that is given us.',
  27006:
    'Duty is simple to say and hard to live: with great power comes great responsibility—don’t ever forget that.',
  27007:
    'Sacrifice puts the needs of the many above the needs of the few, or the one.',
  27008:
    'Integrity is not who I am underneath, but what I do that defines me.',
  27009:
    'Heroism can be a quiet stand: I am no man—and still I will not yield.',
  27010:
    'Accountability refuses the sideline: I can’t just watch bad things happen; I have to do something; I have to be better.',
  27011:
    'Mentorship names the gift and the weight: you have a power most will never understand, and with it responsibility.',
  27012:
    'Consequence follows power: if you can do good for others, you have a moral obligation to do those things.',

  // Yoda
  30001:
    'Commitment leaves no half-measure: do or do not—there is no try.',
  30004:
    'Attachment is the path to fear; train yourself to let go of everything you fear to lose.',
  30006:
    'Patience is not delay dressed as virtue—do or do not; there is no try.',
  30007:
    'Humility knows size matters not: look at me—judge me by my size, do you?',
  30008:
    'Detachment is the Jedi’s hard lesson: train yourself to let go of everything you fear to lose.',

  // Mark Zuckerberg
  23003:
    'Idealism is good fuel—but be prepared to be misunderstood; anyone with a big vision will be.',
  23004:
    'Community change starts local; even global shifts begin small—with people like us.',
};

function containsWord(word, example) {
  const w = (word || '').toLowerCase();
  const ex = (example || '').toLowerCase();
  if (!w || !ex) return false;
  return (
    ex.includes(w) ||
    ex.includes(w + 's') ||
    ex.includes(w + 'd') ||
    ex.includes(w + 'ing') ||
    ex.includes(w + 'ed')
  );
}

const files = fs.readdirSync(seedDir).filter((f) => f.startsWith('words_') && f.endsWith('.json'));
let updated = 0;
let stillBad = [];

for (const file of files) {
  const full = path.join(seedDir, file);
  const words = JSON.parse(fs.readFileSync(full, 'utf8'));
  let changed = false;

  for (const entry of words) {
    if (fixes[entry.id] != null) {
      entry.example = fixes[entry.id];
      changed = true;
      updated++;
    }
  }

  if (changed) {
    fs.writeFileSync(full, JSON.stringify(words, null, 2) + '\n', 'utf8');
    console.log('Updated', file);
  }
}

// Verify
for (const file of files) {
  const words = JSON.parse(fs.readFileSync(path.join(seedDir, file), 'utf8'));
  for (const entry of words) {
    if (!containsWord(entry.word, entry.example)) {
      stillBad.push({ file, id: entry.id, word: entry.word, example: entry.example });
    }
  }
}

console.log(`Applied ${updated} example rewrites.`);
console.log(`Remaining failures: ${stillBad.length}`);
if (stillBad.length) {
  console.log(JSON.stringify(stillBad, null, 2));
  process.exitCode = 1;
}
