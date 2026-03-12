package com.kareem.GitMatch.core.repository;

import com.kareem.GitMatch.core.entity.SwipeAction;
import com.kareem.GitMatch.core.enums.FeedItemType;
import com.kareem.GitMatch.core.enums.SwipeDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SwipeActionRepository extends JpaRepository<SwipeAction, UUID> {

    List<SwipeAction> findByUserId(UUID userId);

    Page<SwipeAction> findByUserIdAndDirection(UUID userId, SwipeDirection direction, Pageable pageable);

    boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

    List<SwipeAction> findByUserIdAndItemType(UUID userId, FeedItemType itemType);
}
