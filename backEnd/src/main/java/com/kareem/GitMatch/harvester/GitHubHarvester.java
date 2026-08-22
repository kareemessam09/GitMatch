package com.kareem.GitMatch.harvester;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.entity.RepositoryItem;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import com.kareem.GitMatch.core.repository.RepositoryItemRepository;
import com.kareem.GitMatch.service.GitHubService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class GitHubHarvester {

    private static final Logger log = LoggerFactory.getLogger(GitHubHarvester.class);


    private static final List<String> DEFAULT_LANGUAGES = List.of(
            "Java", "Kotlin", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "Swift"
    );


    private static final java.util.Map<String, String> TOPIC_TO_SEARCH_TERM = java.util.Map.ofEntries(
            java.util.Map.entry("Android", "android"),
            java.util.Map.entry("iOS", "ios"),
            java.util.Map.entry("React", "react"),
            java.util.Map.entry("AI/ML", "machine-learning"),
            java.util.Map.entry("DevOps", "devops"),
            java.util.Map.entry("Web3", "web3"),
            java.util.Map.entry("Flutter", "flutter"),
            java.util.Map.entry("Cloud", "cloud"),
            java.util.Map.entry("Security", "security"),
            java.util.Map.entry("Data Science", "data-science")
    );


    private static final Set<String> LANGUAGE_TOPICS = Set.of(
            "Kotlin", "Java", "Python", "TypeScript", "Rust", "Go", "Swift", "C++"
    );


    private static final int STARTUP_PER_LANGUAGE = 3;

    private final GitHubService gitHubService;
    private final RepositoryItemRepository repoRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    public GitHubHarvester(GitHubService gitHubService,
                           RepositoryItemRepository repoRepository,
                           AppUserRepository appUserRepository,
                           ObjectMapper objectMapper) {
        this.gitHubService = gitHubService;
        this.repoRepository = repoRepository;
        this.appUserRepository = appUserRepository;
        this.objectMapper = objectMapper;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("App started — triggering small seed GitHub harvest...");
        harvestRepositories(STARTUP_PER_LANGUAGE);
    }


    @Scheduled(cron = "${gitmatch.harvester.cron.repos}")
    public void scheduledHarvest() {
        log.info("Scheduled GitHub harvest starting...");
        harvestRepositories(5);
    }


    private List<String> resolveSearchLanguages() {
        Set<String> languages = new LinkedHashSet<>();

        // Collect all preferred topics from all users
        List<AppUser> users = appUserRepository.findAll();
        for (AppUser user : users) {
            if (user.getPreferredTopics() != null && !user.getPreferredTopics().isEmpty()) {
                for (String topic : user.getPreferredTopics().split(",")) {
                    String trimmed = topic.trim();
                    if (LANGUAGE_TOPICS.contains(trimmed)) {
                        languages.add(trimmed);
                    }
                }
            }
            if (user.getPreferredLanguages() != null && !user.getPreferredLanguages().isEmpty()) {
                for (String lang : user.getPreferredLanguages().split(",")) {
                    languages.add(lang.trim());
                }
            }
        }

        if (languages.isEmpty()) {
            log.info("No user language preferences found, using defaults");
            return DEFAULT_LANGUAGES;
        }

        log.info("Resolved user-preferred languages for harvest: {}", languages);
        return List.copyOf(languages);
    }


    private Set<String> resolveSearchTopics() {
        Set<String> searchTopics = new LinkedHashSet<>();

        List<AppUser> users = appUserRepository.findAll();
        for (AppUser user : users) {
            if (user.getPreferredTopics() != null && !user.getPreferredTopics().isEmpty()) {
                for (String topic : user.getPreferredTopics().split(",")) {
                    String trimmed = topic.trim();
                    String searchTerm = TOPIC_TO_SEARCH_TERM.get(trimmed);
                    if (searchTerm != null) {
                        searchTopics.add(searchTerm);
                    }
                }
            }
        }

        log.info("Resolved user-preferred topics for harvest: {}", searchTopics);
        return searchTopics;
    }


    private void harvestRepositories(int limitPerLanguage) {
        int totalNew = 0;

        // 1. Harvest by programming language
        List<String> languages = resolveSearchLanguages();
        for (String language : languages) {
            totalNew += harvestByLanguage(language, limitPerLanguage);
        }

        // 2. Harvest by topic-based search terms (e.g., "android", "machine-learning")
        Set<String> topics = resolveSearchTopics();
        for (String topic : topics) {
            totalNew += harvestByTopic(topic, limitPerLanguage);
        }

        log.info("GitHub harvest complete. {} new repositories added.", totalNew);
    }

    private int harvestByLanguage(String language, int limit) {
        try {
            String responseBody = gitHubService.searchRepositories(language, 1);
            return parseAndSaveRepos(responseBody, language, limit);
        } catch (Exception e) {
            log.error("Harvest failed for language '{}': {}", language, e.getMessage());
            return 0;
        }
    }

    private int harvestByTopic(String topic, int limit) {
        try {
            String responseBody = gitHubService.searchByTopic(topic, 1);
            return parseAndSaveRepos(responseBody, "topic:" + topic, limit);
        } catch (Exception e) {
            log.error("Harvest failed for topic '{}': {}", topic, e.getMessage());
            return 0;
        }
    }

    private int parseAndSaveRepos(String responseBody, String queryLabel, int limit) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");

            if (!items.isArray()) {
                log.warn("No items array in GitHub response for: {}", queryLabel);
                return 0;
            }

            int added = 0;
            for (JsonNode item : items) {
                if (added >= limit) break;

                String githubId = String.valueOf(item.path("id").asLong());
                if (repoRepository.existsByGithubId(githubId)) continue;

                String avatarUrl = item.path("owner").path("avatar_url").asText(null);


                StringBuilder topicsSb = new StringBuilder();
                JsonNode topicsNode = item.path("topics");
                if (topicsNode.isArray()) {
                    for (int i = 0; i < topicsNode.size(); i++) {
                        if (i > 0) topicsSb.append(",");
                        topicsSb.append(topicsNode.get(i).asText());
                    }
                }
                String topics = topicsSb.length() > 0 ? topicsSb.toString() : null;

                Integer openIssuesCount = item.path("open_issues_count").asInt(0);
                String licenseName = item.path("license").path("spdx_id").asText(null);

                RepositoryItem repo = new RepositoryItem(
                        githubId,
                        item.path("name").asText(),
                        item.path("owner").path("login").asText(),
                        item.path("html_url").asText(),
                        item.path("language").asText(null),
                        item.path("stargazers_count").asInt(0),
                        item.path("forks_count").asInt(0),
                        item.path("description").asText(null),
                        avatarUrl,
                        topics,
                        openIssuesCount,
                        licenseName
                );

                repoRepository.save(repo);
                added++;
            }

            log.info("Harvested '{}' — {} new repos (limit: {})", queryLabel, added, limit);
            return added;
        } catch (Exception e) {
            log.error("Failed to parse repos for '{}': {}", queryLabel, e.getMessage());
            return 0;
        }
    }
}
