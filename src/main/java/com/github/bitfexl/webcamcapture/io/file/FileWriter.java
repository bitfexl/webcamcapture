package com.github.bitfexl.webcamcapture.io.file;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@ApplicationScoped
public class FileWriter {
    /**
     * Write a file.
     * @param path The path to write to, overwrites if existent.
     * @param contents The file contents to write.
     * @return true: written successfully, false: error writing file;
     */
    public boolean write(Path path, byte[] contents) {
        try {
            Files.write(path, contents, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
