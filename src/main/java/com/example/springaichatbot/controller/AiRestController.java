package com.example.springaichatbot.controller;

//import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import com.example.springaichatbot.model.HumanMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class AiRestController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public AiRestController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient
                .prompt()
                .user(humanMessage.query())
                .advisors(spec -> spec
                        .param(CONVERSATION_ID, humanMessage.sessionId()))
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("AI inference error", e);
                    return Flux.just("Đã có lỗi xảy ra, vui lòng thử lại sau!");
                });
    }

    @PostMapping("/reset/{sessionId}")
    public void resetSession(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
        log.info("Reset session memory for ID: {}", sessionId);
    }
}
