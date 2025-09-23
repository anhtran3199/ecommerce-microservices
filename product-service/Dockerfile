FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]