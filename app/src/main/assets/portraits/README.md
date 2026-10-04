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

Alex Hormozi (`orator_alex_hormozi`), Charlie Munger (`orator_charlie_munger`),
Warren Buffett (`orator_warren_buffett`), Gary Vaynerchuk
(`orator_gary_vaynerchuk`), Ryan Holiday (`orator_ryan_holiday`), Chris
Williamson (`orator_chris_williamson`), Naval Ravikant
(`orator_naval_ravikant`), Jocko Willink (`orator_jocko_willink`), and
David Goggins (`orator_david_goggins`) follow this same fallback. No modern business, podcast, or Stoic orator ships a WebP
in this tree. Profile renders gold-ringed initials, and a generated likeness
is not part of the portrait pipeline.

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
