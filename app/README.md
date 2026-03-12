# GitMatch App - Mobile Client

**GitMatch** is a Tinder-style mobile application built for developers. It solves the problem of "developer fatigue" by providing a curated, swipable deck of underrated GitHub repositories and critical tech news/release notes. 

Built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, this codebase shares 95%+ of its logic and UI across platforms, while specifically targeting Android as the primary native release.

---

## 🚀 What The App Does (Core Functionality)

The application centers around a highly interactive, physical-feeling "Card Deck" UI.

1. **The Discovery Deck:**
   * **Hybrid Feed:** Users see a stack of cards blending hidden-gem GitHub Repositories and crucial Tech News. Swipeable top tabs allow filtering (For You | 🧑‍💻 Repos | 📰 News).
   * **Swipe Gestures:**
     * 👉 **Swipe Right (Like):** Saves the item to the user's "Vault" (and stars it on GitHub).
     * 👈 **Swipe Left (Ignore):** Dismisses the item and loads the next.
     * 👆 **Swipe Up (Expand):** Opens full details, deep-dives into the AI summary, or displays the README.

2. **Smart Cards (Powered by Backend AI):**
   * **Repo Cards:** Show star count, primary language, an AI-generated 1-sentence summary, a core code snippet, and highlight if the repo has "Good First Issues" for open-source contributors.
   * **News Cards:** Display an AI-generated 3-bullet TL;DR so developers can absorb the news instantly without reading the full article.

3. **The Vault:**
   * A saved collection of all "Right Swipes". Allows users to easily find that tool they saw 3 days ago.

4. **Onboarding & Personalization:**
   * First-time users select their tech stack (e.g., Android, Rust, AI) which adjusts the backend recommendation weights.

---

## 🛠️ Tech Stack & Libraries

| Category              | Framework / Library                                  |
|-----------------------|------------------------------------------------------|
| **Platform**          | Kotlin Multiplatform (KMP)                           |
| **UI**                | Compose Multiplatform (Material 3)                   |
| **Architecture**      | Clean MVI (Model-View-Intent)                        |
| **Networking**        | Ktor Client (ContentNegotiation, Logging)            |
| **Serialization**     | `kotlinx.serialization` (JSON mapping)               |
| **Dependency Inject** | Koin                                                 |
| **State Management**  | Kotlin Coroutines + `StateFlow`                      |
| **Image Loading**     | Coil 3 (Multiplatform)                               |
| **Navigation**        | Compose Navigation                                   |

---

## 📂 Project Structure

The project follows a strict feature-based, unidirectional data flow (MVI) architecture located inside the `composeApp/src/commonMain/kotlin` directory.

```text
app/
├── CLAUDE_INSTRUCTIONS.md           # Strict AI coding guide
└── composeApp/
    └── src/
        ├── androidMain/             # Native Android entry point
        ├── iosMain/                 # Native iOS entry point (future)
        └── commonMain/kotlin/com/kareem/gitmatch/
            ├── App.kt               # Root composable & Navigation Host
            │
            ├── core/                # App-wide infrastructure
            │   ├── network/         # Ktor client & backend DTOs (FeedCardDto)
            │   ├── model/           # Domain models (FeedCard)
            │   ├── di/              # Koin modules
            │   └── theme/           # Color (Dark Mode first), Typography
            │
            ├── feature/             # Independent screen modules (MVI)
            │   ├── discover/        # Swiping Deck UI, State, ViewModel
            │   │   └── components/  # SwipeableCard, RepoCardContent
            │   ├── vault/           # Saved items UI
            │   ├── detail/          # Swipe-up expanded view
            │   └── onboarding/      # Initial tech-stack selection
            │
            └── data/                # Data access layer
                ├── repository/      # RepositoryImpl (DTO-to-Domain mapping)
                └── local/           # DataStore preferences (theme/auth)
```

---

## 🧠 Architectural Flow (MVI)

Every feature in the `feature/` directory strictly follows this unidirectional flow:
1. **Composable (`Screen.kt`):** Completely stateless. Observes `UiState` and emits user actions as `Intent` callbacks.
2. **Intent (`Intent.kt`):** A sealed interface representing user actions (e.g., `SwipeRight(id)`).
3. **ViewModel (`ViewModel.kt`):** Receives intents, launches Coroutines, calls Repositories, and updates the `MutableStateFlow<UiState>`.
4. **UiState (`UiState.kt`):** An immutable data class holding the exact visual state of the screen (e.g., `isLoading`, `cardsList`).

---

## 🏃‍♂️ How to Run

> **Note:** Do NOT run this project in VS Code.

1. Open the `/app` folder in **Android Studio** (Fleet or Ladybug recommended).
2. Sync the Gradle project.
3. To run on Android: Select the `composeApp` run configuration and click **Play** (or Shift+F10) with an Android Emulator or physical device attached.
4. Ensure the Spring Boot backend (`/backEnd`) is running on `localhost:8080`, as the Android App (`10.0.2.2` via emulator) will look for the local API.
