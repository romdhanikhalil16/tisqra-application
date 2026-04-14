FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget

# Copy the pre-built JAR(s) from local build.
# Spring Boot's repackage goal leaves a "*.jar.original" next to the runnable jar.
COPY services/order-service/target/*.jar /app/

# Keep only the runnable jar as /app/app.jar
RUN set -eux; \
    JAR="$(ls -1 /app/*.jar | grep -v '\\.jar\\.original$' | head -n 1)"; \
    mv "$JAR" /app/app.jar; \
    rm -f /app/*.jar.original

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
