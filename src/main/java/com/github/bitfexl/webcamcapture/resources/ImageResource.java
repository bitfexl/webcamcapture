package com.github.bitfexl.webcamcapture.resources;

import com.github.bitfexl.webcamcapture.services.WebcamService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("/image")
public class ImageResource {
    @Inject
    WebcamService service;

    @Path("/{partialPath:.+}")
    @GET
    public java.nio.file.Path getImage(String partialPath) {
        final java.nio.file.Path path = service.resolvePartialPath(partialPath);
        if (path != null) {
            return path;
        }
        throw new WebApplicationException(400);
    }
}
