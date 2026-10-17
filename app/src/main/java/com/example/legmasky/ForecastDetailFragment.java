package com.example.legmasky;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legmasky.databinding.FragmentForecastDetailBinding;
import com.example.legmasky.model.ForecastExtendedResponse;
import com.example.legmasky.network.ApiClient;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastDetailFragment extends Fragment {

    private static final String STATION_ID = "station-001";
    private FragmentForecastDetailBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForecastDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configura la toolbar
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // --- LOGICA DI SFONDO DINAMICO COERENTE CON FIRSTFRAGMENT ---
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

        // Recupera le previsioni estese (da rete con fallback automatico su cache offline)
        fetchExtendedForecast();
    }

    private void fetchExtendedForecast() {
        ApiClient.getApi().getExtendedForecast(STATION_ID, "base").enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ForecastExtendedResponse> call, @NonNull Response<ForecastExtendedResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().forecast_days != null) {
                    ForecastExtendedResponse data = response.body();

                    // 1. Salva la cache fresca
                    ForecastCacheManager.saveForecast(requireContext(), data);

                    // 2. Nascondi il banner dell'avviso offline (i dati sono online)
                    if (binding != null) {
                        binding.cardOfflineNotice.setVisibility(View.GONE);
                    }

                    // 3. Popola gli adapter
                    setupAdapters(data.forecast_days);
                } else {
                    android.util.Log.e("LegmaSky", "Errore HTTP forecast esteso: " + response.code());
                    loadFromCacheOrFallback();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastExtendedResponse> call, @NonNull Throwable t) {
                android.util.Log.e("LegmaSky", "Fallimento di rete forecast esteso: " + t.getMessage());
                loadFromCacheOrFallback();
            }
        });
    }

    private void loadFromCacheOrFallback() {
        if (!isAdded() || getContext() == null || binding == null) return;

        ForecastExtendedResponse cachedData = ForecastCacheManager.loadCachedForecast(requireContext());
        if (cachedData != null && cachedData.forecast_days != null && !cachedData.forecast_days.isEmpty()) {

            // --- MOSTRA E CONFIGURA IL BANNER OFFLINE ---
            String timeAgo = ForecastCacheManager.getFormattedTimeAgo(requireContext());
            binding.tvOfflineNotice.setText("Dati in cache (offline). Ultimo aggiornamento: " + timeAgo);
            binding.cardOfflineNotice.setVisibility(View.VISIBLE);

            // --- LOGICA DI SCORRIMENTO DEI GIORNI SCADUTI ---
            long cacheTimestamp = ForecastCacheManager.getCacheTimestamp(requireContext());
            long now = System.currentTimeMillis();
            long diffMillis = now - cacheTimestamp;
            int diffDays = (int) (diffMillis / (1000L * 60L * 60L * 24L));

            List<ForecastExtendedResponse.DayForecast> days = cachedData.forecast_days;

            if (diffDays > 0) {
                // Rimuovi i giorni ormai trascorsi dall'inizio
                for (int i = 0; i < diffDays && !days.isEmpty(); i++) {
                    days.remove(0);
                }

                // Aggiorna i nomi dei giorni rimasti
                for (int i = 0; i < days.size(); i++) {
                    if (i == 0) days.get(i).dayName = "Oggi";
                    else if (i == 1) days.get(i).dayName = "Domani";
                }

                // Completa fino a 5 giorni marcandoli con "No data"
                while (days.size() < 5) {
                    ForecastExtendedResponse.DayForecast noDataDay = new ForecastExtendedResponse.DayForecast();
                    int dayIndex = days.size();
                    noDataDay.dayName = (dayIndex == 0) ? "Oggi" : (dayIndex == 1 ? "Domani" : "Giorno +" + (dayIndex + 1));
                    noDataDay.tempRange = "No data";
                    noDataDay.icon = "❓";
                    noDataDay.hourlyList = new java.util.ArrayList<>();

                    for (int h = 0; h < 24; h++) {
                        ForecastExtendedResponse.HourlyForecast hItem = new ForecastExtendedResponse.HourlyForecast();
                        hItem.time = String.format(java.util.Locale.getDefault(), "%02d:00", h);
                        hItem.condition = "No data";
                        hItem.temp = "N/D";
                        hItem.rain = "N/D";
                        hItem.icon = "❓";
                        noDataDay.hourlyList.add(hItem);
                    }

                    days.add(noDataDay);
                }
            }

            setupAdapters(days);
        } else {
            android.util.Log.e("LegmaSky", "Nessuna cache locale disponibile.");
            binding.tvOfflineNotice.setText("Connessione assente. Nessun dato in cache.");
            binding.cardOfflineNotice.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapters(List<ForecastExtendedResponse.DayForecast> days) {
        if (binding == null) return;

        // Configura il selettore dei giorni orizzontale
        DayAdapter dayAdapter = new DayAdapter(days, (selectedDay, position) -> {
            HourlyAdapter hourlyAdapter = new HourlyAdapter(selectedDay.hourlyList);
            binding.rvHourly.setAdapter(hourlyAdapter);
        });

        binding.rvDays.setAdapter(dayAdapter);

        // Seleziona di default il primo giorno (Oggi)
        if (!days.isEmpty()) {
            binding.rvHourly.setAdapter(new HourlyAdapter(days.get(0).hourlyList));
        }
    }

    // --- ADAPTER GIORNI (ORIZZONTALE) ---
    private static class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        private final List<ForecastExtendedResponse.DayForecast> items;
        private final OnDayClickListener listener;
        private int selectedPosition = 0;

        interface OnDayClickListener {
            void onDayClick(ForecastExtendedResponse.DayForecast day, int position);
        }

        public DayAdapter(List<ForecastExtendedResponse.DayForecast> items, OnDayClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_selector, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ForecastExtendedResponse.DayForecast day = items.get(position);
            holder.tvDayName.setText(day.dayName);
            holder.tvDayIcon.setText(day.icon);
            holder.tvDayTemp.setText(day.tempRange);

            boolean isSelected = selectedPosition == position;
            holder.card.setStrokeColor(isSelected ? Color.WHITE : Color.parseColor("#66FFFFFF"));
            holder.card.setCardBackgroundColor(isSelected ? Color.parseColor("#66FFFFFF") : Color.parseColor("#33FFFFFF"));

            holder.itemView.setOnClickListener(v -> {
                int previous = selectedPosition;
                selectedPosition = holder.getAbsoluteAdapterPosition();
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previous);
                    notifyItemChanged(selectedPosition);
                    listener.onDayClick(day, selectedPosition);
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView tvDayName, tvDayIcon, tvDayTemp;

            ViewHolder(View v) {
                super(v);
                card = (MaterialCardView) v;
                tvDayName = v.findViewById(R.id.tvDayName);
                tvDayIcon = v.findViewById(R.id.tvDayIcon);
                tvDayTemp = v.findViewById(R.id.tvDayTemp);
            }
        }
    }

    // --- ADAPTER METEO ORARIO (VERTICALE) ---
    private static class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
        private final List<ForecastExtendedResponse.HourlyForecast> items;

        public HourlyAdapter(List<ForecastExtendedResponse.HourlyForecast> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (items == null) return;
            ForecastExtendedResponse.HourlyForecast item = items.get(position);
            holder.tvHour.setText(item.time);
            holder.tvWeatherIcon.setText(item.icon);
            if (holder.tvCondition != null) {
                holder.tvCondition.setText(item.condition);
            }
            holder.tvTemp.setText(item.temp);
            if (holder.tvRain != null) {
                holder.tvRain.setText(item.rain);
            }
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHour, tvWeatherIcon, tvCondition, tvTemp, tvRain;

            ViewHolder(View v) {
                super(v);
                tvHour = v.findViewById(R.id.tvHour);
                tvWeatherIcon = v.findViewById(R.id.tvWeatherIcon);
                tvCondition = v.findViewById(R.id.tvCondition);
                tvTemp = v.findViewById(R.id.tvTemp);
                tvRain = v.findViewById(R.id.tvRain);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
}