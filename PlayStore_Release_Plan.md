# Play Store Release Plan — Rhetorica

**Status**: Draft (Post-MVP / Pre-Launch Phase)  
**Last Updated**: 2026-07-11  
**Owner**: TBD  
**Related**: [App_Plan.md](./App_Plan.md), [Agents.md](./Agents.md), **[Owner_Checklist.md](./Owner_Checklist.md)** (owner-only launch tasks)

## 1. Goal

Publish Rhetorica (a polished, offline-first rhetorical vocabulary app) to the Google Play Store as a high-quality, production-ready application.

Success means:
- The app is available on the Play Store for Android users (API 27+).
- It passes Google’s automated pre-launch checks and policy review.
- Core user flows (daily feed, save/unsave, widget, profile customization, notifications) work reliably.
- The listing presents a premium, trustworthy product.

## 2. Current State (Codebase Snapshot)

### Strengths (Release-Friendly)
- Fully offline (Room + local JSON seed data) — dramatically simplifies Data Safety and privacy.
- No accounts, no analytics, no ads, no external network calls in the core app.
- Modern tech: Compose + Material 3, Hilt, WorkManager, Navigation Compose, targetSdk 35.
- Adaptive launcher icon + monochrome support already implemented.
- Daily Word of the Day notifications + premium gold-bordered widget (color + opacity customization).
- Notification permission handling exists (`POST_NOTIFICATIONS` + runtime request in `MainActivity`).
- Good theming and visual identity (gold/burgundy/cream dark-first palette).
- **Quiz**: definition-match multiple choice + letter-guess Quest, per-word Leitner spaced repetition with review prioritisation, part-of-speech-matched distractors.
- **TTS**: real `TextToSpeech` via `TtsSpeaker` (word detail + notification Hear; await utterance *start* for receivers).
- **Deep links**: widget speech CTA and notification body / “More info” open word detail via `MainActivity` (internal explicit intents).
- **Home**: Word of the Day hero + empty / filter-empty states.
- **Progress**: day streak / opens / saved / quizzes / mastered / due on Profile.
- **Background**: notification fires at the chosen local time (self-re-arming one-shot worker); widget rolls over after local midnight.
- **Typography / assets**: bundled Playfair Display (OFL); Profile orator logos are gold-ringed initials (not photos).
- **Seed**: version-gated reload (`SEED_VERSION`) with orphan prune on content shrink; version only advances after successful load.

### Known Gaps & Technical Debt
- **Build/Release**:
  - Release minify/R8 and ProGuard rules are in place. `signingConfigs.release` reads env vars (`RHETORICA_STORE_*`) and falls back to the debug key when they are absent — the keystore itself is owner-only and not committed.
  - `versionCode = 2`, `versionName = "1.0.0"`.
  - Schema is v17 with no destructive-migration fallback; every schema change must ship a `Migration`.
- **Still needed for the Play listing**:
  - Hosted public Privacy Policy URL (in-app policy exists).
  - Screenshots and feature graphic.
  - Play Console Data Safety / Content Rating.
- **Code Hygiene**:
  - Numerous `Log.d` / `Log.e` calls left in production paths.
  - Some `// TODO` comments remaining (non-blocking).
  - Test coverage is unit-level only (~20 files: seed validation, WotD selector, Leitner scheduler, streaks, quiz round builder, schedulers); no instrumentation tests.
- **Store Assets & Policy**:
  - In-app Privacy Policy exists (`assets/privacy_policy.html`); a hosted public URL is still required for the listing.
  - No screenshots, feature graphic, or store descriptions prepared.
  - Data Safety section not yet configured in Play Console.
- **Other**:
  - Backup rules are minimal (`<full-backup-content />`).
  - CI (`.github/workflows/ci.yml`) runs unit tests, `assembleDebug`, and `assembleRelease` on push/PR; there is no automated Play upload.

**Package**: `com.rhetorica.app` (already set, good — do not change).

## 3. High-Level Phases

| Phase | Focus | Key Deliverables |
|-------|-------|------------------|
| Phase 0 | Foundations & Policy | Play Console account, Privacy Policy, Data Safety answers |
| Phase 1 | Build & Signing | Proper release signing, minify + R8 enabled, AAB builds cleanly |
| Phase 2 | Code Quality & Completeness | Guard Logs, remaining polish, expand tests |
| Phase 3 | Store Presence | Screenshots, feature graphic, polished title/description, app icon validation |
| Phase 4 | Testing & Pre-Launch | Internal/Closed testing, address pre-launch report issues |
| Phase 5 | Launch | Production rollout (staged or full) + post-launch monitoring |

## 4. Detailed Requirements

### 4.1 Google Play Console & Account
- Create / use a Google Play Developer account ($25 one-time).
- Create the app entry using `com.rhetorica.app`.
- Complete the Content Rating questionnaire.
- Set up Internal Testing track first (recommended), then Closed, then Open/Production.

### 4.2 Build, Signing & Release Configuration
**Status in `app/build.gradle.kts`**:

- [Done] `signingConfigs.release` driven by `RHETORICA_STORE_*` env vars (use Play App Signing — upload the AAB and let Google manage the final signing key). Owner still needs to create the keystore and set the variables on the release machine/CI.
- [Done] Release build type wired to the signing config (debug-key fallback when env vars are absent).
- [Done] `isMinifyEnabled = true` and `isShrinkResources = true` for release.
- [Done] `proguard-rules.pro` carries keep rules for:
  - Room entities/DAOs
  - Hilt / WorkManager
  - Kotlinx Serialization
  - Compose (if needed)
  - Any custom receivers / providers
- Establish a versioning strategy (e.g., `versionCode` increments strictly; `versionName` follows semver).
- Produce **Android App Bundle (.aab)** — APKs are no longer accepted for new apps.

