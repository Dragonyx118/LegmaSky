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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.example.legmasky.databinding.FragmentFirstBinding;
import com.example.legmasky.model.StationData;
import com.example.legmasky.network.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Handler;
import android.os.Looper;
import com.example.legmasky.model.ForecastResponse;

public class FirstFragment extends Fragment {

    private static final String STATION_ID = "station-001";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private FragmentFirstBinding binding;

    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minuti

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Nome città provvisorio finché il GPS non risponde
        binding.tvCityName.setText("Posizione...");

        // Sfondo provvisorio finché non arrivano i dati reali
        updateDynamicBackground(false, false);

        checkPermissionAndFetchLocation();
        fetchWeatherData();
        startAutoRefresh();
        fetchForecast();

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
        // Geocoder fa lavoro di rete/CPU: va eseguito fuori dal thread principale
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.ITALIAN);
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

                String cityName = "Posizione sconosciuta";
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    // Locality = città; fallback su subAdminArea (es. Provincia) se la città manca
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
        // Mostra subito l'ultimo dato salvato, mentre aspetti la risposta dal server
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
        int month = calendar.get(Calendar.MONTH); // 0 = Gennaio, 11 = Dicembre

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
                if (palette != null) {
                    int topColor = palette.getDarkMutedColor(palette.getDominantColor(Color.TRANSPARENT));
                    binding.collapsingToolbar.setContentScrimColor(topColor);
                    binding.collapsingToolbar.setStatusBarScrimColor(topColor);
                }
            });
        }
    }

    private int getDayBackgroundBySeason(int month) {
        // month: 0=Gen, 1=Feb, ... 11=Dic
        if (month == 11 || month == 0 || month == 1) {
            return R.drawable.bg_winter_foggy; // inverno
        } else if (month >= 8 && month <= 10) {
            return R.drawable.bg_autumn_clear; // autunno
        } else {
            return R.drawable.bg_summer_clear; // primavera + estate (nessun asset "spring" ancora)
        }
    }

    private String getSeason(int month) {
        // month: 0=Gen, 1=Feb, ... 11=Dic
        if (month == 11 || month == 0 || month == 1) return "winter";
        if (month >= 2 && month <= 4) return "spring";
        if (month >= 5 && month <= 7) return "summer";
        return "autumn"; // mesi 8, 9, 10
    }

    private String getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 10) return "morning";
        if (hour >= 10 && hour < 18) return "day";
        if (hour >= 18 && hour < 21) return "evening";
        return "night";
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        binding = null;
    }
}