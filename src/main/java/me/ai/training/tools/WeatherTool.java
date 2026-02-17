package me.ai.training.tools;

import me.ai.training.weather.Weather;
import me.ai.training.weather.WeatherApiService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/12/2026
 *
 *
 */
@Service
public class WeatherTool {
    private final WeatherApiService weatherApiService;

    public WeatherTool(WeatherApiService weatherApiService){
        this.weatherApiService = weatherApiService;
    }

    @Tool(description = "Get weather information of given city")
    public String getWeather(@ToolParam(description = "city of which we want to get weather information") String city){
        return weatherApiService.apply(new Weather.Request(city)).toString();
    }
}
