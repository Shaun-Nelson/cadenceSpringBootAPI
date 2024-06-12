package com.snelson.cadenceAPI.utils;

public class TimeConverter {

    public static String convertMillisToMinutesSeconds(long millis) {
        long minutes = Math.abs((millis / 1000) / 60);
        long seconds = (millis / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

