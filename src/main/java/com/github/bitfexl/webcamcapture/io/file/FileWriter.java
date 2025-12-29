package com.github.bitfexl.webcamcapture.io.file;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@ApplicationScoped
public class FileWriter {
    /**
     * Write a file, also writes a new line (\n).
     * @param path The path to write to, overwrites if existent.
     * @param s The string to write (utf8 encoded).
     * @return true: written successfully, false: error writing file;
     */
    public boolean writeLine(Path path, String s) {
        return write(path, (s + "\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write a file.
     * @param path The path to write to, overwrites if existent.
     * @param s The string to write (utf8 encoded).
     * @return true: written successfully, false: error writing file;
     */
    public boolean write(Path path, String s) {
        return write(path, s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write a file.
     * @param path The path to write to, overwrites if existent.
     * @param contents The file contents to write.
     * @return true: written successfully, false: error writing file;
     */
    public boolean write(Path path, byte[] contents) {
        try {
            path.getParent().toFile().mkdirs();
            Files.write(path, contents, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Append a line to a file, does also append a new line (\n).
     * @param path The file to append to.
     * @param s The string to write (utf8 encoded).
     * @return true: written successfully, false: error writing file;
     */
    public boolean appendLine(Path path, String s) {
        return append(path, (s + "\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Append a line to a file, does not append a new line.
     * @param path The file to append to.
     * @param s The string to write (utf8 encoded).
     * @return true: written successfully, false: error writing file;
     */
    public boolean append(Path path, String s) {
        return append(path, s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Append to a file. Only appends the provided contents.
     * @param path The path to append to.
     * @param contents The file contents to write.
     * @return true: written successfully, false: error writing file;
     */
    public boolean append(Path path, byte[] contents) {
        try {
            path.getParent().toFile().mkdirs();
            Files.write(path, contents, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
