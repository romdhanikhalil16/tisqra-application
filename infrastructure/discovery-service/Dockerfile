FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file from local build
COPY infrastructure/discovery-service/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
