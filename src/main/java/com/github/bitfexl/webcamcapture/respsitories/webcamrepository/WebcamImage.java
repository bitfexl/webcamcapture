package com.github.bitfexl.webcamcapture.respsitories.webcamrepository;

import java.nio.file.Path;
import java.time.Instant;

public record WebcamImage(String hash, Instant timestamp, Path path) {

}
