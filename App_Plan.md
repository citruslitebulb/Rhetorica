Create a modern Android Compose app called "Rhetorica" - a clone of "Vocabulary - Learn words daily".

## 1) Product Goal
Build a polished Android app in Jetpack Compose that helps users learn and retain vocabulary daily through a swipeable feed, saved words, and lightweight quizzes.

## 2) Success Criteria (MVP)
- User can browse a daily word feed smoothly.
- User can open word detail, save/unsave words, and review saved words.
- User progress is persisted locally and survives app restarts.
- User can place a Word of the Day home screen widget with polished typography.
- User can customize widget appearance settings, including background color or image and optional translucency.
- App feels premium: dark-first, responsive UI, meaningful animations.

## 3) Scope
### In Scope (MVP)
- Local-first app (Room DB, no backend required).
- Feed, Word Detail, Saved, Quiz, Profile/Settings shell.
- Basic learning progress tracking (viewed/saved/quiz stats).
- TextToSpeech placeholder hooks.
- Home Screen Widget: Word of the Day + definition with beautiful font/style.
- Widget appearance settings: background color or image (bundled presets + gallery) with optional translucency.
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

## 6.5) Current Status
### Completed
- Foundation is in place: Gradle, Compose, Hilt, Room, WorkManager, app entry points, and base theme.
- Seed data and dictionary/orator data flow are implemented locally.
- Home feed is implemented with reusable word cards and orator-based filtering.
- Word detail flow is implemented.
- Save/unsave interactions are implemented from feed and detail.
- Saved screen is implemented with filtering and sorting basics.
- Profile/Settings shell exists with orator selection and widget appearance controls.
- Word of the Day widget foundation is implemented and renders live app data.
- Widget appearance settings currently support flat color presets plus translucency, persist locally, and refresh the widget reliably.

### Still Outstanding
- Widget image backgrounds, presets/gallery selection, and related image/translucency handling.
- Motion polish, broader accessibility pass, and performance polish.
- Notification time picker (currently WorkManager 24h periodic with fixed initial delay).
- Onboarding / first-run orator pick flow.

### Recently Improved
- Quiz: definition-match multiple choice with session score + progress persistence (scoped to selected orator when set).
- TTS: real `TextToSpeech` via `TtsSpeaker` (word detail + notification Hear; single-engine lifecycle).
- Deep links: widget speech CTA and notification body / “More info” open word detail.
- Home: Word of the Day hero + empty / filter-empty states.
- Word detail: speech-matched orator quote + speak button; safe not-found state.
- Profile: opens / saved / quiz progress stats.
- Seed: version-gated reload with orphan prune (chunked deletes; any words_*.json parse failure or invalid oratorId aborts without prune/version bump; quote/speech prune only when those assets load cleanly).

### Store Launch Phase (New Focus)
The core MVP feature set is largely complete. The project has entered the **Google Play Store release preparation** phase. See the dedicated plan:

→ **[PlayStore_Release_Plan.md](./PlayStore_Release_Plan.md)**

Key remaining work for publication includes release signing + minification, producing store assets (screenshots, feature graphic), creating a Privacy Policy, and filling out the Play Console Data Safety / Content Rating sections. Quiz and TTS are implemented in-app.

## 7) MVP Milestones
### Milestone 1 - Foundation
1. [Done] Root Gradle setup with latest stable Android/Compose/Kotlin tooling.
2. [Done] Hilt setup, app entry points, base module wiring.
3. [Done] Theme system: dark-first, dynamic colors, typography, spacing tokens.

### Milestone 2 - Data + Feed
1. [Done] Define word domain model + Room schema (`words`, `saved_words`, `progress`, `dictionaries`).
2. [Done] Seed fake data from local JSON into DB (first launch) with orator-specific collections.
3. [Done/Current fallback] Implement Home Feed with swipeable cards (`Pager` preferred; fallback `LazyColumn`).
4. [Done] Add dictionary selection UI in Profile/Settings to filter feed by orator.

### Milestone 3 - Core Learning Flows
1. [Done] Word Detail screen/sheet with pronunciation placeholder and examples.
2. [Done] Save/unsave interactions from feed + detail.
3. [Done] Saved screen with filtering/sorting basics.

### Milestone 4 - Quiz + Profile Shell
1. [Done] Basic quiz flow (definition-match multiple choice + session score).
2. [Not started] Persist quiz outcomes to progress table.
3. [Done/Partial] Profile/Settings scaffold with TTS placeholder and widget appearance settings controls.

### Milestone 5 - Widget MVP
1. [Done] Build Word of the Day widget layout (word + definition, premium typography).
2. [Done] Build a dedicated widget appearance settings flow/config surface.
3. [Partial] Support background customization (flat color picker + image presets/gallery selection).
4. [Partial] Add optional translucency level for both color and image backgrounds.
5. [Done] Save widget appearance preferences and refresh widget state reliably.

### Milestone 6 - Polish + QA
1. Motion polish (card transitions, micro-interactions, loading states).
2. Empty/error states and baseline accessibility checks.
3. Performance pass on low/mid devices.

## 8) Definition of Done (MVP)
- All core flows work offline with persisted state.
- Home screen widget renders correctly and updates with Word of the Day.
- Widget appearance settings persist (color/image preset/gallery + translucency).
- User can select from multiple orator dictionaries and feed filters accordingly.
- No major crashes in smoke testing.
- Navigation and state restoration behave correctly.
- UI is cohesive in dark and light modes.
- Codebase is modular enough for later API/sync integration.

> **Note**: Full "Definition of Done" for publishing to the Google Play Store is defined in [PlayStore_Release_Plan.md](./PlayStore_Release_Plan.md#7-definition-of-store-ready-v10). The MVP DoD above is a prerequisite.

## 9) Immediate First Deliverable (this sprint)
1. [Done] Full root build files and convention plugins.
2. [Done] App theme and reusable word card component.
3. [Done] Bottom navigation: Home Feed, Saved, Quiz, Profile.
4. [Done] Fake word model + Room entity/DAO.
5. [Done/Current fallback] Feed screen with swipe interaction.
6. [Done] Word detail route (sheet or full screen).
7. [Done] Basic Hilt + repository wiring.
8. [Done] Widget foundation: app widget provider + static preview layout.

## 10) Open Decisions (Need Your Input)
1. Feed interaction: strict vertical pager, or scroll feed with occasional hero cards?
2. Quiz type for MVP: multiple choice, typing, or mixed?
3. Seed dataset source: self-curated JSON, public word list, or generated starter set?
4. Detail pattern: bottom sheet (faster) vs full screen (cleaner navigation)?
5. Should Profile include streaks in MVP, or defer to phase 2?

---

## 11) Post-MVP: Google Play Store Release (See Dedicated Plan)
Major new workstream: preparing the app for public distribution on the Google Play Store.

All release-specific planning, technical requirements, asset needs, policy work, code tasks, and phased milestones are documented in:

**`PlayStore_Release_Plan.md`** (root of the repository).

This includes:
- Release signing configuration and enabling R8/minification.
- Completing or gating incomplete features (Quiz, TTS, notification deep links).
- Store listing assets (screenshots, feature graphic).
- Privacy Policy + Data Safety section.
- Pre-launch testing process and Definition of "Store-Ready".

Update the dedicated release plan as work progresses. Re-read both `App_Plan.md` and `PlayStore_Release_Plan.md` before starting significant release-related changes.
