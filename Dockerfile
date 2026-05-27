FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN ./mvnw -B -ntp package -DskipTests || mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]