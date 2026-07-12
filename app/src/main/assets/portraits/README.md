# Orator portrait library

Runtime assets are packaged as Android drawables:

```text
app/src/main/res/drawable-nodpi/orator_<slug>.jpg
```

Lookup: `OratorPortraits` maps dictionary `id` → slug → `R.drawable.orator_<slug>`.  
Missing files fall back to a gold monogram in `OratorPortrait`.

## Art direction

- Square source (UI crops to a gold-ring circle)
- Face-centered bust, three-quarter view preferred
- Dark charcoal / ink background
- Soft gold rim light, museum-medallion feel
- No text, logos, or watermarks
- ~512–1024px preferred (current set is generated medallion portraits)

## Naming (must match `OratorPortraits.slugById`)

See `OratorPortraits.kt` for the canonical id → slug table (ids 1–38).

## Replacing an asset

1. Export/replace `orator_<slug>.jpg` (or `.webp` / `.png`) in `drawable-nodpi`.
2. Rebuild the app — no code change needed if the filename matches.
