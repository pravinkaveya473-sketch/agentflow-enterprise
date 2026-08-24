# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
ARG MODULE=services/core-service
COPY . .
RUN --mount=type=cache,target=/root/.m2/repository \
    chmod +x mvnw \
    && ./mvnw -q -pl ${MODULE} -am clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=5

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE=services/core-service
RUN mkdir -p /app/data/uploads \
    && chown -R 10001:0 /app
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
USER 10001
EXPOSE 8080 8081 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
