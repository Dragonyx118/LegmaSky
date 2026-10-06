package com.example.legmasky;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.example.legmasky.BuildConfig;

import com.example.legmasky.databinding.ActivityMainBinding;

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
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Aggiornamento disponibile")
                        .setMessage("Nuova versione " + versionName + " disponibile.\n\n" + releaseNotes)
                        .setPositiveButton("Scarica", (dialog, which) ->
                                UpdateChecker.openDownloadUrl(MainActivity.this, downloadUrl))
                        .setNegativeButton("Più tardi", null)
                        .show();
            }

            @Override
            public void onUpToDate() {
                // Nessuna azione: app già aggiornata
            }

            @Override
            public void onError(String message) {
                Log.e("LegmaSky", "Controllo aggiornamenti fallito: " + message);
            }
        });
    }
}