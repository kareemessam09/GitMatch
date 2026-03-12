package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.network.NetworkResult

interface FeedRepository {
    suspend fun getDiscoverFeed(page: Int, size: Int): NetworkResult<List<FeedCard>>
    suspend fun getVaultItems(): NetworkResult<List<FeedCard>>
    suspend fun getCardDetail(cardId: String): NetworkResult<FeedCard>
}
