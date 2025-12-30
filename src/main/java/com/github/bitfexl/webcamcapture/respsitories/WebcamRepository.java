package com.github.bitfexl.webcamcapture.respsitories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.config.WebcamSource;
import com.github.bitfexl.webcamcapture.io.file.FileReader;
import com.github.bitfexl.webcamcapture.io.file.FileWriter;
import com.github.bitfexl.webcamcapture.resources.ImageResource;
import com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamCacheDir;
import com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class WebcamRepository {
    private static final String CACHE_CSV_HEADER = "name,timestamp,hash";
    @Inject
    ImageResource imageResource;

    /**
     * Webcam index file. Contains the webcam names and cache dirs.
     * Each cache dir contains an index csv file with: name, timestamp, hash of the files.
     * @param index
     */
    private record WebcamCacheIndex(List<WebcamIndexEntry> index) {
        public record WebcamIndexEntry(String name, String dir) { }
    }

    private final DateTimeFormatter filenameDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneOffset.UTC);

    @Inject
    FileWriter fileWriter;

    @Inject
    FileReader fileReader;

    private Path webcamsCacheDir;

    public Path getWebcamsCacheDir() {
        return webcamsCacheDir;
    }

    private final Map<String, WebcamCacheDir> webcams = new HashMap<>();

    private final Map<WebcamCacheDir, List<WebcamImage>> webcamImages = new HashMap<>();

    @Inject
    synchronized void injectConfig(ApplicationConfig config) {
        // inject config and read index

        this.webcamsCacheDir = Path.of(config.config().cacheDir());

        final ObjectMapper mapper = new ObjectMapper();

        // read previous json index file

        WebcamCacheIndex cacheIndex = null;

        final Path jsonIndex = webcamsCacheDir.resolve("index.json");

        if (jsonIndex.toFile().exists()) {
            final String json = fileReader.readFileUtf8(jsonIndex);
            if (json != null) {
                try {
                    cacheIndex = mapper.readValue(json, WebcamCacheIndex.class);
                } catch (Exception ignored) { }
            }
        }

        final Map<String, WebcamCacheIndex.WebcamIndexEntry> indexEntryMap = new HashMap<>();
        if (cacheIndex != null) {
            cacheIndex.index().forEach(entry -> indexEntryMap.put(entry.name(), entry));
        }

        // read/create source index file

        for (WebcamSource source : config.config().webcams()) {
            final WebcamCacheIndex.WebcamIndexEntry indexEntry = indexEntryMap.get(source.name());

            final Path sourceCacheDir = webcamsCacheDir.resolve(indexEntry != null ? indexEntry.dir() : source.name().replaceAll("[^a-zA-Z0-9]", "_") +  "-" + UUID.randomUUID());
            final Path indexFile = sourceCacheDir.resolve("index.csv");

            final WebcamCacheDir webcamCacheDir = new WebcamCacheDir(source.name(), sourceCacheDir);
            final List<WebcamImage> images = new ArrayList<>();
            webcams.put(source.name(), webcamCacheDir);
            webcamImages.put(webcamCacheDir, images);

            // read or create index file
            if (indexEntry != null) {
                final String[] rawImages = fileReader.readFileUtf8(indexFile).split("\n");
                for (int i = 1; i < rawImages.length; i++) {
                    final String[] parts = rawImages[i].split(",");
                    images.add(new WebcamImage(parts[2], Instant.parse(parts[1]), sourceCacheDir.resolve(parts[0])));
                }
            } else {
                fileWriter.writeLine(indexFile, CACHE_CSV_HEADER);
            }
        }

        // create new index file

        final List<WebcamCacheIndex.WebcamIndexEntry> indexEntries = webcams.values().stream()
                .map(wcd-> new WebcamCacheIndex.WebcamIndexEntry(
                        wcd.name(),
                        wcd.dir().getFileName().toString()
                )).toList();
        final WebcamCacheIndex webcamCacheIndex = new WebcamCacheIndex(indexEntries);
        try {
            fileWriter.write(jsonIndex, mapper.writeValueAsString(webcamCacheIndex));
        } catch (Exception ex) {
            Log.error("Error writing index.", ex);
        }
    }

    public List<WebcamImage> getImages(String webcamName) {
        final WebcamCacheDir webcam = webcams.get(webcamName);
        if (webcam == null) {
            return null;
        }
        return webcamImages.get(webcam);
    }

    /**
     * Write an image.
     * @param webcamName The webcam to write the image for.
     * @param extension The file extension.
     * @param timestamp The timestamp at which the image was fetched, should be now, calls can only increment the timestamp.
     * @param hash The hash of the image, it will not be written if the last image was the exact same.
     * @param bytes The image contents.
     * @return The written image or null if an error occurred.
     */
    public synchronized WebcamImage writeImage(String webcamName, String extension, Instant timestamp, String hash, byte[] bytes) {
        final WebcamCacheDir webcam = webcams.get(webcamName);
        if (webcam == null) {
            return null;
        }

        final List<WebcamImage> previousImages = getImages(webcamName);
        if (previousImages != null && !previousImages.isEmpty()) {
            final WebcamImage lastImage = previousImages.getLast();
            if (lastImage.hash().equals(hash)) {
                return lastImage;
            }
        }

        final Path filePath = webcam.dir().resolve(filenameDateTimeFormatter.format(timestamp) + "." + extension);
        if (!fileWriter.write(filePath, bytes)) {
            return null;
        }

        final WebcamImage image = new WebcamImage(hash, timestamp, filePath);
        webcamImages.get(webcam).add(image);
        appendToIndex(webcam.dir(), image);
        return image;
    }

    private synchronized void appendToIndex(Path sourceCacheDir, WebcamImage image) {
        fileWriter.appendLine(sourceCacheDir.resolve("index.csv"), image.path().getFileName() + "," + image.timestamp() + "," + image.hash());
    }
}
