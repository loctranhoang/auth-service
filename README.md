# Auth Service

This project implements an authentication service using Java and Spring Boot.

## Features
- Secure authentication endpoints for user login and registration
- Token-based authentication (JWT)
- User role and permission handling
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
Override settings using environment variables or profile-specific YAML files (e.g., `application-prod.yml`).

### Environment Variables
- `SPRING_PROFILES_ACTIVE` — Set the Spring profile (e.g., `dev`, `prod`)
- `JWT_SECRET` — Secret key for signing JWT tokens
- `PORT` — Port to run the service (default: 8080)

These variables can be set directly or via Docker environment flags:
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
  Ensure nothing else is running on the configured port (default 8080) or set a different port using the `PORT` environment variable.
- **JWT authentication errors:**
  Confirm `JWT_SECRET` matches between the service and any clients generating tokens.
- **Profile-specific issues:**
  Check that the correct profile is active (`SPRING_PROFILES_ACTIVE`) and appropriate configuration files are present.

## Contributing

We welcome contributions! To propose changes:
- Fork this repository
- Create a feature branch
- Submit pull requests describing your changes

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for more information.
