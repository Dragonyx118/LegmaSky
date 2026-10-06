package com.example.legmasky;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private static final String TAG = "LegmaSky";
    private static final String GITHUB_API_URL =
            "https://api.github.com/repos/Dragonyx118/LegmaSky/releases/latest";

    public interface UpdateListener {
        void onUpdateAvailable(String versionName, String downloadUrl, String releaseNotes);
        void onUpToDate();
        void onError(String message);
    }

    public static void checkForUpdate(Context context, int currentVersionCode, UpdateListener listener) {
        new Thread(() -> {
            try {
                JSONObject json = fetchLatestReleaseJson();
                String tagName = json.getString("tag_name"); // es. "v1.3.0"
                String releaseNotes = json.optString("body", "");

                int remoteVersionCode = extractVersionCode(releaseNotes);

                String downloadUrl = null;
                if (json.has("assets") && json.getJSONArray("assets").length() > 0) {
                    downloadUrl = json.getJSONArray("assets")
                            .getJSONObject(0)
                            .getString("browser_download_url");
                } else if (json.has("html_url")) {
                    downloadUrl = json.getString("html_url");
                }

                Handler mainHandler = new Handler(Looper.getMainLooper());

                if (remoteVersionCode > currentVersionCode && downloadUrl != null) {
                    String finalDownloadUrl = downloadUrl;
                    mainHandler.post(() -> listener.onUpdateAvailable(tagName, finalDownloadUrl, releaseNotes));
                } else {
                    mainHandler.post(listener::onUpToDate);
                }

            } catch (Exception e) {
                Log.e(TAG, "Errore controllo aggiornamenti: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> listener.onError(e.getMessage()));
            }
        }).start();
    }

    private static JSONObject fetchLatestReleaseJson() throws Exception {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } finally {
            conn.disconnect();
        }
    }

    private static int extractVersionCode(String releaseBody) {
        try {
            for (String line : releaseBody.split("\n")) {
                if (line.trim().startsWith("versionCode=")) {
                    return Integer.parseInt(line.trim().split("=")[1].trim());
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public static void openDownloadUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}