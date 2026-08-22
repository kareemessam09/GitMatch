package com.kareem.GitMatch.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kareem.GitMatch.core.enums.FeedItemType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record FeedCardResponse(
    UUID id,
    FeedItemType type,
    String title,
    String subtitle,
    String oneSentenceSummary,
    String threeBulletTldr,
    String whyInteresting,
    String idealFor,
    String language,
    Integer starCount,
    Integer forksCount,
    Integer openIssuesCount,
    String licenseName,
    List<String> topics,
    Boolean hasGoodFirstIssues,
    Boolean isReleaseNote,
    String sourceUrl,
    String imageUrl,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime publishedAt
) {}
