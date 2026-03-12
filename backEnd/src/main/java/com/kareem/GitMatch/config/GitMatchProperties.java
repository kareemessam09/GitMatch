package com.kareem.GitMatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding of all gitmatch.* configuration properties.
 */
@ConfigurationProperties(prefix = "gitmatch")
public record GitMatchProperties(
    AiProperties ai,
    GitHubProperties github,
    HarvesterProperties harvester,
    FeedProperties feed
) {
    public record AiProperties(GeminiProperties gemini) {
        public record GeminiProperties(String apiKey, String model, String baseUrl) {}
    }

    public record GitHubProperties(String apiToken, int minStars, int maxStars) {}

    public record HarvesterProperties(CronProperties cron) {
        public record CronProperties(String repos, String news) {}
    }

    public record FeedProperties(int defaultPageSize) {}
}
