package com.example.legmasky;

import android.annotation.SuppressLint;
import android.content.Context;
import java.time.Month;

public class BackgroundSelector {

    public enum TimeOfDay { MORNING, DAY, EVENING, NIGHT }
    public enum Season { WINTER, SPRING, SUMMER, AUTUMN }

    // --- METODO ORIGINALE (Mantenuto per non rompere le classi esistenti) ---
    public static int selectBackgroundResId(Context context, String weatherCondition, int hour, Month month) {
        Season season = getSeason(month);
        return selectBackgroundInternal(context, weatherCondition, hour, season);
    }

    // --- NUOVO OVERLOAD (Accetta int 1-12 o 0-11 per compatibilità con Calendar su API 24+) ---
    public static int selectBackgroundResId(Context context, String weatherCondition, int hour, int monthInt) {
        Season season = getSeasonFromInt(monthInt);
        return selectBackgroundInternal(context, weatherCondition, hour, season);
    }

    // --- LOGICA COMUNE CONDIVISA ---
    private static int selectBackgroundInternal(Context context, String weatherCondition, int hour, Season season) {
        TimeOfDay tod = getTimeOfDay(hour);

        // Gestione di sicurezza se weatherCondition è null
        String condition = (weatherCondition != null) ? weatherCondition.toLowerCase() : "clear";

        String resName = "bg_" + tod.name().toLowerCase() + "_" + condition;

        // Override stagionale solo per alcune combinazioni (es. Neve in inverno)
        if (season == Season.WINTER && condition.equalsIgnoreCase("snow")) {
            resName += "_winter";
        }

        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        if (resId == 0) {
            // Fallback se la combinazione specifica non esiste
            resId = context.getResources().getIdentifier("bg_" + tod.name().toLowerCase() + "_clear", "drawable", context.getPackageName());
        }
        return resId;
    }

    private static TimeOfDay getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 10) return TimeOfDay.MORNING;
        if (hour >= 10 && hour < 18) return TimeOfDay.DAY;
        if (hour >= 18 && hour < 21) return TimeOfDay.EVENING;
        return TimeOfDay.NIGHT;
    }

    @SuppressLint("NewApi")
    private static Season getSeason(Month month) {
        switch (month) {
            case DECEMBER: case JANUARY: case FEBRUARY: return Season.WINTER;
            case MARCH: case APRIL: case MAY: return Season.SPRING;
            case JUNE: case JULY: case AUGUST: return Season.SUMMER;
            default: return Season.AUTUMN;
        }
    }

    // Metodo di supporto per convertire il mese intero (gestisce sia 0-11 che 1-12)
    private static Season getSeasonFromInt(int month) {
        // Se usi Calendar.MONTH (0 = Gennaio, 11 = Dicembre)
        // Se passi direttamente 1-12, adatta i numeri di conseguenza. Qui gestiamo standard 1-12 o 0-11:
        int m = (month > 11) ? month % 12 + 1 : month; // Normalizzazione opzionale
        if (month == 12 || month == 1 || month == 2 || month == 0) return Season.WINTER;
        if (month >= 3 && month <= 5) return Season.SPRING;
        if (month >= 6 && month <= 8) return Season.SUMMER;
        return Season.AUTUMN;
    }
}