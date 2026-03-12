package com.kareem.gitmatch.core.network

import com.kareem.gitmatch.data.local.PreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        preferencesManager: PreferencesManager,
        baseUrl: String = ApiConfig.BASE_URL
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)

                // Attach JWT token to every request if logged in
                val token = runBlocking { preferencesManager.getAuthTokenOnce() }
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }
}

object ApiConfig {
    // Use your machine's LAN IP for physical device testing
    const val BASE_URL = "http://192.168.1.112:8080/api/v1/"

    // Base URL without /api/v1/ — used for OAuth redirect URLs
    const val AUTH_BASE_URL = "http://192.168.1.112:8080"
}
