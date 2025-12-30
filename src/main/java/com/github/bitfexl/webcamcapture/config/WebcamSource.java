package com.github.bitfexl.webcamcapture.config;

public record WebcamSource(
        String name,
        String url,
        boolean addRandom,
        String updateInterval,
        Integer maxCaptures
) {
}
