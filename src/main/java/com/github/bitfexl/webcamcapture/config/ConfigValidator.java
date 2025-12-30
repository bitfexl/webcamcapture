package com.github.bitfexl.webcamcapture.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static com.github.bitfexl.webcamcapture.util.DurationParser.DURATION_PATTERN;

@ApplicationScoped
public class ConfigValidator {
    public boolean validate(WebcamCaptureConfig config) {
        if (config.cacheDir() == null) {
            return false;
        }

        if (!sourcesNameUnique(config)) {
            return false;
        }

        if (!sourcesValid(config)) {
            return false;
        }

        return true;
    }

    private boolean sourcesNameUnique(WebcamCaptureConfig config) {
        final Set<String> names = new HashSet<>();
        for (WebcamSource source : config.webcams()) {
            if (!names.add(source.name())) {
                return false;
            }
        }
        return true;
    }

    private boolean sourcesValid(WebcamCaptureConfig config) {
        for (WebcamSource source : config.webcams()) {
            if (!sourceValid(source)) {
                return false;
            }
        }
        return true;
    }

    private boolean sourceValid(WebcamSource source) {
        // check url
        try {
            URI.create(source.url());
        } catch (Exception ex) {
            return false;
        }

        // check update interval
        if (!source.updateInterval().matches(DURATION_PATTERN)) {
            return false;
        }

        // check max captures
        if (source.maxCaptures() != null && source.maxCaptures() < 1) {
            return false;
        }

        // check save interval
        if (source.minSaveInterval() != null && !source.minSaveInterval().matches(DURATION_PATTERN)) {
            return false;
        }

        return true;
    }
}
