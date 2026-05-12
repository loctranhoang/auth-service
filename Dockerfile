FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/app/target/auth-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
