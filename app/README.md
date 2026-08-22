# GitMatch Mobile App

Kotlin Multiplatform + Compose Multiplatform app. Currently targets Android.

## What It Does

A swipeable card deck for discovering GitHub repos and tech news. Swipe right to save, left to skip, up to expand for details.

- **Repo cards** show star count, language, an AI summary, and whether it has "good first issues"
- **News cards** show a short TL;DR so you don't have to click through
- **Vault** is where your saved items live

## Tech

KMP, Compose Multiplatform (Material 3), Koin for DI, Ktor for networking, Coil for images, MVI architecture.

## Running

Open the `app/` folder in Android Studio (Ladybug or newer recommended). Sync Gradle, select the `composeApp` run config, and hit Play.

The backend needs to be running on `localhost:8080`. If you're using the Android emulator, the app talks to `10.0.2.2` (the host machine).

## Structure

```
composeApp/src/commonMain/kotlin/com/kareem/gitmatch/
├── core/           # network, models, DI, theme
├── feature/        # screens: discover, vault, detail, onboarding, auth, settings
├── data/           # repositories, local preferences
└── App.kt          # navigation host
```

Each feature follows MVI: `Screen` (stateless) → `Intent` → `ViewModel` → `UiState`.
