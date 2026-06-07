# Agents.md — Rhetorica

Project rules and context for AI coding agents. These instructions apply to all work in this repository.

## Must Read First
**Read `App_Plan.md` before starting any non-trivial feature work or refactoring.**

`App_Plan.md` is the source of truth for:
- Product goals and MVP success criteria
- Current implementation status (what is done vs. outstanding)
- Intended architecture and scope
- Open decisions

## Project Overview
Rhetorica is a polished, offline-first Android vocabulary app inspired by "Vocabulary - Learn words daily," but focused on rhetorical language and oratory.

Users browse a swipeable/scrollable daily word feed drawn from curated dictionaries of famous orators (Demosthenes, Cicero, Pericles, Shakespeare, Lincoln, MLK Jr., Churchill, etc.). They can save words, view rich details (definitions, examples, linked quotes and full speeches), review saved words, browse full speeches, customize a beautiful **Word of the Day** home screen widget, and receive daily notifications.

Core characteristics:
- 100% local (Room database, no backend in MVP)
- Dark-first premium feel with elegant typography and gold/burgundy/cream palette
- Strong emphasis on widget quality and daily habit formation
- Seed data driven by orator-specific JSON collections

## Tech Stack
- **Language / UI**: Kotlin 100%, Jetpack Compose + Material 3
- **Architecture**: Single-Activity, Navigation Compose, Hilt (DI), ViewModel + Kotlin Flow/StateFlow
- **Persistence**: Room (entities + DAOs for words, saved words, progress, dictionaries, preferences, quotes, speeches). Current schema version: 14. Heavy use of migrations and `@TypeConverters` (JSON lists).
- **Seeding**: Kotlinx Serialization + assets in `app/src/main/assets/data/seed/`. `SeedDataLoader` runs on every launch (upserts dictionaries, words, quotes, speeches).
- **Background / Widget**: WorkManager (Hilt-enabled) for daily Word of the Day notifications + `AppWidgetProvider`.
- **Build**: Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`), KSP for Room/Hilt.
- **Min / Target**: API 27 / 35

## Build, Run & Common Tasks
Use the Gradle wrapper (works from repo root on Windows via `gradlew.bat` or `./gradlew` in PowerShell/Git Bash).

```powershell
# Build
.\gradlew assembleDebug

# Install on connected device/emulator
.\gradlew installDebug

# Clean
.\gradlew clean

# Run unit tests
.\gradlew test

# For Android Studio: open the project and run the `app` configuration.
```

The app automatically seeds (or re-upserts) data on launch via `RhetoricaApp.onCreate` → `SeedDataLoader.loadSeedDataIfNeeded()`. (A previous debug "Re-seed data" button in Profile has been removed; developers can trigger `SeedDataLoader.loadSeedDataIfNeeded()` directly if needed during development.)

## Repository Layout (Current)
The project is a single Gradle module (`:app`). Package: `com.rhetorica.app`.

```
app/src/main/
├── assets/data/seed/          # dictionaries.json + per-orator words_*.json, quotes_*.json + speeches.json
├── java/com/rhetorica/app/
│   ├── core/
│   │   ├── model/             # OratorProfile.kt etc.
│   │   ├── navigation/        # RhetoricaNavHost.kt, TopLevelDestination.kt
│   │   └── ui/                # Reusable cards (WordListCard.kt)
│   ├── data/
│   │   ├── local/             # All Room *Entity, *Dao, RhetoricaDatabase, Converters, summaries
│   │   ├── repository/        # WordRepository, DictionaryRepository
│   │   └── seed/              # SeedDataLoader.kt
│   ├── di/                    # DatabaseModule.kt, JsonModule.kt
│   ├── feature/
│   │   ├── home/, saved/, word/, quiz/, profile/, speech/
│   ├── notification/          # NotificationChannelManager, WordOfDayWorker, receivers
│   ├── ui/theme/              # RhetoricaTheme, Type.kt
│   ├── widget/                # WordOfDayWidgetProvider + WidgetAppearance.kt
│   ├── MainActivity.kt
│   ├── RhetoricaApp.kt        # @HiltAndroidApp + WorkManager + seeding
│   └── ui/RhetoricaApp.kt     # Root Scaffold + bottom nav
```

Note: The multi-module structure described in older sections of `App_Plan.md` (separate `core/`, `feature/`, `data/` modules) was aspirational and is **not** the current on-disk layout. We use package organization inside the single `app` module.

## Architecture & Coding Patterns
- **DI**: Hilt is the standard. Use `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, constructor injection, and `@Inject` for assisted factories where needed.
- **State management**: Unidirectional. ViewModels expose `StateFlow<SomeUiState>`. Prefer `combine(flow1, flow2, ...)` from DAOs/repositories + `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- **Data layer**: DAOs return `Flow<List<...>>` or `Flow<Entity?>` (observe* naming is common). Repositories are thin adapters. Use `withTransaction` for multi-table seed operations.
- **Navigation**: 
  - Top-level destinations via `TopLevelDestination` enum + bottom bar in `RhetoricaApp`.
  - Detail screens registered via extension functions (`wordDetailScreen(...)`, `speechDetailScreen(...)`, `navigateToWordDetail(...)`).
  - Use `popBackStack()`, `launchSingleTop`, `saveState`/`restoreState` for bottom nav.
- **Compose**: Use Material 3 components. Follow existing patterns for cards, filtering, and detail sheets/screens. Prefer stable, minimal public APIs for composables.
- **Theme**: Always go through `RhetoricaTheme`. Dark-first. Use the custom color scheme (gold primary `#D4AF37` in dark, cream paper background in light). Reference `MaterialTheme` tokens.
- **Widget**: Appearance logic lives in `WidgetAppearance` (color presets, opacity helpers, bitmap generation for rounded backgrounds). Call `WidgetAppearance.refreshAllWidgets(context)` after any change that should update live widgets.
- **Background work**: Periodic `WordOfDayWorker` via WorkManager (unique, REPLACE policy). Notification actions are handled in `MainActivity.onCreate`/`onNewIntent` (some deep-link navigation is still TODO).
- **Seeding**: Treat the JSON assets as the source of truth. The loader always re-processes dictionaries/words/quotes/speeches on launch (REPLACE strategy). Add new orators by adding matching JSON files + entry in `dictionaries.json`.

