# Auth Service

A robust authentication and authorization service built with Java and Spring Boot. Auth Service manages secure user authentication, registration, and role-based authorization for modern applications. It uses JWT (JSON Web Token) for stateless, token-based security, and supports extensible provider and environment-based configuration.

## Features

- Secure user registration and login endpoints
- JWT-based authentication and authorization
- Role- and permission-based access control
- Easily integrates with microservices and other systems
- Extensible architecture for adding additional authentication providers
- Environment-based configuration via profiles

## Compatibility

- **Java Version:** 17 or higher
- **Spring Boot:** 3.x compatible

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker (optional, for containerized deployment)

### Installation and Setup

1. **Clone the repository:**

    ```bash
    git clone <repository-url>
    cd auth-service
    ```

2. **Build the project:**

    ```bash
    mvn clean package
    ```

3. **Run the service:**

    ```bash
    mvn spring-boot:run
    # or
    java -jar target/auth-service-*.jar
    ```

### Running with Docker

1. **Build the Docker image:**

    ```bash
    docker build -t auth-service .
    ```

2. **Run the Docker container:**

    ```bash
    docker run -p 8080:8080 auth-service
    ```

    - You can inject environment variables as needed (see below).

## Configuration

- **Default configuration:** `src/main/resources/application.yml`
- **Profile-specific configuration:** Create `application-<profile>.yml` in `src/main/resources`
- **Override settings:** Use environment variables or system properties

### Common Environment Variables

- `SPRING_PROFILES_ACTIVE` — Spring Boot profile (e.g. `dev`, `prod`)
- `JWT_SECRET` — Secret key for signing JWTs (required in production)
- `PORT` — Service port (default: 8080)

**Example Docker run with environment variables:**

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=mysecret \
  auth-service
```

## REST API Usage Examples

### Register a new user

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "password": "strong-password"
}
```

### Login and receive JWT

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "strong-password"
}
```

- **Success:** Returns a JWT token for authenticated requests.

### Authenticated endpoint example

```http
GET /api/user/me
Authorization: Bearer <JWT_TOKEN>
```

## Running Tests

Run unit and integration tests:

```bash
mvn test
```

## Troubleshooting

- **Port already in use:** Change it with the `PORT` environment variable.
- **JWT errors:** Ensure `JWT_SECRET` is identical between the auth service and clients.
- **Profile/config problems:** Confirm `SPRING_PROFILES_ACTIVE` is set correctly and profile config files exist.

## Contributing

We welcome contributions! To get started:

- Fork the repository and create a feature branch
- Follow Java/Spring Boot best practices
- Add appropriate tests and documentation
- Open a pull request with a clear description of your changes

For significant design changes, please open an issue for discussion first.

## License

Auth Service is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Further Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JWT Introduction](https://jwt.io/introduction/)
