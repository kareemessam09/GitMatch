package com.kareem.gitmatch.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferenceRequestDto(
    val preferredTopics: List<String>
)
