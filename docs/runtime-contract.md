# Auth-Service Runtime Contract

This document is the repository-owned runtime integration contract for
auth-service. External consumers, deployment automation, CI/CD pipelines, and
infrastructure repositories should use this contract instead of inferring
runtime behavior from source code or container configuration.

The contract describes the stable runtime interface of auth-service. It does
not describe internal implementation, source layout, persistence models, or
business logic.

## Contract Status

- Service name: `auth-service`
- Runtime type: Spring Boot HTTP service packaged as an executable Java JAR
- Java runtime: Java 21
- Build tool: Maven
- Artifact identity: `com.az.auth:auth-service`
- Packaged JAR name: `target/auth-service.jar`
- Container entrypoint: `java -jar /app.jar`
- Default service port: `8080`
- Current local development profile: `local`

The following are stable integration interfaces unless this document is updated
in the same change that modifies them:

- Process startup contract
- Required runtime dependencies
- Environment variable configuration contract
- HTTP network port and Kubernetes service port names
- Public HTTP authentication routes
- Readiness and liveness signaling expectations

## Startup Contract

auth-service starts as an executable Spring Boot JAR.

Supported startup forms:

```sh
mvn spring-boot:run
```

```sh
mvn package
java -jar target/auth-service.jar
```

The container image expects the built JAR at `target/auth-service.jar` during
image build time and starts it as `/app.jar`.

```sh
docker build -t auth-service .
docker run --rm -p 8080:8080 auth-service
```

The service reads runtime configuration from Spring Boot property sources,
including environment variables. External runtime configuration should be
supplied by the deployment environment; secrets must not be committed.

## Local Development Runtime

The current supported local development contract uses the `local` Spring
profile and is represented by `.env.example`.

Local runtime defaults:

| Setting | Local value |
| --- | --- |
| Spring profile | `local` |
| HTTP port | `${SERVER_PORT:8081}` |
| PostgreSQL URL | `${AUTH_DB_URL:jdbc:postgresql://localhost:5432/platform}` |
| Keycloak URL | `${AUTH_KEYCLOAK_SERVER_URL:http://localhost:8080}` |
| Keycloak realm | `${AUTH_KEYCLOAK_REALM:az}` |
| Keycloak client ID | `${AUTH_KEYCLOAK_CLIENT_ID:auth-service}` |
| Keycloak admin realm | `${AUTH_KEYCLOAK_ADMIN_REALM:master}` |
| Keycloak admin client ID | `${AUTH_KEYCLOAK_ADMIN_CLIENT_ID:auth-service-admin}` |

For local development, copy `.env.example` to `.env`, replace placeholder
values with local-only credentials, export the variables into the shell or IDE,
and start the service with the `local` profile.

```powershell
Copy-Item .env.example .env
```

```sh
mvn spring-boot:run
```

Local consumers should reach auth-service at `http://localhost:8081` unless
`SERVER_PORT` is overridden.

## Runtime Dependencies

auth-service requires these external runtime dependencies:

| Dependency | Purpose | Required for |
| --- | --- | --- |
| PostgreSQL | Application datasource | Application startup and persistence |
| Keycloak | User registration and token authentication | Authentication HTTP routes |

The service expects PostgreSQL to be reachable through `AUTH_DB_URL` and expects
the supplied credentials to be valid for that database.

The service expects Keycloak to expose the configured realm and clients. User
registration requires either admin client credentials or admin username/password
credentials. User login requires a configured authentication client in the
application realm.

## Configuration Contract

Configuration is supplied through environment variables. Values shown as
defaults are the currently supported defaults, not a substitute for production
secret management.

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the runtime profile | `default` |
| `SERVER_PORT` | Overrides the HTTP port for the `local` profile | `8081` in `local` |
| `AUTH_DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/authdb`; `jdbc:postgresql://localhost:5432/platform` in `local` |
| `AUTH_DB_USERNAME` | PostgreSQL username | `auth_user`; empty in `local` |
| `AUTH_DB_PASSWORD` | PostgreSQL password | `auth_password`; empty in `local` |
| `AUTH_KEYCLOAK_SERVER_URL` | Keycloak base URL | `http://localhost:8080` |
| `AUTH_KEYCLOAK_REALM` | Application Keycloak realm | `auth`; `az` in `local` |
| `AUTH_KEYCLOAK_CLIENT_ID` | Application Keycloak client ID | `auth-service` |
| `AUTH_KEYCLOAK_CLIENT_SECRET` | Optional application client secret | Empty |
| `AUTH_KEYCLOAK_ADMIN_REALM` | Keycloak admin authentication realm | `master` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` | Keycloak admin client ID | `auth-service-admin` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` | Admin client secret for registration | Empty |
| `AUTH_KEYCLOAK_ADMIN_USERNAME` | Admin username for registration | Empty |
| `AUTH_KEYCLOAK_ADMIN_PASSWORD` | Admin password for registration | Empty |

