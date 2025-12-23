package com.github.bitfexl.webcamcapture.respsitories;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.io.file.FileWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class WebcamRepository {
    private final DateTimeFormatter filenameDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneOffset.UTC);

    private Path webcamCacheDir;

    @Inject
    void injectConfig(ApplicationConfig config) {
        webcamCacheDir = Path.of(config.config().cacheDir());
    }

    @Inject
    FileWriter fileWriter;

    private static class WebcamRecord {
        record WebcamImage(String hash, Instant timestamp, Path path) { }

        WebcamImage latestImage;

        String webcamDir;
    }

    private final Map<String, WebcamRecord> webcams = new HashMap<>();

    public Path getLatestImage(String webcamName) {
        final WebcamRecord webcam = getWebcam(webcamName);
        if (webcam.latestImage != null) {
            return webcam.latestImage.path();
        }
        return null;
    }

    public boolean writeImage(String webcamName, String extension, Instant timestamp, String hash, byte[] bytes) {
        final WebcamRecord webcam = getWebcam(webcamName);
        if (webcam.latestImage != null && webcam.latestImage.hash.equals(hash)) {
            return true;
        }

        final Path dir = Path.of(webcamCacheDir.toString(), webcam.webcamDir);
        dir.toFile().mkdirs();
        final Path filePath = Path.of(dir.toString(), filenameDateTimeFormatter.format(timestamp) + "." + extension);
        if (!fileWriter.write(filePath, bytes)) {
            return false;
        }

        webcam.latestImage = new WebcamRecord.WebcamImage(hash, timestamp, filePath);
        return true;
    }

    private WebcamRecord getWebcam(String name) {
        return webcams.computeIfAbsent(name, s -> {
            final WebcamRecord webcamRecord = new WebcamRecord();
            webcamRecord.webcamDir = name.replaceAll("[^a-zA-Z0-9]", "_");
            return webcamRecord;
        });
    }
}
