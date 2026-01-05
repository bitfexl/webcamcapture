package com.github.bitfexl.webcamcapture.resources;

import com.github.bitfexl.webcamcapture.services.URLService;
import com.github.bitfexl.webcamcapture.services.WebcamService;
import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Path("/webcam")
public class WebcamResource {
    public record WebcamImage(Instant timestamp, String url) { }

    @Inject
    WebcamService service;

    @Inject
    URLService urlService;

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
        return convertWebcamImage(image, uriInfo.getBaseUri());
    }

    @GET
    @Path("/{name}/latest/{count}")
    public List<WebcamImage> get(String name, int count, UriInfo uriInfo) {
        return get(name, Instant.now(), count, uriInfo);
    }

    @GET
    @Path("/{name}/{timestamp}/{count}")
    public List<WebcamImage> get(String name, Instant timestamp, int count, UriInfo uriInfo) {
        if (count < 1) {
            throw new ClientErrorException(400);
        }
        final List<com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage> images = service.getImages(name, timestamp, count);
        if (images.isEmpty()) {
            throw new NotFoundException();
        }
        final URI baseUri = uriInfo.getBaseUri();
        return images.stream().map(image -> convertWebcamImage(image, baseUri)).toList();
    }

    private WebcamImage convertWebcamImage(com.github.bitfexl.webcamcapture.respsitories.webcamrepository.WebcamImage image, URI baseUri) {
        return new WebcamImage(image.timestamp(), urlService.resolve("./image/" + service.getPartialPath(image), baseUri).toString());
    }
}