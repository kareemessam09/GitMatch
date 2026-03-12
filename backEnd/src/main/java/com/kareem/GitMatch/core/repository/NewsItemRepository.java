package com.kareem.GitMatch.core.repository;

import com.kareem.GitMatch.core.entity.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewsItemRepository extends JpaRepository<NewsItem, UUID> {

    boolean existsBySourceUrl(String sourceUrl);

    Optional<NewsItem> findBySourceUrl(String sourceUrl);

    /**
     * Finds news items that the given user has not yet swiped on.
     */
    @Query("SELECT n FROM NewsItem n WHERE n.id NOT IN " +
           "(SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<NewsItem> findUnswipedNews(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds news items that have not yet been processed by AI.
     */
    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NULL")
    Page<NewsItem> findUnprocessedNews(Pageable pageable);

    /**
     * Finds news items with no AI summary, returning a List (for batch processing).
     */
    List<NewsItem> findByAiOneSentenceSummaryIsNull(Pageable pageable);

    /**
     * Finds news items that HAVE been AI-processed.
     */
    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NOT NULL")
    Page<NewsItem> findProcessedNews(Pageable pageable);

    /**
     * Finds AI-processed news that the user hasn't swiped on.
     */
    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NOT NULL " +
           "AND n.id NOT IN (SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<NewsItem> findProcessedUnswipedNews(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds news items that have no image URL (for backfilling).
     */
    List<NewsItem> findByImageUrlIsNull();
}
