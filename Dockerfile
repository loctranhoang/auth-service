FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN ./mvnw clean package -DskipTests

# --- Runtime Image ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
