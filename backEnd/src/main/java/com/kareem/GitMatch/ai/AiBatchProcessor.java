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
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Steady-stream AI processor: processes exactly ONE item every 30 seconds.
 * <p>
 * Gemini free tier allows ~15 requests/minute. By sending 1 request every
 * ~30+ seconds (fixedDelay waits for completion before counting), we stay
 * safely at ~6 req/min — zero quota issues guaranteed.
 * <p>
 * Priority: unprocessed repos first, then news. Alternates automatically.
 * Starts 3 seconds after boot and keeps ticking indefinitely.
 */
@Component
public class AiBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(AiBatchProcessor.class);

    private final GeminiClient geminiClient;
    private final RepositoryItemRepository repoRepository;
    private final NewsItemRepository newsRepository;
    private final ObjectMapper objectMapper;

    public AiBatchProcessor(GeminiClient geminiClient,
                            RepositoryItemRepository repoRepository,
                            NewsItemRepository newsRepository,
                            ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.repoRepository = repoRepository;
        this.newsRepository = newsRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Ticks every 30 seconds (after the previous call finishes).
     * Picks exactly 1 unprocessed item and sends it to Gemini.
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void processNextItem() {
        // Try one unprocessed repo first
        List<RepositoryItem> repos = repoRepository
                .findByAiOneSentenceSummaryIsNull(PageRequest.of(0, 1));

        if (!repos.isEmpty()) {
            processRepo(repos.getFirst());
            return;
        }

        // No repos left — try one unprocessed news item
        List<NewsItem> news = newsRepository
                .findByAiOneSentenceSummaryIsNull(PageRequest.of(0, 1));

        if (!news.isEmpty()) {
            processNewsItem(news.getFirst());
            return;
        }

        log.debug("AI pipeline idle — all items are processed");
    }

    @Transactional
    protected void processRepo(RepositoryItem repo) {
        try {
            log.info("AI processing repo: {} ({})", repo.getName(), repo.getId());

            String prompt = String.format(AiProcessorService.REPO_SUMMARY_PROMPT,
                    repo.getName(), repo.getOwner(), repo.getLanguage(),
                    repo.getStars() != null ? repo.getStars() : 0,
                    repo.getForks() != null ? repo.getForks() : 0,
                    repo.getOpenIssuesCount() != null ? repo.getOpenIssuesCount() : 0,
                    repo.getTopics() != null ? repo.getTopics() : "none",
                    repo.getDescription() != null ? repo.getDescription() : "No description");

            GeminiResponse response = geminiClient.generate(prompt);
            if (response == null) {
                log.warn("Rate-limited — will retry repo '{}' next tick", repo.getName());
                return;
            }

            String text = response.extractText();
            if (text != null) {
                text = cleanJsonResponse(text);
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
                log.info("✓ AI summary done for repo: {}", repo.getName());
            } else {
                log.warn("Gemini returned no content for repo: {}", repo.getName());
            }
        } catch (Exception e) {
            log.error("AI processing failed for repo {} ({}): {}",
                    repo.getName(), repo.getId(), e.getMessage());
        }
    }

    @Transactional
    protected void processNewsItem(NewsItem news) {
        try {
            log.info("AI processing news: {} ({})", news.getTitle(), news.getId());

            String prompt = String.format(AiProcessorService.NEWS_SUMMARY_PROMPT,
                    news.getTitle(),
                    news.getAuthor() != null ? news.getAuthor() : "Unknown",
                    news.getSourceUrl());

            GeminiResponse response = geminiClient.generate(prompt);
            if (response == null) {
                log.warn("Rate-limited — will retry news '{}' next tick", news.getTitle());
                return;
            }

            String text = response.extractText();
            if (text != null) {
                text = cleanJsonResponse(text);
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
                log.info("✓ AI summary done for news: {}", news.getTitle());
            } else {
                log.warn("Gemini returned no content for news: {}", news.getTitle());
            }
        } catch (Exception e) {
            log.error("AI processing failed for news {} ({}): {}",
                    news.getTitle(), news.getId(), e.getMessage());
        }
    }

    /**
     * Strips markdown code fences that Gemini sometimes wraps around JSON.
     */
    private String cleanJsonResponse(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }
}
