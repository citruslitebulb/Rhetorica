const fs = require("fs/promises");
const path = require("path");

const seedDir = path.join(__dirname, "..", "app", "src", "main", "assets", "data", "seed");
const dictionariesPath = path.join(seedDir, "dictionaries.json");

const oratorFiles = {
  1: "words_demosthenes.json",
  2: "words_cicero.json",
  3: "words_pericles.json",
  4: "words_isocrates.json",
  5: "words_lincoln.json",
  6: "words_douglass.json",
  7: "words_bryan.json",
  8: "words_churchill.json",
  9: "words_mlk.json",
  10: "words_jfk.json",
  11: "words_fdr.json",
  12: "words_mandela.json",
  13: "words_gandhi.json",
  14: "words_thatcher.json",
  15: "words_obama.json",
  16: "words_angelou.json",
  17: "words_malala.json",
  18: "words_shakespeare.json",
};

const oratorCategory = {
  1: "ancient",
  2: "ancient",
  3: "ancient",
  4: "ancient",
  5: "nineteenth",
  6: "nineteenth",
  7: "nineteenth",
  8: "twentieth",
  9: "twentieth",
  10: "twentieth",
  11: "twentieth",
  12: "twentieth",
  13: "twentieth",
  14: "twentieth",
  15: "modern",
  16: "modern",
  17: "modern",
  18: "literary",
};

const parsePool = (entries) =>
  entries.map((entry) => {
    const [word, complexity] = entry.split("|");
    return { word, complexity };
  });

const genericPool = parsePool([
  "advocate|intermediate",
  "alliance|intermediate",
  "amplify|intermediate",
  "adapt|easy",
  "balance|easy",
  "catalyst|advanced",
  "challenge|easy",
  "character|easy",
  "choice|easy",
  "clarity|easy",
  "coalition|intermediate",
  "community|easy",
  "consensus|intermediate",
  "conviction|intermediate",
  "courage|easy",
  "credible|intermediate",
  "debate|easy",
  "dialogue|intermediate",
  "dignity|intermediate",
  "discourse|intermediate",
  "empathy|intermediate",
  "endeavor|intermediate",
  "endure|easy",
  "equity|intermediate",
  "ethics|intermediate",
  "evidence|intermediate",
  "fairness|easy",
  "fortitude|advanced",
  "freedom|easy",
  "future|easy",
  "govern|intermediate",
  "guide|easy",
  "harmony|easy",
  "heal|easy",
  "humility|intermediate",
  "impact|easy",
  "inclusion|intermediate",
  "influence|intermediate",
  "inspire|easy",
  "integrity|intermediate",
  "justice|easy",
  "legacy|intermediate",
  "liberty|intermediate",
  "listen|easy",
  "mandate|advanced",
  "momentum|intermediate",
  "moral|easy",
  "nuance|advanced",
  "opportunity|easy",
  "optimism|intermediate",
  "peace|easy",
  "principle|intermediate",
  "progress|intermediate",
  "purpose|easy",
  "reason|easy",
  "reform|intermediate",
  "resilience|intermediate",
  "respect|easy",
  "restore|easy",
  "rhetoric|advanced",
  "service|easy",
  "solidarity|intermediate",
  "strategy|intermediate",
  "testimony|intermediate",
  "unity|easy",
  "urgent|intermediate",
  "vision|intermediate",
  "wisdom|intermediate",
  "civility|intermediate",
  "mission|easy",
]);

