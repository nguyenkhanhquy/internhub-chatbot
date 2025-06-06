package com.example.springaichatbot.controller;

import com.example.springaichatbot.model.HumanMessage;
import com.example.springaichatbot.tool.DateTimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class ChatbotController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final DateTimeTools dateTimeTools;

    public ChatbotController(ChatClient chatClient, ChatMemory chatMemory, DateTimeTools dateTimeTools) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.dateTimeTools = dateTimeTools;
    }

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient.prompt()
//                .tools(dateTimeTools)
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, humanMessage.sessionId()))
                .user(u -> u.text("""
                        User Questions:
                        {question}
                        """)
                        .param("question", humanMessage.query()))
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("AI inference error for session {}: {}", humanMessage.sessionId(), e.getMessage(), e);
                    return Flux.just("Đã có lỗi xảy ra, vui lòng thử lại sau!");
                });
    }

    @PostMapping("/reset/{sessionId}")
    public void resetSession(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
        log.info("Reset session memory for ID: {}", sessionId);
    }
}
