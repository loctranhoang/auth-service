FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN ./mvnw -B clean package -DskipTests || mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
