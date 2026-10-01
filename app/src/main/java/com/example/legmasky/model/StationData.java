package com.example.legmasky.model;

public class StationData {
    public boolean success;
    public String station_id;
    public String module;
    public LastData data;

    // Usato da /stations/{station_id}, che mette i dati in "last_data" invece di "data"
    public LastData last_data;

    public static class LastData {
        public double humidity;
        public double lux;
        public double pressure;
        public double rain_mm;
        public double temperature;
        public double wind_direction;
        public double wind_speed;
    }
}