## Terminology & Product Language (Use Consistently)
- Orator / orator profile / dictionary (not "deck" or generic "category")
- Word of the Day / daily feed
- Saved words
- Speeches (full texts), quotes (excerpts linked to words)
- Widget appearance (color presets + translucency; image backgrounds partial)
- Refer to sections and concepts exactly as named in `App_Plan.md` and `core/model/OratorProfile.kt` (e.g. `themeCategories`, `rotateThroughAll`, etc.).

## Current Status Highlights (see App_Plan.md for full details)
**Largely complete**:
- Home feed with orator/theme filtering + save/unsave
- Word detail + speech linking
- Saved list with filtering/sorting
- Profile/settings (orator selection, widget controls)
- Word of the Day widget foundation + color/translucency customization
- Daily notification scheduling
- Full speeches browser
- Rich seed data (many orators + quotes + speeches)

**Outstanding / partial**:
- Quiz flow (destination exists, implementation minimal)
- Widget image backgrounds + gallery selection
- Real TTS (text-to-speech) beyond placeholders
- Polish: motion, accessibility, performance, empty states
- Some notification deep linking is stubbed

Always check the "Current Status", "Still Outstanding", and "MVP Milestones" sections in `App_Plan.md` before planning work.

## When Implementing Changes
1. Read `App_Plan.md` (and this file).
2. Make small, focused changes that map to one clear user-facing behavior or fix.
3. Reuse existing terminology, patterns, and file locations.
4. Call out any assumptions explicitly in your response or code comments.
5. After changes, provide a short verification plan (e.g. "Builds cleanly. Verified home feed filtering by orator and theme. Widget updates on color change.").
6. Do not introduce new third-party dependencies without strong justification (current set is deliberate and minimal).
7. Database schema changes **must** include a proper `Migration` in `RhetoricaDatabase` + version bump.
8. Seed JSON changes affect first-run and re-seed for all users — keep them additive where possible.
9. Update or add tests when behavior changes (currently very few tests exist; at minimum validate seed loading).

## Verification Expectations
- The app must build and install (`assembleDebug` / `installDebug`).
- Core flows should be manually exercised: feed browsing + filtering, save/unsave, word detail, widget placement + appearance changes, profile settings.
- For widget or notification work, test on a physical device or emulator with the widget added to the home screen.
- Run `./gradlew test` and address any failures.
- Re-seeding behavior is exercised automatically on app launch (via RhetoricaApp + SeedDataLoader). Verify seed data updates take effect after editing JSON assets by relaunching the app (or force-stop + relaunch).

## Git & Workflow
- This is a single branch repo (main) at the time of writing.
- Commit messages should be clear and reference the area changed (e.g. "widget: persist image background selection").
- Keep changes reviewable. Large refactors should be broken up.

## Additional Context
- The `.cursor/` and `.windsurf/` directories contain tool-specific copies of some of this guidance and a reusable `review-staged-changes` skill. The canonical cross-agent instructions live here in `Agents.md` and `App_Plan.md`.
- There is minimal test coverage today (`app/src/test/...` has only a seed validation test). Expand thoughtfully.

Follow these rules and the plan in `App_Plan.md`. When in doubt, ask for clarification on requirements or open decisions rather than guessing.