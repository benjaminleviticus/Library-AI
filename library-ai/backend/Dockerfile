# Stage 1: Build the Spring Boot JAR
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# Copy Gradle wrapper and files
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Copy Gradle build files and download dependencies
COPY build.gradle settings.gradle ./
RUN ./gradlew build -x test --no-daemon || echo "Dependencies cached"

# Copy source code and build JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Lightweight production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8081

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
