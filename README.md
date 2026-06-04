# Auth Service

A robust authentication service built with Java and Spring Boot. This project delivers secure endpoints for user authentication, registration, and authorization, using JWT (JSON Web Token) for token-based security and extensible role/permission management.

## Features
- Secure user login and registration endpoints
- JWT token-based authentication and authorization
- Role- and permission-based access control
- Easy integration with other systems and microservices
- Extensible architecture for adding authentication providers
- Environment-based configuration support

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or newer

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
    # Or
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

## Configuration
- Default settings: `src/main/resources/application.yml`
- Override with environment variables or `application-<profile>.yml`

### Common Environment Variables
- `SPRING_PROFILES_ACTIVE` — Set active profile (e.g., `dev`, `prod`)
- `JWT_SECRET` — Secret key for JWT signing (required in production)
- `PORT` — Service port (default: 8080)

Example with Docker environment variables:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=mysecret \
  auth-service
```

## Example API Usage

- **Register a new user:**
    ```http
    POST /api/auth/register
    Content-Type: application/json
    {
      "username": "alice",
      "password": "strong-password"
    }
    ```
- **User Login:**
    ```http
    POST /api/auth/login
    Content-Type: application/json
    {
      "username": "alice",
      "password": "strong-password"
    }
    ```
    - **Response:** JWT token included in the response for authenticated requests.

## Running Tests

Run all tests:
```bash
mvn test
```

## Troubleshooting
- **Port in use:** Change the port via the `PORT` environment variable if 8080 is unavailable.
- **JWT errors:** Ensure `JWT_SECRET` matches between auth-service and clients.
- **Profile errors:** Confirm `SPRING_PROFILES_ACTIVE` is correctly set and configuration files exist.

## Contributing

Contributions are welcome! To propose changes:
- Fork the repo and create a feature branch
- Follow Java/Spring Boot best practices
- Add tests and documentation
- Submit a pull request describing your changes

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for details.
