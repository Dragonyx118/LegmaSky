package com.example.legmasky.model;

public class ForecastResponse {
    public boolean success;
    public String station_id;
    public Forecast forecast;

    public static class Forecast {
        public String condition;
        public String label;
        public String icon;
        public double pressure_slp;
        public String season;
        public String trend;
        public String trend_icon;
        public Double delta_1h;
        public Double delta_3h;
        public Double delta_6h;
        public Double delta_24h;
        public boolean worsening;
        public boolean improving;
    }
}