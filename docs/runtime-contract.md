# Auth-Service Runtime Contract

## Purpose

This document is the authoritative runtime integration contract for
`auth-service`. External consumers such as infrastructure repositories,
deployment automation, CI/CD pipelines, and other services should use this
contract to integrate with `auth-service` instead of inferring runtime behavior
from application source code or container configuration.

The contract describes the stable runtime interface of the service. It does not
document implementation details, source code structure, or internal business
logic.

## Stable Runtime Interface

External consumers may rely on the following integration surface:

- `auth-service` is a Spring Boot HTTP service packaged as an executable Java
  archive.
- The service runs on Java 21.
- The container image starts the service with `java -jar /app.jar`.
- The service listens for HTTP traffic on the configured Spring Boot server
  port.
- The default server port is `8080`.
- The local development profile listens on port `8081` unless `SERVER_PORT` is
  supplied.
- The Kubernetes service exposes the application through a named `http` port on
  port `8080`.
- The current Kubernetes readiness and liveness contract is TCP reachability on
  the application HTTP port.
- The externally visible authentication API is rooted at `/api/auth`.

Values not listed in this document are not part of the runtime contract unless
they are added here in a future change.

## Runtime Startup Contract

The service can be started by either:

- Running the Spring Boot application with Maven:

  ```sh
  mvn spring-boot:run
  ```

- Running a packaged Java archive:

  ```sh
  mvn package
  java -jar target/auth-service.jar
  ```

- Running the repository-owned container image contract:

  ```sh
  java -jar /app.jar
  ```

Container runtimes must provide a Java 21 compatible runtime. The repository
Dockerfile currently uses an Eclipse Temurin Java 21 base image and exposes
container port `8080`.

## Local Development Runtime

The supported local development contract uses the `local` Spring profile:

```sh
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8081
```

With the `local` profile, the service expects:

- PostgreSQL reachable at `AUTH_DB_URL`, defaulting to
  `jdbc:postgresql://localhost:5432/platform`.
- Keycloak reachable at `AUTH_KEYCLOAK_SERVER_URL`, defaulting to
  `http://localhost:8080`.
- Keycloak realm `az` unless `AUTH_KEYCLOAK_REALM` is supplied.
- HTTP traffic to reach the service on `http://localhost:8081` unless
  `SERVER_PORT` is supplied.

The repository `.env.example` file is the local environment template. Copy it to
an uncommitted `.env`, replace placeholder values with local credentials, and
export those variables before starting the service.

## Dependencies

The service requires the following runtime dependencies:

| Dependency | Required For | Contract |
| --- | --- | --- |
| PostgreSQL | Application startup and persistence access | A reachable PostgreSQL database must be supplied through the datasource configuration. |
| Keycloak | Authentication, token issuance, and user registration integration | A reachable Keycloak server, realm, application client, and admin credentials must be supplied through the Keycloak configuration. |

No Kafka, Redis, message broker, object store, or cache dependency is part of
the current runtime contract.

## Configuration Contract

The service is configured with environment variables resolved by Spring Boot.
Deployment systems may provide equivalent Spring configuration, but environment
variables are the portable contract for external automation.

### Core Runtime

| Variable | Purpose | Default | Required For |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the active runtime profile. | `default` | Profile-specific runtime behavior |
| `SERVER_PORT` | Overrides the HTTP listener port where supported by the active profile. | `8081` in `local`; otherwise profile-defined or `8080` | Local development port override |

### PostgreSQL

| Variable | Purpose | Default | Required For |
| --- | --- | --- | --- |
| `AUTH_DB_URL` | JDBC URL for the PostgreSQL database. | `jdbc:postgresql://localhost:5432/authdb`; `local` defaults to `jdbc:postgresql://localhost:5432/platform` | Runtime startup |
| `AUTH_DB_USERNAME` | PostgreSQL username. | `auth_user`; empty in `local` unless supplied | Runtime startup |
| `AUTH_DB_PASSWORD` | PostgreSQL password. | `auth_password`; empty in `local` unless supplied | Runtime startup |

