package com.kareem.gitmatch.core.model

data class FeedCard(
    val id: String,
    val type: FeedItemType,
    val title: String,
    val subtitle: String,
    val summary: String,
    val bullets: String,
    val whyInteresting: String?,
    val idealFor: String?,
    val language: String?,
    val starCount: Int?,
    val forksCount: Int?,
    val openIssuesCount: Int?,
    val licenseName: String?,
    val topics: List<String>,
    val hasGoodFirstIssues: Boolean,
    val isReleaseNote: Boolean,
    val sourceUrl: String,
    val imageUrl: String?,
    val publishedAt: String?
)
