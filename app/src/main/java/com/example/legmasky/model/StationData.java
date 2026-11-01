package com.example.legmasky.model;

public class StationData {
    public boolean success;
    public String station_id;
    public String module;
    public LastData data;

    // Usato da /stations/{station_id}, che mette i dati in "last_data" invece di "data"
    public LastData last_data;

    // Presenti solo nella risposta di /data/{station_id}/latest
    public SunInfo sun;
    public String sensor_warning;

    public static class LastData {
        public double humidity;
        public double lux;
        public double pressure;
        public double rain_mm;
        public double temperature;
        public double wind_direction;
        public double wind_speed;

        // NEW — presente solo se temperature+humidity disponibili (tipicamente modulo "base")
        // Double (boxed) invece di double: resta null se il backend non lo calcola,
        // cosi' l'app puo' distinguere "0.0 gradi" da "non disponibile"
        public Double feels_like;

        // NEW — presenti solo chiamando getLatest(stationId, "mod-storm"), modulo del sensore
        // fulmini AS3935. Lightning_distance_km = distanza dell'ultimo fulmine rilevato;
        // lightning_strikes_last_hour = numero di fulmini rilevati nell'ultima ora
        // (entro il raggio di rilevamento del sensore, tipicamente fino a 40km)
        public Double lightning_distance_km;
        public Double lightning_strikes_last_hour;
    }
}