const categoryPools = {
  ancient: parsePool([
    "agora|advanced",
    "anarchy|advanced",
    "arbitration|intermediate",
    "assembly|easy",
    "civic|intermediate",
    "decree|intermediate",
    "deliberation|intermediate",
    "dialectic|advanced",
    "diplomacy|intermediate",
    "edict|advanced",
    "envoy|intermediate",
    "forum|intermediate",
    "guardian|easy",
    "hegemony|advanced",
    "jurisprudence|advanced",
    "legitimacy|advanced",
    "magistrate|advanced",
    "mediation|intermediate",
    "monarchy|intermediate",
    "oligarchy|advanced",
    "oration|intermediate",
    "panegyric|advanced",
    "parable|intermediate",
    "patrimony|advanced",
    "philosopher|intermediate",
    "plebiscite|advanced",
    "polity|advanced",
    "precept|advanced",
    "prudence|advanced",
    "ratify|intermediate",
    "senate|intermediate",
    "sovereignty|advanced",
    "stoic|advanced",
    "symposium|advanced",
    "tribunal|intermediate",
    "truce|intermediate",
    "veneration|advanced",
    "virtuous|intermediate",
    "zeal|intermediate",
    "counsel|intermediate",
  ]),
  nineteenth: parsePool([
    "abolition|intermediate",
    "agrarian|advanced",
    "autonomy|intermediate",
    "bondage|intermediate",
    "citizenship|intermediate",
    "commerce|intermediate",
    "conciliation|advanced",
    "constitutional|advanced",
    "dissent|intermediate",
    "emancipation|advanced",
    "franchise|intermediate",
    "frontier|intermediate",
    "grievance|intermediate",
    "homestead|intermediate",
    "industry|intermediate",
    "labor|easy",
    "livelihood|intermediate",
    "manifesto|advanced",
    "moderation|intermediate",
    "ordinance|advanced",
    "petition|intermediate",
    "pluralism|advanced",
    "populism|advanced",
    "posterity|advanced",
    "proclamation|advanced",
    "prosperity|intermediate",
    "reconstruction|advanced",
    "referendum|advanced",
    "representation|intermediate",
    "reformer|intermediate",
    "suffrage|advanced",
    "tariff|advanced",
    "tenement|intermediate",
    "toil|intermediate",
    "union|intermediate",
    "uplift|intermediate",
    "welfare|intermediate",
    "commonwealth|advanced",
    "federation|advanced",
    "accountability|intermediate",
  ]),
  twentieth: parsePool([
    "armistice|advanced",
    "atrocity|intermediate",
    "ceasefire|intermediate",
    "detente|advanced",
    "diplomat|intermediate",
    "fascism|advanced",
    "fortify|intermediate",
    "genocide|advanced",
    "humanitarian|advanced",
    "ideology|advanced",
    "infamy|advanced",
    "insurgency|advanced",
    "liberation|intermediate",
    "mobilize|intermediate",
    "morale|intermediate",
    "occupation|intermediate",
    "oppression|intermediate",
    "patriotism|intermediate",
    "policy|intermediate",
    "precedent|advanced",
    "propaganda|advanced",
    "reconciliation|advanced",
    "recovery|intermediate",
    "refuge|advanced",
    "sanction|intermediate",
    "security|intermediate",
    "statesman|advanced",
    "summit|intermediate",
    "surrender|intermediate",
    "totalitarian|advanced",
    "treaty|intermediate",
    "turmoil|intermediate",
    "upheaval|advanced",
    "vigilance|advanced",
    "wartime|intermediate",
    "unification|advanced",
    "reprisal|advanced",
    "stability|intermediate",
    "resistance|intermediate",
    "deterrence|advanced",
  ]),
  modern: parsePool([
    "access|easy",
    "activism|intermediate",
    "agency|intermediate",
    "advocacy|intermediate",
    "belonging|intermediate",
    "collaborate|intermediate",
    "compassion|easy",
    "creative|easy",
    "digital|easy",
    "education|easy",
    "expression|intermediate",
    "global|easy",
    "healing|easy",
    "identity|intermediate",
    "imagination|intermediate",
    "innovation|intermediate",
    "intersection|advanced",
    "literacy|intermediate",
    "mentor|intermediate",
    "narrative|intermediate",
    "perseverance|intermediate",
    "platform|intermediate",
    "possibility|intermediate",
    "renewal|intermediate",
    "representation|intermediate",
    "scholarship|advanced",
    "confidence|intermediate",
    "storytelling|intermediate",
    "sustainable|advanced",
    "transform|intermediate",
    "transparency|advanced",
    "uplift|intermediate",
    "visibility|intermediate",
    "voice|easy",
    "wellness|intermediate",
    "empower|intermediate",
    "curiosity|intermediate",
    "openness|intermediate",
    "equitable|advanced",
    "participation|intermediate",
  ]),
  literary: parsePool([
    "allegory|advanced",
    "ardor|advanced",
    "cadence|intermediate",
    "couplet|advanced",
    "dramatic|intermediate",
    "eloquence|advanced",
    "flourish|intermediate",
    "foreswear|advanced",
    "grandeur|advanced",
    "hallowed|advanced",
    "lament|intermediate",
    "lyric|intermediate",
    "melancholy|advanced",
    "metaphor|intermediate",
    "mirth|intermediate",
    "muse|intermediate",
    "narrator|intermediate",
    "ornate|advanced",
    "parable|intermediate",
    "parchment|advanced",
    "poetic|intermediate",
    "portent|advanced",
    "prologue|advanced",
    "prose|intermediate",
    "quill|advanced",
    "radiance|intermediate",
    "rapture|advanced",
    "refrain|intermediate",
    "revelry|advanced",
    "rhyme|easy",
    "soliloquy|advanced",
    "sonnet|advanced",
    "stanza|advanced",
    "tempest|advanced",
    "theatrical|intermediate",
    "timeless|intermediate",
    "tragedy|intermediate",
    "wit|intermediate",
    "wondrous|advanced",
    "yearning|intermediate",
  ]),
};

