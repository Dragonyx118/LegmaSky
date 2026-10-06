package com.example.legmasky.model;

import java.util.List;

public class OfficialAlertsResponse {
    public boolean success;
    public String station_id;
    public List<Alert> alerts;

    public static class Alert {
        public String source;
        public String color;
        public String title;
        public String zone;
    }
}