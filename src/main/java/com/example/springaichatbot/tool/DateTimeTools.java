package com.example.springaichatbot.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        String now = LocalDateTime.now().toString();
        log.info("Current date time tool called: {}", now);
        String userDateTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        log.info("User date time tool called: {}", userDateTime);
        return userDateTime;
    }
}
