# Auth Service

This project is an authentication service built with Java and Spring Boot. It provides basic authentication and authorization functionalities typically required in modern microservice architectures.

## Features
- User authentication
- JWT token generation and validation
- Role-based access control

## Requirements
- Java 17 or later
- Maven 3.8+

## Project Structure
- **src/main/java/com/az/auth**: Main source code for the Spring Boot application
- **src/test/java/com/az/auth**: Unit and integration tests

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
   The service runs on [http://localhost:8080](http://localhost:8080) by default.
   
## Configuration
Application configurations can be set via `src/main/resources/application.yml` or by specifying profile-based files such as `application-dev.yml`.

## Usage Example
To authenticate a user, send a POST request to `/auth/login` with user credentials. You will receive a JWT token on success.

```json
{
  "username": "user",
  "password": "password"
}
```

Example cURL:
```sh
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "user", "password": "password"}'
```

The token can be used for subsequent authenticated requests:
```sh
curl -H "Authorization: Bearer <token>" http://localhost:8080/protected-endpoint
```

## Development
- Make sure to follow the existing [code style](#) and contribution guidelines.
- Run tests using:
   ```sh
   mvn test
   ```

## Version
- Current version: 1.0.0

## License
This project is licensed under the MIT License.
