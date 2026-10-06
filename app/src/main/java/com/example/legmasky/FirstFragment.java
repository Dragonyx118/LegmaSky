package com.example.legmasky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.example.legmasky.databinding.FragmentFirstBinding;
import com.example.legmasky.model.ForecastResponse;
import com.example.legmasky.model.OfficialAlertsResponse;
import com.example.legmasky.model.StationData;
import com.example.legmasky.network.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private static final String STATION_ID = "station-001";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private FragmentFirstBinding binding;

    // Timer per dati meteo di base e previsioni (5 minuti)
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    // Timer dedicato ad alta frequenza per le allerte meteo (1 minuto)
    private Handler alertsRefreshHandler;
    private Runnable alertsRefreshRunnable;
    private static final long ALERTS_REFRESH_INTERVAL_MS = 1 * 60 * 1000;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Nome città provvisorio finché il GPS non risponde
        binding.tvCityName.setText("Posizione...");

        // Sfondo provvisorio finché non arrivano i dati reali
        updateDynamicBackground(false, false);

        checkPermissionAndFetchLocation();

        // 1. Caricamento immediato da Cache locale prima della rete
        List<OfficialAlertsResponse.Alert> cachedAlerts = AlertsCache.load(requireContext());
        if (cachedAlerts != null) {
            bindOfficialAlerts(cachedAlerts);
        }

        fetchWeatherData();
        fetchForecast();

        // 2. Avvio dei timer separati per aggiornamento automatico
        startAutoRefresh();
        startAlertsAutoRefresh();

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

    private void fetchOfficialAlerts() {
        ApiClient.getApi().getOfficialAlerts(STATION_ID).enqueue(new Callback<OfficialAlertsResponse>() {
            @Override
            public void onResponse(Call<OfficialAlertsResponse> call, Response<OfficialAlertsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<OfficialAlertsResponse.Alert> alerts = response.body().alerts;
                    // Salva le nuove allerte nella cache locale
                    AlertsCache.save(requireContext(), alerts);
                    bindOfficialAlerts(alerts);
                } else {
                    android.util.Log.e("LegmaSky", "Risposta allerte non valida. Mantengo ultime allerte in cache.");
                }
            }

            @Override
            public void onFailure(Call<OfficialAlertsResponse> call, Throwable t) {
                android.util.Log.e("LegmaSky", "Errore allerte ufficiali: " + t.getMessage(), t);
                // In caso di errore o server down, non pulisce le allerte visibili a schermo
            }
        });
    }

    private void bindOfficialAlerts(List<OfficialAlertsResponse.Alert> alerts) {
        if (binding == null) return;

        binding.alertsContainer.removeAllViews();

        if (alerts == null || alerts.isEmpty()) {
            binding.alertsContainer.setVisibility(View.GONE);
            return;
        }

        binding.alertsContainer.setVisibility(View.VISIBLE);

        for (OfficialAlertsResponse.Alert alert : alerts) {
            LinearLayout chip = new LinearLayout(requireContext());
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
            chip.setPadding(28, 20, 28, 20);

            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            chipParams.setMargins(0, 0, 0, 10);
            chip.setLayoutParams(chipParams);

            chip.setBackgroundResource(R.drawable.bg_badge_rounded);
            chip.getBackground().mutate().setTint(colorForAlert(alert.color));

            TextView tv = new TextView(requireContext());
            String text = iconForAlert(alert.color) + " " + alert.title;
            if (alert.zone != null && !alert.zone.isEmpty()) {
                text += " — " + alert.zone;
            }
            tv.setText(text);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(13f);

            chip.addView(tv);
            binding.alertsContainer.addView(chip);
        }
    }

    private String iconForAlert(String color) {
        switch (color) {
            case "gialla": return "🟡";
            case "arancione": return "🟠";
            case "rossa": return "🔴";
            default: return "⚪";
        }
    }

    private int colorForAlert(String color) {
        switch (color) {
            case "gialla": return Color.parseColor("#B8860B");
            case "arancione": return Color.parseColor("#CC6600");
            case "rossa": return Color.parseColor("#B22222");
            default: return Color.parseColor("#555555");
        }
    }

    private void fetchForecast() {
        ApiClient.getApi().getForecast(STATION_ID, "base").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().success && response.body().forecast != null) {
                    bindForecast(response.body().forecast);
                } else {
                    android.util.Log.e("LegmaSky", "Forecast non disponibile");
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                android.util.Log.e("LegmaSky", "Errore chiamata forecast: " + t.getMessage(), t);
            }
        });
    }

    private void bindForecast(ForecastResponse.Forecast f) {
        if (binding == null) return;
        binding.tvConditionAndMinMax.setText(f.icon + " " + f.label);
    }

    @SuppressLint("SetTextI18n")
    private void checkPermissionAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                resolveCityName(location.getLatitude(), location.getLongitude());
            } else {
                android.util.Log.e("LegmaSky", "Location nulla: GPS non ancora disponibile o disattivato");
                if (binding != null) binding.tvCityName.setText("Posizione non disponibile");
            }
        }).addOnFailureListener(e -> {
            android.util.Log.e("LegmaSky", "Errore ottenimento posizione: " + e.getMessage(), e);
            if (binding != null) binding.tvCityName.setText("Posizione non disponibile");
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndFetchLocation();
            } else {
                if (binding != null) binding.tvCityName.setText("Permesso posizione negato");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void resolveCityName(double lat, double lon) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.ITALIAN);
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

                String cityName = "Posizione sconosciuta";
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    cityName = address.getLocality() != null ? address.getLocality()
                            : address.getSubAdminArea() != null ? address.getSubAdminArea()
                              : "Posizione sconosciuta";
                }

                String finalCityName = cityName;
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvCityName.setText(finalCityName);
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("LegmaSky", "Errore geocoding: " + e.getMessage(), e);
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) binding.tvCityName.setText("Posizione sconosciuta");
                    });
                }
            }
        }).start();
    }

    private void fetchWeatherData() {
        StationData.LastData cached = WeatherCache.load(requireContext());
        if (cached != null) {
            bindWeatherData(cached, true);
        }

        ApiClient.getApi().getLatest(STATION_ID, "base").enqueue(new Callback<StationData>() {
            @Override
            public void onResponse(Call<StationData> call, Response<StationData> response) {
                android.util.Log.d("LegmaSky", "Risposta HTTP: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    StationData.LastData data = response.body().data;
                    WeatherCache.save(requireContext(), data);
                    bindWeatherData(data, false);
                } else {
                    android.util.Log.e("LegmaSky", "Risposta non valida. Mantengo ultimi dati in cache.");
                    showStaleDataWarning();
                }
            }

            @Override
            public void onFailure(Call<StationData> call, Throwable t) {
                android.util.Log.e("LegmaSky", "Errore chiamata API: " + t.getMessage(), t);
                showStaleDataWarning();
            }
        });
    }

    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchWeatherData();
                fetchForecast();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    private void startAlertsAutoRefresh() {
        alertsRefreshHandler = new Handler(Looper.getMainLooper());
        alertsRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOfficialAlerts();
                alertsRefreshHandler.postDelayed(this, ALERTS_REFRESH_INTERVAL_MS);
            }
        };
        // Esegue subito la chiamata iniziale e pianifica le successive ogni 60 sec
        fetchOfficialAlerts();
        alertsRefreshHandler.postDelayed(alertsRefreshRunnable, ALERTS_REFRESH_INTERVAL_MS);
    }

    @SuppressLint("SetTextI18n")
    private void showStaleDataWarning() {
        long lastUpdate = WeatherCache.getLastUpdateTimestamp(requireContext());
        if (lastUpdate > 0 && binding != null) {
            String time = new SimpleDateFormat("HH:mm", Locale.ITALIAN).format(new Date(lastUpdate));
            binding.tvConditionAndMinMax.setText("Dati non aggiornati (ultimo: " + time + ")");
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindWeatherData(StationData.LastData data, boolean isFromCache) {
        if (binding == null) return;
        binding.tvPressureValue.setText(Math.round(data.pressure) + " hPa");

        binding.tvLocationSub.setText("Stazione " + STATION_ID);
        binding.tvMainTemperature.setText(String.format("%.1f°", data.temperature));

        if (!isFromCache) {
            binding.tvConditionAndMinMax.setText("Pressione " + Math.round(data.pressure) + " hPa");
        }

        binding.tvHumidityValue.setText(Math.round(data.humidity) + "%");
        binding.tvWindValue.setText(String.valueOf(data.wind_speed));
        binding.lblWindDir.setText(windDirectionLabel(data.wind_direction));

        binding.tvUvValue.setText("N/D");
        binding.tvFeelsLikeValue.setText("N/D");

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
        int month = calendar.get(Calendar.MONTH);

        int backgroundRes;

        if (hour >= 5 && hour <= 6) {
            backgroundRes = R.drawable.bg_sunrise_clear;
        } else if (hour >= 7 && hour <= 8) {
            backgroundRes = R.drawable.bg_morning_cloudy;
        } else if (hour >= 9 && hour <= 17) {
            backgroundRes = getDayBackgroundBySeason(month);
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
                if (palette == null || binding == null) return;

                int topColor = palette.getDarkMutedColor(palette.getDominantColor(Color.TRANSPARENT));
                binding.collapsingToolbar.setContentScrimColor(topColor);
                binding.collapsingToolbar.setStatusBarScrimColor(topColor);

                int dominant = palette.getDominantColor(Color.WHITE);
                double luminance = ColorUtils.calculateLuminance(dominant);
                int textColor = luminance > 0.5 ? Color.BLACK : Color.WHITE;

                binding.tvCityName.setTextColor(textColor);
                binding.tvMainTemperature.setTextColor(textColor);
                binding.tvConditionAndMinMax.setTextColor(textColor);
                binding.tvLocationSub.setTextColor(textColor);
            });
        }
    }

    private int getDayBackgroundBySeason(int month) {
        if (month == 11 || month == 0 || month == 1) {
            return R.drawable.bg_winter_foggy;
        } else if (month >= 8 && month <= 10) {
            return R.drawable.bg_autumn_clear;
        } else {
            return R.drawable.bg_summer_clear;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (alertsRefreshHandler != null && alertsRefreshRunnable != null) {
            alertsRefreshHandler.removeCallbacks(alertsRefreshRunnable);
        }
        binding = null;
    }
}