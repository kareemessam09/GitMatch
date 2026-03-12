# GitMatch Backend — Claude Skills & Rules

> **READ THIS FILE FIRST before writing any code in this project.**
> This is the single source of truth for coding standards, conventions, and architectural rules.

---

## 1. Project Identity

| Key              | Value                                      |
|------------------|--------------------------------------------|
| Project Name     | GitMatch                                   |
| Base Package     | `com.kareem.GitMatch`                      |
| Language         | Java 21                                    |
| Framework        | Spring Boot 4.0.3                          |
| Build Tool       | Maven (`pom.xml`)                          |
| Database         | PostgreSQL (via Spring Data JPA)           |
| Security         | Spring Security (OAuth2 / JWT — to be configured) |
| AI Provider      | Google Gemini API (REST / HTTP)            |
| Source Root       | `src/main/java/com/kareem/GitMatch/`       |
| Test Root         | `src/test/java/com/kareem/GitMatch/`       |
| Config File       | `src/main/resources/application.properties` (may migrate to `application.yml`) |

---

## 2. Package Structure & File Placement

Always place classes in the correct sub-package under `com.kareem.GitMatch`. Never create classes directly inside the base package except `GitMatchApplication.java`.

```
com.kareem.GitMatch
├── GitMatchApplication.java          # Entry point — DO NOT MODIFY unless adding @Enable* annotations
│
├── api/                              # REST Controllers ONLY
│   ├── FeedController.java
│   ├── SwipeController.java
│   └── UserController.java
│
├── core/
│   ├── entity/                       # JPA @Entity classes ONLY
│   │   ├── RepositoryItem.java
│   │   ├── NewsItem.java
│   │   ├── AppUser.java
│   │   └── SwipeAction.java
│   ├── repository/                   # Spring Data JPA interfaces ONLY
│   │   ├── RepositoryItemRepository.java
│   │   ├── NewsItemRepository.java
│   │   ├── AppUserRepository.java
│   │   └── SwipeActionRepository.java
│   └── enums/
│       ├── SwipeDirection.java       # RIGHT, LEFT, UP
│       ├── FeedItemType.java         # REPO, NEWS
│       └── ContentSource.java        # GITHUB, DEVTO, HACKERNEWS, MEDIUM, RELEASE_NOTES
│
├── service/                          # Business logic
│   ├── FeedService.java
│   ├── SwipeService.java
│   └── GitHubService.java
│
├── harvester/                        # Scheduled data-fetching jobs
│   ├── GitHubHarvester.java
│   └── NewsHarvester.java
│
├── ai/                               # AI integration
│   ├── GeminiClient.java
│   ├── AiProcessorService.java
│   └── dto/                          # AI-specific request/response DTOs
│       ├── GeminiRequest.java
│       └── GeminiResponse.java
│
├── dto/                              # DTOs for mobile API communication
│   ├── request/
│   │   ├── SwipeRequest.java
│   │   └── UserPreferenceRequest.java
│   └── response/
│       └── FeedCardResponse.java
│
└── config/
    ├── AsyncConfig.java
    ├── SecurityConfig.java
    └── RestClientConfig.java
```

### Rules:
- **Controllers** (`api/`) must NEVER contain business logic. They only validate input, call a Service, and return a response.
- **Services** (`service/`) contain all business logic. They call Repositories and other Services.
- **Repositories** (`core/repository/`) are interfaces extending `JpaRepository`. No implementation classes needed.
- **Entities** (`core/entity/`) are JPA-managed POJOs. They must NOT contain business logic or call other services.
- **DTOs** (`dto/`) transfer data between layers. Entities must NEVER be returned directly from Controllers.
- **Harvester** (`harvester/`) classes are `@Component` beans with `@Scheduled` methods. They call Services to persist data.
- **AI** (`ai/`) classes handle all interactions with external AI APIs. They are called by Services, never by Controllers directly.

---

## 3. Naming Conventions

| Element                  | Convention                              | Example                           |
|--------------------------|-----------------------------------------|-----------------------------------|
| Classes                  | PascalCase                              | `FeedService`, `SwipeController`  |
| Methods                  | camelCase, verb-first                   | `getDiscoverFeed()`, `recordSwipe()` |
| Variables                | camelCase                               | `starCount`, `isReleaseNote`      |
| Constants                | UPPER_SNAKE_CASE                        | `MAX_FEED_PAGE_SIZE`              |
| Entity classes           | Singular noun                           | `RepositoryItem` (not `RepositoryItems`) |
| Repository interfaces    | `{Entity}Repository`                    | `RepositoryItemRepository`        |
| DB table names           | snake_case, plural                      | `repository_items`, `news_items`  |
| DB column names          | snake_case                              | `star_count`, `github_id`         |
| REST endpoints           | kebab-case, plural nouns                | `/api/v1/feed-cards`, `/api/v1/swipes` |
| DTO classes              | `{Purpose}Request` / `{Purpose}Response`| `SwipeRequest`, `FeedCardResponse`|
| Enums                    | PascalCase class, UPPER_CASE values     | `SwipeDirection.RIGHT`            |
| Test classes             | `{ClassUnderTest}Tests`                 | `FeedServiceTests`                |

