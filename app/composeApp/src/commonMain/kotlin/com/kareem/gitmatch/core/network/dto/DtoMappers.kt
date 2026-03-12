package com.kareem.gitmatch.core.network.dto

import com.kareem.gitmatch.core.model.AuthProvider
import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.model.FeedItemType
import com.kareem.gitmatch.core.model.UserProfile

fun FeedCardDto.toDomain(): FeedCard = FeedCard(
    id = id,
    type = FeedItemType.valueOf(type),
    title = title,
    subtitle = subtitle.orEmpty(),
    summary = oneSentenceSummary.orEmpty(),
    bullets = threeBulletTldr.orEmpty(),
    whyInteresting = whyInteresting,
    idealFor = idealFor,
    language = language,
    starCount = starCount,
    forksCount = forksCount,
    openIssuesCount = openIssuesCount,
    licenseName = licenseName,
    topics = topics.orEmpty(),
    hasGoodFirstIssues = hasGoodFirstIssues ?: false,
    isReleaseNote = isReleaseNote ?: false,
    sourceUrl = sourceUrl,
    imageUrl = imageUrl,
    publishedAt = publishedAt
)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    githubUsername = githubUsername,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    authProvider = authProvider?.let { runCatching { AuthProvider.valueOf(it) }.getOrNull() },
    hasGithubToken = hasGithubToken,
    preferredLanguages = preferredLanguages,
    preferredTopics = preferredTopics
)
