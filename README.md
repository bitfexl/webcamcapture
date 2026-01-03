# Webcam Capture

This server captures any webcam and saves its image periodically.

**Build**

```sh
podman build -t webcamcapture .
```

**Run**

```sh
podman run -it --rm -p 8080:8080 -v ./webcamcaptureconfig.json:/app/webcamcaptureconfig.json webcamcapture
```

**Config (webcamcapture.json)**

```
{
  // The cache dir for saved images inside the container, will be relative to /app.
  "cacheDir": "./webcam-save",
  "webcams": [
    // All webcams which should be saved.
    {
      // The name (for rest calls), must be unique.
      "name": "My Webcam",
      // The url of the webcams latest image.
      "url": "https://example.com/webcam/latest/image.jpg",
      // How often the image gets fetched (number + s, m, h for secnods, minutes, hours).
      "updateInterval": "1m",
      // How many images are saved at maximum.
      "maxCaptures": 500,
      // How often images are saved (not updated), so every 10 minutes (or more, depending on update)
      // an image is saved, all other images inbetween saves are deleted. Same format as update interval.
      "minSaveInterval": "10m",
      // Add a random query parameter, helps to avoid caches.
      "addRandom": true
    },
    // ... other webcams.
  ]
}
```

**Build and install (tested on fedora)**

Install command:

```sh
curl https://raw.githubusercontent.com/bitfexl/webcamcapture/refs/heads/master/installscript | sh
```
Afterward create `webcamcaptureconfig.json` and restart the service (`webcamcapture.service`).

Full script:

```sh
dnf install git podman podlet -y
git clone https://github.com/bitfexl/webcamcapture.git
cd ./webcamcapture/
podman build -t webcamcapture .
podlet -f -a -i podman run -p 8080:8080 -v ./webcamcaptureconfig.json:/app/webcamcaptureconfig.json -v webcam-save:/app/webcam-save/ webcamcapture
podman quadlet install -r --reload-systemd webcamcapture.container
systemctl start webcamcapture.service
```

**Rest Endpoints**

The dev server also includes a swagger ui at: http://localhost:8080/q/swagger-ui/

| Endpoint | Description                                                                                     |
|-|-------------------------------------------------------------------------------------------------|
| / | The server serves a simple html site as the index, which allows viewing all webcams.            |
|  /index | Json index of available webcams.                                                                |
| /webcam/{name}/latest | Get metadata and url for the latest image of the webcam (webcam name from /index, config file). |
| /webcam/{name}/{timestamp} | Get metadata and url for the image closest to the timestamp (iso 8601 timestamp). |
| /image/{partialPath} | Will be used by the url returned form the /webcam endpoints to get the image file. |

---

# Quarkus Readme below

---

# webcamcapture

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/webcamcapture-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
