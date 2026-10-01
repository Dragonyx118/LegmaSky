package com.example.legmasky;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.example.legmasky.databinding.FragmentFirstBinding;
import com.example.legmasky.model.StationData;
import com.example.legmasky.network.ApiClient;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private static final String STATION_ID = "station-001";

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sfondo provvisorio finché non arrivano i dati reali
        updateDynamicBackground(false, false);

        fetchWeatherData();

        // Gestione dissolvenza allo scroll
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (totalScrollRange == 0) return;

                float percentage = (float) Math.abs(verticalOffset) / (float) totalScrollRange;

                binding.expandedContent.setAlpha(1f - (percentage * 1.5f));

                if (percentage >= 0.8f) {
                    binding.collapsingToolbar.setTitleEnabled(true);
                } else {
                    binding.collapsingToolbar.setTitleEnabled(false);
                }
            }
        });
    }

    private void fetchWeatherData() {
        ApiClient.getApi().getLatest(STATION_ID, "base").enqueue(new Callback<StationData>() {
            @Override
            public void onResponse(Call<StationData> call, Response<StationData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindWeatherData(response.body().data);
                }
            }

            @Override
            public void onFailure(Call<StationData> call, Throwable t) {
                // TODO: mostra stato di errore (es. Snackbar o testo "Impossibile caricare i dati")
            }
        });
    }

    private void bindWeatherData(StationData.LastData data) {
        if (binding == null) return;
        binding.tvPressureValue.setText(Math.round(data.pressure) + " hPa");

        // Nome stazione statico: l'API non fornisce un nome leggibile, solo station_id
        binding.tvCityName.setText("Cascina Dossena");
        binding.tvLocationSub.setText("Stazione " + STATION_ID);

        binding.tvMainTemperature.setText(Math.round(data.temperature) + "°");
        binding.tvConditionAndMinMax.setText("Pressione " + data.pressure + " hPa");

        binding.tvHumidityValue.setText(Math.round(data.humidity) + "%");
        binding.tvWindValue.setText(String.valueOf(data.wind_speed));
        binding.lblWindDir.setText(windDirectionLabel(data.wind_direction));

        // UV, AQI, Percepita: non disponibili dal backend per ora — nascondiamo le relative card
        // invece di mostrare dati finti. Quando colleghi il modulo mod-air o calcoli la percepita,
        // puoi rimuovere queste righe e popolare i valori normalmente.
        binding.tvUvValue.setText("N/D");
        binding.tvFeelsLikeValue.setText("N/D");

        // Niente info meteo (sereno/pioggia/nuvoloso) dal backend attuale: sfondo resta legato solo all'ora
        updateDynamicBackground(false, false);
    }

    private String windDirectionLabel(double degrees) {
        String[] dirs = {"Nord", "Nord-est", "Est", "Sud-est", "Sud", "Sud-ovest", "Ovest", "Nord-ovest"};
        int index = (int) Math.round(degrees / 45.0) % 8;
        return dirs[index];
    }

    private void updateDynamicBackground(boolean isCloudy, boolean isFoggy) {
        if (binding == null) return;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int backgroundRes;

        if (isFoggy) {
            backgroundRes = R.drawable.bg_winter_foggy;
        } else if (isCloudy) {
            backgroundRes = R.drawable.bg_cloudy;
        } else if (hour >= 5 && hour <= 6) {
            backgroundRes = R.drawable.bg_sunrise_clear;
        } else if (hour >= 7 && hour <= 8) {
            backgroundRes = R.drawable.bg_morning_cloudy;
        } else if (hour >= 9 && hour <= 17) {
            backgroundRes = R.drawable.bg_summer_clear;
        } else if (hour >= 18 && hour <= 19) {
            backgroundRes = R.drawable.bg_first_sunset_clear;
        } else if (hour == 20) {
            backgroundRes = R.drawable.bg_late_sunset_clear;
        } else if (hour >= 21 && hour <= 22) {
            backgroundRes = R.drawable.bg_first_night_clear;
        } else {
            backgroundRes = R.drawable.bg_night_clear;
        }

        binding.backgroundImage.setImageResource(backgroundRes);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), backgroundRes);
        if (bitmap != null) {
            Palette.from(bitmap).generate(palette -> {
                if (palette != null) {
                    int topColor = palette.getDarkMutedColor(palette.getDominantColor(Color.TRANSPARENT));
                    binding.collapsingToolbar.setContentScrimColor(topColor);
                    binding.collapsingToolbar.setStatusBarScrimColor(topColor);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}