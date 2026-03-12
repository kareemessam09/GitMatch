package com.kareem.GitMatch.service;

import com.kareem.GitMatch.config.GitMatchProperties;
import com.kareem.GitMatch.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Handles interactions with the GitHub REST API, including
 * search queries and starring repositories on behalf of users.
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private final RestClient githubRestClient;
    private final GitMatchProperties properties;

    public GitHubService(@Qualifier("githubRestClient") RestClient githubRestClient,
                         GitMatchProperties properties) {
        this.githubRestClient = githubRestClient;
        this.properties = properties;
    }

    /**
     * Searches GitHub for underrated repositories matching the configured criteria.
     *
     * @param language the programming language to filter by (e.g., "Java")
     * @param page     the result page
     * @return the raw JSON response body from GitHub Search API
     */
    public String searchRepositories(String language, int page) {
        int minStars = properties.github().minStars();
        int maxStars = properties.github().maxStars();
        String query = String.format("stars:%d..%d pushed:>2025-01-01 language:%s", minStars, maxStars, language);

        log.info("Searching GitHub repos: query='{}', page={}", query, page);

        return executeSearch(query, page);
    }

    /**
     * Searches GitHub for repositories matching a topic keyword.
     *
     * @param topic the topic to search for (e.g., "android", "machine-learning")
     * @param page  the result page
     * @return the raw JSON response body from GitHub Search API
     */
    public String searchByTopic(String topic, int page) {
        int minStars = properties.github().minStars();
        int maxStars = properties.github().maxStars();
        String query = String.format("stars:%d..%d pushed:>2025-01-01 topic:%s", minStars, maxStars, topic);

        log.info("Searching GitHub repos by topic: query='{}', page={}", query, page);

        return executeSearch(query, page);
    }

    private String executeSearch(String query, int page) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.github.com/search/repositories")
                    .queryParam("q", query)
                    .queryParam("sort", "updated")
                    .queryParam("order", "desc")
                    .queryParam("per_page", 30)
                    .queryParam("page", page)
                    .build()
                    .toUri();

            return RestClient.builder().build().get()
                    .uri(uri)
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + properties.github().apiToken())
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("GitHub search API call failed: {}", e.getMessage(), e);
            throw new ExternalApiException("GitHub search API call failed", e);
        }
    }

    /**
     * Stars a repository on GitHub using the provided access token.
     *
     * @param owner       the repo owner
     * @param repo        the repo name
     * @param accessToken the user's GitHub OAuth access token
     */
    public void starRepository(String owner, String repo, String accessToken) {
        log.info("Starring repo {}/{} on GitHub", owner, repo);
        try {
            githubRestClient.put()
                    .uri("/user/starred/{owner}/{repo}", owner, repo)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Length", "0")
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully starred {}/{}", owner, repo);
        } catch (Exception e) {
            log.error("Failed to star repo {}/{}: {}", owner, repo, e.getMessage());
            throw new ExternalApiException("Failed to star repository on GitHub", e);
        }
    }
}
