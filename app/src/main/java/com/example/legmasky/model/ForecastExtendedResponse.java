package com.example.legmasky.model;

import java.util.List;

public class ForecastExtendedResponse {
    public boolean success;
    public String station_id;
    public ForecastResponse.Forecast current_summary;
    public List<DayForecast> forecast_days;

    public static class DayForecast {
        public String dayName;
        public String icon;
        public String tempRange;
        public List<HourlyForecast> hourlyList;
    }

    public static class HourlyForecast {
        public String time;
        public String icon;
        public String condition;
        public String temp;
        public String rain;
        public String wind;
    }
}