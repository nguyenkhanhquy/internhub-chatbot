package com.example.springaichatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherTool {

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Get current weather by latitude and longitude using Open-Meteo API")
    public String getWeather(double latitude, double longitude) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true",
                latitude, longitude
        );

        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "Không thể lấy dữ liệu thời tiết tại vị trí này.";
        }
    }
}
