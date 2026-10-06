package com.example.legmasky;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
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

    public static void downloadAndInstallApk(Context context, String url, String versionName) {
        String fileName = "legmasky-" + versionName + ".apk";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        if (file.exists()) {
            installApk(context, file);
            return;
        }

        Toast.makeText(context, "Download aggiornamento avviato...", Toast.LENGTH_SHORT).show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Aggiornamento LegmaSky");
        request.setDescription("Download della versione " + versionName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(file));
        request.setMimeType("application/vnd.android.package-archive");

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) {
            Toast.makeText(context, "Impossibile avviare il DownloadManager", Toast.LENGTH_SHORT).show();
            return;
        }

        long downloadId = manager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    try {
                        ctxt.unregisterReceiver(this);
                    } catch (Exception ignored) {}
                    installApk(ctxt, file);
                }
            }
        };

        ContextCompat.registerReceiver(
                context,
                onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
        );
    }

    private static void installApk(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setTitle("Autorizzazione richiesta")
                        .setMessage("Per completare l'aggiornamento automatico, abilita l'autorizzazione \"Consenti da questa fonte\" per LegmaSky nelle impostazioni.")
                        .setPositiveButton("Vai alle Impostazioni", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                    .setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Annulla", null)
                        .show();
                return;
            }
        }

        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    public static void openDownloadUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}