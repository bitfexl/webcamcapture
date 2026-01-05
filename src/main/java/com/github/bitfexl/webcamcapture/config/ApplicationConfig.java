package com.github.bitfexl.webcamcapture.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bitfexl.webcamcapture.io.file.FileReader;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;

/**
 * Holds (reads and validates) the application config.
 */
@ApplicationScoped
public class ApplicationConfig {
    private static String configFile;

    public static void setConfigFile(String path) {
        configFile = path;
    }

    private WebcamCaptureConfig config;

    @Inject
    ConfigValidator validator;

    @Inject
    FileReader fileReader;

    /**
     * Reads or gets the config, validates config on first read.
     * @return The application config.
     */
    public WebcamCaptureConfig config() {
        if (this.config == null) {
            final WebcamCaptureConfig config = readConfig();

            if (config == null) {
                Log.fatal("Error reading config.");
            } else if (!validator.validate(config)) {
                Log.fatal("Error validating config.");
            } else {
                this.config = config;
            }
        }

        return this.config;
    }

    private WebcamCaptureConfig readConfig() {
        final String file = fileReader.readFileUtf8(Path.of(configFile));
        if (file == null) {
            return null;
        }

        try {
            return new ObjectMapper().readValue(file, WebcamCaptureConfig.class);
        } catch (JsonProcessingException ex) {
            Log.error("Invalid json config.", ex);
            return null;
        }
    }
}
