package com.example.legmasky.model;

import java.util.List;

public class ForecastExtendedResponse {
    public boolean success;
    public String station_id;
    public CurrentSummary current_summary;
    public List<DayForecast> forecast_days;
    public double correction_bias_applied;

    public static class CurrentSummary {
        public String condition;
        public String label;
        public String icon;
        public double pressure_slp;
        public String season;
        public String trend;
        public String trend_icon;
    }

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
    }
}