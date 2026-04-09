FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR(s) from local build.
# Spring Boot's repackage goal leaves a "*.jar.original" next to the runnable jar.
COPY services/event-service/target/*.jar /app/

# Keep only the runnable jar as /app/app.jar
RUN set -eux; \
    JAR="$(ls -1 /app/*.jar | grep -v '\\.jar\\.original$' | head -n 1)"; \
    mv "$JAR" /app/app.jar; \
    rm -f /app/*.jar.original

EXPOSE 8083

# Event service can take >1 minute to fully start (Flyway/JPA/Kafka).
# Use a longer start period and timeout to avoid false "unhealthy" during warmup.
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
