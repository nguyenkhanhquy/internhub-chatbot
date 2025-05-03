package com.example.springaichatbot.model;

public record HumanMessage(String query, String sessionId) {
    public HumanMessage {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query cannot be null or empty");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId cannot be null or empty");
        }
    }
}
