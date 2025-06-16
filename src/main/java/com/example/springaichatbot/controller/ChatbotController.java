package com.example.springaichatbot.controller;

import com.example.springaichatbot.model.HumanMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
public class ChatbotController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatbotController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient.prompt()
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, humanMessage.sessionId()))
                .user(humanMessage.query())
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("AI inference error for session {}: {}", humanMessage.sessionId(), e.getMessage(), e);
                    return Flux.just("Hệ thống đã đạt giới hạn số lượng câu hỏi, vui lòng quay lại sau.");
                });
    }

    @PostMapping("/reset")
    public void resetSession(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        chatMemory.clear(sessionId);
        log.info("Reset session memory for ID: {}", sessionId);
    }
}
