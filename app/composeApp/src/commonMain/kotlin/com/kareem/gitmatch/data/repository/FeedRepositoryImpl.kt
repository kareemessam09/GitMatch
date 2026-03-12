package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.network.GitMatchApi
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.core.network.dto.toDomain
import com.kareem.gitmatch.data.local.LocalFeedCache

class FeedRepositoryImpl(
    private val api: GitMatchApi,
    private val localCache: LocalFeedCache
) : FeedRepository {

    override suspend fun getDiscoverFeed(page: Int, size: Int): NetworkResult<List<FeedCard>> {
        return runCatching {
            val dtos = api.getDiscoverFeed(page, size)
            // Cache the fetched data locally
            localCache.cacheFeed(dtos)
            dtos.map { it.toDomain() }
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { error ->
                // On network failure, serve from local cache
                val cached = localCache.getCachedFeed()
                if (cached.isNotEmpty()) {
                    val start = page * size
                    val end = minOf(start + size, cached.size)
                    if (start < cached.size) {
                        NetworkResult.Success(cached.subList(start, end).map { it.toDomain() })
                    } else {
                        NetworkResult.Error(error.message ?: "Failed to load feed")
                    }
                } else {
                    NetworkResult.Error(error.message ?: "Failed to load feed")
                }
            }
        )
    }

    override suspend fun getVaultItems(): NetworkResult<List<FeedCard>> {
        return runCatching {
            val dtos = api.getVaultItems()
            // Cache vault locally
            localCache.cacheVault(dtos)
            dtos.map { it.toDomain() }
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { error ->
                // On network failure, serve from local cache
                val cached = localCache.getCachedVault()
                if (cached.isNotEmpty()) {
                    NetworkResult.Success(cached.map { it.toDomain() })
                } else {
                    NetworkResult.Error(error.message ?: "Failed to load vault")
                }
            }
        )
    }

    override suspend fun getCardDetail(cardId: String): NetworkResult<FeedCard> {
        return runCatching {
            api.getCardDetail(cardId).toDomain()
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { error ->
                // Try to find in local cache
                val allCached = localCache.getCachedFeed() + localCache.getCachedVault()
                val cached = allCached.find { it.id == cardId }
                if (cached != null) {
                    NetworkResult.Success(cached.toDomain())
                } else {
                    NetworkResult.Error(error.message ?: "Failed to load card detail")
                }
            }
        )
    }
}
