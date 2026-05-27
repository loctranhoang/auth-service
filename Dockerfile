FROM eclipse-temurin:21-jdk-alpine

# Build the application and create the target directory if not exists
RUN mkdir -p /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

WORKDIR /app

ENTRYPOINT ["java", "-jar", "app.jar"]
