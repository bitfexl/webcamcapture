package com.github.bitfexl.webcamcapture.services;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.config.WebcamSource;
import com.github.bitfexl.webcamcapture.io.http.HTTPClient;
import com.github.bitfexl.webcamcapture.respsitories.WebcamRepository;
import com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage;
import com.github.bitfexl.webcamcapture.util.DurationParser;
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
import java.util.UUID;

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
            // schedule updates
            jobSchedulerService.addJob(source.updateInterval(), () -> {
                updateWebcam(source);
            });

            // schedule cleanups
            if (source.minSaveInterval() != null) {
                jobSchedulerService.addJob(source.minSaveInterval(), () -> {
                    cleanupWebcamImages(source);
                });
            }
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

    public List<WebcamImage> getImages(String webcamName, Instant nearTimestamp, int count) {
        final List<WebcamImage> images = webcamRepository.getImages(webcamName);
        if (images.isEmpty()) {
            return List.of();
        }
        final int toIndex = findClosestIndex(images, nearTimestamp) + 1;
        return images.subList(Math.max(0, toIndex - count), toIndex);
    }

    /**
     * Update the webcam (save the image). Respects max captures and deletes images if necessary.
     * @param webcamSource The source to update.
     */
    public void updateWebcam(WebcamSource webcamSource) {
        final String url = webcamSource.addRandom() ? webcamSource.url() + "?" + UUID.randomUUID() : webcamSource.url();
        final byte[] bytes = httpClient.getBytes(url, Map.of("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.3"));
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

    /**
     * Save cleanups: after the min save interval, from the beginning,
     * delete all images within this minimum interval,
     * start again from the next saved image, delete all images in the intervals but the very last (most recent).
     * @param webcamSource The source to clean up for.
     */
    public void cleanupWebcamImages(WebcamSource webcamSource) {
        if (webcamSource.minSaveInterval() == null) {
            return;
        }

        final List<WebcamImage> images = webcamRepository.getImages(webcamSource.name());

        // first and last are always kept
        if (images.size() < 3) {
            return;
        }

        final long ms = DurationParser.parseDuration(webcamSource.minSaveInterval());

        Instant last = images.getFirst().timestamp();

        for (int i = 1; i < images.size() - 1; i++) {
            final WebcamImage image = images.get(i);
            if (Duration.between(last, image.timestamp()).toMillis() < ms) {
                webcamRepository.removeImage(webcamSource.name(), image);
            } else {
                last = image.timestamp();
            }
        }
    }

    private String getExtension(String url) {
        final String[] parts = URI.create(url).getPath().split("\\.");
        return parts[parts.length - 1];
    }

    private WebcamImage findClosest(List<WebcamImage> images, Instant timestamp) {
        return images.get(findClosestIndex(images, timestamp));
    }

    private int findClosestIndex(List<WebcamImage> images, Instant timestamp) {
        int left = 0;
        int right = images.size() - 1;

        while (left <= right) {
            final int mid = left + (right - left) / 2;
            final Instant midValue = images.get(mid).timestamp();

            if (midValue.equals(timestamp)) {
                return mid;
            } else if (midValue.isBefore(timestamp)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (right < 0 || right >= images.size()) {
            return left;
        }
        // left never < 0 because while left <= right and previous if right < 0 returns
        if (/*left < 0 ||*/ left >= images.size()) {
            return right;
        }

        final Instant a = images.get(left).timestamp();
        final Instant b = images.get(right).timestamp();
        final int result = Duration.between(a, timestamp).compareTo(Duration.between(b, timestamp));
        if (result <= 0) {
            return left;
        } else {
            return right;
        }
    }
}
