FROM eclipse-temurin:21-jdk-alpine AS builder
# Set working directory
WORKDIR /build

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Package the application
RUN ./mvnw -B -DskipTests=true package || mvn -B -DskipTests=true package

# Copy artifacts to a minimal run image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the jar produced by the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port 8080 by default (Spring Boot default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
