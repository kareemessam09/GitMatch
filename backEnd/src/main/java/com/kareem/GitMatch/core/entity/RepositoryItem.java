package com.kareem.GitMatch.core.entity;

import com.kareem.GitMatch.core.enums.ContentSource;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a GitHub repository discovered by the Harvester.
 */
@Entity
@Table(name = "repository_items")
public class RepositoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_id", nullable = false, unique = true)
    private String githubId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "language")
    private String language;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "forks")
    private Integer forks;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "ai_one_sentence_summary", columnDefinition = "TEXT")
    private String aiOneSentenceSummary;

    @Column(name = "ai_three_bullet_tldr", columnDefinition = "TEXT")
    private String aiThreeBulletTldr;

    @Column(name = "has_good_first_issues")
    private Boolean hasGoodFirstIssues;

    @Column(name = "why_interesting", columnDefinition = "TEXT")
    private String whyInteresting;

    @Column(name = "ideal_for", columnDefinition = "TEXT")
    private String idealFor;

    @Column(name = "topics", columnDefinition = "TEXT")
    private String topics;

    @Column(name = "open_issues_count")
    private Integer openIssuesCount;

    @Column(name = "license_name")
    private String licenseName;

    @Column(name = "open_graph_image_url", columnDefinition = "TEXT")
    private String openGraphImageUrl;

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
    protected RepositoryItem() {}

    public RepositoryItem(String githubId, String name, String owner, String url,
                          String language, Integer stars, Integer forks, String description,
                          String avatarUrl, String topics, Integer openIssuesCount,
                          String licenseName) {
        this.githubId = githubId;
        this.name = name;
        this.owner = owner;
        this.url = url;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.topics = topics;
        this.openIssuesCount = openIssuesCount;
        this.licenseName = licenseName;
        this.openGraphImageUrl = "https://opengraph.githubassets.com/1/" + owner + "/" + name;
        this.contentSource = ContentSource.GITHUB;
    }

    // Getters
    public UUID getId() { return id; }
    public String getGithubId() { return githubId; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public String getUrl() { return url; }
    public String getLanguage() { return language; }
    public Integer getStars() { return stars; }
    public Integer getForks() { return forks; }
    public String getDescription() { return description; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getAiOneSentenceSummary() { return aiOneSentenceSummary; }
    public String getAiThreeBulletTldr() { return aiThreeBulletTldr; }
    public Boolean getHasGoodFirstIssues() { return hasGoodFirstIssues; }
    public String getWhyInteresting() { return whyInteresting; }
    public String getIdealFor() { return idealFor; }
    public String getTopics() { return topics; }
    public Integer getOpenIssuesCount() { return openIssuesCount; }
    public String getLicenseName() { return licenseName; }
    public String getOpenGraphImageUrl() { return openGraphImageUrl; }
    public ContentSource getContentSource() { return contentSource; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters for fields that may be updated after creation
    public void setAiOneSentenceSummary(String aiOneSentenceSummary) {
        this.aiOneSentenceSummary = aiOneSentenceSummary;
    }

    public void setAiThreeBulletTldr(String aiThreeBulletTldr) {
        this.aiThreeBulletTldr = aiThreeBulletTldr;
    }

    public void setHasGoodFirstIssues(Boolean hasGoodFirstIssues) {
        this.hasGoodFirstIssues = hasGoodFirstIssues;
    }

    public void setWhyInteresting(String whyInteresting) {
        this.whyInteresting = whyInteresting;
    }

    public void setIdealFor(String idealFor) {
        this.idealFor = idealFor;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public void setOpenGraphImageUrl(String openGraphImageUrl) {
        this.openGraphImageUrl = openGraphImageUrl;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public void setForks(Integer forks) {
        this.forks = forks;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
