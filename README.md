# Auth Service

A Spring Boot based authentication and authorization service for securing applications.

## Overview
This project provides token-based authentication and authorization, user management, and easy integration with other services.

## Features
- User registration and management
- JWT (JSON Web Token) authentication
- Role-based access control (RBAC)
- Secure password storage
- RESTful API

## Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.8+

### Building the Project
```
mvn clean package
```

### Running the Application
You can run the service using Maven:
```
mvn spring-boot:run
```
Or with the generated JAR file:
```
java -jar target/auth-service-*.jar
```

### Spring Profiles
The service uses profile-specific configurations. Default settings are in `application.yml`. Override for development, staging, or production by providing `--spring.profiles.active=<profile>`.

### API Endpoints
Common endpoints:
- `POST /api/auth/register` – Register a new user
- `POST /api/auth/login` – Authenticate and receive a JWT
- `GET /api/users/me` – Get current user info (requires authentication)

Refer to API documentation (e.g., Swagger/OpenAPI, if available) for full details.

## Configuration
Edit `src/main/resources/application.yml` or `application-<profile>.yml` for customizing database, security, and environment-specific values.

## Contributing
Contributions are welcome! Please:
- Fork the repo and create your branch (`git checkout -b feature/example`)
- Commit changes (`git commit -am 'Add new feature'`)
- Push to your branch (`git push origin feature/example`)
- Open a pull request

## License
Distributed under the MIT License. See `LICENSE` for details.

## Contact
For questions, open an issue or contact the maintainers.
