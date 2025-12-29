package com.github.bitfexl.webcamcapture.resources;

import com.github.bitfexl.webcamcapture.services.WebcamService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriInfo;

import java.time.Instant;

@Path("/webcam")
public class WebcamResource {
    public record WebcamImage(Instant timestamp, String url) { }

    @Inject
    WebcamService service;

    @GET
    @Path("/{name}/latest")
    public WebcamImage get(String name, UriInfo uriInfo) {
        return get(name, Instant.now(), uriInfo);
    }

    @GET
    @Path("/{name}/{timestamp}")
    public WebcamImage get(String name, Instant timestamp, UriInfo uriInfo) {
        final com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage image = service.getImage(name, timestamp);
        if (image == null) {
            throw new NotFoundException();
        }
        return new WebcamImage(image.timestamp(), uriInfo.getBaseUri().resolve("./image/" + service.getPartialPath(image)).toString());
    }
}