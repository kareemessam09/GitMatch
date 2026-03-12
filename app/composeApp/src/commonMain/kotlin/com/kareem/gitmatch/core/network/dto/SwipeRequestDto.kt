package com.kareem.gitmatch.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SwipeRequestDto(
    val itemId: String,
    val direction: String
)
