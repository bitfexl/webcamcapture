package com.github.bitfexl.webcamcapture.services;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;

@ApplicationScoped
public class URLService {

    private URI configuredBaseUrl;

    @Inject
    void injectConfig(ApplicationConfig config) {
        final String baseUrl = config.config().baseURL();
        if (baseUrl != null && !baseUrl.isBlank()) {
            try {
                configuredBaseUrl = URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
            } catch (Exception ex) {
                Log.error("Invalid config: invalid url (" + baseUrl + ").", ex);
            }
        }
    }

    public URI resolve(String path, URI fallbackBase) {
        if (configuredBaseUrl != null) {
            return configuredBaseUrl.resolve(path);
        } else {
            return fallbackBase.resolve(path);
        }
    }
}