### Keycloak

| Variable | Purpose | Default | Required For |
| --- | --- | --- | --- |
| `AUTH_KEYCLOAK_SERVER_URL` | Base URL for the Keycloak server. | `http://localhost:8080` | Login and registration |
| `AUTH_KEYCLOAK_REALM` | Realm used for application authentication. | `auth`; `local` defaults to `az` | Login and registration |
| `AUTH_KEYCLOAK_CLIENT_ID` | Keycloak client used for login. | `auth-service` | Login |
| `AUTH_KEYCLOAK_CLIENT_SECRET` | Secret for the login client when the client is confidential. | Empty | Login when the Keycloak client requires a secret |
| `AUTH_KEYCLOAK_ADMIN_REALM` | Realm used by the administrative Keycloak client. | `master` | Registration |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` | Keycloak administrative client ID. | `auth-service-admin` | Registration |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` | Secret for client-credentials based administrative access. | Empty | Registration when using admin client credentials |
| `AUTH_KEYCLOAK_ADMIN_USERNAME` | Username for password-grant based administrative access. | Empty | Registration when admin client secret is not supplied |
| `AUTH_KEYCLOAK_ADMIN_PASSWORD` | Password for password-grant based administrative access. | Empty | Registration when admin client secret is not supplied |

Registration requires either:

- `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET`; or
- both `AUTH_KEYCLOAK_ADMIN_USERNAME` and `AUTH_KEYCLOAK_ADMIN_PASSWORD`.

Secrets must be supplied by the environment, orchestrator, or secret-management
system. They must not be committed to this repository.

## Network Contract

### Local HTTP

For local development with `SPRING_PROFILES_ACTIVE=local`, the default service
base URL is:

```text
http://localhost:8081
```

If `SERVER_PORT` is supplied, the base URL must use that port.

### Container HTTP

The container image exposes application port `8080`. Container orchestrators
should route HTTP traffic to the configured application port.

### Kubernetes

The repository Kubernetes contract exposes:

- Namespace: `auth-service`
- Workload: `Deployment/auth-service`
- Service: `Service/auth-service`
- Service type: `ClusterIP`
- Service port: named `http`, port `8080`
- Target port: named container port `http`
- Ingress class: `nginx`
- Ingress host: `auth-service.example.com`
- TLS secret name: `auth-service-tls`
- Ingress path: `/` with `Prefix` matching

Remote environments may provide environment-specific DNS, TLS certificates,
image pull credentials, and ingress infrastructure. The application expects
traffic to arrive as HTTP on its configured server port after any ingress or
load-balancer termination.

## HTTP Interface Contract

The current externally visible authentication API is:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user through the configured Keycloak realm. |
| `POST` | `/api/auth/login` | Exchange user credentials for a Keycloak token response. |

The service does not currently publish a root resource contract at `/`.

## Health and Readiness Contract

The current runtime readiness contract is TCP reachability on the configured
HTTP port.

In Kubernetes, both probes use TCP checks against the named `http` port:

- Readiness probe: TCP socket on `http`
- Liveness probe: TCP socket on `http`

No HTTP health endpoint is part of the current runtime contract. External
platforms that require HTTP health checks must either use TCP checks or add a
future contract change before depending on an HTTP health URL.

## Environment Contract Status

| Environment | Contract Status |
| --- | --- |
| Local development | Supported and documented by this contract. |
| Kubernetes base manifests | Repository-owned base contract for deployment integration. |
| Development, staging, production profiles | Present as profile-specific configuration, but environment-owned values, secrets, DNS, TLS, image pull credentials, and infrastructure must be provided outside this contract unless explicitly added here. |

## Change Control

Changes that alter startup behavior, required dependencies, required
configuration, exposed network ports, health signaling, or externally visible
API paths must update this document in the same change set.
