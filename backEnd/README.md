# GitMatch Backend

Spring Boot backend that aggregates GitHub repos and tech news, runs them through Gemini for summaries, and serves a unified feed API to the mobile app.

## Setup

You'll need:
- Java 17+
- PostgreSQL running locally (or a connection string to one)
- A Gemini API key
- A GitHub personal access token (for the harvester)

Environment variables (or set them in `application.properties`):

```
DB_URL=jdbc:postgresql://localhost:5432/gitmatch
DB_USERNAME=...
DB_PASSWORD=...
GEMINI_API_KEY=...
GITHUB_API_TOKEN=...
JWT_SECRET=...          # Base64-encoded, 256-bit
ENCRYPTION_KEY=...     # Base64-encoded AES key for token encryption at rest
```

Then:
```bash
cd backEnd
./mvnw spring-boot:run
```

## Key Packages

- `api/` — REST controllers (feed, swipes, users)
- `service/` — business logic (FeedService, SwipeService)
- `harvester/` — scheduled jobs that pull repos from GitHub and articles from various news sources
- `ai/` — Gemini client and the batch processor that summarizes everything
- `core/entity/` — JPA entities
- `config/` — security, async, encryption setup

## Data Flow

1. GitHubHarvester fetches trending repos (by language + topic), saves them raw
2. NewsHarvester pulls from Dev.to API, HN API, and several RSS feeds
3. AiBatchProcessor picks up unprocessed items one by one, sends them to Gemini, saves the summaries
4. FeedController serves a mixed feed of repos + news to the mobile app
5. When a user swipes right on a repo, the backend stars it on GitHub using their OAuth token

## API Endpoints

All endpoints are under `/api/v1/` and require JWT auth unless noted.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/feed-cards` | Personalized discovery feed |
| GET | `/feed-cards/vault` | User's saved/liked items |
| GET | `/feed-cards/{cardId}` | Single card detail |
| POST | `/swipes` | Record a swipe |
| GET | `/users/me` | Current user profile |
| PUT | `/users/me/preferences` | Update tech preferences |

## Notes

- The harvester runs on startup (small seed) and then on a cron schedule
- Gemini free tier is ~15 req/min, so the batch processor does 1 item every 30 seconds
- OAuth2 login is handled via Spring Security — the success handler creates the JWT
- Tokens (GitHub access token, etc.) are encrypted at rest with AES-256-GCM
