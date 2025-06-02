package com.example.springaichatbot.controller;

import com.example.springaichatbot.model.HumanMessage;
import com.example.springaichatbot.service.MySQLToChromaService;
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
    private final MySQLToChromaService mySQLToChromaService;

    public AiRestController(ChatClient chatClient, ChatMemory chatMemory, MySQLToChromaService mySQLToChromaService) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.mySQLToChromaService = mySQLToChromaService;
    }

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient.prompt()
                .advisors(advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, humanMessage.sessionId()))
                .user(u -> u.text("""
                        Câu hỏi của người dùng:
                        {question}
                        Câu trả lời của trợ lý AI:
                        """)
                        .param("question", humanMessage.query()))
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

    @PostMapping("/sync-mysql-to-chroma")
    public String syncMySQLToChroma() {
        try {
            log.info("Bắt đầu đồng bộ dữ liệu từ MySQL sang ChromaDB...");
            mySQLToChromaService.processDataFromMySQL();
            return "Đồng bộ dữ liệu thành công từ MySQL sang ChromaDB!";
        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ dữ liệu: {}", e.getMessage(), e);
            return "Lỗi khi đồng bộ dữ liệu: " + e.getMessage();
        }
    }
}