---

## 4. Coding Standards

### 4.1 General
- Use Java 21 features wherever appropriate (records for DTOs, pattern matching, text blocks for prompts, sealed interfaces if needed).
- Prefer **constructor injection** over field injection (`@Autowired` on fields is FORBIDDEN).
- All injected dependencies must be `private final`.
- Use Lombok ONLY if already in `pom.xml`. Do NOT add Lombok as a new dependency — use Java Records for DTOs and write explicit constructors/getters for entities.
- Every public method in a Service must have a clear Javadoc comment explaining its purpose.

### 4.2 REST Controllers
```java
// ✅ CORRECT PATTERN
@RestController
@RequestMapping("/api/v1/feed-cards")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<List<FeedCardResponse>> getDiscoverFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getDiscoverFeed(page, size));
    }
}
```
- All endpoints are versioned under `/api/v1/`.
- Always return `ResponseEntity<T>`.
- Use `@Valid` on request body DTOs.
- Never throw raw exceptions from controllers — use `@ControllerAdvice` + `@ExceptionHandler` for global error handling.

### 4.3 Entities
```java
// ✅ CORRECT PATTERN
@Entity
@Table(name = "repository_items")
public class RepositoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String githubId;

    // ... other fields

    // No-arg constructor required by JPA
    protected RepositoryItem() {}

    // All-args constructor for application use
    public RepositoryItem(String githubId, String name, ...) { ... }

    // Getters (and setters only when truly needed)
}
```
- Use `UUID` for all primary keys (`@GeneratedValue(strategy = GenerationType.UUID)`).
- Specify `@Table(name = "...")` and `@Column(name = "...")` explicitly.
- Include `createdAt` and `updatedAt` fields on every entity using `@CreationTimestamp` / `@UpdateTimestamp`.
- The user entity MUST be named `AppUser` (not `User`) to avoid conflicts with PostgreSQL's reserved `user` keyword.

### 4.4 Repositories
```java
// ✅ CORRECT PATTERN
public interface RepositoryItemRepository extends JpaRepository<RepositoryItem, UUID> {

    boolean existsByGithubId(String githubId);

    @Query("SELECT r FROM RepositoryItem r WHERE r.id NOT IN " +
           "(SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<RepositoryItem> findUnswipedRepos(@Param("userId") UUID userId, Pageable pageable);
}
```
- Use derived query methods for simple lookups.
- Use `@Query` with JPQL for anything complex. Avoid native SQL unless absolutely necessary.
- Always use `Pageable`/`Page` for any endpoint that could return many results.

### 4.5 Services
- Annotate with `@Service`.
- Use `@Transactional` on methods that perform write operations.
- Use `@Transactional(readOnly = true)` on read-only methods.
- Log important operations using SLF4J: `private static final Logger log = LoggerFactory.getLogger(MyService.class);`

### 4.6 Error Handling
Create a global exception handler:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle ResourceNotFoundException → 404
    // Handle IllegalArgumentException → 400
    // Handle generic Exception → 500 with a safe message
}
```
- Define custom exceptions in a `com.kareem.GitMatch.exception` package (e.g., `ResourceNotFoundException`, `ExternalApiException`).
- NEVER expose stack traces or internal details to the client.

---

## 5. AI Integration Rules

### 5.1 Gemini API Communication
- All Gemini API calls go through `GeminiClient.java`. No other class may call the Gemini API directly.
- Use Spring's `RestClient` (preferred in Spring Boot 4.x) or `WebClient` for HTTP calls.
- Store the Gemini API key in `application.properties` as `gitmatch.ai.gemini.api-key` and inject it via `@Value`. NEVER hardcode API keys.

### 5.2 Prompts
- All AI prompts must be stored as `String` constants or loaded from resource files — NOT inline in method bodies.
- Every prompt MUST instruct the AI to return **strict JSON**. Example:

```java
public static final String REPO_SUMMARY_PROMPT = """
    You are a technical assistant. Analyze the following GitHub repository README.
    Return ONLY a valid JSON object with these exact keys:
    {
      "one_sentence_summary": "A single sentence explaining what this repo does.",
      "three_bullet_tldr": ["Bullet 1", "Bullet 2", "Bullet 3"],
      "core_code_snippet": "A short, representative code snippet (max 10 lines).",
      "has_good_first_issues": true/false,
      "primary_technology": "The main language or framework"
    }
    Do NOT include markdown formatting, code fences, or any text outside the JSON object.
    
    README content:
    %s
    """;
