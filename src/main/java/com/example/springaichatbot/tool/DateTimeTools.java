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
        ZonedDateTime zonedNow = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        int hour = zonedNow.getHour();
        int minute = zonedNow.getMinute();
        int second = zonedNow.getSecond();
        int day = zonedNow.getDayOfMonth();
        int month = zonedNow.getMonthValue();
        int year = zonedNow.getYear();

        String period = hour < 12 ? "sáng" : "chiều";
        int displayHour = hour % 12 == 0 ? 12 : hour % 12;

        String formatted = String.format(
                "Bây giờ là %d giờ %02d phút %02d giây %s ngày %d/%d/%d theo múi giờ Việt Nam",
                displayHour, minute, second, period, day, month, year
        );

        log.info("Formatted user date time: {}", formatted);
        return formatted;
    }
}
