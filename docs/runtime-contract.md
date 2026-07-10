# Auth-Service Runtime Contract

## Purpose

This document is the authoritative runtime integration contract for
auth-service. External consumers such as infrastructure repositories,
deployment automation, CI/CD pipelines, and service integrations should use
this contract instead of inferring runtime behavior from application source
code, Docker configuration, or Kubernetes manifests.

The contract describes externally visible runtime behavior and supported
configuration. It does not describe internal implementation, source layout, or
business logic.

## Stable Integration Surface

External consumers may rely on the following runtime characteristics:

- Service name: `auth-service`.
- Runtime type: Spring Boot HTTP service packaged as an executable JAR.
- Java runtime: Java 21.
- Build tool: Maven.
- Default container entrypoint: `java -jar /app.jar`.
- Default HTTP container port: `8080`.
- Kubernetes service name: `auth-service`.
- Kubernetes service port name: `http`.
- Kubernetes service port: `8080`.
- Public API base path: `/api/auth`.
- Runtime dependencies: PostgreSQL and Keycloak.

The following are not stable integration interfaces unless this document is
updated to include them:

- Internal Java packages, classes, or method names.
- Database schema internals.
- Dockerfile implementation details beyond the documented runtime entrypoint
  and exposed port.
- Kubernetes manifest structure beyond the documented service, ingress,
  dependency, and readiness expectations.
- Profile-specific example hostnames or placeholder credentials.

## Start Contract

### Local Maven Runtime

The supported local development startup path is:

```sh
mvn spring-boot:run
```

The service can also be started from a packaged JAR:

```sh
mvn package
java -jar target/auth-service.jar
```

Local execution requires Java 21 or later and Maven. The local `local` profile
is the repository-supported profile for running against the local
Platform-Infra stack.

### Container Runtime

The container runtime contract is:

```sh
java -jar /app.jar
```

The image expects an executable JAR to be present at build time as
`target/auth-service.jar`. The container exposes HTTP port `8080`.

## Local Development Runtime

For local development with the Platform-Infra stack:

- Use Spring profile `local`.
- Set `SPRING_PROFILES_ACTIVE=local`.
- The service listens on `SERVER_PORT`, defaulting to `8081` for the `local`
  profile.
- PostgreSQL is reached through `AUTH_DB_URL`, defaulting to
  `jdbc:postgresql://localhost:5432/platform` for the `local` profile.
- Keycloak is reached through `AUTH_KEYCLOAK_SERVER_URL`, defaulting to
  `http://localhost:8080` for the `local` profile.
- The local Keycloak realm defaults to `az`.

The repository provides `.env.example` as the local environment template.
Consumers should copy it to `.env`, replace placeholder values with local-only
credentials or secrets, and export those variables before starting the service.
The `.env` file is local-only and must not be committed.

## Required Runtime Dependencies

### PostgreSQL

auth-service requires a reachable PostgreSQL database at startup.

The database connection contract is configured through:

- `AUTH_DB_URL`
- `AUTH_DB_USERNAME`
- `AUTH_DB_PASSWORD`

The default profile expects:

- URL: `jdbc:postgresql://localhost:5432/authdb`
- Username: `auth_user`
- Password: `auth_password`

The local profile expects:

- URL: `jdbc:postgresql://localhost:5432/platform`
- Username supplied by `AUTH_DB_USERNAME`
- Password supplied by `AUTH_DB_PASSWORD`

### Keycloak

auth-service requires a reachable Keycloak server for authentication and user
registration operations.

The Keycloak runtime contract is configured through:

- `AUTH_KEYCLOAK_SERVER_URL`
- `AUTH_KEYCLOAK_REALM`
- `AUTH_KEYCLOAK_CLIENT_ID`
- `AUTH_KEYCLOAK_CLIENT_SECRET`
- `AUTH_KEYCLOAK_ADMIN_REALM`
- `AUTH_KEYCLOAK_ADMIN_CLIENT_ID`
- `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET`
- `AUTH_KEYCLOAK_ADMIN_USERNAME`
- `AUTH_KEYCLOAK_ADMIN_PASSWORD`

