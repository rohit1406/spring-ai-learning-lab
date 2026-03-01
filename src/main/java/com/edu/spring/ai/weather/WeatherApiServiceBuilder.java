package com.edu.spring.ai.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * @author Rohit Muneshwar
 * @created on 2/11/2026
 *
 * Builder class to get the weather information from WeatherAPI https://www.weatherapi.com/
 */
@Service
@Slf4j
public class WeatherApiServiceBuilder {
    @Value("${spring.weather.api.base-url}")
    private String baseUrl;

    @Value("${spring.weather.api.api-key}")
    private String apiKey;

    public Weather.Response getWeatherInformation(String city){
        log.debug("Getting weather information from {}",baseUrl);
        return RestClient.builder().baseUrl(baseUrl).build()
                .get()
                .uri(builder -> builder.path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", city)
                        .build())
                .retrieve()
                .body(Weather.Response.class);
    }
}
