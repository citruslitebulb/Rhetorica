# Orator portrait library

These WebP files are **not shown in the current UI**. Profile orator rows use
gold-ringed initials from `OratorPortraits.monogram` via `OratorPortrait`
(the full name is shown beside the logo, not under it).

Photo drawables still live at:

```text
app/src/main/res/drawable-nodpi/orator_<slug>.webp
```

`OratorPortraits.drawableRes` can resolve them by dictionary `id` → slug if
photos are re-enabled later.

## Art direction (if photos are used again)

- Square source (UI crops to a gold-ring circle)
- Face-centered bust, three-quarter view preferred
- Dark charcoal / ink background
- Soft gold rim light, museum-medallion feel
- No text, logos, or watermarks
- ~512–1024px preferred (current set is generated medallion portraits)

## Naming (must match `OratorPortraits.slugById`)

See `OratorPortraits.kt` for the canonical id → slug table.

## Replacing an asset

1. Export/replace `orator_<slug>.webp` (or `.png` / `.jpg`) in `drawable-nodpi`.
2. Rebuild the app — no code change needed if the filename matches. The current
   Profile UI will still show initials until `OratorPortrait` is wired back to
   the drawable.
