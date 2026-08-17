package com.garage.management.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Date/time utility for ETA calculation and formatting.
 */
public final class DateTimeUtil {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a").withZone(IST);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a").withZone(IST);

    private DateTimeUtil() {}

    /**
     * Calculate ETA = startTime + durationMinutes.
     */
    public static Instant calculateEta(Instant startTime, int durationMinutes) {
        return startTime.plusSeconds((long) durationMinutes * 60);
    }

    /**
     * Format an Instant to a human-readable display string (IST).
     */
    public static String format(Instant instant) {
        if (instant == null) return "N/A";
        return DISPLAY_FORMATTER.format(instant);
    }

    /**
     * Format only the time portion (e.g. "04:30 PM").
     */
    public static String formatTime(Instant instant) {
        if (instant == null) return "N/A";
        return TIME_FORMATTER.format(instant);
    }

    /**
     * Current UTC timestamp.
     */
    public static Instant now() {
        return Instant.now();
    }
}
