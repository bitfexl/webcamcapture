package com.github.bitfexl.webcamcapture.util;

public class DurationParser {
    private DurationParser() { }

    /**
     * A regex pattern to check if a duration is parsable.
     */
    public static final String DURATION_PATTERN = "[1-9]\\d*(\\.\\d+)?[smh]";

    /**
     * Parse a prechecked duration to milliseconds.
     * @param duration The duration to parse, must be prechecked with DURATION_PATTERN.
     * @return The duration in milliseconds.
     */
    public static long parseDuration(String duration) {
        double number = Double.parseDouble(duration.substring(0, duration.length() - 1));
        number *= 1000;

        final char unit = duration.charAt(duration.length() - 1);
        if (unit == 'm' || unit == 'h') {
            number *= 60;
        }
        if (unit == 'h') {
            number *= 60;
        }

        return (long) number;
    }
}
