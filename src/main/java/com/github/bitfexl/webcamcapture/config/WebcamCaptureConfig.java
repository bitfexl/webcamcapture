package com.github.bitfexl.webcamcapture.config;

import java.util.List;

public record WebcamCaptureConfig(
        String cacheDir,
        List<WebcamSource> webcams
) {
}
