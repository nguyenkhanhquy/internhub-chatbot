package com.example.springaichatbot.model;

public record AiMessage(String content) {
    public AiMessage {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or empty");
        }
    }
}
