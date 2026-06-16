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

## Configuration

All environment and profile-specific configuration files are located under `src/main/resources` (e.g., `application.yml`).
You can override configuration by creating profile-specific files (e.g., `application-dev.yml`).

## Kubernetes GHCR Image Pulls

The Kubernetes service account in `k8s/service-account.yaml` references the `ghcr-auth-service-pull` image pull secret. Create the secret in the `auth-service` namespace before deploying manifests that pull `ghcr.io/loctranhoang/auth-service`.

Use a GitHub token with `read:packages` permission, and do not commit the token or generated secret data:

```sh
kubectl create secret docker-registry ghcr-auth-service-pull \
  --namespace auth-service \
  --docker-server=ghcr.io \
  --docker-username=<github-username> \
  --docker-password=<github-token> \
  --docker-email=<email-address> \
  --dry-run=client \
  -o yaml | kubectl apply -f -
```

The committed `k8s/image-pull-secret.yaml` file is a credentials-free template for manifest validation only. Replace it at deploy time with a real cluster secret created by the command above or by your external secret-management process.

## Contribution Guidelines

We welcome contributions! Please open issues or pull requests as needed. See `CONTRIBUTING.md` if available, or contact the maintainers for guidance.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
