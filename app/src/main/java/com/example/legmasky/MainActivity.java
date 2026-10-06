package com.example.legmasky;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.legmasky.BuildConfig;

import com.example.legmasky.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        // Estende la vista sotto status bar e navigation bar senza aggiungere margin al layout principale
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            return insets;
        });
        UpdateChecker.checkForUpdate(this, BuildConfig.VERSION_CODE, new UpdateChecker.UpdateListener() {
            @Override
            public void onUpdateAvailable(String versionName, String downloadUrl, String releaseNotes) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Aggiornamento disponibile")
                        .setMessage("Nuova versione " + versionName + " disponibile.\n\n" + releaseNotes)
                        .setPositiveButton("Scarica", (dialog, which) -> {
                            UpdateChecker.openDownloadUrl(MainActivity.this, downloadUrl);
                        })
                        .setNegativeButton("Più tardi", null)
                        .show();
            }

            @Override
            public void onUpToDate() {
                // nessuna azione, app già aggiornata
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("LegmaSky", "Controllo aggiornamenti fallito: " + message);
            }
        });
    }
}