```

### 5.3 Async Processing
- AI summarization MUST be done asynchronously using `@Async`.
- The `AsyncConfig.java` must define a custom `ThreadPoolTaskExecutor` with sensible limits (core=2, max=5, queue=100).
- Every `@Async` method must return `CompletableFuture<T>`.
- Always wrap AI calls in try-catch. On failure, log the error and save the entity with a `null` summary (graceful degradation). NEVER let an AI failure crash the Harvester.

---

## 6. Harvester Rules

- Use `@Scheduled(fixedRate = 3600000)` (hourly) or `cron` expressions.
- Add `@EnableScheduling` to `GitMatchApplication.java` or `AsyncConfig.java`.
- The Harvester MUST check if a repository/article already exists in the DB (by `githubId` or `sourceUrl`) before saving — **idempotent inserts only**.
- GitHub API queries should target underrated repos: `stars:100..5000 pushed:>YYYY-MM-DD language:{lang}`.
- Respect API rate limits. Log remaining rate-limit headers from GitHub responses.

---

## 7. DTO Design (Mobile API Contract)

The mobile app expects a unified card format. Both repos and news must map to this single response DTO:

```java
public record FeedCardResponse(
    UUID id,
    FeedItemType type,              // REPO or NEWS
    String title,                   // Repo name or Article title
    String subtitle,                // "owner/repo" or "by Author Name"
    String oneSentenceSummary,      // AI-generated
    List<String> threeBulletTldr,   // AI-generated
    String codeSnippet,             // null for NEWS items
    String language,                // "Java", "Kotlin", etc. — null for NEWS
    Integer starCount,              // null for NEWS
    Boolean hasGoodFirstIssues,     // null for NEWS
    Boolean isReleaseNote,          // null for REPO
    String sourceUrl,               // GitHub URL or Article URL
    LocalDateTime publishedAt
) {}
```

---

## 8. Configuration Properties

Use the `gitmatch.*` namespace for all custom properties:

```properties
# --- Database ---
spring.datasource.url=jdbc:postgresql://localhost:5432/gitmatch
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# --- GitMatch Custom ---
gitmatch.ai.gemini.api-key=${GEMINI_API_KEY}
gitmatch.ai.gemini.model=gemini-2.0-flash
gitmatch.ai.gemini.base-url=https://generativelanguage.googleapis.com/v1beta

gitmatch.github.api-token=${GITHUB_API_TOKEN}
gitmatch.github.min-stars=100
gitmatch.github.max-stars=5000

gitmatch.harvester.cron.repos=0 0 */2 * * *
gitmatch.harvester.cron.news=0 30 */3 * * *

gitmatch.feed.default-page-size=20
```

- NEVER hardcode secrets. Use environment variables (`${...}`) or a `.env` file.
- Create a `@ConfigurationProperties(prefix = "gitmatch")` class to bind these values in a type-safe way.

---

## 9. Testing Standards

- Every Service class must have a corresponding test class.
- Use JUnit 5 + Mockito for unit tests.
- Use `@DataJpaTest` for repository layer tests.
- Use `@WebMvcTest` for controller layer tests (with `@MockBean` for services).
- Test names follow the pattern: `shouldReturnFeedCards_whenUserHasPreferences()`.
- AI integration tests should use mocked responses (never call the real Gemini API in tests).

---

## 10. Git & Code Quality

- Write clear, atomic commit messages: `feat: add FeedController with paged discovery endpoint`.
- Follow conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- Do NOT commit `application.properties` with real credentials. Use `application.properties.example` as a template.

---

## 11. Common Mistakes to AVOID

| ❌ DO NOT                                          | ✅ DO INSTEAD                                              |
|----------------------------------------------------|------------------------------------------------------------|
| Return Entity objects from Controllers             | Map Entities → DTOs in the Service layer                   |
| Use `@Autowired` on fields                         | Use constructor injection                                  |
| Hardcode API keys in source code                   | Use `@Value("${...}")` or `@ConfigurationProperties`      |
| Call Gemini API synchronously in the Harvester     | Use `@Async` + `CompletableFuture`                         |
| Create a JPA entity named `User`                   | Name it `AppUser` to avoid PostgreSQL conflicts            |
| Use `Long` for IDs                                 | Use `UUID` for all primary keys                            |
| Let AI failures crash the scheduled job            | Wrap in try-catch, log, and continue                       |
| Write one giant "do everything" service            | Keep services focused: `FeedService`, `SwipeService`, etc. |
| Skip pagination on list endpoints                  | Always use `Pageable` / `Page<T>`                          |
| Put SQL strings in Controllers or Services         | Use Repository methods or `@Query` annotations             |

---

## 12. Phase Execution Checklist

When asked to implement a phase, follow this order within each phase:
1. **Enums** first (they have no dependencies).
2. **Entities** second (they depend on enums).
3. **Repositories** third (they depend on entities).
4. **Services** fourth (they depend on repositories).
5. **Controllers** last (they depend on services).
6. **Tests** alongside or immediately after each layer.

Always verify the code compiles (`./mvnw compile`) after completing each phase.
