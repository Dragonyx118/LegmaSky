package com.example.legmasky;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;

import com.example.legmasky.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gestisce automaticamente la trasparenza di status e navigation bar
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Applica gli insets al layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> insets);

        // Controllo degli aggiornamenti
        UpdateChecker.checkForUpdate(this, BuildConfig.VERSION_CODE, new UpdateChecker.UpdateListener() {
            @Override
            public void onUpdateAvailable(String versionName, String downloadUrl, String releaseNotes) {
                // Layout interno minimale per permettere lo scroll delle note di rilascio
                TextView tvNotes = new TextView(MainActivity.this);
                tvNotes.setText(releaseNotes.isEmpty() ? "Miglioramenti e correzioni di bug." : releaseNotes);
                tvNotes.setPadding(60, 20, 60, 20);
                tvNotes.setTextSize(14f);

                ScrollView scrollView = new ScrollView(MainActivity.this);
                scrollView.addView(tvNotes);

                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setIcon(android.R.drawable.stat_sys_download)
                        .setTitle("Aggiornamento " + versionName)
                        .setMessage("È disponibile una nuova versione dell'app.")
                        .setView(scrollView)
                        .setCancelable(false)
                        .setPositiveButton("Aggiorna ora", (dialog, which) -> {
                            UpdateChecker.downloadAndInstallApk(MainActivity.this, downloadUrl, versionName);
                        })
                        .setNegativeButton("Più tardi", null)
                        .show();
            }

            @Override
            public void onUpToDate() {
                // Nessun aggiornamento necessario
            }

            @Override
            public void onError(String message) {
                Log.e("LegmaSky", "Errore controllo aggiornamenti: " + message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Controlla se c'è un APK scaricato in attesa di essere installato
        // Cerca i file scaricati nella cartella dei download dell'app
        File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir != null && downloadsDir.exists()) {
            File[] files = downloadsDir.listFiles((dir, name) -> name.startsWith("legmasky-") && name.endsWith(".apk"));
            if (files != null && files.length > 0) {
                File pendingApk = files[0]; // Prende il primo APK scaricato

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (getPackageManager().canRequestPackageInstalls()) {
                        // L'utente ha appena concesso il permesso: avvia subito l'installatore
                        Uri apkUri = FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".provider",
                                pendingApk
                        );
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                }
            }
        }
    }
}