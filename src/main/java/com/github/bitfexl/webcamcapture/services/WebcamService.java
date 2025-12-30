package com.github.bitfexl.webcamcapture.services;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.config.WebcamSource;
import com.github.bitfexl.webcamcapture.io.http.HTTPClient;
import com.github.bitfexl.webcamcapture.respsitories.WebcamRepository;
import com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage;
import com.github.bitfexl.webcamcapture.util.Hashing;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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

    public Path resolvePartialPath(String path) {
        if (path.matches("[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+\\.[a-zA-Z]+")) {
            return webcamRepository.getWebcamsCacheDir().resolve(path);
        }
        return null;
    }

    public String getPartialPath(WebcamImage webcamImage) {
        return webcamImage.path().getParent().getFileName() + "/" + webcamImage.path().getFileName();
    }

    public WebcamImage getImage(String webcamName, Instant nearTimestamp) {
        final List<WebcamImage> images = webcamRepository.getImages(webcamName);
        if (images.isEmpty()) {
            return null;
        }
        return findClosest(images, nearTimestamp);
    }

    public void updateWebcam(WebcamSource webcamSource) {
        final byte[] bytes = httpClient.getBytes(webcamSource.url(), Map.of());
        final String hash = hashing.md5(bytes);
        webcamRepository.writeImage(webcamSource.name(), getExtension(webcamSource.url()), Instant.now(), hash, bytes);

        // remove older captures if max captures has been reached
        if (webcamSource.maxCaptures() != null) {
            final List<WebcamImage> images = webcamRepository.getImages(webcamSource.name());
            for (int i = 0; images.size() - i > webcamSource.maxCaptures(); i++) {
                webcamRepository.removeImage(webcamSource.name(), images.get(i));
            }
        }
    }

    private String getExtension(String url) {
        final String[] parts = URI.create(url).getPath().split("\\.");
        return parts[parts.length - 1];
    }

    private WebcamImage findClosest(List<WebcamImage> images, Instant timestamp) {
        int left = 0;
        int right = images.size() - 1;

        while (left <= right) {
            final int mid = left + (right - left) / 2;
            final Instant midValue = images.get(mid).timestamp();

            if (midValue.equals(timestamp)) {
                return images.get(mid);
            } else if (midValue.isBefore(timestamp)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (right < 0 || right >= images.size()) {
            return images.get(left);
        }
        // left never < 0 because while left <= right and previous if right < 0 returns
        if (/*left < 0 ||*/ left >= images.size()) {
            return images.get(right);
        }

        final Instant a = images.get(left).timestamp();
        final Instant b = images.get(right).timestamp();
        final int result = Duration.between(a, timestamp).compareTo(Duration.between(b, timestamp));
        if (result <= 0) {
            return images.get(left);
        } else {
            return images.get(right);
        }
    }
}
