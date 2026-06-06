# Auth Service

A Spring Boot-based authentication microservice responsible for handling user authentication, authorization, and token issuance in a microservices architecture.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Building and Running](#building-and-running)
- [Testing](#testing)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)

## Overview

The **Auth Service** delivers secure and extensible authentication and authorization APIs for other services in your ecosystem. Built with Java and Spring Boot, it emphasizes reliability, scalability, and rapid integration.

## Features
- RESTful endpoints for authentication and authorization
- JWT token issuance and validation
- Secure password hashing and user credential management
- Designed for role-based access control (RBAC) extensions
- Easy integration with additional microservices

## Installation

To install and begin using the Auth Service locally:

1. **Clone the repository**
   ```sh
   git clone https://github.com/your-org/auth-service.git
   cd auth-service
   ```

2. **Build the project**
   - With Maven Wrapper:
     ```sh
     ./mvnw clean install
     ```
   - Or with Maven installed globally:
     ```sh
     mvn clean install
     ```

## Usage

The Auth Service launches by default on port `8080`. API endpoints are under `/api/auth`.

**Authentication Request Example:**
```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "your-user",
  "password": "your-pass"
}
```

**Sample Response:**
```json
{
  "token": "<JWT_TOKEN_STRING>"
}
```

Access the implementation in: `src/main/java/com/az/auth`.

## Configuration

Customize the service via Spring Boot's [`application.yml`](src/main/resources/application.yml) and optional profile files (e.g., `application-dev.yml`). Properties can be overridden using environment variables or the command line.

**Key Configurations:**
- **Port:**
  ```sh
  java -jar target/auth-service-*.jar --server.port=9090
  ```
- **Profile Selection:**
  ```sh
  java -jar target/auth-service-*.jar --spring.profiles.active=dev
  ```
- **JWT & User Settings:** Modify `application.yml` for JWT secret, token expiration, and user data source configuration.

Refer to comments within the `application.yml` files for further details on each setting.

## Building and Running

Start the Auth Service using one of the following methods:

- **Using Maven Wrapper:**
  ```sh
  ./mvnw spring-boot:run
  ```
- **Using Built JAR:**
  ```sh
  java -jar target/auth-service-*.jar
  ```

Select Spring Boot profiles using `--spring.profiles.active` if needed.

## Testing

Run the test suite to verify code and service functionality:

```sh
./mvnw test
```
Or if Maven is installed globally:
```sh
mvn test
```

Tests (unit and integration) are under `src/test/java/com/az/auth`.

## Contributing

We welcome contributions! To contribute:

1. Fork the repository and create a new branch for your feature/fix.
2. Write clear, well-tested code, and update documentation as needed.
3. Submit a pull request with a description of your changes.

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Issue Tracker](https://github.com/your-org/auth-service/issues)
- [API Docs](/docs) (if available)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Update Rationale (AUTH-245)

- Ensured purpose, usage, and configuration sections reflect the current project structure and features.
- Clarified installation and running instructions to cover common developer workflows.
- Revised contributing section for clarity and added explicit procedural instructions.
- Added detail on configuration options, emphasizing customization and use of Spring profiles.
- Provided an update rationale section to document the changes for traceability as required by the acceptance criteria.

**Review is recommended to ensure accuracy with current code and environment settings.**
