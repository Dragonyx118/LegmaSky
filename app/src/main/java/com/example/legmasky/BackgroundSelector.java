package com.example.legmasky;

import android.annotation.SuppressLint;
import android.content.Context;
import java.time.LocalDateTime;
import java.time.Month;

public class BackgroundSelector {

    public enum TimeOfDay { MORNING, DAY, EVENING, NIGHT }
    public enum Season { WINTER, SPRING, SUMMER, AUTUMN }

    public static int selectBackgroundResId(Context context, String weatherCondition, int hour, Month month) {
        TimeOfDay tod = getTimeOfDay(hour);
        Season season = getSeason(month);

        String resName = "bg_" + tod.name().toLowerCase() + "_" + weatherCondition.toLowerCase();

        // Override stagionale solo per alcune combinazioni (es. neve in inverno)
        if (season == Season.WINTER && weatherCondition.equalsIgnoreCase("snow")) {
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
}