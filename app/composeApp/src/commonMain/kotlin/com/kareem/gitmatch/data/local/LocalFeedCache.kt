package com.kareem.gitmatch.data.local

import com.kareem.gitmatch.core.network.dto.FeedCardDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * In-memory + persistent local cache for feed cards.
 * Replaces the old approach of storing everything in shared preferences.
 *
 * Feed data is cached in memory and serialized to a JSON string via DataStore
 * so the user sees cards immediately on next app launch.
 */
class LocalFeedCache(
    private val preferencesManager: PreferencesManager
) {
    private val mutex = Mutex()
    private var memoryCache: MutableList<FeedCardDto> = mutableListOf()
    private var vaultCache: MutableList<FeedCardDto> = mutableListOf()
    private var initialized = false

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Initializes the in-memory cache from persisted local storage.
     */
    suspend fun initialize() {
        if (initialized) return
        mutex.withLock {
            if (initialized) return
            val feedJson = preferencesManager.getCachedFeed()
            if (feedJson.isNotEmpty()) {
                runCatching {
                    memoryCache = json.decodeFromString<List<FeedCardDto>>(feedJson).toMutableList()
                }
            }
            val vaultJson = preferencesManager.getCachedVault()
            if (vaultJson.isNotEmpty()) {
                runCatching {
                    vaultCache = json.decodeFromString<List<FeedCardDto>>(vaultJson).toMutableList()
                }
            }
            initialized = true
        }
    }

    /**
     * Caches discover feed cards locally.
     * Appends new pages and deduplicates by ID.
     */
    suspend fun cacheFeed(cards: List<FeedCardDto>) {
        mutex.withLock {
            val existingIds = memoryCache.map { it.id }.toSet()
            val newCards = cards.filter { it.id !in existingIds }
            memoryCache.addAll(newCards)
            // Keep max 200 cards in cache
            if (memoryCache.size > 200) {
                memoryCache = memoryCache.takeLast(200).toMutableList()
            }
            persistFeed()
        }
    }

    /**
     * Returns cached feed cards (for offline use or fast startup).
     */
    suspend fun getCachedFeed(): List<FeedCardDto> {
        initialize()
        return mutex.withLock { memoryCache.toList() }
    }

    /**
     * Caches vault (liked) cards locally.
     */
    suspend fun cacheVault(cards: List<FeedCardDto>) {
        mutex.withLock {
            vaultCache = cards.toMutableList()
            persistVault()
        }
    }

    /**
     * Returns cached vault cards.
     */
    suspend fun getCachedVault(): List<FeedCardDto> {
        initialize()
        return mutex.withLock { vaultCache.toList() }
    }

    /**
     * Clears all cached data (e.g., on logout).
     */
    suspend fun clearAll() {
        mutex.withLock {
            memoryCache.clear()
            vaultCache.clear()
            preferencesManager.clearCachedFeed()
            preferencesManager.clearCachedVault()
        }
    }

    private suspend fun persistFeed() {
        val jsonStr = json.encodeToString(memoryCache.toList())
        preferencesManager.setCachedFeed(jsonStr)
    }

    private suspend fun persistVault() {
        val jsonStr = json.encodeToString(vaultCache.toList())
        preferencesManager.setCachedVault(jsonStr)
    }
}
