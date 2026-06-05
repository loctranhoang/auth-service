# Auth Service

A Spring Boot-based authentication microservice responsible for handling user authentication, authorization, and token issuance in your microservices architecture.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Overview

The **Auth Service** provides secure user authentication workflows that can be used by other services. It’s designed to be extensible, robust, and production-ready, leveraging Java and Spring Boot for rapid development and scalability.

## Features
- RESTful authentication API endpoints
- Token issuance and validation (JWT support)
- Password hashing and user verification
- Extendable for role-based access control (RBAC)
- Easy integration with other microservices

## Installation

1. **Clone the Repository:**
   ```sh
   git clone https://github.com/your-org/auth-service.git
   cd auth-service
   ```

2. **Build the Project:**
   ```sh
   ./mvnw clean install
   ```
   Or with Maven installed globally:
   ```sh
   mvn clean install
   ```

3. **Run the Application:**
   ```sh
   ./mvnw spring-boot:run
   ```
   Or:
   ```sh
   java -jar target/auth-service-*.jar
   ```

## Usage

By default, the service starts on port `8080`. API documentation and endpoints are defined under `/api/auth` (see your `src/main/java/com/az/auth` for implementation details).

Example authentication request:
```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "your-user",
  "password": "your-pass"
}
```

Response:
```json
{
  "token": "<JWT_TOKEN_STRING>"
}
```

## Contributing

1. Fork this repository and create a feature branch.
2. Ensure your changes include tests as appropriate.
3. Submit a pull request with a clear explanation of your changes.

See [CONTRIBUTING.md](CONTRIBUTING.md) for full contributor guidelines.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
