Create a modern Android Compose app called "VocabDaily" - a clone of "Vocabulary - Learn words daily".

Tech stack:
- Kotlin 100%
- Jetpack Compose + Material 3
- Single Activity
- Hilt for DI
- Room for local DB (words + saved + progress)
- Kotlin Flows + ViewModel
- Navigation Compose (type-safe)
- Coil for images if needed
- Kotlinx Serialization for word data

Project structure with convention plugins:
- app/
- core/ (ui, designsystem, util, data)
- feature/ (feed, word, saved, quiz, settings)
- data/ (local Room + remote if we add API later)

Use Gradle Kotlin DSL + Version Catalog.

First deliver:
1. Full root build files with latest stable versions (Compose BOM 2025+, etc.)
2. App theme with dynamic colors + custom word card style
3. Main navigation with bottom bar (Home Feed, Saved, Quiz, Profile)
4. Fake word data model + Room entity
5. Vertical swipeable word feed screen using Pager or LazyColumn with cards
6. Word detail bottom sheet or screen
7. Basic Hilt setup

Make it beautiful, smooth animations, dark-first friendly. Add placeholder for TextToSpeech and Widget.