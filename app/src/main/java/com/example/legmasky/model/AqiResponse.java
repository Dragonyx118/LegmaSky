package com.example.legmasky.model;

public class AqiResponse {
    public String status; // "ok" se la richiesta è andata a buon fine
    public Data data;

    public static class Data {
        public int aqi; // indice AQI (scala WAQI, 0-500+)
    }
}