package com.github.bitfexl.webcamcapture.services;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.config.WebcamSource;
import com.github.bitfexl.webcamcapture.io.http.HTTPClient;
import com.github.bitfexl.webcamcapture.respsitories.WebcamRepository;
import com.github.bitfexl.webcamcapture.util.Hashing;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Startup
@ApplicationScoped
public class WebcamService {
    @Inject
    WebcamRepository webcamRepository;

    @Inject
    HTTPClient httpClient;

    @Inject
    Hashing hashing;

    @Inject
    JobSchedulerService jobSchedulerService;

    @Inject
    void injectConfig(ApplicationConfig config) {
        for (WebcamSource source : config.config().webcams()) {
            jobSchedulerService.addJob(source.updateInterval(), () -> {
                updateWebcam(source);
            });
        }
    }

    public void updateWebcam(WebcamSource webcamSource) {
        final byte[] bytes = httpClient.getBytes(webcamSource.url(), Map.of());
        final String hash = hashing.md5(bytes);
        webcamRepository.writeImage(webcamSource.name(), getExtension(webcamSource.url()), Instant.now(), hash, bytes);
    }

    private String getExtension(String url) {
        final String[] parts = URI.create(url).getPath().split("\\.");
        return parts[parts.length - 1];
    }
}
