# GitMatch App — Claude Skills & Rules (KMP + Compose Multiplatform)

> **READ THIS FILE FIRST before writing any code in the mobile app.**
> This is the single source of truth for coding standards, conventions, architecture, and UI rules for the KMP mobile application.

---

## 1. Project Identity

| Key              | Value                                                    |
|------------------|----------------------------------------------------------|
| Project Name     | GitMatch                                                 |
| Platform         | Kotlin Multiplatform (KMP) — targeting **Android** first |
| UI Framework     | Compose Multiplatform                                    |
| Networking       | Ktor Client                                              |
| Serialization    | Kotlinx.serialization                                    |
| DI               | Koin                                                     |
| Image Loading    | Coil 3 (Compose Multiplatform compatible)                |
| Navigation       | Compose Navigation (Jetpack / Multiplatform)             |
| State Management | Kotlin Coroutines + Flow + ViewModel                     |
| Local Storage    | DataStore (Preferences) for settings, SQLDelight for offline cache (optional) |
| Min SDK (Android)| 26                                                       |
| Kotlin Version   | Latest stable (2.1.x+)                                  |
| Build Tool       | Gradle (Kotlin DSL — `build.gradle.kts`)                 |
| IDE              | Android Studio (user runs here, NOT in VS Code)          |

---

## 2. Architecture: Clean MVI (Model–View–Intent)

Use a strict **unidirectional data flow** architecture. This keeps the swiping UI predictable and testable.

```
[User Gesture/Action]
        ↓
    [Intent]        ← User swipes right, taps a tab, etc.
        ↓
  [ViewModel]       ← Processes intent, calls Repository/UseCase
        ↓
   [UiState]        ← Immutable data class emitted via StateFlow
        ↓
  [Composable]      ← Renders the UI based on UiState only
```

### Rules:
- Composables are **stateless renderers**. They receive a `UiState` and emit `Intent` callbacks. They NEVER call repositories or perform logic.
- ViewModels hold `StateFlow<UiState>` and expose an `onIntent(intent: Intent)` function.
- Repositories abstract data sources (remote API via Ktor, local cache).
- Use Cases (optional) encapsulate a single business operation (e.g., `SwipeRightUseCase`).

---

## 3. Module / Package Structure

