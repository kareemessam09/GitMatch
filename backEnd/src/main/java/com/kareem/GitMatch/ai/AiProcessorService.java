package com.kareem.GitMatch.ai;

import com.kareem.GitMatch.ai.dto.GeminiResponse;
import com.kareem.GitMatch.core.entity.NewsItem;
import com.kareem.GitMatch.core.entity.RepositoryItem;
import com.kareem.GitMatch.core.repository.NewsItemRepository;
import com.kareem.GitMatch.core.repository.RepositoryItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Async service that processes entities through the Gemini AI to generate
 * summaries, bullet points, code snippets, and tags.
 */
@Service
public class AiProcessorService {

    private static final Logger log = LoggerFactory.getLogger(AiProcessorService.class);

    private final GeminiClient geminiClient;
    private final RepositoryItemRepository repoRepository;
    private final NewsItemRepository newsRepository;
    private final ObjectMapper objectMapper;

    /**
     * Prompt template for repository summarization.
     * Instructs Gemini to return strict JSON.
     */
    public static final String REPO_SUMMARY_PROMPT = """
            You are a technical assistant. Analyze the following GitHub repository information.
            Return ONLY a valid JSON object with these exact keys:
            {
              "one_sentence_summary": "A single sentence explaining what this repo does.",
              "three_bullet_tldr": ["Bullet 1", "Bullet 2", "Bullet 3"],
              "why_interesting": "2 sentences about what makes this project stand out or unique.",
              "ideal_for": "Who this repo is ideal for, e.g. Beginners, Data Scientists, React Developers",
              "has_good_first_issues": true/false,
              "primary_technology": "The main language or framework"
            }
            Do NOT include markdown formatting, code fences, or any text outside the JSON object.
            
            Repository: %s
            Owner: %s
            Language: %s
            Stars: %d
            Forks: %d
            Open Issues: %d
            Topics: %s
            Description: %s
            """;

    /**
     * Prompt template for news article summarization.
     */
    public static final String NEWS_SUMMARY_PROMPT = """
            You are a technical assistant. Analyze the following tech news article or release note.
            Return ONLY a valid JSON object with these exact keys:
            {
              "one_sentence_summary": "A single sentence summarizing the article.",
              "three_bullet_tldr": ["Key point 1", "Key point 2", "Key point 3"],
              "is_release_note": true/false
            }
            Do NOT include markdown formatting, code fences, or any text outside the JSON object.
            
            Title: %s
            Author: %s
            URL: %s
            """;

    public AiProcessorService(GeminiClient geminiClient,
                               RepositoryItemRepository repoRepository,
                               NewsItemRepository newsRepository,
                               ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.repoRepository = repoRepository;
        this.newsRepository = newsRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Asynchronously processes a repository item through Gemini AI
     * to generate a summary, bullets, and code snippet.
     *
     * @param repoId the repository item ID
     * @return a CompletableFuture that completes when processing is done
     */
    @Async("aiTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processRepository(java.util.UUID repoId) {
        log.info("AI processing repository: {}", repoId);

        try {
            RepositoryItem repo = repoRepository.findById(repoId).orElse(null);
            if (repo == null) {
                log.warn("Repository {} not found, skipping AI processing", repoId);
                return CompletableFuture.completedFuture(null);
            }

            String prompt = String.format(REPO_SUMMARY_PROMPT,
                    repo.getName(), repo.getOwner(), repo.getLanguage(),
                    repo.getStars() != null ? repo.getStars() : 0,
                    repo.getForks() != null ? repo.getForks() : 0,
                    repo.getOpenIssuesCount() != null ? repo.getOpenIssuesCount() : 0,
                    repo.getTopics() != null ? repo.getTopics() : "none",
                    repo.getDescription() != null ? repo.getDescription() : "No description");

            GeminiResponse response = geminiClient.generate(prompt);
            String text = response != null ? response.extractText() : null;

            if (text != null) {
                JsonNode json = objectMapper.readTree(text);

                repo.setAiOneSentenceSummary(json.path("one_sentence_summary").asText(null));

                JsonNode bullets = json.path("three_bullet_tldr");
                if (bullets.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < bullets.size(); i++) {
                        if (i > 0) sb.append("|");
                        sb.append(bullets.get(i).asText());
                    }
                    repo.setAiThreeBulletTldr(sb.toString());
                }

                repo.setWhyInteresting(json.path("why_interesting").asText(null));
                repo.setIdealFor(json.path("ideal_for").asText(null));
                repo.setHasGoodFirstIssues(json.path("has_good_first_issues").asBoolean(false));

                repoRepository.save(repo);
                log.info("AI processing complete for repository: {}", repo.getName());
            } else {
                log.warn("Gemini returned no content for repository: {}", repo.getName());
            }
        } catch (Exception e) {
            log.error("AI processing failed for repository {}: {}", repoId, e.getMessage());
            // Graceful degradation — entity keeps its original description
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Asynchronously processes a news item through Gemini AI
     * to generate a summary and bullet points.
     *
     * @param newsId the news item ID
     * @return a CompletableFuture that completes when processing is done
     */
    @Async("aiTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processNewsItem(java.util.UUID newsId) {
        log.info("AI processing news item: {}", newsId);

        try {
            NewsItem news = newsRepository.findById(newsId).orElse(null);
            if (news == null) {
                log.warn("News item {} not found, skipping AI processing", newsId);
                return CompletableFuture.completedFuture(null);
            }

            String prompt = String.format(NEWS_SUMMARY_PROMPT,
                    news.getTitle(),
                    news.getAuthor() != null ? news.getAuthor() : "Unknown",
                    news.getSourceUrl());

            GeminiResponse response = geminiClient.generate(prompt);
            String text = response != null ? response.extractText() : null;

            if (text != null) {
                JsonNode json = objectMapper.readTree(text);

                news.setAiOneSentenceSummary(json.path("one_sentence_summary").asText(null));

                JsonNode bullets = json.path("three_bullet_tldr");
                if (bullets.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < bullets.size(); i++) {
                        if (i > 0) sb.append("|");
                        sb.append(bullets.get(i).asText());
                    }
                    news.setAiThreeBulletTldr(sb.toString());
                }

                news.setIsReleaseNote(json.path("is_release_note").asBoolean(false));

                newsRepository.save(news);
                log.info("AI processing complete for news item: {}", news.getTitle());
            } else {
                log.warn("Gemini returned no content for news item: {}", news.getTitle());
            }
        } catch (Exception e) {
            log.error("AI processing failed for news item {}: {}", newsId, e.getMessage());
            // Graceful degradation — entity keeps its original data
        }

        return CompletableFuture.completedFuture(null);
    }
}
