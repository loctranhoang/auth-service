# Auth Service

A Spring Boot-based authentication and authorization microservice designed to secure applications via modern, stateless protocols.

## Overview
Auth Service provides robust authentication, authorization, and user management for Java/Spring Boot ecosystems. Main features include:
- RESTful endpoints for user registration, login, and profile management
- JWT (JSON Web Token) authentication
- Role-Based Access Control (RBAC)
- Secure password hashing
- Extensibility for integration with other microservices

## Features
- User registration and lifecycle management
- Stateless JWT issuance & verification
- RBAC with configurable roles and permissions
- Secure REST APIs for authentication and authorization
- Profile-based, environment-specific configuration

## Installation

### Prerequisites
- Java 17 or newer
- Maven 3.8+

### Clone the Repository
```
git clone https://github.com/<your-org>/auth-service.git
cd auth-service
```

### Build the Project
```
mvn clean package
```

## Configuration
Configuration is managed using Spring Boot's YAML files located in `src/main/resources/`:
- `application.yml`: Base/default settings
- `application-local.yml`, `application-dev.yml`, `application-prod.yml`: Override for specific environments

To activate a profile, pass the profile name when starting:
```
mvn spring-boot:run -Dspring-boot.run.profiles=local
# or with the jar
java -jar target/auth-service-*.jar --spring.profiles.active=prod
```

Update database, security, and third-party settings in the respective YAML file according to your environment. Refer to code and comments in the YAML files for details on configurable properties.

## Usage

The main RESTful API endpoints include:
- `POST /api/auth/register` — Register a user (returns confirmation or auth token)
- `POST /api/auth/login` — Login and receive a JWT for secured endpoints
- `GET /api/users/me` — Retrieve authenticated user's info (JWT in Authorization header required)

API requests must use the `Authorization: Bearer <jwt>` header for protected endpoints.

> For full API documentation, consult Swagger/OpenAPI at `/swagger-ui.html` or `/v3/api-docs` (if enabled), or refer to the codebase for endpoint specifics.

## Running Tests
Execute all tests with:
```
mvn test
```
By default, tests use Embedded H2 or profile-specific test configurations; override via `-Dspring.profiles.active=test` as needed.

## Contributing
Contributions are appreciated! Please:
1. Fork and clone the repository
2. Create a feature branch: `git checkout -b feature/your-idea`
3. Commit your changes: `git commit -am 'Describe feature'`
4. Push your branch: `git push origin feature/your-idea`
5. Open a Pull Request for review

Before submitting, ensure all tests pass and code adheres to the existing style/conventions.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Changelog & Updates
- **2026-06-04**: Updated documentation for configuration, testing, and usage.
- View Git history or releases for previous changes and version notes.

## Contact
For support or questions, please open an issue in this repository or contact a maintainer. Contributions, issues, and queries are always welcome.
