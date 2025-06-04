package com.example.springaichatbot.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeocodingTool {

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Convert a city name to latitude and longitude using Nominatim API")
    public String getCoordinates(String locationName) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1",
                locationName.replace(" ", "+")
        );

        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "Không thể tìm tọa độ cho địa điểm: " + locationName;
        }
    }
}