Required secret-bearing values for a deployed runtime:

- `AUTH_DB_USERNAME`
- `AUTH_DB_PASSWORD`
- `AUTH_KEYCLOAK_CLIENT_SECRET` when the configured Keycloak client is
  confidential
- `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` when using admin client credentials
- `AUTH_KEYCLOAK_ADMIN_USERNAME` and `AUTH_KEYCLOAK_ADMIN_PASSWORD` when using
  admin username/password credentials instead of an admin client secret

At least one Keycloak admin authentication method must be configured for user
registration:

- `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET`; or
- both `AUTH_KEYCLOAK_ADMIN_USERNAME` and `AUTH_KEYCLOAK_ADMIN_PASSWORD`.

## Network Contract

The service exposes HTTP.

| Runtime | Addressing contract |
| --- | --- |
| Default process runtime | `http://<host>:8080` |
| Local profile runtime | `http://localhost:${SERVER_PORT}`, default `http://localhost:8081` |
| Container runtime | Container port `8080` |
| Kubernetes Service | Service `auth-service`, namespace `auth-service`, port name `http`, port `8080` |
| Kubernetes Ingress | Host `auth-service.example.com`, path prefix `/`, TLS secret `auth-service-tls`, ingress class `nginx` |

Kubernetes deployments should route traffic to the named container port `http`.
The current base Kubernetes service is a `ClusterIP` service.

## HTTP Interface

The externally visible authentication HTTP routes are:

| Method | Path | Purpose | Success status |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user through the configured Keycloak realm | `201 Created` |
| `POST` | `/api/auth/login` | Authenticate a user and return token data from Keycloak | `200 OK` |

Registration request body:

```json
{
  "username": "user-name",
  "email": "user@example.com",
  "firstName": "Given",
  "lastName": "Family",
  "password": "secret"
}
```

Registration success body:

```json
{
  "userId": "keycloak-user-id",
  "username": "user-name",
  "email": "user@example.com"
}
```

Login request body:

```json
{
  "username": "user-name",
  "password": "secret"
}
```

Login success body:

```json
{
  "tokenType": "Bearer",
  "accessToken": "access-token",
  "expiresIn": 300,
  "refreshExpiresIn": 1800,
  "refreshToken": "refresh-token",
  "idToken": "id-token",
  "scope": "openid profile email",
  "sessionState": "session-id"
}
```

Error responses use this shape:

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

Current error codes exposed by the service:

| HTTP status | Code |
| --- | --- |
| `400 Bad Request` | `INVALID_REQUEST` |
| `401 Unauthorized` | `INVALID_CREDENTIALS` |
| `409 Conflict` | `USER_ALREADY_EXISTS` |
| `500 Internal Server Error` | `KEYCLOAK_CONFIGURATION_ERROR` |
| `502 Bad Gateway` | `KEYCLOAK_INTEGRATION_ERROR` |

## Health and Readiness Contract

The current runtime contract does not include a dedicated HTTP health endpoint.

Kubernetes readiness and liveness currently use TCP socket checks against the
named HTTP port:

- Readiness: TCP socket on port `http`, initial delay 15 seconds, period 10
  seconds
- Liveness: TCP socket on port `http`, initial delay 30 seconds, period 20
  seconds

External infrastructure may use successful TCP connection establishment to the
service HTTP port as the current readiness signal. HTTP actuator endpoints such
as `/actuator/health` are not part of the current contract.

## Environment Profiles

The contract is designed to support multiple environments, but only the local
runtime is fully documented as a supported developer workflow here.

Current profile names recognized by repository configuration:

- `default`
- `local`
- `dev`
- `staging`
- `prod`
- `test`

Non-local deployment environments should supply environment-specific endpoints,
credentials, secrets, DNS, TLS, and Kubernetes integration outside this
documented base contract. Any future environment promoted to a supported
integration target must update this document with its runtime contract.

## Stable Integration Boundaries

External consumers may rely on:

- The service being startable as a Java 21 executable JAR
- The container listening on port `8080`
- Local profile development defaulting to port `8081`
- PostgreSQL and Keycloak being required external dependencies
- The documented environment variable names
- The Kubernetes service name, namespace, and port name in the base manifests
- The documented authentication HTTP routes and response shapes
- TCP socket readiness/liveness as the current health mechanism

External consumers must not rely on:

- Internal Java classes, packages, methods, or source layout
- Database schema details not published in a separate schema contract
- Undocumented Spring Boot properties
- Actuator endpoints
- Placeholder credentials or example profile values as production defaults
- Dockerfile or Kubernetes manifest internals beyond the stable fields listed
  in this contract