Registration requires admin credentials. Consumers must provide either an admin
client secret or an admin username and password. Authentication uses the
configured client ID and optional client secret.

## Configuration Contract

The following environment variables are part of the supported runtime contract.

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the Spring runtime profile. | `default` |
| `SERVER_PORT` | Overrides the HTTP listener port where supported by the active profile. | `8081` in `local`; otherwise profile-defined |
| `AUTH_DB_URL` | PostgreSQL JDBC URL. | `jdbc:postgresql://localhost:5432/authdb` |
| `AUTH_DB_USERNAME` | PostgreSQL username. | `auth_user` |
| `AUTH_DB_PASSWORD` | PostgreSQL password. | `auth_password` |
| `AUTH_KEYCLOAK_SERVER_URL` | Keycloak base URL. | `http://localhost:8080` |
| `AUTH_KEYCLOAK_REALM` | Keycloak realm used for user authentication. | `auth`; `az` in `local` |
| `AUTH_KEYCLOAK_CLIENT_ID` | Keycloak client ID used for login. | `auth-service` |
| `AUTH_KEYCLOAK_CLIENT_SECRET` | Keycloak client secret used for login when the client is confidential. | empty |
| `AUTH_KEYCLOAK_ADMIN_REALM` | Keycloak realm used for admin authentication. | `master` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` | Keycloak admin client ID used for registration. | `auth-service-admin` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` | Keycloak admin client secret used for registration. | empty |
| `AUTH_KEYCLOAK_ADMIN_USERNAME` | Keycloak admin username used for registration when no admin client secret is supplied. | empty |
| `AUTH_KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin password used for registration when no admin client secret is supplied. | empty |

Environment-specific deployments should provide secret values through their
secret-management mechanism rather than committed configuration files.

## Network Contract

### HTTP API

auth-service exposes HTTP endpoints under `/api/auth`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Registers a user through the configured Keycloak realm. |
| `POST` | `/api/auth/login` | Authenticates a user and returns token information from Keycloak. |

Validation and integration failures are returned as JSON error responses with
stable `code` and `message` fields.

### Kubernetes Service

The Kubernetes service contract is:

- Namespace: `auth-service`.
- Service name: `auth-service`.
- Service type: `ClusterIP`.
- Port name: `http`.
- Service port: `8080`.
- Target port: container port named `http`.

In-cluster consumers should route to the `auth-service` Kubernetes service on
port `8080`.

### Kubernetes Ingress

The local Kubernetes ingress contract is:

- Ingress class: `nginx`.
- Host: `auth-service.example.com`.
- TLS secret: `auth-service-tls`.
- Path: `/`.
- Path type: `Prefix`.

The local ingress routes all paths for the host to the `auth-service` service
HTTP port.

Remote environments may use different DNS names, TLS issuers, or secret
management. They should preserve the service HTTP contract unless this document
is updated.

## Readiness and Health Contract

No HTTP actuator health endpoint is currently part of the stable runtime
contract.

The current Kubernetes readiness and liveness contract uses TCP probes against
the HTTP port named `http`:

- Readiness: TCP socket on `http`, initial delay 15 seconds, period 10 seconds.
- Liveness: TCP socket on `http`, initial delay 30 seconds, period 20 seconds.

For local or external automation, readiness can be determined by confirming the
process is running and accepting TCP connections on the configured HTTP port.
An HTTP 404 from the Spring Boot application on an otherwise routed request is
evidence that network routing reached auth-service, because the root path is not
part of the public API contract.

## Deployment Expectations

Deployments must provide:

- A Java 21 compatible runtime or container image.
- PostgreSQL connectivity and credentials.
- Keycloak connectivity, realm, client, and registration credentials.
- An HTTP listener exposed on the configured port.
- Secret management for database passwords and Keycloak client or admin
  credentials.

Kubernetes deployments that pull from GHCR require an image pull secret
compatible with the service account configuration. The repository includes a
credentials-free image pull secret template for manifest validation; real
cluster credentials must be created by deployment automation or external secret
management.

## Compatibility Notes

Future development, staging, and production environments should evolve this
contract before changing externally consumed runtime behavior. Infrastructure
repositories should treat changes to this document as the source of truth for
auth-service runtime integration.
