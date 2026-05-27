FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy Maven build descriptor and source code
COPY pom.xml ./
COPY src ./src

# Build the application (requires maven to be installed in the container)
RUN apk add --no-cache maven && \
    mvn package -DskipTests && \
    cp target/*.jar app.jar && \
    rm -rf /root/.m2 && \
    apk del maven

# Expose typical Spring Boot port (optional, can adjust)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
