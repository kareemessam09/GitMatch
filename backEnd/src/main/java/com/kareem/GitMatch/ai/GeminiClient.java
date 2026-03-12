package com.kareem.GitMatch.ai;

import com.kareem.GitMatch.ai.dto.GeminiRequest;
import com.kareem.GitMatch.ai.dto.GeminiResponse;
import com.kareem.GitMatch.config.GitMatchProperties;
import com.kareem.GitMatch.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Handles all HTTP communication with the Google Gemini API.
 * No other class should call the Gemini API directly.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient geminiRestClient;
    private final GitMatchProperties properties;

    public GeminiClient(@Qualifier("geminiRestClient") RestClient geminiRestClient,
                        GitMatchProperties properties) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
    }

    /**
     * Sends a prompt to the Gemini API and returns the response.
     * Returns null (instead of throwing) on 429 rate-limit errors
     * so the batch processor can gracefully skip and retry next cycle.
     *
     * @param prompt the text prompt to send
     * @return the Gemini API response, or null if rate-limited
     */
    public GeminiResponse generate(String prompt) {
        String model = properties.ai().gemini().model();
        String apiKey = properties.ai().gemini().apiKey();

        log.debug("Sending prompt to Gemini model '{}' (length: {} chars)", model, prompt.length());

        try {
            GeminiRequest request = GeminiRequest.fromPrompt(prompt);

            GeminiResponse response = geminiRestClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.extractText() == null) {
                log.warn("Gemini returned empty response for prompt (first 100 chars): {}",
                        prompt.substring(0, Math.min(100, prompt.length())));
            }

            return response;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("429")) {
                log.warn("Gemini rate limit hit (429). Will retry next batch cycle.");
                return null;
            }
            log.error("Gemini API call failed: {}", msg);
            throw new ExternalApiException("Gemini API call failed", e);
        }
    }
}
