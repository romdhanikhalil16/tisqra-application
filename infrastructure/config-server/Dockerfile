FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR(s) from local build.
# Spring Boot's repackage goal leaves a "*.jar.original" next to the runnable jar.
COPY infrastructure/config-server/target/*.jar /app/

# Keep only the runnable jar as /app/app.jar
RUN set -eux; \
    JAR="$(ls -1 /app/*.jar | grep -v '\\.jar\\.original$' | head -n 1)"; \
    mv "$JAR" /app/app.jar; \
    rm -f /app/*.jar.original

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
