package com.example.legmasky;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.legmasky.model.ForecastExtendedResponse;
import com.google.gson.Gson;

public class ForecastCacheManager {
    private static final String PREF_NAME = "legma_forecast_cache_prefs";
    private static final String KEY_JSON = "cached_forecast_json";
    private static final String KEY_TIMESTAMP = "cached_timestamp";

    public static void saveForecast(Context context, ForecastExtendedResponse response) {
        if (response == null || context == null) return;
        try {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = gson.toJson(response);

            boolean success = prefs.edit()
                    .putString(KEY_JSON, json)
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .commit(); // Scrittura sincrona per garantire la persistenza immediata su disco

            Log.d("LegmaCache", "Cache salvata con successo: " + success);
        } catch (Exception e) {
            Log.e("LegmaCache", "Errore nel salvataggio cache: " + e.getMessage(), e);
        }
    }

    public static ForecastExtendedResponse loadCachedForecast(Context context) {
        if (context == null) return null;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_JSON, null);

        if (json == null || json.isEmpty()) {
            Log.w("LegmaCache", "Nessuna stringa JSON trovata in SharedPreferences.");
            return null;
        }

        try {
            Gson gson = new Gson();
            ForecastExtendedResponse data = gson.fromJson(json, ForecastExtendedResponse.class);
            Log.d("LegmaCache", "Cache caricata con successo su disco!");
            return data;
        } catch (Exception e) {
            Log.e("LegmaCache", "Errore nel parsing JSON dalla cache: " + e.getMessage(), e);
            return null;
        }
    }

    public static long getCacheTimestamp(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_TIMESTAMP, 0);
    }

    public static String getFormattedTimeAgo(Context context) {
        long timestamp = getCacheTimestamp(context);
        if (timestamp == 0) return "Sconosciuto";

        long diffMillis = System.currentTimeMillis() - timestamp;
        long minutes = diffMillis / (1000L * 60L);
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) {
            return "Poco fa";
        } else if (minutes < 60) {
            return minutes + " min fa";
        } else if (hours < 24) {
            return hours + (hours == 1 ? " ora fa" : " ore fa");
        } else {
            return days + (days == 1 ? " giorno fa" : " giorni fa");
        }
    }
}