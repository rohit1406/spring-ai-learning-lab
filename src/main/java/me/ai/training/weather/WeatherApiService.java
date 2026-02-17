package me.ai.training.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

/**
 * @author Rohit Muneshwar
 * @created on 2/11/2026
 *
 * Gets the weather information from WeatherAPI https://www.weatherapi.com/
 */
@Service
public class WeatherApiService implements Function<Weather.Request, Weather.Response> {
    @Autowired
    private WeatherApiServiceBuilder weatherApiServiceBuilder;

    @Override
    public Weather.Response apply(Weather.Request request){
        return weatherApiServiceBuilder.getWeatherInformation(request.city());
    }
}
