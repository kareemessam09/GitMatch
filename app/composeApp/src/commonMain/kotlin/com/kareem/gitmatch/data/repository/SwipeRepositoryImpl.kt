package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.SwipeDirection
import com.kareem.gitmatch.core.network.GitMatchApi
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.core.network.dto.SwipeRequestDto

class SwipeRepositoryImpl(
    private val api: GitMatchApi
) : SwipeRepository {

    override suspend fun recordSwipe(cardId: String, direction: SwipeDirection): NetworkResult<Unit> {
        return runCatching {
            api.recordSwipe(
                SwipeRequestDto(
                    itemId = cardId,
                    direction = direction.name
                )
            )
        }.fold(
            onSuccess = { NetworkResult.Success(Unit) },
            onFailure = { NetworkResult.Error(it.message ?: "Failed to record swipe") }
        )
    }
}
