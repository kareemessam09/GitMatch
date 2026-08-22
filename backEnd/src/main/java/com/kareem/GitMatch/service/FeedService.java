package com.kareem.GitMatch.service;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.entity.NewsItem;
import com.kareem.GitMatch.core.entity.RepositoryItem;
import com.kareem.GitMatch.core.entity.SwipeAction;
import com.kareem.GitMatch.core.enums.FeedItemType;
import com.kareem.GitMatch.core.enums.SwipeDirection;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import com.kareem.GitMatch.core.repository.NewsItemRepository;
import com.kareem.GitMatch.core.repository.RepositoryItemRepository;
import com.kareem.GitMatch.core.repository.SwipeActionRepository;
import com.kareem.GitMatch.dto.response.FeedCardResponse;
import com.kareem.GitMatch.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);

    private final RepositoryItemRepository repoRepository;
    private final NewsItemRepository newsRepository;
    private final SwipeActionRepository swipeActionRepository;
    private final AppUserRepository appUserRepository;

    public FeedService(RepositoryItemRepository repoRepository,
                       NewsItemRepository newsRepository,
                       SwipeActionRepository swipeActionRepository,
                       AppUserRepository appUserRepository) {
        this.repoRepository = repoRepository;
        this.newsRepository = newsRepository;
        this.swipeActionRepository = swipeActionRepository;
        this.appUserRepository = appUserRepository;
    }


    @Transactional(readOnly = true)
    public List<FeedCardResponse> getDiscoverFeed(int page, int size) {
        log.info("Fetching discover feed — page: {}, size: {}", page, size);

        // Fetch 'size' repos AND 'size' news (each type gets the full page size)
        Page<RepositoryItem> repos = repoRepository.findProcessedRepos(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<NewsItem> news = newsRepository.findProcessedNews(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<FeedCardResponse> feed = new ArrayList<>();
        repos.getContent().forEach(repo -> feed.add(mapRepoToCard(repo)));
        news.getContent().forEach(item -> feed.add(mapNewsToCard(item)));


        Collections.shuffle(feed);
        return feed;
    }


    @Transactional(readOnly = true)
    public List<FeedCardResponse> getPersonalizedFeed(UUID userId, int page, int size) {
        log.info("Fetching personalized feed for user {} — page: {}, size: {}", userId, page, size);

        Pageable repoPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Pageable newsPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));


        Page<RepositoryItem> repos;
        AppUser user = appUserRepository.findById(userId).orElse(null);
        List<String> preferredLanguages = parseCommaSeparated(user != null ? user.getPreferredLanguages() : null);
        List<String> preferredTopics = parseCommaSeparated(user != null ? user.getPreferredTopics() : null);

        if (!preferredLanguages.isEmpty()) {

            repos = repoRepository.findPersonalizedUnswipedRepos(userId, preferredLanguages, repoPageable);
            log.info("Personalized repo query returned {} repos (filtered by languages: {})",
                    repos.getContent().size(), preferredLanguages);

            if (repos.getContent().size() < size / 2) {
                Page<RepositoryItem> fallback = repoRepository.findProcessedUnswipedRepos(userId, repoPageable);
                List<RepositoryItem> combined = new ArrayList<>(repos.getContent());
                List<UUID> existingIds = combined.stream().map(RepositoryItem::getId).collect(Collectors.toList());
                for (RepositoryItem r : fallback.getContent()) {
                    if (!existingIds.contains(r.getId()) && combined.size() < size) {
                        combined.add(r);
                    }
                }
                repos = new org.springframework.data.domain.PageImpl<>(combined);
            }
        } else {
            repos = repoRepository.findProcessedUnswipedRepos(userId, repoPageable);
        }

        Page<NewsItem> news = newsRepository.findProcessedUnswipedNews(userId, newsPageable);

        log.info("Feed results — repos: {}, news: {}", repos.getContent().size(), news.getContent().size());

        List<FeedCardResponse> feed = new ArrayList<>();
        repos.getContent().forEach(repo -> feed.add(mapRepoToCard(repo)));
        news.getContent().forEach(item -> feed.add(mapNewsToCard(item)));

        Collections.shuffle(feed);
        log.info("Returning {} total feed cards", feed.size());
        return feed;
    }

    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<FeedCardResponse> getVaultItems(UUID userId) {
        log.info("Fetching vault items for user {}", userId);

        List<SwipeAction> liked = swipeActionRepository.findByUserIdAndDirection(
                userId, SwipeDirection.RIGHT,
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();

        List<FeedCardResponse> vault = new ArrayList<>();
        for (SwipeAction swipe : liked) {
            try {
                if (swipe.getItemType() == FeedItemType.REPO) {
                    repoRepository.findById(swipe.getItemId())
                            .ifPresent(repo -> vault.add(mapRepoToCard(repo)));
                } else if (swipe.getItemType() == FeedItemType.NEWS) {
                    newsRepository.findById(swipe.getItemId())
                            .ifPresent(news -> vault.add(mapNewsToCard(news)));
                }
            } catch (Exception e) {
                log.warn("Could not resolve vault item {}: {}", swipe.getItemId(), e.getMessage());
            }
        }
        return vault;
    }


    @Transactional(readOnly = true)
    public FeedCardResponse getCardById(UUID cardId) {
        log.info("Fetching card detail for {}", cardId);

        return repoRepository.findById(cardId)
                .map(this::mapRepoToCard)
                .orElseGet(() -> newsRepository.findById(cardId)
                        .map(this::mapNewsToCard)
                        .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId)));
    }

    private FeedCardResponse mapRepoToCard(RepositoryItem repo) {
        List<String> topics = repo.getTopics() != null && !repo.getTopics().isEmpty()
                ? Arrays.asList(repo.getTopics().split(","))
                : List.of();

        // Prefer the OG image, fall back to avatar
        String imageUrl = repo.getOpenGraphImageUrl() != null
                ? repo.getOpenGraphImageUrl()
                : repo.getAvatarUrl();

        return new FeedCardResponse(
                repo.getId(),
                FeedItemType.REPO,
                repo.getName(),
                repo.getOwner() + "/" + repo.getName(),
                repo.getAiOneSentenceSummary(),
                repo.getAiThreeBulletTldr(),
                repo.getWhyInteresting(),
                repo.getIdealFor(),
                repo.getLanguage(),
                repo.getStars(),
                repo.getForks(),
                repo.getOpenIssuesCount(),
                repo.getLicenseName(),
                topics,
                repo.getHasGoodFirstIssues(),
                null,
                repo.getUrl(),
                imageUrl,
                repo.getCreatedAt()
        );
    }

    private FeedCardResponse mapNewsToCard(NewsItem news) {
        return new FeedCardResponse(
                news.getId(),
                FeedItemType.NEWS,
                news.getTitle(),
                "by " + (news.getAuthor() != null ? news.getAuthor() : "Unknown"),
                news.getAiOneSentenceSummary(),
                news.getAiThreeBulletTldr(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                news.getIsReleaseNote(),
                news.getSourceUrl(),
                news.getImageUrl(),
                news.getPublishedDate()
        );
    }
}
