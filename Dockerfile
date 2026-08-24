FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
ARG MODULE=services/core-service
COPY . .
RUN chmod +x mvnw && ./mvnw -q -pl ${MODULE} -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE=services/core-service
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 appuser \
    && mkdir -p /app/data/uploads \
    && chown -R appuser:appuser /app
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
USER appuser
EXPOSE 8080 8081 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