**Recommended**: Add a CI step (or manual script) that builds a signed release AAB using environment variables for keystore/passwords (never commit secrets).

### 4.3 App Code & Quality
**Done for core experience** (verify on device before upload):
- Real `TextToSpeech` (`TtsSpeaker`) on word detail + notification Hear.
- Notification “More info” deep-links to word detail; widget speech CTA opens speech.
- Quiz is a working definition-match flow with progress stats.
- Widget color presets + opacity applied via elegant gold-bordered card.

**Still needed before upload**:
- Remove or guard all `Log.*` calls in non-debug code paths.
- Address remaining user-facing TODOs.
- Expand ProGuard rules and test a release build end-to-end on a physical device/emulator.
- Consider basic crash reporting (Play Console built-in minimum; optional Crashlytics).

### 4.4 Store Listing Assets
Google requires (at minimum):
- High-quality screenshots (2–8) for phone; ideally also tablet and large screen.
- Feature graphic (1024 × 500 px).
- App icon (already have adaptive + round + monochrome — validate it looks good at store sizes).
- Title (≤ 30 chars), short description (≤ 80 chars), full description.
- Promotional video (optional but increases conversion).

**Current assets available**:
- Vector logo assets in `Logo/` folder and `res/drawable/ic_launcher_*`.
- Widget design mockups in `design-mockups/widget-proposal/`.
- No store screenshots or feature graphic yet.

### 4.5 Legal, Policy & Content
- **Privacy Policy** (mandatory): Host a publicly accessible page or Google Doc. Declare that the app is fully offline and collects no personal data.
- **Data Safety section** in Play Console: Expect to select “No data collected or shared”. Still answer the questionnaire accurately.
- Content rating: Historical oratory content should rate cleanly for “Everyone” or “Teen” (be honest about any mature language in source speeches).
- App behavior: Daily notifications are for habit formation — this is acceptable but document it clearly if asked.

### 4.6 Testing, Rollout & Monitoring
- Use Play Console’s Internal Testing track first.
- Add at least a few real devices/users to Closed Testing before opening further.
- Monitor the Pre-launch report (crashes, ANRs, accessibility, security, battery, etc.).
- After launch: Watch for reviews mentioning notification frequency, widget behavior, or TTS expectations.

## 5. Specific Code & Config Tasks (Actionable)

### Build Configuration (`app/build.gradle.kts`)
- [Done] `signingConfigs { release { ... } }` (env-var driven).
- [Done] `buildTypes.release` minified, shrunk, and signed.
- [Owner] Bump `versionCode` / `versionName` for the first store build.

### ProGuard (`app/proguard-rules.pro`)
- [Done] Keep rules for Room, Hilt, WorkManager, kotlinx.serialization, and notification receivers.

### Hygiene
- Audit and clean `Log` statements across the codebase.
- Expand unit/integration tests around seeding, widget selection, and progress.

### Store-Facing Strings
- Consider adding dedicated store strings (or keep them in a separate resource file) for title, descriptions, etc.

## 6. Milestones

1. **M0 — Policy & Console Ready**
   - Play Console app created.
   - Privacy Policy published.
   - Data Safety + Content Rating completed (draft).

2. **M1 — Release Build Pipeline**
   - Signed release AAB can be produced locally.
   - Minify + resource shrinking enabled and tested.
   - Versioning strategy documented.

3. **M2 — Core Experience Complete** *(largely done — re-verify on device)*
   - Notification actions (favorite + deep link + hear) work.
   - Quiz functional with progress.
   - TTS works from detail + notification.
   - Widget customizes color/opacity and opens speech / word detail.

4. **M3 — Store Assets & Listing**
   - Full set of screenshots + feature graphic.
   - Polished listing text.
   - App icon validated.

5. **M4 — Pre-Launch Validation**
   - Internal + Closed testing completed.
   - Pre-launch report issues addressed.
   - No critical crashes or policy blockers.

6. **M5 — Production Launch**
   - Staged rollout (e.g., 5% → 50% → 100%) or full release.
   - Monitoring and first update process established.

## 7. Definition of "Store-Ready" (v1.0)

- A signed Android App Bundle builds successfully with minification.
- All bottom navigation destinations feel complete (or intentionally limited).
- Notifications work end-to-end, including actions.
- Widget can be added and customized.
- Privacy Policy exists and Data Safety is accurate.
- High-quality screenshots and listing assets are uploaded.
- The app passes Google’s pre-launch report with no blocking issues.
- At least one full manual smoke test on a physical device (feed → save → widget → profile → daily notification).

## 8. Risks & Open Decisions

- **Notification frequency**: Daily at a fixed time may annoy some users. Consider quiet hours or a toggle in Profile later.
- **Keystore management**: Local keystore + manual upload first, or Play App Signing + CI secrets from the start?
- **Long-term support**: Post-launch update cadence and how we handle seed data expansions (`SEED_VERSION` bumps).
- **App name / branding**: Confirm `Rhetorica` and tagline are final and not trademark-conflicting in target markets.

## 9. References & Next Steps

- Read [App_Plan.md](./App_Plan.md) for original MVP scope and outstanding items.
- Follow [Agents.md](./Agents.md) rules (especially around database migrations and seed data changes).
- After each major change, run `./gradlew assembleRelease` (once signing is configured) and test on device.
- Track progress against the phases above.

---

**How to use this document**:
- Treat this as the single source of truth for everything related to reaching the Play Store.
- Update the “Current State” and milestone checkboxes as work progresses.
- Before starting large implementation work, re-read both this file and `App_Plan.md`.

When ready to begin execution, create focused tasks or sub-agents against the specific items in Sections 4 and 5.
