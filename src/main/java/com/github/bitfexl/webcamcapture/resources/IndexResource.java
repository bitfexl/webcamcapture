package com.github.bitfexl.webcamcapture.resources;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import com.github.bitfexl.webcamcapture.config.WebcamSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/index")
public class IndexResource {
    public record WebcamIndex(List<IndexedWebcam> webcams) {
        public record IndexedWebcam(String name) { }
    }

    private WebcamIndex webcamIndex;

    @Inject
    void injectConfig(ApplicationConfig config) {
        final List<WebcamIndex.IndexedWebcam> webcams = new ArrayList<>();
        for (WebcamSource source : config.config().webcams()) {
            webcams.add(new WebcamIndex.IndexedWebcam(source.name()));
        }
        this.webcamIndex = new WebcamIndex(webcams);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WebcamIndex getWebCamIndex() {
        return webcamIndex;
    }
}
