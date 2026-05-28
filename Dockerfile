### --- AUTH-SERVICE Dockerfile ---

# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache maven

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the packaged jar from the build stage
COPY --from=build /build/target/*.jar app.jar

# Expose the default Spring Boot port (for local dev convenience)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]