package com.example.springaichatbot.controller;

import com.example.springaichatbot.model.HumanMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@RestController
public class AiRestController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public AiRestController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    private static final String SYSTEM_PROMPT = """
            Bạn là một trợ lý ảo thông minh hỗ trợ người dùng trên website quản lý thực tập của Khoa CNTT, Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE).
            Chỉ cung cấp thông tin dựa trên dữ liệu hiện có, không tự suy diễn hoặc bịa thêm thông tin.
            Bắt đầu hỗ trợ người dùng ngay bây giờ!
            """;

    @PostMapping("/inference")
    public Flux<String> ask(@RequestBody HumanMessage humanMessage) {
        log.info("Received message: {}", humanMessage);
        return this.chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(humanMessage.query())
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, humanMessage.sessionId()))
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
