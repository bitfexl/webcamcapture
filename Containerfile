FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY --chmod=777 mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline
RUN ./mvnw quarkus:go-offline

COPY . .
COPY --chmod=777 mvnw .
RUN ./mvnw package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/quarkus-app .
CMD ["java", "-jar", "./quarkus-app/quarkus-run.jar"]