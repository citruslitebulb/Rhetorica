/**
 * Convert plural headwords in seed words_*.json to singular lemmas.
 * Drops plural entries when the singular already exists elsewhere.
 * Exceptions: physics, politics, -ness abstracts, -ics fields, etc.
 */
const fs = require("fs");
const path = require("path");

const seed = path.join(__dirname, "..", "app", "src", "main", "assets", "data", "seed");
const files = fs
  .readdirSync(seed)
  .filter((f) => f.startsWith("words_") && f.endsWith(".json"));

const keepWhole = new Set([
  "access",
  "focus",
  "pass",
  "progress",
  "oasis",
  "stasis",
  "logos",
  "physics",
  "politics",
  "metrics",
  "fortress",
  "oppress",
  "species",
  "series",
  "means",
  "news",
  "ethics",
  "classics",
  "mathematics",
  "economics",
  "statistics",
  "linguistics",
  "gymnastics",
  "athletics",
  "acoustics",
  "optics",
  "tactics",
  "logistics",
  "dynamics",
  "mechanics",
  "aesthetics",
  "metaphysics",
  "semantics",
  "pragmatics",
]);

// Note: do NOT blanket-keep -ics (that would keep "cynics"). Field names live in keepWhole.
const keepSuffix = [/ness$/i, /less$/i, /ous$/i, /sis$/i, /ss$/i, /us$/i, /is$/i];

function looksLikePlural(word) {
  if (!word || word.length < 3) return false;
  const lower = word.trim().toLowerCase();
  if (keepWhole.has(lower)) return false;
  if (keepSuffix.some((re) => re.test(lower))) return false;
  if (!/s$/i.test(word)) return false;
  if (/ies$/i.test(word) && word.length > 4) return true;
  if (/(?:ch|sh|x|z|s)es$/i.test(word)) return true;
  if (/[^s]s$/i.test(word)) return true;
  return false;
}

function toSingular(word) {
  const w = word.trim();
  if (/^high-standards$/i.test(w)) return "high standard";
  if (/^short-cuts$/i.test(w)) return "short-cut";
  if (/ies$/i.test(w) && w.length > 4) {
    const base = w.slice(0, -3);
    return base + (w === w.toUpperCase() ? "Y" : "y");
  }
  if (/(?:ches|shes|xes|zes|sses)$/i.test(w)) return w.slice(0, -2);
  if (/s$/i.test(w)) {
    const s = w.slice(0, -1);
    if (w[0] === w[0].toUpperCase() && w.slice(1) === w.slice(1).toLowerCase()) {
      return s[0].toUpperCase() + s.slice(1).toLowerCase();
    }
    return s;
  }
  return w;
}

function singularizeDefinition(def, singular) {
  if (!def) return def;
  let d = def;
  if (/^high standard$/i.test(singular)) {
    d = d
      .replace(/^Demanding criteria for quality that raise the floor for everyone\./i,
        "A demanding criterion for quality that raises the floor for everyone.")
      .replace(/^Demanding criteria/i, "A demanding quality bar");
  }
  if (/^fundamental$/i.test(singular)) {
    d = d.replace(
      /^The basic principles or elements of a subject or skill; the essential parts\./i,
      "A basic principle or element of a subject or skill; an essential part.",
    );
  }
  if (/^principle$/i.test(singular)) {
    d = d.replace(
      /^Fundamental truths or propositions that serve as the foundation for a system of /i,
      "A fundamental truth or proposition that serves as the foundation for a system of ",
    );
  }
  return d;
}

/** Natural example rewrites when the singular lemma is not already in the quote. */
function singularizeExample(example, singular) {
  const ex = String(example || "");
  const lw = singular.toLowerCase();
  const le = ex.toLowerCase();
  // Passes WordExampleValidationTest: lemma, lemma+"s", lemma+"d"/"ed"/"ing"
  if (le.includes(lw) || le.includes(lw + "s") || le.includes(lw + "d") ||
      le.includes(lw + "ing") || le.includes(lw + "ed")) {
    return ex;
  }
  if (/^high standard$/i.test(singular)) {
    return ex
      .replace(/High-standards/g, "High standards")
      .replace(/high-standards/g, "high standards")
      .replace(/High-standards are/g, "High standards are");
  }
  // Prefer rewriting common plural headword forms inside the example over prefix hacks.
  const escaped = String(example || "").replace(
    new RegExp(`\\b${singular.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}s\\b`, "gi"),
    (m) => (m[0] === m[0].toUpperCase() ? singular[0].toUpperCase() + singular.slice(1) : singular),
  );
  const le2 = escaped.toLowerCase();
  if (le2.includes(lw) || le2.includes(lw + "s")) return escaped;
  // Last resort: keep original quote and accept plural containment only if already handled.
  return ex;
}

// Existing lemmas across the library
const existing = new Set();
for (const f of files) {
  for (const w of JSON.parse(fs.readFileSync(path.join(seed, f), "utf8"))) {
    existing.add(String(w.word).toLowerCase());
  }
}

const report = { renamed: [], removed: [] };

for (const f of files) {
  const p = path.join(seed, f);
  const words = JSON.parse(fs.readFileSync(p, "utf8"));
  const next = [];
  let changed = false;

  for (const w of words) {
    if (!looksLikePlural(w.word)) {
      next.push(w);
      continue;
    }

    const singular = toSingular(w.word);
    const singularKey = singular.toLowerCase();

    if (existing.has(singularKey) && singularKey !== String(w.word).toLowerCase()) {
      report.removed.push({
        file: f,
        id: w.id,
        from: w.word,
        reason: `singular already exists (${singular})`,
      });
      changed = true;
      continue;
    }

    const old = w.word;
    w.word = singular;
    w.definition = singularizeDefinition(w.definition, singular);
    w.example = singularizeExample(w.example, singular);

    report.renamed.push({ file: f, id: w.id, from: old, to: singular });
    existing.add(singularKey);
    next.push(w);
    changed = true;
  }

  if (changed) {
    fs.writeFileSync(p, JSON.stringify(next, null, 2) + "\n");
  }
}

console.log("RENAMED", report.renamed.length);
for (const r of report.renamed) console.log(`  ${r.file} #${r.id}: ${r.from} -> ${r.to}`);
console.log("REMOVED", report.removed.length);
for (const r of report.removed) console.log(`  ${r.file} #${r.id}: ${r.from} (${r.reason})`);
