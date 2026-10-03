package com.example.legmasky;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.legmasky.model.StationData;

public class WeatherCache {

    private static final String PREFS_NAME = "weather_cache";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_HUMIDITY = "humidity";
    private static final String KEY_PRESSURE = "pressure";
    private static final String KEY_WIND_SPEED = "wind_speed";
    private static final String KEY_WIND_DIRECTION = "wind_direction";
    private static final String KEY_TIMESTAMP = "last_update_timestamp";
    private static final String KEY_HAS_DATA = "has_data";

    public static void save(Context context, StationData.LastData data) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_TEMPERATURE, (float) data.temperature)
                .putFloat(KEY_HUMIDITY, (float) data.humidity)
                .putFloat(KEY_PRESSURE, (float) data.pressure)
                .putFloat(KEY_WIND_SPEED, (float) data.wind_speed)
                .putFloat(KEY_WIND_DIRECTION, (float) data.wind_direction)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .putBoolean(KEY_HAS_DATA, true)
                .apply();
    }

    public static StationData.LastData load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_HAS_DATA, false)) return null;

        StationData.LastData data = new StationData.LastData();
        data.temperature = prefs.getFloat(KEY_TEMPERATURE, 0f);
        data.humidity = prefs.getFloat(KEY_HUMIDITY, 0f);
        data.pressure = prefs.getFloat(KEY_PRESSURE, 0f);
        data.wind_speed = prefs.getFloat(KEY_WIND_SPEED, 0f);
        data.wind_direction = prefs.getFloat(KEY_WIND_DIRECTION, 0f);
        return data;
    }

    public static long getLastUpdateTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_TIMESTAMP, 0L);
    }
}