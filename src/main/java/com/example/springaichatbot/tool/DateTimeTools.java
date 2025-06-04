package com.example.springaichatbot.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
    }
}
