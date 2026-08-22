# GitMatch

A Tinder-style app for discovering GitHub repos and tech news. Swipe right to save/star, left to skip, up for details.

[▶ Watch the preview video](https://lnkd.in/p/eAeut4gz)

Built with Kotlin Multiplatform (Compose) on the frontend and Spring Boot + PostgreSQL on the backend. Uses Gemini API to generate short summaries of repos and articles.

## Tech Stack

| Layer | Tech |
|-------|------|
| Frontend | Kotlin Multiplatform, Compose Multiplatform |
| Backend | Spring Boot (Java), Spring Data JPA |
| Database | PostgreSQL |
| AI | Gemini API |
| Auth | OAuth2 (GitHub, Google) + JWT |

## Running

### Backend

```bash
cd backEnd
# set up your DB_URL, GEMINI_API_KEY, etc. in application.properties or env vars
./mvnw spring-boot:run
```

### Mobile App

Open the `app/` folder in Android Studio, sync Gradle, and run on an emulator or device. Make sure the backend is running on `localhost:8080`.

## How It Works

- **GitHubHarvester** pulls trending repos from the GitHub API on a schedule
- **NewsHarvester** pulls articles from Dev.to, Hacker News, Medium RSS, Google News RSS, and a few other sources
- **AiBatchProcessor** sends unprocessed items to Gemini one at a time (stays under the free tier rate limit)
- The mobile app shows a swiping deck of repos + news. Swiping right on a repo also stars it on GitHub if the user has OAuth connected.
