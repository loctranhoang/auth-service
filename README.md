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
```sh
git clone https://github.com/<your-org>/auth-service.git
cd auth-service
```

### Build the Project
```sh
mvn clean package
```

## Configuration
Configuration is managed using Spring Boot's YAML files located in `src/main/resources/`:
- `application.yml`: Base/default settings
- `application-local.yml`, `application-dev.yml`, `application-prod.yml`: Overrides for specific environments

To activate a profile, pass the profile name when starting:
```sh
mvn spring-boot:run -Dspring-boot.run.profiles=local
# or with the jar
java -jar target/auth-service-*.jar --spring.profiles.active=prod
```

Update database, security, and third-party settings in the respective YAML file according to your environment. See the YAML files' comments for details on configurable properties.

## Usage

The main RESTful API endpoints include:
- `POST /api/auth/register` — Register a user (returns confirmation or auth token)
- `POST /api/auth/login` — Login and receive a JWT for secured endpoints
- `GET /api/users/me` — Retrieve authenticated user's info (JWT in Authorization header required)

For protected endpoints, provide the `Authorization: Bearer <jwt>` header.

API documentation—if enabled—is available via:
- `/swagger-ui.html`
- `/v3/api-docs`

See the codebase for up-to-date endpoint specifics.

## Running Tests
Run all tests using:
```sh
mvn test
```
By default, tests use an embedded H2 database or profile-specific test configs. Override the active profile as needed:
```sh
mvn test -Dspring.profiles.active=test
```

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork and clone the repository
2. Create a feature branch: `git checkout -b feature/your-idea`
3. Commit your changes: `git commit -am 'Describe feature'`
4. Push your branch: `git push origin feature/your-idea`
5. Open a Pull Request for review

Before submitting, make sure all tests pass and your code matches the project's style.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Changelog & Updates
- **2026-06-04**: Documentation updated for installation, configuration, usage, and testing.
- See Git history or Releases for additional details and change history.

## Contact
To report issues or ask questions, please open a GitHub issue or contact a project maintainer. We welcome contributions, bug reports, and suggestions.
