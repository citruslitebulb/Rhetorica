/**
 * Remap non-canonical theme slugs in seed JSON to WordThemes.all values.
 * Only rewrites `categories` / `themeCategories` arrays — never word/definition text.
 */
const fs = require("fs");
const path = require("path");

const seed = path.join(__dirname, "..", "app", "src", "main", "assets", "data", "seed");

const map = {
  technology: "tech",
  innovation: "tech",
  balance: "humanities",
  belief: "inspirational",
  comeback: "courage",
  community: "leadership",
  competition: "leadership",
  "customer-focus": "tech",
  destiny: "inspirational",
  discipline: "leadership",
  dreams: "inspirational",
  duty: "leadership",
  enterprise: "leadership",
  entrepreneurship: "tech",
  ethics: "humanities",
  failure: "courage",
  family: "inspirational",
  fear: "courage",
  forgiveness: "inspirational",
  freedom: "democracy",
  fundamentals: "leadership",
  "global-impact": "leadership",
  heart: "inspirational",
  heroism: "courage",
  hope: "inspirational",
  information: "tech",
  "long-termism": "leadership",
  mentorship: "leadership",
  moonshots: "tech",
  motivation: "inspirational",
  patience: "courage",
  perseverance: "courage",
  philanthropy: "legacy",
  power: "leadership",
  purpose: "inspirational",
  redemption: "courage",
  resilience: "courage",
  respect: "leadership",
  responsibility: "leadership",
  space: "tech",
  stoicism: "humanities",
  sustainability: "legacy",
  teamwork: "leadership",
  underdog: "courage",
  unity: "democracy",
  virtue: "humanities",
  vision: "leadership",
  wisdom: "humanities",
};

const valid = new Set([
  "inspirational",
  "tech",
  "humanities",
  "arts",
  "leadership",
  "democracy",
  "courage",
  "legacy",
]);

function remapArrayField(text, field) {
  const re = new RegExp(`"${field}"\\s*:\\s*\\[([^\\]]*)\\]`, "g");
  return text.replace(re, (_m, inner) => {
    const items = [...inner.matchAll(/"([^"]+)"/g)].map((x) => x[1]);
    const remapped = items.map((item) => map[item] || item);
    const unique = [...new Set(remapped)];
    const invalid = unique.filter((t) => !valid.has(t));
    if (invalid.length > 0) {
      throw new Error(`Unmapped themes for ${field}: ${invalid.join(", ")}`);
    }
    return `"${field}": [${unique.map((i) => `"${i}"`).join(", ")}]`;
  });
}

const files = fs
  .readdirSync(seed)
  .filter((f) => f.startsWith("words_") || f === "dictionaries.json");

let changed = 0;
for (const f of files) {
  const p = path.join(seed, f);
  let text = fs.readFileSync(p, "utf8");
  const orig = text;
  text = remapArrayField(text, "categories");
  text = remapArrayField(text, "themeCategories");
  if (text !== orig) {
    fs.writeFileSync(p, text);
    changed += 1;
    console.log("updated", f);
  }
}

const themes = new Set();
const words = new Set();
for (const f of files) {
  const text = fs.readFileSync(path.join(seed, f), "utf8");
  for (const m of text.matchAll(/"(?:categories|themeCategories)"\s*:\s*\[([^\]]*)\]/g)) {
    for (const t of m[1].matchAll(/"([^"]+)"/g)) {
      themes.add(t[1]);
    }
  }
  if (f.startsWith("words_")) {
    for (const m of text.matchAll(/"word"\s*:\s*"([^"]+)"/g)) {
      words.add(m[1]);
    }
  }
}
const invalid = [...themes].filter((t) => !valid.has(t));
console.log("files_changed=" + changed);
console.log("remaining_invalid=" + invalid.join(","));
console.log("sample_words_contains_resilience=" + [...words].includes("resilience"));
console.log("all_themes=" + [...themes].sort().join(","));
if (invalid.length > 0) process.exitCode = 1;
