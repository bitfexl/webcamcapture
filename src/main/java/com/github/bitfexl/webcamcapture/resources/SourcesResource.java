package com.github.bitfexl.webcamcapture.resources;

import com.github.bitfexl.webcamcapture.io.http.HTTPClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/sources")
public class SourcesResource {

    @Inject
    HTTPClient httpClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        httpClient.getBytes("https://segelfliegen-linz.org/wp-content/uploads/webcam/LOLO_Webcam_Signalfeld.jpg", Map.of());
        return "Hello from Quarkus REST";
    }
}
