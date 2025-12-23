package com.github.bitfexl.webcamcapture.io.http;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@ApplicationScoped
public class HTTPClient {
    private final HttpClient httpClient = HttpClient.newBuilder()/*.proxy(ProxySelector.of(new InetSocketAddress("localhost", 1234)))*/.build();

    /**
     * Send a get request. This method may throw an error.
     * @param url The url to get.
     * @param headers The headers for the request or an empty map.
     * @return The response bytes.
     */
    public byte[] getBytes(String url, Map<String, String> headers) {
        if (!headers.isEmpty()) {
            return send(HttpRequest
                    .newBuilder()
                    .uri(URI.create(url))
                    .headers(getHeadersArray(headers))
                    .build()
            );
        }
        return send(HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .build()
        );
    }

    private byte[] send(HttpRequest request) {
        final HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        try (final InputStream in  = response.body()) {
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new RuntimeException("Invalid http response code.");
            }
            return in.readAllBytes();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String[] getHeadersArray(Map<String, String> headers) {
        final String[] headersArray = new String[headers.size() * 2];
        int i = 0;
        for (String header : headers.keySet()) {
            headersArray[i++] = header;
            headersArray[i++] = headers.get(header);
        }
        return headersArray;
    }
}
