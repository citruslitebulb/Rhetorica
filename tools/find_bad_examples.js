const fs = require('fs');
const path = require('path');

const seed = path.join('app', 'src', 'main', 'assets', 'data', 'seed');
const files = fs.readdirSync(seed).filter((f) => f.startsWith('words_') && f.endsWith('.json'));

function containsWord(word, example) {
  const lowerWord = (word || '').toLowerCase();
  const lowerExample = (example || '').toLowerCase();
  if (!lowerWord || !lowerExample) return false;
  return (
    lowerExample.includes(lowerWord) ||
    lowerExample.includes(lowerWord + 's') ||
    lowerExample.includes(lowerWord + 'd') ||
    lowerExample.includes(lowerWord + 'ing') ||
    lowerExample.includes(lowerWord + 'ed')
  );
}

const failures = [];
for (const f of files) {
  const words = JSON.parse(fs.readFileSync(path.join(seed, f), 'utf8'));
  for (const entry of words) {
    if (!containsWord(entry.word, entry.example)) {
      failures.push({
        file: f,
        id: entry.id,
        word: entry.word,
        example: entry.example,
        speech: entry.speech || null,
        source: entry.source || null,
      });
    }
  }
}

console.log(JSON.stringify({ count: failures.length, failures }, null, 2));
