# Auth Service

A Spring Boot-based authentication microservice responsible for handling user authentication, authorization, and token issuance within a microservices architecture.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Building and Running](#building-and-running)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)

## Overview

The **Auth Service** provides secure user authentication and authorization workflows that can be easily integrated by other services. It is designed for extensibility, robustness, and production-readiness, leveraging Java and Spring Boot for rapid development and scalability.

## Features
- RESTful authentication API endpoints
- Token issuance and validation (supports JWT)
- Secure password hashing and user verification
- Extensible for role-based access control (RBAC)
- Ready for seamless integration with other microservices

## Installation

To set up the service locally:

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

By default, the service starts on port `8080`. API endpoints are available under `/api/auth`.

**Example authentication request:**
```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "your-user",
  "password": "your-pass"
}
```

**Example response:**
```json
{
  "token": "<JWT_TOKEN_STRING>"
}
```

For implementation details, see: `src/main/java/com/az/auth`.

## Configuration

The service uses Spring Boot's [application.yml](src/main/resources/application.yml) (and optionally profile-specific configuration files such as `application-dev.yml`) for all customizable settings. You can override configuration properties using environment variables or via the command line:

- **Port Configuration:**
  ```sh
  java -jar target/auth-service-*.jar --server.port=9090
  ```
- **Profiles:**
  ```sh
  java -jar target/auth-service-*.jar --spring.profiles.active=dev
  ```
- **JWT Settings and User Management:**
  Update relevant sections in `application.yml` to configure JWT secrets, token expiration, user data sources, and more.

Refer to the in-file comments in each configuration file for explanations and options.

## Building and Running

- **Start the application** using Maven Wrapper:
  ```sh
  ./mvnw spring-boot:run
  ```
  Or by running the built JAR:
  ```sh
  java -jar target/auth-service-*.jar
  ```

- **Profiles:** The service can be configured with different Spring Boot profiles using the `--spring.profiles.active` flag.

## Contributing

1. Fork this repository and create a feature branch.
2. Write clear, tested code and update/add documentation where necessary.
3. Open a pull request describing your changes.

See [CONTRIBUTING.md](CONTRIBUTING.md) for contributor guidelines.

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Issue Tracker](https://github.com/your-org/auth-service/issues)
- [API Docs](/docs) (if enabled)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
