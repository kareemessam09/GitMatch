package com.kareem.GitMatch.core.entity;

import com.kareem.GitMatch.core.enums.ContentSource;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a news article or release note discovered by the Harvester.
 */
@Entity
@Table(name = "news_items")
public class NewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "author", columnDefinition = "TEXT")
    private String author;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "is_release_note")
    private Boolean isReleaseNote;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "ai_three_bullet_tldr", columnDefinition = "TEXT")
    private String aiThreeBulletTldr;

    @Column(name = "ai_one_sentence_summary", columnDefinition = "TEXT")
    private String aiOneSentenceSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_source")
    private ContentSource contentSource;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // No-arg constructor required by JPA
    protected NewsItem() {}

    public NewsItem(String sourceUrl, String title, String author,
                    LocalDateTime publishedDate, Boolean isReleaseNote, ContentSource contentSource,
                    String imageUrl) {
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
        this.isReleaseNote = isReleaseNote;
        this.contentSource = contentSource;
        this.imageUrl = imageUrl;
    }

    // Getters
    public UUID getId() { return id; }
    public String getSourceUrl() { return sourceUrl; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public LocalDateTime getPublishedDate() { return publishedDate; }
    public Boolean getIsReleaseNote() { return isReleaseNote; }
    public String getImageUrl() { return imageUrl; }
    public String getAiThreeBulletTldr() { return aiThreeBulletTldr; }
    public String getAiOneSentenceSummary() { return aiOneSentenceSummary; }
    public ContentSource getContentSource() { return contentSource; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters for fields updated after creation
    public void setAiThreeBulletTldr(String aiThreeBulletTldr) {
        this.aiThreeBulletTldr = aiThreeBulletTldr;
    }

    public void setAiOneSentenceSummary(String aiOneSentenceSummary) {
        this.aiOneSentenceSummary = aiOneSentenceSummary;
    }

    public void setIsReleaseNote(Boolean isReleaseNote) {
        this.isReleaseNote = isReleaseNote;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
