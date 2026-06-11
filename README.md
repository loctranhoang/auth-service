# Auth Service

A Spring Boot-based authentication service for managing authorization and user authentication within your application ecosystem.

## Project Overview

The Auth Service provides essential authentication and authorization functionalities for microservice architectures.

- **Language:** Java
- **Framework:** Spring Boot
- **Repository:** `auth-service`

## Features

- User registration and login endpoints
- JWT-based authentication
- Secure password handling
- Role-based access control
- Extensible design for integration with other services

## Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.8.x or later

### Building the Project

Clone the repository and build using Maven:

```sh
git clone https://github.com/your-org/auth-service.git
cd auth-service
mvn clean install
```

### Running the Service

To start the service locally:

```sh
mvn spring-boot:run
```

Or build a JAR and run it:

```sh
mvn package
java -jar target/auth-service-*.jar
```

By default, the service will be available at `http://localhost:8080`.

## Usage Example

Typical authentication request (example endpoint):

```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "youruser",
  "password": "yourpassword"
}
```

A successful response will include a JWT token for use in subsequent requests.

## Testing

Run all tests using Maven:

```sh
mvn test
```

## Kubernetes Deployment

Production Kubernetes manifests are in `k8s/production` and can be applied with:

```sh
kubectl apply -k k8s/production
```

Before deploying, create these production-only Kubernetes secrets in the `auth-service` namespace:

- `auth-service-db` with `url`, `username`, and `password` keys.
- `ghcr-pull-secret` for pulling the private GHCR image, if the image is not public.

The production GitHub Actions deployment workflow expects `PROD_KUBE_CONFIG` to contain a base64-encoded kubeconfig. It applies the manifests, updates the `auth-service` deployment image to the image built for the current workflow run, and waits for the rollout to complete.

## Configuration

All environment and profile-specific configuration files are located under `src/main/resources` (e.g., `application.yml`).
You can override configuration by creating profile-specific files (e.g., `application-dev.yml`).

## Contribution Guidelines

We welcome contributions! Please open issues or pull requests as needed. See `CONTRIBUTING.md` if available, or contact the maintainers for guidance.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
