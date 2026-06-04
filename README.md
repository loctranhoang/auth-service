# Auth Service

The **Auth Service** is a secure authentication and authorization microservice built with Java 17+ and Spring Boot. It provides endpoints and core logic for verifying user credentials, generating and validating JWT tokens, and enforcing role-based access control. This service is suitable for modern, containerized microservice architectures.

## Features
- Secure user authentication
- JWT token issuance and validation
- Role-based access control
- Configurable via environment profiles
- RESTful APIs for common auth flows

## Requirements
- Java 17 or higher
- Maven 3.8+

## Project Structure
- **src/main/java/com/az/auth/**: Application source
- **src/test/java/com/az/auth/**: Tests (unit, integration)

## Getting Started

1. **Clone the repository:**
    ```sh
    git clone <repository-url>
    cd auth-service
    ```
2. **Build the project:**
    ```sh
    mvn clean install
    ```
3. **Run the application:**
    ```sh
    mvn spring-boot:run
    ```
4. **Access the API:**
    The service runs at [http://localhost:8080](http://localhost:8080) by default.

## Configuration
Configuration defaults are in `src/main/resources/application.yml`. Environment/profile-specific overrides can be placed in files like `application-dev.yml`.

Common configuration properties include:
- JWT secret, expiration, and header
- Server port and context path
- Datasource settings if using persistent storage

You may set Java system properties or environment variables as needed.

## Usage Example
To authenticate a user, send a POST request with username and password to the login endpoint:

**Request:**
```
POST /auth/login
Content-Type: application/json
{
  "username": "user",
  "password": "password"
}
```

**Example using cURL:**
```sh
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "user", "password": "password"}'
```

**Response:**
- On success: A JWT token is returned in the response body.

To access a protected endpoint, provide the token:
```sh
curl -H "Authorization: Bearer <token>" http://localhost:8080/protected-endpoint
```

## Development
- Follow the project's code style and contribution guidelines.
- Run tests with:
    ```sh
    mvn test
    ```
- Pull requests and issue reports are welcome.

## Version
- Current version: 1.0.0

## License
This project is licensed under the MIT License.
