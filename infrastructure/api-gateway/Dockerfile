FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file from local build
COPY infrastructure/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
