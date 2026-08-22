package com.kareem.GitMatch.core.entity;

import com.kareem.GitMatch.core.enums.FeedItemType;
import com.kareem.GitMatch.core.enums.SwipeDirection;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "swipe_actions")
public class SwipeAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private FeedItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private SwipeDirection direction;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    protected SwipeAction() {}

    public SwipeAction(UUID userId, UUID itemId, FeedItemType itemType, SwipeDirection direction) {
        this.userId = userId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.direction = direction;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getItemId() { return itemId; }
    public FeedItemType getItemType() { return itemType; }
    public SwipeDirection getDirection() { return direction; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
