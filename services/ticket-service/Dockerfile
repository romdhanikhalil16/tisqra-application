FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file from local build
COPY services/ticket-service/target/*.jar app.jar

EXPOSE 8085

# Ticket service can take >1 minute to fully start (Flyway/JPA/Kafka).
# Use a longer start period and timeout to avoid false "unhealthy" during warmup.
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

