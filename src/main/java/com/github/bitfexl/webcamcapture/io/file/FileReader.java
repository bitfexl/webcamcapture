package com.github.bitfexl.webcamcapture.io.file;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class FileReader {
    /**
     * Read a file as an utf8 encoded string.
     * @param path The file to read.
     * @return The file contents or null if an error occurred.
     */
    public String readFileUtf8(Path path) {
        final byte[] file = readFile(path);
        if (file == null) {
            return null;
        }
        return new String(file, StandardCharsets.UTF_8);
    }

    /**
     * Read a file.
     * @param path The file to read.
     * @return The file contents or null if an error occurred.
     */
    public byte[] readFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            return null;
        }
    }
}
