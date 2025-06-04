package com.example.springaichatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DateTimeTools {
    @Tool(description = "Get the user's current date and time")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
    }
}
