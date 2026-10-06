package com.example.legmasky;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legmasky.databinding.FragmentForecastDetailBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ForecastDetailFragment extends Fragment {

    private FragmentForecastDetailBinding binding;

    // Modelli temporanei per dati Mock
    public static class DayForecastMock {
        public String dayName;
        public String icon;
        public String tempRange;
        public List<HourlyForecastMock> hourlyList;

        public DayForecastMock(String dayName, String icon, String tempRange, List<HourlyForecastMock> hourlyList) {
            this.dayName = dayName;
            this.icon = icon;
            this.tempRange = tempRange;
            this.hourlyList = hourlyList;
        }
    }

    public static class HourlyForecastMock {
        public String time;
        public String icon;
        public String temp;
        public String wind;

        public HourlyForecastMock(String time, String icon, String temp, String wind) {
            this.time = time;
            this.icon = icon;
            this.temp = temp;
            this.wind = wind;
        }
    }

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
            if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
                ((androidx.appcompat.app.AppCompatActivity) getActivity()).getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Genera dati Template/Mock a 5 giorni con orari
        List<DayForecastMock> days = generateMockData();

        // Configura il selettore dei giorni
        DayAdapter dayAdapter = new DayAdapter(days, (selectedDay, position) -> {
            binding.tvSelectedDayTitle.setText("Previsione oraria per " + selectedDay.dayName);
            HourlyAdapter hourlyAdapter = new HourlyAdapter(selectedDay.hourlyList);
            binding.rvHourly.setAdapter(hourlyAdapter);
        });

        binding.rvDays.setAdapter(dayAdapter);

        // Seleziona di default il primo giorno
        if (!days.isEmpty()) {
            binding.tvSelectedDayTitle.setText("Previsione oraria per " + days.get(0).dayName);
            binding.rvHourly.setAdapter(new HourlyAdapter(days.get(0).hourlyList));
        }
    }

    // --- TEMPLATE DATA GENERATOR ---
    private List<DayForecastMock> generateMockData() {
        List<DayForecastMock> list = new ArrayList<>();
        String[] weekDays = {"Oggi", "Domani", "Mercoledì", "Giovedì", "Venerdì"};
        String[] icons = {"☀️", "⛅", "🌧️", "🌤️", "⛈️"};

        for (int i = 0; i < 5; i++) {
            List<HourlyForecastMock> hours = new ArrayList<>();
            for (int h = 0; h < 24; h += 3) {
                String timeStr = String.format("%02d:00", h);
                int mockTemp = 15 + (int) (Math.sin(h / 3.0) * 8);
                int mockWind = 5 + (h * 2) % 15;
                hours.add(new HourlyForecastMock(timeStr, icons[i], mockTemp + "°C", "💨 " + mockWind + " km/h"));
            }
            list.add(new DayForecastMock(weekDays[i], icons[i], "22° / 14°", hours));
        }
        return list;
    }

    // --- ADAPTER GIORNI (ORIZZONTALE) ---
    private static class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        private final List<DayForecastMock> items;
        private final OnDayClickListener listener;
        private int selectedPosition = 0;

        interface OnDayClickListener {
            void onDayClick(DayForecastMock day, int position);
        }

        public DayAdapter(List<DayForecastMock> items, OnDayClickListener listener) {
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
            DayForecastMock day = items.get(position);
            holder.tvDayName.setText(day.dayName);
            holder.tvDayIcon.setText(day.icon);
            holder.tvDayTemp.setText(day.tempRange);

            boolean isSelected = selectedPosition == position;
            holder.card.setStrokeColor(isSelected ? Color.WHITE : Color.parseColor("#66FFFFFF"));
            holder.card.setCardBackgroundColor(isSelected ? Color.parseColor("#66FFFFFF") : Color.parseColor("#33FFFFFF"));

            holder.itemView.setOnClickListener(v -> {
                int previous = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                listener.onDayClick(day, selectedPosition);
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
        private final List<HourlyForecastMock> items;

        public HourlyAdapter(List<HourlyForecastMock> items) {
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
            HourlyForecastMock item = items.get(position);
            holder.tvHour.setText(item.time);
            holder.tvWeatherIcon.setText(item.icon);
            holder.tvTemp.setText(item.temp);
            holder.tvWind.setText(item.wind);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHour, tvWeatherIcon, tvTemp, tvWind;

            ViewHolder(View v) {
                super(v);
                tvHour = v.findViewById(R.id.tvHour);
                tvWeatherIcon = v.findViewById(R.id.tvWeatherIcon);
                tvTemp = v.findViewById(R.id.tvTemp);
                tvWind = v.findViewById(R.id.tvWind);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}