package com.kareem.GitMatch.core.entity;

import com.kareem.GitMatch.config.TokenEncryptionConverter;
import com.kareem.GitMatch.core.enums.AuthProvider;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_username", unique = true)
    private String githubUsername;

    @Column(name = "email")
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    @Convert(converter = TokenEncryptionConverter.class)
    @Column(name = "github_access_token", columnDefinition = "TEXT")
    private String githubAccessToken;

    @Column(name = "preferred_languages", columnDefinition = "TEXT")
    private String preferredLanguages;

    @Column(name = "preferred_topics", columnDefinition = "TEXT")
    private String preferredTopics;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected AppUser() {}

    public AppUser(String githubUsername, String email, String displayName, String avatarUrl) {
        this.githubUsername = githubUsername;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public UUID getId() { return id; }
    public String getGithubUsername() { return githubUsername; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public String getGithubAccessToken() { return githubAccessToken; }
    public String getPreferredLanguages() { return preferredLanguages; }
    public String getPreferredTopics() { return preferredTopics; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }
    public void setGithubAccessToken(String githubAccessToken) { this.githubAccessToken = githubAccessToken; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
    public void setPreferredLanguages(String preferredLanguages) { this.preferredLanguages = preferredLanguages; }
    public void setPreferredTopics(String preferredTopics) { this.preferredTopics = preferredTopics; }
}
