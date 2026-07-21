/**
 * Remove hyphenated headwords from seed vocabulary.
 * Prefer closed compounds, spaced open compounds, or drop duplicates.
 */
const fs = require("fs");
const path = require("path");

const seed = path.join(__dirname, "..", "app", "src", "main", "assets", "data", "seed");
const files = fs
  .readdirSync(seed)
  .filter((f) => f.startsWith("words_") && f.endsWith(".json"));

/**
 * Map hyphenated lemma (lowercase) → replacement headword, or null to drop.
 * Intentionally omitted (keep hyphen): walk-on (standard sports compound).
 */
const replacements = new Map([
  ["long-termism", "longtermism"],
  ["day-one", "day one"],
  ["left-side", "left side"],
  ["name-on-the-front", "team identity"],
  ["small-town", "small town"],
  ["non-violence", null], // keep words_mlk nonviolence
  ["self-mastery", "self mastery"],
  ["short-cut", "shortcut"],
  ["faint-hearted", "fainthearted"],
]);

/** Optional example rewrites so validation still finds the lemma. */
const exampleFixes = new Map([
  [
    "longtermism",
    (ex) =>
      ex.replace(/Long-termism/g, "Longtermism").replace(/long-termism/g, "longtermism"),
  ],
  [
    "day one",
    (ex) => ex.replace(/Day-one/g, "Day one").replace(/day-one/g, "day one"),
  ],
  [
    "left side",
    (ex) => ex.replace(/Left-side/g, "Left side").replace(/left-side/g, "left side"),
  ],
  [
    "team identity",
    (ex) =>
      "Team identity means the name on the front of the jersey matters more than the name on the back.",
  ],
  [
    "small town",
    (ex) =>
      ex.replace(/small-town/g, "small town").replace(/Small-town/g, "Small town"),
  ],
  [
    "self mastery",
    (ex) =>
      ex.replace(/Self-mastery/g, "Self mastery").replace(/self-mastery/g, "self mastery"),
  ],
  [
    "shortcut",
    (ex) =>
      ex
        .replace(/short-cuts/g, "shortcuts")
        .replace(/short-cut/g, "shortcut")
        .replace(/Short-cuts/g, "Shortcuts"),
  ],
  [
    "fainthearted",
    (ex) =>
      ex
        .replace(/faint-hearted/g, "fainthearted")
        .replace(/Faint-hearted/g, "Fainthearted")
        .replace(/faint-heart/g, "faintheart"),
  ],
]);

const existing = new Set();
for (const f of files) {
  for (const w of JSON.parse(fs.readFileSync(path.join(seed, f), "utf8"))) {
    existing.add(String(w.word).toLowerCase());
  }
}

const report = { renamed: [], removed: [], leftover: [] };

for (const f of files) {
  const p = path.join(seed, f);
  const words = JSON.parse(fs.readFileSync(p, "utf8"));
  const next = [];
  let changed = false;

  for (const w of words) {
    const raw = String(w.word);
    if (!raw.includes("-")) {
      next.push(w);
      continue;
    }

    const key = raw.toLowerCase();
    if (!replacements.has(key)) {
      report.leftover.push({ file: f, id: w.id, word: raw });
      next.push(w);
      continue;
    }

    const replacement = replacements.get(key);
    if (replacement == null) {
      report.removed.push({ file: f, id: w.id, from: raw, reason: "duplicate of closed form" });
      changed = true;
      continue;
    }

    if (
      existing.has(replacement.toLowerCase()) &&
      replacement.toLowerCase() !== key
    ) {
      report.removed.push({
        file: f,
        id: w.id,
        from: raw,
        reason: `target exists (${replacement})`,
      });
      changed = true;
      continue;
    }

    const old = w.word;
    w.word = replacement;
    if (exampleFixes.has(replacement)) {
      w.example = exampleFixes.get(replacement)(w.example || "");
    }
    // light definition cleanup of hyphen forms
    w.definition = String(w.definition || "")
      .replace(/long-termism/gi, "longtermism")
      .replace(/day-one/gi, "day one")
      .replace(/self-mastery/gi, "self mastery")
      .replace(/short-cut/gi, "shortcut")
      .replace(/faint-hearted/gi, "fainthearted")
      .replace(/non-violence/gi, "nonviolence")
      .replace(/small-town/gi, "small town");

    report.renamed.push({ file: f, id: w.id, from: old, to: replacement });
    existing.add(replacement.toLowerCase());
    next.push(w);
    changed = true;
  }

  if (changed) {
    fs.writeFileSync(p, JSON.stringify(next, null, 2) + "\n");
  }
}

/** Hyphenated lemmas kept on purpose (not closed or spaced). */
const keepHyphen = new Set(["walk-on"]);

// Final scan
for (const f of files) {
  for (const w of JSON.parse(fs.readFileSync(path.join(seed, f), "utf8"))) {
    const lemma = String(w.word);
    if (lemma.includes("-") && !keepHyphen.has(lemma.toLowerCase())) {
      report.leftover.push({ file: f, id: w.id, word: w.word });
    }
  }
}

console.log("RENAMED", report.renamed.length);
report.renamed.forEach((r) => console.log(`  ${r.file} #${r.id}: ${r.from} -> ${r.to}`));
console.log("REMOVED", report.removed.length);
report.removed.forEach((r) => console.log(`  ${r.file} #${r.id}: ${r.from} (${r.reason})`));
console.log("LEFTOVER", report.leftover.length);
report.leftover.forEach((r) => console.log(`  ${r.file} #${r.id}: ${r.word}`));
