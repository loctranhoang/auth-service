# Auth Service

## Overview
Auth Service is a Java-based microservice built with Spring Boot designed to manage authentication within a distributed system. It serves as the authentication backend, providing endpoints for user login, token generation, and validation.

## Features
- User authentication (login)
- JWT token issuance and validation
- RESTful API structure
- Configurable authentication providers (future scope)

## Requirements
- Java 17 or newer
- Maven 3.8+
- Spring Boot 3.x

## Getting Started

### 1. Clone the Repository
```
git clone https://github.com/<your-org>/auth-service.git
cd auth-service
```

### 2. Build the Project
```
mvn clean install
```

### 3. Run the Application
You can run the application using Maven:
```
mvn spring-boot:run
```
Or by running the built JAR:
```
java -jar target/auth-service-*.jar
```

The service will start on port `8080` by default. You can override this in `src/main/resources/application.yml`.

### 4. Configuration
- The main configuration file is located at `src/main/resources/application.yml`.
- To set environment variables (e.g., for secrets), define them before running:
  - `export SPRING_PROFILES_ACTIVE=dev`
  - `export JWT_SECRET=your_jwt_secret`

### 5. API Usage
API endpoints and usage instructions are documented with OpenAPI/Swagger (if enabled). By default, Swagger UI is available at `/swagger-ui.html`.

## Contribution Guidelines
Contributions, bug reports, and feature requests are welcome! To contribute:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature`
3. Commit your changes
4. Open a pull request

Ensure your code follows the existing style guidelines and is accompanied by relevant tests when applicable.

## License
This project is licensed under the MIT License.

---
Maintained by the AZ team.
