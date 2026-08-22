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


    @Query("SELECT n FROM NewsItem n WHERE n.id NOT IN " +
           "(SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<NewsItem> findUnswipedNews(@Param("userId") UUID userId, Pageable pageable);


    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NULL")
    Page<NewsItem> findUnprocessedNews(Pageable pageable);


    List<NewsItem> findByAiOneSentenceSummaryIsNull(Pageable pageable);


    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NOT NULL")
    Page<NewsItem> findProcessedNews(Pageable pageable);


    @Query("SELECT n FROM NewsItem n WHERE n.aiOneSentenceSummary IS NOT NULL " +
           "AND n.id NOT IN (SELECT s.itemId FROM SwipeAction s WHERE s.userId = :userId)")
    Page<NewsItem> findProcessedUnswipedNews(@Param("userId") UUID userId, Pageable pageable);


    List<NewsItem> findByImageUrlIsNull();
}
