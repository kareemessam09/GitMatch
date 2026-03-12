package com.kareem.gitmatch.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeedCardDto(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String? = null,
    val oneSentenceSummary: String? = null,
    val threeBulletTldr: String? = null,
    val whyInteresting: String? = null,
    val idealFor: String? = null,
    val language: String? = null,
    val starCount: Int? = null,
    val forksCount: Int? = null,
    val openIssuesCount: Int? = null,
    val licenseName: String? = null,
    val topics: List<String>? = null,
    val hasGoodFirstIssues: Boolean? = null,
    val isReleaseNote: Boolean? = null,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val publishedAt: String? = null
)
