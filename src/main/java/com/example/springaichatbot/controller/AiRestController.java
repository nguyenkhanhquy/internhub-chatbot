package com.example.springaichatbot.controller;

import com.example.springaichatbot.model.HumanMessage;
import com.example.springaichatbot.tool.DateTimeTools;
import com.example.springaichatbot.tool.GeocodingTool;
import com.example.springaichatbot.tool.WeatherTool;
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
public class AiRestController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final DateTimeTools dateTimeTools;
    private final GeocodingTool geocodingTool;
    private final WeatherTool weatherTool;

    public AiRestController(ChatClient chatClient, ChatMemory chatMemory, DateTimeTools dateTimeTools, GeocodingTool geocodingTool, WeatherTool weatherTool) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.dateTimeTools = dateTimeTools;
        this.geocodingTool = geocodingTool;
        this.weatherTool = weatherTool;
    }

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient.prompt()
                .tools(dateTimeTools, geocodingTool, weatherTool)
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
