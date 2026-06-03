# Auth Service

This project implements a robust authentication service using Java and Spring Boot.

## Features
- Secure authentication endpoints for user login and registration
- Token-based authentication (JWT)
- User role and permission management
- Easy integration with other systems
- Extensible architecture for adding new authentication providers

## Getting Started

### Prerequisites
- Java 17 or above
- Maven 3.6+

### Setup Instructions
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd auth-service
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the service**
   ```bash
   mvn spring-boot:run
   ```
   or
   ```bash
   java -jar target/auth-service-*.jar
   ```

### Running with Docker
1. **Build the Docker image**
   ```bash
   docker build -t auth-service .
   ```
2. **Run the Docker container**
   ```bash
   docker run -p 8080:8080 auth-service
   ```

## Configuration

Default configuration is managed via `src/main/resources/application.yml`.
Override settings using environment variables or profile-specific YAML files (for example, `application-prod.yml`).

### Environment Variables
- `SPRING_PROFILES_ACTIVE` — Set the Spring profile (`dev`, `prod`, etc.)
- `JWT_SECRET` — Secret key for signing JWT tokens (must be set in production)
- `PORT` — Port to run the service (default: 8080)

Set variables in your shell or via Docker flags, for example:
```bash
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e JWT_SECRET=mysecret auth-service
```

## Example Usage

- **Register a new user:**
  ```http
  POST /api/auth/register
  {
    "username": "alice",
    "password": "strong-password"
  }
  ```
- **Login:**
  ```http
  POST /api/auth/login
  {
    "username": "alice",
    "password": "strong-password"
  }
  ```
  Response will include a JWT token for authenticated requests.

## Running Tests

Run all tests:
```bash
mvn test
```

## Troubleshooting

- **Port already in use:**
  Make sure nothing else is listening on the configured port (default 8080), or specify a different port using the `PORT` environment variable.
- **JWT authentication errors:**
  Ensure the `JWT_SECRET` value matches between auth-service and clients generating tokens.
- **Profile-specific issues:**
  Confirm the proper profile is active (`SPRING_PROFILES_ACTIVE`) and the relevant configuration files exist in `src/main/resources`.

## Contributing

We welcome contributions! To propose changes:
- Fork this repository
- Create a feature branch
- Submit a pull request describing your changes

Follow standard Java/Spring Boot development guidelines. Please include appropriate tests and documentation for your changes.

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for more information.
