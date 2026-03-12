package com.kareem.GitMatch.core.repository;

import com.kareem.GitMatch.core.entity.RepositoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryItemRepository extends JpaRepository<RepositoryItem, UUID> {

    boolean existsByGithubId(String githubId);

    Optional<RepositoryItem> findByGithubId(String githubId);

    /**
     * Finds repositories that the given user has not yet swiped on.
     */
    @Query("SELECT r FROM RepositoryItem r WHERE r.id NOT IN " +
           "(SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<RepositoryItem> findUnswipedRepos(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds repositories that have not yet been processed by AI (no summary).
     */
    @Query("SELECT r FROM RepositoryItem r WHERE r.aiOneSentenceSummary IS NULL")
    Page<RepositoryItem> findUnprocessedRepos(Pageable pageable);

    /**
     * Finds repos with no AI summary, returning a List (for batch processing).
     */
    List<RepositoryItem> findByAiOneSentenceSummaryIsNull(Pageable pageable);

    /**
     * Finds repos that HAVE been AI-processed (have a summary).
     */
    @Query("SELECT r FROM RepositoryItem r WHERE r.aiOneSentenceSummary IS NOT NULL")
    Page<RepositoryItem> findProcessedRepos(Pageable pageable);

    /**
     * Finds AI-processed repos that the user hasn't swiped on.
     */
    @Query("SELECT r FROM RepositoryItem r WHERE r.aiOneSentenceSummary IS NOT NULL " +
           "AND r.id NOT IN (SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<RepositoryItem> findProcessedUnswipedRepos(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT r FROM RepositoryItem r WHERE r.aiOneSentenceSummary IS NOT NULL " +
           "AND r.id NOT IN (SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId) " +
           "AND r.language IN (:languages)")
    Page<RepositoryItem> findPersonalizedUnswipedRepos(
            @Param("userId") UUID userId,
            @Param("languages") List<String> languages,
            Pageable pageable);
}
