package com.kareem.GitMatch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures RestClient beans for external API communication.
 */
@Configuration
public class RestClientConfig {

    @Value("${gitmatch.ai.gemini.base-url}")
    private String geminiBaseUrl;

    @Bean(name = "geminiRestClient")
    public RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(geminiBaseUrl)
                .build();
    }

    @Bean(name = "githubRestClient")
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
