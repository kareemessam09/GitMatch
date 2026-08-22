package com.kareem.GitMatch.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
public class ApiDocController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("application", "GitMatch API");
        doc.put("version", "v1");
        doc.put("description", "Backend API for GitMatch — a Tinder-style discovery app for GitHub repositories and tech news.");
        doc.put("status", "running");

        // Endpoints
        doc.put("endpoints", List.of(
            endpoint("GET", "/api/v1/feed-cards",
                     "Get the discovery feed (mixed repos + news)",
                     Map.of("page", "int (default: 0)", "size", "int (default: 20)")),

            endpoint("GET", "/api/v1/feed-cards/personalized",
                     "Get a personalized feed excluding already-swiped items",
                     Map.of("userId", "UUID (required)", "page", "int (default: 0)", "size", "int (default: 20)")),

            endpoint("POST", "/api/v1/swipes",
                     "Record a swipe action (RIGHT = like, LEFT = ignore, UP = more info)",
                     Map.of("body", Map.of(
                         "userId", "UUID",
                         "itemId", "UUID",
                         "itemType", "REPO | NEWS",
                         "direction", "RIGHT | LEFT | UP"
                     ))),

            endpoint("GET", "/api/v1/swipes/liked",
                     "Get user's liked/saved items (the Vault)",
                     Map.of("userId", "UUID (required)", "page", "int (default: 0)", "size", "int (default: 20)")),

            endpoint("GET", "/api/v1/users/{userId}",
                     "Get a user's profile by ID",
                     Map.of("userId", "UUID (path param)")),

            endpoint("PUT", "/api/v1/users/preferences",
                     "Update a user's language and topic preferences",
                     Map.of("body", Map.of(
                         "userId", "UUID",
                         "preferredLanguages", "List<String>",
                         "preferredTopics", "List<String>"
                     )))
        ));

        // Data sources
        doc.put("data_sources", Map.of(
            "repositories", "GitHub Search API (harvested every 2 hours)",
            "news", "Dev.to API (harvested every 3 hours)",
            "ai_summaries", "Google Gemini API (async processing)"
        ));

        return ResponseEntity.ok(doc);
    }

    private Map<String, Object> endpoint(String method, String path, String description, Map<String, Object> parameters) {
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", method);
        ep.put("path", path);
        ep.put("description", description);
        ep.put("parameters", parameters);
        return ep;
    }
}
