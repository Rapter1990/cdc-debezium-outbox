# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Copy Maven files for dependency resolution
COPY pom.xml ./
COPY .mvn .mvn

# Copy application source code
COPY src src

# Build the project and create the executable JAR
RUN mvn clean install -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR notification-service

# Copy the JAR file from the build stage
COPY --from=build target/*.jar notification-service.jar

# Expose port 3435
EXPOSE 3435

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "-jar", "notification-service.jar"]