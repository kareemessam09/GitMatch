package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.SwipeDirection
import com.kareem.gitmatch.core.network.NetworkResult

interface SwipeRepository {
    suspend fun recordSwipe(cardId: String, direction: SwipeDirection): NetworkResult<Unit>
}
