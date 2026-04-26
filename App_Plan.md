Create a modern Android Compose app called "VocabDaily" - a clone of "Vocabulary - Learn words daily".

## 1) Product Goal
Build a polished Android app in Jetpack Compose that helps users learn and retain vocabulary daily through a swipeable feed, saved words, and lightweight quizzes.

## 2) Success Criteria (MVP)
- User can browse a daily word feed smoothly.
- User can open word detail, save/unsave words, and review saved words.
- User progress is persisted locally and survives app restarts.
- User can place a Word of the Day home screen widget with polished typography.
- User can customize widget background (flat color or image) with optional translucency.
- App feels premium: dark-first, responsive UI, meaningful animations.

## 3) Scope
### In Scope (MVP)
- Local-first app (Room DB, no backend required).
- Feed, Word Detail, Saved, Quiz, Profile/Settings shell.
- Basic learning progress tracking (viewed/saved/quiz stats).
- TextToSpeech placeholder hooks.
- Home Screen Widget: Word of the Day + definition with beautiful font/style.
- Widget background customization: flat color or image (bundled presets + gallery), each with optional translucency.
- Vocabulary dictionary selection: choose from curated word collections by famous orators (e.g., Shakespeare, MLK Jr., Churchill).

### Out of Scope (for now)
- Authentication and cloud sync.
- Real remote API integration.
- Full spaced repetition engine (can be phase 2).
- Social, gamification, or subscriptions/paywall.

## 4) Tech Stack
- Kotlin 100%
- Jetpack Compose + Material 3
- Single-Activity architecture
- Navigation Compose (type-safe routes)
- ViewModel + Kotlin Flow
- Hilt (DI)
- Room (local persistence: words, saved state, progress)
- Kotlinx Serialization (seed/fake data parsing)
- Coil (optional, only if image assets are needed)
- Gradle Kotlin DSL + Version Catalog + convention plugins

## 5) Project Structure
- `app/`
- `core/`
  - `ui/`
  - `designsystem/`
  - `util/`
  - `model/`
- `feature/`
  - `feed/`
  - `word/`
  - `saved/`
  - `quiz/`
  - `settings/`
- `data/`
  - `local/` (Room, DAO, entities)
  - `repository/`
  - `seed/` (bundled JSON / fake data)
  - `remote/` (reserved for future API)

## 6) Architecture Notes
- UI state should be unidirectional (`StateFlow` from ViewModel).
- Repositories mediate between Room and feature modules.
- Keep entities separate from UI models (mapping layer in `data` or `core/model`).
- Navigation destinations should not access DB directly; use feature use-cases/repositories.

## 7) MVP Milestones
### Milestone 1 - Foundation
1. Root Gradle setup with latest stable Android/Compose/Kotlin tooling.
2. Hilt setup, app entry points, base module wiring.
3. Theme system: dark-first, dynamic colors, typography, spacing tokens.

### Milestone 2 - Data + Feed
1. Define word domain model + Room schema (`words`, `saved_words`, `progress`, `dictionaries`).
2. Seed fake data from local JSON into DB (first launch) with orator-specific collections.
3. Implement Home Feed with swipeable cards (`Pager` preferred; fallback `LazyColumn`).
4. Add dictionary selection UI in Profile/Settings to filter feed by orator.

### Milestone 3 - Core Learning Flows
1. Word Detail screen/sheet with pronunciation placeholder and examples.
2. Save/unsave interactions from feed + detail.
3. Saved screen with filtering/sorting basics.

### Milestone 4 - Quiz + Profile Shell
1. Basic quiz flow (multiple choice or definition match).
2. Persist quiz outcomes to progress table.
3. Profile/Settings scaffold with TTS placeholder and widget customization controls.

### Milestone 5 - Widget MVP
1. Build Word of the Day widget layout (word + definition, premium typography).
2. Support background customization (flat color picker + image presets/gallery selection).
3. Add optional translucency level for both color and image backgrounds.
4. Save widget style preferences and refresh widget state reliably.

### Milestone 6 - Polish + QA
1. Motion polish (card transitions, micro-interactions, loading states).
2. Empty/error states and baseline accessibility checks.
3. Performance pass on low/mid devices.

## 8) Definition of Done (MVP)
- All core flows work offline with persisted state.
- Home screen widget renders correctly and updates with Word of the Day.
- Widget style customization persists (color/image preset/gallery + translucency).
- User can select from multiple orator dictionaries and feed filters accordingly.
- No major crashes in smoke testing.
- Navigation and state restoration behave correctly.
- UI is cohesive in dark and light modes.
- Codebase is modular enough for later API/sync integration.

## 9) Immediate First Deliverable (this sprint)
1. Full root build files and convention plugins.
2. App theme and reusable word card component.
3. Bottom navigation: Home Feed, Saved, Quiz, Profile.
4. Fake word model + Room entity/DAO.
5. Feed screen with swipe interaction.
6. Word detail route (sheet or full screen).
7. Basic Hilt + repository wiring.
8. Widget foundation: app widget provider + static preview layout.

## 10) Open Decisions (Need Your Input)
1. Feed interaction: strict vertical pager, or scroll feed with occasional hero cards?
2. Quiz type for MVP: multiple choice, typing, or mixed?
3. Seed dataset source: self-curated JSON, public word list, or generated starter set?
4. Detail pattern: bottom sheet (faster) vs full screen (cleaner navigation)?
5. Should Profile include streaks in MVP, or defer to phase 2?