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
        String now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        log.info("Current date and time in Asia/Ho_Chi_Minh timezone: {}", now);
        return now.replaceAll("\\+\\d{2}:\\d{2}", "");
    }
}
