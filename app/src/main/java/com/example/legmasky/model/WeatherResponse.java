package com.example.legmasky.model;

public class WeatherResponse {
    public String stationName;
    public double temperature;
    public double tempMin;
    public double tempMax;
    public double feelsLike;
    public int humidity;
    public double windSpeed;
    public String windDirection;
    public String condition; // "clear", "cloudy", "rain", "snow", "fog"
    public int aqi;
    public int uvIndex;
    public long timestamp;
}