Place all shared logic in `commonMain`. Platform-specific code goes in `androidMain` / `iosMain` only when absolutely necessary.

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/kareem/gitmatch/
│   │   ├── App.kt                          # Root Composable + Navigation host
│   │   │
│   │   ├── core/
│   │   │   ├── network/
│   │   │   │   ├── GitMatchApi.kt          # Ktor HttpClient setup + API endpoints
│   │   │   │   ├── dto/
│   │   │   │   │   └── FeedCardDto.kt      # Mirrors backend FeedCardResponse exactly
│   │   │   │   └── NetworkResult.kt        # Sealed class: Success / Error / Loading
│   │   │   ├── model/
│   │   │   │   ├── FeedCard.kt             # Domain model (mapped from DTO)
│   │   │   │   ├── SwipeDirection.kt       # Enum: RIGHT, LEFT, UP
│   │   │   │   └── FeedItemType.kt         # Enum: REPO, NEWS
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt            # Koin module definitions
│   │   │   └── theme/
│   │   │       ├── Theme.kt               # Material 3 theme & colors
│   │   │       ├── Color.kt
│   │   │       └── Type.kt
│   │   │
│   │   ├── feature/
│   │   │   ├── discover/                   # The main swiping screen
│   │   │   │   ├── DiscoverScreen.kt       # Composable — the card deck
│   │   │   │   ├── DiscoverViewModel.kt
│   │   │   │   ├── DiscoverUiState.kt
│   │   │   │   ├── DiscoverIntent.kt
│   │   │   │   └── components/
│   │   │   │       ├── SwipeableCard.kt    # Individual swipe card composable
│   │   │   │       ├── RepoCardContent.kt  # Card face for repositories
│   │   │   │       ├── NewsCardContent.kt  # Card face for news/releases
│   │   │   │       ├── TabRow.kt           # "For You" / "Repos" / "News" tabs
│   │   │   │       └── SwipeHint.kt        # Overlay icons (❤️ ✕ ℹ️) during drag
│   │   │   │
│   │   │   ├── vault/                      # Saved / Liked items
│   │   │   │   ├── VaultScreen.kt
│   │   │   │   ├── VaultViewModel.kt
│   │   │   │   ├── VaultUiState.kt
│   │   │   │   └── components/
│   │   │   │       └── SavedCardItem.kt
│   │   │   │
│   │   │   ├── detail/                     # "Swipe Up" expanded detail view
│   │   │   │   ├── DetailScreen.kt
│   │   │   │   └── DetailViewModel.kt
│   │   │   │
│   │   │   ├── onboarding/                 # First-launch interest picker
│   │   │   │   ├── OnboardingScreen.kt
│   │   │   │   └── OnboardingViewModel.kt
│   │   │   │
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   │
│   │   └── data/
│   │       ├── repository/
│   │       │   ├── FeedRepository.kt       # Interface
│   │       │   ├── FeedRepositoryImpl.kt   # Calls GitMatchApi, maps DTOs → domain
│   │       │   ├── SwipeRepository.kt
│   │       │   └── SwipeRepositoryImpl.kt
│   │       └── local/
│   │           └── PreferencesManager.kt   # DataStore for onboarding prefs, theme
│   │
│   ├── androidMain/kotlin/com/kareem/gitmatch/
│   │   └── Platform.android.kt             # Android-specific expect/actual
│   │
│   └── commonMain/resources/
│       └── (drawable assets, fonts, etc.)
│
├── build.gradle.kts
└── (Android manifests, etc.)
```

### Placement Rules:
- **`core/`** — Shared infrastructure: networking, DI, theming, base models. Feature modules depend on `core/`, never the reverse.
- **`feature/`** — Each screen is a self-contained feature package with its own Screen, ViewModel, UiState, Intent, and `components/` sub-package.
- **`data/`** — Repository layer. Feature ViewModels call repositories, never Ktor directly.
- **One composable per file.** A file named `SwipeableCard.kt` contains only `@Composable fun SwipeableCard(...)`.

---

## 4. Naming Conventions

| Element              | Convention                                  | Example                                   |
|----------------------|---------------------------------------------|-------------------------------------------|
| Packages             | all lowercase, no underscores               | `com.kareem.gitmatch.feature.discover`    |
| Composables          | PascalCase, noun/noun-phrase                | `SwipeableCard`, `DiscoverScreen`         |
| ViewModels           | `{Feature}ViewModel`                        | `DiscoverViewModel`                       |
| UiState classes      | `{Feature}UiState`                          | `DiscoverUiState`                         |
| Intent sealed class  | `{Feature}Intent`                           | `DiscoverIntent`                          |
| Repository interface | `{Domain}Repository`                        | `FeedRepository`                          |
| Repository impl      | `{Domain}RepositoryImpl`                    | `FeedRepositoryImpl`                      |
| DTO classes          | `{Name}Dto`                                 | `FeedCardDto`                             |
| Domain models        | Plain name                                  | `FeedCard`                                |
| Event/Callback params| `on` + Verb                                 | `onSwipeRight`, `onTabSelected`           |
| Boolean variables    | `is`/`has`/`should` prefix                  | `isLoading`, `hasGoodFirstIssues`         |
| Constants            | UPPER_SNAKE_CASE in companion object        | `DEFAULT_PAGE_SIZE`                       |
| Files                | One public declaration per file, name matches| `SwipeableCard.kt`, `FeedRepository.kt`  |

---

## 5. Coding Standards

### 5.1 General Kotlin
- Use **Kotlin idioms**: `data class`, `sealed class`, `sealed interface`, extension functions, `when` expressions, scope functions (`let`, `also`, `apply`).
- Prefer `val` over `var`. Mutability is only acceptable inside ViewModel internal state.
- Use `data class` for UiState. Use `sealed interface` for Intent and navigation events.
- NEVER use `!!` (not-null assertion). Use `?.let {}`, `?: default`, or `requireNotNull()` with a clear message.
- Use Kotlin Coroutines for all async work. NEVER use callbacks or `Thread`.
- All suspending work in ViewModels must be launched in `viewModelScope`.

### 5.2 Compose UI
```kotlin
// ✅ CORRECT PATTERN — Stateless composable
@Composable
fun SwipeableCard(
    card: FeedCard,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // UI only — no state mutation, no API calls
}
```
- Composables accept data and callbacks as parameters. NO side effects inside composables except `LaunchedEffect` / `SideEffect`.
- Always provide a `modifier: Modifier = Modifier` as the LAST parameter.
- Use `Material 3` components and theming (MaterialTheme.colorScheme, MaterialTheme.typography).
- Extract reusable UI into the `components/` sub-package of the feature.
- Use `remember` and `derivedStateOf` for computed values. Use `rememberSaveable` for values that survive config changes.
- Animations: Use `animateFloatAsState`, `Animatable`, and `pointerInput` for the swipe gesture. The swipe must feel snappy and physical (use spring-based animations).

### 5.3 ViewModel Pattern
```kotlin
// ✅ CORRECT PATTERN
class DiscoverViewModel(
    private val feedRepository: FeedRepository,
    private val swipeRepository: SwipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    fun onIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.SwipeRight -> handleSwipeRight(intent.cardId)
            is DiscoverIntent.SwipeLeft -> handleSwipeLeft(intent.cardId)
            is DiscoverIntent.SwipeUp -> handleSwipeUp(intent.cardId)
            is DiscoverIntent.LoadNextPage -> loadFeed()
            is DiscoverIntent.SelectTab -> filterByTab(intent.tab)
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            feedRepository.getDiscoverFeed(page, size)
                .onSuccess { cards ->
                    _uiState.update { it.copy(cards = cards, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
```
- NEVER expose `MutableStateFlow` publicly. Only expose `StateFlow`.
- Inject dependencies via constructor (Koin handles instantiation).
- Catch all exceptions inside `viewModelScope.launch`. NEVER let a crash escape.

### 5.4 UiState & Intent
```kotlin
// ✅ CORRECT PATTERN
data class DiscoverUiState(
    val cards: List<FeedCard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: FeedTab = FeedTab.FOR_YOU
)

sealed interface DiscoverIntent {
    data class SwipeRight(val cardId: String) : DiscoverIntent
    data class SwipeLeft(val cardId: String) : DiscoverIntent
    data class SwipeUp(val cardId: String) : DiscoverIntent
    data class SelectTab(val tab: FeedTab) : DiscoverIntent
    data object LoadNextPage : DiscoverIntent
}

enum class FeedTab { FOR_YOU, REPOS, NEWS }
```
- UiState is a single `data class` with sensible defaults.
- Intent is a `sealed interface` with `data class` or `data object` subtypes.
- NEVER put functions or behavior inside UiState or Intent.

---

## 6. Networking (Ktor Client)

### 6.1 API Client Setup
```kotlin
// ✅ CORRECT PATTERN
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }
    install(Logging) {
        level = LogLevel.BODY  // Use LogLevel.NONE in production
    }
    defaultRequest {
        url("http://10.0.2.2:8080/api/v1/")  // Android emulator → localhost
        contentType(ContentType.Application.Json)
    }
}
```
- Use `kotlinx.serialization` with `@Serializable` on all DTOs.
- Base URL must be configurable (not hardcoded). Use a `BuildConfig` field or a config object.
- The Android emulator accesses the host machine's `localhost` via `10.0.2.2`. A real device needs the machine's LAN IP.

### 6.2 DTO ↔ Domain Mapping
The backend sends `FeedCardResponse`. The app has a matching DTO and a separate domain model:

```kotlin
// DTO — mirrors the backend JSON exactly
@Serializable
data class FeedCardDto(
    val id: String,
    val type: String,                   // "REPO" or "NEWS"
    val title: String,
    val subtitle: String?,
    val oneSentenceSummary: String?,
    val threeBulletTldr: List<String>?,
    val codeSnippet: String?,
    val language: String?,
    val starCount: Int?,
    val hasGoodFirstIssues: Boolean?,
    val isReleaseNote: Boolean?,
    val sourceUrl: String,
    val publishedAt: String?            // ISO 8601 string
)

// Domain model — used by UI
data class FeedCard(
    val id: String,
    val type: FeedItemType,
    val title: String,
    val subtitle: String,
    val summary: String,
    val bullets: List<String>,
    val codeSnippet: String?,
    val language: String?,
    val starCount: Int?,
    val hasGoodFirstIssues: Boolean,
    val isReleaseNote: Boolean,
    val sourceUrl: String,
    val publishedAt: String?
)

// Mapper extension
fun FeedCardDto.toDomain(): FeedCard = FeedCard(
    id = id,
    type = FeedItemType.valueOf(type),
    title = title,
    subtitle = subtitle.orEmpty(),
    summary = oneSentenceSummary.orEmpty(),
    bullets = threeBulletTldr.orEmpty(),
    codeSnippet = codeSnippet,
    language = language,
    starCount = starCount,
    hasGoodFirstIssues = hasGoodFirstIssues ?: false,
    isReleaseNote = isReleaseNote ?: false,
    sourceUrl = sourceUrl,
    publishedAt = publishedAt
)
```

### Rules:
- DTOs are `@Serializable` and match backend JSON keys exactly.
- Domain models are plain `data class` (NOT `@Serializable`).
- Mapping happens in the Repository layer, NEVER in ViewModel or Composables.
- Wrap all Ktor calls in `runCatching {}` or a custom `NetworkResult` sealed class. NEVER let network exceptions crash the app.

---

## 7. Dependency Injection (Koin)

```kotlin
// ✅ CORRECT PATTERN
val appModule = module {
    // Network
    single { provideHttpClient() }
    single { GitMatchApi(get()) }

    // Repositories
    single<FeedRepository> { FeedRepositoryImpl(get()) }
    single<SwipeRepository> { SwipeRepositoryImpl(get()) }

    // ViewModels
    viewModel { DiscoverViewModel(get(), get()) }
    viewModel { VaultViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
}
```
- Use `single {}` for singletons (HttpClient, Repositories).
- Use `viewModel {}` for ViewModels.
- Bind to interfaces: `single<FeedRepository> { FeedRepositoryImpl(get()) }`.
- Initialize Koin in the platform-specific entry point (e.g., `Application.onCreate()` for Android).

---

## 8. Navigation

```kotlin
// ✅ CORRECT PATTERN
sealed interface Screen {
    @Serializable data object Onboarding : Screen
    @Serializable data object Discover : Screen
    @Serializable data object Vault : Screen
    @Serializable data class Detail(val cardId: String) : Screen
    @Serializable data object Settings : Screen
}
```
- Use Compose Navigation with type-safe routes.
- Define all routes in a single `Screen` sealed interface.
- The `NavHost` lives in `App.kt`. Each screen is a `composable<Screen.X> { }` block.
- Pass only IDs between screens (e.g., `cardId: String`), NEVER full data objects.

---

## 9. The Swipe Gesture — Implementation Guide

This is the signature UX of the app. It must feel premium.

### Gesture Detection
```kotlin
// Use Modifier.pointerInput to detect drag gestures
Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDragEnd = { /* Determine swipe direction based on velocity/offset */ },
        onDrag = { change, dragAmount ->
            // Update card offset & rotation in real time
        }
    )
}
```

### Visual Feedback During Drag
- **Dragging Right:** Card tilts clockwise. A green "❤️ LIKE" overlay fades in.
- **Dragging Left:** Card tilts counter-clockwise. A red "✕ NOPE" overlay fades in.
- **Dragging Up:** Card lifts upward. A blue "ℹ️ MORE" overlay fades in.
- The overlay opacity is proportional to the drag distance (0% at center → 100% at threshold).

### Snap/Dismiss Thresholds
- If the user releases the card **before** reaching the threshold (e.g., 120dp offset), animate it back to center with a spring animation.
- If the card **passes** the threshold, animate it flying off-screen in the swipe direction, then trigger the corresponding intent.

### Card Stack
- Show a maximum of **3 cards** stacked (top card interactive, 2 behind slightly scaled down and offset for depth illusion).
- When the top card is dismissed, the second card animates upward to become the new top card.

---

## 10. Card Content Design

### Repository Card
```
┌────────────────────────────────┐
│  ⭐ 1,240          Kotlin 🟣  │  ← Star count + Language badge
│                                │
│   AndroidPoet / DevTools       │  ← owner/name
│                                │
│   "A debug overlay for        │
│    Compose apps that shows     │  ← AI 1-sentence summary
│    recomposition counts"       │
│                                │
│   ```kotlin                    │
│   DevToolsOverlay {            │  ← Core code snippet (syntax highlighted)
│     MyApp()                    │
│   }                            │
│   ```                          │
│                                │
│  🏷️ Good First Issues          │  ← Badge (only if true)
└────────────────────────────────┘
```

### News / Release Notes Card
```
┌────────────────────────────────┐
│  📰 NEWS        Mar 10, 2026  │  ← Type badge + Date
│  (or 🚀 RELEASE)              │  ← Release notes variant
│                                │
│   "Kotlin 2.2 Released:       │
│    What You Need to Know"      │  ← Article title
│                                │
│   • Context receivers stable   │
│   • New K2 compiler default    │  ← AI 3-bullet TL;DR
│   • Compose compiler merged    │
│                                │
│   by Jane Smith — Dev.to       │  ← Author + Source
└────────────────────────────────┘
```

---

## 11. Theming & Colors

Use Material 3 dynamic color where available, with a strong fallback palette:

| Purpose              | Light Mode   | Dark Mode    |
|----------------------|--------------|--------------|
| Primary              | `#6C63FF`    | `#9D97FF`    |
| Background           | `#FAFAFA`    | `#121212`    |
| Card Surface         | `#FFFFFF`    | `#1E1E1E`    |
| Swipe Right Overlay  | `#4CAF50` (green)  | same   |
| Swipe Left Overlay   | `#F44336` (red)    | same   |
| Swipe Up Overlay     | `#2196F3` (blue)   | same   |
| Code Snippet BG      | `#F5F5F5`   | `#2D2D2D`    |
| Star Badge           | `#FFC107` (amber)  | same   |
| Good First Issue Tag | `#7057FF` (purple) | same   |

- Default to **dark mode**. Developers prefer dark themes.
- Cards should have subtle rounded corners (`16.dp`) and a light shadow/elevation.

---

## 12. Common Mistakes to AVOID

| ❌ DO NOT                                          | ✅ DO INSTEAD                                                |
|----------------------------------------------------|--------------------------------------------------------------|
| Expose MutableStateFlow from ViewModel             | Expose `StateFlow` via `.asStateFlow()`                      |
| Call API directly from a Composable                | Call it in ViewModel → Repository → Ktor                     |
| Use `!!` anywhere                                  | Use safe calls `?.`, `?: default`, or `requireNotNull()`     |
| Pass full objects via navigation                   | Pass an ID string, let the destination ViewModel load data   |
| Return DTOs to the UI layer                        | Map DTO → Domain model in the Repository                     |
| Use `Thread.sleep()` or callbacks                  | Use `delay()`, Coroutines, and Flow                          |
| Hardcode base URL strings                          | Use a config object or BuildConfig field                     |
| Put business logic in Composables                  | Keep Composables as pure renderers of UiState                |
| Create massive god-composables                     | Extract components into the `components/` sub-package        |
| Skip error handling on network calls               | Wrap in `runCatching` or use `NetworkResult` sealed class    |
| Use `@Autowired` / Java patterns in Kotlin         | Use idiomatic Kotlin (constructor params, Koin)              |
| Animate with `Thread` or `Handler`                 | Use Compose animation APIs (`animateFloatAsState`, etc.)     |

---

## 13. Phase Execution Checklist

When asked to implement a phase, follow this order:
1. **Models & Enums** — Domain classes, sealed interfaces, enums.
2. **Network Layer** — DTOs, Ktor client, API class.
3. **Repository Layer** — Interface + Implementation with DTO→Domain mapping.
4. **ViewModel** — UiState, Intent, ViewModel logic.
5. **UI / Composables** — Screen composable + extracted components.
6. **DI Wiring** — Register everything in Koin module.
7. **Navigation** — Add the screen to the NavHost.

Build and run after each phase.

---

## 14. Key Files Quick Reference

| Need to...                     | Go to                                          |
|--------------------------------|------------------------------------------------|
| Add a new API endpoint call    | `core/network/GitMatchApi.kt`                  |
| Add a new screen               | `feature/{name}/` — create Screen, VM, UiState |
| Change the theme/colors        | `core/theme/Theme.kt`, `Color.kt`              |
| Add a new dependency (Koin)    | `core/di/AppModule.kt`                          |
| Add a navigation route         | `App.kt` NavHost + `Screen` sealed interface   |
| Change base URL                | `core/network/GitMatchApi.kt` client config    |
| Add a new domain model         | `core/model/`                                  |
| Add a shared UI component      | `feature/{name}/components/`                   |
