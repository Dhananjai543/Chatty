package com.chatty.utilities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private DateUtils() {
        // Utility class - prevent instantiation
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_ONLY_FORMATTER);
    }

    public static String formatDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_ONLY_FORMATTER);
    }

    public static LocalDateTime fromDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDateTime today = LocalDateTime.now();
        return dateTime.toLocalDate().equals(today.toLocalDate());
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        
        long days = hours / 24;
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";
        
        return formatDateOnly(dateTime);
    }
}
