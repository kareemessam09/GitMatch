package com.kareem.GitMatch.service;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.entity.RepositoryItem;
import com.kareem.GitMatch.core.entity.SwipeAction;
import com.kareem.GitMatch.core.enums.FeedItemType;
import com.kareem.GitMatch.core.enums.SwipeDirection;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import com.kareem.GitMatch.core.repository.RepositoryItemRepository;
import com.kareem.GitMatch.core.repository.SwipeActionRepository;
import com.kareem.GitMatch.dto.request.SwipeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class SwipeService {

    private static final Logger log = LoggerFactory.getLogger(SwipeService.class);

    private final SwipeActionRepository swipeActionRepository;
    private final GitHubService gitHubService;
    private final AppUserRepository appUserRepository;
    private final RepositoryItemRepository repositoryItemRepository;

    public SwipeService(SwipeActionRepository swipeActionRepository,
                        GitHubService gitHubService,
                        AppUserRepository appUserRepository,
                        RepositoryItemRepository repositoryItemRepository) {
        this.swipeActionRepository = swipeActionRepository;
        this.gitHubService = gitHubService;
        this.appUserRepository = appUserRepository;
        this.repositoryItemRepository = repositoryItemRepository;
    }


    @Transactional
    public SwipeAction recordSwipe(SwipeRequest request) {
        log.info("Recording swipe: user={}, item={}, direction={}", request.userId(), request.itemId(), request.direction());

        if (swipeActionRepository.existsByUserIdAndItemId(request.userId(), request.itemId())) {
            throw new IllegalArgumentException("User has already swiped on this item.");
        }

        SwipeAction action = new SwipeAction(
                request.userId(),
                request.itemId(),
                request.itemType(),
                request.direction()
        );

        SwipeAction saved = swipeActionRepository.save(action);


        if (request.direction() == SwipeDirection.RIGHT && request.itemType() == FeedItemType.REPO) {
            log.info("Right swipe on repo — triggering async GitHub star for item {}", request.itemId());
            asyncStarOnGitHub(request.userId(), request.itemId());
        }

        return saved;
    }


    @Transactional
    public SwipeAction recordSwipe(UUID userId, UUID itemId, SwipeDirection direction) {
        FeedItemType itemType = repositoryItemRepository.existsById(itemId)
                ? FeedItemType.REPO
                : FeedItemType.NEWS;
        return recordSwipe(new SwipeRequest(userId, itemId, itemType, direction));
    }


    @Async("aiTaskExecutor")
    public void asyncStarOnGitHub(UUID userId, UUID itemId) {
        try {
            Optional<AppUser> userOpt = appUserRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("Cannot auto-star: user {} not found", userId);
                return;
            }

            AppUser user = userOpt.get();
            if (user.getGithubAccessToken() == null || user.getGithubAccessToken().isBlank()) {
                log.info("User {} has no GitHub token — skipping auto-star", userId);
                return;
            }

            Optional<RepositoryItem> repoOpt = repositoryItemRepository.findById(itemId);
            if (repoOpt.isEmpty()) {
                log.warn("Cannot auto-star: repository item {} not found", itemId);
                return;
            }

            RepositoryItem repo = repoOpt.get();
            gitHubService.starRepository(repo.getOwner(), repo.getName(), user.getGithubAccessToken());
            log.info("Auto-starred {}/{} for user {}", repo.getOwner(), repo.getName(), userId);

        } catch (Exception e) {
            log.error("Async auto-star failed for user={}, item={}: {}", userId, itemId, e.getMessage());
    
        }
    }


    @Transactional(readOnly = true)
    public Page<SwipeAction> getLikedItems(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return swipeActionRepository.findByUserIdAndDirection(userId, SwipeDirection.RIGHT, pageable);
    }


    @Transactional(readOnly = true)
    public List<SwipeAction> getSwipeHistory(UUID userId) {
        return swipeActionRepository.findByUserId(userId);
    }
}
