package com.edu.spring.ai.weather;

/**
 * @author Rohit Muneshwar
 * @created on 2/11/2026
 *
 *
 */
public class Weather {
    public record Request(String city){}
    public record Response(Location location, Current current){}
    public record Location(String name, String country){}
    public record Current(String temp_c){}
}
