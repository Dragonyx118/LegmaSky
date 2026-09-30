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
import com.google.android.material.appbar.AppBarLayout;

import java.util.Calendar;

public class FirstFragment extends Fragment {

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

        updateDynamicBackground(false, false);

        // Gestione dissolvenza allo scroll
        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (totalScrollRange == 0) return;

                float percentage = (float) Math.abs(verticalOffset) / (float) totalScrollRange;

                // Fai sfumare il testo centrale in uscita
                binding.expandedContent.setAlpha(1f - (percentage * 1.5f));

                // Mostra il titolo nella Toolbar quando collassato
                if (percentage >= 0.8f) {
                    binding.collapsingToolbar.setTitleEnabled(true);
                } else {
                    binding.collapsingToolbar.setTitleEnabled(false);
                }
            }
        });
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

        // Estrae il colore superiore dall'immagine per la Toolbar durante il collasso
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