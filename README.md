# Auth Service

This project is an authentication service built using Java and Spring Boot. It provides authentication, authorization, and user management capabilities for integrating secure access into your applications and services.

## Features

- User registration, login, and logout
- JWT-based authentication
- Role-based access control (RBAC)
- Password hashing
- REST API with Spring Boot
- Easily extensible security configuration

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+

### Building the Project

```
mvn clean install
```

### Running the Service

You can run the service locally using:

```
mvn spring-boot:run
```

Or build a runnable JAR:

```
mvn package
java -jar target/auth-service-*.jar
```

### API Usage

- The base URL for all endpoints is: `http://localhost:8080/`
- Example endpoints:
    - `POST /api/auth/register` - Register a new user
    - `POST /api/auth/login` - Authenticate and receive a JWT
    - `GET /api/user/me` - Get current user details (requires authentication)

API specifications and further documentation will be provided in `/docs` or via OpenAPI/Swagger in future releases.

## Configuration

Application configuration is managed via `application.yml` in `src/main/resources`. You can override settings for local development and deployments using Spring Boot profiles.

## Contribution Guidelines

1. Fork the repository and create your branch from `main`.
2. Use descriptive commit messages and reference issue keys when applicable.
3. Ensure all unit tests pass:
    ```
    mvn test
    ```
4. Submit a pull request for review.

Please refer to the project's [issue tracker](https://github.com/your-org/auth-service/issues) to report bugs or suggest features.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

*Maintained by the Azure Auth Team.*
