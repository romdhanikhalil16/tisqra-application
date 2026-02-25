FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file from local build
COPY services/event-service/target/*.jar app.jar

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