const advancedExistingWords = new Set([
  "forsooth",
  "hither",
  "thither",
  "wherefore",
  "verily",
  "betwixt",
  "methinks",
  "philippic",
  "demagogue",
  "sophistry",
  "rhetorician",
  "satyagraha",
  "swaraj",
  "apartheid",
  "reconciliation",
  "obsequious",
  "magnanimity",
  "convivial",
  "chiaroscuro",
  "soliloquy",
  "bequeath",
  "countenance",
  "portentous",
  "quintessence",
  "circumspection",
  "perspicacity",
  "magniloquence",
  "hegemony",
  "jurisprudence",
  "panegyric",
  "oligarchy",
  "plebiscite",
  "polity",
  "prudence",
  "sovereignty",
  "symposium",
  "commonwealth",
  "detente",
  "insurgency",
  "reprisal",
  "deterrence",
  "intersectional",
]);

const easyExistingWords = new Set([
  "hope",
  "change",
  "rise",
  "dream",
  "unity",
  "peace",
  "future",
  "guide",
  "voice",
  "service",
  "honor",
  "fear",
  "bear",
  "choice",
  "courage",
  "truth",
  "duty",
  "people",
  "faith",
  "love",
  "freedom",
  "education",
  "vision",
  "impact",
  "inspire",
  "community",
  "justice",
]);

function classifyExisting(word) {
  const normalized = normalizeWord(word);
  const wordLength = word.length;

  // Check against curated word lists first
  if (advancedExistingWords.has(normalized)) return "advanced";
  if (easyExistingWords.has(normalized)) return "easy";

  // Fallback: classify based on word characteristics
  // Very short words (≤4 letters) are typically easy
  if (wordLength <= 4) return "easy";

  // Very long words (≥12 letters) are typically advanced
  if (wordLength >= 12) return "advanced";

  // Words with common prefixes/suffixes for complex concepts
  const complexPatterns = /^(anti|counter|de|dis|in|inter|micro|multi|non|over|post|pre|pro|re|semi|sub|super|trans|ultra|un|under)/i;
  if (complexPatterns.test(word) && wordLength >= 8) return "advanced";

  // Default to intermediate for words not in curated lists
  return "intermediate";
}

function normalizeWord(word) {
  return word.toLowerCase().replace(/[^a-z]/g, "");
}

function sanitizeDefinition(definition) {
  return definition.replace(/\s+/g, " ").replace(/"/g, "'").trim();
}

function titleCase(word) {
  return word.charAt(0).toUpperCase() + word.slice(1);
}

function buildFallbackExample(word, partOfSpeech) {
  const lower = word.toLowerCase();
  if (partOfSpeech.includes("verb")) {
    return `The speaker tried to ${lower} the audience with a calm but forceful appeal.`;
  }
  if (partOfSpeech.includes("adjective")) {
    return `Her ${lower} tone gave the address an unmistakable sense of purpose.`;
  }
  if (partOfSpeech.includes("adverb")) {
    return `He spoke ${lower} when the room needed clarity more than noise.`;
  }
  return `The speech returned to the idea of ${lower} as a guiding principle.`;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function buildFallbackDefinition(word) {
  return `A useful English word connected to ${word.toLowerCase()} in public speech or writing.`;
}

async function fetchWordData(word, attempt = 1) {
  const url = `https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(word)}`;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 10000);

  try {
    const response = await fetch(url, { signal: controller.signal });
    clearTimeout(timeoutId);

    if (response.status === 429 && attempt <= 5) {
      await sleep(900 * attempt);
      return fetchWordData(word, attempt + 1);
    }

    if (!response.ok) {
      console.warn(`API returned ${response.status} for "${word}", using fallback`);
      const fallbackPartOfSpeech = "noun";
      return {
        word: titleCase(word),
        partOfSpeech: fallbackPartOfSpeech,
        definition: buildFallbackDefinition(word),
        example: buildFallbackExample(word, fallbackPartOfSpeech),
      };
    }

    const payload = await response.json();

    // Validate response structure
    if (!Array.isArray(payload) || payload.length === 0) {
      console.warn(`Invalid API response structure for "${word}", using fallback`);
      const fallbackPartOfSpeech = "noun";
      return {
        word: titleCase(word),
        partOfSpeech: fallbackPartOfSpeech,
        definition: buildFallbackDefinition(word),
        example: buildFallbackExample(word, fallbackPartOfSpeech),
      };
    }

    const entry = payload[0];
    if (!entry || !Array.isArray(entry.meanings) || entry.meanings.length === 0) {
      console.warn(`No meanings found for "${word}", using fallback`);
      const fallbackPartOfSpeech = "noun";
      return {
        word: titleCase(word),
        partOfSpeech: fallbackPartOfSpeech,
        definition: buildFallbackDefinition(word),
        example: buildFallbackExample(word, fallbackPartOfSpeech),
      };
    }

    const meaning = entry.meanings?.find((item) => item.definitions?.length) ?? entry.meanings?.[0];
    const definitionEntry = meaning?.definitions?.find((item) => item.definition) ?? meaning?.definitions?.[0];
    const partOfSpeech = meaning?.partOfSpeech ?? "noun";
    const definition = sanitizeDefinition(definitionEntry?.definition ?? buildFallbackDefinition(word));
    const example = sanitizeDefinition(definitionEntry?.example ?? buildFallbackExample(word, partOfSpeech));
    return {
      word: titleCase(word),
      partOfSpeech,
      definition,
      example,
    };
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') {
      console.warn(`Request timeout for "${word}", using fallback`);
    } else {
      console.warn(`Network error fetching "${word}": ${error.message}, using fallback`);
    }
    const fallbackPartOfSpeech = "noun";
    return {
      word: titleCase(word),
      partOfSpeech: fallbackPartOfSpeech,
      definition: buildFallbackDefinition(word),
      example: buildFallbackExample(word, fallbackPartOfSpeech),
    };
  }
}

