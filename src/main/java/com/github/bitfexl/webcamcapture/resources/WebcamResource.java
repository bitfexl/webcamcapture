package com.github.bitfexl.webcamcapture.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriInfo;

import java.time.Instant;

@Path("/webcam")
public class WebcamResource {
    public record WebcamImage(Instant timestamp, String url) { }

    @GET
    @Path("/{name}/latest")
    public WebcamImage get(String name, UriInfo uriInfo) {
        return get(name, Instant.now(), uriInfo);
    }

    @GET
    @Path("/{name}/{timestamp}")
    public WebcamImage get(String name, Instant timestamp, UriInfo uriInfo) {
        final String imageName = /* TODO: get */ name + timestamp;
        new Exception().printStackTrace();
        return new WebcamImage(timestamp, uriInfo.getBaseUri().resolve("./image/" + imageName).toString());
    }
}