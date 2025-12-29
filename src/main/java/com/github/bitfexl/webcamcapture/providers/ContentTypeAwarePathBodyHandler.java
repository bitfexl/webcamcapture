package com.github.bitfexl.webcamcapture.providers;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.common.providers.serialisers.PathBodyHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Provider
public class ContentTypeAwarePathBodyHandler extends PathBodyHandler {
    private final Map<String, String> mediaTypes;

    {
        mediaTypes = new HashMap<>();

        try {
            String s = new String(Objects.requireNonNull(ContentTypeAwarePathBodyHandler.class.getResourceAsStream("/mime.types")).readAllBytes(), StandardCharsets.UTF_8);
            s = s.replaceAll("[\\n\\r]", "");
            final int startI = s.indexOf("{", s.indexOf("types"));
            final int endI = s.indexOf("}", startI);
            s = s.substring(startI + 1, endI);
            for (String def : s.split(";")) {
                final String[] parts = def.split(" ");
                String mediaType = null;
                for (String part : parts) {
                    if (part.isEmpty()) {
                        continue;
                    }
                    if (mediaType == null) {
                        mediaType = part;
                    } else {
                        mediaTypes.put(part.toLowerCase(), mediaType);
                    }
                }
            }
        } catch (IOException ex) {
            Log.error("Error reading mime type definitions.", ex);
        }
    }

    @Override
    public void writeTo(Path uploadFile, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        final String[] parts = uploadFile.toString().split("\\.");
        final String extension = parts[parts.length - 1];
        httpHeaders.put(HttpHeaders.CONTENT_TYPE, getMediaType(extension));
        super.writeTo(uploadFile, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    private List<Object> getMediaType(String extension) {
        final String mediaType = mediaTypes.get(extension.toLowerCase());
        if (mediaType == null) {
            return List.of();
        }
        return List.of(mediaType);
    }
}