async function fetchAllWordData(words) {
  const results = new Map();
  for (const word of words) {
    const data = await fetchWordData(word);
    results.set(normalizeWord(word), data);
    await sleep(325);
  }
  return results;
}

async function main() {
  const dictionaries = JSON.parse(await fs.readFile(dictionariesPath, "utf8"));

  const wordFiles = await Promise.all(
    Object.entries(oratorFiles).map(async ([oratorId, fileName]) => {
      const filePath = path.join(seedDir, fileName);
      const words = JSON.parse(await fs.readFile(filePath, "utf8"));
      return {
        oratorId: Number(oratorId),
        fileName,
        filePath,
        words,
      };
    }),
  );

  let nextId = Math.max(
    ...wordFiles.flatMap((item) => item.words.map((word) => word.id)),
  ) + 1;

  const lookupWords = new Set();
  for (const pool of [genericPool, ...Object.values(categoryPools)]) {
    for (const entry of pool) {
      lookupWords.add(entry.word);
    }
  }

  const dictionaryData = await fetchAllWordData([...lookupWords]);

  for (const wordFile of wordFiles) {
    const existingWords = wordFile.words.map((entry) => ({
      ...entry,
      complexity: entry.complexity ?? classifyExisting(entry.word),
    }));

    const existingSet = new Set(existingWords.map((entry) => normalizeWord(entry.word)));
    const category = oratorCategory[wordFile.oratorId];
    const candidatePools = [...categoryPools[category], ...genericPool];
    const additions = [];

    for (const candidate of candidatePools) {
      const normalized = normalizeWord(candidate.word);
      if (existingSet.has(normalized)) continue;

      const sourceData = dictionaryData.get(normalized);
      if (!sourceData) continue;

      additions.push({
        id: nextId++,
        word: sourceData.word,
        definition: sourceData.definition,
        example: sourceData.example,
        partOfSpeech: sourceData.partOfSpeech,
        oratorId: wordFile.oratorId,
        complexity: candidate.complexity,
      });
      existingSet.add(normalized);

      if (existingWords.length + additions.length >= 100) {
        break;
      }
    }

    if (existingWords.length + additions.length < 100) {
      throw new Error(
        `${wordFile.fileName} only reached ${existingWords.length + additions.length} entries.`,
      );
    }

    const output = [...existingWords, ...additions].slice(0, 100);
    await fs.writeFile(wordFile.filePath, `${JSON.stringify(output, null, 2)}\n`, "utf8");
  }

  const updatedDictionaries = dictionaries.map((dictionary) => ({
    ...dictionary,
    wordCount: 100,
  }));
  await fs.writeFile(dictionariesPath, `${JSON.stringify(updatedDictionaries, null, 2)}\n`, "utf8");

  console.log("Expanded all seed vocabularies to 100 words per orator.");
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
