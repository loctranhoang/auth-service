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

## Contributing

We welcome contributions! To propose changes:
- Fork this repository
- Create a feature branch
- Submit pull requests describing your changes

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for more information.
