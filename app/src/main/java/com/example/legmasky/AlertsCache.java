package com.example.legmasky;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.legmasky.model.OfficialAlertsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AlertsCache {

    private static final String PREFS_NAME = "alerts_cache";
    private static final String KEY_ALERTS_JSON = "alerts_json";
    private static final String KEY_HAS_ALERTS = "has_alerts";

    public static void save(Context context, List<OfficialAlertsResponse.Alert> alerts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(alerts);
        prefs.edit()
                .putString(KEY_ALERTS_JSON, json)
                .putBoolean(KEY_HAS_ALERTS, true)
                .apply();
    }

    public static List<OfficialAlertsResponse.Alert> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_HAS_ALERTS, false)) return null;

        String json = prefs.getString(KEY_ALERTS_JSON, null);
        if (json == null) return null;

        Type type = new TypeToken<List<OfficialAlertsResponse.Alert>>() {}.getType();
        return new Gson().fromJson(json, type);
    }
}